package org.komapper.extension.validator

/**
 * Validates that the number is greater than or equal to the specified minimum value.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.int().min(0)
 * validator.validate(10)  // Success
 * validator.validate(-1)  // Failure
 * ```
 *
 * @param value Minimum value (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the minimum constraint
 */
fun <T : Comparable<T>> IdentityValidator<T>.min(
    value: T,
    message: MessageProvider<T> = Message.resource(),
): IdentityValidator<T> = constrain("kova.number.min", Constraints.min(value, message))

/**
 * Validates that the number is less than or equal to the specified maximum value.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.int().max(100)
 * validator.validate(50)   // Success
 * validator.validate(150)  // Failure
 * ```
 *
 * @param value Maximum value (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the maximum constraint
 */
fun <T : Comparable<T>> IdentityValidator<T>.max(
    value: T,
    message: MessageProvider<T> = Message.resource(),
): IdentityValidator<T> = constrain("kova.number.max", Constraints.max(value, message))

/**
 * Validates that the number is strictly greater than the specified value.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.int().gt(0)
 * validator.validate(1)   // Success
 * validator.validate(0)   // Failure
 * validator.validate(-1)  // Failure
 * ```
 *
 * @param value The value to compare against (exclusive)
 * @param message Custom error message provider
 * @return A new validator with the greater-than constraint
 */
fun <T : Comparable<T>> IdentityValidator<T>.gt(
    value: T,
    message: MessageProvider<T> = Message.resource(),
): IdentityValidator<T> = constrain("kova.number.gt", Constraints.gt(value, message))

/**
 * Validates that the number is greater than or equal to the specified value.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.int().gte(0)
 * validator.validate(1)   // Success
 * validator.validate(0)   // Success
 * validator.validate(-1)  // Failure
 * ```
 *
 * @param value The value to compare against (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the greater-than-or-equal constraint
 */
fun <T : Comparable<T>> IdentityValidator<T>.gte(
    value: T,
    message: MessageProvider<T> = Message.resource(),
): IdentityValidator<T> = constrain("kova.number.gte", Constraints.gte(value, message))

/**
 * Validates that the number is strictly less than the specified value.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.int().lt(100)
 * validator.validate(50)   // Success
 * validator.validate(100)  // Failure
 * validator.validate(150)  // Failure
 * ```
 *
 * @param value The value to compare against (exclusive)
 * @param message Custom error message provider
 * @return A new validator with the less-than constraint
 */
fun <T : Comparable<T>> IdentityValidator<T>.lt(
    value: T,
    message: MessageProvider<T> = Message.resource(),
): IdentityValidator<T> = constrain("kova.number.lt", Constraints.lt(value, message))

/**
 * Validates that the number is less than or equal to the specified value.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.int().lte(100)
 * validator.validate(50)   // Success
 * validator.validate(100)  // Success
 * validator.validate(150)  // Failure
 * ```
 *
 * @param value The value to compare against (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the less-than-or-equal constraint
 */
fun <T : Comparable<T>> IdentityValidator<T>.lte(
    value: T,
    message: MessageProvider<T> = Message.resource(),
): IdentityValidator<T> = constrain("kova.number.lte", Constraints.lte(value, message))

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
fun <T : Number> IdentityValidator<T>.positive(message: MessageProvider<T> = Message.resource()): IdentityValidator<T> =
    constrain("kova.number.positive") {
        satisfies(it.input.toDouble() > 0.0, message(it, it.input))
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
fun <T : Number> IdentityValidator<T>.negative(message: MessageProvider<T> = Message.resource()): IdentityValidator<T> =
    constrain("kova.number.negative") {
        satisfies(it.input.toDouble() < 0.0, message(it, it.input))
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
fun <T : Number> IdentityValidator<T>.notPositive(message: MessageProvider<T> = Message.resource()): IdentityValidator<T> =
    constrain("kova.number.notPositive") {
        satisfies(it.input.toDouble() <= 0.0, message(it, it.input))
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
fun <T : Number> IdentityValidator<T>.notNegative(message: MessageProvider<T> = Message.resource()): IdentityValidator<T> =
    constrain("kova.number.notNegative") {
        satisfies(it.input.toDouble() >= 0.0, message(it, it.input))
    }
