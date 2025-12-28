package org.komapper.extension.validator

/**
 * Validates that the character sequence length is at least the specified minimum.
 *
 * Example:
 * ```kotlin
 * tryValidate { min("hello", 3) } // Success
 * tryValidate { min("hi", 3) }    // Failure
 * ```
 *
 * @param length Minimum length (inclusive)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.min(
    input: CharSequence,
    length: Int,
    message: MessageProvider = { "kova.charSequence.min".resource(length) },
) = input.constrain("kova.charSequence.min") { satisfies(it.length >= length, message) }

/**
 * Validates that the character sequence length does not exceed the specified maximum.
 *
 * Example:
 * ```kotlin
 * tryValidate { max("hello", 10) }           // Success
 * tryValidate { max("very long string", 10) } // Failure
 * ```
 *
 * @param length Maximum length (inclusive)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.max(
    input: CharSequence,
    length: Int,
    message: MessageProvider = { "kova.charSequence.max".resource(length) },
) = input.constrain("kova.charSequence.max") { satisfies(it.length <= length, message) }

/**
 * Validates that the character sequence is not blank (not empty and not only whitespace).
 *
 * Example:
 * ```kotlin
 * tryValidate { notBlank("hello") } // Success
 * tryValidate { notBlank("   ") }   // Failure
 * tryValidate { notBlank("") }      // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.notBlank(
    input: CharSequence,
    message: MessageProvider = { "kova.charSequence.notBlank".resource },
) = input.constrain("kova.charSequence.notBlank") { satisfies(it.isNotBlank(), message) }

/**
 * Validates that the character sequence is blank (empty or only whitespace).
 *
 * Example:
 * ```kotlin
 * tryValidate { blank("   ") }   // Success
 * tryValidate { blank("") }      // Success
 * tryValidate { blank("hello") } // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.blank(
    input: CharSequence,
    message: MessageProvider = { "kova.charSequence.blank".resource },
) = input.constrain("kova.charSequence.blank") { satisfies(it.isBlank(), message) }

/**
 * Validates that the character sequence is not empty.
 *
 * Example:
 * ```kotlin
 * tryValidate { notEmpty("hello") } // Success
 * tryValidate { notEmpty("   ") }   // Success (contains whitespace)
 * tryValidate { notEmpty("") }      // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.notEmpty(
    input: CharSequence,
    message: MessageProvider = { "kova.charSequence.notEmpty".resource },
) = input.constrain("kova.charSequence.notEmpty") { satisfies(it.isNotEmpty(), message) }

/**
 * Validates that the character sequence is empty.
 *
 * Example:
 * ```kotlin
 * tryValidate { empty("") }      // Success
 * tryValidate { empty("   ") }   // Failure (contains whitespace)
 * tryValidate { empty("hello") } // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.empty(
    input: CharSequence,
    message: MessageProvider = { "kova.charSequence.empty".resource },
) = input.constrain("kova.charSequence.empty") { satisfies(it.isEmpty(), message) }

/**
 * Validates that the character sequence length equals exactly the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { length("hello", 5) } // Success
 * tryValidate { length("hi", 5) }    // Failure
 * ```
 *
 * @param length Exact length required
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.length(
    input: CharSequence,
    length: Int,
    message: MessageProvider = { "kova.charSequence.length".resource(length) },
) = input.constrain("kova.charSequence.length") { satisfies(it.length == length, message) }

/**
 * Validates that the character sequence starts with the specified prefix.
 *
 * Example:
 * ```kotlin
 * tryValidate { startsWith("Hello World", "Hello") } // Success
 * tryValidate { startsWith("Goodbye", "Hello") }     // Failure
 * ```
 *
 * @param prefix The required prefix
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.startsWith(
    input: CharSequence,
    prefix: CharSequence,
    message: MessageProvider = { "kova.charSequence.startsWith".resource(prefix) },
) = input.constrain("kova.charSequence.startsWith") { satisfies(it.startsWith(prefix, ignoreCase = false), message) }

/**
 * Validates that the character sequence does not start with the specified prefix.
 *
 * Example:
 * ```kotlin
 * tryValidate { notStartsWith("Goodbye", "Hello") }     // Success
 * tryValidate { notStartsWith("Hello World", "Hello") } // Failure
 * ```
 *
 * @param prefix The prefix that must not be present
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.notStartsWith(
    input: CharSequence,
    prefix: CharSequence,
    message: MessageProvider = { "kova.charSequence.notStartsWith".resource(prefix) },
) = input.constrain("kova.charSequence.notStartsWith") {
    satisfies(!it.startsWith(prefix, ignoreCase = false), message)
}

/**
 * Validates that the character sequence ends with the specified suffix.
 *
 * Example:
 * ```kotlin
 * tryValidate { endsWith("document.txt", ".txt") } // Success
 * tryValidate { endsWith("document.pdf", ".txt") } // Failure
 * ```
 *
 * @param suffix The required suffix
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.endsWith(
    input: CharSequence,
    suffix: CharSequence,
    message: MessageProvider = { "kova.charSequence.endsWith".resource(suffix) },
) = input.constrain("kova.charSequence.endsWith") { satisfies(it.endsWith(suffix, ignoreCase = false), message) }

/**
 * Validates that the character sequence does not end with the specified suffix.
 *
 * Example:
 * ```kotlin
 * tryValidate { notEndsWith("document.pdf", ".txt") } // Success
 * tryValidate { notEndsWith("document.txt", ".txt") } // Failure
 * ```
 *
 * @param suffix The suffix that must not be present
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.notEndsWith(
    input: CharSequence,
    suffix: CharSequence,
    message: MessageProvider = { "kova.charSequence.notEndsWith".resource(suffix) },
) = input.constrain("kova.charSequence.notEndsWith") { satisfies(!it.endsWith(suffix, ignoreCase = false), message) }

/**
 * Validates that the character sequence contains the specified substring.
 *
 * Example:
 * ```kotlin
 * tryValidate { contains("hello world", "world") } // Success
 * tryValidate { contains("hello", "world") }       // Failure
 * ```
 *
 * @param infix The required substring
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.contains(
    input: CharSequence,
    infix: CharSequence,
    message: MessageProvider = { "kova.charSequence.contains".resource(infix) },
) = input.constrain("kova.charSequence.contains") { satisfies(infix in it, message) }

/**
 * Validates that the character sequence does not contain the specified substring.
 *
 * Example:
 * ```kotlin
 * tryValidate { notContains("hello", "world") }       // Success
 * tryValidate { notContains("hello world", "world") } // Failure
 * ```
 *
 * @param infix The substring that must not be present
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.notContains(
    input: CharSequence,
    infix: CharSequence,
    message: MessageProvider = { "kova.charSequence.notContains".resource(infix) },
) = input.constrain("kova.charSequence.notContains") { satisfies(infix !in it, message) }

/**
 * Validates that the character sequence matches the specified regular expression pattern.
 *
 * Example:
 * ```kotlin
 * tryValidate { matches("123-4567", Regex("\\d{3}-\\d{4}")) } // Success
 * tryValidate { matches("12-34", Regex("\\d{3}-\\d{4}")) }    // Failure
 * ```
 *
 * @param pattern The regex pattern to match
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.matches(
    input: CharSequence,
    pattern: Regex,
    message: MessageProvider = { "kova.charSequence.matches".resource(pattern) },
) = input.constrain("kova.charSequence.matches") { satisfies(pattern.matches(it), message) }

/**
 * Validates that the character sequence does not match the specified regular expression pattern.
 *
 * Example:
 * ```kotlin
 * tryValidate { notMatches("hello", Regex("\\d+")) } // Success
 * tryValidate { notMatches("123", Regex("\\d+")) }   // Failure
 * ```
 *
 * @param pattern The regex pattern that must not match
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.notMatches(
    input: CharSequence,
    pattern: Regex,
    message: MessageProvider = { "kova.charSequence.notMatches".resource(pattern) },
) = input.constrain("kova.charSequence.notMatches") { satisfies(!pattern.matches(it), message) }
