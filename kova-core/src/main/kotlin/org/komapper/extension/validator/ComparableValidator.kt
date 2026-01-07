package org.komapper.extension.validator

/**
 * Validates that the number is greater than or equal to the specified minimum value.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureMin(10, 0) }  // Success
 * tryValidate { ensureMin(-1, 0) }  // Failure
 * ```
 *
 * @param value Minimum value (inclusive)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <S : Comparable<S>> Validation.ensureMin(
    input: S,
    value: S,
    message: MessageProvider = { "kova.comparable.min".resource(value) },
) = input.constrain("kova.comparable.min") { satisfies(it >= value, message) }

/**
 * Validates that the number is less than or equal to the specified maximum value.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureMax(50, 100) }   // Success
 * tryValidate { ensureMax(150, 100) }  // Failure
 * ```
 *
 * @param value Maximum value (inclusive)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <S : Comparable<S>> Validation.ensureMax(
    input: S,
    value: S,
    message: MessageProvider = { "kova.comparable.max".resource(value) },
) = input.constrain("kova.comparable.max") { satisfies(it <= value, message) }

/**
 * Validates that the number is strictly greater than the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureGreaterThan(1, 0) }   // Success
 * tryValidate { ensureGreaterThan(0, 0) }   // Failure
 * tryValidate { ensureGreaterThan(-1, 0) }  // Failure
 * ```
 *
 * @param value The value to compare against (exclusive)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <S : Comparable<S>> Validation.ensureGreaterThan(
    input: S,
    value: S,
    message: MessageProvider = { "kova.comparable.greaterThan".resource(value) },
) = input.constrain("kova.comparable.greaterThan") { satisfies(it > value, message) }

/**
 * Validates that the number is greater than or equal to the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureGreaterThanOrEqual(1, 0) }   // Success
 * tryValidate { ensureGreaterThanOrEqual(0, 0) }   // Success
 * tryValidate { ensureGreaterThanOrEqual(-1, 0) }  // Failure
 * ```
 *
 * @param value The value to compare against (inclusive)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <S : Comparable<S>> Validation.ensureGreaterThanOrEqual(
    input: S,
    value: S,
    message: MessageProvider = { "kova.comparable.greaterThanOrEquals".resource(value) },
) = input.constrain("kova.comparable.greaterThanOrEquals") { satisfies(it >= value, message) }

/**
 * Validates that the number is strictly less than the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureLessThan(50, 100) }   // Success
 * tryValidate { ensureLessThan(100, 100) }  // Failure
 * tryValidate { ensureLessThan(150, 100) }  // Failure
 * ```
 *
 * @param value The value to compare against (exclusive)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <S : Comparable<S>> Validation.ensureLessThan(
    input: S,
    value: S,
    message: MessageProvider = { "kova.comparable.lessThan".resource(value) },
) = input.constrain("kova.comparable.lessThan") { satisfies(it < value, message) }

/**
 * Validates that the number is less than or equal to the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureLessThanOrEqual(50, 100) }   // Success
 * tryValidate { ensureLessThanOrEqual(100, 100) }  // Success
 * tryValidate { ensureLessThanOrEqual(150, 100) }  // Failure
 * ```
 *
 * @param value The value to compare against (inclusive)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <S : Comparable<S>> Validation.ensureLessThanOrEqual(
    input: S,
    value: S,
    message: MessageProvider = { "kova.comparable.lessThanOrEquals".resource(value) },
) = input.constrain("kova.comparable.lessThanOrEquals") { satisfies(it <= value, message) }

/**
 * Validates that the value is within the specified range.
 *
 * This validator supports ranges that implement both ClosedRange and OpenEndRange interfaces,
 * such as IntRange, LongRange, CharRange, etc. It accepts both closed range syntax (1..10)
 * and open-ended range syntax (1..<10).
 *
 * Example with closed range syntax (1..10):
 * ```kotlin
 * tryValidate { ensureInRange(5, 1..10) }    // Success
 * tryValidate { ensureInRange(1, 1..10) }    // Success (inclusive start)
 * tryValidate { ensureInRange(10, 1..10) }   // Success (inclusive end)
 * tryValidate { ensureInRange(0, 1..10) }    // Failure
 * tryValidate { ensureInRange(11, 1..10) }   // Failure
 * ```
 *
 * Example with open-ended range syntax (1..<10):
 * ```kotlin
 * tryValidate { ensureInRange(5, 1..<10) }   // Success
 * tryValidate { ensureInRange(1, 1..<10) }   // Success (inclusive start)
 * tryValidate { ensureInRange(9, 1..<10) }   // Success
 * tryValidate { ensureInRange(10, 1..<10) }  // Failure (exclusive end)
 * tryValidate { ensureInRange(0, 1..<10) }   // Failure
 * ```
 *
 * @param range The range to check against (must implement both ClosedRange and OpenEndRange)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <S : Comparable<S>, R> Validation.ensureInRange(
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
 * tryValidate { ensureInClosedRange(5, 1.0..10.0) }    // Success
 * tryValidate { ensureInClosedRange(1, 1.0..10.0) }    // Success (inclusive start)
 * tryValidate { ensureInClosedRange(10, 1.0..10.0) }   // Success (inclusive end)
 * tryValidate { ensureInClosedRange(0, 1.0..10.0) }    // Failure
 * tryValidate { ensureInClosedRange(11, 1.0..10.0) }   // Failure
 * ```
 *
 * @param range The closed range to check against
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <S : Comparable<S>> Validation.ensureInClosedRange(
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
 * tryValidate { ensureInOpenEndRange(5, 1..<10) }    // Success
 * tryValidate { ensureInOpenEndRange(1, 1..<10) }    // Success (inclusive start)
 * tryValidate { ensureInOpenEndRange(9, 1..<10) }    // Success
 * tryValidate { ensureInOpenEndRange(10, 1..<10) }   // Failure (exclusive end)
 * tryValidate { ensureInOpenEndRange(0, 1..<10) }    // Failure
 * ```
 *
 * @param range The open-ended range to check against
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <S : Comparable<S>> Validation.ensureInOpenEndRange(
    input: S,
    range: OpenEndRange<S>,
    message: MessageProvider = { "kova.comparable.inOpenEndRange".resource(range) },
) = input.constrain("kova.comparable.inOpenEndRange") { satisfies(it in range, message) }
