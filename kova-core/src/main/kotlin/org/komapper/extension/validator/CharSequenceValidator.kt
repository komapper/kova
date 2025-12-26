package org.komapper.extension.validator

/**
 * Validates that the character sequence length is at least the specified minimum.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().min(3)
 * validator.validate("hello") // Success
 * validator.validate("hi")    // Failure
 * ```
 *
 * @param length Minimum length (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the minimum length constraint
 */
@IgnorableReturnValue
context(_: Validation, _: Accumulate)
fun CharSequence.min(
    length: Int,
    message: MessageProvider = { "kova.charSequence.min".resource(length) },
) = constrain("kova.charSequence.min") { satisfies(it.length >= length, message) }

/**
 * Validates that the character sequence length does not exceed the specified maximum.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().max(10)
 * validator.validate("hello")      // Success
 * validator.validate("very long string") // Failure
 * ```
 *
 * @param length Maximum length (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the maximum length constraint
 */
@IgnorableReturnValue
context(_: Validation, _: Accumulate)
fun CharSequence.max(
    length: Int,
    message: MessageProvider = { "kova.charSequence.max".resource(length) },
) = constrain("kova.charSequence.max") { satisfies(it.length <= length, message) }

/**
 * Validates that the character sequence is not blank (not empty and not only whitespace).
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().notBlank()
 * validator.validate("hello") // Success
 * validator.validate("   ")   // Failure
 * validator.validate("")      // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the not-blank constraint
 */
@IgnorableReturnValue
context(_: Validation, _: Accumulate)
fun CharSequence.notBlank(message: MessageProvider = { "kova.charSequence.notBlank".resource }) =
    constrain("kova.charSequence.notBlank") { satisfies(it.isNotBlank(), message) }

/**
 * Validates that the character sequence is blank (empty or only whitespace).
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().blank()
 * validator.validate("   ")   // Success
 * validator.validate("")      // Success
 * validator.validate("hello") // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the blank constraint
 */
@IgnorableReturnValue
context(_: Validation, _: Accumulate)
fun CharSequence.blank(message: MessageProvider = { "kova.charSequence.blank".resource }) =
    constrain("kova.charSequence.blank") { satisfies(it.isBlank(), message) }

/**
 * Validates that the character sequence is not empty.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().notEmpty()
 * validator.validate("hello") // Success
 * validator.validate("   ")   // Success (contains whitespace)
 * validator.validate("")      // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the not-empty constraint
 */
@IgnorableReturnValue
context(_: Validation, _: Accumulate)
fun CharSequence.notEmpty(message: MessageProvider = { "kova.charSequence.notEmpty".resource }) =
    constrain("kova.charSequence.notEmpty") { satisfies(it.isNotEmpty(), message) }

/**
 * Validates that the character sequence is empty.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().empty()
 * validator.validate("")      // Success
 * validator.validate("   ")   // Failure (contains whitespace)
 * validator.validate("hello") // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the empty constraint
 */
@IgnorableReturnValue
context(_: Validation, _: Accumulate)
fun CharSequence.empty(message: MessageProvider = { "kova.charSequence.empty".resource }) =
    constrain("kova.charSequence.empty") { satisfies(it.isEmpty(), message) }

/**
 * Validates that the character sequence length equals exactly the specified value.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().length(5)
 * validator.validate("hello") // Success
 * validator.validate("hi")    // Failure
 * ```
 *
 * @param length Exact length required
 * @param message Custom error message provider
 * @return A new validator with the exact length constraint
 */
@IgnorableReturnValue
context(_: Validation, _: Accumulate)
fun CharSequence.length(
    length: Int,
    message: MessageProvider = { "kova.charSequence.length".resource(length) },
) = constrain("kova.charSequence.length") { satisfies(it.length == length, message) }

/**
 * Validates that the character sequence starts with the specified prefix.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().startsWith("Hello")
 * validator.validate("Hello World") // Success
 * validator.validate("Goodbye")     // Failure
 * ```
 *
 * @param prefix The required prefix
 * @param message Custom error message provider
 * @return A new validator with the starts-with constraint
 */
@IgnorableReturnValue
context(_: Validation, _: Accumulate)
fun CharSequence.startsWith(
    prefix: CharSequence,
    message: MessageProvider = { "kova.charSequence.startsWith".resource(prefix) },
) = constrain("kova.charSequence.startsWith") { satisfies(it.startsWith(prefix, ignoreCase = false), message) }

/**
 * Validates that the character sequence does not start with the specified prefix.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().notStartsWith("Hello")
 * validator.validate("Goodbye")     // Success
 * validator.validate("Hello World") // Failure
 * ```
 *
 * @param prefix The prefix that must not be present
 * @param message Custom error message provider
 * @return A new validator with the not-starts-with constraint
 */
@IgnorableReturnValue
context(_: Validation, _: Accumulate)
fun CharSequence.notStartsWith(
    prefix: CharSequence,
    message: MessageProvider = { "kova.charSequence.notStartsWith".resource(prefix) },
) = constrain("kova.charSequence.notStartsWith") { satisfies(!it.startsWith(prefix, ignoreCase = false), message) }

/**
 * Validates that the character sequence ends with the specified suffix.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().endsWith(".txt")
 * validator.validate("document.txt") // Success
 * validator.validate("document.pdf") // Failure
 * ```
 *
 * @param suffix The required suffix
 * @param message Custom error message provider
 * @return A new validator with the ends-with constraint
 */
@IgnorableReturnValue
context(_: Validation, _: Accumulate)
fun CharSequence.endsWith(
    suffix: CharSequence,
    message: MessageProvider = { "kova.charSequence.endsWith".resource(suffix) },
) = constrain("kova.charSequence.endsWith") { satisfies(it.endsWith(suffix, ignoreCase = false), message) }

/**
 * Validates that the character sequence does not end with the specified suffix.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().notEndsWith(".txt")
 * validator.validate("document.pdf") // Success
 * validator.validate("document.txt") // Failure
 * ```
 *
 * @param suffix The suffix that must not be present
 * @param message Custom error message provider
 * @return A new validator with the not-ends-with constraint
 */
@IgnorableReturnValue
context(_: Validation, _: Accumulate)
fun CharSequence.notEndsWith(
    suffix: CharSequence,
    message: MessageProvider = { "kova.charSequence.notEndsWith".resource(suffix) },
) = constrain("kova.charSequence.notEndsWith") { satisfies(!it.endsWith(suffix, ignoreCase = false), message) }

/**
 * Validates that the character sequence contains the specified substring.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().contains("world")
 * validator.validate("hello world") // Success
 * validator.validate("hello")       // Failure
 * ```
 *
 * @param infix The required substring
 * @param message Custom error message provider
 * @return A new validator with the contains constraint
 */
@IgnorableReturnValue
context(_: Validation, _: Accumulate)
fun CharSequence.contains(
    infix: CharSequence,
    message: MessageProvider = { "kova.charSequence.contains".resource(infix) },
) = constrain("kova.charSequence.contains") { satisfies(infix in it, message) }

/**
 * Validates that the character sequence does not contain the specified substring.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().notContains("world")
 * validator.validate("hello")       // Success
 * validator.validate("hello world") // Failure
 * ```
 *
 * @param infix The substring that must not be present
 * @param message Custom error message provider
 * @return A new validator with the not-contains constraint
 */
@IgnorableReturnValue
context(_: Validation, _: Accumulate)
fun CharSequence.notContains(
    infix: CharSequence,
    message: MessageProvider = { "kova.charSequence.notContains".resource(infix) },
) = constrain("kova.charSequence.notContains") { satisfies(infix !in it, message) }

/**
 * Validates that the character sequence matches the specified regular expression pattern.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().matches(Regex("\\d{3}-\\d{4}"))
 * validator.validate("123-4567") // Success
 * validator.validate("12-34")    // Failure
 * ```
 *
 * @param pattern The regex pattern to match
 * @param message Custom error message provider
 * @return A new validator with the regex constraint
 */
@IgnorableReturnValue
context(_: Validation, _: Accumulate)
fun CharSequence.matches(
    pattern: Regex,
    message: MessageProvider = { "kova.charSequence.matches".resource(pattern) },
) = constrain("kova.charSequence.matches") { satisfies(pattern.matches(it), message) }

/**
 * Validates that the character sequence does not match the specified regular expression pattern.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().notMatches(Regex("\\d+"))
 * validator.validate("hello") // Success
 * validator.validate("123")   // Failure
 * ```
 *
 * @param pattern The regex pattern that must not match
 * @param message Custom error message provider
 * @return A new validator with the not-matches constraint
 */
@IgnorableReturnValue
context(_: Validation, _: Accumulate)
fun CharSequence.notMatches(
    pattern: Regex,
    message: MessageProvider = { "kova.charSequence.notMatches".resource(pattern) },
) = constrain("kova.charSequence.notMatches") { satisfies(!pattern.matches(it), message) }
