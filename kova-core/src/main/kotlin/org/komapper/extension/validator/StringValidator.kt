package org.komapper.extension.validator

import kotlin.reflect.KClass

/**
 * Type alias for string validators.
 *
 * Provides a convenient type for validators that work with String inputs and outputs.
 */
typealias StringValidator = IdentityValidator<String>

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
fun StringValidator.isInt(message: MessageProvider = { "kova.string.isInt".resource }) =
    constrain { satisfies(it.toIntOrNull() != null, message) }

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
fun StringValidator.isLong(message: MessageProvider = { "kova.string.isLong".resource }) =
    constrain { satisfies(it.toLongOrNull() != null, message) }

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
fun StringValidator.isShort(message: MessageProvider = { "kova.string.isShort".resource }) =
    constrain { satisfies(it.toShortOrNull() != null, message) }

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
fun StringValidator.isByte(message: MessageProvider = { "kova.string.isByte".resource }) =
    constrain { satisfies(it.toByteOrNull() != null, message) }

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
fun StringValidator.isDouble(message: MessageProvider = { "kova.string.isDouble".resource }) =
    constrain { satisfies(it.toDoubleOrNull() != null, message) }

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
fun StringValidator.isFloat(message: MessageProvider = { "kova.string.isFloat".resource }) =
    constrain { satisfies(it.toFloatOrNull() != null, message) }

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
fun StringValidator.isBigDecimal(message: MessageProvider = { "kova.string.isBigDecimal".resource }) =
    constrain { satisfies(it.toBigDecimalOrNull() != null, message) }

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
fun StringValidator.isBigInteger(message: MessageProvider = { "kova.string.isBigInteger".resource }) =
    constrain { satisfies(it.toBigIntegerOrNull() != null, message) }

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
fun StringValidator.isBoolean(message: MessageProvider = { "kova.string.isBoolean".resource }) =
    constrain { satisfies(it.toBooleanStrictOrNull() != null, message) }

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
    message: ValidationContext.(validNames: List<String>) -> Message = { "kova.string.isEnum".resource(it) },
): StringValidator {
    val enumValues = klass.java.enumConstants
    val validNames = enumValues.map { it.name }
    return constrain { satisfies(validNames.contains(it)) { message(validNames) } }
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
 * @param message Custom error message provider
 * @return A new validator with the is-enum constraint
 */
inline fun <reified E : Enum<E>> StringValidator.isEnum(
    noinline message: ValidationContext.(validNames: List<String>) -> Message = { "kova.string.isEnum".resource(it) },
): StringValidator = isEnum(E::class, message)

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
fun StringValidator.uppercase(message: MessageProvider = { "kova.string.uppercase".resource }) =
    constrain { satisfies(it == it.uppercase(), message) }

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
fun StringValidator.lowercase(message: MessageProvider = { "kova.string.lowercase".resource }) =
    constrain { satisfies(it == it.lowercase(), message) }

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
 * val validator = Kova.string().toUppercase()
 * validator.validate("hello") // Success: "HELLO"
 * ```
 *
 * @return A new validator that transforms to uppercase
 */
fun StringValidator.toUppercase() = map { it.uppercase() }

/**
 * Transforms the string to lowercase.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().toLowercase()
 * validator.validate("HELLO") // Success: "hello"
 * ```
 *
 * @return A new validator that transforms to lowercase
 */
fun StringValidator.toLowercase() = map { it.lowercase() }

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
