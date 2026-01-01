package org.komapper.extension.validator

/**
 * Validates that the boolean value is true.
 *
 * This constraint checks if the input boolean value is true.
 * Uses the "kova.boolean.isTrue" constraint ID.
 *
 * Example:
 * ```kotlin
 * tryValidate { isTrue(true) }   // Success
 * tryValidate { isTrue(false) }  // Failure
 * ```
 *
 * @param input The boolean value to validate
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.isTrue(
    input: Boolean,
    message: MessageProvider = { "kova.boolean.isTrue".resource() },
) = input.constrain("kova.boolean.isTrue") { satisfies(it, message) }

/**
 * Validates that the boolean value is false.
 *
 * This constraint checks if the input boolean value is false.
 * Uses the "kova.boolean.isFalse" constraint ID.
 *
 * Example:
 * ```kotlin
 * tryValidate { isFalse(false) }  // Success
 * tryValidate { isFalse(true) }   // Failure
 * ```
 *
 * @param input The boolean value to validate
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.isFalse(
    input: Boolean,
    message: MessageProvider = { "kova.boolean.isFalse".resource() },
) = input.constrain("kova.boolean.isFalse") { satisfies(!it, message) }
