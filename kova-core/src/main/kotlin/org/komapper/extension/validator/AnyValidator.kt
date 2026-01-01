package org.komapper.extension.validator

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
fun <S> Validation.eqValue(
    input: S,
    value: S,
    message: MessageProvider = { "kova.any.eqValue".resource(value) },
) = input.constrain("kova.any.eqValue") { satisfies(it == value, message) }

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
fun <S> Validation.notEqValue(
    input: S,
    value: S,
    message: MessageProvider = { "kova.any.notEqValue".resource(value) },
) = input.constrain("kova.any.notEqValue") { satisfies(it != value, message) }

/**
 * Validates that the input value is contained in the specified iterable.
 *
 * This constraint checks if the input value is present in the given iterable
 * using the `in` operator (element equality check). Uses the "kova.any.inIterable"
 * constraint ID.
 *
 * Example:
 * ```kotlin
 * tryValidate { inIterable("bbb", listOf("aaa", "bbb", "ccc")) }  // Success
 * tryValidate { inIterable("ddd", listOf("aaa", "bbb", "ccc")) }  // Failure
 * ```
 *
 * @param iterable The iterable that must contain the input value
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <S> Validation.inIterable(
    input: S,
    iterable: Iterable<S>,
    message: MessageProvider = { "kova.any.inIterable".resource(iterable) },
) = input.constrain("kova.any.inIterable") { satisfies(it in iterable, message) }
