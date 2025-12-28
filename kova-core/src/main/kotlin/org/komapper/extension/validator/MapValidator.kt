package org.komapper.extension.validator

/**
 * Validates that the map size is at least the specified minimum.
 *
 * Example:
 * ```kotlin
 * tryValidate { min(mapOf("a" to 1, "b" to 2, "c" to 3), 2) } // Success
 * tryValidate { min(mapOf("a" to 1), 2) }                     // Failure
 * ```
 *
 * @param size Minimum map size (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the minimum size constraint
 */
@IgnorableReturnValue
fun Validation.min(
    input: Map<*, *>,
    size: Int,
    message: SizeMessageProvider = { "kova.map.min".resource(it, size) },
) = input.constrain("kova.map.min") { satisfies(it.size >= size) { message(it.size) } }

/**
 * Validates that the map size does not exceed the specified maximum.
 *
 * Example:
 * ```kotlin
 * tryValidate { max(mapOf("a" to 1, "b" to 2), 3) }                   // Success
 * tryValidate { max(mapOf("a" to 1, "b" to 2, "c" to 3, "d" to 4), 3) } // Failure
 * ```
 *
 * @param size Maximum map size (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the maximum size constraint
 */
@IgnorableReturnValue
fun Validation.max(
    input: Map<*, *>,
    size: Int,
    message: SizeMessageProvider = { "kova.map.max".resource(it, size) },
) = input.constrain("kova.map.max") { satisfies(it.size <= size) { message(it.size) } }

/**
 * Validates that the map is not empty.
 *
 * Example:
 * ```kotlin
 * tryValidate { notEmpty(mapOf("a" to 1)) } // Success
 * tryValidate { notEmpty(mapOf()) }         // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the not-empty constraint
 */
@IgnorableReturnValue
fun Validation.notEmpty(
    input: Map<*, *>,
    message: MessageProvider = { "kova.map.notEmpty".resource },
) = input.constrain("kova.map.notEmpty") { satisfies(it.isNotEmpty(), message) }

/**
 * Validates that the map size equals exactly the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { size(mapOf("a" to 1, "b" to 2, "c" to 3), 3) } // Success
 * tryValidate { size(mapOf("a" to 1, "b" to 2), 3) }           // Failure
 * ```
 *
 * @param size Exact map size required
 * @param message Custom error message provider
 * @return A new validator with the exact size constraint
 */
@IgnorableReturnValue
fun Validation.size(
    input: Map<*, *>,
    size: Int,
    message: SizeMessageProvider = { "kova.map.size".resource(it, size) },
) = input.constrain("kova.map.size") { satisfies(it.size == size) { message(it.size) } }

/**
 * Validates that the map contains the specified key.
 *
 * Example:
 * ```kotlin
 * tryValidate { hasKey(mapOf("foo" to 1, "bar" to 2), "foo") }  // Success
 * tryValidate { hasKey(mapOf("bar" to 2, "baz" to 3), "foo") }  // Failure
 * ```
 *
 * @param key The key that must be present in the map
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <K> Validation.hasKey(
    input: Map<K, *>,
    key: K,
    message: MessageProvider = { "kova.map.containsKey".resource(key) },
) = input.constrain("kova.map.containsKey") { satisfies(it.containsKey(key), message) }

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
@IgnorableReturnValue
fun <K> Validation.notContainsKey(
    input: Map<K, *>,
    key: K,
    message: MessageProvider = { "kova.map.notContainsKey".resource(key) },
) = input.constrain("kova.map.notContainsKey") { satisfies(!it.containsKey(key), message) }

/**
 * Validates that the map contains the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { hasValue(mapOf("foo" to 42, "bar" to 2), 42) }  // Success
 * tryValidate { hasValue(mapOf("foo" to 1, "bar" to 2), 42) }   // Failure
 * ```
 *
 * @param value The value that must be present in the map
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <V> Validation.hasValue(
    input: Map<*, V>,
    value: V,
    message: MessageProvider = { "kova.map.containsValue".resource(value) },
) = input.constrain("kova.map.containsValue") { satisfies(it.containsValue(value), message) }

/**
 * Validates that the map does not contain the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { notContainsValue(mapOf("foo" to 1, "bar" to 2), 42) }   // Success
 * tryValidate { notContainsValue(mapOf("foo" to 42, "bar" to 2), 42) }  // Failure
 * ```
 *
 * @param value The value that must not be present in the map
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <V> Validation.notContainsValue(
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
 *     onEach(mapOf("foo" to 42, "bar" to 10)) { entry ->
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
fun <K, V> Validation.onEach(
    input: Map<K, V>,
    validator: Constraint<Map.Entry<K, V>>,
) = input.constrain("kova.map.onEach") {
    appendPath(text = "<map entry>") {
        validateOnEach(
            input,
            "kova.map.onEach",
            validator,
        )
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
 *     onEachKey(mapOf("abc" to 1, "def" to 2)) { min(it, 2); max(it, 10) }
 * } // Success
 *
 * tryValidate {
 *     onEachKey(mapOf("a" to 1, "b" to 2)) { min(it, 2); max(it, 10) }
 * } // Failure: keys too short
 * ```
 *
 * @param validator The validator to apply to each key
 * @return A new validator with per-key validation
 */
@IgnorableReturnValue
fun <K> Validation.onEachKey(
    input: Map<K, *>,
    validator: Constraint<K>,
) = input.constrain("kova.map.onEachKey") {
    validateOnEach(input, "kova.map.onEachKey") { entry ->
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
 *     onEachValue(mapOf("a" to 10, "b" to 20)) { min(it, 0); max(it, 100) }
 * } // Success
 *
 * tryValidate {
 *     onEachValue(mapOf("a" to -1, "b" to 150)) { min(it, 0); max(it, 100) }
 * } // Failure: values out of range
 * ```
 *
 * @param validator The validator to apply to each value
 * @return A new validator with per-value validation
 */
@IgnorableReturnValue
fun <V> Validation.onEachValue(
    input: Map<*, V>,
    validator: Constraint<V>,
) = input.constrain("kova.map.onEachValue") {
    validateOnEach(input, "kova.map.onEachValue") { entry ->
        appendPath(text = "[${entry.key}]<map value>") { validator(entry.value) }
    }
}

private fun <K, V> Validation.validateOnEach(
    input: Map<K, V>,
    constraintId: String,
    validate: Constraint<Map.Entry<K, V>>,
): Unit =
    withMessage({ constraintId.resource(it) }) {
        for (entry in input.entries) accumulating { validate(entry) }
    }
