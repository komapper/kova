package org.komapper.extension.validator

/**
 * Validates that the boolean value is true.
 *
 * This constraint checks if the boolean value is true.
 * Uses the "kova.boolean.true" constraint ID.
 *
 * Example:
 * ```kotlin
 * tryValidate { true.ensureTrue() }   // Success
 * tryValidate { false.ensureTrue() }  // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
public fun Boolean.ensureTrue(message: MessageProvider = { "kova.boolean.true".resource() }) =
    apply { constrain("kova.boolean.true") { satisfies(it, message) } }

/**
 * Validates that the boolean value is false.
 *
 * This constraint checks if the boolean value is false.
 * Uses the "kova.boolean.false" constraint ID.
 *
 * Example:
 * ```kotlin
 * tryValidate { false.ensureFalse() }  // Success
 * tryValidate { true.ensureFalse() }   // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
public fun Boolean.ensureFalse(message: MessageProvider = { "kova.boolean.false".resource() }) =
    apply { constrain("kova.boolean.false") { satisfies(!it, message) } }
