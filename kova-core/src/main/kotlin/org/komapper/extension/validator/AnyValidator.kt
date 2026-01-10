package org.komapper.extension.validator

/**
 * Validates that the value is equal to the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureEquals(42, 42) }  // Success
 * tryValidate { ensureEquals(41, 42) }  // Failure
 * tryValidate { ensureEquals(43, 42) }  // Failure
 * ```
 *
 * @param value The value to compare against
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun <S> ensureEquals(
    input: S,
    value: S,
    message: MessageProvider = { "kova.any.equals".resource(value) },
) = input.constrain("kova.any.equals") { satisfies(it == value, message) }

/**
 * Validates that the value is not equal to the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureNotEquals(1, 0) }   // Success
 * tryValidate { ensureNotEquals(-1, 0) }  // Success
 * tryValidate { ensureNotEquals(0, 0) }   // Failure
 * ```
 *
 * @param value The value to compare against
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun <S> ensureNotEquals(
    input: S,
    value: S,
    message: MessageProvider = { "kova.any.notEquals".resource(value) },
) = input.constrain("kova.any.notEquals") { satisfies(it != value, message) }

/**
 * Validates that the input value is contained in the specified iterable.
 *
 * This constraint checks if the input value is present in the given iterable
 * using the `in` operator (element equality check). Uses the "kova.any.in"
 * constraint ID.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureIn("bbb", listOf("aaa", "bbb", "ccc")) }  // Success
 * tryValidate { ensureIn("ddd", listOf("aaa", "bbb", "ccc")) }  // Failure
 * ```
 *
 * @param iterable The iterable that must contain the input value
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun <S> ensureIn(
    input: S,
    iterable: Iterable<S>,
    message: MessageProvider = { "kova.any.in".resource(iterable) },
) = input.constrain("kova.any.in") { satisfies(it in iterable, message) }
