package org.komapper.extension.validator

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
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
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver String The string to validate
 * @param message Custom error message provider
 * @return The validated string value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun String.ensureInt(
    message: MessageProvider = { "kova.string.int".resource },
): String = constrain("kova.string.int") { satisfies(it.toIntOrNull() != null, message) }

/**
 * Validates that the string can be parsed as a Long.
 *
 * Example:
 * ```kotlin
 * tryValidate { "123456789".ensureLong() } // Success
 * tryValidate { "abc".ensureLong() }       // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver String The string to validate
 * @param message Custom error message provider
 * @return The validated string value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun String.ensureLong(
    message: MessageProvider = { "kova.string.long".resource },
): String = constrain("kova.string.long") { satisfies(it.toLongOrNull() != null, message) }

/**
 * Validates that the string can be parsed as a Short.
 *
 * Example:
 * ```kotlin
 * tryValidate { "123".ensureShort() } // Success
 * tryValidate { "abc".ensureShort() } // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver String The string to validate
 * @param message Custom error message provider
 * @return The validated string value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun String.ensureShort(
    message: MessageProvider = { "kova.string.short".resource },
): String = constrain("kova.string.short") { satisfies(it.toShortOrNull() != null, message) }

/**
 * Validates that the string can be parsed as a Byte.
 *
 * Example:
 * ```kotlin
 * tryValidate { "12".ensureByte() }  // Success
 * tryValidate { "abc".ensureByte() } // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver String The string to validate
 * @param message Custom error message provider
 * @return The validated string value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun String.ensureByte(
    message: MessageProvider = { "kova.string.byte".resource },
): String = constrain("kova.string.byte") { satisfies(it.toByteOrNull() != null, message) }

/**
 * Validates that the string can be parsed as a Double.
 *
 * Example:
 * ```kotlin
 * tryValidate { "12.5".ensureDouble() } // Success
 * tryValidate { "abc".ensureDouble() }  // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver String The string to validate
 * @param message Custom error message provider
 * @return The validated string value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun String.ensureDouble(
    message: MessageProvider = { "kova.string.double".resource },
): String = constrain("kova.string.double") { satisfies(it.toDoubleOrNull() != null, message) }

/**
 * Validates that the string can be parsed as a Float.
 *
 * Example:
 * ```kotlin
 * tryValidate { "12.5".ensureFloat() } // Success
 * tryValidate { "abc".ensureFloat() }  // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver String The string to validate
 * @param message Custom error message provider
 * @return The validated string value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun String.ensureFloat(
    message: MessageProvider = { "kova.string.float".resource },
): String = constrain("kova.string.float") { satisfies(it.toFloatOrNull() != null, message) }

/**
 * Validates that the string can be parsed as a BigDecimal.
 *
 * Example:
 * ```kotlin
 * tryValidate { "123.456789".ensureBigDecimal() } // Success
 * tryValidate { "abc".ensureBigDecimal() }        // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver String The string to validate
 * @param message Custom error message provider
 * @return The validated string value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun String.ensureBigDecimal(
    message: MessageProvider = { "kova.string.bigDecimal".resource },
): String = constrain("kova.string.bigDecimal") { satisfies(it.toBigDecimalOrNull() != null, message) }

/**
 * Validates that the string can be parsed as a BigInteger.
 *
 * Example:
 * ```kotlin
 * tryValidate { "123456789012345".ensureBigInteger() } // Success
 * tryValidate { "abc".ensureBigInteger() }             // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver String The string to validate
 * @param message Custom error message provider
 * @return The validated string value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun String.ensureBigInteger(
    message: MessageProvider = { "kova.string.bigInteger".resource },
): String = constrain("kova.string.bigInteger") { satisfies(it.toBigIntegerOrNull() != null, message) }

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
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver String The string to validate
 * @param message Custom error message provider
 * @return The validated string value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun String.ensureBoolean(
    message: MessageProvider = { "kova.string.boolean".resource },
): String = constrain("kova.string.boolean") { satisfies(it.toBooleanStrictOrNull() != null, message) }

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
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver String The string to validate
 * @param E The enum type
 * @param klass The enum class to validate against
 * @param message Custom error message provider that receives the list of valid enum names
 * @return The validated string value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun <E : Enum<E>> String.ensureEnum(
    klass: KClass<E>,
    message: (validNames: List<String>) -> Message = { "kova.string.enum".resource(it) },
): String = constrain("kova.string.enum") { val _ = it.transformToEnum(klass, message) }

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
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver String The string to validate
 * @param E The enum type
 * @param message Custom error message provider that receives the list of valid enum names
 * @return The validated string value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public inline fun <reified E : Enum<E>> String.ensureEnum(
    noinline message: (validNames: List<String>) -> Message = { "kova.string.enum".resource(it) },
): String = ensureEnum(E::class, message)

/**
 * Validates that the string can be parsed as a LocalDate.
 *
 * Example:
 * ```kotlin
 * tryValidate { "2025-01-17".ensureDate() }                                    // Success (ISO format)
 * tryValidate { "17/01/2025".ensureDate(DateTimeFormatter.ofPattern("dd/MM/yyyy")) } // Success (custom format)
 * tryValidate { "invalid".ensureDate() }                                       // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver String The string to validate
 * @param formatter The DateTimeFormatter to use for parsing (defaults to ISO_LOCAL_DATE)
 * @param message Custom error message provider
 * @return The validated string value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun String.ensureDate(
    formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE,
    message: MessageProvider = { "kova.string.localDate".resource },
): String = constrain("kova.string.localDate") { val _ = it.transformToDate(formatter, message) }

/**
 * Validates that the string can be parsed as a LocalDateTime.
 *
 * Example:
 * ```kotlin
 * tryValidate { "2025-01-17T10:30:00".ensureDateTime() }                                    // Success (ISO format)
 * tryValidate { "17/01/2025 10:30".ensureDateTime(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) } // Success (custom format)
 * tryValidate { "invalid".ensureDateTime() }                                               // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver String The string to validate
 * @param formatter The DateTimeFormatter to use for parsing (defaults to ISO_LOCAL_DATE_TIME)
 * @param message Custom error message provider
 * @return The validated string value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun String.ensureDateTime(
    formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME,
    message: MessageProvider = { "kova.string.localDateTime".resource },
): String = constrain("kova.string.localDateTime") { val _ = it.transformToDateTime(formatter, message) }

/**
 * Validates that the string can be parsed as a LocalTime.
 *
 * Example:
 * ```kotlin
 * tryValidate { "10:30:00".ensureTime() }                                    // Success (ISO format)
 * tryValidate { "10:30".ensureTime(DateTimeFormatter.ofPattern("HH:mm")) }   // Success (custom format)
 * tryValidate { "invalid".ensureTime() }                                     // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver String The string to validate
 * @param formatter The DateTimeFormatter to use for parsing (defaults to ISO_LOCAL_TIME)
 * @param message Custom error message provider
 * @return The validated string value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun String.ensureTime(
    formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_TIME,
    message: MessageProvider = { "kova.string.localTime".resource },
): String = constrain("kova.string.localTime") { val _ = it.transformToTime(formatter, message) }

/**
 * Validates that the string is in ensureUppercase.
 *
 * Example:
 * ```kotlin
 * tryValidate { "HELLO".ensureUppercase() } // Success
 * tryValidate { "hello".ensureUppercase() } // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver String The string to validate
 * @param message Custom error message provider
 * @return The validated string value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun String.ensureUppercase(
    message: MessageProvider = { "kova.string.uppercase".resource },
): String = constrain("kova.string.uppercase") { satisfies(it == it.uppercase(Locale.getDefault()), message) }

/**
 * Validates that the string is in ensureLowercase.
 *
 * Example:
 * ```kotlin
 * tryValidate { "hello".ensureLowercase() } // Success
 * tryValidate { "HELLO".ensureLowercase() } // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver String The string to validate
 * @param message Custom error message provider
 * @return The validated string value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun String.ensureLowercase(
    message: MessageProvider = { "kova.string.lowercase".resource },
): String = constrain("kova.string.lowercase") { satisfies(it == it.lowercase(Locale.getDefault()), message) }

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
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver String The string to validate and convert
 * @param message Custom error message provider
 * @return The converted Int value
 */
context(_: Validation)
public fun String.transformToInt(
    message: MessageProvider = { "kova.string.int".resource },
): Int = transformOrRaise("kova.string.int", message) { it.toIntOrNull() }

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
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver String The string to validate and convert
 * @param message Custom error message provider
 * @return The converted Long value
 */
context(_: Validation)
public fun String.transformToLong(
    message: MessageProvider = { "kova.string.long".resource },
): Long = transformOrRaise("kova.string.long", message) { it.toLongOrNull() }

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
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver String The string to validate and convert
 * @param message Custom error message provider
 * @return The converted Short value
 */
context(_: Validation)
public fun String.transformToShort(
    message: MessageProvider = { "kova.string.short".resource },
): Short = transformOrRaise("kova.string.short", message) { it.toShortOrNull() }

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
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver String The string to validate and convert
 * @param message Custom error message provider
 * @return The converted Byte value
 */
context(_: Validation)
public fun String.transformToByte(
    message: MessageProvider = { "kova.string.byte".resource },
): Byte = transformOrRaise("kova.string.byte", message) { it.toByteOrNull() }

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
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver String The string to validate and convert
 * @param message Custom error message provider
 * @return The converted Double value
 */
context(_: Validation)
public fun String.transformToDouble(
    message: MessageProvider = { "kova.string.double".resource },
): Double = transformOrRaise("kova.string.double", message) { it.toDoubleOrNull() }

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
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver String The string to validate and convert
 * @param message Custom error message provider
 * @return The converted Float value
 */
context(_: Validation)
public fun String.transformToFloat(
    message: MessageProvider = { "kova.string.float".resource },
): Float = transformOrRaise("kova.string.float", message) { it.toFloatOrNull() }

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
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver String The string to validate and convert
 * @param message Custom error message provider
 * @return The converted BigDecimal value
 */
context(_: Validation)
public fun String.transformToBigDecimal(
    message: MessageProvider = { "kova.string.bigDecimal".resource },
): java.math.BigDecimal = transformOrRaise("kova.string.bigDecimal", message) { it.toBigDecimalOrNull() }

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
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver String The string to validate and convert
 * @param message Custom error message provider
 * @return The converted BigInteger value
 */
context(_: Validation)
public fun String.transformToBigInteger(
    message: MessageProvider = { "kova.string.bigInteger".resource },
): java.math.BigInteger = transformOrRaise("kova.string.bigInteger", message) { it.toBigIntegerOrNull() }

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
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver String The string to validate and convert
 * @param message Custom error message provider
 * @return The converted Boolean value
 */
context(_: Validation)
public fun String.transformToBoolean(
    message: MessageProvider = { "kova.string.boolean".resource },
): Boolean = transformOrRaise("kova.string.boolean", message) { it.toBooleanStrictOrNull() }

/**
 * Validates that the string is a valid enum name and converts it to the enum value (reified version).
 *
 * This is a type-transforming validator that outputs the enum type.
 *
 * Example:
 * ```kotlin
 * enum class Role { ADMIN, USER, GUEST }
 * tryValidate { "ADMIN".transformToEnum<Role>() } // Success: Role.ADMIN
 * tryValidate { "OTHER".transformToEnum<Role>() } // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver String The string to validate and convert
 * @param E The enum type
 * @param message Custom error message provider that receives the list of valid enum names
 * @return The converted enum value
 */
context(_: Validation)
public inline fun <reified E : Enum<E>> String.transformToEnum(
    noinline message: (validNames: List<String>) -> Message = { "kova.string.enum".resource(it) },
): E = transformToEnum(E::class, message)

/**
 * Validates that the string is a valid enum name and converts it to the enum value.
 *
 * This is a type-transforming validator that outputs the enum type.
 *
 * Example:
 * ```kotlin
 * enum class Role { ADMIN, USER, GUEST }
 * tryValidate { "ADMIN".transformToEnum(Role::class) } // Success: Role.ADMIN
 * tryValidate { "OTHER".transformToEnum(Role::class) } // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver String The string to validate and convert
 * @param E The enum type
 * @param klass The enum class to convert to
 * @param message Custom error message provider that receives the list of valid enum names
 * @return The converted enum value
 */
context(_: Validation)
public fun <E : Enum<E>> String.transformToEnum(
    klass: KClass<E>,
    message: (validNames: List<String>) -> Message = { "kova.string.enum".resource(it) },
): E = transformOrRaise("kova.string.enum", {
    message(klass.java.enumConstants.map { enum -> enum.name })
}) { it.toEnumOrNull(klass) }

private fun <E : Enum<E>> String.toEnumOrNull(klass: KClass<E>): E? =
    runCatching { java.lang.Enum.valueOf(klass.java, this) }.getOrNull()

/**
 * Validates that the string can be parsed as a LocalDate and converts it.
 *
 * This is a type-transforming validator that outputs LocalDate.
 *
 * Example:
 * ```kotlin
 * tryValidate { "2025-01-17".transformToDate() }                                    // Success (ISO format)
 * tryValidate { "17/01/2025".transformToDate(DateTimeFormatter.ofPattern("dd/MM/yyyy")) } // Success (custom format)
 * tryValidate { "invalid".transformToDate() }                                        // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver String The string to validate and convert
 * @param formatter The DateTimeFormatter to use for parsing (defaults to ISO_LOCAL_DATE)
 * @param message Custom error message provider
 * @return The converted LocalDate value
 */
context(_: Validation)
public fun String.transformToDate(
    formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE,
    message: MessageProvider = { "kova.string.localDate".resource },
): LocalDate = transformOrRaise("kova.string.localDate", message) {
    runCatching { LocalDate.parse(it, formatter) }.getOrNull()
}

/**
 * Validates that the string can be parsed as a LocalDateTime and converts it.
 *
 * This is a type-transforming validator that outputs LocalDateTime.
 *
 * Example:
 * ```kotlin
 * tryValidate { "2025-01-17T10:30:00".transformToDateTime() }                                    // Success (ISO format)
 * tryValidate { "17/01/2025 10:30".transformToDateTime(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) } // Success (custom format)
 * tryValidate { "invalid".transformToDateTime() }                                                // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver String The string to validate and convert
 * @param formatter The DateTimeFormatter to use for parsing (defaults to ISO_LOCAL_DATE_TIME)
 * @param message Custom error message provider
 * @return The converted LocalDateTime value
 */
context(_: Validation)
public fun String.transformToDateTime(
    formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME,
    message: MessageProvider = { "kova.string.localDateTime".resource },
): LocalDateTime = transformOrRaise("kova.string.localDateTime", message) {
    runCatching { LocalDateTime.parse(it, formatter) }.getOrNull()
}

/**
 * Validates that the string can be parsed as a LocalTime and converts it.
 *
 * This is a type-transforming validator that outputs LocalTime.
 *
 * Example:
 * ```kotlin
 * tryValidate { "10:30:00".transformToTime() }                                    // Success (ISO format)
 * tryValidate { "10:30".transformToTime(DateTimeFormatter.ofPattern("HH:mm")) }   // Success (custom format)
 * tryValidate { "invalid".transformToTime() }                                     // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver String The string to validate and convert
 * @param formatter The DateTimeFormatter to use for parsing (defaults to ISO_LOCAL_TIME)
 * @param message Custom error message provider
 * @return The converted LocalTime value
 */
context(_: Validation)
public fun String.transformToTime(
    formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_TIME,
    message: MessageProvider = { "kova.string.localTime".resource },
): LocalTime = transformOrRaise("kova.string.localTime", message) {
    runCatching { LocalTime.parse(it, formatter) }.getOrNull()
}
