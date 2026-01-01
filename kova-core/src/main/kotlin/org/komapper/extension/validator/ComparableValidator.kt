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
 * tryValidate { gtEqValue(1, 0) }   // Success
 * tryValidate { gtEqValue(0, 0) }   // Success
 * tryValidate { gtEqValue(-1, 0) }  // Failure
 * ```
 *
 * @param value The value to compare against (inclusive)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <S : Comparable<S>> Validation.gtEqValue(
    input: S,
    value: S,
    message: MessageProvider = { "kova.comparable.gtEqValue".resource(value) },
) = input.constrain("kova.comparable.gtEqValue") { satisfies(it >= value, message) }

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
 * tryValidate { ltEqValue(50, 100) }   // Success
 * tryValidate { ltEqValue(100, 100) }  // Success
 * tryValidate { ltEqValue(150, 100) }  // Failure
 * ```
 *
 * @param value The value to compare against (inclusive)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <S : Comparable<S>> Validation.ltEqValue(
    input: S,
    value: S,
    message: MessageProvider = { "kova.comparable.ltEqValue".resource(value) },
) = input.constrain("kova.comparable.ltEqValue") { satisfies(it <= value, message) }

/**
 * Validates that the value is within the specified range.
 *
 * This validator supports ranges that implement both ClosedRange and OpenEndRange interfaces,
 * such as IntRange, LongRange, CharRange, etc. It accepts both closed range syntax (1..10)
 * and open-ended range syntax (1..<10).
 *
 * Example with closed range syntax (1..10):
 * ```kotlin
 * tryValidate { inRange(5, 1..10) }    // Success
 * tryValidate { inRange(1, 1..10) }    // Success (inclusive start)
 * tryValidate { inRange(10, 1..10) }   // Success (inclusive end)
 * tryValidate { inRange(0, 1..10) }    // Failure
 * tryValidate { inRange(11, 1..10) }   // Failure
 * ```
 *
 * Example with open-ended range syntax (1..<10):
 * ```kotlin
 * tryValidate { inRange(5, 1..<10) }   // Success
 * tryValidate { inRange(1, 1..<10) }   // Success (inclusive start)
 * tryValidate { inRange(9, 1..<10) }   // Success
 * tryValidate { inRange(10, 1..<10) }  // Failure (exclusive end)
 * tryValidate { inRange(0, 1..<10) }   // Failure
 * ```
 *
 * @param range The range to check against (must implement both ClosedRange and OpenEndRange)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <S : Comparable<S>, R> Validation.inRange(
    input: S,
    range: R,
    message: MessageProvider = { "kova.comparable.inRange".resource(range) },
) where R : ClosedRange<S>, R : OpenEndRange<S> = input.constrain("kova.comparable.inRange") { satisfies(it in range, message) }

/**
 * Validates that the value is within the specified closed range.
 *
 * A closed range includes both the start and end values (inclusive on both ends).
 *
 * Example:
 * ```kotlin
 * tryValidate { inClosedRange(5, 1.0..10.0) }    // Success
 * tryValidate { inClosedRange(1, 1.0..10.0) }    // Success (inclusive start)
 * tryValidate { inClosedRange(10, 1.0..10.0) }   // Success (inclusive end)
 * tryValidate { inClosedRange(0, 1.0..10.0) }    // Failure
 * tryValidate { inClosedRange(11, 1.0..10.0) }   // Failure
 * ```
 *
 * @param range The closed range to check against
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <S : Comparable<S>> Validation.inClosedRange(
    input: S,
    range: ClosedRange<S>,
    message: MessageProvider = { "kova.comparable.inClosedRange".resource(range) },
) = input.constrain("kova.comparable.inClosedRange") { satisfies(it in range, message) }

/**
 * Validates that the value is within the specified open-ended range.
 *
 * An open-ended range includes the start value but excludes the end value (inclusive start, exclusive end).
 *
 * Example:
 * ```kotlin
 * tryValidate { inOpenEndRange(5, 1..<10) }    // Success
 * tryValidate { inOpenEndRange(1, 1..<10) }    // Success (inclusive start)
 * tryValidate { inOpenEndRange(9, 1..<10) }    // Success
 * tryValidate { inOpenEndRange(10, 1..<10) }   // Failure (exclusive end)
 * tryValidate { inOpenEndRange(0, 1..<10) }    // Failure
 * ```
 *
 * @param range The open-ended range to check against
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <S : Comparable<S>> Validation.inOpenEndRange(
    input: S,
    range: OpenEndRange<S>,
    message: MessageProvider = { "kova.comparable.inOpenEndRange".resource(range) },
) = input.constrain("kova.comparable.inOpenEndRange") { satisfies(it in range, message) }
