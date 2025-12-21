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
typealias ComparableValidator<T, S> = Validator<T, S>

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
fun <T, S : Comparable<S>> ComparableValidator<T, S>.min(
    value: S,
    message: MessageProvider = { "kova.comparable.min".resource(value) },
): ComparableValidator<T, S> = constrain("kova.comparable.min") { satisfies(it >= value, message) }

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
fun <T, S : Comparable<S>> ComparableValidator<T, S>.max(
    value: S,
    message: MessageProvider = { "kova.comparable.max".resource(value) },
): ComparableValidator<T, S> = constrain("kova.comparable.max") { satisfies(it <= value, message) }

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
fun <T, S : Comparable<S>> ComparableValidator<T, S>.gt(
    value: S,
    message: MessageProvider = { "kova.comparable.gt".resource(value) },
): ComparableValidator<T, S> = constrain("kova.comparable.gt") { satisfies(it > value, message) }

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
fun <T, S : Comparable<S>> ComparableValidator<T, S>.gte(
    value: S,
    message: MessageProvider = { "kova.comparable.gte".resource(value) },
): ComparableValidator<T, S> = constrain("kova.comparable.gte") { satisfies(it >= value, message) }

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
fun <T, S : Comparable<S>> ComparableValidator<T, S>.lt(
    value: S,
    message: MessageProvider = { "kova.comparable.lt".resource(value) },
): ComparableValidator<T, S> = constrain("kova.comparable.lt") { satisfies(it < value, message) }

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
fun <T, S : Comparable<S>> ComparableValidator<T, S>.lte(
    value: S,
    message: MessageProvider = { "kova.comparable.lte".resource(value) },
): ComparableValidator<T, S> = constrain("kova.comparable.lte") { satisfies(it <= value, message) }

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
fun <T, S : Comparable<S>> ComparableValidator<T, S>.eq(
    value: S,
    message: MessageProvider = { "kova.comparable.eq".resource(value) },
): ComparableValidator<T, S> = constrain("kova.comparable.eq") { satisfies(it == value, message) }

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
fun <T, S : Comparable<S>> ComparableValidator<T, S>.notEq(
    value: S,
    message: MessageProvider = { "kova.comparable.notEq".resource(value) },
): ComparableValidator<T, S> = constrain("kova.comparable.notEq") { satisfies(it != value, message) }
