package org.komapper.extension.validator

import java.math.BigDecimal
import java.math.BigInteger
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Main entry point for creating validators in Kova.
 *
 * This interface provides factory methods for creating type-safe validators
 * for common types and collections. All validators are immutable and composable.
 *
 * Example usage:
 * ```kotlin
 * // Simple validation
 * val result = Kova.string().min(1).max(10).tryValidate("hello")
 *
 * // Numeric validation
 * val ageValidator = Kova.int().min(0).max(120)
 *
 * // Collection validation
 * val listValidator = Kova.list<String>().min(1).onEach(Kova.string().min(1))
 * ```
 *
 * Access this interface through the companion object:
 * ```kotlin
 * import org.komapper.extension.validator.Kova
 *
 * Kova.string() // Use directly
 * ```
 */
interface Kova {
    /** Creates a validator for Boolean values. */
    fun boolean(): Validator<Boolean, Boolean> = generic()

    /** Creates a validator for String values with string-specific constraints. */
    fun string(): StringValidator = StringValidator()

    /** Creates a validator for Int values with numeric constraints. */
    fun int(): NumberValidator<Int> = NumberValidator()

    /** Creates a validator for Long values with numeric constraints. */
    fun long(): NumberValidator<Long> = NumberValidator()

    /** Creates a validator for Double values with numeric constraints. */
    fun double(): NumberValidator<Double> = NumberValidator()

    /** Creates a validator for Float values with numeric constraints. */
    fun float(): NumberValidator<Float> = NumberValidator()

    /** Creates a validator for Byte values with numeric constraints. */
    fun byte(): NumberValidator<Byte> = NumberValidator()

    /** Creates a validator for Short values with numeric constraints. */
    fun short(): NumberValidator<Short> = NumberValidator()

    /** Creates a validator for BigDecimal values with numeric constraints. */
    fun bigDecimal(): NumberValidator<BigDecimal> = NumberValidator()

    /** Creates a validator for BigInteger values with numeric constraints. */
    fun bigInteger(): NumberValidator<BigInteger> = NumberValidator()

    /** Creates a validator for UInt values with unsigned integer constraints. */
    fun uInt(): UIntValidator = UIntValidator()

    /** Creates a validator for ULong values with unsigned integer constraints. */
    fun uLong(): ULongValidator = ULongValidator()

    /** Creates a validator for UByte values with unsigned integer constraints. */
    fun uByte(): UByteValidator = UByteValidator()

    /** Creates a validator for UShort values with unsigned integer constraints. */
    fun uShort(): UShortValidator = UShortValidator()

    /**
     * Creates a validator for temporal values with temporal constraints.
     *
     * This generic method supports LocalDate, LocalTime, LocalDateTime, and any other
     * type that implements both Temporal and Comparable.
     *
     * @param T The temporal type to validate
     * @param clock The clock used for temporal comparisons like past() and future(). Defaults to system default zone.
     * @param temporalNow Strategy for obtaining the current temporal value
     * @return A temporal validator for type T
     *
     * Example:
     * ```kotlin
     * val dateValidator = Kova.temporal<LocalDate>(temporalNow = LocalDateNow)
     * val timeValidator = Kova.temporal<LocalTime>(temporalNow = LocalTimeNow)
     * ```
     */
    fun <T> temporal(
        clock: Clock = Clock.systemDefaultZone(),
        temporalNow: TemporalNow<T>,
    ): TemporalValidator<T> where T : java.time.temporal.Temporal, T : Comparable<T> =
        TemporalValidator(clock = clock, temporalNow = temporalNow)

    /**
     * Creates a validator for LocalDate values with temporal constraints.
     *
     * @param clock The clock used for temporal comparisons like past() and future(). Defaults to system default zone.
     */
    fun localDate(clock: Clock = Clock.systemDefaultZone()): TemporalValidator<LocalDate> =
        temporal(clock = clock, temporalNow = LocalDateNow)

    /**
     * Creates a validator for LocalTime values with temporal constraints.
     *
     * @param clock The clock used for temporal comparisons like past() and future(). Defaults to system default zone.
     */
    fun localTime(clock: Clock = Clock.systemDefaultZone()): TemporalValidator<LocalTime> =
        temporal(clock = clock, temporalNow = LocalTimeNow)

    /**
     * Creates a validator for LocalDateTime values with temporal constraints.
     *
     * @param clock The clock used for temporal comparisons like past() and future(). Defaults to system default zone.
     */
    fun localDateTime(clock: Clock = Clock.systemDefaultZone()): TemporalValidator<LocalDateTime> =
        temporal(clock = clock, temporalNow = LocalDateTimeNow)

    /** Creates a validator for Collection values with size and element validation. */
    fun <E> collection(): CollectionValidator<E, Collection<E>> = CollectionValidator()

    /** Creates a validator for List values with size and element validation. */
    fun <E> list(): CollectionValidator<E, List<E>> = CollectionValidator()

    /** Creates a validator for Set values with size and element validation. */
    fun <E> set(): CollectionValidator<E, Set<E>> = CollectionValidator()

    /** Creates a validator for Map values with size, key, and value validation. */
    fun <K, V> map(): MapValidator<K, V> = MapValidator()

    /** Creates a validator for Map.Entry values with key and value validation. */
    fun <K, V> mapEntry(): MapEntryValidator<K, V> = MapEntryValidator()

    /**
     * Creates a generic validator that accepts any value of type T.
     *
     * This is useful as a starting point for custom validators or when no specific validation is needed.
     */
    fun <T> generic(): Validator<T, T> = EmptyValidator()

    /**
     * Creates a nullable validator that accepts null values.
     *
     * @return A validator that accepts null and any non-null value of type T
     *
     * Example:
     * ```kotlin
     * val validator = Kova.nullable<String>()
     * validator.tryValidate(null) // Success
     * validator.tryValidate("hello") // Success
     * ```
     */
    fun <T : Any> nullable(): NullableValidator<T, T> = NullableValidator("nullable", generic())

    /**
     * Creates a nullable validator with a default value for null inputs.
     *
     * @param defaultValue The value to use when the input is null
     * @return A validator that replaces null with the default value
     *
     * Example:
     * ```kotlin
     * val validator = Kova.nullable("default")
     * validator.validate(null) // Returns "default"
     * validator.validate("hello") // Returns "hello"
     * ```
     */
    fun <T : Any> nullable(defaultValue: T): WithDefaultNullableValidator<T, T> = nullable { defaultValue }

    /**
     * Creates a nullable validator with a lazy-evaluated default value for null inputs.
     *
     * @param withDefault A function that provides the default value when the input is null
     * @return A validator that replaces null with the result of withDefault()
     */
    fun <T : Any> nullable(withDefault: () -> T): WithDefaultNullableValidator<T, T> =
        WithDefaultNullableValidator("nullable", generic<T>().asNullable(withDefault))

    /**
     * Creates a validator that only accepts a specific literal value.
     *
     * @param value The exact value that will be accepted
     * @param message Custom error message provider
     * @return A validator that only succeeds for the specified value
     */
    fun <T : Any> literal(
        value: T,
        message: MessageProvider1<T, T> = Message.resource1("kova.literal.single"),
    ): Validator<T, T> = LiteralValidator<T>().single(value, message)

    /**
     * Creates a validator that only accepts values from a specified list.
     *
     * @param values The list of acceptable values
     * @param message Custom error message provider
     * @return A validator that only succeeds for values in the list
     */
    fun <T : Any> literal(
        values: List<T>,
        message: MessageProvider1<T, List<T>> = Message.resource1("kova.literal.list"),
    ): Validator<T, T> = LiteralValidator<T>().list(values.toList(), message)

    /**
     * Creates a validator that only accepts values from a specified vararg list.
     *
     * @param values The acceptable values
     * @param message Custom error message provider
     * @return A validator that only succeeds for values in the list
     */
    fun <T : Any> literal(
        vararg values: T,
        message: MessageProvider1<T, List<T>> = Message.resource1("kova.literal.list"),
    ): Validator<T, T> = literal(values.toList(), message)

    /**
     * Fails validation with a text message.
     *
     * This is useful for custom validation logic that needs to fail with a specific message.
     *
     * @param content The failure message text
     * @throws MessageException Always throws with the provided message
     */
    fun fail(content: String): Nothing = fail(Message.Text(content))

    /**
     * Fails validation with a structured message.
     *
     * @param message The failure message
     * @throws MessageException Always throws with the provided message
     */
    fun fail(message: Message): Nothing = throw MessageException(message)

    companion object : Kova
}
