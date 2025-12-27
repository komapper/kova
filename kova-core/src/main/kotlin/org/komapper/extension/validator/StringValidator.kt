package org.komapper.extension.validator

import java.util.Locale
import kotlin.reflect.KClass

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
@IgnorableReturnValue
fun Validation.isInt(
    input: String,
    message: MessageProvider = { "kova.string.isInt".resource },
) = input.constrain("kova.string.isInt") { toInt(input, message) }

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
@IgnorableReturnValue
fun Validation.isLong(
    input: String,
    message: MessageProvider = { "kova.string.isLong".resource },
) = input.constrain("kova.string.isLong") { toLong(input, message) }

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
@IgnorableReturnValue
fun Validation.isShort(
    input: String,
    message: MessageProvider = { "kova.string.isShort".resource },
) = input.constrain("kova.string.isShort") { toShort(input, message) }

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
@IgnorableReturnValue
fun Validation.isByte(
    input: String,
    message: MessageProvider = { "kova.string.isByte".resource },
) = input.constrain("kova.string.isByte") { toByte(input, message) }

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
@IgnorableReturnValue
fun Validation.isDouble(
    input: String,
    message: MessageProvider = { "kova.string.isDouble".resource },
) = input.constrain("kova.string.isDouble") { toDouble(input, message) }

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
@IgnorableReturnValue
fun Validation.isFloat(
    input: String,
    message: MessageProvider = { "kova.string.isFloat".resource },
) = input.constrain("kova.string.isFloat") { toFloat(input, message) }

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
@IgnorableReturnValue
fun Validation.isBigDecimal(
    input: String,
    message: MessageProvider = { "kova.string.isBigDecimal".resource },
) = input.constrain("kova.string.isBigDecimal") { toBigDecimal(input, message) }

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
@IgnorableReturnValue
fun Validation.isBigInteger(
    input: String,
    message: MessageProvider = { "kova.string.isBigInteger".resource },
) = input.constrain("kova.string.isBigInteger") { toBigInteger(input, message) }

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
@IgnorableReturnValue
fun Validation.isBoolean(
    input: String,
    message: MessageProvider = { "kova.string.isBoolean".resource },
) = input.constrain("kova.string.isBoolean") { toBoolean(input, message) }

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
@IgnorableReturnValue
fun <E : Enum<E>> Validation.isEnum(
    input: String,
    klass: KClass<E>,
    message: (validNames: List<String>) -> Message = { "kova.string.isEnum".resource(it) },
) = input.constrain("kova.string.isEnum") { toEnum(input, klass, message) }

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
@IgnorableReturnValue
inline fun <reified E : Enum<E>> Validation.isEnum(
    input: String,
    noinline message: (validNames: List<String>) -> Message = { "kova.string.isEnum".resource(it) },
) = isEnum(input, E::class, message)

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
inline fun <reified E : Enum<E>> Validation.toEnum(
    input: String,
    noinline message: (validNames: List<String>) -> Message = { "kova.string.isEnum".resource(it) },
) = toEnum(input, E::class, message)

fun <E : Enum<E>> Validation.toEnum(
    input: String,
    klass: KClass<E>,
    message: (validNames: List<String>) -> Message = { "kova.string.isEnum".resource(it) },
): E =
    toNonNullable(runCatching { java.lang.Enum.valueOf(klass.java, input) }.getOrNull()) {
        message(klass.java.enumConstants.map { enum -> enum.name })
    }

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
@IgnorableReturnValue
fun Validation.uppercase(
    input: String,
    message: MessageProvider = { "kova.string.uppercase".resource },
) = input.constrain("kova.string.uppercase") { satisfies(it == it.uppercase(Locale.getDefault()), message) }

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
@IgnorableReturnValue
fun Validation.lowercase(
    input: String,
    message: MessageProvider = { "kova.string.lowercase".resource },
) = input.constrain("kova.string.lowercase") { satisfies(it == it.lowercase(Locale.getDefault()), message) }

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
fun String.toUppercase() = uppercase()

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
fun String.toLowercase() = lowercase()

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
fun Validation.toInt(
    input: String,
    message: MessageProvider = { "kova.string.isInt".resource },
) = toNonNullable(input.toIntOrNull(), message)

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
fun Validation.toLong(
    input: String,
    message: MessageProvider = { "kova.string.isLong".resource },
) = toNonNullable(input.toLongOrNull(), message)

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
fun Validation.toShort(
    input: String,
    message: MessageProvider = { "kova.string.isShort".resource },
) = toNonNullable(input.toShortOrNull(), message)

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
fun Validation.toByte(
    input: String,
    message: MessageProvider = { "kova.string.isByte".resource },
) = toNonNullable(input.toByteOrNull(), message)

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
fun Validation.toDouble(
    input: String,
    message: MessageProvider = { "kova.string.isDouble".resource },
) = toNonNullable(input.toDoubleOrNull(), message)

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
fun Validation.toFloat(
    input: String,
    message: MessageProvider = { "kova.string.isFloat".resource },
) = toNonNullable(input.toFloatOrNull(), message)

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
fun Validation.toBigDecimal(
    input: String,
    message: MessageProvider = { "kova.string.isBigDecimal".resource },
) = toNonNullable(input.toBigDecimalOrNull(), message)

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
fun Validation.toBigInteger(
    input: String,
    message: MessageProvider = { "kova.string.isBigInteger".resource },
) = toNonNullable(input.toBigIntegerOrNull(), message)

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
fun Validation.toBoolean(
    input: String,
    message: MessageProvider = { "kova.string.isBoolean".resource },
) = toNonNullable(input.toBooleanStrictOrNull(), message)
