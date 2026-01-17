package org.komapper.extension.validator

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
 * // Assuming today is 2025-01-15
 * tryValidate { LocalDate.of(2025, 1, 16).ensureInFuture() }  // Success
 * tryValidate { LocalDate.of(2025, 1, 15).ensureInFuture() }  // Failure (present)
 * tryValidate { LocalDate.of(2025, 1, 14).ensureInFuture() }  // Failure (past)
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver S The temporal value to validate
 * @param S The temporal type (LocalDate, LocalTime, LocalDateTime, Instant, etc.)
 * @param message Custom error message provider
 * @return The validated input value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public inline fun <reified S> S.ensureInFuture(
    noinline message: MessageProvider = { "kova.temporal.inFuture".resource },
): S where S : Temporal, S : Comparable<S> = constrain("kova.temporal.inFuture") { satisfies(it > now(), message) }

/**
 * Validates that the temporal value is in the future or present (greater than or equal to now).
 *
 * The current time is determined by the clock in [ValidationConfig] at validation time.
 *
 * Example:
 * ```kotlin
 * // Assuming today is 2025-01-15
 * tryValidate { LocalDate.of(2025, 1, 16).ensureInFutureOrPresent() }  // Success (future)
 * tryValidate { LocalDate.of(2025, 1, 15).ensureInFutureOrPresent() }  // Success (present)
 * tryValidate { LocalDate.of(2025, 1, 14).ensureInFutureOrPresent() }  // Failure (past)
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver S The temporal value to validate
 * @param S The temporal type (LocalDate, LocalTime, LocalDateTime, Instant, etc.)
 * @param message Custom error message provider
 * @return The validated input value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public inline fun <reified S> S.ensureInFutureOrPresent(
    noinline message: MessageProvider = { "kova.temporal.inFutureOrPresent".resource },
): S where S : Temporal, S : Comparable<S> = constrain("kova.temporal.inFutureOrPresent") { satisfies(it >= now(), message) }

/**
 * Validates that the temporal value is in the past (strictly less than now).
 *
 * The current time is determined by the clock in [ValidationConfig] at validation time.
 *
 * Example:
 * ```kotlin
 * // Assuming today is 2025-01-15
 * tryValidate { LocalDate.of(2025, 1, 14).ensureInPast() }  // Success
 * tryValidate { LocalDate.of(2025, 1, 15).ensureInPast() }  // Failure (present)
 * tryValidate { LocalDate.of(2025, 1, 16).ensureInPast() }  // Failure (future)
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver S The temporal value to validate
 * @param S The temporal type (LocalDate, LocalTime, LocalDateTime, Instant, etc.)
 * @param message Custom error message provider
 * @return The validated input value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public inline fun <reified S> S.ensureInPast(
    noinline message: MessageProvider = { "kova.temporal.inPast".resource },
): S where S : Temporal, S : Comparable<S> = constrain("kova.temporal.inPast") { satisfies(it < now(), message) }

/**
 * Validates that the temporal value is in the past or present (less than or equal to now).
 *
 * The current time is determined by the clock in [ValidationConfig] at validation time.
 *
 * Example:
 * ```kotlin
 * // Assuming today is 2025-01-15
 * tryValidate { LocalDate.of(2025, 1, 14).ensureInPastOrPresent() }  // Success (past)
 * tryValidate { LocalDate.of(2025, 1, 15).ensureInPastOrPresent() }  // Success (present)
 * tryValidate { LocalDate.of(2025, 1, 16).ensureInPastOrPresent() }  // Failure (future)
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver S The temporal value to validate
 * @param S The temporal type (LocalDate, LocalTime, LocalDateTime, Instant, etc.)
 * @param message Custom error message provider
 * @return The validated input value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public inline fun <reified S> S.ensureInPastOrPresent(
    noinline message: MessageProvider = { "kova.temporal.inPastOrPresent".resource },
): S where S : Temporal, S : Comparable<S> = constrain("kova.temporal.inPastOrPresent") { satisfies(it <= now(), message) }

/**
 * Obtains the current temporal value for the specified type using the configured clock.
 *
 * This internal function maps from a [KClass] to the appropriate static `now(Clock)` method
 * for each supported temporal type. It is used by the temporal constraint extension functions
 * (ensureInFuture, ensureInPast, etc.) to obtain the current time at validation time.
 *
 * The clock is obtained from [ValidationConfig.clock] via the [Validation] context parameter.
 *
 * The function uses reified type parameters at the call site to ensure type safety, and
 * the unchecked cast here is safe because the when expression guarantees that the returned
 * value's type matches the requested [KClass].
 *
 * @param Validation (context parameter) The validation context containing the clock configuration
 * @param T The temporal type to obtain the current value for
 * @return The current temporal value
 * @throws IllegalStateException if the temporal type is not supported
 */
@PublishedApi
context(v: Validation)
internal inline fun <reified T : Temporal> now(): T {
    val clock = v.config.clock
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
