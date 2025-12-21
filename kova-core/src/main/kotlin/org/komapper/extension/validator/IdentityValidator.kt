package org.komapper.extension.validator

import org.komapper.extension.validator.ValidationResult.Success

/**
 * Type alias for validators where the input and output types are the same.
 *
 * This simplifies type signatures for validators that validate but don't transform the type,
 * such as string validators, number validators, and most primitive type validators.
 *
 * Example:
 * ```kotlin
 * // Instead of: Validator<String, String>
 * val validator: IdentityValidator<String> = Kova.string().min(1).max(10)
 * ```
 */
typealias IdentityValidator<T> = Validator<T, T>

/**
 * Validates that the input equals the specified value.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().literal("admin")
 * validator.validate("admin") // Success
 * validator.validate("user")  // Failure
 * ```
 *
 * @param value The expected value
 * @param message Custom error message provider
 * @return A new validator that accepts only the specified value
 */
fun <T, S> Validator<T, S>.literal(
    value: S,
    message: MessageProvider = { "kova.literal.single".resource(value) },
) = constrain("kova.literal.single") { satisfies(it == value, message) }

/**
 * Validates that the input is one of the specified values.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().literal(listOf("admin", "user", "guest"))
 * validator.validate("admin") // Success
 * validator.validate("other") // Failure
 * ```
 *
 * @param values The list of acceptable values
 * @param message Custom error message provider
 * @return A new validator that accepts only values from the list
 */
fun <T, S> Validator<T, S>.literal(
    values: List<S>,
    message: MessageProvider = { "kova.literal.list".resource(values) },
) = constrain("kova.literal.list") { satisfies(it in values, message) }

/**
 * Conditionally applies this validator based on a predicate.
 *
 * If the condition returns false, validation passes automatically without executing
 * this validator. This is useful for conditional validation logic.
 *
 * Example:
 * ```kotlin
 * // Only validate length if the string is not blank
 * val validator = Kova.string()
 *     .min(3).max(20)
 *     .onlyIf { it.isNotBlank() }
 *
 * // Validate discount code only if provided
 * val discountValidator = Kova.string()
 *     .matches(Regex("^[A-Z0-9]{5,10}$"))
 *     .onlyIf { it.isNotBlank() }
 * ```
 *
 * @param condition Predicate that determines whether to apply this validator
 * @return A new validator that conditionally validates
 */
fun <T> IdentityValidator<T>.onlyIf(condition: (T) -> Boolean) =
    IdentityValidator<T> { input ->
        if (condition(input)) {
            execute(input)
        } else {
            Success(input)
        }
    }

/**
 * Converts this validator to accept nullable input.
 *
 * When the input is null, validation succeeds with null output.
 * When the input is non-null, the original validator logic is applied.
 *
 * This is useful for making required validators optional or for chaining with nullable properties.
 *
 * Example:
 * ```kotlin
 * // Make a string validator nullable
 * val validator = Kova.string().min(3).max(20).asNullable()
 * validator.validate(null)    // Success(null)
 * validator.validate("hello") // Success("hello")
 * validator.validate("ab")    // Failure (too short)
 *
 * // Use with ObjectSchema for optional fields
 * object UserSchema : ObjectSchema<User>({
 *     User::middleName {
 *         it.min(1).max(50).asNullable()
 *     }
 * })
 * ```
 *
 * @return A new nullable validator that accepts null input
 * @see Validator.asNullable for the base version that works with any validator type
 */
fun <T, S : Any> Validator<T, S>.asNullable(): NullableValidator<T, S> =
    Validator { input -> if (input == null) Success(null) else execute(input) }
