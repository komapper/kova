package org.komapper.extension.validator

import java.util.Locale
import kotlin.reflect.KClass

/**
 * Validates that the string can be parsed as an Int.
 *
 * Example:
 * ```kotlin
 * tryValidate { "123".ensureInt() }  // Success
 * tryValidate { "12.5".ensureInt() } // Failure
 * tryValidate { "abc".ensureInt() }  // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun String.ensureInt(
    message: MessageProvider = { "kova.string.int".resource },
) = this.constrain("kova.string.int") { val _ = transformToInt(it, message) }

/**
 * Validates that the string can be parsed as a Long.
 *
 * Example:
 * ```kotlin
 * tryValidate { "123456789".ensureLong() } // Success
 * tryValidate { "abc".ensureLong() }       // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun String.ensureLong(
    message: MessageProvider = { "kova.string.long".resource },
) = this.constrain("kova.string.long") { val _ = transformToLong(it, message) }

/**
 * Validates that the string can be parsed as a Short.
 *
 * Example:
 * ```kotlin
 * tryValidate { "123".ensureShort() } // Success
 * tryValidate { "abc".ensureShort() } // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun String.ensureShort(
    message: MessageProvider = { "kova.string.short".resource },
) = this.constrain("kova.string.short") { val _ = transformToShort(it, message) }

/**
 * Validates that the string can be parsed as a Byte.
 *
 * Example:
 * ```kotlin
 * tryValidate { "12".ensureByte() }  // Success
 * tryValidate { "abc".ensureByte() } // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun String.ensureByte(
    message: MessageProvider = { "kova.string.byte".resource },
) = this.constrain("kova.string.byte") { val _ = transformToByte(it, message) }

/**
 * Validates that the string can be parsed as a Double.
 *
 * Example:
 * ```kotlin
 * tryValidate { "12.5".ensureDouble() } // Success
 * tryValidate { "abc".ensureDouble() }  // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun String.ensureDouble(
    message: MessageProvider = { "kova.string.double".resource },
) = this.constrain("kova.string.double") { val _ = transformToDouble(it, message) }

/**
 * Validates that the string can be parsed as a Float.
 *
 * Example:
 * ```kotlin
 * tryValidate { "12.5".ensureFloat() } // Success
 * tryValidate { "abc".ensureFloat() }  // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun String.ensureFloat(
    message: MessageProvider = { "kova.string.float".resource },
) = this.constrain("kova.string.float") { val _ = transformToFloat(it, message) }

/**
 * Validates that the string can be parsed as a BigDecimal.
 *
 * Example:
 * ```kotlin
 * tryValidate { "123.456789".ensureBigDecimal() } // Success
 * tryValidate { "abc".ensureBigDecimal() }        // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun String.ensureBigDecimal(
    message: MessageProvider = { "kova.string.bigDecimal".resource },
) = this.constrain("kova.string.bigDecimal") { val _ = transformToBigDecimal(it, message) }

/**
 * Validates that the string can be parsed as a BigInteger.
 *
 * Example:
 * ```kotlin
 * tryValidate { "123456789012345".ensureBigInteger() } // Success
 * tryValidate { "abc".ensureBigInteger() }             // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun String.ensureBigInteger(
    message: MessageProvider = { "kova.string.bigInteger".resource },
) = this.constrain("kova.string.bigInteger") { val _ = transformToBigInteger(it, message) }

/**
 * Validates that the string can be parsed as a Boolean.
 *
 * Accepts "true" or "false" (case-insensitive).
 *
 * Example:
 * ```kotlin
 * tryValidate { "true".ensureBoolean() }  // Success
 * tryValidate { "false".ensureBoolean() } // Success
 * tryValidate { "yes".ensureBoolean() }   // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun String.ensureBoolean(
    message: MessageProvider = { "kova.string.boolean".resource },
) = this.constrain("kova.string.boolean") { val _ = transformToBoolean(it, message) }

/**
 * Validates that the string is a valid name for the specified enum type.
 *
 * Example:
 * ```kotlin
 * enum class Role { ADMIN, USER, GUEST }
 * tryValidate { "ADMIN".ensureEnum(Role::class) } // Success
 * tryValidate { "OTHER".ensureEnum(Role::class) } // Failure
 * ```
 *
 * @param klass The enum class to validate against
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun <E : Enum<E>> String.ensureEnum(
    klass: KClass<E>,
    message: (validNames: List<String>) -> Message = { "kova.string.enum".resource(it) },
) = this.constrain("kova.string.enum") { val _ = transformToEnum(it, klass, message) }

/**
 * Validates that the string is a valid name for the specified enum type (reified version).
 *
 * Example:
 * ```kotlin
 * enum class Role { ADMIN, USER, GUEST }
 * tryValidate { "ADMIN".ensureEnum<Role>() } // Success
 * tryValidate { "OTHER".ensureEnum<Role>() } // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
inline fun <reified E : Enum<E>> String.ensureEnum(
    noinline message: (validNames: List<String>) -> Message = { "kova.string.enum".resource(it) },
) = ensureEnum(E::class, message)

/**
 * Validates that the string is a valid enum name and converts it to the enum value.
 *
 * This is a type-transforming validator that outputs the enum type.
 *
 * Example:
 * ```kotlin
 * enum class Role { ADMIN, USER, GUEST }
 * tryValidate { transformToEnum<Role>("ADMIN") } // Success: Role.ADMIN
 * tryValidate { transformToEnum<Role>("OTHER") } // Failure
 * ```
 */
context(_: Validation)
inline fun <reified E : Enum<E>> transformToEnum(
    input: String,
    noinline message: (validNames: List<String>) -> Message = { "kova.string.enum".resource(it) },
) = transformToEnum(input, E::class, message)

context(_: Validation)
fun <E : Enum<E>> transformToEnum(
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
 * tryValidate { "HELLO".ensureUppercase() } // Success
 * tryValidate { "hello".ensureUppercase() } // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun String.ensureUppercase(
    message: MessageProvider = { "kova.string.uppercase".resource },
) = this.constrain("kova.string.uppercase") { satisfies(it == it.uppercase(Locale.getDefault()), message) }

/**
 * Validates that the string is in ensureLowercase.
 *
 * Example:
 * ```kotlin
 * tryValidate { "hello".ensureLowercase() } // Success
 * tryValidate { "HELLO".ensureLowercase() } // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun String.ensureLowercase(
    message: MessageProvider = { "kova.string.lowercase".resource },
) = this.constrain("kova.string.lowercase") { satisfies(it == it.lowercase(Locale.getDefault()), message) }

/**
 * Validates that the string can be parsed as an Int and converts it.
 *
 * This is a type-transforming validator that outputs Int.
 *
 * Example:
 * ```kotlin
 * tryValidate { transformToInt("123") } // Success: 123
 * tryValidate { transformToInt("abc") } // Failure
 * ```
 */
context(_: Validation)
fun transformToInt(
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
 * tryValidate { transformToLong("123456789") } // Success: 123456789L
 * tryValidate { transformToLong("abc") }       // Failure
 * ```
 */
context(_: Validation)
fun transformToLong(
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
 * tryValidate { transformToShort("123") } // Success: 123.toShort()
 * tryValidate { transformToShort("abc") } // Failure
 * ```
 */
context(_: Validation)
fun transformToShort(
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
 * tryValidate { transformToByte("12") }  // Success: 12.toByte()
 * tryValidate { transformToByte("abc") } // Failure
 * ```
 */
context(_: Validation)
fun transformToByte(
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
 * tryValidate { transformToDouble("12.5") } // Success: 12.5
 * tryValidate { transformToDouble("abc") }  // Failure
 * ```
 */
context(_: Validation)
fun transformToDouble(
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
 * tryValidate { transformToFloat("12.5") } // Success: 12.5f
 * tryValidate { transformToFloat("abc") }  // Failure
 * ```
 */
context(_: Validation)
fun transformToFloat(
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
 * tryValidate { transformToBigDecimal("123.456789") } // Success: BigDecimal("123.456789")
 * tryValidate { transformToBigDecimal("abc") }        // Failure
 * ```
 */
context(_: Validation)
fun transformToBigDecimal(
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
 * tryValidate { transformToBigInteger("123456789012345") } // Success: BigInteger("123456789012345")
 * tryValidate { transformToBigInteger("abc") }             // Failure
 * ```
 */
context(_: Validation)
fun transformToBigInteger(
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
 * tryValidate { transformToBoolean("true") }  // Success: true
 * tryValidate { transformToBoolean("false") } // Success: false
 * tryValidate { transformToBoolean("yes") }   // Failure
 * ```
 */
context(_: Validation)
fun transformToBoolean(
    input: String,
    message: MessageProvider = { "kova.string.boolean".resource },
) = toNonNullable(input.toBooleanStrictOrNull(), "kova.string.boolean", message)
