package org.komapper.extension.validator

import org.komapper.extension.validator.ValidationResult.Success

/**
 * Type alias for validators that accept nullable input but produce non-nullable output.
 *
 * This validator has a default value that is used when the input is null,
 * ensuring the output is always non-null.
 *
 * Example:
 * ```kotlin
 * val validator: WithDefaultNullableValidator<String, String> =
 *     Kova.string().min(1).asNullable("default")
 *
 * validator.validate(null)    // Success: "default" (non-null)
 * validator.validate("hello") // Success: "hello"
 * validator.validate("")      // Failure: too short
 * ```
 *
 * @param T The non-null input type
 * @param S The non-null output type (always non-null due to default)
 */
typealias WithDefaultNullableValidator<T, S> = Validator<T?, S>

/**
 * Converts a non-nullable validator to a nullable validator with a default value.
 *
 * When the input is null, the validator returns the provided default value.
 * This ensures the output is always non-null.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().min(3).asNullable("default")
 * validator.validate(null)    // Success: "default"
 * validator.validate("hello") // Success: "hello"
 * validator.validate("ab")    // Failure: too short
 * ```
 *
 * @param defaultValue The value to use when input is null
 * @return A new validator that accepts null input but produces non-nullable output
 */
fun <T : Any, S : Any> Validator<T, S>.asNullable(defaultValue: S): WithDefaultNullableValidator<T, S> = asNullable { defaultValue }

/**
 * Converts a non-nullable validator to a nullable validator with a lazily-evaluated default value.
 *
 * When the input is null, the provider function is called to generate the default value.
 * This is useful when the default value is expensive to compute or needs to be fresh each time.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().asNullable { UUID.randomUUID().toString() }
 * validator.validate(null)    // Success: newly generated UUID
 * validator.validate("hello") // Success: "hello"
 * ```
 *
 * @param withDefault Function that generates the default value when input is null
 * @return A new validator that accepts null input but produces non-nullable output
 */
fun <T : Any, S : Any> Validator<T, S>.asNullable(withDefault: () -> S): WithDefaultNullableValidator<T, S> =
    Validator { input, context ->
        val defaultValue = withDefault()
        val context = context.addLog("Validator.asNullable(defaultValue=$defaultValue)")
        if (input == null) Success(defaultValue, context) else execute(input, context)
    }

/**
 * Adds a custom constraint to this validator.
 *
 * The constraint can check both null and non-null values before the default is applied.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().asNullable("default")
 *     .constrain("custom") {
 *         satisfies(
 *             it.input == null || it.input.length >= 3,
 *             "Must be null or at least 3 chars"
 *         )
 *     }
 * ```
 *
 * @param id Unique identifier for the constraint
 * @param check Constraint logic that produces a [ConstraintResult]
 * @return A new validator with the constraint applied
 */
fun <T : Any, S : Any> WithDefaultNullableValidator<T, S>.constrain(
    id: String,
    check: ConstraintScope.(ConstraintContext<T?>) -> ConstraintResult,
): WithDefaultNullableValidator<T, S> = compose(ConstraintValidator(Constraint(id, check)))

/**
 * Validates that the input is null.
 *
 * This constraint fails if the input is non-null.
 * Note that even though this validator has a default, this constraint checks
 * the input before the default is applied.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().asNullable("default").isNull()
 * validator.validate(null)    // Success: "default"
 * validator.validate("hello") // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator that only accepts null input
 */
fun <T : Any, S : Any> WithDefaultNullableValidator<T, S>.isNull(
    message: MessageProvider<T?> = Message.resource(),
): WithDefaultNullableValidator<T, S> = constrain("kova.nullable.isNull", Constraints.isNull(message))

/**
 * Validates that the input is not null.
 *
 * This constraint fails if the input is null (before the default is applied).
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().asNullable("default").notNull()
 * validator.validate("hello") // Success: "hello"
 * validator.validate(null)    // Failure: rejected before default is applied
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator that rejects null input
 */
fun <T : Any, S : Any> WithDefaultNullableValidator<T, S>.notNull(
    message: MessageProvider<T?> = Message.resource(),
): WithDefaultNullableValidator<T, S> = constrain("kova.nullable.notNull", Constraints.notNull(message))

/**
 * Converts this validator to a standard validator with non-nullable output.
 *
 * Since this validator already produces non-nullable output (due to the default),
 * this is effectively a no-op type conversion.
 *
 * Example:
 * ```kotlin
 * val withDefault: WithDefaultNullableValidator<String, String> =
 *     Kova.string().asNullable("default")
 * val standard: Validator<String?, String> = withDefault.toNonNullable()
 * ```
 *
 * @return The same validator with standard Validator type
 */
fun <T : Any, S : Any> WithDefaultNullableValidator<T, S>.toNonNullable(): Validator<T?, S> = map { it }
