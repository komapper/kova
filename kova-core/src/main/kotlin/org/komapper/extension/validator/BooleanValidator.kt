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
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver Boolean The boolean value to validate
 * @param message Custom error message provider
 * @return The validated boolean value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun Boolean.ensureTrue(message: MessageProvider = { "kova.boolean.true".resource() }): Boolean =
    constrain("kova.boolean.true") { satisfies(it, message) }

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
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver Boolean The boolean value to validate
 * @param message Custom error message provider
 * @return The validated boolean value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun Boolean.ensureFalse(message: MessageProvider = { "kova.boolean.false".resource() }): Boolean =
    constrain("kova.boolean.false") { satisfies(!it, message) }
