package org.komapper.extension.validator

typealias SizeMessageProvider = (actualSize: Int) -> Message

/**
 * Validates that the collection ensureSize is at least the specified minimum.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureMinSize(listOf("a", "b", "c"), 2) } // Success
 * tryValidate { ensureMinSize(listOf("a"), 2) }           // Failure
 * ```
 *
 * @param size Minimum collection ensureSize (inclusive)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.ensureMinSize(
    input: Collection<*>,
    size: Int,
    message: SizeMessageProvider = { "kova.collection.minSize".resource(it, size) },
) = input.constrain("kova.collection.minSize") { satisfies(it.size >= size) { message(it.size) } }

/**
 * Validates that the collection ensureSize does not exceed the specified maximum.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureMaxSize(listOf("a", "b"), 3) }            // Success
 * tryValidate { ensureMaxSize(listOf("a", "b", "c", "d"), 3) }  // Failure
 * ```
 *
 * @param size Maximum collection ensureSize (inclusive)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.ensureMaxSize(
    input: Collection<*>,
    size: Int,
    message: SizeMessageProvider = { "kova.collection.maxSize".resource(it, size) },
) = input.constrain("kova.collection.maxSize") { satisfies(it.size <= size) { message(it.size) } }

/**
 * Validates that the collection ensureSize equals exactly the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureSize(listOf("a", "b", "c"), 3) } // Success
 * tryValidate { ensureSize(listOf("a", "b"), 3) }      // Failure
 * ```
 *
 * @param ensureSize Exact collection ensureSize required
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.ensureSize(
    input: Collection<*>,
    size: Int,
    message: SizeMessageProvider = { "kova.collection.size".resource(it, size) },
) = input.constrain("kova.collection.size") { satisfies(it.size == size) { message(it.size) } }
