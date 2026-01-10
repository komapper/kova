package org.komapper.extension.validator

typealias CountMessageProvider = (actualCount: Int) -> Message

/**
 * Validates that the iterable is not ensureEmpty.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureNotEmpty(listOf("a")) } // Success
 * tryValidate { ensureNotEmpty(listOf()) }    // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun ensureNotEmpty(
    input: Iterable<*>,
    message: MessageProvider = { "kova.iterable.notEmpty".resource },
) = input.constrain("kova.iterable.notEmpty") { satisfies(it.iterator().hasNext(), message) }

/**
 * Validates that the iterable ensureContains the specified element.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureHas(listOf("foo", "bar"), "foo") }  // Success
 * tryValidate { ensureHas(listOf("bar", "baz"), "foo") }  // Failure
 * ```
 *
 * @param element The element that must be present in the iterable
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun <E> ensureHas(
    input: Iterable<E>,
    element: E,
    message: MessageProvider = { "kova.iterable.contains".resource(element) },
) = ensureContains(input, element, message)

/**
 * Validates that the iterable ensureContains the specified element.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureContains(listOf("foo", "bar"), "foo") }  // Success
 * tryValidate { ensureContains(listOf("bar", "baz"), "foo") }  // Failure
 * ```
 *
 * @param element The element that must be present in the iterable
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun <E> ensureContains(
    input: Iterable<E>,
    element: E,
    message: MessageProvider = { "kova.iterable.contains".resource(element) },
) = input.constrain("kova.iterable.contains") { satisfies(it.contains(element), message) }

/**
 * Validates that the iterable does not contain the specified element.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureNotContains(listOf("bar", "baz"), "foo") }  // Success
 * tryValidate { ensureNotContains(listOf("foo", "bar"), "foo") }  // Failure
 * ```
 *
 * @param element The element that must not be present in the iterable
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun <E> ensureNotContains(
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
 *     ensureEach(listOf("abc", "def")) { min(it, 2); max(it, 10) }
 * } // Success
 *
 * tryValidate {
 *     ensureEach(listOf("a", "b")) { min(it, 2); max(it, 10) }
 * } // Failure: elements too short
 * ```
 *
 * @param validate The validator to apply to each element
 */
@IgnorableReturnValue
context(_: Validation)
fun <E> ensureEach(
    input: Iterable<E>,
    validate: context(Validation)(E) -> Unit,
) = input.constrain("kova.iterable.each") {
    context(validation) {
        withMessage({ "kova.iterable.each".resource(it) }) {
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
