package org.komapper.extension.validator

import kotlin.reflect.KProperty1

/**
 * Base class for defining validation schemas for objects.
 *
 * ObjectSchema allows you to define validation rules for each property of an object,
 * as well as object-level constraints that validate relationships between properties.
 *
 * Example usage:
 * ```kotlin
 * data class User(val name: String, val age: Int)
 *
 * object UserSchema : ObjectSchema<User>() {
 *     val name = User::name { it.min(1).max(50) }
 *     val age = User::age { it.min(0).max(120) }
 * }
 *
 * // Validate an object
 * val result = UserSchema.tryValidate(User("Alice", 30))
 * ```
 *
 * Object-level constraints:
 * ```kotlin
 * data class Period(val startDate: LocalDate, val endDate: LocalDate)
 *
 * object PeriodSchema : ObjectSchema<Period>({
 *     constrain("dateRange") {
 *         satisfies(it.input.startDate <= it.input.endDate, "Start date must be before end date")
 *     }
 * }) {
 *     val startDate = Period::startDate { it }
 *     val endDate = Period::endDate { it }
 * }
 * ```
 *
 * Object construction with validation:
 * ```kotlin
 * object PersonSchema : ObjectSchema<Person>() {
 *     private val nameV = Person::name { it.min(1) }
 *     private val ageV = Person::age { it.min(0) }
 *
 *     fun bind(name: String, age: Int) = factory {
 *         create(::Person, nameV.bind(name), ageV.bind(age))
 *     }
 * }
 *
 * // Validate and construct an object
 * val result = PersonSchema.bind("Alice", 30).tryCreate()
 * ```
 *
 * @param T The type of object to validate
 * @param block Lambda for defining object-level constraints
 */
open class ObjectSchema<T : Any> private constructor(
    private val ruleMap: MutableMap<String, Rule>,
    private val block: ObjectSchemaScope<T>.() -> Unit = {},
) : Validator<T, T> {
    /**
     * Creates an ObjectSchema with optional object-level constraints.
     *
     * @param block Lambda for defining constraints that validate relationships between properties
     */
    constructor(block: ObjectSchemaScope<T>.() -> Unit = {}) : this(mutableMapOf(), block)

    override fun execute(
        input: T,
        context: ValidationContext,
    ): ValidationResult<T> {
        val constraints: MutableList<Constraint<T>> = mutableListOf()
        block(ObjectSchemaScope(constraints))
        val klass = input::class
        val rootName = klass.qualifiedName ?: klass.simpleName ?: klass.toString()
        val context = context.addRoot(rootName, input)
        val ruleResult = applyRules(input, context, ruleMap)
        val constraintResult = applyConstraints(input, context, constraints)
        if (context.failFast && ruleResult.isFailure()) {
            return ruleResult
        }
        if (context.failFast && constraintResult.isFailure()) {
            return constraintResult
        }
        return ruleResult + constraintResult
    }

    private fun applyRules(
        input: T,
        context: ValidationContext,
        ruleMap: Map<String, Rule>,
    ): ValidationResult<T> {
        val results = mutableListOf<ValidationResult<T>>()
        for ((key, rule) in ruleMap) {
            val result = applyRule(input, context, key, rule)
            if (result.isFailure() && context.failFast) {
                return result
            }
            results.add(result)
        }
        return results.fold(ValidationResult.Success(input, context), ValidationResult<T>::plus)
    }

    private fun applyRule(
        input: T,
        context: ValidationContext,
        key: String,
        rule: Rule,
    ): ValidationResult<T> {
        val value = rule.transform(input)
        val validator = rule.choose(input)
        val pathResult = context.addPathChecked(key, value)
        return when (pathResult) {
            is ValidationResult.Success -> {
                when (val result = validator.execute(pathResult.value, pathResult.context)) {
                    is ValidationResult.Success -> ValidationResult.Success(input, result.context)
                    is ValidationResult.Failure -> ValidationResult.Failure(Input.Some(input), result.messages)
                }
            }
            // If circular reference detected, terminate validation early with success
            is ValidationResult.Failure -> ValidationResult.Success(input, context)
        }
    }

    private fun applyConstraints(
        input: T,
        context: ValidationContext,
        constraints: List<Constraint<T>>,
    ): ValidationResult<T> {
        val validator =
            constraints
                .map { ConstraintValidator(it) }
                .fold(Validator.success<T>() as IdentityValidator<T>) { acc, v -> acc + v }
        return validator.execute(input, context)
    }

    /**
     * Replaces the validator for a specific property.
     *
     * This allows you to override the validation rule for a property after the schema has been defined.
     *
     * @param key The property to replace the validator for
     * @param validator The new validator to use
     * @return A new ObjectSchema with the replaced validator
     */
    fun <V> replace(
        key: KProperty1<T, V>,
        validator: IdentityValidator<V>,
    ): ObjectSchema<T> {
        val rule =
            Rule(
                transform = { receiver: T -> key.get(receiver) } as (Any?) -> Any?,
                choose = { _: T -> validator } as (Any?) -> IdentityValidator<Any?>,
            )
        val newRuleMap = ruleMap.toMutableMap()
        newRuleMap.replace(key.name, rule)
        return ObjectSchema(newRuleMap)
    }

    /**
     * Defines a validation rule for a property.
     *
     * This operator function allows you to use the invoke syntax to define validators
     * for object properties. The lambda receives a base validator as a parameter (`it`),
     * which can be used to build the validator chain.
     *
     * Example:
     * ```kotlin
     * object UserSchema : ObjectSchema<User>() {
     *     val name = User::name { it.min(1).max(50) }
     *     // The lambda parameter 'it' is a base validator for the property type
     *     // The property 'name' gets the returned validator
     * }
     * ```
     *
     * @param block Lambda that receives a base validator and creates the validator for this property
     * @return The validator created by the block
     */
    operator fun <V, VALIDATOR : Validator<V, V>> KProperty1<T, V>.invoke(block: (Validator<V, V>) -> VALIDATOR): VALIDATOR {
        val validator = block(Validator.success())
        addRule(this) { _ -> validator }
        return validator
    }

    /**
     * Defines a conditional validation rule for a property based on the object's state.
     *
     * This allows you to choose different validators based on the value of other properties.
     * The lambda receives the object instance and a base validator as parameters, enabling
     * you to build different validator chains based on the object's state.
     *
     * Example:
     * ```kotlin
     * data class Address(val country: String, val postalCode: String)
     *
     * object AddressSchema : ObjectSchema<Address>() {
     *     val country = Address::country { it }
     *     val postalCode = Address::postalCode choose { address, v ->
     *         when (address.country) {
     *             "US" -> v.length(5)
     *             "CA" -> v.length(6)
     *             else -> v.min(1)
     *         }
     *     }
     * }
     * ```
     *
     * @param resolve Lambda that receives the object and a base validator, and chooses a validator
     * @return The resolution function for further use
     */
    infix fun <V, VALIDATOR : IdentityValidator<V>> KProperty1<T, V>.choose(resolve: (T, Validator<V, V>) -> VALIDATOR): (T) -> VALIDATOR {
        val chooser = { receiver: T -> resolve(receiver, Validator.success()) }
        addRule(this, chooser)
        return chooser
    }

    /**
     * Defines a conditional validation rule for a property based on a selected value from the object.
     *
     * This overload of `choose` allows you to select a specific value from the object first,
     * then use that value to determine which validator to apply. This is useful when you want to
     * base validation decisions on a computed or derived value rather than the entire object.
     *
     * Example:
     * ```kotlin
     * data class Product(val category: String, val price: Double, val taxRate: Double)
     *
     * object ProductSchema : ObjectSchema<Product>() {
     *     val category = Product::category { it }
     *     val price = Product::price choose(
     *         select = { it.taxRate },
     *         resolve = { taxRate, v ->
     *             when {
     *                 taxRate > 0.1 -> v.min(100.0)  // High tax items must be at least $100
     *                 else -> v.min(10.0)             // Low tax items must be at least $10
     *             }
     *         }
     *     )
     * }
     * ```
     *
     * @param select Lambda that extracts a value from the object to base the validator choice on
     * @param resolve Lambda that receives the selected value and a base validator, and returns a validator
     * @return The resolution function for further use
     */
    fun <V, VALIDATOR : IdentityValidator<V>, S> KProperty1<T, V>.choose(
        select: (T) -> S,
        resolve: (S, Validator<V, V>) -> VALIDATOR,
    ): (S) -> VALIDATOR {
        val provide = { value: S -> resolve(value, Validator.success()) }
        val choose = { receiver: T -> provide(select(receiver)) }
        addRule(this, choose)
        return provide
    }

    private fun <T, V> addRule(
        key: KProperty1<T, V>,
        choose: (T) -> Validator<V, V>,
    ) {
        val transform = { receiver: T -> key.get(receiver) }
        transform as (Any?) -> Any?
        choose as (Any?) -> IdentityValidator<Any?>
        ruleMap[key.name] = Rule(transform, choose)
    }
}

internal data class Rule(
    val transform: (Any?) -> Any?,
    val choose: (Any?) -> IdentityValidator<Any?>,
)

/**
 * Scope for defining object-level constraints within an ObjectSchema.
 *
 * This scope is provided in the constructor lambda of ObjectSchema, allowing you to define
 * validation rules that apply to the entire object rather than individual properties.
 * This is useful for validating relationships between properties.
 *
 * Example:
 * ```kotlin
 * data class Period(val startDate: LocalDate, val endDate: LocalDate)
 *
 * object PeriodSchema : ObjectSchema<Period>({
 *     constrain("dateRange") {
 *         satisfies(it.input.startDate <= it.input.endDate, "Start date must be before or equal to end date")
 *     }
 * }) {
 *     val startDate = Period::startDate { it }
 *     val endDate = Period::endDate { it }
 * }
 * ```
 *
 * @param T The type of object being validated
 */
class ObjectSchemaScope<T : Any> internal constructor(
    private val constraints: MutableList<Constraint<T>>,
) {
    /**
     * Defines an object-level constraint.
     *
     * @param id Unique identifier for this constraint (used in error messages)
     * @param check Lambda that performs the validation using ConstraintScope
     */
    fun constrain(
        id: String,
        check: ConstraintScope<T>.(ConstraintContext<T>) -> ConstraintResult,
    ) {
        constraints.add(Constraint(id, check))
    }
}
