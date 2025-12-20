package org.komapper.extension.validator

/**
 * Type alias for map validators.
 *
 * Provides a convenient type for validators that work with Map types.
 *
 * @param K The key type of the map
 * @param V The value type of the map
 */
typealias MapValidator<K, V> = IdentityValidator<Map<K, V>>

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
fun <K, V> MapValidator<K, V>.min(
    size: Int,
    message: LengthMessageProvider<Map<K, V>> = { resource(it, size) },
) = constrain("kova.map.min") {
    satisfies(input.size >= size) { message(input.size) }
}

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
fun <K, V> MapValidator<K, V>.max(
    size: Int,
    message: LengthMessageProvider<Map<K, V>> = { resource(it, size) },
) = constrain("kova.map.max") {
    satisfies(input.size <= size) { message(input.size) }
}

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
fun <K, V> MapValidator<K, V>.notEmpty(message: MessageProvider<Map<K, V>> = Message::Resource) =
    constrain("kova.map.notEmpty") {
        satisfies(input.isNotEmpty(), message)
    }

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
fun <K, V> MapValidator<K, V>.length(
    size: Int,
    message: LengthMessageProvider<Map<K, V>> = { resource(it, size) },
) = constrain("kova.map.length") {
    satisfies(input.size == size) { message(input.size) }
}

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
fun <K, V> MapValidator<K, V>.containsKey(
    key: K,
    message: MessageProvider<Map<K, V>> = { resource(key) },
) = constrain("kova.map.containsKey") {
    satisfies(input.containsKey(key), message)
}

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
fun <K, V> MapValidator<K, V>.notContainsKey(
    key: K,
    message: MessageProvider<Map<K, V>> = { resource(key) },
) = constrain("kova.map.notContainsKey") {
    satisfies(!input.containsKey(key), message)
}

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
fun <K, V> MapValidator<K, V>.containsValue(
    value: V,
    message: MessageProvider<Map<K, V>> = { resource(value) },
) = constrain("kova.map.containsValue") {
    satisfies(input.containsValue(value), message)
}

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
fun <K, V> MapValidator<K, V>.notContainsValue(
    value: V,
    message: MessageProvider<Map<K, V>> = { resource(value) },
) = constrain("kova.map.notContainsValue") {
    satisfies(!input.containsValue(value), message)
}

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
fun <K, V> MapValidator<K, V>.onEach(validator: Validator<Map.Entry<K, V>, *>) =
    constrain("kova.map.onEach") {
        appendPath(text = "<map entry>") { validateOnEach(validator) }
    }

/**
 * Lambda-based overload of [onEach] for more fluent validation composition.
 *
 * This allows building an entry validator using a lambda function instead of providing
 * a pre-built validator instance.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.map<String, Int>()
 *     .onEach { entry ->
 *         entry.constrain("validEntry") { ctx ->
 *             satisfies(ctx.input.key.length >= 2 && ctx.input.value >= 0,
 *                 "Key must be at least 2 chars and value non-negative")
 *         }
 *     }
 * ```
 *
 * @param block A function that builds an entry validator from a success validator
 * @return A new validator with per-entry validation
 */
fun <K, V> MapValidator<K, V>.onEach(block: (IdentityValidator<Map.Entry<K, V>>) -> Validator<Map.Entry<K, V>, *>) =
    onEach(block(Validator.success()))

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
fun <K, V> MapValidator<K, V>.onEachKey(validator: Validator<K, *>) =
    constrain("kova.map.onEachKey") {
        validateOnEach { entry ->
            appendPath(text = "<map key>") { validator.execute(entry.key) }
        }
    }

/**
 * Lambda-based overload of [onEachKey] for more fluent validation composition.
 *
 * This allows building a key validator using a lambda function instead of providing
 * a pre-built validator instance.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.map<String, Int>()
 *     .notEmpty()
 *     .onEachKey { it.min(2).max(10) }
 *
 * validator.validate(mapOf("abc" to 1, "def" to 2)) // Success
 * validator.validate(mapOf("a" to 1, "b" to 2))     // Failure: keys too short
 * ```
 *
 * @param block A function that builds a key validator from a success validator
 * @return A new validator with per-key validation
 */
fun <K, V> MapValidator<K, V>.onEachKey(block: (IdentityValidator<K>) -> Validator<K, *>) = onEachKey(block(Validator.success()))

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
fun <K, V> MapValidator<K, V>.onEachValue(validator: Validator<V, *>) =
    constrain("kova.map.onEachValue") {
        validateOnEach { entry ->
            appendPath(text = "[${entry.key}]<map value>") { validator.execute(entry.value) }
        }
    }

/**
 * Lambda-based overload of [onEachValue] for more fluent validation composition.
 *
 * This allows building a value validator using a lambda function instead of providing
 * a pre-built validator instance.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.map<String, Int>()
 *     .notEmpty()
 *     .onEachValue { it.min(0).max(100) }
 *
 * validator.validate(mapOf("a" to 10, "b" to 20))  // Success
 * validator.validate(mapOf("a" to -1, "b" to 150)) // Failure: values out of range
 * ```
 *
 * @param block A function that builds a value validator from a success validator
 * @return A new validator with per-value validation
 */
fun <K, V> MapValidator<K, V>.onEachValue(block: (IdentityValidator<V>) -> Validator<V, *>) = onEachValue(block(Validator.success()))

private fun <K, V> ConstraintContext<Map<K, V>>.validateOnEach(validate: Validator<Map.Entry<K, V>, *>): ConstraintResult {
    val failures = mutableListOf<ValidationResult.Failure<*>>()
    for (entry in input.entries) {
        val result = validate.execute(entry)
        if (result.isFailure()) {
            failures.add(result)
            if (failFast) break
        }
    }
    val messages = failures.flatMap { it.messages }
    return satisfies(messages.isEmpty()) {
        Message.Collection(Message.Resource(this, messages), failures)
    }
}
