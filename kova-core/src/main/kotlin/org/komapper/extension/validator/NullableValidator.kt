package org.komapper.extension.validator

/**
 * Type alias for validators that accept nullable input and produce nullable output.
 *
 * This allows validation of optional values where null is a valid state.
 *
 * Example:
 * ```kotlin
 * val validator: NullableValidator<String, String> = Kova.string().min(1).asNullable()
 * validator.validate(null)    // Success: null
 * validator.validate("hello") // Success: "hello"
 * validator.validate("")      // Failure: too short
 * ```
 *
 * @param T The non-null input type
 * @param S The non-null output type
 */
typealias NullableValidator<T, S> = Validator<T?, S?>

/**
 * Adds a custom constraint to this nullable validator.
 *
 * The constraint can check both null and non-null values.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().asNullable()
 *     .constrain("custom") {
 *         satisfies(it.input == null || it.input.length >= 3, "Must be null or at least 3 chars")
 *     }
 * ```
 *
 * @param id Unique identifier for the constraint
 * @param check Constraint logic that produces a [ConstraintResult]
 * @return A new nullable validator with the constraint applied
 */
fun <T : Any, S : Any> NullableValidator<T, S>.constrain(
    id: String,
    check: ConstraintScope<T?>.(ConstraintContext<T?>) -> ConstraintResult,
): NullableValidator<T, S> = compose(ConstraintValidator(Constraint(id, check)))

/**
 * Validates that the input is null.
 *
 * This constraint fails if the input is non-null.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().asNullable().isNull()
 * validator.validate(null)    // Success: null
 * validator.validate("hello") // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator that only accepts null
 */
fun <T : Any, S : Any> NullableValidator<T, S>.isNull(message: MessageProvider = MessageProvider.resource()): NullableValidator<T, S> =
    constrain("kova.nullable.isNull") {
        satisfies(it.input == null, message())
    }

/**
 * Validates that the input is null OR satisfies a custom validator.
 *
 * This is a convenience method equivalent to `isNull().or(validator.asNullable())`.
 * Either the input must be null, or it must satisfy the validator built by the block.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().asNullable()
 *     .isNullOr { it.min(3).max(10) }
 *
 * validator.validate(null)    // Success: null (first branch passes)
 * validator.validate("hello") // Success: "hello" (second branch passes)
 * validator.validate("ab")    // Failure: not null and too short
 * ```
 *
 * @param block A function that builds a validator from a success validator
 * @param message Custom error message provider for the null check
 * @return A new validator that accepts null or values satisfying the block validator
 */
fun <T : Any, S : Any> NullableValidator<T, S>.isNullOr(
    block: (Validator<T, T>) -> Validator<T, S>,
    message: MessageProvider = MessageProvider.resource(),
): NullableValidator<T, S> = isNull(message).or(block(Validator.success()).asNullable())

/**
 * Validates that the input is not null.
 *
 * This constraint fails if the input is null.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().asNullable().notNull()
 * validator.validate("hello") // Success: "hello"
 * validator.validate(null)    // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator that rejects null
 */
fun <T : Any, S : Any> NullableValidator<T, S>.notNull(message: MessageProvider = MessageProvider.resource()): NullableValidator<T, S> =
    constrain("kova.nullable.notNull") {
        satisfies(it.input != null, message())
    }

/**
 * Validates that the input is not null AND satisfies a custom validator.
 *
 * This is a convenience method equivalent to `notNull().and(validator.asNullable())`.
 * The input must be non-null and must satisfy the validator built by the block.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().asNullable()
 *     .notNullAnd { it.min(3).max(10) }
 *
 * validator.validate("hello") // Success: "hello" (both constraints pass)
 * validator.validate(null)    // Failure: is null
 * validator.validate("ab")    // Failure: not null but too short
 * ```
 *
 * @param block A function that builds a validator from a success validator
 * @param message Custom error message provider for the null check
 * @return A new validator that rejects null and validates non-null values with the block validator
 */
fun <T : Any, S : Any> NullableValidator<T, S>.notNullAnd(
    block: (Validator<T, T>) -> Validator<T, S>,
    message: MessageProvider = MessageProvider.resource(),
): NullableValidator<T, S> = notNull(message).and(block(Validator.success()).asNullable())

/**
 * Converts a nullable validator to a validator with non-nullable output.
 *
 * This adds a `notNull()` constraint and converts the output type from `S?` to `S`.
 *
 * Example:
 * ```kotlin
 * val nullableValidator = Kova.string().min(3).asNullable()
 * val nonNullableValidator: Validator<String?, String> = nullableValidator.toNonNullable()
 *
 * nonNullableValidator.validate("hello") // Success: "hello" (non-null type)
 * nonNullableValidator.validate(null)    // Failure
 * ```
 *
 * @return A validator that rejects null and produces non-nullable output
 */
inline fun <T : Any, reified S : Any> NullableValidator<T, S>.toNonNullable(): Validator<T?, S> = notNull().map { it!! }

/**
 * Provides a default value for null inputs.
 *
 * If the input is null, the validator returns the default value instead.
 * This converts the validator to a [NullCoalescingValidator] that produces non-nullable output.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().min(3).asNullable().withDefault("default")
 * validator.validate(null)    // Success: "default"
 * validator.validate("hello") // Success: "hello"
 * validator.validate("ab")    // Failure: too short
 * ```
 *
 * @param defaultValue The value to use when input is null
 * @return A new validator with non-nullable output that uses the default for null inputs
 */
inline fun <T : Any, reified S : Any> NullableValidator<T, S>.withDefault(defaultValue: S): NullCoalescingValidator<T, S> =
    withDefault { defaultValue }

/**
 * Provides a lazily-evaluated default value for null inputs.
 *
 * If the input is null, the provider function is called to generate the default value.
 * This is useful when the default value is expensive to compute.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().asNullable()
 *     .withDefault { generateDefaultValue() }
 *
 * validator.validate(null)    // Success: result of generateDefaultValue()
 * validator.validate("hello") // Success: "hello"
 * ```
 *
 * @param provide Function that generates the default value
 * @return A new validator with non-nullable output that uses the provided default for null inputs
 */
inline fun <T : Any, reified S : Any> NullableValidator<T, S>.withDefault(noinline provide: () -> S): NullCoalescingValidator<T, S> =
    map { it ?: provide() }

/**
 * Operator overload for [and]. Combines this nullable validator with a non-nullable validator.
 *
 * The non-nullable validator is automatically converted to nullable.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().asNullable() + Kova.string().min(3)
 * validator.validate(null)    // Success: null
 * validator.validate("hello") // Success: "hello"
 * validator.validate("ab")    // Failure: too short
 * ```
 *
 * @param other The non-nullable validator to combine with
 * @return A new nullable validator combining both
 */
operator fun <T : Any, S : Any> NullableValidator<T, S>.plus(other: Validator<T, S>): NullableValidator<T, S> = and(other)

/**
 * Combines this nullable validator with a non-nullable validator using logical AND.
 *
 * The non-nullable validator is automatically converted to nullable before combining.
 * Both validators must pass for the combined validator to pass.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().asNullable() and Kova.string().min(3)
 * validator.validate(null)    // Success: null
 * validator.validate("hello") // Success: "hello"
 * validator.validate("ab")    // Failure: too short
 * ```
 *
 * @param other The non-nullable validator to combine with
 * @return A new nullable validator combining both
 */
fun <T : Any, S : Any> NullableValidator<T, S>.and(other: Validator<T, S>): NullableValidator<T, S> = and(other.asNullable())

/**
 * Combines this nullable validator with a non-nullable validator using logical OR.
 *
 * The non-nullable validator is automatically converted to nullable before combining.
 * Either validator can pass for the combined validator to pass.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().asNullable().isNull() or Kova.string().min(3)
 * validator.validate(null)    // Success: null (first validator passes)
 * validator.validate("hello") // Success: "hello" (second validator passes)
 * validator.validate("ab")    // Failure: both validators fail
 * ```
 *
 * @param other The non-nullable validator to combine with
 * @return A new nullable validator combining both
 */
fun <T : Any, S : Any> NullableValidator<T, S>.or(other: Validator<T, S>): NullableValidator<T, S> = or(other.asNullable())
