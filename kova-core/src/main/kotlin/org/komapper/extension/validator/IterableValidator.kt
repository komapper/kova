package org.komapper.extension.validator

public typealias CountMessageProvider = (actualCount: Int) -> Message

/**
 * Validates that the iterable is not empty.
 *
 * Example:
 * ```kotlin
 * tryValidate { listOf("a").ensureNotEmpty() } // Success
 * tryValidate { listOf<String>().ensureNotEmpty() }    // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param T The type of iterable being validated
 * @receiver The iterable to validate
 * @param message Custom error message provider
 * @return The validated input value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun <T : Iterable<*>> T.ensureNotEmpty(message: MessageProvider = { "kova.iterable.notEmpty".resource }): T =
    constrain("kova.iterable.notEmpty") { satisfies(it.iterator().hasNext(), message) }

/**
 * Validates that the iterable contains the specified element.
 *
 * Example:
 * ```kotlin
 * tryValidate { listOf("foo", "bar").ensureContains("foo") }  // Success
 * tryValidate { listOf("bar", "baz").ensureContains("foo") }  // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param T The type of iterable being validated
 * @param E The type of elements in the iterable
 * @receiver The iterable to validate
 * @param element The element that must be present in the iterable
 * @param message Custom error message provider
 * @return The validated input value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun <T : Iterable<E>, E> T.ensureContains(
    element: E,
    message: MessageProvider = { "kova.iterable.contains".resource(element) },
): T = constrain("kova.iterable.contains") { satisfies(it.contains(element), message) }

/**
 * Validates that the iterable does not contain the specified element.
 *
 * Example:
 * ```kotlin
 * tryValidate { listOf("bar", "baz").ensureNotContains("foo") }  // Success
 * tryValidate { listOf("foo", "bar").ensureNotContains("foo") }  // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param T The type of iterable being validated
 * @param E The type of elements in the iterable
 * @receiver The iterable to validate
 * @param element The element that must not be present in the iterable
 * @param message Custom error message provider
 * @return The validated input value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun <T : Iterable<E>, E> T.ensureNotContains(
    element: E,
    message: MessageProvider = { "kova.iterable.notContains".resource(element) },
): T = constrain("kova.iterable.notContains") { satisfies(!it.contains(element), message) }

/**
 * Validates each element of the iterable using the specified validator.
 *
 * If any element fails validation, the entire iterable validation fails.
 * Error paths include element indices for better error reporting.
 *
 * Example:
 * ```kotlin
 * tryValidate {
 *     listOf("abc", "def").ensureEach { it.ensureLengthInRange(2..10) }
 * } // Success
 *
 * tryValidate {
 *     listOf("a", "b").ensureEach { it.ensureLengthInRange(2..10) }
 * } // Failure: elements too short
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param T The type of iterable being validated
 * @param E The type of elements in the iterable
 * @receiver The iterable to validate
 * @param validate The validator to apply to each element
 * @return The validated input value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun <T : Iterable<E>, E> T.ensureEach(validate: context(Validation)(E) -> Unit): T =
    constrain("kova.iterable.each") {
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
