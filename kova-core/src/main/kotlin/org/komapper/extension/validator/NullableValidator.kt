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
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param T The type of the nullable value being validated
 * @receiver The nullable value to validate
 * @param message Custom error message provider
 * @return The validated input value (unchanged)
 */
@IgnorableReturnValue
context(_: Validation)
public fun <T> T.ensureNull(message: MessageProvider = { "kova.nullable.null".resource }): T =
    constrain("kova.nullable.null") { satisfies(it == null, message) }

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
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param T The type of the nullable value being validated
 * @receiver The nullable value to validate
 * @param message Custom error message provider for the null check
 * @param block Validation block to execute if input is not null
 * @return The validated input value (unchanged)
 */
@IgnorableReturnValue
context(_: Validation)
public inline fun <T> T.ensureNullOr(
    noinline message: MessageProvider = { "kova.nullable.null".resource },
    block: context(Validation)(T & Any) -> Unit,
): T = apply { ior<Unit> { this.ensureNull(message) } orElse { block(this!!) } }

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
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param T The type of the nullable value being validated
 * @receiver The nullable value to validate
 * @param message Custom error message provider
 * @return The validated non-null input value with type `T & Any`
 */
@IgnorableReturnValue
context(_: Validation)
public fun <T> T.ensureNotNull(message: MessageProvider = { "kova.nullable.notNull".resource }): T & Any {
    contract { returns() implies (this@ensureNotNull != null) }
    raiseIf("kova.nullable.notNull", message) { it == null }
    return this
}
