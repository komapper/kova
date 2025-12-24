package org.komapper.extension.validator

typealias LengthMessageProvider = (actualSize: Int) -> Message

/**
 * Validates that the collection size is at least the specified minimum.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.collection<String>().min(2)
 * validator.validate(listOf("a", "b", "c")) // Success
 * validator.validate(listOf("a"))           // Failure
 * ```
 *
 * @param size Minimum collection size (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the minimum size constraint
 */
context(_: ValidationContext)
fun Collection<*>.min(
    size: Int,
    message: LengthMessageProvider = { "kova.collection.min".resource(it, size) },
) = constrain("kova.collection.min") { satisfies(it.size >= size) { message(it.size) } }

/**
 * Validates that the collection size does not exceed the specified maximum.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.collection<String>().max(3)
 * validator.validate(listOf("a", "b"))       // Success
 * validator.validate(listOf("a", "b", "c", "d")) // Failure
 * ```
 *
 * @param size Maximum collection size (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the maximum size constraint
 */
context(_: ValidationContext)
fun Collection<*>.max(
    size: Int,
    message: LengthMessageProvider = { "kova.collection.max".resource(it, size) },
) = constrain("kova.collection.max") { satisfies(it.size <= size) { message(it.size) } }

/**
 * Validates that the collection is not empty.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.collection<String>().notEmpty()
 * validator.validate(listOf("a")) // Success
 * validator.validate(listOf())    // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the not-empty constraint
 */
context(_: ValidationContext)
fun Collection<*>.notEmpty(message: MessageProvider = { "kova.collection.notEmpty".resource }) =
    constrain("kova.collection.notEmpty") { satisfies(it.isNotEmpty(), message) }

/**
 * Validates that the collection size equals exactly the specified value.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.collection<String>().length(3)
 * validator.validate(listOf("a", "b", "c")) // Success
 * validator.validate(listOf("a", "b"))      // Failure
 * ```
 *
 * @param size Exact collection size required
 * @param message Custom error message provider
 * @return A new validator with the exact size constraint
 */
context(_: ValidationContext)
fun Collection<*>.length(
    size: Int,
    message: LengthMessageProvider = { "kova.collection.length".resource(it, size) },
) = constrain("kova.collection.length") { satisfies(it.size == size) { message(it.size) } }

/**
 * Validates that the collection contains the specified element.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.collection<String>().contains("foo")
 * validator.validate(listOf("foo", "bar"))  // Success
 * validator.validate(listOf("bar", "baz"))  // Failure
 * ```
 *
 * @param element The element that must be present in the collection
 * @param message Custom error message provider
 * @return A new validator with the contains constraint
 */
context(_: ValidationContext)
fun <E> Collection<E>.has(
    element: E,
    message: MessageProvider = { "kova.collection.contains".resource(element) },
) = constrain("kova.collection.contains") { satisfies(it.contains(element), message) }

/**
 * Validates that the collection does not contain the specified element.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.collection<String>().notContains("foo")
 * validator.validate(listOf("bar", "baz"))  // Success
 * validator.validate(listOf("foo", "bar"))  // Failure
 * ```
 *
 * @param element The element that must not be present in the collection
 * @param message Custom error message provider
 * @return A new validator with the notContains constraint
 */
context(_: ValidationContext)
fun <E> Collection<E>.notContains(
    element: E,
    message: MessageProvider = { "kova.collection.notContains".resource(element) },
) = constrain("kova.collection.notContains") { satisfies(!it.contains(element), message) }

/**
 * Validates each element of the collection using the specified validator.
 *
 * If any element fails validation, the entire collection validation fails.
 * Error paths include element indices for better error reporting.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.collection<String>()
 *     .notEmpty()
 *     .onEach(Kova.string().min(2).max(10))
 *
 * validator.validate(listOf("abc", "def"))    // Success
 * validator.validate(listOf("a", "b"))        // Failure: elements too short
 * ```
 *
 * @param validate The validator to apply to each element
 * @return A new validator with per-element validation
 */
context(_: ValidationContext)
fun <E> Collection<E>.onEach(validate: Constraint<E>) =
    constrain("kova.collection.onEach") { input ->
        withMessage({ "kova.collection.onEach".resource(it) }) {
            for ((i, element) in input.withIndex()) {
                appendPath("[$i]<collection element>") {
                    validate(element).accumulateMessages().getOrElse { return@withMessage it }
                }
            }
            Unit.success()
        }
    }
