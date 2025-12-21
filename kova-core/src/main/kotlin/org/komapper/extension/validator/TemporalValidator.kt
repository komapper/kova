package org.komapper.extension.validator

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.Year
import java.time.YearMonth
import java.time.ZonedDateTime
import java.time.temporal.Temporal
import kotlin.reflect.KClass

/**
 * Type alias for temporal validators.
 *
 * Temporal validators work with types that implement both [Temporal] and [Comparable],
 * such as LocalDate, LocalTime, LocalDateTime, Instant, etc.
 *
 * The clock used for temporal comparisons (past, future, etc.) is obtained from
 * [ValidationConfig.clock] at validation time, allowing for easy testing with fixed clocks.
 *
 * Example:
 * ```kotlin
 * // Using system clock (default)
 * val validator = Kova.localDate().past().min(LocalDate.of(2020, 1, 1))
 * validator.validate(LocalDate.of(2024, 6, 15))
 *
 * // Using fixed clock for testing
 * val fixedClock = Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC)
 * val config = ValidationConfig(clock = fixedClock)
 * validator.tryValidate(LocalDate.of(2024, 12, 31), config = config)
 * ```
 */
typealias TemporalValidator<T> = IdentityValidator<T>

/**
 * Validates that the temporal value is in the future (strictly greater than now).
 *
 * The current time is determined by the clock in [ValidationConfig] at validation time.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.localDate().future()
 * // Assuming today is 2025-01-15
 * validator.validate(LocalDate.of(2025, 1, 16))  // Success
 * validator.validate(LocalDate.of(2025, 1, 15))  // Failure (present)
 * validator.validate(LocalDate.of(2025, 1, 14))  // Failure (past)
 * ```
 *
 * @param T The temporal type (LocalDate, LocalTime, LocalDateTime, Instant, etc.)
 * @param message Custom error message provider
 * @return A new validator with the future constraint
 */
inline fun <reified T> TemporalValidator<T>.future(
    noinline message: MessageProvider = { "kova.temporal.future".resource },
): TemporalValidator<T>
        where T : Temporal, T : Comparable<T> =
    constrain("kova.temporal.future") { satisfies(it > now(clock), message) }

/**
 * Validates that the temporal value is in the future or present (greater than or equal to now).
 *
 * The current time is determined by the clock in [ValidationConfig] at validation time.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.localDate().futureOrPresent()
 * // Assuming today is 2025-01-15
 * validator.validate(LocalDate.of(2025, 1, 16))  // Success (future)
 * validator.validate(LocalDate.of(2025, 1, 15))  // Success (present)
 * validator.validate(LocalDate.of(2025, 1, 14))  // Failure (past)
 * ```
 *
 * @param T The temporal type (LocalDate, LocalTime, LocalDateTime, Instant, etc.)
 * @param message Custom error message provider
 * @return A new validator with the future-or-present constraint
 */
inline fun <reified T> TemporalValidator<T>.futureOrPresent(
    noinline message: MessageProvider = { "kova.temporal.futureOrPresent".resource },
): TemporalValidator<T>
        where T : Temporal, T : Comparable<T> =
    constrain("kova.temporal.futureOrPresent") { satisfies(it >= now(clock), message) }

/**
 * Validates that the temporal value is in the past (strictly less than now).
 *
 * The current time is determined by the clock in [ValidationConfig] at validation time.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.localDate().past()
 * // Assuming today is 2025-01-15
 * validator.validate(LocalDate.of(2025, 1, 14))  // Success
 * validator.validate(LocalDate.of(2025, 1, 15))  // Failure (present)
 * validator.validate(LocalDate.of(2025, 1, 16))  // Failure (future)
 * ```
 *
 * @param T The temporal type (LocalDate, LocalTime, LocalDateTime, Instant, etc.)
 * @param message Custom error message provider
 * @return A new validator with the past constraint
 */
inline fun <reified T> TemporalValidator<T>.past(
    noinline message: MessageProvider = { "kova.temporal.past".resource },
): TemporalValidator<T>
        where T : Temporal, T : Comparable<T> =
    constrain("kova.temporal.past") { satisfies(it < now(clock), message) }

/**
 * Validates that the temporal value is in the past or present (less than or equal to now).
 *
 * The current time is determined by the clock in [ValidationConfig] at validation time.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.localDate().pastOrPresent()
 * // Assuming today is 2025-01-15
 * validator.validate(LocalDate.of(2025, 1, 14))  // Success (past)
 * validator.validate(LocalDate.of(2025, 1, 15))  // Success (present)
 * validator.validate(LocalDate.of(2025, 1, 16))  // Failure (future)
 * ```
 *
 * @param T The temporal type (LocalDate, LocalTime, LocalDateTime, Instant, etc.)
 * @param message Custom error message provider
 * @return A new validator with the past-or-present constraint
 */
inline fun <reified T> TemporalValidator<T>.pastOrPresent(
    noinline message: MessageProvider = { "kova.temporal.pastOrPresent".resource },
): TemporalValidator<T>
        where T : Temporal, T : Comparable<T> =
    constrain("kova.temporal.pastOrPresent") { satisfies(it <= now(clock), message) }

/**
 * Obtains the current temporal value for the specified type using the provided clock.
 *
 * This internal function maps from a [KClass] to the appropriate static `now(Clock)` method
 * for each supported temporal type. It is used by the temporal constraint extension functions
 * (future, past, etc.) to obtain the current time at validation time.
 *
 * The function uses reified type parameters at the call site to ensure type safety, and
 * the unchecked cast here is safe because the when expression guarantees that the returned
 * value's type matches the requested [KClass].
 *
 * @param T The temporal type to obtain the current value for
 * @param clock The clock to use for determining the current time
 * @return The current temporal value
 * @throws IllegalStateException if the temporal type is not supported
 */
@PublishedApi
internal inline fun <reified T : Temporal> now(clock: Clock): T {
    val now =
        when (T::class) {
            Instant::class -> Instant.now(clock)
            LocalDate::class -> LocalDate.now(clock)
            LocalDateTime::class -> LocalDateTime.now(clock)
            LocalTime::class -> LocalTime.now(clock)
            OffsetDateTime::class -> OffsetDateTime.now(clock)
            OffsetTime::class -> OffsetTime.now(clock)
            Year::class -> Year.now(clock)
            YearMonth::class -> YearMonth.now(clock)
            ZonedDateTime::class -> ZonedDateTime.now(clock)
            else -> error("Unsupported temporal type: ${T::class}")
        }
    @Suppress("UNCHECKED_CAST")
    return now as T
}
