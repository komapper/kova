package org.komapper.extension.validator

/**
 * Validates that the map size is at least the specified minimum.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.map<String, Int>().min(2)
 * validator.validate(mapOf("a" to 1, "b" to 2, "c" to 3)) // Success
 * validator.validate(mapOf("a" to 1))                     // Failure
 * ```
 *
 * @param size Minimum map size (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the minimum size constraint
 */
@IgnorableReturnValue
context(_: Validation, _: Accumulate)
fun Map<*, *>.min(
    size: Int,
    message: LengthMessageProvider = { "kova.map.min".resource(it, size) },
) = constrain("kova.map.min") { satisfies(it.size >= size) { message(it.size) } }

/**
 * Validates that the map size does not exceed the specified maximum.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.map<String, Int>().max(3)
 * validator.validate(mapOf("a" to 1, "b" to 2))                // Success
 * validator.validate(mapOf("a" to 1, "b" to 2, "c" to 3, "d" to 4)) // Failure
 * ```
 *
 * @param size Maximum map size (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the maximum size constraint
 */
@IgnorableReturnValue
context(_: Validation, _: Accumulate)
fun Map<*, *>.max(
    size: Int,
    message: LengthMessageProvider = { "kova.map.max".resource(it, size) },
) = constrain("kova.map.max") { satisfies(it.size <= size) { message(it.size) } }

/**
 * Validates that the map is not empty.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.map<String, Int>().notEmpty()
 * validator.validate(mapOf("a" to 1)) // Success
 * validator.validate(mapOf())         // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the not-empty constraint
 */
@IgnorableReturnValue
context(_: Validation, _: Accumulate)
fun Map<*, *>.notEmpty(message: MessageProvider = { "kova.map.notEmpty".resource }) =
    constrain("kova.map.notEmpty") { satisfies(it.isNotEmpty(), message) }

/**
 * Validates that the map size equals exactly the specified value.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.map<String, Int>().length(3)
 * validator.validate(mapOf("a" to 1, "b" to 2, "c" to 3)) // Success
 * validator.validate(mapOf("a" to 1, "b" to 2))           // Failure
 * ```
 *
 * @param size Exact map size required
 * @param message Custom error message provider
 * @return A new validator with the exact size constraint
 */
@IgnorableReturnValue
context(_: Validation, _: Accumulate)
fun Map<*, *>.length(
    size: Int,
    message: LengthMessageProvider = { "kova.map.length".resource(it, size) },
) = constrain("kova.map.length") { satisfies(it.size == size) { message(it.size) } }

/**
 * Validates that the map contains the specified key.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.map<String, Int>().containsKey("foo")
 * validator.validate(mapOf("foo" to 1, "bar" to 2))  // Success
 * validator.validate(mapOf("bar" to 2, "baz" to 3))  // Failure
 * ```
 *
 * @param key The key that must be present in the map
 * @param message Custom error message provider
 * @return A new validator with the containsKey constraint
 */
@IgnorableReturnValue
context(_: Validation, _: Accumulate)
fun <K> Map<K, *>.hasKey(
    key: K,
    message: MessageProvider = { "kova.map.containsKey".resource(key) },
) = constrain("kova.map.containsKey") { satisfies(it.containsKey(key), message) }

/**
 * Validates that the map does not contain the specified key.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.map<String, Int>().notContainsKey("foo")
 * validator.validate(mapOf("bar" to 2, "baz" to 3))  // Success
 * validator.validate(mapOf("foo" to 1, "bar" to 2))  // Failure
 * ```
 *
 * @param key The key that must not be present in the map
 * @param message Custom error message provider
 * @return A new validator with the notContainsKey constraint
 */
@IgnorableReturnValue
context(_: Validation, _: Accumulate)
fun <K> Map<K, *>.notContainsKey(
    key: K,
    message: MessageProvider = { "kova.map.notContainsKey".resource(key) },
) = constrain("kova.map.notContainsKey") { satisfies(!it.containsKey(key), message) }

/**
 * Validates that the map contains the specified value.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.map<String, Int>().containsValue(42)
 * validator.validate(mapOf("foo" to 42, "bar" to 2))  // Success
 * validator.validate(mapOf("foo" to 1, "bar" to 2))   // Failure
 * ```
 *
 * @param value The value that must be present in the map
 * @param message Custom error message provider
 * @return A new validator with the containsValue constraint
 */
@IgnorableReturnValue
context(_: Validation, _: Accumulate)
fun <V> Map<*, V>.hasValue(
    value: V,
    message: MessageProvider = { "kova.map.containsValue".resource(value) },
) = constrain("kova.map.containsValue") { satisfies(it.containsValue(value), message) }

/**
 * Validates that the map does not contain the specified value.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.map<String, Int>().notContainsValue(42)
 * validator.validate(mapOf("foo" to 1, "bar" to 2))   // Success
 * validator.validate(mapOf("foo" to 42, "bar" to 2))  // Failure
 * ```
 *
 * @param value The value that must not be present in the map
 * @param message Custom error message provider
 * @return A new validator with the notContainsValue constraint
 */
@IgnorableReturnValue
context(_: Validation, _: Accumulate)
fun <V> Map<*, V>.notContainsValue(
    value: V,
    message: MessageProvider = { "kova.map.notContainsValue".resource(value) },
) = constrain("kova.map.notContainsValue") { satisfies(!it.containsValue(value), message) }

/**
 * Validates each entry (key-value pair) of the map using the specified validator.
 *
 * If any entry fails validation, the entire map validation fails.
 * Error paths include entry information for better error reporting.
 *
 * Example:
 * ```kotlin
 * val entryValidator = Validator<Map.Entry<String, Int>, Map.Entry<String, Int>> { entry, ctx ->
 *     if (entry.key.length >= 2 && entry.value >= 0) {
 *         ValidationResult.Success(entry, ctx)
 *     } else {
 *         ValidationResult.Failure(/* ... */)
 *     }
 * }
 * val validator = Kova.map<String, Int>().onEach(entryValidator)
 * ```
 *
 * @param validator The validator to apply to each entry
 * @return A new validator with per-entry validation
 */
@IgnorableReturnValue
context(_: Validation, _: Accumulate)
fun <K, V> Map<K, V>.onEach(validator: Constraint<Map.Entry<K, V>>) =
    constrain("kova.map.onEach") { appendPath(text = "<map entry>") { validateOnEach("kova.map.onEach", validator) } }

/**
 * Validates each key of the map using the specified validator.
 *
 * If any key fails validation, the entire map validation fails.
 * Error paths include key information for better error reporting.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.map<String, Int>()
 *     .notEmpty()
 *     .onEachKey(Kova.string().min(2).max(10))
 *
 * validator.validate(mapOf("abc" to 1, "def" to 2)) // Success
 * validator.validate(mapOf("a" to 1, "b" to 2))     // Failure: keys too short
 * ```
 *
 * @param validator The validator to apply to each key
 * @return A new validator with per-key validation
 */
@IgnorableReturnValue
context(_: Validation, _: Accumulate)
fun <K> Map<K, *>.onEachKey(validator: Constraint<K>) =
    constrain("kova.map.onEachKey") {
        validateOnEach("kova.map.onEachKey") { entry ->
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
 * val validator = Kova.map<String, Int>()
 *     .notEmpty()
 *     .onEachValue(Kova.int().min(0).max(100))
 *
 * validator.validate(mapOf("a" to 10, "b" to 20))  // Success
 * validator.validate(mapOf("a" to -1, "b" to 150)) // Failure: values out of range
 * ```
 *
 * @param validator The validator to apply to each value
 * @return A new validator with per-value validation
 */
@IgnorableReturnValue
context(_: Validation, _: Accumulate)
fun <V> Map<*, V>.onEachValue(validator: Constraint<V>) =
    constrain("kova.map.onEachValue") {
        validateOnEach("kova.map.onEachValue") { entry ->
            appendPath(text = "[${entry.key}]<map value>") { validator(entry.value) }
        }
    }

context(_: Validation, _: Accumulate)
private fun <K, V> Map<K, V>.validateOnEach(
    constraintId: String,
    validate: Constraint<Map.Entry<K, V>>,
): Unit =
    withMessage({ constraintId.resource(it) }) {
        for (entry in entries) accumulating { validate(entry) }
    }
