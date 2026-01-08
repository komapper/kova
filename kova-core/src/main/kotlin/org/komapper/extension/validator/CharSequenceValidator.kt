package org.komapper.extension.validator

/**
 * Validates that the character sequence ensureLength is at least the specified minimum.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureMinLength("hello", 3) } // Success
 * tryValidate { ensureMinLength("hi", 3) }    // Failure
 * ```
 *
 * @param length Minimum ensureLength (inclusive)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.ensureMinLength(
    input: CharSequence,
    length: Int,
    message: MessageProvider = { "kova.charSequence.minLength".resource(length) },
) = input.constrain("kova.charSequence.minLength") { satisfies(it.length >= length, message) }

/**
 * Validates that the character sequence ensureLength does not exceed the specified maximum.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureMaxLength("hello", 10) }           // Success
 * tryValidate { ensureMaxLength("very long string", 10) } // Failure
 * ```
 *
 * @param length Maximum ensureLength (inclusive)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.ensureMaxLength(
    input: CharSequence,
    length: Int,
    message: MessageProvider = { "kova.charSequence.maxLength".resource(length) },
) = input.constrain("kova.charSequence.maxLength") { satisfies(it.length <= length, message) }

/**
 * Validates that the character sequence is not ensureBlank (not ensureEmpty and not only whitespace).
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureNotBlank("hello") } // Success
 * tryValidate { ensureNotBlank("   ") }   // Failure
 * tryValidate { ensureNotBlank("") }      // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.ensureNotBlank(
    input: CharSequence,
    message: MessageProvider = { "kova.charSequence.notBlank".resource },
) = input.constrain("kova.charSequence.notBlank") { satisfies(it.isNotBlank(), message) }

/**
 * Validates that the character sequence is ensureBlank (ensureEmpty or only whitespace).
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureBlank("   ") }   // Success
 * tryValidate { ensureBlank("") }      // Success
 * tryValidate { ensureBlank("hello") } // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.ensureBlank(
    input: CharSequence,
    message: MessageProvider = { "kova.charSequence.blank".resource },
) = input.constrain("kova.charSequence.blank") { satisfies(it.isBlank(), message) }

/**
 * Validates that the character sequence is not ensureEmpty.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureNotEmpty("hello") } // Success
 * tryValidate { ensureNotEmpty("   ") }   // Success (ensureContains whitespace)
 * tryValidate { ensureNotEmpty("") }      // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.ensureNotEmpty(
    input: CharSequence,
    message: MessageProvider = { "kova.charSequence.notEmpty".resource },
) = input.constrain("kova.charSequence.notEmpty") { satisfies(it.isNotEmpty(), message) }

/**
 * Validates that the character sequence is ensureEmpty.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureEmpty("") }      // Success
 * tryValidate { ensureEmpty("   ") }   // Failure (ensureContains whitespace)
 * tryValidate { ensureEmpty("hello") } // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.ensureEmpty(
    input: CharSequence,
    message: MessageProvider = { "kova.charSequence.empty".resource },
) = input.constrain("kova.charSequence.empty") { satisfies(it.isEmpty(), message) }

/**
 * Validates that the character sequence ensureLength equals exactly the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureLength("hello", 5) } // Success
 * tryValidate { ensureLength("hi", 5) }    // Failure
 * ```
 *
 * @param ensureLength Exact ensureLength required
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.ensureLength(
    input: CharSequence,
    length: Int,
    message: MessageProvider = { "kova.charSequence.length".resource(length) },
) = input.constrain("kova.charSequence.length") { satisfies(it.length == length, message) }

/**
 * Validates that the character sequence length is within the specified range.
 *
 * Supports ranges that implement both ClosedRange and OpenEndRange interfaces,
 * such as IntRange, allowing both closed (1..100) and open-ended (1..<100) syntax.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureLengthInRange("hello", 1..10) }      // Success
 * tryValidate { ensureLengthInRange("hi", 1..10) }         // Success
 * tryValidate { ensureLengthInRange("", 1..10) }           // Failure (too short)
 * tryValidate { ensureLengthInRange("very long text", 1..<5) }  // Failure (too long)
 * ```
 *
 * @param range The range for valid lengths (must implement both ClosedRange and OpenEndRange)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <R> Validation.ensureLengthInRange(
    input: CharSequence,
    range: R,
    message: MessageProvider = { "kova.charSequence.lengthInRange".resource(range) },
) where R : ClosedRange<Int>, R : OpenEndRange<Int> =
    input.constrain("kova.charSequence.lengthInRange") { satisfies(it.length in range, message) }

/**
 * Validates that the character sequence starts with the specified prefix.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureStartsWith("Hello World", "Hello") } // Success
 * tryValidate { ensureStartsWith("Goodbye", "Hello") }     // Failure
 * ```
 *
 * @param prefix The required prefix
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.ensureStartsWith(
    input: CharSequence,
    prefix: CharSequence,
    message: MessageProvider = { "kova.charSequence.startsWith".resource(prefix) },
) = input.constrain("kova.charSequence.startsWith") { satisfies(it.startsWith(prefix, ignoreCase = false), message) }

/**
 * Validates that the character sequence does not start with the specified prefix.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureNotStartsWith("Goodbye", "Hello") }     // Success
 * tryValidate { ensureNotStartsWith("Hello World", "Hello") } // Failure
 * ```
 *
 * @param prefix The prefix that must not be present
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.ensureNotStartsWith(
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
 * tryValidate { ensureEndsWith("document.txt", ".txt") } // Success
 * tryValidate { ensureEndsWith("document.pdf", ".txt") } // Failure
 * ```
 *
 * @param suffix The required suffix
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.ensureEndsWith(
    input: CharSequence,
    suffix: CharSequence,
    message: MessageProvider = { "kova.charSequence.endsWith".resource(suffix) },
) = input.constrain("kova.charSequence.endsWith") { satisfies(it.endsWith(suffix, ignoreCase = false), message) }

/**
 * Validates that the character sequence does not end with the specified suffix.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureNotEndsWith("document.pdf", ".txt") } // Success
 * tryValidate { ensureNotEndsWith("document.txt", ".txt") } // Failure
 * ```
 *
 * @param suffix The suffix that must not be present
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.ensureNotEndsWith(
    input: CharSequence,
    suffix: CharSequence,
    message: MessageProvider = { "kova.charSequence.notEndsWith".resource(suffix) },
) = input.constrain("kova.charSequence.notEndsWith") { satisfies(!it.endsWith(suffix, ignoreCase = false), message) }

/**
 * Validates that the character sequence ensureContains the specified substring.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureContains("hello world", "world") } // Success
 * tryValidate { ensureContains("hello", "world") }       // Failure
 * ```
 *
 * @param infix The required substring
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.ensureContains(
    input: CharSequence,
    infix: CharSequence,
    message: MessageProvider = { "kova.charSequence.contains".resource(infix) },
) = input.constrain("kova.charSequence.contains") { satisfies(infix in it, message) }

/**
 * Validates that the character sequence does not contain the specified substring.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureNotContains("hello", "world") }       // Success
 * tryValidate { ensureNotContains("hello world", "world") } // Failure
 * ```
 *
 * @param infix The substring that must not be present
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.ensureNotContains(
    input: CharSequence,
    infix: CharSequence,
    message: MessageProvider = { "kova.charSequence.notContains".resource(infix) },
) = input.constrain("kova.charSequence.notContains") { satisfies(infix !in it, message) }

/**
 * Validates that the character sequence ensureMatches the specified regular expression pattern.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureMatches("123-4567", Regex("\\d{3}-\\d{4}")) } // Success
 * tryValidate { ensureMatches("12-34", Regex("\\d{3}-\\d{4}")) }    // Failure
 * ```
 *
 * @param pattern The regex pattern to match
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.ensureMatches(
    input: CharSequence,
    pattern: Regex,
    message: MessageProvider = { "kova.charSequence.matches".resource(pattern) },
) = input.constrain("kova.charSequence.matches") { satisfies(pattern.matches(it), message) }

/**
 * Validates that the character sequence does not match the specified regular expression pattern.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureNotMatches("hello", Regex("\\d+")) } // Success
 * tryValidate { ensureNotMatches("123", Regex("\\d+")) }   // Failure
 * ```
 *
 * @param pattern The regex pattern that must not match
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.ensureNotMatches(
    input: CharSequence,
    pattern: Regex,
    message: MessageProvider = { "kova.charSequence.notMatches".resource(pattern) },
) = input.constrain("kova.charSequence.notMatches") { satisfies(!pattern.matches(it), message) }
