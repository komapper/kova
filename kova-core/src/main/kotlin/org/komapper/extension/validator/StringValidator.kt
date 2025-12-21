package org.komapper.extension.validator

import kotlin.reflect.KClass

/**
 * Type alias for string validators.
 *
 * Provides a convenient type for validators that work with String inputs and outputs.
 */
typealias StringValidator<T> = Validator<T, String>

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
fun <T> StringValidator<T>.isInt(message: MessageProvider = { "kova.string.isInt".resource }) =
    constrain("kova.string.isInt", Kova.string().toInt(message))

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
fun <T> StringValidator<T>.isLong(message: MessageProvider = { "kova.string.isLong".resource }) =
    constrain("kova.string.isLong", Kova.string().toLong(message))

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
fun <T> StringValidator<T>.isShort(message: MessageProvider = { "kova.string.isShort".resource }) =
    constrain("kova.string.isShort", Kova.string().toShort(message))

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
fun <T> StringValidator<T>.isByte(message: MessageProvider = { "kova.string.isByte".resource }) =
    constrain("kova.string.isByte", Kova.string().toByte(message))

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
fun <T> StringValidator<T>.isDouble(message: MessageProvider = { "kova.string.isDouble".resource }) =
    constrain("kova.string.isDouble", Kova.string().toDouble(message))

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
fun <T> StringValidator<T>.isFloat(message: MessageProvider = { "kova.string.isFloat".resource }) =
    constrain("kova.string.isFloat", Kova.string().toFloat(message))

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
fun <T> StringValidator<T>.isBigDecimal(message: MessageProvider = { "kova.string.isBigDecimal".resource }) =
    constrain("kova.string.isBigDecimal", Kova.string().toBigDecimal(message))

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
fun <T> StringValidator<T>.isBigInteger(message: MessageProvider = { "kova.string.isBigInteger".resource }) =
    constrain("kova.string.isBigInteger", Kova.string().toBigInteger(message))

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
fun <T> StringValidator<T>.isBoolean(message: MessageProvider = { "kova.string.isBoolean".resource }) =
    constrain("kova.string.isBoolean", Kova.string().toBoolean(message))

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
fun <E : Enum<E>, T> StringValidator<T>.isEnum(
    klass: KClass<E>,
    message: ValidationContext.(validNames: List<String>) -> Message = { "kova.string.isEnum".resource(it) },
) = constrain("kova.string.isEnum", Kova.string().toEnum(klass, message))

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
inline fun <reified E : Enum<E>, T> StringValidator<T>.isEnum(
    noinline message: ValidationContext.(validNames: List<String>) -> Message = { "kova.string.isEnum".resource(it) },
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
inline fun <reified E : Enum<E>, T> StringValidator<T>.toEnum(
    noinline message: ValidationContext.(validNames: List<String>) -> Message = { "kova.string.isEnum".resource(it) },
) = toEnum(E::class, message)

fun <E : Enum<E>, T> StringValidator<T>.toEnum(
    klass: KClass<E>,
    message: ValidationContext.(validNames: List<String>) -> Message = { "kova.string.isEnum".resource(it) },
): Validator<T, E> =
    then {
        runCatching { java.lang.Enum.valueOf(klass.java, it) }.getOrNull().satisfiesNotNull {
            message(klass.java.enumConstants.map { enum -> enum.name })
        }
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
fun <T> StringValidator<T>.uppercase(message: MessageProvider = { "kova.string.uppercase".resource }) =
    constrain("kova.string.uppercase") { satisfies(it == it.uppercase(), message) }

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
fun <T> StringValidator<T>.lowercase(message: MessageProvider = { "kova.string.lowercase".resource }) =
    constrain("kova.string.lowercase") { satisfies(it == it.lowercase(), message) }

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
fun <T> StringValidator<T>.trim() = map { it.trim() }

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
fun <T> StringValidator<T>.toUppercase() = map { it.uppercase() }

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
fun <T> StringValidator<T>.toLowercase() = map { it.lowercase() }

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
fun <T> StringValidator<T>.toInt(message: MessageProvider = { "kova.string.isInt".resource }) =
    then { it.toIntOrNull().satisfiesNotNull(message) }

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
fun <T> StringValidator<T>.toLong(message: MessageProvider = { "kova.string.isLong".resource }) =
    then { it.toLongOrNull().satisfiesNotNull(message) }

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
fun <T> StringValidator<T>.toShort(message: MessageProvider = { "kova.string.isShort".resource }) =
    then { it.toShortOrNull().satisfiesNotNull(message) }

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
fun <T> StringValidator<T>.toByte(message: MessageProvider = { "kova.string.isByte".resource }) =
    then { it.toByteOrNull().satisfiesNotNull(message) }

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
fun <T> StringValidator<T>.toDouble(message: MessageProvider = { "kova.string.isDouble".resource }) =
    then { it.toDoubleOrNull().satisfiesNotNull(message) }

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
fun <T> StringValidator<T>.toFloat(message: MessageProvider = { "kova.string.isFloat".resource }) =
    then { it.toFloatOrNull().satisfiesNotNull(message) }

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
fun <T> StringValidator<T>.toBigDecimal(message: MessageProvider = { "kova.string.isBigDecimal".resource }) =
    then { it.toBigDecimalOrNull().satisfiesNotNull(message) }

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
fun <T> StringValidator<T>.toBigInteger(message: MessageProvider = { "kova.string.isBigInteger".resource }) =
    then { it.toBigIntegerOrNull().satisfiesNotNull(message) }

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
fun <T> StringValidator<T>.toBoolean(message: MessageProvider = { "kova.string.isBoolean".resource }) =
    then { it.toBooleanStrictOrNull().satisfiesNotNull(message) }
