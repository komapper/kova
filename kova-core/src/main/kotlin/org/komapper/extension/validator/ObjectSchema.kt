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
 *     val name = User::name { Kova.string().min(1).max(50) }
 *     val age = User::age { Kova.int().min(0).max(120) }
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
 *     val startDate = Period::startDate { Kova.localDate() }
 *     val endDate = Period::endDate { Kova.localDate() }
 * }
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
                    is ValidationResult.Failure -> ValidationResult.Failure(result.details)
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
                .fold(EmptyValidator<T>() as Validator<T, T>) { acc, v -> acc + v }
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
        validator: Validator<V, V>,
    ): ObjectSchema<T> {
        val rule =
            Rule(
                transform = { receiver: T -> key.get(receiver) } as (Any?) -> Any?,
                choose = { _: T -> validator } as (Any?) -> Validator<Any?, Any?>,
            )
        val newRuleMap = ruleMap.toMutableMap()
        newRuleMap.replace(key.name, rule)
        return ObjectSchema(newRuleMap)
    }

    /**
     * Defines a validation rule for a property.
     *
     * This operator function allows you to use the invoke syntax to define validators
     * for object properties. The property must be defined as a member of the ObjectSchema
     * subclass (not in the constructor lambda).
     *
     * Example:
     * ```kotlin
     * object UserSchema : ObjectSchema<User>() {
     *     val name = User::name { Kova.string().min(1).max(50) }
     *     // The property 'name' gets the returned StringValidator
     * }
     * ```
     *
     * @param block Lambda that creates the validator for this property
     * @return The validator created by the block
     */
    operator fun <V, VALIDATOR : Validator<V, V>> KProperty1<T, V>.invoke(block: () -> VALIDATOR): VALIDATOR {
        val validator = block()
        addRule(this) { _ -> validator }
        return validator
    }

    /**
     * Defines a conditional validation rule for a property based on the object's state.
     *
     * This allows you to choose different validators based on the value of other properties.
     *
     * Example:
     * ```kotlin
     * object UserSchema : ObjectSchema<User>() {
     *     val type = User::type { Kova.string() }
     *     val identifier = User::identifier choose { user ->
     *         when (user.type) {
     *             "email" -> Kova.string().email()
     *             "phone" -> Kova.string().matches(phoneRegex)
     *             else -> Kova.string().min(1)
     *         }
     *     }
     * }
     * ```
     *
     * @param block Lambda that chooses a validator based on the object
     * @return The block function for further use
     */
    infix fun <V, VALIDATOR : Validator<V, V>> KProperty1<T, V>.choose(block: (T) -> VALIDATOR): (T) -> VALIDATOR {
        addRule(this, block)
        return block
    }

    private fun <T, V> addRule(
        key: KProperty1<T, V>,
        choose: (T) -> Validator<V, V>,
    ) {
        val transform = { receiver: T -> key.get(receiver) }
        transform as (Any?) -> Any?
        choose as (Any?) -> Validator<Any?, Any?>
        ruleMap[key.name] = Rule(transform, choose)
    }

    /**
     * Creates an argument from a raw value and its validator.
     *
     * Used with [arguments] to build objects from validated inputs.
     *
     * @param value The input value to validate
     * @param validator The validator to apply to the value
     * @return An Arg that can be passed to the arguments builder
     */
    fun <IN, OUT> arg(
        value: IN,
        validator: Validator<IN, OUT>,
    ): Arg<OUT> = Arg.Value(value, validator)

    /**
     * Creates an argument from an ObjectFactory and its validator.
     *
     * Used with [arguments] to build nested objects from validated inputs.
     *
     * @param factory The ObjectFactory that creates the nested object
     * @param validator The validator to apply to the created object
     * @return An Arg that can be passed to the arguments builder
     */
    fun <IN, OUT> arg(
        factory: ObjectFactory<IN>,
        validator: Validator<IN, OUT>,
    ): Arg<OUT> = Arg.Factory(factory, validator)

    /**
     * Creates an object factory with 1 validated argument.
     *
     * Use this to validate inputs and construct objects in a type-safe way.
     *
     * Example:
     * ```kotlin
     * object PersonSchema : ObjectSchema<Person>() {
     *     private val name = Person::name { Kova.string().min(1) }
     *
     *     fun build(nameInput: String) =
     *         arguments(arg(nameInput, name)).build(::Person)
     * }
     * val result = PersonSchema.build("Alice").tryCreate()
     * ```
     */
    fun <T0> arguments(arg0: Arg<T0>) = Arguments(this, arg0)

    /**
     * Creates an object factory with 2 validated arguments.
     *
     * @see arguments for usage examples
     */
    fun <T0, T1> arguments(
        arg0: Arg<T0>,
        arg1: Arg<T1>,
    ) = Arguments1(this, arg0, arg1)

    /** Creates an object factory with 3 validated arguments. @see arguments */
    fun <T0, T1, T2> arguments(
        arg0: Arg<T0>,
        arg1: Arg<T1>,
        arg2: Arg<T2>,
    ) = Arguments2(this, arg0, arg1, arg2)

    /** Creates an object factory with 4 validated arguments. @see arguments */
    fun <T0, T1, T2, T3> arguments(
        arg0: Arg<T0>,
        arg1: Arg<T1>,
        arg2: Arg<T2>,
        arg3: Arg<T3>,
    ) = Arguments3(this, arg0, arg1, arg2, arg3)

    /** Creates an object factory with 5 validated arguments. @see arguments */
    fun <T0, T1, T2, T3, T4> arguments(
        arg0: Arg<T0>,
        arg1: Arg<T1>,
        arg2: Arg<T2>,
        arg3: Arg<T3>,
        arg4: Arg<T4>,
    ) = Arguments4(this, arg0, arg1, arg2, arg3, arg4)

    /** Creates an object factory with 6 validated arguments. @see arguments */
    fun <T0, T1, T2, T3, T4, T5> arguments(
        arg0: Arg<T0>,
        arg1: Arg<T1>,
        arg2: Arg<T2>,
        arg3: Arg<T3>,
        arg4: Arg<T4>,
        arg5: Arg<T5>,
    ) = Arguments5(this, arg0, arg1, arg2, arg3, arg4, arg5)

    /** Creates an object factory with 7 validated arguments. @see arguments */
    fun <T0, T1, T2, T3, T4, T5, T6> arguments(
        arg0: Arg<T0>,
        arg1: Arg<T1>,
        arg2: Arg<T2>,
        arg3: Arg<T3>,
        arg4: Arg<T4>,
        arg5: Arg<T5>,
        arg6: Arg<T6>,
    ) = Arguments6(this, arg0, arg1, arg2, arg3, arg4, arg5, arg6)

    /** Creates an object factory with 8 validated arguments. @see arguments */
    fun <T0, T1, T2, T3, T4, T5, T6, T7> arguments(
        arg0: Arg<T0>,
        arg1: Arg<T1>,
        arg2: Arg<T2>,
        arg3: Arg<T3>,
        arg4: Arg<T4>,
        arg5: Arg<T5>,
        arg6: Arg<T6>,
        arg7: Arg<T7>,
    ) = Arguments7(this, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7)

    /** Creates an object factory with 9 validated arguments. @see arguments */
    fun <T0, T1, T2, T3, T4, T5, T6, T7, T8> arguments(
        arg0: Arg<T0>,
        arg1: Arg<T1>,
        arg2: Arg<T2>,
        arg3: Arg<T3>,
        arg4: Arg<T4>,
        arg5: Arg<T5>,
        arg6: Arg<T6>,
        arg7: Arg<T7>,
        arg8: Arg<T8>,
    ) = Arguments8(this, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8)

    /** Creates an object factory with 10 validated arguments. @see arguments */
    fun <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> arguments(
        arg0: Arg<T0>,
        arg1: Arg<T1>,
        arg2: Arg<T2>,
        arg3: Arg<T3>,
        arg4: Arg<T4>,
        arg5: Arg<T5>,
        arg6: Arg<T6>,
        arg7: Arg<T7>,
        arg8: Arg<T8>,
        arg9: Arg<T9>,
    ) = Arguments9(this, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9)
}

internal data class Rule(
    val transform: (Any?) -> Any?,
    val choose: (Any?) -> Validator<Any?, Any?>,
)

class ObjectSchemaScope<T : Any> internal constructor(
    private val constraints: MutableList<Constraint<T>>,
) : Constrainable<T, Unit> {
    override fun constrain(
        id: String,
        check: ConstraintScope.(ConstraintContext<T>) -> ConstraintResult,
    ) {
        constraints.add(Constraint(id, check))
    }
}
