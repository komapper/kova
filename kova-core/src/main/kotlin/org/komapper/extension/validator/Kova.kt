package org.komapper.extension.validator

import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.MonthDay
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.Year
import java.time.YearMonth
import java.time.ZonedDateTime
import java.time.temporal.Temporal

/**
 * Main entry point for creating validators in Kova.
 *
 * This interface provides factory methods for creating type-safe validators
 * for common types and collections. All validators are immutable and composable.
 *
 * Example usage:
 * ```kotlin
 * // String validation
 * val result = Kova.string().min(1).max(10).tryValidate("hello")
 *
 * // Numeric validation
 * val ageValidator = Kova.int().min(0).max(120)
 *
 * // Temporal validation with system clock (default)
 * val dateValidator = Kova.localDate().past().min(LocalDate.of(2020, 1, 1))
 * dateValidator.validate(LocalDate.of(2024, 6, 15))
 *
 * // Temporal validation with custom clock for testing
 * val fixedClock = Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC)
 * val config = ValidationConfig(clock = fixedClock)
 * dateValidator.tryValidate(LocalDate.of(2024, 12, 31), config = config)
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
     * Creates a validator for LocalDate values with temporal constraints.
     *
     * Temporal constraints (past, future, pastOrPresent, futureOrPresent) use the clock
     * from [ValidationConfig]. By default, [java.time.Clock.systemDefaultZone] is used.
     * For testing, pass a custom clock via the config parameter in validation methods.
     *
     * Example:
     * ```kotlin
     * // Using default system clock
     * val validator = Kova.localDate().past()
     * validator.validate(LocalDate.of(2024, 1, 1))
     *
     * // Using custom clock for testing
     * val fixedClock = Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC)
     * val config = ValidationConfig(clock = fixedClock)
     * validator.tryValidate(LocalDate.of(2024, 12, 31), config = config)
     * ```
     */
    fun localDate(): TemporalValidator<LocalDate> = generic()

    /**
     * Creates a validator for LocalTime values with temporal constraints.
     *
     * Temporal constraints use the clock from [ValidationConfig].
     * See [localDate] for clock configuration details.
     */
    fun localTime(): TemporalValidator<LocalTime> = generic()

    /**
     * Creates a validator for LocalDateTime values with temporal constraints.
     *
     * Temporal constraints use the clock from [ValidationConfig].
     * See [localDate] for clock configuration details.
     */
    fun localDateTime(): TemporalValidator<LocalDateTime> = generic()

    /**
     * Creates a validator for Instant values with temporal constraints.
     *
     * Temporal constraints use the clock from [ValidationConfig].
     * See [localDate] for clock configuration details.
     */
    fun instant(): TemporalValidator<Instant> = generic()

    /**
     * Creates a validator for MonthDay values.
     *
     * Note: MonthDay does not implement [Temporal], so it does not support temporal-specific
     * constraints (past, future, pastOrPresent, futureOrPresent). However, it does implement
     * [Comparable], so comparison constraints (min, max, gt, gte, lt, lte) are available.
     */
    fun monthDay(): IdentityValidator<MonthDay> = generic()

    /**
     * Creates a validator for OffsetDateTime values with temporal constraints.
     *
     * Temporal constraints use the clock from [ValidationConfig].
     * See [localDate] for clock configuration details.
     */
    fun offsetDateTime(): TemporalValidator<OffsetDateTime> = generic()

    /**
     * Creates a validator for OffsetTime values with temporal constraints.
     *
     * Temporal constraints use the clock from [ValidationConfig].
     * See [localDate] for clock configuration details.
     */
    fun offsetTime(): TemporalValidator<OffsetTime> = generic()

    /**
     * Creates a validator for Year values with temporal constraints.
     *
     * Temporal constraints use the clock from [ValidationConfig].
     * See [localDate] for clock configuration details.
     */
    fun year(): TemporalValidator<Year> = generic()

    /**
     * Creates a validator for YearMonth values with temporal constraints.
     *
     * Temporal constraints use the clock from [ValidationConfig].
     * See [localDate] for clock configuration details.
     */
    fun yearMonth(): TemporalValidator<YearMonth> = generic()

    /**
     * Creates a validator for ZonedDateTime values with temporal constraints.
     *
     * Temporal constraints use the clock from [ValidationConfig].
     * See [localDate] for clock configuration details.
     */
    fun zonedDateTime(): TemporalValidator<ZonedDateTime> = generic()

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
    fun <T : Any> nullable(defaultValue: T): NullCoalescingValidator<T, T> = nullable { defaultValue }

    /**
     * Creates a nullable validator with a lazy-evaluated default value for null inputs.
     *
     * @param withDefault A function that provides the default value when the input is null
     * @return A validator that replaces null with the result of withDefault()
     */
    fun <T : Any> nullable(withDefault: () -> T): NullCoalescingValidator<T, T> = generic<T>().asNullable(withDefault)

    /**
     * Creates a validator that only accepts a specific literal value.
     *
     * @param value The exact value that will be accepted
     * @param message Custom error message provider
     * @return A validator that only succeeds for the specified value
     */
    fun <T : Any> literal(
        value: T,
        message: MessageProvider? = null,
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
        message: MessageProvider? = null,
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
        message: MessageProvider? = null,
    ): IdentityValidator<T> = literal(values.toList(), message)

    /**
     * Fails validation with a text message.
     *
     * This is useful for custom validation logic that needs to fail with a specific message.
     *
     * @param content The failure message text
     * @throws MessageException Always throws with the provided message
     */
    fun fail(message: String): Nothing = throw MessageException(message)

    companion object : Kova
}
