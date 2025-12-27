package org.komapper.extension.validator

/**
 * Validates that the number is positive (greater than zero).
 *
 * Example:
 * ```kotlin
 * val validator = Kova.int().positive()
 * validator.validate(1)   // Success
 * validator.validate(0)   // Failure
 * validator.validate(-1)  // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the positive constraint
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
 * val validator = Kova.int().negative()
 * validator.validate(-1)  // Success
 * validator.validate(0)   // Failure
 * validator.validate(1)   // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the negative constraint
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
 * val validator = Kova.int().notPositive()
 * validator.validate(-1)  // Success
 * validator.validate(0)   // Success
 * validator.validate(1)   // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the not-positive constraint
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
 * val validator = Kova.int().notNegative()
 * validator.validate(0)   // Success
 * validator.validate(1)   // Success
 * validator.validate(-1)  // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the not-negative constraint
 */
@IgnorableReturnValue
fun Validation.notNegative(
    input: Number,
    message: MessageProvider = { "kova.number.notNegative".resource },
) = input.constrain("kova.number.notNegative") { satisfies(it.toDouble() >= 0.0, message) }
