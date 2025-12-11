package org.komapper.extension.validator

/**
 * Type alias for validators that validate number types.
 *
 * This is semantically equivalent to [IdentityValidator] but provides clearer intent
 * that the validator is specifically for numeric validation (Int, Long, Double, etc.).
 *
 * Using this type alias improves code readability by making it explicit that
 * number-specific constraints are being applied.
 *
 * Example:
 * ```kotlin
 * val validator: NumberValidator<Int> = Kova.int().positive().max(100)
 * val priceValidator: NumberValidator<Double> = Kova.double().notNegative()
 * ```
 */
typealias NumberValidator<T> = IdentityValidator<T>

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
fun <T : Number> NumberValidator<T>.positive(message: MessageProvider = Message.resource()): NumberValidator<T> =
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
fun <T : Number> NumberValidator<T>.negative(message: MessageProvider = Message.resource()): NumberValidator<T> =
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
fun <T : Number> NumberValidator<T>.notPositive(message: MessageProvider = Message.resource()): NumberValidator<T> =
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
fun <T : Number> NumberValidator<T>.notNegative(message: MessageProvider = Message.resource()): NumberValidator<T> =
    constrain("kova.number.notNegative") {
        satisfies(it.input.toDouble() >= 0.0, message())
    }
