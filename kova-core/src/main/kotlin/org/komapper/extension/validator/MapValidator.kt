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
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param T The map type being validated
 * @receiver The map to validate
 * @param size Exact map size required
 * @param message Custom error message provider
 * @return The validated input value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun <T : Map<*, *>> T.ensureSize(
    size: Int,
    message: SizeMessageProvider = { "kova.map.size".resource(it, size) },
): T = constrain("kova.map.size") { satisfies(it.size == size) { message(it.size) } }

/**
 * Validates that the map size is at least the specified minimum.
 *
 * Example:
 * ```kotlin
 * tryValidate { mapOf("a" to 1, "b" to 2, "c" to 3).ensureSizeAtLeast(2) } // Success
 * tryValidate { mapOf("a" to 1).ensureSizeAtLeast(2) }                     // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param T The map type being validated
 * @receiver The map to validate
 * @param size Minimum map size (inclusive)
 * @param message Custom error message provider
 * @return The validated input value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun <T : Map<*, *>> T.ensureSizeAtLeast(
    size: Int,
    message: SizeMessageProvider = { "kova.map.sizeAtLeast".resource(it, size) },
): T = constrain("kova.map.sizeAtLeast") { satisfies(it.size >= size) { message(it.size) } }

/**
 * Validates that the map size does not exceed the specified maximum.
 *
 * Example:
 * ```kotlin
 * tryValidate { mapOf("a" to 1, "b" to 2).ensureSizeAtMost(3) }                   // Success
 * tryValidate { mapOf("a" to 1, "b" to 2, "c" to 3, "d" to 4).ensureSizeAtMost(3) } // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param T The map type being validated
 * @receiver The map to validate
 * @param size Maximum map size (inclusive)
 * @param message Custom error message provider
 * @return The validated input value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun <T : Map<*, *>> T.ensureSizeAtMost(
    size: Int,
    message: SizeMessageProvider = { "kova.map.sizeAtMost".resource(it, size) },
): T = constrain("kova.map.sizeAtMost") { satisfies(it.size <= size) { message(it.size) } }

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
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param T The map type being validated
 * @param R The range type (must implement both ClosedRange and OpenEndRange)
 * @receiver The map to validate
 * @param range The allowed size range (supports both ClosedRange and OpenEndRange)
 * @param message Custom error message provider
 * @return The validated input value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun <T : Map<*, *>, R> T.ensureSizeInRange(
    range: R,
    message: MessageProvider = { "kova.map.sizeInRange".resource(range) },
): T where R : ClosedRange<Int>, R : OpenEndRange<Int> = constrain("kova.map.sizeInRange") { satisfies(it.size in range, message) }

/**
 * Validates that the map is not empty.
 *
 * Example:
 * ```kotlin
 * tryValidate { mapOf("a" to 1).ensureNotEmpty() }       // Success
 * tryValidate { mapOf<String, Int>().ensureNotEmpty() }  // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param T The map type being validated
 * @receiver The map to validate
 * @param message Custom error message provider
 * @return The validated input value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun <T : Map<*, *>> T.ensureNotEmpty(message: MessageProvider = { "kova.map.notEmpty".resource }): T =
    constrain("kova.map.notEmpty") { satisfies(it.isNotEmpty(), message) }

/**
 * Validates that the map contains the specified key.
 *
 * Example:
 * ```kotlin
 * tryValidate { mapOf("foo" to 1, "bar" to 2).ensureHasKey("foo") }  // Success
 * tryValidate { mapOf("bar" to 2, "baz" to 3).ensureHasKey("foo") }  // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param T The map type being validated
 * @param K The key type of the map
 * @receiver The map to validate
 * @param key The key that must be present in the map
 * @param message Custom error message provider
 * @return The validated input value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun <T : Map<K, *>, K> T.ensureHasKey(
    key: K,
    message: MessageProvider = { "kova.map.containsKey".resource(key) },
): T = ensureContainsKey(key, message)

/**
 * Validates that the map contains the specified key.
 *
 * Example:
 * ```kotlin
 * tryValidate { mapOf("foo" to 1, "bar" to 2).ensureContainsKey("foo") }  // Success
 * tryValidate { mapOf("bar" to 2, "baz" to 3).ensureContainsKey("foo") }  // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param T The map type being validated
 * @param K The key type of the map
 * @receiver The map to validate
 * @param key The key that must be present in the map
 * @param message Custom error message provider
 * @return The validated input value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun <T : Map<K, *>, K> T.ensureContainsKey(
    key: K,
    message: MessageProvider = { "kova.map.containsKey".resource(key) },
): T = constrain("kova.map.containsKey") { satisfies(it.containsKey(key), message) }

/**
 * Validates that the map does not contain the specified key.
 *
 * Example:
 * ```kotlin
 * tryValidate { mapOf("bar" to 2, "baz" to 3).ensureNotContainsKey("foo") }  // Success
 * tryValidate { mapOf("foo" to 1, "bar" to 2).ensureNotContainsKey("foo") }  // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param T The map type being validated
 * @param K The key type of the map
 * @receiver The map to validate
 * @param key The key that must not be present in the map
 * @param message Custom error message provider
 * @return The validated input value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun <T : Map<K, *>, K> T.ensureNotContainsKey(
    key: K,
    message: MessageProvider = { "kova.map.notContainsKey".resource(key) },
): T = constrain("kova.map.notContainsKey") { satisfies(!it.containsKey(key), message) }

/**
 * Validates that the map contains the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { mapOf("foo" to 42, "bar" to 2).ensureHasValue(42) }  // Success
 * tryValidate { mapOf("foo" to 1, "bar" to 2).ensureHasValue(42) }   // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param T The map type being validated
 * @param V The value type of the map
 * @receiver The map to validate
 * @param value The value that must be present in the map
 * @param message Custom error message provider
 * @return The validated input value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun <T : Map<*, V>, V> T.ensureHasValue(
    value: V,
    message: MessageProvider = { "kova.map.containsValue".resource(value) },
): T = ensureContainsValue(value, message)

/**
 * Validates that the map contains the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { mapOf("foo" to 42, "bar" to 2).ensureContainsValue(42) }  // Success
 * tryValidate { mapOf("foo" to 1, "bar" to 2).ensureContainsValue(42) }   // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param T The map type being validated
 * @param V The value type of the map
 * @receiver The map to validate
 * @param value The value that must be present in the map
 * @param message Custom error message provider
 * @return The validated input value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun <T : Map<*, V>, V> T.ensureContainsValue(
    value: V,
    message: MessageProvider = { "kova.map.containsValue".resource(value) },
): T = constrain("kova.map.containsValue") { satisfies(it.containsValue(value), message) }

/**
 * Validates that the map does not contain the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { mapOf("foo" to 1, "bar" to 2).ensureNotContainsValue(42) }   // Success
 * tryValidate { mapOf("foo" to 42, "bar" to 2).ensureNotContainsValue(42) }  // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param T The map type being validated
 * @param V The value type of the map
 * @receiver The map to validate
 * @param value The value that must not be present in the map
 * @param message Custom error message provider
 * @return The validated input value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun <T : Map<*, V>, V> T.ensureNotContainsValue(
    value: V,
    message: MessageProvider = { "kova.map.notContainsValue".resource(value) },
): T = constrain("kova.map.notContainsValue") { satisfies(!it.containsValue(value), message) }

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
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param T The map type being validated
 * @param K The key type of the map
 * @param V The value type of the map
 * @receiver The map to validate
 * @param validator The validator to apply to each entry
 * @return The validated input value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun <T : Map<K, V>, K, V> T.ensureEach(validator: context(Validation)(Map.Entry<K, V>) -> Unit): T =
    constrain("kova.map.each") {
        validateOnEach(this@ensureEach, "kova.map.each") { entry ->
            appendPath(text = "<map entry>") { validator(entry) }
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
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param T The map type being validated
 * @param K The key type of the map
 * @receiver The map to validate
 * @param validator The validator to apply to each key
 * @return The validated input value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun <T : Map<K, *>, K> T.ensureEachKey(validator: context(Validation)(K) -> Unit): T =
    constrain("kova.map.eachKey") {
        validateOnEach(this@ensureEachKey, "kova.map.eachKey") { entry ->
            appendPath(text = "<map key>") { validator(entry.key) }
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
 *     mapOf("a" to 10, "b" to 20).ensureEachValue { it.ensureAtLeast(0).ensureAtMost(100) }
 * } // Success
 *
 * tryValidate {
 *     mapOf("a" to -1, "b" to 150).ensureEachValue { it.ensureAtLeast(0).ensureAtMost(100) }
 * } // Failure: values out of range
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param T The map type being validated
 * @param V The value type of the map
 * @receiver The map to validate
 * @param validator The validator to apply to each value
 * @return The validated input value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun <T : Map<*, V>, V> T.ensureEachValue(validator: context(Validation)(V) -> Unit): T =
    constrain("kova.map.eachValue") {
        validateOnEach(this@ensureEachValue, "kova.map.eachValue") { entry ->
            appendPath(text = "[${entry.key}]<map value>") { validator(entry.value) }
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
