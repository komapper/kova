package org.komapper.extension.validator

/**
 * Validates that the map ensureSize is at least the specified minimum.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureMinSize(mapOf("a" to 1, "b" to 2, "c" to 3), 2) } // Success
 * tryValidate { ensureMinSize(mapOf("a" to 1), 2) }                     // Failure
 * ```
 *
 * @param size Minimum map ensureSize (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the minimum ensureSize constraint
 */
@IgnorableReturnValue
fun Validation.ensureMinSize(
    input: Map<*, *>,
    size: Int,
    message: SizeMessageProvider = { "kova.map.minSize".resource(it, size) },
) = input.constrain("kova.map.minSize") { satisfies(it.size >= size) { message(it.size) } }

/**
 * Validates that the map ensureSize does not exceed the specified maximum.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureMaxSize(mapOf("a" to 1, "b" to 2), 3) }                   // Success
 * tryValidate { ensureMaxSize(mapOf("a" to 1, "b" to 2, "c" to 3, "d" to 4), 3) } // Failure
 * ```
 *
 * @param size Maximum map ensureSize (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the maximum ensureSize constraint
 */
@IgnorableReturnValue
fun Validation.ensureMaxSize(
    input: Map<*, *>,
    size: Int,
    message: SizeMessageProvider = { "kova.map.maxSize".resource(it, size) },
) = input.constrain("kova.map.maxSize") { satisfies(it.size <= size) { message(it.size) } }

/**
 * Validates that the map is not ensureEmpty.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureNotEmpty(mapOf("a" to 1)) } // Success
 * tryValidate { ensureNotEmpty(mapOf()) }         // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the not-ensureEmpty constraint
 */
@IgnorableReturnValue
fun Validation.ensureNotEmpty(
    input: Map<*, *>,
    message: MessageProvider = { "kova.map.notEmpty".resource },
) = input.constrain("kova.map.notEmpty") { satisfies(it.isNotEmpty(), message) }

/**
 * Validates that the map ensureSize equals exactly the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureSize(mapOf("a" to 1, "b" to 2, "c" to 3), 3) } // Success
 * tryValidate { ensureSize(mapOf("a" to 1, "b" to 2), 3) }           // Failure
 * ```
 *
 * @param ensureSize Exact map ensureSize required
 * @param message Custom error message provider
 * @return A new validator with the exact ensureSize constraint
 */
@IgnorableReturnValue
fun Validation.ensureSize(
    input: Map<*, *>,
    size: Int,
    message: SizeMessageProvider = { "kova.map.size".resource(it, size) },
) = input.constrain("kova.map.size") { satisfies(it.size == size) { message(it.size) } }

/**
 * Validates that the map size is within the specified range.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureSizeInRange(mapOf("a" to 1, "b" to 2, "c" to 3), 2..5) } // Success
 * tryValidate { ensureSizeInRange(mapOf("a" to 1), 2..5) }                     // Failure
 * tryValidate { ensureSizeInRange(mapOf("a" to 1, "b" to 2, "c" to 3), 1..<3) } // Failure (open-ended range)
 * ```
 *
 * @param range The allowed size range (supports both ClosedRange and OpenEndRange)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <R> Validation.ensureSizeInRange(
    input: Map<*, *>,
    range: R,
    message: MessageProvider = { "kova.map.sizeInRange".resource(range) },
) where R : ClosedRange<Int>, R : OpenEndRange<Int> = input.constrain("kova.map.sizeInRange") { satisfies(it.size in range, message) }

/**
 * Validates that the map ensureContains the specified key.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureHasKey(mapOf("foo" to 1, "bar" to 2), "foo") }  // Success
 * tryValidate { ensureHasKey(mapOf("bar" to 2, "baz" to 3), "foo") }  // Failure
 * ```
 *
 * @param key The key that must be present in the map
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <K> Validation.ensureHasKey(
    input: Map<K, *>,
    key: K,
    message: MessageProvider = { "kova.map.containsKey".resource(key) },
) = ensureContainsKey(input, key, message)

/**
 * Validates that the map ensureContains the specified key.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureContainsKey(mapOf("foo" to 1, "bar" to 2), "foo") }  // Success
 * tryValidate { ensureContainsKey(mapOf("bar" to 2, "baz" to 3), "foo") }  // Failure
 * ```
 *
 * @param key The key that must be present in the map
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <K> Validation.ensureContainsKey(
    input: Map<K, *>,
    key: K,
    message: MessageProvider = { "kova.map.containsKey".resource(key) },
) = input.constrain("kova.map.containsKey") { satisfies(it.containsKey(key), message) }

/**
 * Validates that the map does not contain the specified key.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureNotContainsKey(mapOf("bar" to 2, "baz" to 3), "foo") }  // Success
 * tryValidate { ensureNotContainsKey(mapOf("foo" to 1, "bar" to 2), "foo") }  // Failure
 * ```
 *
 * @param key The key that must not be present in the map
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <K> Validation.ensureNotContainsKey(
    input: Map<K, *>,
    key: K,
    message: MessageProvider = { "kova.map.notContainsKey".resource(key) },
) = input.constrain("kova.map.notContainsKey") { satisfies(!it.containsKey(key), message) }

/**
 * Validates that the map does not contain the specified key.
 *
 * Example:
 * ```kotlin
 * tryValidate { notContainsKey(mapOf("bar" to 2, "baz" to 3), "foo") }  // Success
 * tryValidate { notContainsKey(mapOf("foo" to 1, "bar" to 2), "foo") }  // Failure
 * ```
 *
 * @param key The key that must not be present in the map
 * @param message Custom error message provider
 */
@Deprecated(
    "Use ensureNotContainsKey for naming consistency",
    ReplaceWith("ensureNotContainsKey(input, key, message)"),
)
@IgnorableReturnValue
fun <K> Validation.notContainsKey(
    input: Map<K, *>,
    key: K,
    message: MessageProvider = { "kova.map.notContainsKey".resource(key) },
) = ensureNotContainsKey(input, key, message)

/**
 * Validates that the map ensureContains the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureHasValue(mapOf("foo" to 42, "bar" to 2), 42) }  // Success
 * tryValidate { ensureHasValue(mapOf("foo" to 1, "bar" to 2), 42) }   // Failure
 * ```
 *
 * @param value The value that must be present in the map
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <V> Validation.ensureHasValue(
    input: Map<*, V>,
    value: V,
    message: MessageProvider = { "kova.map.containsValue".resource(value) },
) = ensureContainsValue(input, value, message)

/**
 * Validates that the map contains the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureContainsValue(mapOf("foo" to 42, "bar" to 2), 42) }  // Success
 * tryValidate { ensureContainsValue(mapOf("foo" to 1, "bar" to 2), 42) }   // Failure
 * ```
 *
 * @param value The value that must be present in the map
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <V> Validation.ensureContainsValue(
    input: Map<*, V>,
    value: V,
    message: MessageProvider = { "kova.map.containsValue".resource(value) },
) = input.constrain("kova.map.containsValue") { satisfies(it.containsValue(value), message) }

/**
 * Validates that the map ensureContains the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureCcontainsValue(mapOf("foo" to 42, "bar" to 2), 42) }  // Success
 * tryValidate { ensureCcontainsValue(mapOf("foo" to 1, "bar" to 2), 42) }   // Failure
 * ```
 *
 * @param value The value that must be present in the map
 * @param message Custom error message provider
 */
@Deprecated(
    "Use ensureContainsValue (fixed typo)",
    ReplaceWith("ensureContainsValue(input, value, message)"),
)
@IgnorableReturnValue
fun <V> Validation.ensureCcontainsValue(
    input: Map<*, V>,
    value: V,
    message: MessageProvider = { "kova.map.containsValue".resource(value) },
) = ensureContainsValue(input, value, message)

/**
 * Validates that the map does not contain the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureNotContainsValue(mapOf("foo" to 1, "bar" to 2), 42) }   // Success
 * tryValidate { ensureNotContainsValue(mapOf("foo" to 42, "bar" to 2), 42) }  // Failure
 * ```
 *
 * @param value The value that must not be present in the map
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <V> Validation.ensureNotContainsValue(
    input: Map<*, V>,
    value: V,
    message: MessageProvider = { "kova.map.notContainsValue".resource(value) },
) = input.constrain("kova.map.notContainsValue") { satisfies(!it.containsValue(value), message) }

/**
 * Validates each entry (key-value pair) of the map using the specified validator.
 *
 * If any entry fails validation, the entire map validation fails.
 * Error paths include entry information for better error reporting.
 *
 * Example:
 * ```kotlin
 * tryValidate {
 *     ensureEach(mapOf("foo" to 42, "bar" to 10)) { entry ->
 *         if (entry.key.ensureLength >= 2 && entry.value >= 0) {
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
fun <K, V> Validation.ensureEach(
    input: Map<K, V>,
    validator: Validation.(Map.Entry<K, V>) -> Unit,
) = input.constrain("kova.map.each") {
    with(validation) {
        validateOnEach(input, "kova.map.each") { entry ->
            appendPath(text = "<map entry>") { validator(entry) }
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
 *     ensureEachKey(mapOf("abc" to 1, "def" to 2)) { min(it, 2); max(it, 10) }
 * } // Success
 *
 * tryValidate {
 *     ensureEachKey(mapOf("a" to 1, "b" to 2)) { min(it, 2); max(it, 10) }
 * } // Failure: keys too short
 * ```
 *
 * @param validator The validator to apply to each key
 * @return A new validator with per-key validation
 */
@IgnorableReturnValue
fun <K> Validation.ensureEachKey(
    input: Map<K, *>,
    validator: Validation.(K) -> Unit,
) = input.constrain("kova.map.eachKey") {
    with(validation) {
        validateOnEach(input, "kova.map.eachKey") { entry ->
            appendPath(text = "<map key>") { validator(entry.key) }
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
 *     ensureEachValue(mapOf("a" to 10, "b" to 20)) { min(it, 0); max(it, 100) }
 * } // Success
 *
 * tryValidate {
 *     ensureEachValue(mapOf("a" to -1, "b" to 150)) { min(it, 0); max(it, 100) }
 * } // Failure: values out of range
 * ```
 *
 * @param validator The validator to apply to each value
 * @return A new validator with per-value validation
 */
@IgnorableReturnValue
fun <V> Validation.ensureEachValue(
    input: Map<*, V>,
    validator: Validation.(V) -> Unit,
) = input.constrain("kova.map.eachValue") {
    with(validation) {
        validateOnEach(input, "kova.map.eachValue") { entry ->
            appendPath(text = "[${entry.key}]<map value>") { validator(entry.value) }
        }
    }
}

private fun <K, V> Validation.validateOnEach(
    input: Map<K, V>,
    constraintId: String,
    validate: Validation.(Map.Entry<K, V>) -> Unit,
): Unit =
    withMessage({ constraintId.resource(it) }) {
        for (entry in input.entries) accumulating { validate(entry) }
    }
