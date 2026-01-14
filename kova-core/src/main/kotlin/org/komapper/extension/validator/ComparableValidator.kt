package org.komapper.extension.validator

/**
 * Validates that the value is greater than or equal to the specified minimum value.
 *
 * Example:
 * ```kotlin
 * tryValidate { 10.ensureAtLeast(0) }  // Success
 * tryValidate { (-1).ensureAtLeast(0) }  // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param S The comparable type being validated
 * @receiver The comparable value to validate
 * @param value Minimum value (inclusive)
 * @param message Custom error message provider
 * @return The validated input value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun <S : Comparable<S>> S.ensureAtLeast(
    value: S,
    message: MessageProvider = { "kova.comparable.atLeast".resource(value) },
): S = constrain("kova.comparable.atLeast") { satisfies(it >= value, message) }

/**
 * Validates that the value is less than or equal to the specified maximum value.
 *
 * Example:
 * ```kotlin
 * tryValidate { 50.ensureAtMost(100) }   // Success
 * tryValidate { 150.ensureAtMost(100) }  // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param S The comparable type being validated
 * @receiver The comparable value to validate
 * @param value Maximum value (inclusive)
 * @param message Custom error message provider
 * @return The validated input value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun <S : Comparable<S>> S.ensureAtMost(
    value: S,
    message: MessageProvider = { "kova.comparable.atMost".resource(value) },
): S = constrain("kova.comparable.atMost") { satisfies(it <= value, message) }

/**
 * Validates that the value is strictly greater than the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { 1.ensureGreaterThan(0) }   // Success
 * tryValidate { 0.ensureGreaterThan(0) }   // Failure
 * tryValidate { (-1).ensureGreaterThan(0) }  // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param S The comparable type being validated
 * @receiver The comparable value to validate
 * @param value The value to compare against (exclusive)
 * @param message Custom error message provider
 * @return The validated input value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun <S : Comparable<S>> S.ensureGreaterThan(
    value: S,
    message: MessageProvider = { "kova.comparable.greaterThan".resource(value) },
): S = constrain("kova.comparable.greaterThan") { satisfies(it > value, message) }

/**
 * Validates that the value is strictly less than the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { 50.ensureLessThan(100) }   // Success
 * tryValidate { 100.ensureLessThan(100) }  // Failure
 * tryValidate { 150.ensureLessThan(100) }  // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param S The comparable type being validated
 * @receiver The comparable value to validate
 * @param value The value to compare against (exclusive)
 * @param message Custom error message provider
 * @return The validated input value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun <S : Comparable<S>> S.ensureLessThan(
    value: S,
    message: MessageProvider = { "kova.comparable.lessThan".resource(value) },
): S = constrain("kova.comparable.lessThan") { satisfies(it < value, message) }

/**
 * Validates that the value is within the specified range.
 *
 * This validator supports ranges that implement both ClosedRange and OpenEndRange interfaces,
 * such as IntRange, LongRange, CharRange, etc. It accepts both closed range syntax (1..10)
 * and open-ended range syntax (1..<10).
 *
 * Example with closed range syntax (1..10):
 * ```kotlin
 * tryValidate { 5.ensureInRange(1..10) }    // Success
 * tryValidate { 1.ensureInRange(1..10) }    // Success (inclusive start)
 * tryValidate { 10.ensureInRange(1..10) }   // Success (inclusive end)
 * tryValidate { 0.ensureInRange(1..10) }    // Failure
 * tryValidate { 11.ensureInRange(1..10) }   // Failure
 * ```
 *
 * Example with open-ended range syntax (1..<10):
 * ```kotlin
 * tryValidate { 5.ensureInRange(1..<10) }   // Success
 * tryValidate { 1.ensureInRange(1..<10) }   // Success (inclusive start)
 * tryValidate { 9.ensureInRange(1..<10) }   // Success
 * tryValidate { 10.ensureInRange(1..<10) }  // Failure (exclusive end)
 * tryValidate { 0.ensureInRange(1..<10) }   // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param S The comparable type being validated
 * @param R The range type that implements both ClosedRange and OpenEndRange
 * @receiver The comparable value to validate
 * @param range The range to check against (must implement both ClosedRange and OpenEndRange)
 * @param message Custom error message provider
 * @return The validated input value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun <S : Comparable<S>, R> S.ensureInRange(
    range: R,
    message: MessageProvider = { "kova.comparable.inRange".resource(range) },
): S where R : ClosedRange<S>, R : OpenEndRange<S> = constrain("kova.comparable.inRange") { satisfies(it in range, message) }

/**
 * Validates that the value is within the specified closed range.
 *
 * A closed range includes both the start and end values (inclusive on both ends).
 *
 * Example:
 * ```kotlin
 * tryValidate { 5.ensureInClosedRange(1.0..10.0) }    // Success
 * tryValidate { 1.ensureInClosedRange(1.0..10.0) }    // Success (inclusive start)
 * tryValidate { 10.ensureInClosedRange(1.0..10.0) }   // Success (inclusive end)
 * tryValidate { 0.ensureInClosedRange(1.0..10.0) }    // Failure
 * tryValidate { 11.ensureInClosedRange(1.0..10.0) }   // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param S The comparable type being validated
 * @receiver The comparable value to validate
 * @param range The closed range to check against
 * @param message Custom error message provider
 * @return The validated input value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun <S : Comparable<S>> S.ensureInClosedRange(
    range: ClosedRange<S>,
    message: MessageProvider = { "kova.comparable.inClosedRange".resource(range) },
): S = constrain("kova.comparable.inClosedRange") { satisfies(it in range, message) }

/**
 * Validates that the value is within the specified open-ended range.
 *
 * An open-ended range includes the start value but excludes the end value (inclusive start, exclusive end).
 *
 * Example:
 * ```kotlin
 * tryValidate { 5.ensureInOpenEndRange(1..<10) }    // Success
 * tryValidate { 1.ensureInOpenEndRange(1..<10) }    // Success (inclusive start)
 * tryValidate { 9.ensureInOpenEndRange(1..<10) }    // Success
 * tryValidate { 10.ensureInOpenEndRange(1..<10) }   // Failure (exclusive end)
 * tryValidate { 0.ensureInOpenEndRange(1..<10) }    // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param S The comparable type being validated
 * @receiver The comparable value to validate
 * @param range The open-ended range to check against
 * @param message Custom error message provider
 * @return The validated input value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun <S : Comparable<S>> S.ensureInOpenEndRange(
    range: OpenEndRange<S>,
    message: MessageProvider = { "kova.comparable.inOpenEndRange".resource(range) },
): S = constrain("kova.comparable.inOpenEndRange") { satisfies(it in range, message) }
