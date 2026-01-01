package org.komapper.extension.validator

typealias CountMessageProvider = (actualCount: Int) -> Message

/**
 * Validates that the iterable is not empty.
 *
 * Example:
 * ```kotlin
 * tryValidate { notEmpty(listOf("a")) } // Success
 * tryValidate { notEmpty(listOf()) }    // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.notEmpty(
    input: Iterable<*>,
    message: MessageProvider = { "kova.iterable.notEmpty".resource },
) = input.constrain("kova.iterable.notEmpty") { satisfies(it.iterator().hasNext(), message) }

/**
 * Validates that the iterable contains the specified element.
 *
 * Example:
 * ```kotlin
 * tryValidate { has(listOf("foo", "bar"), "foo") }  // Success
 * tryValidate { has(listOf("bar", "baz"), "foo") }  // Failure
 * ```
 *
 * @param element The element that must be present in the iterable
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <E> Validation.has(
    input: Iterable<E>,
    element: E,
    message: MessageProvider = { "kova.iterable.contains".resource(element) },
) = contains(input, element, message)

/**
 * Validates that the iterable contains the specified element.
 *
 * Example:
 * ```kotlin
 * tryValidate { contains(listOf("foo", "bar"), "foo") }  // Success
 * tryValidate { contains(listOf("bar", "baz"), "foo") }  // Failure
 * ```
 *
 * @param element The element that must be present in the iterable
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <E> Validation.contains(
    input: Iterable<E>,
    element: E,
    message: MessageProvider = { "kova.iterable.contains".resource(element) },
) = input.constrain("kova.iterable.contains") { satisfies(it.contains(element), message) }

/**
 * Validates that the iterable does not contain the specified element.
 *
 * Example:
 * ```kotlin
 * tryValidate { notContains(listOf("bar", "baz"), "foo") }  // Success
 * tryValidate { notContains(listOf("foo", "bar"), "foo") }  // Failure
 * ```
 *
 * @param element The element that must not be present in the iterable
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <E> Validation.notContains(
    input: Iterable<E>,
    element: E,
    message: MessageProvider = { "kova.iterable.notContains".resource(element) },
) = input.constrain("kova.iterable.notContains") { satisfies(!it.contains(element), message) }

/**
 * Validates each element of the iterable using the specified validator.
 *
 * If any element fails validation, the entire iterable validation fails.
 * Error paths include element indices for better error reporting.
 *
 * Example:
 * ```kotlin
 * tryValidate {
 *     onEach(listOf("abc", "def")) { min(it, 2); max(it, 10) }
 * } // Success
 *
 * tryValidate {
 *     onEach(listOf("a", "b")) { min(it, 2); max(it, 10) }
 * } // Failure: elements too short
 * ```
 *
 * @param validate The validator to apply to each element
 */
@IgnorableReturnValue
fun <E> Validation.onEach(
    input: Iterable<E>,
    validate: Validation.(E) -> Unit,
) = input.constrain("kova.iterable.onEach") {
    with(validation) {
        withMessage({ "kova.iterable.onEach".resource(it) }) {
            for ((i, element) in input.withIndex()) {
                accumulating {
                    appendPath("[$i]<iterable element>") {
                        validate(element)
                    }
                }
            }
        }
    }
}
