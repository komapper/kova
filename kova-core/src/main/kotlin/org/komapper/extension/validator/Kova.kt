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
    fun boolean(): IdentityValidator<Boolean> = generic()

    /** Creates a validator for String values with string-specific constraints. */
    fun string(): IdentityValidator<String> = generic()

    /** Creates a validator for Int values with numeric constraints. */
    fun int(): IdentityValidator<Int> = generic()

    /** Creates a validator for Long values with numeric constraints. */
    fun long(): IdentityValidator<Long> = generic()

    /** Creates a validator for Double values with numeric constraints. */
    fun double(): IdentityValidator<Double> = generic()

    /** Creates a validator for Float values with numeric constraints. */
    fun float(): IdentityValidator<Float> = generic()

    /** Creates a validator for Byte values with numeric constraints. */
    fun byte(): IdentityValidator<Byte> = generic()

    /** Creates a validator for Short values with numeric constraints. */
    fun short(): IdentityValidator<Short> = generic()

    /** Creates a validator for BigDecimal values with numeric constraints. */
    fun bigDecimal(): IdentityValidator<BigDecimal> = generic()

    /** Creates a validator for BigInteger values with numeric constraints. */
    fun bigInteger(): IdentityValidator<BigInteger> = generic()

    /** Creates a validator for UInt values with unsigned integer constraints. */
    fun uInt(): IdentityValidator<UInt> = generic()

    /** Creates a validator for ULong values with unsigned integer constraints. */
    fun uLong(): IdentityValidator<ULong> = generic()

    /** Creates a validator for UByte values with unsigned integer constraints. */
    fun uByte(): IdentityValidator<UByte> = generic()

    /** Creates a validator for UShort values with unsigned integer constraints. */
    fun uShort(): IdentityValidator<UShort> = generic()

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
    fun <E> collection(): IdentityValidator<Collection<E>> = generic()

    /** Creates a validator for List values with size and element validation. */
    fun <E> list(): IdentityValidator<List<E>> = generic()

    /** Creates a validator for Set values with size and element validation. */
    fun <E> set(): IdentityValidator<Set<E>> = generic()

    /** Creates a validator for Map values with size, key, and value validation. */
    fun <K, V> map(): IdentityValidator<Map<K, V>> = generic()

    /** Creates a validator for Map.Entry values with key and value validation. */
    fun <K, V> mapEntry(): IdentityValidator<Map.Entry<K, V>> = generic()

    /**
     * Creates a generic validator that accepts any value of type T.
     *
     * This is useful as a starting point for custom validators or when no specific validation is needed.
     */
    fun <T> generic(): IdentityValidator<T> = Validator.success()

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
    fun <T : Any> nullable(): NullableValidator<T, T> = generic()

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
        generic<T>().asNullable(withDefault)

    /**
     * Creates a validator that only accepts a specific literal value.
     *
     * @param value The exact value that will be accepted
     * @param message Custom error message provider
     * @return A validator that only succeeds for the specified value
     */
    fun <T : Any> literal(
        value: T,
        message: MessageProvider1<T, T>? = null,
    ): IdentityValidator<T> = if (message == null) generic<T>().literal(value) else generic<T>().literal(value, message)

    /**
     * Creates a validator that only accepts values from a specified list.
     *
     * @param values The list of acceptable values
     * @param message Custom error message provider
     * @return A validator that only succeeds for values in the list
     */
    fun <T : Any> literal(
        values: List<T>,
        message: MessageProvider1<T, List<T>>? = null,
    ): IdentityValidator<T> = if (message == null) generic<T>().literal(values) else generic<T>().literal(values, message)

    /**
     * Creates a validator that only accepts values from a specified vararg list.
     *
     * @param values The acceptable values
     * @param message Custom error message provider
     * @return A validator that only succeeds for values in the list
     */
    fun <T : Any> literal(
        vararg values: T,
        message: MessageProvider1<T, List<T>>? = null,
    ): IdentityValidator<T> = literal(values.toList(), message)

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
