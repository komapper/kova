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
fun <T> IdentityValidator<T>.min(
    value: T,
    message: MessageProvider1<T, T> = Message.resource1("kova.number.min"),
): IdentityValidator<T> where T : Number, T : Comparable<T> = constrain(message.id, Constraints.min(value, message))

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
fun <T> IdentityValidator<T>.max(
    value: T,
    message: MessageProvider1<T, T> = Message.resource1("kova.number.max"),
): IdentityValidator<T> where T : Number, T : Comparable<T> = constrain(message.id, Constraints.max(value, message))

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
fun <T> IdentityValidator<T>.gt(
    value: T,
    message: MessageProvider1<T, T> = Message.resource1("kova.number.gt"),
): IdentityValidator<T> where T : Number, T : Comparable<T> = constrain(message.id, Constraints.gt(value, message))

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
fun <T> IdentityValidator<T>.gte(
    value: T,
    message: MessageProvider1<T, T> = Message.resource1("kova.number.gte"),
): IdentityValidator<T> where T : Number, T : Comparable<T> = constrain(message.id, Constraints.gte(value, message))

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
fun <T> IdentityValidator<T>.lt(
    value: T,
    message: MessageProvider1<T, T> = Message.resource1("kova.number.lt"),
): IdentityValidator<T> where T : Number, T : Comparable<T> = constrain(message.id, Constraints.lt(value, message))

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
fun <T> IdentityValidator<T>.lte(
    value: T,
    message: MessageProvider1<T, T> = Message.resource1("kova.number.lte"),
): IdentityValidator<T> where T : Number, T : Comparable<T> = constrain(message.id, Constraints.lte(value, message))

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
fun <T> IdentityValidator<T>.positive(
    message: MessageProvider0<T> = Message.resource0("kova.number.positive"),
): IdentityValidator<T> where T : Number, T : Comparable<T> =
    constrain(message.id) {
        satisfies(it.input.toDouble() > 0.0, message(it))
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
fun <T> IdentityValidator<T>.negative(
    message: MessageProvider0<T> = Message.resource0("kova.number.negative"),
): IdentityValidator<T> where T : Number, T : Comparable<T> =
    constrain(message.id) {
        satisfies(it.input.toDouble() < 0.0, message(it))
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
fun <T> IdentityValidator<T>.notPositive(
    message: MessageProvider0<T> = Message.resource0("kova.number.notPositive"),
): IdentityValidator<T> where T : Number, T : Comparable<T> =
    constrain(message.id) {
        satisfies(it.input.toDouble() <= 0.0, message(it))
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
fun <T> IdentityValidator<T>.notNegative(
    message: MessageProvider0<T> = Message.resource0("kova.number.notNegative"),
): IdentityValidator<T> where T : Number, T : Comparable<T> =
    constrain(message.id) {
        satisfies(it.input.toDouble() >= 0.0, message(it))
    }
