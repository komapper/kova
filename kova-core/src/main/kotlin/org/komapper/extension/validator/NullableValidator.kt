package org.komapper.extension.validator

import kotlin.contracts.contract

/**
 * Validates that the input is null.
 *
 * This constraint fails if the input is non-null.
 *
 * Example:
 * ```kotlin
 * tryValidate { isNull(null) }    // Success
 * tryValidate { isNull("hello") } // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <T> Validation.isNull(
    input: T,
    message: MessageProvider = { "kova.nullable.isNull".resource },
) = input.constrain("kova.nullable.isNull") { satisfies(it == null, message) }

/**
 * Validates that the input is null OR satisfies the given constraints.
 *
 * This constraint tries to validate that the input is null first. If that fails,
 * it executes the provided block with the non-null input value. This is useful
 * when a value can be either null or must satisfy certain constraints if present.
 *
 * Example:
 * ```kotlin
 * tryValidate { isNullOr(null) { min(it, 3) } }      // Success (is null)
 * tryValidate { isNullOr("hello") { min(it, 3) } }  // Success (satisfies min)
 * tryValidate { isNullOr("hi") { min(it, 3) } }     // Failure (too short)
 * ```
 *
 * @param input The nullable input value to validate
 * @param message Custom error message provider for null validation
 * @param block Validation block to execute if input is not null
 */
@IgnorableReturnValue
inline fun <T> Validation.isNullOr(
    input: T,
    noinline message: MessageProvider = { "kova.nullable.isNull".resource },
    block: Validation.(T & Any) -> Unit,
) = or<Unit> { isNull(input, message) } orElse { block(input!!) }

/**
 * Validates that the input is not null.
 *
 * This constraint fails if the input is null.
 *
 * Example:
 * ```kotlin
 * tryValidate { notNull("hello") } // Success
 * tryValidate { notNull(null) }    // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <T> Validation.notNull(
    input: T,
    message: MessageProvider = { "kova.nullable.notNull".resource },
) = input.constrain("kova.nullable.notNull") { satisfies(it != null, message) }

/**
 * Converts a nullable input to a non-nullable output.
 *
 * This validates that the input is not null and converts the output type from `T?` to `T & Any`.
 * The validation uses the "kova.nullable.notNull" constraint ID and properly propagates the
 * validation path context for error reporting.
 *
 * Example:
 * ```kotlin
 * fun Validation.validateString(s: String?): String {
 *     if (s != null) min(s, 3)
 *     return toNonNullable(s)
 * }
 *
 * tryValidate { validateString("hello") } // Success
 * tryValidate { validateString(null) }    // Failure
 * ```
 *
 * @param input The nullable input value to validate and convert
 * @param message Custom error message provider for the null check
 * @return The non-null input value with type `T & Any`
 */
@IgnorableReturnValue
fun <T> Validation.toNonNullable(
    input: T,
    message: MessageProvider = { "kova.nullable.notNull".resource },
): T & Any = toNonNullable(input, "kova.nullable.notNull", message)

/**
 * Converts a nullable input to a non-nullable output with a custom constraint ID.
 *
 * This is an internal overload that allows specifying a custom constraint ID
 * for the null check validation. It validates that the input is not null and
 * converts the output type from `T?` to `T & Any`.
 *
 * This overload is primarily used internally when building custom validators
 * that need to use a specific constraint ID for error reporting.
 *
 * @param input The nullable input value to validate and convert
 * @param constraintId Custom constraint ID for the validation error
 * @param message Custom error message provider
 * @return The non-null input value with type `T & Any`
 */
@IgnorableReturnValue
fun <T> Validation.toNonNullable(
    input: T,
    constraintId: String,
    message: MessageProvider,
): T & Any {
    contract { returns() implies (input != null) }
    val accumulateValue =
        input.constrain(constraintId) {
            satisfies(input != null, message)
            input
        }
    return accumulateValue.value
}
