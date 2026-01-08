package org.komapper.extension.validator

/**
 * Validates that the number is ensurePositive (greater than zero).
 *
 * Example:
 * ```kotlin
 * tryValidate { ensurePositive(1) }   // Success
 * tryValidate { ensurePositive(0) }   // Failure
 * tryValidate { ensurePositive(-1) }  // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.ensurePositive(
    input: Number,
    message: MessageProvider = { "kova.number.positive".resource },
) = input.constrain("kova.number.positive") { satisfies(it.toDouble() > 0.0, message) }

/**
 * Validates that the number is ensureNegative (less than zero).
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureNegative(-1) }  // Success
 * tryValidate { ensureNegative(0) }   // Failure
 * tryValidate { ensureNegative(1) }   // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.ensureNegative(
    input: Number,
    message: MessageProvider = { "kova.number.negative".resource },
) = input.constrain("kova.number.negative") { satisfies(it.toDouble() < 0.0, message) }

/**
 * Validates that the number is not ensurePositive (less than or equal to zero).
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureNotPositive(-1) }  // Success
 * tryValidate { ensureNotPositive(0) }   // Success
 * tryValidate { ensureNotPositive(1) }   // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.ensureNotPositive(
    input: Number,
    message: MessageProvider = { "kova.number.notPositive".resource },
) = input.constrain("kova.number.notPositive") { satisfies(it.toDouble() <= 0.0, message) }

/**
 * Validates that the number is not ensureNegative (greater than or equal to zero).
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureNotNegative(0) }   // Success
 * tryValidate { ensureNotNegative(1) }   // Success
 * tryValidate { ensureNotNegative(-1) }  // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.ensureNotNegative(
    input: Number,
    message: MessageProvider = { "kova.number.notNegative".resource },
) = input.constrain("kova.number.notNegative") { satisfies(it.toDouble() >= 0.0, message) }
