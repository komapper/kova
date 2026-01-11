package org.komapper.extension.validator

public typealias SizeMessageProvider = (actualSize: Int) -> Message

/**
 * Validates that the collection size equals exactly the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { listOf("a", "b", "c").ensureSize(3) } // Success
 * tryValidate { listOf("a", "b").ensureSize(3) }      // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param T The collection type being validated
 * @receiver The collection to validate
 * @param size Exact collection size required
 * @param message Custom error message provider
 * @return The validated input value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun <T : Collection<*>> T.ensureSize(
    size: Int,
    message: SizeMessageProvider = { "kova.collection.size".resource(it, size) },
): T = constrain("kova.collection.size") { satisfies(it.size == size) { message(it.size) } }

/**
 * Validates that the collection size is at least the specified minimum.
 *
 * Example:
 * ```kotlin
 * tryValidate { listOf("a", "b", "c").ensureSizeAtLeast(2) } // Success
 * tryValidate { listOf("a").ensureSizeAtLeast(2) }           // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param T The collection type being validated
 * @receiver The collection to validate
 * @param size Minimum collection size (inclusive)
 * @param message Custom error message provider
 * @return The validated input value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun <T : Collection<*>> T.ensureSizeAtLeast(
    size: Int,
    message: SizeMessageProvider = { "kova.collection.sizeAtLeast".resource(it, size) },
): T = constrain("kova.collection.sizeAtLeast") { satisfies(it.size >= size) { message(it.size) } }

/**
 * Validates that the collection size does not exceed the specified maximum.
 *
 * Example:
 * ```kotlin
 * tryValidate { listOf("a", "b").ensureSizeAtMost(3) }            // Success
 * tryValidate { listOf("a", "b", "c", "d").ensureSizeAtMost(3) }  // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param T The collection type being validated
 * @receiver The collection to validate
 * @param size Maximum collection size (inclusive)
 * @param message Custom error message provider
 * @return The validated input value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun <T : Collection<*>> T.ensureSizeAtMost(
    size: Int,
    message: SizeMessageProvider = { "kova.collection.sizeAtMost".resource(it, size) },
): T = constrain("kova.collection.sizeAtMost") { satisfies(it.size <= size) { message(it.size) } }

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
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param T The collection type being validated
 * @param R The range type (must implement both ClosedRange and OpenEndRange)
 * @receiver The collection to validate
 * @param range The allowed size range (supports both ClosedRange and OpenEndRange)
 * @param message Custom error message provider
 * @return The validated input value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun <T : Collection<*>, R> T.ensureSizeInRange(
    range: R,
    message: MessageProvider = { "kova.collection.sizeInRange".resource(range) },
): T where R : ClosedRange<Int>, R : OpenEndRange<Int> = constrain("kova.collection.sizeInRange") { satisfies(it.size in range, message) }
