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
 * object UserSchema : ObjectSchema<User>({
 *     User::name { it.min(1).max(50) }
 *     User::age { it.min(0).max(120) }
 * })
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
 *     Period::startDate { it.min(LocalDate.of(2025, 1, 1)) }
 *     Period::endDate { it.max(LocalDate.of(2025, 12, 31)) }
 *     constrain("dateRange") {
 *         satisfies(it.input.startDate <= it.input.endDate, "Start date must be before end date")
 *     }
 * })
 * ```
 *
 * @param T The type of object to validate
 * @param block Lambda for defining object-level constraints
 */
open class ObjectSchema<T : Any>(
    private val block: ObjectSchemaScope<T>.() -> Unit = {},
) : Validator<T, T> {
    private val constraints: List<Constraint<T>>
    private val ruleMap: Map<KProperty1<T, *>, Rule>

    init {
        val constraints: MutableList<Constraint<T>> = mutableListOf()
        val ruleMap: MutableMap<KProperty1<T, *>, Rule> = mutableMapOf()
        block(ObjectSchemaScope(this, constraints, ruleMap))
        this.constraints = constraints
        this.ruleMap = ruleMap
    }

    override fun execute(
        input: T,
        context: ValidationContext,
    ): ValidationResult<T> {
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
        ruleMap: Map<KProperty1<T, *>, Rule>,
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
        key: KProperty1<T, *>,
        rule: Rule,
    ): ValidationResult<T> {
        val value = rule.transform(input)
        val validator = rule.choose(input)
        val pathResult = context.addPathChecked(key.name, value)
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
                .fold(Validator.success<T>()) { acc, v -> acc + v }
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
        val choose = { _: T -> validator }
        return ObjectSchema({
            constraints.forEach { constrain(it.id, it.check) }
            ruleMap.forEach { addRule(it.key, it.value.choose) }
            addRule(key, choose)
        })
    }

    /**
     * Composes this schema with another schema using AND logic (operator form).
     *
     * This operator is equivalent to calling [and]. The validation succeeds only if both schemas
     * are satisfied. If either schema fails, the validation fails and returns all error messages
     * from both schemas.
     *
     * Example:
     * ```kotlin
     * val schemaA = object : ObjectSchema<User>({
     *     User::name { it.min(1).max(10) }
     * }) {}
     *
     * val schemaB = object : ObjectSchema<User>({
     *     User::id { it.min(1) }
     * }) {}
     *
     * val combined = schemaA + schemaB
     * // Validation succeeds only if both name and id constraints are satisfied
     * ```
     *
     * @param other The schema to compose with this schema
     * @return A new ObjectSchema that validates using AND logic
     */
    operator fun plus(other: ObjectSchema<T>): ObjectSchema<T> = and(other)

    /**
     * Composes this schema with another schema using AND logic.
     *
     * The validation succeeds only if both this schema and the other schema are satisfied.
     * If either schema fails, the validation fails and returns all error messages from both schemas.
     *
     * Example:
     * ```kotlin
     * val schemaA = object : ObjectSchema<User>({
     *     User::name { it.min(1).max(10) }
     * }) {}
     *
     * val schemaB = object : ObjectSchema<User>({
     *     User::id { it.min(1) }
     * }) {}
     *
     * val combined = schemaA and schemaB
     * // Validation succeeds only if both name and id constraints are satisfied
     * ```
     *
     * @param other The schema to compose with this schema
     * @return A new ObjectSchema that validates using AND logic
     */
    infix fun and(other: ObjectSchema<T>): ObjectSchema<T> {
        val composed = (this as IdentityValidator<T>) and other
        return object : ObjectSchema<T>() {
            override fun execute(
                input: T,
                context: ValidationContext,
            ): ValidationResult<T> = composed.execute(input, context)
        }
    }

    /**
     * Composes this schema with another schema using OR logic.
     *
     * The validation succeeds if at least one of the schemas is satisfied.
     * If both schemas fail, the validation fails and returns a single error message with
     * constraint ID `kova.or` containing all error messages from both schemas.
     *
     * Example:
     * ```kotlin
     * val schemaA = object : ObjectSchema<User>({
     *     User::name { it.min(1).max(10) }
     * }) {}
     *
     * val schemaB = object : ObjectSchema<User>({
     *     User::id { it.min(1) }
     * }) {}
     *
     * val combined = schemaA or schemaB
     * // Validation succeeds if either name or id constraint is satisfied
     * ```
     *
     * @param other The schema to compose with this schema
     * @return A new ObjectSchema that validates using OR logic
     */
    infix fun or(other: ObjectSchema<T>): ObjectSchema<T> {
        val composed = (this as IdentityValidator<T>) or other
        return object : ObjectSchema<T>() {
            override fun execute(
                input: T,
                context: ValidationContext,
            ): ValidationResult<T> = composed.execute(input, context)
        }
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
 *     Period::startDate { it }
 *     Period::endDate { it }
 *     constrain("dateRange") {
 *         satisfies(it.input.startDate <= it.input.endDate, "Start date must be before or equal to end date")
 *     }
 * })
 * ```
 *
 * @param T The type of object being validated
 */
class ObjectSchemaScope<T : Any> internal constructor(
    val self: ObjectSchema<T>,
    private val constraints: MutableList<Constraint<T>>,
    private val ruleMap: MutableMap<KProperty1<T, *>, Rule>,
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

    /**
     * Defines a validation rule for a property.
     *
     * This operator function allows you to use the invoke syntax to define validators
     * for object properties. The lambda receives a base validator as a parameter (`it`),
     * which can be used to build the validator chain.
     *
     * Example:
     * ```kotlin
     * object UserSchema : ObjectSchema<User>({
     *     User::name { it.min(1).max(50) }
     *     // The lambda parameter 'it' is a base validator for the property type
     * })
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
     * object AddressSchema : ObjectSchema<Address>({
     *     Address::country { it }
     *     Address::postalCode choose { address, v ->
     *         when (address.country) {
     *             "US" -> v.length(5)
     *             "CA" -> v.length(6)
     *             else -> v.min(1)
     *         }
     *     }
     * })
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

    internal fun <V> addRule(
        key: KProperty1<T, V>,
        choose: (T) -> Validator<V, V>,
    ) {
        key as KProperty1<T, *>
        val transform = { receiver: T -> key.get(receiver) }
        @Suppress("UNCHECKED_CAST")
        transform as (Any?) -> Any?
        @Suppress("UNCHECKED_CAST")
        choose as (Any?) -> IdentityValidator<Any?>
        ruleMap[key] = Rule(transform, choose)
    }
}
