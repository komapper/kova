package org.komapper.extension.validator

typealias CountMessageProvider = (actualCount: Int) -> Message

/**
 * Validates that the iterable is not ensureEmpty.
 *
 * Example:
 * ```kotlin
 * tryValidate { listOf("a").ensureNotEmpty() } // Success
 * tryValidate { listOf<String>().ensureNotEmpty() }    // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun Iterable<*>.ensureNotEmpty(message: MessageProvider = { "kova.iterable.notEmpty".resource }) =
    apply { constrain("kova.iterable.notEmpty") { satisfies(it.iterator().hasNext(), message) } }

/**
 * Validates that the iterable ensureContains the specified element.
 *
 * Example:
 * ```kotlin
 * tryValidate { listOf("foo", "bar").ensureHas("foo") }  // Success
 * tryValidate { listOf("bar", "baz").ensureHas("foo") }  // Failure
 * ```
 *
 * @param element The element that must be present in the iterable
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun <E> Iterable<E>.ensureHas(
    element: E,
    message: MessageProvider = { "kova.iterable.contains".resource(element) },
) = ensureContains(element, message)

/**
 * Validates that the iterable ensureContains the specified element.
 *
 * Example:
 * ```kotlin
 * tryValidate { listOf("foo", "bar").ensureContains("foo") }  // Success
 * tryValidate { listOf("bar", "baz").ensureContains("foo") }  // Failure
 * ```
 *
 * @param element The element that must be present in the iterable
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun <E> Iterable<E>.ensureContains(
    element: E,
    message: MessageProvider = { "kova.iterable.contains".resource(element) },
) = apply { constrain("kova.iterable.contains") { satisfies(it.contains(element), message) } }

/**
 * Validates that the iterable does not contain the specified element.
 *
 * Example:
 * ```kotlin
 * tryValidate { listOf("bar", "baz").ensureNotContains("foo") }  // Success
 * tryValidate { listOf("foo", "bar").ensureNotContains("foo") }  // Failure
 * ```
 *
 * @param element The element that must not be present in the iterable
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun <E> Iterable<E>.ensureNotContains(
    element: E,
    message: MessageProvider = { "kova.iterable.notContains".resource(element) },
) = apply { constrain("kova.iterable.notContains") { satisfies(!it.contains(element), message) } }

/**
 * Validates each element of the iterable using the specified validator.
 *
 * If any element fails validation, the entire iterable validation fails.
 * Error paths include element indices for better error reporting.
 *
 * Example:
 * ```kotlin
 * tryValidate {
 *     listOf("abc", "def").ensureEach { it.ensureLengthAtLeast(2); it.ensureLengthAtMost(10) }
 * } // Success
 *
 * tryValidate {
 *     listOf("a", "b").ensureEach { it.ensureLengthAtLeast(2); it.ensureLengthAtMost(10) }
 * } // Failure: elements too short
 * ```
 *
 * @param validate The validator to apply to each element
 */
@IgnorableReturnValue
context(_: Validation)
fun <E> Iterable<E>.ensureEach(validate: context(Validation)(E) -> Unit) =
    apply {
        constrain("kova.iterable.each") {
            context(validation) {
                withMessage({ "kova.iterable.each".resource(it) }) {
                    for ((i, element) in this@ensureEach.withIndex()) {
                        accumulating {
                            appendPath("[$i]<iterable element>") {
                                validate(element)
                            }
                        }
                    }
                }
            }
        }
    }
