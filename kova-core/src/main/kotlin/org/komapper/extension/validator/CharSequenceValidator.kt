package org.komapper.extension.validator

/**
 * Represents a type alias for a validator that operates on types extending CharSequence.
 * This alias is used to simplify or clarify the designation of a validator for CharSequence-related types.
 *
 * @param T the type parameter that must extend CharSequence
 */
typealias CharSequenceValidator<T> = IdentityValidator<T>

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
fun <T : CharSequence> CharSequenceValidator<T>.min(
    length: Int,
    message: MessageProvider = Message.resource(),
) = constrain("kova.charSequence.min") {
    satisfies(it.input.length >= length, message(length))
}

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
fun <T : CharSequence> CharSequenceValidator<T>.max(
    length: Int,
    message: MessageProvider = Message.resource(),
) = constrain("kova.charSequence.max") {
    satisfies(it.input.length <= length, message(length))
}

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
fun <T : CharSequence> CharSequenceValidator<T>.notBlank(message: MessageProvider = Message.resource()) =
    constrain("kova.charSequence.notBlank") {
        satisfies(it.input.isNotBlank(), message())
    }

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
fun <T : CharSequence> CharSequenceValidator<T>.blank(message: MessageProvider = Message.resource()) =
    constrain("kova.charSequence.blank") {
        satisfies(it.input.isBlank(), message())
    }

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
fun <T : CharSequence> CharSequenceValidator<T>.notEmpty(message: MessageProvider = Message.resource()) =
    constrain("kova.charSequence.notEmpty") {
        satisfies(it.input.isNotEmpty(), message())
    }

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
fun <T : CharSequence> CharSequenceValidator<T>.empty(message: MessageProvider = Message.resource()) =
    constrain("kova.charSequence.empty") {
        satisfies(it.input.isEmpty(), message())
    }

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
fun <T : CharSequence> CharSequenceValidator<T>.length(
    length: Int,
    message: MessageProvider = Message.resource(),
) = constrain("kova.charSequence.length") {
    satisfies(it.input.length == length, message(length))
}

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
fun <T : CharSequence> CharSequenceValidator<T>.startsWith(
    prefix: CharSequence,
    message: MessageProvider = Message.resource(),
) = constrain("kova.charSequence.startsWith") {
    satisfies(it.input.startsWith(prefix), message(prefix))
}

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
fun <T : CharSequence> CharSequenceValidator<T>.notStartsWith(
    prefix: CharSequence,
    message: MessageProvider = Message.resource(),
) = constrain("kova.charSequence.notStartsWith") {
    satisfies(!it.input.startsWith(prefix), message(prefix))
}

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
fun <T : CharSequence> CharSequenceValidator<T>.endsWith(
    suffix: CharSequence,
    message: MessageProvider = Message.resource(),
) = constrain("kova.charSequence.endsWith") {
    satisfies(it.input.endsWith(suffix), message(suffix))
}

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
fun <T : CharSequence> CharSequenceValidator<T>.notEndsWith(
    suffix: CharSequence,
    message: MessageProvider = Message.resource(),
) = constrain("kova.charSequence.notEndsWith") {
    satisfies(!it.input.endsWith(suffix), message(suffix))
}

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
fun <T : CharSequence> CharSequenceValidator<T>.contains(
    infix: CharSequence,
    message: MessageProvider = Message.resource(),
) = constrain("kova.charSequence.contains") {
    satisfies(it.input.contains(infix), message(infix))
}

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
fun <T : CharSequence> CharSequenceValidator<T>.notContains(
    infix: CharSequence,
    message: MessageProvider = Message.resource(),
) = constrain("kova.charSequence.notContains") {
    satisfies(!it.input.contains(infix), message(infix))
}

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
fun <T : CharSequence> CharSequenceValidator<T>.matches(
    pattern: Regex,
    message: MessageProvider = Message.resource(),
) = constrain("kova.charSequence.matches") {
    satisfies(pattern.matches(it.input), message(pattern))
}

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
fun <T : CharSequence> CharSequenceValidator<T>.notMatches(
    pattern: Regex,
    message: MessageProvider = Message.resource(),
) = constrain("kova.charSequence.notMatches") {
    satisfies(!pattern.matches(it.input), message(pattern))
}

/**
 * Validates that the character sequence is a valid email address.
 *
 * Uses a comprehensive email validation pattern that checks for common email format requirements.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().email()
 * validator.validate("user@example.com") // Success
 * validator.validate("invalid-email")    // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the email constraint
 */
fun <T : CharSequence> CharSequenceValidator<T>.email(message: MessageProvider = Message.resource()) =
    constrain("kova.charSequence.email") {
        val emailPattern =
            Regex(
                "^(?!\\.)(?!.*\\.\\.)([a-z0-9_'+\\-\\.]*)[a-z0-9_+-]@([a-z0-9][a-z0-9\\-]*\\.)+[a-z]{2,}\$",
                RegexOption.IGNORE_CASE,
            )
        satisfies(emailPattern.matches(it.input), message())
    }
