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
public funString.ensureInt(
    message: MessageProvider = { "kova.string.int".resource },
) = apply { constrain("kova.string.int") { val _ = it.transformToInt(message) } }

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
public funString.ensureLong(
    message: MessageProvider = { "kova.string.long".resource },
) = apply { constrain("kova.string.long") { val _ = it.transformToLong(message) } }

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
public funString.ensureShort(
    message: MessageProvider = { "kova.string.short".resource },
) = apply { constrain("kova.string.short") { val _ = it.transformToShort(message) } }

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
public funString.ensureByte(
    message: MessageProvider = { "kova.string.byte".resource },
) = apply { constrain("kova.string.byte") { val _ = it.transformToByte(message) } }

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
public funString.ensureDouble(
    message: MessageProvider = { "kova.string.double".resource },
) = apply { constrain("kova.string.double") { val _ = it.transformToDouble(message) } }

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
public funString.ensureFloat(
    message: MessageProvider = { "kova.string.float".resource },
) = apply { constrain("kova.string.float") { val _ = it.transformToFloat(message) } }

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
public funString.ensureBigDecimal(
    message: MessageProvider = { "kova.string.bigDecimal".resource },
) = apply { constrain("kova.string.bigDecimal") { val _ = it.transformToBigDecimal(message) } }

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
public funString.ensureBigInteger(
    message: MessageProvider = { "kova.string.bigInteger".resource },
) = apply { constrain("kova.string.bigInteger") { val _ = it.transformToBigInteger(message) } }

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
public funString.ensureBoolean(
    message: MessageProvider = { "kova.string.boolean".resource },
) = apply { constrain("kova.string.boolean") { val _ = it.transformToBoolean(message) } }

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
public fun<E : Enum<E>> String.ensureEnum(
    klass: KClass<E>,
    message: (validNames: List<String>) -> Message = { "kova.string.enum".resource(it) },
) = apply { constrain("kova.string.enum") { val _ = it.transformToEnum(klass, message) } }

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
public inline fun<reified E : Enum<E>> String.ensureEnum(
    noinline message: (validNames: List<String>) -> Message = { "kova.string.enum".resource(it) },
) = ensureEnum(E::class, message)

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
public funString.ensureUppercase(
    message: MessageProvider = { "kova.string.uppercase".resource },
) = apply { constrain("kova.string.uppercase") { satisfies(it == it.uppercase(Locale.getDefault()), message) } }

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
public funString.ensureLowercase(
    message: MessageProvider = { "kova.string.lowercase".resource },
) = apply { constrain("kova.string.lowercase") { satisfies(it == it.lowercase(Locale.getDefault()), message) } }

/**
 * Validates that the string can be parsed as an Int and converts it.
 *
 * This is a type-transforming validator that outputs Int.
 *
 * Example:
 * ```kotlin
 * tryValidate { "123".transformToInt() } // Success: 123
 * tryValidate { "abc".transformToInt() } // Failure
 * ```
 */
context(_: Validation)
public funString.transformToInt(
    message: MessageProvider = { "kova.string.int".resource },
) = toIntOrNull().toNonNullable("kova.string.int", message)

/**
 * Validates that the string can be parsed as a Long and converts it.
 *
 * This is a type-transforming validator that outputs Long.
 *
 * Example:
 * ```kotlin
 * tryValidate { "123456789".transformToLong() } // Success: 123456789L
 * tryValidate { "abc".transformToLong() }       // Failure
 * ```
 */
context(_: Validation)
public funString.transformToLong(
    message: MessageProvider = { "kova.string.long".resource },
) = toLongOrNull().toNonNullable("kova.string.long", message)

/**
 * Validates that the string can be parsed as a Short and converts it.
 *
 * This is a type-transforming validator that outputs Short.
 *
 * Example:
 * ```kotlin
 * tryValidate { "123".transformToShort() } // Success: 123.toShort()
 * tryValidate { "abc".transformToShort() } // Failure
 * ```
 */
context(_: Validation)
public funString.transformToShort(
    message: MessageProvider = { "kova.string.short".resource },
) = toShortOrNull().toNonNullable("kova.string.short", message)

/**
 * Validates that the string can be parsed as a Byte and converts it.
 *
 * This is a type-transforming validator that outputs Byte.
 *
 * Example:
 * ```kotlin
 * tryValidate { "12".transformToByte() }  // Success: 12.toByte()
 * tryValidate { "abc".transformToByte() } // Failure
 * ```
 */
context(_: Validation)
public funString.transformToByte(
    message: MessageProvider = { "kova.string.byte".resource },
) = toByteOrNull().toNonNullable("kova.string.byte", message)

/**
 * Validates that the string can be parsed as a Double and converts it.
 *
 * This is a type-transforming validator that outputs Double.
 *
 * Example:
 * ```kotlin
 * tryValidate { "12.5".transformToDouble() } // Success: 12.5
 * tryValidate { "abc".transformToDouble() }  // Failure
 * ```
 */
context(_: Validation)
public funString.transformToDouble(
    message: MessageProvider = { "kova.string.double".resource },
) = toDoubleOrNull().toNonNullable("kova.string.double", message)

/**
 * Validates that the string can be parsed as a Float and converts it.
 *
 * This is a type-transforming validator that outputs Float.
 *
 * Example:
 * ```kotlin
 * tryValidate { "12.5".transformToFloat() } // Success: 12.5f
 * tryValidate { "abc".transformToFloat() }  // Failure
 * ```
 */
context(_: Validation)
public funString.transformToFloat(
    message: MessageProvider = { "kova.string.float".resource },
) = toFloatOrNull().toNonNullable("kova.string.float", message)

/**
 * Validates that the string can be parsed as a BigDecimal and converts it.
 *
 * This is a type-transforming validator that outputs BigDecimal.
 *
 * Example:
 * ```kotlin
 * tryValidate { "123.456789".transformToBigDecimal() } // Success: BigDecimal("123.456789")
 * tryValidate { "abc".transformToBigDecimal() }        // Failure
 * ```
 */
context(_: Validation)
public funString.transformToBigDecimal(
    message: MessageProvider = { "kova.string.bigDecimal".resource },
) = toBigDecimalOrNull().toNonNullable("kova.string.bigDecimal", message)

/**
 * Validates that the string can be parsed as a BigInteger and converts it.
 *
 * This is a type-transforming validator that outputs BigInteger.
 *
 * Example:
 * ```kotlin
 * tryValidate { "123456789012345".transformToBigInteger() } // Success: BigInteger("123456789012345")
 * tryValidate { "abc".transformToBigInteger() }             // Failure
 * ```
 */
context(_: Validation)
public funString.transformToBigInteger(
    message: MessageProvider = { "kova.string.bigInteger".resource },
) = toBigIntegerOrNull().toNonNullable("kova.string.bigInteger", message)

/**
 * Validates that the string can be parsed as a Boolean and converts it.
 *
 * This is a type-transforming validator that outputs Boolean.
 *
 * Example:
 * ```kotlin
 * tryValidate { "true".transformToBoolean() }  // Success: true
 * tryValidate { "false".transformToBoolean() } // Success: false
 * tryValidate { "yes".transformToBoolean() }   // Failure
 * ```
 */
context(_: Validation)
public funString.transformToBoolean(
    message: MessageProvider = { "kova.string.boolean".resource },
) = toBooleanStrictOrNull().toNonNullable("kova.string.boolean", message)

/**
 * Validates that the string is a valid enum name and converts it to the enum value.
 *
 * This is a type-transforming validator that outputs the enum type.
 *
 * Example:
 * ```kotlin
 * enum class Role { ADMIN, USER, GUEST }
 * tryValidate { "ADMIN".transformToEnum<Role>() } // Success: Role.ADMIN
 * tryValidate { "OTHER".transformToEnum<Role>() } // Failure
 * ```
 */
context(_: Validation)
public inline fun<reified E : Enum<E>> String.transformToEnum(
    noinline message: (validNames: List<String>) -> Message = { "kova.string.enum".resource(it) },
) = transformToEnum(E::class, message)

context(_: Validation)
public fun<E : Enum<E>> String.transformToEnum(
    klass: KClass<E>,
    message: (validNames: List<String>) -> Message = { "kova.string.enum".resource(it) },
): E {
    val enumOrNull = runCatching { java.lang.Enum.valueOf(klass.java, this) }.getOrNull()
    return enumOrNull.toNonNullable("kova.string.enum") {
        message(klass.java.enumConstants.map { enum -> enum.name })
    }
}
