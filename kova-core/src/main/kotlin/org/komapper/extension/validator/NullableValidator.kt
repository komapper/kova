package org.komapper.extension.validator

import kotlin.contracts.contract

/**
 * Validates that the input is null.
 *
 * This constraint fails if the input is non-null. Uses the "kova.nullable.null"
 * constraint ID.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureNull(null) }    // Success
 * tryValidate { ensureNull("hello") } // Failure
 * ```
 *
 * @param input The nullable input value to validate
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun <T> ensureNull(
    input: T,
    message: MessageProvider = { "kova.nullable.null".resource },
) = input.constrain("kova.nullable.null") { satisfies(it == null, message) }

/**
 * Validates that the input is null OR satisfies the given constraints.
 *
 * This function attempts to validate that the input is null first using [ensureNull].
 * If that validation fails (input is not null), it executes the provided validation
 * block with the non-null input value. This is useful for optional fields that must
 * satisfy certain constraints when present.
 *
 * Uses the [or] combinator with [orElse] fallback pattern.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureNullOr(null) { min(it, 3) } }      // Success (is null)
 * tryValidate { ensureNullOr("hello") { min(it, 3) } }  // Success (satisfies min)
 * tryValidate { ensureNullOr("hi") { min(it, 3) } }     // Failure (too short)
 * ```
 *
 * @param input The nullable input value to validate
 * @param message Custom error message provider for the null check
 * @param block Validation block to execute if input is not null
 */
@IgnorableReturnValue
context(_: Validation)
inline fun <T> ensureNullOr(
    input: T,
    noinline message: MessageProvider = { "kova.nullable.null".resource },
    block: context(Validation)(T & Any) -> Unit,
) = or<Unit> { ensureNull(input, message) } orElse { block(input!!) }

/**
 * Validates that the input is not null.
 *
 * This constraint fails if the input is null. Uses the "kova.nullable.notNull"
 * constraint ID.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureNotNull("hello") } // Success
 * tryValidate { ensureNotNull(null) }    // Failure
 * ```
 *
 * @param input The nullable input value to validate
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun <T> ensureNotNull(
    input: T,
    message: MessageProvider = { "kova.nullable.notNull".resource },
): Accumulate.Value<Unit> {
    contract { returns() implies (input != null) }
    return raiseIfNull(input, "kova.nullable.notNull", message)
}

/**
 * Converts a nullable input to a non-nullable output with a custom constraint ID.
 *
 * This is an internal function that allows specifying a custom constraint ID for the
 * null check validation. It validates that the input is not null and converts the output
 * type from `T?` to `T & Any`.
 *
 * This function is primarily used internally by type conversion validators (e.g., [parseInt],
 * [parseLong], [parseEnum]) that need to report errors with their specific constraint IDs
 * (e.g., "kova.string.ensureInt", "kova.string.ensureEnum") rather than the generic
 * "kova.nullable.notNull".
 *
 * The function uses [constrain] to properly track the validation path context and returns
 * the accumulated value, ensuring proper error reporting in nested validation scenarios.
 *
 * @param input The nullable input value to validate and convert
 * @param constraintId Custom constraint ID for the validation error (e.g., "kova.string.ensureInt")
 * @param message Custom error message provider for the validation error
 * @return The non-null input value with type `T & Any`
 */
@IgnorableReturnValue
context(_: Validation)
fun <T> toNonNullable(
    input: T,
    constraintId: String,
    message: MessageProvider,
): T & Any = raiseIfNull(input, constraintId, message).let { input }

@IgnorableReturnValue
context(_: Validation)
private fun <T> raiseIfNull(
    input: T,
    constraintId: String,
    message: MessageProvider,
): Accumulate.Value<Unit> {
    contract { returns() implies (input != null) }
    return input.constrain(constraintId) { satisfies(it != null, message) }.also {
        if (it is Accumulate.Error) it.raise()
    }
}
