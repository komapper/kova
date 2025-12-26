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
context(_: Validation, _: Accumulate)
fun String.isInt(message: MessageProvider = { "kova.string.isInt".resource }) = constrain("kova.string.isInt") { toInt(message) }

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
context(_: Validation, _: Accumulate)
fun String.isLong(message: MessageProvider = { "kova.string.isLong".resource }) = constrain("kova.string.isLong") { toLong(message) }

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
context(_: Validation, _: Accumulate)
fun String.isShort(message: MessageProvider = { "kova.string.isShort".resource }) = constrain("kova.string.isShort") { toShort(message) }

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
context(_: Validation, _: Accumulate)
fun String.isByte(message: MessageProvider = { "kova.string.isByte".resource }) = constrain("kova.string.isByte") { toByte(message) }

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
context(_: Validation, _: Accumulate)
fun String.isDouble(message: MessageProvider = { "kova.string.isDouble".resource }) =
    constrain("kova.string.isDouble") { toDouble(message) }

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
context(_: Validation, _: Accumulate)
fun String.isFloat(message: MessageProvider = { "kova.string.isFloat".resource }) = constrain("kova.string.isFloat") { toFloat(message) }

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
context(_: Validation, _: Accumulate)
fun String.isBigDecimal(message: MessageProvider = { "kova.string.isBigDecimal".resource }) =
    constrain("kova.string.isBigDecimal") { toBigDecimal(message) }

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
context(_: Validation, _: Accumulate)
fun String.isBigInteger(message: MessageProvider = { "kova.string.isBigInteger".resource }) =
    constrain("kova.string.isBigInteger") { toBigInteger(message) }

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
context(_: Validation, _: Accumulate)
fun String.isBoolean(message: MessageProvider = { "kova.string.isBoolean".resource }) =
    constrain("kova.string.isBoolean") { toBoolean(message) }

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
context(_: Validation, _: Accumulate)
fun <E : Enum<E>> String.isEnum(
    klass: KClass<E>,
    message: (validNames: List<String>) -> Message = { "kova.string.isEnum".resource(it) },
) = constrain("kova.string.isEnum") { toEnum(klass, message) }

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
context(_: Validation, _: Accumulate)
inline fun <reified E : Enum<E>> String.isEnum(
    noinline message: (validNames: List<String>) -> Message = { "kova.string.isEnum".resource(it) },
) = isEnum(E::class, message)

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
context(_: Validation, _: Accumulate)
inline fun <reified E : Enum<E>> String.toEnum(
    noinline message: (validNames: List<String>) -> Message = { "kova.string.isEnum".resource(it) },
) = toEnum(E::class, message)

context(_: Validation, _: Accumulate)
fun <E : Enum<E>> String.toEnum(
    klass: KClass<E>,
    message: (validNames: List<String>) -> Message = { "kova.string.isEnum".resource(it) },
): E =
    runCatching { java.lang.Enum.valueOf(klass.java, this) }.getOrNull().toNonNullable {
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
context(_: Validation, _: Accumulate)
fun String.uppercase(message: MessageProvider = { "kova.string.uppercase".resource }) =
    constrain("kova.string.uppercase") { satisfies(it == it.uppercase(Locale.getDefault()), message) }

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
context(_: Validation, _: Accumulate)
fun String.lowercase(message: MessageProvider = { "kova.string.lowercase".resource }) =
    constrain("kova.string.lowercase") { satisfies(it == it.lowercase(Locale.getDefault()), message) }

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
context(_: Validation, _: Accumulate)
fun String.toInt(message: MessageProvider = { "kova.string.isInt".resource }) = toIntOrNull().toNonNullable(message)

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
context(_: Validation, _: Accumulate)
fun String.toLong(message: MessageProvider = { "kova.string.isLong".resource }) = toLongOrNull().toNonNullable(message)

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
context(_: Validation, _: Accumulate)
fun String.toShort(message: MessageProvider = { "kova.string.isShort".resource }) = toShortOrNull().toNonNullable(message)

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
context(_: Validation, _: Accumulate)
fun String.toByte(message: MessageProvider = { "kova.string.isByte".resource }) = toByteOrNull().toNonNullable(message)

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
context(_: Validation, _: Accumulate)
fun String.toDouble(message: MessageProvider = { "kova.string.isDouble".resource }) = toDoubleOrNull().toNonNullable(message)

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
context(_: Validation, _: Accumulate)
fun String.toFloat(message: MessageProvider = { "kova.string.isFloat".resource }) = toFloatOrNull().toNonNullable(message)

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
context(_: Validation, _: Accumulate)
fun String.toBigDecimal(message: MessageProvider = { "kova.string.isBigDecimal".resource }) = toBigDecimalOrNull().toNonNullable(message)

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
context(_: Validation, _: Accumulate)
fun String.toBigInteger(message: MessageProvider = { "kova.string.isBigInteger".resource }) = toBigIntegerOrNull().toNonNullable(message)

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
context(_: Validation, _: Accumulate)
fun String.toBoolean(message: MessageProvider = { "kova.string.isBoolean".resource }) = toBooleanStrictOrNull().toNonNullable(message)
