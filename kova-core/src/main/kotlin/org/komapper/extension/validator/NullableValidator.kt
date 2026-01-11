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
 * tryValidate { null.ensureNull() }    // Success
 * tryValidate { "hello".ensureNull() } // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
public fun<T> T.ensureNull(message: MessageProvider = { "kova.nullable.null".resource }) =
    apply { constrain("kova.nullable.null") { satisfies(it == null, message) } }

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
 * tryValidate { null.ensureNullOr { it.ensureLengthAtLeast(3) } }      // Success (is null)
 * tryValidate { "hello".ensureNullOr { it.ensureLengthAtLeast(3) } }  // Success (satisfies min)
 * tryValidate { "hi".ensureNullOr { it.ensureLengthAtLeast(3) } }     // Failure (too short)
 * ```
 *
 * @param message Custom error message provider for the null check
 * @param block Validation block to execute if input is not null
 */
@IgnorableReturnValue
context(_: Validation)
public inline fun<T> T.ensureNullOr(
    noinline message: MessageProvider = { "kova.nullable.null".resource },
    block: context(Validation)(T & Any) -> Unit,
) = apply { or<Unit> { this.ensureNull(message) } orElse { block(this!!) } }

/**
 * Validates that the input is not null.
 *
 * This constraint fails if the input is null. Uses the "kova.nullable.notNull"
 * constraint ID.
 *
 * Example:
 * ```kotlin
 * tryValidate { "hello".ensureNotNull() } // Success
 * tryValidate { null.ensureNotNull() }    // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
public fun<T> T.ensureNotNull(message: MessageProvider = { "kova.nullable.notNull".resource }): T & Any {
    contract { returns() implies (this@ensureNotNull != null) }
    return toNonNullable("kova.nullable.notNull", message)
}

/**
 * Converts a nullable input to a non-nullable output with a custom constraint ID.
 *
 * This is an internal function that allows specifying a custom constraint ID for the
 * null check validation. It validates that the input is not null and converts the output
 * type from `T?` to `T & Any`.
 *
 * This function is primarily used internally by type conversion validators (e.g., [transformToInt],
 * [transformToLong], [transformToEnum]) that need to report errors with their specific constraint IDs
 * (e.g., "kova.string.int", "kova.string.enum") rather than the generic
 * "kova.nullable.notNull".
 *
 * The function uses [constrain] to properly track the validation path context and returns
 * the accumulated value, ensuring proper error reporting in nested validation scenarios.
 *
 * @param constraintId Custom constraint ID for the validation error (e.g., "kova.string.ensureInt")
 * @param message Custom error message provider for the validation error
 * @return The non-null input value with type `T & Any`
 */
@IgnorableReturnValue
context(_: Validation)
public fun<T> T.toNonNullable(
    constraintId: String,
    message: MessageProvider,
): T & Any = raiseIfNull(constraintId, message).let { this }

@IgnorableReturnValue
context(_: Validation)
private fun <T> T.raiseIfNull(
    constraintId: String,
    message: MessageProvider,
) {
    contract { returns() implies (this@raiseIfNull != null) }
    val result = constrain(constraintId) { satisfies(it != null, message) }
    if (result is Accumulate.Error) result.raise()
}
