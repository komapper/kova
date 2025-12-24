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
context(_: Validation)
fun Number.positive(message: MessageProvider = { "kova.number.positive".resource }) =
    constrain("kova.number.positive") { satisfies(it.toDouble() > 0.0, message) }

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
context(_: Validation)
fun Number.negative(message: MessageProvider = { "kova.number.negative".resource }) =
    constrain("kova.number.negative") { satisfies(it.toDouble() < 0.0, message) }

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
context(_: Validation)
fun Number.notPositive(message: MessageProvider = { "kova.number.notPositive".resource }) =
    constrain("kova.number.notPositive") { satisfies(it.toDouble() <= 0.0, message) }

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
context(_: Validation)
fun Number.notNegative(message: MessageProvider = { "kova.number.notNegative".resource }) =
    constrain("kova.number.notNegative") { satisfies(it.toDouble() >= 0.0, message) }
