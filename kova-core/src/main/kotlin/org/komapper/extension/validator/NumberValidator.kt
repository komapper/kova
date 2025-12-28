package org.komapper.extension.validator

/**
 * Validates that the number is positive (greater than zero).
 *
 * Example:
 * ```kotlin
 * tryValidate { positive(1) }   // Success
 * tryValidate { positive(0) }   // Failure
 * tryValidate { positive(-1) }  // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.positive(
    input: Number,
    message: MessageProvider = { "kova.number.positive".resource },
) = input.constrain("kova.number.positive") { satisfies(it.toDouble() > 0.0, message) }

/**
 * Validates that the number is negative (less than zero).
 *
 * Example:
 * ```kotlin
 * tryValidate { negative(-1) }  // Success
 * tryValidate { negative(0) }   // Failure
 * tryValidate { negative(1) }   // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.negative(
    input: Number,
    message: MessageProvider = { "kova.number.negative".resource },
) = input.constrain("kova.number.negative") { satisfies(it.toDouble() < 0.0, message) }

/**
 * Validates that the number is not positive (less than or equal to zero).
 *
 * Example:
 * ```kotlin
 * tryValidate { notPositive(-1) }  // Success
 * tryValidate { notPositive(0) }   // Success
 * tryValidate { notPositive(1) }   // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.notPositive(
    input: Number,
    message: MessageProvider = { "kova.number.notPositive".resource },
) = input.constrain("kova.number.notPositive") { satisfies(it.toDouble() <= 0.0, message) }

/**
 * Validates that the number is not negative (greater than or equal to zero).
 *
 * Example:
 * ```kotlin
 * tryValidate { notNegative(0) }   // Success
 * tryValidate { notNegative(1) }   // Success
 * tryValidate { notNegative(-1) }  // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.notNegative(
    input: Number,
    message: MessageProvider = { "kova.number.notNegative".resource },
) = input.constrain("kova.number.notNegative") { satisfies(it.toDouble() >= 0.0, message) }
