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
fun <T : Number> IdentityValidator<T>.positive(message: MessageProvider = Message.resource()): IdentityValidator<T> =
    constrain("kova.number.positive") {
        satisfies(it.input.toDouble() > 0.0, message())
    }

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
fun <T : Number> IdentityValidator<T>.negative(message: MessageProvider = Message.resource()): IdentityValidator<T> =
    constrain("kova.number.negative") {
        satisfies(it.input.toDouble() < 0.0, message())
    }

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
fun <T : Number> IdentityValidator<T>.notPositive(message: MessageProvider = Message.resource()): IdentityValidator<T> =
    constrain("kova.number.notPositive") {
        satisfies(it.input.toDouble() <= 0.0, message())
    }

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
fun <T : Number> IdentityValidator<T>.notNegative(message: MessageProvider = Message.resource()): IdentityValidator<T> =
    constrain("kova.number.notNegative") {
        satisfies(it.input.toDouble() >= 0.0, message())
    }
