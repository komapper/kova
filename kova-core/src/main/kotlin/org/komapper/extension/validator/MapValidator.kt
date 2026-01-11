package org.komapper.extension.validator

/**
 * Validates that the map size equals exactly the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { mapOf("a" to 1, "b" to 2, "c" to 3).ensureSize(3) } // Success
 * tryValidate { mapOf("a" to 1, "b" to 2).ensureSize(3) }           // Failure
 * ```
 *
 * @param size Exact map ensureSize required
 * @param message Custom error message provider
 * @return A new validator with the exact ensureSize constraint
 */
@IgnorableReturnValue
context(_: Validation)
public funMap<*, *>.ensureSize(
    size: Int,
    message: SizeMessageProvider = { "kova.map.size".resource(it, size) },
) = apply { constrain("kova.map.size") { satisfies(it.size == size) { message(it.size) } } }

/**
 * Validates that the map size is at least the specified minimum.
 *
 * Example:
 * ```kotlin
 * tryValidate { mapOf("a" to 1, "b" to 2, "c" to 3).ensureSizeAtLeast(2) } // Success
 * tryValidate { mapOf("a" to 1).ensureSizeAtLeast(2) }                     // Failure
 * ```
 *
 * @param size Minimum map ensureSize (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the minimum ensureSize constraint
 */
@IgnorableReturnValue
context(_: Validation)
public funMap<*, *>.ensureSizeAtLeast(
    size: Int,
    message: SizeMessageProvider = { "kova.map.sizeAtLeast".resource(it, size) },
) = apply { constrain("kova.map.sizeAtLeast") { satisfies(it.size >= size) { message(it.size) } } }

/**
 * Validates that the map size does not exceed the specified maximum.
 *
 * Example:
 * ```kotlin
 * tryValidate { mapOf("a" to 1, "b" to 2).ensureSizeAtMost(3) }                   // Success
 * tryValidate { mapOf("a" to 1, "b" to 2, "c" to 3, "d" to 4).ensureSizeAtMost(3) } // Failure
 * ```
 *
 * @param size Maximum map ensureSize (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the maximum ensureSize constraint
 */
@IgnorableReturnValue
context(_: Validation)
public funMap<*, *>.ensureSizeAtMost(
    size: Int,
    message: SizeMessageProvider = { "kova.map.sizeAtMost".resource(it, size) },
) = apply { constrain("kova.map.sizeAtMost") { satisfies(it.size <= size) { message(it.size) } } }

/**
 * Validates that the map size is within the specified range.
 *
 * Example:
 * ```kotlin
 * tryValidate { mapOf("a" to 1, "b" to 2, "c" to 3).ensureSizeInRange(2..5) } // Success
 * tryValidate { mapOf("a" to 1).ensureSizeInRange(2..5) }                     // Failure
 * tryValidate { mapOf("a" to 1, "b" to 2, "c" to 3).ensureSizeInRange(1..<3) } // Failure (open-ended range)
 * ```
 *
 * @param range The allowed size range (supports both ClosedRange and OpenEndRange)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
public fun<R> Map<*, *>.ensureSizeInRange(
    range: R,
    message: MessageProvider = { "kova.map.sizeInRange".resource(range) },
) where R : ClosedRange<Int>, R : OpenEndRange<Int> = apply { constrain("kova.map.sizeInRange") { satisfies(it.size in range, message) } }

/**
 * Validates that the map is not ensureEmpty.
 *
 * Example:
 * ```kotlin
 * tryValidate { mapOf("a" to 1).ensureNotEmpty() }       // Success
 * tryValidate { mapOf<String, Int>().ensureNotEmpty() }  // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the not-ensureEmpty constraint
 */
@IgnorableReturnValue
context(_: Validation)
public funMap<*, *>.ensureNotEmpty(message: MessageProvider = { "kova.map.notEmpty".resource }) =
    apply { constrain("kova.map.notEmpty") { satisfies(it.isNotEmpty(), message) } }

/**
 * Validates that the map ensureContains the specified key.
 *
 * Example:
 * ```kotlin
 * tryValidate { mapOf("foo" to 1, "bar" to 2).ensureHasKey("foo") }  // Success
 * tryValidate { mapOf("bar" to 2, "baz" to 3).ensureHasKey("foo") }  // Failure
 * ```
 *
 * @param key The key that must be present in the map
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
public fun<K> Map<K, *>.ensureHasKey(
    key: K,
    message: MessageProvider = { "kova.map.containsKey".resource(key) },
) = ensureContainsKey(key, message)

/**
 * Validates that the map ensureContains the specified key.
 *
 * Example:
 * ```kotlin
 * tryValidate { mapOf("foo" to 1, "bar" to 2).ensureContainsKey("foo") }  // Success
 * tryValidate { mapOf("bar" to 2, "baz" to 3).ensureContainsKey("foo") }  // Failure
 * ```
 *
 * @param key The key that must be present in the map
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
public fun<K> Map<K, *>.ensureContainsKey(
    key: K,
    message: MessageProvider = { "kova.map.containsKey".resource(key) },
) = apply { constrain("kova.map.containsKey") { satisfies(it.containsKey(key), message) } }

/**
 * Validates that the map does not contain the specified key.
 *
 * Example:
 * ```kotlin
 * tryValidate { mapOf("bar" to 2, "baz" to 3).ensureNotContainsKey("foo") }  // Success
 * tryValidate { mapOf("foo" to 1, "bar" to 2).ensureNotContainsKey("foo") }  // Failure
 * ```
 *
 * @param key The key that must not be present in the map
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
public fun<K> Map<K, *>.ensureNotContainsKey(
    key: K,
    message: MessageProvider = { "kova.map.notContainsKey".resource(key) },
) = apply { constrain("kova.map.notContainsKey") { satisfies(!it.containsKey(key), message) } }

/**
 * Validates that the map ensureContains the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { mapOf("foo" to 42, "bar" to 2).ensureHasValue(42) }  // Success
 * tryValidate { mapOf("foo" to 1, "bar" to 2).ensureHasValue(42) }   // Failure
 * ```
 *
 * @param value The value that must be present in the map
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
public fun<V> Map<*, V>.ensureHasValue(
    value: V,
    message: MessageProvider = { "kova.map.containsValue".resource(value) },
) = ensureContainsValue(value, message)

/**
 * Validates that the map contains the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { mapOf("foo" to 42, "bar" to 2).ensureContainsValue(42) }  // Success
 * tryValidate { mapOf("foo" to 1, "bar" to 2).ensureContainsValue(42) }   // Failure
 * ```
 *
 * @param value The value that must be present in the map
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
public fun<V> Map<*, V>.ensureContainsValue(
    value: V,
    message: MessageProvider = { "kova.map.containsValue".resource(value) },
) = apply { constrain("kova.map.containsValue") { satisfies(it.containsValue(value), message) } }

/**
 * Validates that the map does not contain the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { mapOf("foo" to 1, "bar" to 2).ensureNotContainsValue(42) }   // Success
 * tryValidate { mapOf("foo" to 42, "bar" to 2).ensureNotContainsValue(42) }  // Failure
 * ```
 *
 * @param value The value that must not be present in the map
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
public fun<V> Map<*, V>.ensureNotContainsValue(
    value: V,
    message: MessageProvider = { "kova.map.notContainsValue".resource(value) },
) = apply { constrain("kova.map.notContainsValue") { satisfies(!it.containsValue(value), message) } }

/**
 * Validates each entry (key-value pair) of the map using the specified validator.
 *
 * If any entry fails validation, the entire map validation fails.
 * Error paths include entry information for better error reporting.
 *
 * Example:
 * ```kotlin
 * tryValidate {
 *     mapOf("foo" to 42, "bar" to 10).ensureEach { entry ->
 *         if (entry.key.length >= 2 && entry.value >= 0) {
 *             // Success
 *         } else {
 *             // Failure
 *         }
 *     }
 * }
 * ```
 *
 * @param validator The validator to apply to each entry
 * @return A new validator with per-entry validation
 */
@IgnorableReturnValue
context(_: Validation)
public fun<K, V> Map<K, V>.ensureEach(validator: context(Validation)(Map.Entry<K, V>) -> Unit) =
    apply {
        constrain("kova.map.each") {
            context(validation) {
                validateOnEach(this@ensureEach, "kova.map.each") { entry ->
                    appendPath(text = "<map entry>") { validator(entry) }
                }
            }
        }
    }

/**
 * Validates each key of the map using the specified validator.
 *
 * If any key fails validation, the entire map validation fails.
 * Error paths include key information for better error reporting.
 *
 * Example:
 * ```kotlin
 * tryValidate {
 *     mapOf("abc" to 1, "def" to 2).ensureEachKey { it.ensureLengthInRange(2..10) }
 * } // Success
 *
 * tryValidate {
 *     mapOf("a" to 1, "b" to 2).ensureEachKey { it.ensureLengthInRange(2..10) }
 * } // Failure: keys too short
 * ```
 *
 * @param validator The validator to apply to each key
 * @return A new validator with per-key validation
 */
@IgnorableReturnValue
context(_: Validation)
public fun<K> Map<K, *>.ensureEachKey(validator: context(Validation)(K) -> Unit) =
    apply {
        constrain("kova.map.eachKey") {
            context(validation) {
                validateOnEach(this@ensureEachKey, "kova.map.eachKey") { entry ->
                    appendPath(text = "<map key>") { validator(entry.key) }
                }
            }
        }
    }

/**
 * Validates each value of the map using the specified validator.
 *
 * If any value fails validation, the entire map validation fails.
 * Error paths include the associated key for better error reporting.
 *
 * Example:
 * ```kotlin
 * tryValidate {
 *     mapOf("a" to 10, "b" to 20).ensureEachValue { it.ensureAtLeast(0); it.ensureAtMost(100) }
 * } // Success
 *
 * tryValidate {
 *     mapOf("a" to -1, "b" to 150).ensureEachValue { it.ensureAtLeast(0); it.ensureAtMost(100) }
 * } // Failure: values out of range
 * ```
 *
 * @param validator The validator to apply to each value
 * @return A new validator with per-value validation
 */
@IgnorableReturnValue
context(_: Validation)
public fun<V> Map<*, V>.ensureEachValue(validator: context(Validation)(V) -> Unit) =
    apply {
        constrain("kova.map.eachValue") {
            context(validation) {
                validateOnEach(this@ensureEachValue, "kova.map.eachValue") { entry ->
                    appendPath(text = "[${entry.key}]<map value>") { validator(entry.value) }
                }
            }
        }
    }

context(_: Validation)
private fun <K, V> validateOnEach(
    input: Map<K, V>,
    constraintId: String,
    validate: context(Validation)(Map.Entry<K, V>) -> Unit,
): Unit =
    withMessage({ constraintId.resource(it) }) {
        for (entry in input.entries) accumulating { validate(entry) }
    }
