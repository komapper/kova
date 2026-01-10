package org.komapper.extension.validator

import java.util.Locale
import kotlin.reflect.KClass

/**
 * Validates that the string can be parsed as an Int.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureInt("123") }  // Success
 * tryValidate { ensureInt("12.5") } // Failure
 * tryValidate { ensureInt("abc") }  // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun ensureInt(
    input: String,
    message: MessageProvider = { "kova.string.int".resource },
) = input.constrain("kova.string.int") { parseInt(input, message) }

/**
 * Validates that the string can be parsed as a Long.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureLong("123456789") } // Success
 * tryValidate { ensureLong("abc") }       // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun ensureLong(
    input: String,
    message: MessageProvider = { "kova.string.long".resource },
) = input.constrain("kova.string.long") { parseLong(input, message) }

/**
 * Validates that the string can be parsed as a Short.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureShort("123") } // Success
 * tryValidate { ensureShort("abc") } // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun ensureShort(
    input: String,
    message: MessageProvider = { "kova.string.short".resource },
) = input.constrain("kova.string.short") { val _ = parseShort(input, message) }

/**
 * Validates that the string can be parsed as a Byte.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureByte("12") }  // Success
 * tryValidate { ensureByte("abc") } // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun ensureByte(
    input: String,
    message: MessageProvider = { "kova.string.byte".resource },
) = input.constrain("kova.string.byte") { val _ = parseByte(input, message) }

/**
 * Validates that the string can be parsed as a Double.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureDouble("12.5") } // Success
 * tryValidate { ensureDouble("abc") }  // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun ensureDouble(
    input: String,
    message: MessageProvider = { "kova.string.double".resource },
) = input.constrain("kova.string.double") { val _ = parseDouble(input, message) }

/**
 * Validates that the string can be parsed as a Float.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureFloat("12.5") } // Success
 * tryValidate { ensureFloat("abc") }  // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun ensureFloat(
    input: String,
    message: MessageProvider = { "kova.string.float".resource },
) = input.constrain("kova.string.float") { val _ = parseFloat(input, message) }

/**
 * Validates that the string can be parsed as a BigDecimal.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureBigDecimal("123.456789") } // Success
 * tryValidate { ensureBigDecimal("abc") }        // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun ensureBigDecimal(
    input: String,
    message: MessageProvider = { "kova.string.bigDecimal".resource },
) = input.constrain("kova.string.bigDecimal") { val _ = parseBigDecimal(input, message) }

/**
 * Validates that the string can be parsed as a BigInteger.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureBigInteger("123456789012345") } // Success
 * tryValidate { ensureBigInteger("abc") }             // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun ensureBigInteger(
    input: String,
    message: MessageProvider = { "kova.string.bigInteger".resource },
) = input.constrain("kova.string.bigInteger") { parseBigInteger(input, message) }

/**
 * Validates that the string can be parsed as a Boolean.
 *
 * Accepts "true" or "false" (case-insensitive).
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureBoolean("true") }  // Success
 * tryValidate { ensureBoolean("false") } // Success
 * tryValidate { ensureBoolean("yes") }   // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun ensureBoolean(
    input: String,
    message: MessageProvider = { "kova.string.boolean".resource },
) = input.constrain("kova.string.boolean") { parseBoolean(input, message) }

/**
 * Validates that the string is a valid name for the specified enum type.
 *
 * Example:
 * ```kotlin
 * enum class Role { ADMIN, USER, GUEST }
 * tryValidate { ensureEnum("ADMIN", Role::class) } // Success
 * tryValidate { ensureEnum("OTHER", Role::class) } // Failure
 * ```
 *
 * @param klass The enum class to validate against
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun <E : Enum<E>> ensureEnum(
    input: String,
    klass: KClass<E>,
    message: (validNames: List<String>) -> Message = { "kova.string.enum".resource(it) },
) = input.constrain("kova.string.enum") { parseEnum(input, klass, message) }

/**
 * Validates that the string is a valid name for the specified enum type (reified version).
 *
 * Example:
 * ```kotlin
 * enum class Role { ADMIN, USER, GUEST }
 * tryValidate { ensureEnum<Role>("ADMIN") } // Success
 * tryValidate { ensureEnum<Role>("OTHER") } // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
inline fun <reified E : Enum<E>> ensureEnum(
    input: String,
    noinline message: (validNames: List<String>) -> Message = { "kova.string.enum".resource(it) },
) = ensureEnum(input, E::class, message)

/**
 * Validates that the string is a valid enum name and converts it to the enum value.
 *
 * This is a type-transforming validator that outputs the enum type.
 *
 * Example:
 * ```kotlin
 * enum class Role { ADMIN, USER, GUEST }
 * tryValidate { parseEnum<Role>("ADMIN") } // Success: Role.ADMIN
 * tryValidate { parseEnum<Role>("OTHER") } // Failure
 * ```
 */
context(_: Validation)
inline fun <reified E : Enum<E>> parseEnum(
    input: String,
    noinline message: (validNames: List<String>) -> Message = { "kova.string.enum".resource(it) },
) = parseEnum(input, E::class, message)

context(_: Validation)
fun <E : Enum<E>> parseEnum(
    input: String,
    klass: KClass<E>,
    message: (validNames: List<String>) -> Message = { "kova.string.enum".resource(it) },
): E =
    toNonNullable(
        runCatching { java.lang.Enum.valueOf(klass.java, input) }.getOrNull(),
        "kova.string.enum",
    ) {
        message(klass.java.enumConstants.map { enum -> enum.name })
    }

/**
 * Validates that the string is in ensureUppercase.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureUppercase("HELLO") } // Success
 * tryValidate { ensureUppercase("hello") } // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun ensureUppercase(
    input: String,
    message: MessageProvider = { "kova.string.uppercase".resource },
) = input.constrain("kova.string.uppercase") { satisfies(it == it.uppercase(Locale.getDefault()), message) }

/**
 * Validates that the string is in ensureLowercase.
 *
 * Example:
 * ```kotlin
 * tryValidate { ensureLowercase("hello") } // Success
 * tryValidate { ensureLowercase("HELLO") } // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun ensureLowercase(
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
 * tryValidate { parseInt("123") } // Success: 123
 * tryValidate { parseInt("abc") } // Failure
 * ```
 */
context(_: Validation)
fun parseInt(
    input: String,
    message: MessageProvider = { "kova.string.int".resource },
) = toNonNullable(input.toIntOrNull(), "kova.string.int", message)

/**
 * Validates that the string can be parsed as a Long and converts it.
 *
 * This is a type-transforming validator that outputs Long.
 *
 * Example:
 * ```kotlin
 * tryValidate { parseLong("123456789") } // Success: 123456789L
 * tryValidate { parseLong("abc") }       // Failure
 * ```
 */
context(_: Validation)
fun parseLong(
    input: String,
    message: MessageProvider = { "kova.string.long".resource },
) = toNonNullable(input.toLongOrNull(), "kova.string.long", message)

/**
 * Validates that the string can be parsed as a Short and converts it.
 *
 * This is a type-transforming validator that outputs Short.
 *
 * Example:
 * ```kotlin
 * tryValidate { parseShort("123") } // Success: 123.parseShort()
 * tryValidate { parseShort("abc") } // Failure
 * ```
 */
context(_: Validation)
fun parseShort(
    input: String,
    message: MessageProvider = { "kova.string.short".resource },
) = toNonNullable(input.toShortOrNull(), "kova.string.short", message)

/**
 * Validates that the string can be parsed as a Byte and converts it.
 *
 * This is a type-transforming validator that outputs Byte.
 *
 * Example:
 * ```kotlin
 * tryValidate { parseByte("12") }  // Success: 12.parseByte()
 * tryValidate { parseByte("abc") } // Failure
 * ```
 */
context(_: Validation)
fun parseByte(
    input: String,
    message: MessageProvider = { "kova.string.byte".resource },
) = toNonNullable(input.toByteOrNull(), "kova.string.byte", message)

/**
 * Validates that the string can be parsed as a Double and converts it.
 *
 * This is a type-transforming validator that outputs Double.
 *
 * Example:
 * ```kotlin
 * tryValidate { parseDouble("12.5") } // Success: 12.5
 * tryValidate { parseDouble("abc") }  // Failure
 * ```
 */
context(_: Validation)
fun parseDouble(
    input: String,
    message: MessageProvider = { "kova.string.double".resource },
) = toNonNullable(input.toDoubleOrNull(), "kova.string.double", message)

/**
 * Validates that the string can be parsed as a Float and converts it.
 *
 * This is a type-transforming validator that outputs Float.
 *
 * Example:
 * ```kotlin
 * tryValidate { parseFloat("12.5") } // Success: 12.5f
 * tryValidate { parseFloat("abc") }  // Failure
 * ```
 */
context(_: Validation)
fun parseFloat(
    input: String,
    message: MessageProvider = { "kova.string.float".resource },
) = toNonNullable(input.toFloatOrNull(), "kova.string.float", message)

/**
 * Validates that the string can be parsed as a BigDecimal and converts it.
 *
 * This is a type-transforming validator that outputs BigDecimal.
 *
 * Example:
 * ```kotlin
 * tryValidate { parseBigDecimal("123.456789") } // Success: BigDecimal("123.456789")
 * tryValidate { parseBigDecimal("abc") }        // Failure
 * ```
 */
context(_: Validation)
fun parseBigDecimal(
    input: String,
    message: MessageProvider = { "kova.string.bigDecimal".resource },
) = toNonNullable(input.toBigDecimalOrNull(), "kova.string.bigDecimal", message)

/**
 * Validates that the string can be parsed as a BigInteger and converts it.
 *
 * This is a type-transforming validator that outputs BigInteger.
 *
 * Example:
 * ```kotlin
 * tryValidate { parseBigInteger("123456789012345") } // Success: BigInteger("123456789012345")
 * tryValidate { parseBigInteger("abc") }             // Failure
 * ```
 */
context(_: Validation)
fun parseBigInteger(
    input: String,
    message: MessageProvider = { "kova.string.bigInteger".resource },
) = toNonNullable(input.toBigIntegerOrNull(), "kova.string.bigInteger", message)

/**
 * Validates that the string can be parsed as a Boolean and converts it.
 *
 * This is a type-transforming validator that outputs Boolean.
 *
 * Example:
 * ```kotlin
 * tryValidate { parseBoolean("true") }  // Success: true
 * tryValidate { parseBoolean("false") } // Success: false
 * tryValidate { parseBoolean("yes") }   // Failure
 * ```
 */
context(_: Validation)
fun parseBoolean(
    input: String,
    message: MessageProvider = { "kova.string.boolean".resource },
) = toNonNullable(input.toBooleanStrictOrNull(), "kova.string.boolean", message)
