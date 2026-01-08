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
 * Validates that the temporal value is in the ensureFuture (strictly greater than now).
 *
 * The current time is determined by the clock in [ValidationConfig] at validation time.
 *
 * Example:
 * ```kotlin
 * // Assuming today is 2025-01-15
 * tryValidate { ensureFuture(LocalDate.of(2025, 1, 16)) }  // Success
 * tryValidate { ensureFuture(LocalDate.of(2025, 1, 15)) }  // Failure (present)
 * tryValidate { ensureFuture(LocalDate.of(2025, 1, 14)) }  // Failure (ensurePast)
 * ```
 *
 * @param S The temporal type (LocalDate, LocalTime, LocalDateTime, Instant, etc.)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
inline fun <reified S> Validation.ensureFuture(
    input: S,
    noinline message: MessageProvider = { "kova.temporal.future".resource },
) where S : Temporal, S : Comparable<S> = input.constrain("kova.temporal.future") { satisfies(it > now(clock), message) }

/**
 * Validates that the temporal value is in the ensureFuture or present (greater than or equal to now).
 *
 * The current time is determined by the clock in [ValidationConfig] at validation time.
 *
 * Example:
 * ```kotlin
 * // Assuming today is 2025-01-15
 * tryValidate { ensureFutureOrPresent(LocalDate.of(2025, 1, 16)) }  // Success (ensureFuture)
 * tryValidate { ensureFutureOrPresent(LocalDate.of(2025, 1, 15)) }  // Success (present)
 * tryValidate { ensureFutureOrPresent(LocalDate.of(2025, 1, 14)) }  // Failure (ensurePast)
 * ```
 *
 * @param S The temporal type (LocalDate, LocalTime, LocalDateTime, Instant, etc.)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
inline fun <reified S> Validation.ensureFutureOrPresent(
    input: S,
    noinline message: MessageProvider = { "kova.temporal.futureOrPresent".resource },
) where S : Temporal, S : Comparable<S> = input.constrain("kova.temporal.futureOrPresent") { satisfies(it >= now(clock), message) }

/**
 * Validates that the temporal value is in the ensurePast (strictly less than now).
 *
 * The current time is determined by the clock in [ValidationConfig] at validation time.
 *
 * Example:
 * ```kotlin
 * // Assuming today is 2025-01-15
 * tryValidate { ensurePast(LocalDate.of(2025, 1, 14)) }  // Success
 * tryValidate { ensurePast(LocalDate.of(2025, 1, 15)) }  // Failure (present)
 * tryValidate { ensurePast(LocalDate.of(2025, 1, 16)) }  // Failure (ensureFuture)
 * ```
 *
 * @param S The temporal type (LocalDate, LocalTime, LocalDateTime, Instant, etc.)
 * @param message Custom error message provider
 */
@IgnorableReturnValue
inline fun <reified S> Validation.ensurePast(
    input: S,
    noinline message: MessageProvider = { "kova.temporal.past".resource },
)where S : Temporal, S : Comparable<S> = input.constrain("kova.temporal.past") { satisfies(it < now(clock), message) }

/**
 * Validates that the temporal value is in the ensurePast or present (less than or equal to now).
 *
 * The current time is determined by the clock in [ValidationConfig] at validation time.
 *
 * Example:
 * ```kotlin
 * // Assuming today is 2025-01-15
 * tryValidate { ensurePastOrPresent(LocalDate.of(2025, 1, 14)) }  // Success (ensurePast)
 * tryValidate { ensurePastOrPresent(LocalDate.of(2025, 1, 15)) }  // Success (present)
 * tryValidate { ensurePastOrPresent(LocalDate.of(2025, 1, 16)) }  // Failure (ensureFuture)
 * ```
 *
 * @param S The temporal type (LocalDate, LocalTime, LocalDateTime, Instant, etc.)
 * @param message Custom error message provider
 * @return A new validator with the ensurePast-or-present constraint
 */
@IgnorableReturnValue
inline fun <reified S> Validation.ensurePastOrPresent(
    input: S,
    noinline message: MessageProvider = { "kova.temporal.pastOrPresent".resource },
) where S : Temporal, S : Comparable<S> = input.constrain("kova.temporal.pastOrPresent") { satisfies(it <= now(clock), message) }

/**
 * Obtains the current temporal value for the specified type using the provided clock.
 *
 * This internal function maps from a [KClass] to the appropriate static `now(Clock)` method
 * for each supported temporal type. It is used by the temporal constraint extension functions
 * (ensureFuture, ensurePast, etc.) to obtain the current time at validation time.
 *
 * The function uses reified type parameters at the call site to ensure type safety, and
 * the unchecked cast here is safe because the when expression guarantees that the returned
 * value's type ensureMatches the requested [KClass].
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
