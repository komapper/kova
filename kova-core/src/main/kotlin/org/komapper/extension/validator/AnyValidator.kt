package org.komapper.extension.validator

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
