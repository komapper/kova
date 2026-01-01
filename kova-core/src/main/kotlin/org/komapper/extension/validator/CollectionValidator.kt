package org.komapper.extension.validator

typealias SizeMessageProvider = (actualSize: Int) -> Message

/**
 * Validates that the collection size is at least the specified minimum.
 *
 * Example:
 * ```kotlin
 * tryValidate { minSize(listOf("a", "b", "c"), 2) } // Success
 * tryValidate { minSize(listOf("a"), 2) }           // Failure
 * ```
 *
 * @param size Minimum collection size (inclusive)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.minSize(
    input: Collection<*>,
    size: Int,
    message: SizeMessageProvider = { "kova.collection.minSize".resource(it, size) },
) = input.constrain("kova.collection.minSize") { satisfies(it.size >= size) { message(it.size) } }

/**
 * Validates that the collection size does not exceed the specified maximum.
 *
 * Example:
 * ```kotlin
 * tryValidate { maxSize(listOf("a", "b"), 3) }            // Success
 * tryValidate { maxSize(listOf("a", "b", "c", "d"), 3) }  // Failure
 * ```
 *
 * @param size Maximum collection size (inclusive)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.maxSize(
    input: Collection<*>,
    size: Int,
    message: SizeMessageProvider = { "kova.collection.maxSize".resource(it, size) },
) = input.constrain("kova.collection.maxSize") { satisfies(it.size <= size) { message(it.size) } }

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
