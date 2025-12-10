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
    message: MessageProvider = Message.resource(),
): IdentityValidator<T> =
    constrain("kova.comparable.min") {
        satisfies(it.input >= value, message(value))
    }

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
    message: MessageProvider = Message.resource(),
): IdentityValidator<T> =
    constrain("kova.comparable.max") {
        satisfies(it.input <= value, message(value))
    }

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
    message: MessageProvider = Message.resource(),
): IdentityValidator<T> =
    constrain("kova.comparable.gt") {
        satisfies(it.input > value, message(value))
    }

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
    message: MessageProvider = Message.resource(),
): IdentityValidator<T> =
    constrain("kova.comparable.gte") {
        satisfies(it.input >= value, message(value))
    }

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
    message: MessageProvider = Message.resource(),
): IdentityValidator<T> =
    constrain("kova.comparable.lt") {
        satisfies(it.input < value, message(value))
    }

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
    message: MessageProvider = Message.resource(),
): IdentityValidator<T> =
    constrain("kova.comparable.lte") {
        satisfies(it.input <= value, message(value))
    }
