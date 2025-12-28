package org.komapper.extension.validator

typealias SizeMessageProvider = (actualSize: Int) -> Message

/**
 * Validates that the collection size is at least the specified minimum.
 *
 * Example:
 * ```kotlin
 * tryValidate { min(listOf("a", "b", "c"), 2) } // Success
 * tryValidate { min(listOf("a"), 2) }           // Failure
 * ```
 *
 * @param size Minimum collection size (inclusive)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.min(
    input: Collection<*>,
    size: Int,
    message: SizeMessageProvider = { "kova.collection.min".resource(it, size) },
) = input.constrain("kova.collection.min") { satisfies(it.size >= size) { message(it.size) } }

/**
 * Validates that the collection size does not exceed the specified maximum.
 *
 * Example:
 * ```kotlin
 * tryValidate { max(listOf("a", "b"), 3) }            // Success
 * tryValidate { max(listOf("a", "b", "c", "d"), 3) }  // Failure
 * ```
 *
 * @param size Maximum collection size (inclusive)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.max(
    input: Collection<*>,
    size: Int,
    message: SizeMessageProvider = { "kova.collection.max".resource(it, size) },
) = input.constrain("kova.collection.max") { satisfies(it.size <= size) { message(it.size) } }

/**
 * Validates that the collection is not empty.
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
    input: Collection<*>,
    message: MessageProvider = { "kova.collection.notEmpty".resource },
) = input.constrain("kova.collection.notEmpty") { satisfies(it.isNotEmpty(), message) }

/**
 * Validates that the collection size equals exactly the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { size(listOf("a", "b", "c"), 3) } // Success
 * tryValidate { size(listOf("a", "b"), 3) }      // Failure
 * ```
 *
 * @param size Exact collection size required
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.size(
    input: Collection<*>,
    size: Int,
    message: SizeMessageProvider = { "kova.collection.size".resource(it, size) },
) = input.constrain("kova.collection.size") { satisfies(it.size == size) { message(it.size) } }

/**
 * Validates that the collection contains the specified element.
 *
 * Example:
 * ```kotlin
 * tryValidate { has(listOf("foo", "bar"), "foo") }  // Success
 * tryValidate { has(listOf("bar", "baz"), "foo") }  // Failure
 * ```
 *
 * @param element The element that must be present in the collection
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <E> Validation.has(
    input: Collection<E>,
    element: E,
    message: MessageProvider = { "kova.collection.contains".resource(element) },
) = contains(input, element, message)

/**
 * Validates that the collection contains the specified element.
 *
 * Example:
 * ```kotlin
 * tryValidate { contains(listOf("foo", "bar"), "foo") }  // Success
 * tryValidate { contains(listOf("bar", "baz"), "foo") }  // Failure
 * ```
 *
 * @param element The element that must be present in the collection
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <E> Validation.contains(
    input: Collection<E>,
    element: E,
    message: MessageProvider = { "kova.collection.contains".resource(element) },
) = input.constrain("kova.collection.contains") { satisfies(it.contains(element), message) }

/**
 * Validates that the collection does not contain the specified element.
 *
 * Example:
 * ```kotlin
 * tryValidate { notContains(listOf("bar", "baz"), "foo") }  // Success
 * tryValidate { notContains(listOf("foo", "bar"), "foo") }  // Failure
 * ```
 *
 * @param element The element that must not be present in the collection
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <E> Validation.notContains(
    input: Collection<E>,
    element: E,
    message: MessageProvider = { "kova.collection.notContains".resource(element) },
) = input.constrain("kova.collection.notContains") { satisfies(!it.contains(element), message) }

/**
 * Validates each element of the collection using the specified validator.
 *
 * If any element fails validation, the entire collection validation fails.
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
    input: Collection<E>,
    validate: Validation.(E) -> Unit,
) = input.constrain("kova.collection.onEach") {
    withMessage({ "kova.collection.onEach".resource(it) }) {
        for ((i, element) in input.withIndex()) {
            accumulating {
                appendPath("[$i]<collection element>") {
                    validate(element)
                }
            }
        }
    }
}
