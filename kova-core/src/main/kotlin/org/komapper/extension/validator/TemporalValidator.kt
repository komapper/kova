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
 * @param S The temporal type (LocalDate, LocalTime, LocalDateTime, Instant, etc.)
 * @param message Custom error message provider
 * @return A new validator with the future constraint
 */
context(_: ValidationContext)
inline fun <reified S> S.future(
    noinline message: MessageProvider = { "kova.temporal.future".resource },
) where S : Temporal, S : Comparable<S> = constrain("kova.temporal.future") { satisfies(it > now(clock), message) }

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
 * @param S The temporal type (LocalDate, LocalTime, LocalDateTime, Instant, etc.)
 * @param message Custom error message provider
 * @return A new validator with the future-or-present constraint
 */
context(_: ValidationContext)
inline fun <reified S> S.futureOrPresent(
    noinline message: MessageProvider = { "kova.temporal.futureOrPresent".resource },
)where S : Temporal, S : Comparable<S> = constrain("kova.temporal.futureOrPresent") { satisfies(it >= now(clock), message) }

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
 * @param S The temporal type (LocalDate, LocalTime, LocalDateTime, Instant, etc.)
 * @param message Custom error message provider
 * @return A new validator with the past constraint
 */
context(_: ValidationContext)
inline fun <reified S> S.past(noinline message: MessageProvider = { "kova.temporal.past".resource })where S : Temporal, S : Comparable<S> =
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
 * @param S The temporal type (LocalDate, LocalTime, LocalDateTime, Instant, etc.)
 * @param message Custom error message provider
 * @return A new validator with the past-or-present constraint
 */
context(_: ValidationContext)
inline fun <reified S> S.pastOrPresent(
    noinline message: MessageProvider = { "kova.temporal.pastOrPresent".resource },
)where S : Temporal, S : Comparable<S> = constrain("kova.temporal.pastOrPresent") { satisfies(it <= now(clock), message) }

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
