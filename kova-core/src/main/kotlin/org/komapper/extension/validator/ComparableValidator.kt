package org.komapper.extension.validator

/**
 * Type alias for validators that validate comparable types.
 *
 * This is semantically equivalent to [IdentityValidator] but provides clearer intent
 * that the validator is specifically for types implementing [Comparable], enabling
 * comparison constraints like min, max, gt, lt, etc.
 *
 * Using this type alias improves code readability by making it explicit that
 * comparison-based constraints are being applied.
 *
 * Example:
 * ```kotlin
 * val validator: ComparableValidator<Int> = Kova.int().min(0).max(100)
 * val dateValidator: ComparableValidator<LocalDate> = Kova.temporal<LocalDate>().min(LocalDate.now())
 * ```
 */
typealias ComparableValidator<T> = IdentityValidator<T>

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
fun <T : Comparable<T>> ComparableValidator<T>.min(
    value: T,
    message: MessageProvider<T> = { resource(value) },
): ComparableValidator<T> =
    constrain("kova.comparable.min") {
        satisfies(input >= value, message)
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
fun <T : Comparable<T>> ComparableValidator<T>.max(
    value: T,
    message: MessageProvider<T> = { resource(value) },
): ComparableValidator<T> =
    constrain("kova.comparable.max") {
        satisfies(input <= value, message)
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
fun <T : Comparable<T>> ComparableValidator<T>.gt(
    value: T,
    message: MessageProvider<T> = { resource(value) },
): ComparableValidator<T> =
    constrain("kova.comparable.gt") {
        satisfies(input > value, message)
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
fun <T : Comparable<T>> ComparableValidator<T>.gte(
    value: T,
    message: MessageProvider<T> = { resource(value) },
): ComparableValidator<T> =
    constrain("kova.comparable.gte") {
        satisfies(input >= value, message)
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
fun <T : Comparable<T>> ComparableValidator<T>.lt(
    value: T,
    message: MessageProvider<T> = { resource(value) },
): ComparableValidator<T> =
    constrain("kova.comparable.lt") {
        satisfies(input < value, message)
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
fun <T : Comparable<T>> ComparableValidator<T>.lte(
    value: T,
    message: MessageProvider<T> = { resource(value) },
): ComparableValidator<T> =
    constrain("kova.comparable.lte") {
        satisfies(input <= value, message)
    }

/**
 * Validates that the value is equal to the specified value.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.int().eq(42)
 * validator.validate(42)  // Success
 * validator.validate(41)  // Failure
 * validator.validate(43)  // Failure
 * ```
 *
 * @param value The value to compare against
 * @param message Custom error message provider
 * @return A new validator with the equality constraint
 */
fun <T : Comparable<T>> ComparableValidator<T>.eq(
    value: T,
    message: MessageProvider<T> = { resource(value) },
): ComparableValidator<T> =
    constrain("kova.comparable.eq") {
        satisfies(input == value, message)
    }

/**
 * Validates that the value is not equal to the specified value.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.int().notEq(0)
 * validator.validate(1)   // Success
 * validator.validate(-1)  // Success
 * validator.validate(0)   // Failure
 * ```
 *
 * @param value The value to compare against
 * @param message Custom error message provider
 * @return A new validator with the inequality constraint
 */
fun <T : Comparable<T>> ComparableValidator<T>.notEq(
    value: T,
    message: MessageProvider<T> = { resource(value) },
): ComparableValidator<T> =
    constrain("kova.comparable.notEq") {
        satisfies(input != value, message)
    }
