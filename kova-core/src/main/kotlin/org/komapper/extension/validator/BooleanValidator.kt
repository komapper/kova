package org.komapper.extension.validator

/**
 * Validates that the boolean value is true.
 *
 * This constraint checks if the input boolean value is true.
 * Uses the "kova.boolean.true" constraint ID.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureTrue(true) }   // Success
 * tryValidate { ensureTrue(false) }  // Failure
 * ```
 *
 * @param input The boolean value to validate
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun ensureTrue(
    input: Boolean,
    message: MessageProvider = { "kova.boolean.true".resource() },
) = input.constrain("kova.boolean.true") { satisfies(it, message) }

/**
 * Validates that the boolean value is false.
 *
 * This constraint checks if the input boolean value is false.
 * Uses the "kova.boolean.false" constraint ID.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureFalse(false) }  // Success
 * tryValidate { ensureFalse(true) }   // Failure
 * ```
 *
 * @param input The boolean value to validate
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun ensureFalse(
    input: Boolean,
    message: MessageProvider = { "kova.boolean.false".resource() },
) = input.constrain("kova.boolean.false") { satisfies(!it, message) }
