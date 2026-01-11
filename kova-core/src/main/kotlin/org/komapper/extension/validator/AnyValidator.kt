package org.komapper.extension.validator

/**
 * Validates that the value is equal to the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { 42.ensureEquals(42) }  // Success
 * tryValidate { 41.ensureEquals(42) }  // Failure
 * tryValidate { 43.ensureEquals(42) }  // Failure
 * ```
 *
 * @param value The value to compare against
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
public fun <S> S.ensureEquals(
    value: S,
    message: MessageProvider = { "kova.any.equals".resource(value) },
): S = constrain("kova.any.equals") { satisfies(it == value, message) }

/**
 * Validates that the value is not equal to the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { 1.ensureNotEquals(0) }   // Success
 * tryValidate { (-1).ensureNotEquals(0) }  // Success
 * tryValidate { 0.ensureNotEquals(0) }   // Failure
 * ```
 *
 * @param value The value to compare against
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
public fun <S> S.ensureNotEquals(
    value: S,
    message: MessageProvider = { "kova.any.notEquals".resource(value) },
): S = constrain("kova.any.notEquals") { satisfies(it != value, message) }

/**
 * Validates that the value is contained in the specified iterable.
 *
 * This constraint checks if the value is present in the given iterable
 * using the `in` operator (element equality check). Uses the "kova.any.in"
 * constraint ID.
 *
 * Example:
 * ```kotlin
 * tryValidate { "bbb".ensureIn(listOf("aaa", "bbb", "ccc")) }  // Success
 * tryValidate { "ddd".ensureIn(listOf("aaa", "bbb", "ccc")) }  // Failure
 * ```
 *
 * @param iterable The iterable that must contain the value
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
public fun <S> S.ensureIn(
    iterable: Iterable<S>,
    message: MessageProvider = { "kova.any.in".resource(iterable) },
): S = constrain("kova.any.in") { satisfies(it in iterable, message) }
