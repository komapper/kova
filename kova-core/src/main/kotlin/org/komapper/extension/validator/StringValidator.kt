package org.komapper.extension.validator

import kotlin.reflect.KClass

/**
 * Type alias for string validators.
 *
 * Provides a convenient type for validators that work with String inputs and outputs.
 */
typealias StringValidator = IdentityValidator<String>

/**
 * Validates that the string length is at least the specified minimum.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().min(3)
 * validator.validate("hello") // Success
 * validator.validate("hi")    // Failure
 * ```
 *
 * @param length Minimum string length (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the minimum length constraint
 */
fun StringValidator.min(
    length: Int,
    message: MessageProvider<String> = Message.resource(),
) = constrain("kova.string.min") {
    satisfies(it.input.length >= length, message(it, it.input, length))
}

/**
 * Validates that the string length does not exceed the specified maximum.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().max(10)
 * validator.validate("hello")      // Success
 * validator.validate("very long string") // Failure
 * ```
 *
 * @param length Maximum string length (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the maximum length constraint
 */
fun StringValidator.max(
    length: Int,
    message: MessageProvider<String> = Message.resource(),
) = constrain("kova.string.max") {
    satisfies(it.input.length <= length, message(it, it.input, length))
}

/**
 * Validates that the string is not blank (not empty and not only whitespace).
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
fun StringValidator.notBlank(message: MessageProvider<String> = Message.resource()) =
    constrain("kova.string.notBlank") {
        satisfies(it.input.isNotBlank(), message(it, it.input))
    }

/**
 * Validates that the string is not empty.
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
fun StringValidator.notEmpty(message: MessageProvider<String> = Message.resource()) =
    constrain("kova.string.notEmpty") {
        satisfies(it.input.isNotEmpty(), message(it, it.input))
    }

/**
 * Validates that the string length equals exactly the specified value.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().length(5)
 * validator.validate("hello") // Success
 * validator.validate("hi")    // Failure
 * ```
 *
 * @param length Exact string length required
 * @param message Custom error message provider
 * @return A new validator with the exact length constraint
 */
fun StringValidator.length(
    length: Int,
    message: MessageProvider<String> = Message.resource(),
) = constrain("kova.string.length") {
    satisfies(it.input.length == length, message(it, it.input, length))
}

/**
 * Validates that the string starts with the specified prefix.
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
fun StringValidator.startsWith(
    prefix: CharSequence,
    message: MessageProvider<String> = Message.resource(),
) = constrain("kova.string.startsWith") {
    satisfies(it.input.startsWith(prefix), message(it, it.input, prefix))
}

/**
 * Validates that the string ends with the specified suffix.
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
fun StringValidator.endsWith(
    suffix: CharSequence,
    message: MessageProvider<String> = Message.resource(),
) = constrain("kova.string.endsWith") {
    satisfies(it.input.endsWith(suffix), message(it, it.input, suffix))
}

/**
 * Validates that the string contains the specified substring.
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
fun StringValidator.contains(
    infix: CharSequence,
    message: MessageProvider<String> = Message.resource(),
) = constrain("kova.string.contains") {
    satisfies(it.input.contains(infix), message(it, it.input, infix))
}

/**
 * Validates that the string matches the specified regular expression pattern.
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
fun StringValidator.matches(
    pattern: Regex,
    message: MessageProvider<String> = Message.resource(),
) = constrain("kova.string.matches") {
    satisfies(pattern.matches(it.input), message(it, it.input, pattern))
}

/**
 * Validates that the string is a valid email address.
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
fun StringValidator.email(message: MessageProvider<String> = Message.resource()) =
    constrain("kova.string.email") {
        val emailPattern =
            Regex(
                "^(?!\\.)(?!.*\\.\\.)([a-z0-9_'+\\-\\.]*)[a-z0-9_+-]@([a-z0-9][a-z0-9\\-]*\\.)+[a-z]{2,}\$",
                RegexOption.IGNORE_CASE,
            )
        satisfies(emailPattern.matches(it.input), message(it, it.input))
    }

/**
 * Validates that the string can be parsed as an Int.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().isInt()
 * validator.validate("123")  // Success
 * validator.validate("12.5") // Failure
 * validator.validate("abc")  // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the is-int constraint
 */
fun StringValidator.isInt(message: MessageProvider<String> = Message.resource()) =
    constrain("kova.string.isInt") {
        satisfies(it.input.toIntOrNull() != null, message(it, it.input))
    }

/**
 * Validates that the string can be parsed as a Long.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().isLong()
 * validator.validate("123456789") // Success
 * validator.validate("abc")       // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the is-long constraint
 */
fun StringValidator.isLong(message: MessageProvider<String> = Message.resource()) =
    constrain("kova.string.isLong") {
        satisfies(it.input.toLongOrNull() != null, message(it, it.input))
    }

/**
 * Validates that the string can be parsed as a Short.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().isShort()
 * validator.validate("123") // Success
 * validator.validate("abc") // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the is-short constraint
 */
fun StringValidator.isShort(message: MessageProvider<String> = Message.resource()) =
    constrain("kova.string.isShort") {
        satisfies(it.input.toShortOrNull() != null, message(it, it.input))
    }

/**
 * Validates that the string can be parsed as a Byte.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().isByte()
 * validator.validate("12")  // Success
 * validator.validate("abc") // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the is-byte constraint
 */
fun StringValidator.isByte(message: MessageProvider<String> = Message.resource()) =
    constrain("kova.string.isByte") {
        satisfies(it.input.toByteOrNull() != null, message(it, it.input))
    }

/**
 * Validates that the string can be parsed as a Double.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().isDouble()
 * validator.validate("12.5") // Success
 * validator.validate("abc")  // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the is-double constraint
 */
fun StringValidator.isDouble(message: MessageProvider<String> = Message.resource()) =
    constrain("kova.string.isDouble") {
        satisfies(it.input.toDoubleOrNull() != null, message(it, it.input))
    }

/**
 * Validates that the string can be parsed as a Float.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().isFloat()
 * validator.validate("12.5") // Success
 * validator.validate("abc")  // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the is-float constraint
 */
fun StringValidator.isFloat(message: MessageProvider<String> = Message.resource()) =
    constrain("kova.string.isFloat") {
        satisfies(it.input.toFloatOrNull() != null, message(it, it.input))
    }

/**
 * Validates that the string can be parsed as a BigDecimal.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().isBigDecimal()
 * validator.validate("123.456789") // Success
 * validator.validate("abc")        // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the is-big-decimal constraint
 */
fun StringValidator.isBigDecimal(message: MessageProvider<String> = Message.resource()) =
    constrain("kova.string.isBigDecimal") {
        satisfies(it.input.toBigDecimalOrNull() != null, message(it, it.input))
    }

/**
 * Validates that the string can be parsed as a BigInteger.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().isBigInteger()
 * validator.validate("123456789012345") // Success
 * validator.validate("abc")             // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the is-big-integer constraint
 */
fun StringValidator.isBigInteger(message: MessageProvider<String> = Message.resource()) =
    constrain("kova.string.isBigInteger") {
        satisfies(it.input.toBigIntegerOrNull() != null, message(it, it.input))
    }

/**
 * Validates that the string can be parsed as a Boolean.
 *
 * Accepts "true" or "false" (case-insensitive).
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().isBoolean()
 * validator.validate("true")  // Success
 * validator.validate("false") // Success
 * validator.validate("yes")   // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the is-boolean constraint
 */
fun StringValidator.isBoolean(message: MessageProvider<String> = Message.resource()) =
    constrain("kova.string.isBoolean") {
        satisfies(it.input.toBooleanStrictOrNull() != null, message(it, it.input))
    }

/**
 * Validates that the string is a valid name for the specified enum type.
 *
 * Example:
 * ```kotlin
 * enum class Role { ADMIN, USER, GUEST }
 * val validator = Kova.string().isEnum(Role::class)
 * validator.validate("ADMIN") // Success
 * validator.validate("OTHER") // Failure
 * ```
 *
 * @param klass The enum class to validate against
 * @param message Custom error message provider
 * @return A new validator with the is-enum constraint
 */
fun <E : Enum<E>> StringValidator.isEnum(
    klass: KClass<E>,
    message: MessageProvider<String> = Message.resource(),
): StringValidator {
    val enumValues = klass.java.enumConstants
    val validNames = enumValues.map { it.name }
    return this.constrain("kova.string.isEnum") { ctx ->
        satisfies(validNames.contains(ctx.input), message(ctx, ctx.input, validNames))
    }
}

/**
 * Validates that the string is a valid name for the specified enum type (reified version).
 *
 * Example:
 * ```kotlin
 * enum class Role { ADMIN, USER, GUEST }
 * val validator = Kova.string().isEnum<Role>()
 * validator.validate("ADMIN") // Success
 * validator.validate("OTHER") // Failure
 * ```
 *
 * @return A new validator with the is-enum constraint
 */
inline fun <reified E : Enum<E>> StringValidator.isEnum(): StringValidator {
    val enumValues = enumValues<E>()
    val validNames = enumValues.map { it.name }
    return this.constrain("kova.string.isEnum") {
        val messageContext = MessageContext(it, listOf(validNames))
        satisfies(validNames.contains(it.input), Message.Resource(messageContext))
    }
}

/**
 * Validates that the string is a valid enum name and converts it to the enum value.
 *
 * This is a type-transforming validator that outputs the enum type.
 *
 * Example:
 * ```kotlin
 * enum class Role { ADMIN, USER, GUEST }
 * val validator = Kova.string().toEnum<Role>()
 * validator.validate("ADMIN") // Success: Role.ADMIN
 * validator.validate("OTHER") // Failure
 * ```
 *
 * @return A new validator that transforms string to enum type
 */
inline fun <reified E : Enum<E>> StringValidator.toEnum(): Validator<String, E> = isEnum<E>().map { enumValueOf<E>(it) }

/**
 * Validates that the string is in uppercase.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().uppercase()
 * validator.validate("HELLO") // Success
 * validator.validate("hello") // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the uppercase constraint
 */
fun StringValidator.uppercase(message: MessageProvider<String> = Message.resource()) =
    constrain("kova.string.uppercase") {
        satisfies(it.input == it.input.uppercase(), message(it, it.input))
    }

/**
 * Validates that the string is in lowercase.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().lowercase()
 * validator.validate("hello") // Success
 * validator.validate("HELLO") // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the lowercase constraint
 */
fun StringValidator.lowercase(message: MessageProvider<String> = Message.resource()) =
    constrain("kova.string.lowercase") {
        satisfies(it.input == it.input.lowercase(), message(it, it.input))
    }

/**
 * Transforms the string by trimming leading and trailing whitespace.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().trim().min(1)
 * validator.validate("  hello  ") // Success: "hello"
 * ```
 *
 * @return A new validator that trims the string
 */
fun StringValidator.trim() = map { it.trim() }

/**
 * Transforms the string to uppercase.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().toUpperCase()
 * validator.validate("hello") // Success: "HELLO"
 * ```
 *
 * @return A new validator that transforms to uppercase
 */
fun StringValidator.toUpperCase() = map { it.uppercase() }

/**
 * Transforms the string to lowercase.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().toLowerCase()
 * validator.validate("HELLO") // Success: "hello"
 * ```
 *
 * @return A new validator that transforms to lowercase
 */
fun StringValidator.toLowerCase() = map { it.lowercase() }

/**
 * Validates that the string can be parsed as an Int and converts it.
 *
 * This is a type-transforming validator that outputs Int.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().toInt()
 * validator.validate("123") // Success: 123
 * validator.validate("abc") // Failure
 * ```
 *
 * @return A new validator that transforms string to Int
 */
fun StringValidator.toInt(): Validator<String, Int> = isInt().map { it.toInt() }

/**
 * Validates that the string can be parsed as a Long and converts it.
 *
 * This is a type-transforming validator that outputs Long.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().toLong()
 * validator.validate("123456789") // Success: 123456789L
 * validator.validate("abc")       // Failure
 * ```
 *
 * @return A new validator that transforms string to Long
 */
fun StringValidator.toLong(): Validator<String, Long> = isLong().map { it.toLong() }

/**
 * Validates that the string can be parsed as a Short and converts it.
 *
 * This is a type-transforming validator that outputs Short.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().toShort()
 * validator.validate("123") // Success: 123.toShort()
 * validator.validate("abc") // Failure
 * ```
 *
 * @return A new validator that transforms string to Short
 */
fun StringValidator.toShort(): Validator<String, Short> = isShort().map { it.toShort() }

/**
 * Validates that the string can be parsed as a Byte and converts it.
 *
 * This is a type-transforming validator that outputs Byte.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().toByte()
 * validator.validate("12")  // Success: 12.toByte()
 * validator.validate("abc") // Failure
 * ```
 *
 * @return A new validator that transforms string to Byte
 */
fun StringValidator.toByte(): Validator<String, Byte> = isByte().map { it.toByte() }

/**
 * Validates that the string can be parsed as a Double and converts it.
 *
 * This is a type-transforming validator that outputs Double.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().toDouble()
 * validator.validate("12.5") // Success: 12.5
 * validator.validate("abc")  // Failure
 * ```
 *
 * @return A new validator that transforms string to Double
 */
fun StringValidator.toDouble(): Validator<String, Double> = isDouble().map { it.toDouble() }

/**
 * Validates that the string can be parsed as a Float and converts it.
 *
 * This is a type-transforming validator that outputs Float.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().toFloat()
 * validator.validate("12.5") // Success: 12.5f
 * validator.validate("abc")  // Failure
 * ```
 *
 * @return A new validator that transforms string to Float
 */
fun StringValidator.toFloat(): Validator<String, Float> = isFloat().map { it.toFloat() }

/**
 * Validates that the string can be parsed as a BigDecimal and converts it.
 *
 * This is a type-transforming validator that outputs BigDecimal.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().toBigDecimal()
 * validator.validate("123.456789") // Success: BigDecimal("123.456789")
 * validator.validate("abc")        // Failure
 * ```
 *
 * @return A new validator that transforms string to BigDecimal
 */
fun StringValidator.toBigDecimal(): Validator<String, java.math.BigDecimal> = isBigDecimal().map { it.toBigDecimal() }

/**
 * Validates that the string can be parsed as a BigInteger and converts it.
 *
 * This is a type-transforming validator that outputs BigInteger.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().toBigInteger()
 * validator.validate("123456789012345") // Success: BigInteger("123456789012345")
 * validator.validate("abc")             // Failure
 * ```
 *
 * @return A new validator that transforms string to BigInteger
 */
fun StringValidator.toBigInteger(): Validator<String, java.math.BigInteger> = isBigInteger().map { it.toBigInteger() }

/**
 * Validates that the string can be parsed as a Boolean and converts it.
 *
 * This is a type-transforming validator that outputs Boolean.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().toBoolean()
 * validator.validate("true")  // Success: true
 * validator.validate("false") // Success: false
 * validator.validate("yes")   // Failure
 * ```
 *
 * @return A new validator that transforms string to Boolean
 */
fun StringValidator.toBoolean(): Validator<String, Boolean> = isBoolean().map { it.toBoolean() }
