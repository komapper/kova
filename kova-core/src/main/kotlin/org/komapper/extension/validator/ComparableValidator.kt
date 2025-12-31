package org.komapper.extension.validator

/**
 * Validates that the number is greater than or equal to the specified minimum value.
 *
 * Example:
 * ```kotlin
 * tryValidate { minValue(10, 0) }  // Success
 * tryValidate { minValue(-1, 0) }  // Failure
 * ```
 *
 * @param value Minimum value (inclusive)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <S : Comparable<S>> Validation.minValue(
    input: S,
    value: S,
    message: MessageProvider = { "kova.comparable.minValue".resource(value) },
) = input.constrain("kova.comparable.minValue") { satisfies(it >= value, message) }

/**
 * Validates that the number is less than or equal to the specified maximum value.
 *
 * Example:
 * ```kotlin
 * tryValidate { maxValue(50, 100) }   // Success
 * tryValidate { maxValue(150, 100) }  // Failure
 * ```
 *
 * @param value Maximum value (inclusive)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <S : Comparable<S>> Validation.maxValue(
    input: S,
    value: S,
    message: MessageProvider = { "kova.comparable.maxValue".resource(value) },
) = input.constrain("kova.comparable.maxValue") { satisfies(it <= value, message) }

/**
 * Validates that the number is strictly greater than the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { gtValue(1, 0) }   // Success
 * tryValidate { gtValue(0, 0) }   // Failure
 * tryValidate { gtValue(-1, 0) }  // Failure
 * ```
 *
 * @param value The value to compare against (exclusive)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <S : Comparable<S>> Validation.gtValue(
    input: S,
    value: S,
    message: MessageProvider = { "kova.comparable.gtValue".resource(value) },
) = input.constrain("kova.comparable.gtValue") { satisfies(it > value, message) }

/**
 * Validates that the number is greater than or equal to the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { gteValue(1, 0) }   // Success
 * tryValidate { gteValue(0, 0) }   // Success
 * tryValidate { gteValue(-1, 0) }  // Failure
 * ```
 *
 * @param value The value to compare against (inclusive)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <S : Comparable<S>> Validation.gteValue(
    input: S,
    value: S,
    message: MessageProvider = { "kova.comparable.gteValue".resource(value) },
) = input.constrain("kova.comparable.gteValue") { satisfies(it >= value, message) }

/**
 * Validates that the number is strictly less than the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { ltValue(50, 100) }   // Success
 * tryValidate { ltValue(100, 100) }  // Failure
 * tryValidate { ltValue(150, 100) }  // Failure
 * ```
 *
 * @param value The value to compare against (exclusive)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <S : Comparable<S>> Validation.ltValue(
    input: S,
    value: S,
    message: MessageProvider = { "kova.comparable.ltValue".resource(value) },
) = input.constrain("kova.comparable.ltValue") { satisfies(it < value, message) }

/**
 * Validates that the number is less than or equal to the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { lteValue(50, 100) }   // Success
 * tryValidate { lteValue(100, 100) }  // Success
 * tryValidate { lteValue(150, 100) }  // Failure
 * ```
 *
 * @param value The value to compare against (inclusive)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <S : Comparable<S>> Validation.lteValue(
    input: S,
    value: S,
    message: MessageProvider = { "kova.comparable.lteValue".resource(value) },
) = input.constrain("kova.comparable.lteValue") { satisfies(it <= value, message) }

/**
 * Validates that the value is equal to the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { eqValue(42, 42) }  // Success
 * tryValidate { eqValue(41, 42) }  // Failure
 * tryValidate { eqValue(43, 42) }  // Failure
 * ```
 *
 * @param value The value to compare against
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <S : Comparable<S>> Validation.eqValue(
    input: S,
    value: S,
    message: MessageProvider = { "kova.comparable.eqValue".resource(value) },
) = input.constrain("kova.comparable.eqValue") { satisfies(it == value, message) }

/**
 * Validates that the value is not equal to the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { notEqValue(1, 0) }   // Success
 * tryValidate { notEqValue(-1, 0) }  // Success
 * tryValidate { notEqValue(0, 0) }   // Failure
 * ```
 *
 * @param value The value to compare against
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <S : Comparable<S>> Validation.notEqValue(
    input: S,
    value: S,
    message: MessageProvider = { "kova.comparable.notEqValue".resource(value) },
) = input.constrain("kova.comparable.notEqValue") { satisfies(it != value, message) }
