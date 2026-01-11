package org.komapper.extension.validator

/**
 * Validates that the character sequence length equals exactly the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { "hello".ensureLength(5) } // Success
 * tryValidate { "hi".ensureLength(5) }    // Failure
 * ```
 *
 * @param length Exact length required
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun <T : CharSequence> T.ensureLength(
    length: Int,
    message: MessageProvider = { "kova.charSequence.length".resource(length) },
) = apply { constrain("kova.charSequence.length") { satisfies(it.length == length, message) } }

/**
 * Validates that the character sequence length is at least the specified minimum.
 *
 * Example:
 * ```kotlin
 * tryValidate { "hello".ensureLengthAtLeast(3) } // Success
 * tryValidate { "hi".ensureLengthAtLeast(3) }    // Failure
 * ```
 *
 * @param length Minimum length (inclusive)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun <T : CharSequence> T.ensureLengthAtLeast(
    length: Int,
    message: MessageProvider = { "kova.charSequence.lengthAtLeast".resource(length) },
) = apply { constrain("kova.charSequence.lengthAtLeast") { satisfies(it.length >= length, message) } }

/**
 * Validates that the character sequence length does not exceed the specified maximum.
 *
 * Example:
 * ```kotlin
 * tryValidate { "hello".ensureLengthAtMost(10) }           // Success
 * tryValidate { "very long string".ensureLengthAtMost(10) } // Failure
 * ```
 *
 * @param length Maximum length (inclusive)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun <T : CharSequence> T.ensureLengthAtMost(
    length: Int,
    message: MessageProvider = { "kova.charSequence.lengthAtMost".resource(length) },
) = apply { constrain("kova.charSequence.lengthAtMost") { satisfies(it.length <= length, message) } }

/**
 * Validates that the character sequence length is within the specified range.
 *
 * Supports ranges that implement both ClosedRange and OpenEndRange interfaces,
 * such as IntRange, allowing both closed (1..100) and open-ended (1..<100) syntax.
 *
 * Example:
 * ```kotlin
 * tryValidate { "hello".ensureLengthInRange(1..10) }      // Success
 * tryValidate { "hi".ensureLengthInRange(1..10) }         // Success
 * tryValidate { "".ensureLengthInRange(1..10) }           // Failure (too short)
 * tryValidate { "very long text".ensureLengthInRange(1..<5) }  // Failure (too long)
 * ```
 *
 * @param range The range for valid lengths (must implement both ClosedRange and OpenEndRange)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun <T : CharSequence, R> T.ensureLengthInRange(
    range: R,
    message: MessageProvider = { "kova.charSequence.lengthInRange".resource(range) },
) where R : ClosedRange<Int>, R : OpenEndRange<Int> =
    apply { constrain("kova.charSequence.lengthInRange") { satisfies(it.length in range, message) } }

/**
 * Validates that the character sequence is not blank (not empty and not only whitespace).
 *
 * Example:
 * ```kotlin
 * tryValidate { "hello".ensureNotBlank() } // Success
 * tryValidate { "   ".ensureNotBlank() }   // Failure
 * tryValidate { "".ensureNotBlank() }      // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun <T : CharSequence> T.ensureNotBlank(message: MessageProvider = { "kova.charSequence.notBlank".resource }) =
    apply { constrain("kova.charSequence.notBlank") { satisfies(it.isNotBlank(), message) } }

/**
 * Validates that the character sequence is blank (empty or only whitespace).
 *
 * Example:
 * ```kotlin
 * tryValidate { "   ".ensureBlank() }   // Success
 * tryValidate { "".ensureBlank() }      // Success
 * tryValidate { "hello".ensureBlank() } // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun <T : CharSequence> T.ensureBlank(message: MessageProvider = { "kova.charSequence.blank".resource }) =
    apply { constrain("kova.charSequence.blank") { satisfies(it.isBlank(), message) } }

/**
 * Validates that the character sequence is not empty.
 *
 * Example:
 * ```kotlin
 * tryValidate { "hello".ensureNotEmpty() } // Success
 * tryValidate { "   ".ensureNotEmpty() }   // Success (contains whitespace)
 * tryValidate { "".ensureNotEmpty() }      // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun <T : CharSequence> T.ensureNotEmpty(message: MessageProvider = { "kova.charSequence.notEmpty".resource }) =
    apply { constrain("kova.charSequence.notEmpty") { satisfies(it.isNotEmpty(), message) } }

/**
 * Validates that the character sequence is empty.
 *
 * Example:
 * ```kotlin
 * tryValidate { "".ensureEmpty() }      // Success
 * tryValidate { "   ".ensureEmpty() }   // Failure (contains whitespace)
 * tryValidate { "hello".ensureEmpty() } // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun <T : CharSequence> T.ensureEmpty(message: MessageProvider = { "kova.charSequence.empty".resource }) =
    apply { constrain("kova.charSequence.empty") { satisfies(it.isEmpty(), message) } }

/**
 * Validates that the character sequence starts with the specified prefix.
 *
 * Example:
 * ```kotlin
 * tryValidate { "Hello World".ensureStartsWith("Hello") } // Success
 * tryValidate { "Goodbye".ensureStartsWith("Hello") }     // Failure
 * ```
 *
 * @param prefix The required prefix
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun <T : CharSequence> T.ensureStartsWith(
    prefix: CharSequence,
    message: MessageProvider = { "kova.charSequence.startsWith".resource(prefix) },
) = apply { constrain("kova.charSequence.startsWith") { satisfies(it.startsWith(prefix, ignoreCase = false), message) } }

/**
 * Validates that the character sequence does not start with the specified prefix.
 *
 * Example:
 * ```kotlin
 * tryValidate { "Goodbye".ensureNotStartsWith("Hello") }     // Success
 * tryValidate { "Hello World".ensureNotStartsWith("Hello") } // Failure
 * ```
 *
 * @param prefix The prefix that must not be present
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun <T : CharSequence> T.ensureNotStartsWith(
    prefix: CharSequence,
    message: MessageProvider = { "kova.charSequence.notStartsWith".resource(prefix) },
) = apply {
    constrain("kova.charSequence.notStartsWith") {
        satisfies(!it.startsWith(prefix, ignoreCase = false), message)
    }
}

/**
 * Validates that the character sequence ends with the specified suffix.
 *
 * Example:
 * ```kotlin
 * tryValidate { "document.txt".ensureEndsWith(".txt") } // Success
 * tryValidate { "document.pdf".ensureEndsWith(".txt") } // Failure
 * ```
 *
 * @param suffix The required suffix
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun <T : CharSequence> T.ensureEndsWith(
    suffix: CharSequence,
    message: MessageProvider = { "kova.charSequence.endsWith".resource(suffix) },
) = apply { constrain("kova.charSequence.endsWith") { satisfies(it.endsWith(suffix, ignoreCase = false), message) } }

/**
 * Validates that the character sequence does not end with the specified suffix.
 *
 * Example:
 * ```kotlin
 * tryValidate { "document.pdf".ensureNotEndsWith(".txt") } // Success
 * tryValidate { "document.txt".ensureNotEndsWith(".txt") } // Failure
 * ```
 *
 * @param suffix The suffix that must not be present
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun <T : CharSequence> T.ensureNotEndsWith(
    suffix: CharSequence,
    message: MessageProvider = { "kova.charSequence.notEndsWith".resource(suffix) },
) = apply { constrain("kova.charSequence.notEndsWith") { satisfies(!it.endsWith(suffix, ignoreCase = false), message) } }

/**
 * Validates that the character sequence ensureContains the specified substring.
 *
 * Example:
 * ```kotlin
 * tryValidate { "hello world".ensureContains("world") } // Success
 * tryValidate { "hello".ensureContains("world") }       // Failure
 * ```
 *
 * @param infix The required substring
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun <T : CharSequence> T.ensureContains(
    infix: CharSequence,
    message: MessageProvider = { "kova.charSequence.contains".resource(infix) },
) = apply { constrain("kova.charSequence.contains") { satisfies(infix in it, message) } }

/**
 * Validates that the character sequence does not contain the specified substring.
 *
 * Example:
 * ```kotlin
 * tryValidate { "hello".ensureNotContains("world") }       // Success
 * tryValidate { "hello world".ensureNotContains("world") } // Failure
 * ```
 *
 * @param infix The substring that must not be present
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun <T : CharSequence> T.ensureNotContains(
    infix: CharSequence,
    message: MessageProvider = { "kova.charSequence.notContains".resource(infix) },
) = apply { constrain("kova.charSequence.notContains") { satisfies(infix !in it, message) } }

/**
 * Validates that the character sequence ensureMatches the specified regular expression pattern.
 *
 * Example:
 * ```kotlin
 * tryValidate { "123-4567".ensureMatches(Regex("\\d{3}-\\d{4}")) } // Success
 * tryValidate { "12-34".ensureMatches(Regex("\\d{3}-\\d{4}")) }    // Failure
 * ```
 *
 * @param pattern The regex pattern to match
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun <T : CharSequence> T.ensureMatches(
    pattern: Regex,
    message: MessageProvider = { "kova.charSequence.matches".resource(pattern) },
) = apply { constrain("kova.charSequence.matches") { satisfies(pattern.matches(it), message) } }

/**
 * Validates that the character sequence does not match the specified regular expression pattern.
 *
 * Example:
 * ```kotlin
 * tryValidate { "hello".ensureNotMatches(Regex("\\d+")) } // Success
 * tryValidate { "123".ensureNotMatches(Regex("\\d+")) }   // Failure
 * ```
 *
 * @param pattern The regex pattern that must not match
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun <T : CharSequence> T.ensureNotMatches(
    pattern: Regex,
    message: MessageProvider = { "kova.charSequence.notMatches".resource(pattern) },
) = apply { constrain("kova.charSequence.notMatches") { satisfies(!pattern.matches(it), message) } }
