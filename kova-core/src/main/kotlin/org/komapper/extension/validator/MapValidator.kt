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
fun min(
    input: Map<*, *>,
    size: Int,
    message: LengthMessageProvider = { "kova.map.min".resource(it, size) },
) = input.constrain("kova.map.min") { satisfies(it.size >= size) { message(it.size) } }

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
fun max(
    input: Map<*, *>,
    size: Int,
    message: LengthMessageProvider = { "kova.map.max".resource(it, size) },
) = input.constrain("kova.map.max") { satisfies(it.size <= size) { message(it.size) } }

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
fun notEmpty(
    input: Map<*, *>,
    message: MessageProvider = { "kova.map.notEmpty".resource },
) = input.constrain("kova.map.notEmpty") { satisfies(it.isNotEmpty(), message) }

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
fun length(
    input: Map<*, *>,
    size: Int,
    message: LengthMessageProvider = { "kova.map.length".resource(it, size) },
) = input.constrain("kova.map.length") { satisfies(it.size == size) { message(it.size) } }

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
fun <K> hasKey(
    input: Map<K, *>,
    key: K,
    message: MessageProvider = { "kova.map.containsKey".resource(key) },
) = input.constrain("kova.map.containsKey") { satisfies(it.containsKey(key), message) }

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
fun <K> notContainsKey(
    input: Map<K, *>,
    key: K,
    message: MessageProvider = { "kova.map.notContainsKey".resource(key) },
) = input.constrain("kova.map.notContainsKey") { satisfies(!it.containsKey(key), message) }

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
fun <V> hasValue(
    input: Map<*, V>,
    value: V,
    message: MessageProvider = { "kova.map.containsValue".resource(value) },
) = input.constrain("kova.map.containsValue") { satisfies(it.containsValue(value), message) }

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
fun <V> notContainsValue(
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
fun <K, V> onEach(
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
fun <K> onEachKey(
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
fun <V> onEachValue(
    input: Map<*, V>,
    validator: Constraint<V>,
) = input.constrain("kova.map.onEachValue") {
    validateOnEach(input, "kova.map.onEachValue") { entry ->
        appendPath(text = "[${entry.key}]<map value>") { validator(entry.value) }
    }
}

context(_: Validation, _: Accumulate)
private fun <K, V> validateOnEach(
    input: Map<K, V>,
    constraintId: String,
    validate: Constraint<Map.Entry<K, V>>,
): Unit =
    withMessage({ constraintId.resource(it) }) {
        for (entry in input.entries) accumulating { validate(entry) }
    }
