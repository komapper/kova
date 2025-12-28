package org.komapper.extension.validator

/**
 * Validates that the number is greater than or equal to the specified minimum value.
 *
 * Example:
 * ```kotlin
 * tryValidate { min(10, 0) }  // Success
 * tryValidate { min(-1, 0) }  // Failure
 * ```
 *
 * @param value Minimum value (inclusive)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <S : Comparable<S>> Validation.min(
    input: S,
    value: S,
    message: MessageProvider = { "kova.comparable.min".resource(value) },
) = input.constrain("kova.comparable.min") { satisfies(it >= value, message) }

/**
 * Validates that the number is less than or equal to the specified maximum value.
 *
 * Example:
 * ```kotlin
 * tryValidate { max(50, 100) }   // Success
 * tryValidate { max(150, 100) }  // Failure
 * ```
 *
 * @param value Maximum value (inclusive)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <S : Comparable<S>> Validation.max(
    input: S,
    value: S,
    message: MessageProvider = { "kova.comparable.max".resource(value) },
) = input.constrain("kova.comparable.max") { satisfies(it <= value, message) }

/**
 * Validates that the number is strictly greater than the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { gt(1, 0) }   // Success
 * tryValidate { gt(0, 0) }   // Failure
 * tryValidate { gt(-1, 0) }  // Failure
 * ```
 *
 * @param value The value to compare against (exclusive)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <S : Comparable<S>> Validation.gt(
    input: S,
    value: S,
    message: MessageProvider = { "kova.comparable.gt".resource(value) },
) = input.constrain("kova.comparable.gt") { satisfies(it > value, message) }

/**
 * Validates that the number is greater than or equal to the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { gte(1, 0) }   // Success
 * tryValidate { gte(0, 0) }   // Success
 * tryValidate { gte(-1, 0) }  // Failure
 * ```
 *
 * @param value The value to compare against (inclusive)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <S : Comparable<S>> Validation.gte(
    input: S,
    value: S,
    message: MessageProvider = { "kova.comparable.gte".resource(value) },
) = input.constrain("kova.comparable.gte") { satisfies(it >= value, message) }

/**
 * Validates that the number is strictly less than the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { lt(50, 100) }   // Success
 * tryValidate { lt(100, 100) }  // Failure
 * tryValidate { lt(150, 100) }  // Failure
 * ```
 *
 * @param value The value to compare against (exclusive)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <S : Comparable<S>> Validation.lt(
    input: S,
    value: S,
    message: MessageProvider = { "kova.comparable.lt".resource(value) },
) = input.constrain("kova.comparable.lt") { satisfies(it < value, message) }

/**
 * Validates that the number is less than or equal to the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { lte(50, 100) }   // Success
 * tryValidate { lte(100, 100) }  // Success
 * tryValidate { lte(150, 100) }  // Failure
 * ```
 *
 * @param value The value to compare against (inclusive)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <S : Comparable<S>> Validation.lte(
    input: S,
    value: S,
    message: MessageProvider = { "kova.comparable.lte".resource(value) },
) = input.constrain("kova.comparable.lte") { satisfies(it <= value, message) }

/**
 * Validates that the value is equal to the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { eq(42, 42) }  // Success
 * tryValidate { eq(41, 42) }  // Failure
 * tryValidate { eq(43, 42) }  // Failure
 * ```
 *
 * @param value The value to compare against
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <S : Comparable<S>> Validation.eq(
    input: S,
    value: S,
    message: MessageProvider = { "kova.comparable.eq".resource(value) },
) = input.constrain("kova.comparable.eq") { satisfies(it == value, message) }

/**
 * Validates that the value is not equal to the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { notEq(1, 0) }   // Success
 * tryValidate { notEq(-1, 0) }  // Success
 * tryValidate { notEq(0, 0) }   // Failure
 * ```
 *
 * @param value The value to compare against
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <S : Comparable<S>> Validation.notEq(
    input: S,
    value: S,
    message: MessageProvider = { "kova.comparable.notEq".resource(value) },
) = input.constrain("kova.comparable.notEq") { satisfies(it != value, message) }
