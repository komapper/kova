package org.komapper.extension.validator

import java.util.Locale
import kotlin.reflect.KClass

/**
 * Validates that the string can be parsed as an Int.
 *
 * Example:
 * ```kotlin
 * tryValidate { isInt("123") }  // Success
 * tryValidate { isInt("12.5") } // Failure
 * tryValidate { isInt("abc") }  // Failure
 * ```
 *
 * @param message Custom error message provider
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
 * tryValidate { isLong("123456789") } // Success
 * tryValidate { isLong("abc") }       // Failure
 * ```
 *
 * @param message Custom error message provider
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
 * tryValidate { isShort("123") } // Success
 * tryValidate { isShort("abc") } // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.isShort(
    input: String,
    message: MessageProvider = { "kova.string.isShort".resource },
) = input.constrain("kova.string.isShort") { val _ = toShort(input, message) }

/**
 * Validates that the string can be parsed as a Byte.
 *
 * Example:
 * ```kotlin
 * tryValidate { isByte("12") }  // Success
 * tryValidate { isByte("abc") } // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.isByte(
    input: String,
    message: MessageProvider = { "kova.string.isByte".resource },
) = input.constrain("kova.string.isByte") { val _ = toByte(input, message) }

/**
 * Validates that the string can be parsed as a Double.
 *
 * Example:
 * ```kotlin
 * tryValidate { isDouble("12.5") } // Success
 * tryValidate { isDouble("abc") }  // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.isDouble(
    input: String,
    message: MessageProvider = { "kova.string.isDouble".resource },
) = input.constrain("kova.string.isDouble") { val _ = toDouble(input, message) }

/**
 * Validates that the string can be parsed as a Float.
 *
 * Example:
 * ```kotlin
 * tryValidate { isFloat("12.5") } // Success
 * tryValidate { isFloat("abc") }  // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.isFloat(
    input: String,
    message: MessageProvider = { "kova.string.isFloat".resource },
) = input.constrain("kova.string.isFloat") { val _ = toFloat(input, message) }

/**
 * Validates that the string can be parsed as a BigDecimal.
 *
 * Example:
 * ```kotlin
 * tryValidate { isBigDecimal("123.456789") } // Success
 * tryValidate { isBigDecimal("abc") }        // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.isBigDecimal(
    input: String,
    message: MessageProvider = { "kova.string.isBigDecimal".resource },
) = input.constrain("kova.string.isBigDecimal") { val _ = toBigDecimal(input, message) }

/**
 * Validates that the string can be parsed as a BigInteger.
 *
 * Example:
 * ```kotlin
 * tryValidate { isBigInteger("123456789012345") } // Success
 * tryValidate { isBigInteger("abc") }             // Failure
 * ```
 *
 * @param message Custom error message provider
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
 * tryValidate { isBoolean("true") }  // Success
 * tryValidate { isBoolean("false") } // Success
 * tryValidate { isBoolean("yes") }   // Failure
 * ```
 *
 * @param message Custom error message provider
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
 * tryValidate { isEnum("ADMIN", Role::class) } // Success
 * tryValidate { isEnum("OTHER", Role::class) } // Failure
 * ```
 *
 * @param klass The enum class to validate against
 * @param message Custom error message provider
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
 * tryValidate { isEnum<Role>("ADMIN") } // Success
 * tryValidate { isEnum<Role>("OTHER") } // Failure
 * ```
 *
 * @param message Custom error message provider
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
 * tryValidate { toEnum<Role>("ADMIN") } // Success: Role.ADMIN
 * tryValidate { toEnum<Role>("OTHER") } // Failure
 * ```
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
    toNonNullable(
        runCatching { java.lang.Enum.valueOf(klass.java, input) }.getOrNull(),
        "kova.string.isEnum",
    ) {
        message(klass.java.enumConstants.map { enum -> enum.name })
    }

/**
 * Validates that the string is in uppercase.
 *
 * Example:
 * ```kotlin
 * tryValidate { uppercase("HELLO") } // Success
 * tryValidate { uppercase("hello") } // Failure
 * ```
 *
 * @param message Custom error message provider
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
 * tryValidate { lowercase("hello") } // Success
 * tryValidate { lowercase("HELLO") } // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun Validation.lowercase(
    input: String,
    message: MessageProvider = { "kova.string.lowercase".resource },
) = input.constrain("kova.string.lowercase") { satisfies(it == it.lowercase(Locale.getDefault()), message) }

/**
 * Validates that the string can be parsed as an Int and converts it.
 *
 * This is a type-transforming validator that outputs Int.
 *
 * Example:
 * ```kotlin
 * tryValidate { toInt("123") } // Success: 123
 * tryValidate { toInt("abc") } // Failure
 * ```
 */
fun Validation.toInt(
    input: String,
    message: MessageProvider = { "kova.string.isInt".resource },
) = toNonNullable(input.toIntOrNull(), "kova.string.isInt", message)

/**
 * Validates that the string can be parsed as a Long and converts it.
 *
 * This is a type-transforming validator that outputs Long.
 *
 * Example:
 * ```kotlin
 * tryValidate { toLong("123456789") } // Success: 123456789L
 * tryValidate { toLong("abc") }       // Failure
 * ```
 */
fun Validation.toLong(
    input: String,
    message: MessageProvider = { "kova.string.isLong".resource },
) = toNonNullable(input.toLongOrNull(), "kova.string.isLong", message)

/**
 * Validates that the string can be parsed as a Short and converts it.
 *
 * This is a type-transforming validator that outputs Short.
 *
 * Example:
 * ```kotlin
 * tryValidate { toShort("123") } // Success: 123.toShort()
 * tryValidate { toShort("abc") } // Failure
 * ```
 */
fun Validation.toShort(
    input: String,
    message: MessageProvider = { "kova.string.isShort".resource },
) = toNonNullable(input.toShortOrNull(), "kova.string.isShort", message)

/**
 * Validates that the string can be parsed as a Byte and converts it.
 *
 * This is a type-transforming validator that outputs Byte.
 *
 * Example:
 * ```kotlin
 * tryValidate { toByte("12") }  // Success: 12.toByte()
 * tryValidate { toByte("abc") } // Failure
 * ```
 */
fun Validation.toByte(
    input: String,
    message: MessageProvider = { "kova.string.isByte".resource },
) = toNonNullable(input.toByteOrNull(), "kova.string.isByte", message)

/**
 * Validates that the string can be parsed as a Double and converts it.
 *
 * This is a type-transforming validator that outputs Double.
 *
 * Example:
 * ```kotlin
 * tryValidate { toDouble("12.5") } // Success: 12.5
 * tryValidate { toDouble("abc") }  // Failure
 * ```
 */
fun Validation.toDouble(
    input: String,
    message: MessageProvider = { "kova.string.isDouble".resource },
) = toNonNullable(input.toDoubleOrNull(), "kova.string.isDouble", message)

/**
 * Validates that the string can be parsed as a Float and converts it.
 *
 * This is a type-transforming validator that outputs Float.
 *
 * Example:
 * ```kotlin
 * tryValidate { toFloat("12.5") } // Success: 12.5f
 * tryValidate { toFloat("abc") }  // Failure
 * ```
 */
fun Validation.toFloat(
    input: String,
    message: MessageProvider = { "kova.string.isFloat".resource },
) = toNonNullable(input.toFloatOrNull(), "kova.string.isFloat", message)

/**
 * Validates that the string can be parsed as a BigDecimal and converts it.
 *
 * This is a type-transforming validator that outputs BigDecimal.
 *
 * Example:
 * ```kotlin
 * tryValidate { toBigDecimal("123.456789") } // Success: BigDecimal("123.456789")
 * tryValidate { toBigDecimal("abc") }        // Failure
 * ```
 */
fun Validation.toBigDecimal(
    input: String,
    message: MessageProvider = { "kova.string.isBigDecimal".resource },
) = toNonNullable(input.toBigDecimalOrNull(), "kova.string.isBigDecimal", message)

/**
 * Validates that the string can be parsed as a BigInteger and converts it.
 *
 * This is a type-transforming validator that outputs BigInteger.
 *
 * Example:
 * ```kotlin
 * tryValidate { toBigInteger("123456789012345") } // Success: BigInteger("123456789012345")
 * tryValidate { toBigInteger("abc") }             // Failure
 * ```
 */
fun Validation.toBigInteger(
    input: String,
    message: MessageProvider = { "kova.string.isBigInteger".resource },
) = toNonNullable(input.toBigIntegerOrNull(), "kova.string.isBigInteger", message)

/**
 * Validates that the string can be parsed as a Boolean and converts it.
 *
 * This is a type-transforming validator that outputs Boolean.
 *
 * Example:
 * ```kotlin
 * tryValidate { toBoolean("true") }  // Success: true
 * tryValidate { toBoolean("false") } // Success: false
 * tryValidate { toBoolean("yes") }   // Failure
 * ```
 */
fun Validation.toBoolean(
    input: String,
    message: MessageProvider = { "kova.string.isBoolean".resource },
) = toNonNullable(input.toBooleanStrictOrNull(), "kova.string.isBoolean", message)
