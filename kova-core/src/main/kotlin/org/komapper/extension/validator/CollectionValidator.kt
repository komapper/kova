package org.komapper.extension.validator

typealias SizeMessageProvider = (actualSize: Int) -> Message

/**
 * Validates that the collection ensureSize is at least the specified minimum.
 *
 * Example:
 * ```kotlin
 * tryValidate { listOf("a", "b", "c").ensureMinSize(2) } // Success
 * tryValidate { listOf("a").ensureMinSize(2) }           // Failure
 * ```
 *
 * @param size Minimum collection ensureSize (inclusive)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun Collection<*>.ensureMinSize(
    size: Int,
    message: SizeMessageProvider = { "kova.collection.minSize".resource(it, size) },
) = this.constrain("kova.collection.minSize") { satisfies(it.size >= size) { message(it.size) } }

/**
 * Validates that the collection ensureSize does not exceed the specified maximum.
 *
 * Example:
 * ```kotlin
 * tryValidate { listOf("a", "b").ensureMaxSize(3) }            // Success
 * tryValidate { listOf("a", "b", "c", "d").ensureMaxSize(3) }  // Failure
 * ```
 *
 * @param size Maximum collection ensureSize (inclusive)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun Collection<*>.ensureMaxSize(
    size: Int,
    message: SizeMessageProvider = { "kova.collection.maxSize".resource(it, size) },
) = this.constrain("kova.collection.maxSize") { satisfies(it.size <= size) { message(it.size) } }

/**
 * Validates that the collection ensureSize equals exactly the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { listOf("a", "b", "c").ensureSize(3) } // Success
 * tryValidate { listOf("a", "b").ensureSize(3) }      // Failure
 * ```
 *
 * @param size Exact collection ensureSize required
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun Collection<*>.ensureSize(
    size: Int,
    message: SizeMessageProvider = { "kova.collection.size".resource(it, size) },
) = this.constrain("kova.collection.size") { satisfies(it.size == size) { message(it.size) } }

/**
 * Validates that the collection size is within the specified range.
 *
 * Example:
 * ```kotlin
 * tryValidate { listOf("a", "b", "c").ensureSizeInRange(2..5) }   // Success
 * tryValidate { listOf("a").ensureSizeInRange(2..5) }             // Failure
 * tryValidate { listOf("a", "b", "c").ensureSizeInRange(1..<3) }  // Failure (open-ended range)
 * ```
 *
 * @param range The allowed size range (supports both ClosedRange and OpenEndRange)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun <R> Collection<*>.ensureSizeInRange(
    range: R,
    message: MessageProvider = { "kova.collection.sizeInRange".resource(range) },
) where R : ClosedRange<Int>, R : OpenEndRange<Int> = this.constrain("kova.collection.sizeInRange") { satisfies(it.size in range, message) }
