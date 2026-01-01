package org.komapper.extension.validator

import kotlin.contracts.contract

/**
 * Validates that the input is null.
 *
 * This constraint fails if the input is non-null. Uses the "kova.nullable.isNull"
 * constraint ID.
 *
 * Example:
 * ```kotlin
 * tryValidate { isNull(null) }    // Success
 * tryValidate { isNull("hello") } // Failure
 * ```
 *
 * @param input The nullable input value to validate
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
 * This function attempts to validate that the input is null first using [isNull].
 * If that validation fails (input is not null), it executes the provided validation
 * block with the non-null input value. This is useful for optional fields that must
 * satisfy certain constraints when present.
 *
 * Uses the [or] combinator with [orElse] fallback pattern.
 *
 * Example:
 * ```kotlin
 * tryValidate { isNullOr(null) { min(it, 3) } }      // Success (is null)
 * tryValidate { isNullOr("hello") { min(it, 3) } }  // Success (satisfies min)
 * tryValidate { isNullOr("hi") { min(it, 3) } }     // Failure (too short)
 * ```
 *
 * @param input The nullable input value to validate
 * @param message Custom error message provider for the null check
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
 * This constraint fails if the input is null. Uses the "kova.nullable.notNull"
 * constraint ID.
 *
 * Example:
 * ```kotlin
 * tryValidate { notNull("hello") } // Success
 * tryValidate { notNull(null) }    // Failure
 * ```
 *
 * @param input The nullable input value to validate
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <T> Validation.notNull(
    input: T,
    message: MessageProvider = { "kova.nullable.notNull".resource },
): Accumulate.Value<Unit> {
    contract { returns() implies (input != null) }
    return raiseIfNull(input, "kova.nullable.notNull", message)
}

/**
 * Converts a nullable input to a non-nullable output with a custom constraint ID.
 *
 * This is an internal overload that allows specifying a custom constraint ID for the
 * null check validation. It validates that the input is not null and converts the output
 * type from `T?` to `T & Any`.
 *
 * This overload is primarily used internally by type conversion validators (e.g., [toInt],
 * [toLong], [toEnum]) that need to report errors with their specific constraint IDs
 * (e.g., "kova.string.isInt", "kova.string.isEnum") rather than the generic
 * "kova.nullable.notNull".
 *
 * The function uses [constrain] to properly track the validation path context and returns
 * the accumulated value, ensuring proper error reporting in nested validation scenarios.
 *
 * @param input The nullable input value to validate and convert
 * @param constraintId Custom constraint ID for the validation error (e.g., "kova.string.isInt")
 * @param message Custom error message provider for the validation error
 * @return The non-null input value with type `T & Any`
 */
@IgnorableReturnValue
fun <T> Validation.toNonNullable(
    input: T,
    constraintId: String,
    message: MessageProvider,
): T & Any = raiseIfNull(input, constraintId, message).let { input }

@IgnorableReturnValue
private fun <T> Validation.raiseIfNull(
    input: T,
    constraintId: String,
    message: MessageProvider,
): Accumulate.Value<Unit> {
    contract { returns() implies (input != null) }
    return input.constrain(constraintId) { satisfies(it != null, message) }.also {
        if (it is Accumulate.Error) it.raise()
    }
}
