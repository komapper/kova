package org.komapper.extension.validator

import org.komapper.extension.validator.ValidationResult.Failure
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
 * Adds a custom constraint to this validator.
 *
 * This is a fundamental building block for creating custom validation rules.
 * The constraint is chained to the existing validator, executing after it succeeds.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().constrain("alphanumeric") {
 *     satisfies(it.input.all { c -> c.isLetterOrDigit() }, "Must be alphanumeric")
 * }
 * ```
 *
 * @param id Unique identifier for the constraint (used for error tracking)
 * @param check Constraint logic that produces a [ConstraintResult]
 * @return A new validator with the constraint applied
 */
fun <T> IdentityValidator<T>.constrain(
    id: String,
    check: ConstraintScope<T>.(ConstraintContext<T>) -> ConstraintResult,
): IdentityValidator<T> = chain(ConstraintValidator(Constraint(id, check)))

/**
 * Chains two validators where the second validator receives the output of the first if it succeeds.
 *
 * **Key characteristic**: Both input and output must be the same type (T).
 * This is designed for validators that don't transform the type.
 *
 * Unlike [then]:
 * - Requires IN == OUT (same type)
 * - If the first validator fails and failFast is disabled, both validators
 *   are executed and their failures are combined
 *
 * Example with same type:
 * ```kotlin
 * val normalizeValidator = Kova.string().map { it.trim() }
 * val validateValidator = Kova.string().min(1).max(10)
 * val validator = normalizeValidator.chain(validateValidator)
 * // Input: String, Output: String (same type)
 * ```
 *
 * @param next The validator to apply next
 * @return A new validator that chains both validators
 */
fun <T> IdentityValidator<T>.chain(next: IdentityValidator<T>): IdentityValidator<T> =
    IdentityValidator { input, context ->
        when (val result = this.execute(input, context)) {
            is Success -> {
                next.execute(result.value, result.context)
            }

            is Failure -> {
                if (context.failFast) {
                    result
                } else {
                    when (val v = result.value) {
                        is Input.Available -> result + next.execute(v.value, context)
                        is Input.Unusable -> result
                    }
                }
            }
        }
    }

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
fun <T> IdentityValidator<T>.literal(
    value: T,
    message: MessageProvider = Message.resource(),
) = constrain("kova.literal.single") {
    satisfies(it.input == value, message(value))
}

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
fun <T> IdentityValidator<T>.literal(
    values: List<T>,
    message: MessageProvider = Message.resource(),
) = constrain("kova.literal.list") {
    satisfies(it.input in values, message(values))
}

/**
 * Conditionally applies this validator based on a predicate.
 *
 * If the condition returns false, validation passes automatically without executing
 * this validator. This is useful for conditional validation logic.
 *
 * Example:
 * ```kotlin
 * // Only validate email format if the string looks like an email
 * val validator = Kova.string()
 *     .email()
 *     .onlyIf { it.contains("@") }
 *
 * // More practical: validate discount code only if provided
 * val discountValidator = Kova.string()
 *     .min(5)
 *     .onlyIf { it.isNotBlank() }
 * ```
 *
 * @param condition Predicate that determines whether to apply this validator
 * @return A new validator that conditionally validates
 */
fun <T> IdentityValidator<T>.onlyIf(condition: (T) -> Boolean) =
    IdentityValidator<T> { input, context ->
        if (condition(input)) {
            execute(input, context)
        } else {
            Success(input, context)
        }
    }

fun <T : Any> IdentityValidator<T>.asNullable(): NullableValidator<T, T> =
    Validator { input, context ->
        if (input == null) Success(null, context) else this.execute(input, context)
    }
