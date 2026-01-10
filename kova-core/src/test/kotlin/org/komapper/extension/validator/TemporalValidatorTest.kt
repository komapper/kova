package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.MonthDay
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.Year
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Locale

class TemporalValidatorTest :
    FunSpec({

        beforeSpec {
            Locale.setDefault(Locale.US)
        }

        context("LocalDate") {
            val date = LocalDate.of(2025, 1, 1)
            val zone = ZoneOffset.UTC
            val instant = date.atStartOfDay(zone).toInstant()
            val clock = Clock.fixed(instant, zone)
            val config = ValidationConfig(clock = clock)

            context("ensureFuture") {
                test("success") {
                    val result = tryValidate(config) { date.plusDays(1).ensureFuture() }
                    result.shouldBeSuccess()
                }

                test("failure with present value") {
                    val result = tryValidate(config) { date.ensureFuture() }
                    result.shouldBeFailure()
                }

                test("failure with ensurePast value") {
                    val result = tryValidate(config) { date.minusDays(1).ensureFuture() }
                    result.shouldBeFailure()
                }
            }

            context("ensureFutureOrPresent") {
                test("success with ensureFuture value") {
                    val result = tryValidate(config) { date.plusDays(1).ensureFutureOrPresent() }
                    result.shouldBeSuccess()
                }

                test("success with present value") {
                    val result = tryValidate(config) { date.ensureFutureOrPresent() }
                    result.shouldBeSuccess()
                }

                test("failure with ensurePast value") {
                    val result = tryValidate(config) { date.minusDays(1).ensureFutureOrPresent() }
                    result.shouldBeFailure()
                }
            }

            context("ensurePast") {
                test("success") {
                    val result = tryValidate(config) { date.minusDays(1).ensurePast() }
                    result.shouldBeSuccess()
                }

                test("failure with present value") {
                    val result = tryValidate(config) { date.ensurePast() }
                    result.shouldBeFailure()
                }

                test("failure with ensureFuture value") {
                    val result = tryValidate(config) { date.plusDays(1).ensurePast() }
                    result.shouldBeFailure()
                }
            }

            context("ensurePastOrPresent") {
                test("success with ensurePast value") {
                    val result = tryValidate(config) { date.minusDays(1).ensurePastOrPresent() }
                    result.shouldBeSuccess()
                }

                test("success with present value") {
                    val result = tryValidate(config) { date.ensurePastOrPresent() }
                    result.shouldBeSuccess()
                }

                test("failure with ensureFuture value") {
                    val result = tryValidate(config) { date.plusDays(1).ensurePastOrPresent() }
                    result.shouldBeFailure()
                }
            }

            context("ensureAtLeast") {
                val minDate = LocalDate.of(2025, 1, 1)

                test("success with equal value") {
                    val result = tryValidate { minDate.ensureAtLeast(minDate) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { minDate.plusDays(1).ensureAtLeast(minDate) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { minDate.minusDays(1).ensureAtLeast(minDate) }
                    result.shouldBeFailure()
                }
            }

            context("ensureAtMost") {
                val maxDate = LocalDate.of(2025, 12, 31)

                test("success with equal value") {
                    val result = tryValidate { maxDate.ensureAtMost(maxDate) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { maxDate.minusDays(1).ensureAtMost(maxDate) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { maxDate.plusDays(1).ensureAtMost(maxDate) }
                    result.shouldBeFailure()
                }
            }

            context("ensureGreaterThan") {
                val date = LocalDate.of(2025, 6, 15)

                test("success") {
                    val result = tryValidate { date.plusDays(1).ensureGreaterThan(date) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { date.ensureGreaterThan(date) }
                    result.shouldBeFailure()
                }

                test("failure with smaller value") {
                    val result = tryValidate { date.minusDays(1).ensureGreaterThan(date) }
                    result.shouldBeFailure()
                }
            }

            context("ensureGreaterThanOrEqual") {
                val date = LocalDate.of(2025, 6, 15)

                test("success with greater value") {
                    val result = tryValidate { date.plusDays(1).ensureGreaterThanOrEqual(date) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { date.ensureGreaterThanOrEqual(date) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { date.minusDays(1).ensureGreaterThanOrEqual(date) }
                    result.shouldBeFailure()
                }
            }

            context("ensureLessThan") {
                val date = LocalDate.of(2025, 6, 15)

                test("success") {
                    val result = tryValidate { date.minusDays(1).ensureLessThan(date) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { date.ensureLessThan(date) }
                    result.shouldBeFailure()
                }

                test("failure with greater value") {
                    val result = tryValidate { date.plusDays(1).ensureLessThan(date) }
                    result.shouldBeFailure()
                }
            }

            context("ensureLessThanOrEqual") {
                val date = LocalDate.of(2025, 6, 15)

                test("success with smaller value") {
                    val result = tryValidate { date.minusDays(1).ensureLessThanOrEqual(date) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { date.ensureLessThanOrEqual(date) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { date.plusDays(1).ensureLessThanOrEqual(date) }
                    result.shouldBeFailure()
                }
            }
        }

        context("LocalTime") {
            val date = LocalDate.of(2025, 1, 1)
            val time = LocalTime.of(12, 0, 0)
            val zone = ZoneOffset.UTC
            val instant = date.atTime(time).toInstant(zone)
            val clock = Clock.fixed(instant, zone)
            val config = ValidationConfig(clock = clock)

            context("ensureFuture") {
                test("success") {
                    val result = tryValidate(config) { time.plusHours(1).ensureFuture() }
                    result.shouldBeSuccess()
                }

                test("failure with present value") {
                    val result = tryValidate(config) { time.ensureFuture() }
                    result.shouldBeFailure()
                }

                test("failure with ensurePast value") {
                    val result = tryValidate(config) { time.minusHours(1).ensureFuture() }
                    result.shouldBeFailure()
                }
            }

            context("ensureFutureOrPresent") {
                test("success with ensureFuture value") {
                    val result = tryValidate(config) { time.plusHours(1).ensureFutureOrPresent() }
                    result.shouldBeSuccess()
                }

                test("success with present value") {
                    val result = tryValidate(config) { time.ensureFutureOrPresent() }
                    result.shouldBeSuccess()
                }

                test("failure with ensurePast value") {
                    val result = tryValidate(config) { time.minusHours(1).ensureFutureOrPresent() }
                    result.shouldBeFailure()
                }
            }

            context("ensurePast") {
                test("success") {
                    val result = tryValidate(config) { time.minusHours(1).ensurePast() }
                    result.shouldBeSuccess()
                }

                test("failure with present value") {
                    val result = tryValidate(config) { time.ensurePast() }
                    result.shouldBeFailure()
                }

                test("failure with ensureFuture value") {
                    val result = tryValidate(config) { time.plusHours(1).ensurePast() }
                    result.shouldBeFailure()
                }
            }

            context("ensurePastOrPresent") {
                test("success with ensurePast value") {
                    val result = tryValidate(config) { time.minusHours(1).ensurePastOrPresent() }
                    result.shouldBeSuccess()
                }

                test("success with present value") {
                    val result = tryValidate(config) { time.ensurePastOrPresent() }
                    result.shouldBeSuccess()
                }

                test("failure with ensureFuture value") {
                    val result = tryValidate(config) { time.plusHours(1).ensurePastOrPresent() }
                    result.shouldBeFailure()
                }
            }

            context("ensureAtLeast") {
                val minTime = LocalTime.of(9, 0, 0)

                test("success with equal value") {
                    val result = tryValidate { minTime.ensureAtLeast(minTime) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { minTime.plusHours(1).ensureAtLeast(minTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { minTime.minusHours(1).ensureAtLeast(minTime) }
                    result.shouldBeFailure()
                }
            }

            context("ensureAtMost") {
                val maxTime = LocalTime.of(17, 0, 0)

                test("success with equal value") {
                    val result = tryValidate { maxTime.ensureAtMost(maxTime) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { maxTime.minusHours(1).ensureAtMost(maxTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { maxTime.plusHours(1).ensureAtMost(maxTime) }
                    result.shouldBeFailure()
                }
            }

            context("ensureGreaterThan") {
                val time = LocalTime.of(12, 0, 0)

                test("success") {
                    val result = tryValidate { time.plusHours(1).ensureGreaterThan(time) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { time.ensureGreaterThan(time) }
                    result.shouldBeFailure()
                }

                test("failure with smaller value") {
                    val result = tryValidate { time.minusHours(1).ensureGreaterThan(time) }
                    result.shouldBeFailure()
                }
            }

            context("ensureGreaterThanOrEqual") {
                val time = LocalTime.of(12, 0, 0)

                test("success with greater value") {
                    val result = tryValidate { time.plusHours(1).ensureGreaterThanOrEqual(time) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { time.ensureGreaterThanOrEqual(time) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { time.minusHours(1).ensureGreaterThanOrEqual(time) }
                    result.shouldBeFailure()
                }
            }

            context("ensureLessThan") {
                val time = LocalTime.of(12, 0, 0)

                test("success") {
                    val result = tryValidate { time.minusHours(1).ensureLessThan(time) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { time.ensureLessThan(time) }
                    result.shouldBeFailure()
                }

                test("failure with greater value") {
                    val result = tryValidate { time.plusHours(1).ensureLessThan(time) }
                    result.shouldBeFailure()
                }
            }

            context("ensureLessThanOrEqual") {
                val time = LocalTime.of(12, 0, 0)

                test("success with smaller value") {
                    val result = tryValidate { time.minusHours(1).ensureLessThanOrEqual(time) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { time.ensureLessThanOrEqual(time) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { time.plusHours(1).ensureLessThanOrEqual(time) }
                    result.shouldBeFailure()
                }
            }
        }

        context("LocalDateTime") {
            val dateTime = LocalDateTime.of(2025, 1, 1, 12, 0, 0)
            val zone = ZoneOffset.UTC
            val instant = dateTime.toInstant(zone)
            val clock = Clock.fixed(instant, zone)
            val config = ValidationConfig(clock = clock)

            context("ensureFuture") {
                test("success") {
                    val result = tryValidate(config) { dateTime.plusHours(1).ensureFuture() }
                    result.shouldBeSuccess()
                }

                test("failure with present value") {
                    val result = tryValidate(config) { dateTime.ensureFuture() }
                    result.shouldBeFailure()
                }

                test("failure with ensurePast value") {
                    val result = tryValidate(config) { dateTime.minusHours(1).ensureFuture() }
                    result.shouldBeFailure()
                }
            }

            context("ensureFutureOrPresent") {
                test("success with ensureFuture value") {
                    val result = tryValidate(config) { dateTime.plusHours(1).ensureFutureOrPresent() }
                    result.shouldBeSuccess()
                }

                test("success with present value") {
                    val result = tryValidate(config) { dateTime.ensureFutureOrPresent() }
                    result.shouldBeSuccess()
                }

                test("failure with ensurePast value") {
                    val result = tryValidate(config) { dateTime.minusHours(1).ensureFutureOrPresent() }
                    result.shouldBeFailure()
                }
            }

            context("ensurePast") {
                test("success") {
                    val result = tryValidate(config) { dateTime.minusHours(1).ensurePast() }
                    result.shouldBeSuccess()
                }

                test("failure with present value") {
                    val result = tryValidate(config) { dateTime.ensurePast() }
                    result.shouldBeFailure()
                }

                test("failure with ensureFuture value") {
                    val result = tryValidate(config) { dateTime.plusHours(1).ensurePast() }
                    result.shouldBeFailure()
                }
            }

            context("ensurePastOrPresent") {
                test("success with ensurePast value") {
                    val result = tryValidate(config) { dateTime.minusHours(1).ensurePastOrPresent() }
                    result.shouldBeSuccess()
                }

                test("success with present value") {
                    val result = tryValidate(config) { dateTime.ensurePastOrPresent() }
                    result.shouldBeSuccess()
                }

                test("failure with ensureFuture value") {
                    val result = tryValidate(config) { dateTime.plusHours(1).ensurePastOrPresent() }
                    result.shouldBeFailure()
                }
            }

            context("ensureAtLeast") {
                val minDateTime = LocalDateTime.of(2025, 1, 1, 12, 0, 0)

                test("success with equal value") {
                    val result = tryValidate { minDateTime.ensureAtLeast(minDateTime) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { minDateTime.plusHours(1).ensureAtLeast(minDateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { minDateTime.minusHours(1).ensureAtLeast(minDateTime) }
                    result.shouldBeFailure()
                }
            }

            context("ensureAtMost") {
                val maxDateTime = LocalDateTime.of(2025, 12, 31, 23, 59, 59)

                test("success with equal value") {
                    val result = tryValidate { maxDateTime.ensureAtMost(maxDateTime) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { maxDateTime.minusHours(1).ensureAtMost(maxDateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { maxDateTime.plusHours(1).ensureAtMost(maxDateTime) }
                    result.shouldBeFailure()
                }
            }

            context("ensureGreaterThan") {
                val dateTime = LocalDateTime.of(2025, 6, 15, 12, 0, 0)

                test("success") {
                    val result = tryValidate { dateTime.plusHours(1).ensureGreaterThan(dateTime) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { dateTime.ensureGreaterThan(dateTime) }
                    result.shouldBeFailure()
                }

                test("failure with smaller value") {
                    val result = tryValidate { dateTime.minusHours(1).ensureGreaterThan(dateTime) }
                    result.shouldBeFailure()
                }
            }

            context("ensureGreaterThanOrEqual") {
                val dateTime = LocalDateTime.of(2025, 6, 15, 12, 0, 0)

                test("success with greater value") {
                    val result = tryValidate { dateTime.plusHours(1).ensureGreaterThanOrEqual(dateTime) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { dateTime.ensureGreaterThanOrEqual(dateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { dateTime.minusHours(1).ensureGreaterThanOrEqual(dateTime) }
                    result.shouldBeFailure()
                }
            }

            context("ensureLessThan") {
                val dateTime = LocalDateTime.of(2025, 6, 15, 12, 0, 0)

                test("success") {
                    val result = tryValidate { dateTime.minusHours(1).ensureLessThan(dateTime) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { dateTime.ensureLessThan(dateTime) }
                    result.shouldBeFailure()
                }

                test("failure with greater value") {
                    val result = tryValidate { dateTime.plusHours(1).ensureLessThan(dateTime) }
                    result.shouldBeFailure()
                }
            }

            context("ensureLessThanOrEqual") {
                val dateTime = LocalDateTime.of(2025, 6, 15, 12, 0, 0)

                test("success with smaller value") {
                    val result = tryValidate { dateTime.minusHours(1).ensureLessThanOrEqual(dateTime) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { dateTime.ensureLessThanOrEqual(dateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { dateTime.plusHours(1).ensureLessThanOrEqual(dateTime) }
                    result.shouldBeFailure()
                }
            }
        }

        context("Instant") {
            val instant = Instant.parse("2025-01-01T12:00:00Z")
            val clock = Clock.fixed(instant, ZoneOffset.UTC)
            val config = ValidationConfig(clock = clock)

            context("ensureFuture") {
                test("success") {
                    val result = tryValidate(config) { instant.plusSeconds(3600).ensureFuture() }
                    result.shouldBeSuccess()
                }

                test("failure with ensurePast value") {
                    val result = tryValidate(config) { instant.minusSeconds(3600).ensureFuture() }
                    result.shouldBeFailure()
                }
            }

            context("ensurePast") {
                test("success") {
                    val result = tryValidate(config) { instant.minusSeconds(3600).ensurePast() }
                    result.shouldBeSuccess()
                }

                test("failure with ensureFuture value") {
                    val result = tryValidate(config) { instant.plusSeconds(3600).ensurePast() }
                    result.shouldBeFailure()
                }
            }

            context("ensureAtLeast") {
                val minInstant = Instant.parse("2025-01-01T00:00:00Z")

                test("success with equal value") {
                    val result = tryValidate { minInstant.ensureAtLeast(minInstant) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { minInstant.plusSeconds(3600).ensureAtLeast(minInstant) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { minInstant.minusSeconds(3600).ensureAtLeast(minInstant) }
                    result.shouldBeFailure()
                }
            }

            context("ensureAtMost") {
                val maxInstant = Instant.parse("2025-12-31T23:59:59Z")

                test("success with equal value") {
                    val result = tryValidate { maxInstant.ensureAtMost(maxInstant) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { maxInstant.minusSeconds(3600).ensureAtMost(maxInstant) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { maxInstant.plusSeconds(3600).ensureAtMost(maxInstant) }
                    result.shouldBeFailure()
                }
            }
        }

        context("MonthDay") {
            context("ensureAtLeast") {
                val minMonthDay = MonthDay.of(3, 1)

                test("success with equal value") {
                    val result = tryValidate { minMonthDay.ensureAtLeast(minMonthDay) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { MonthDay.of(3, 2).ensureAtLeast(minMonthDay) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { MonthDay.of(2, 28).ensureAtLeast(minMonthDay) }
                    result.shouldBeFailure()
                }
            }

            context("ensureAtMost") {
                val maxMonthDay = MonthDay.of(10, 31)

                test("success with equal value") {
                    val result = tryValidate { maxMonthDay.ensureAtMost(maxMonthDay) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { MonthDay.of(10, 30).ensureAtMost(maxMonthDay) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { MonthDay.of(11, 1).ensureAtMost(maxMonthDay) }
                    result.shouldBeFailure()
                }
            }

            context("ensureGreaterThan") {
                val monthDay = MonthDay.of(6, 15)

                test("success") {
                    val result = tryValidate { MonthDay.of(6, 16).ensureGreaterThan(monthDay) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { monthDay.ensureGreaterThan(monthDay) }
                    result.shouldBeFailure()
                }

                test("failure with smaller value") {
                    val result = tryValidate { MonthDay.of(6, 14).ensureGreaterThan(monthDay) }
                    result.shouldBeFailure()
                }
            }

            context("ensureGreaterThanOrEqual") {
                val monthDay = MonthDay.of(6, 15)

                test("success with greater value") {
                    val result = tryValidate { MonthDay.of(6, 16).ensureGreaterThanOrEqual(monthDay) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { monthDay.ensureGreaterThanOrEqual(monthDay) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { MonthDay.of(6, 14).ensureGreaterThanOrEqual(monthDay) }
                    result.shouldBeFailure()
                }
            }

            context("ensureLessThan") {
                val monthDay = MonthDay.of(6, 15)

                test("success") {
                    val result = tryValidate { MonthDay.of(6, 14).ensureLessThan(monthDay) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { monthDay.ensureLessThan(monthDay) }
                    result.shouldBeFailure()
                }

                test("failure with greater value") {
                    val result = tryValidate { MonthDay.of(6, 16).ensureLessThan(monthDay) }
                    result.shouldBeFailure()
                }
            }

            context("ensureLessThanOrEqual") {
                val monthDay = MonthDay.of(6, 15)

                test("success with smaller value") {
                    val result = tryValidate { MonthDay.of(6, 14).ensureLessThanOrEqual(monthDay) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { monthDay.ensureLessThanOrEqual(monthDay) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { MonthDay.of(6, 16).ensureLessThanOrEqual(monthDay) }
                    result.shouldBeFailure()
                }
            }
        }

        context("OffsetDateTime") {
            val offsetDateTime = OffsetDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC)
            val clock = Clock.fixed(offsetDateTime.toInstant(), ZoneOffset.UTC)
            val config = ValidationConfig(clock = clock)

            context("ensureFuture") {
                test("success") {
                    val result = tryValidate(config) { offsetDateTime.plusHours(1).ensureFuture() }
                    result.shouldBeSuccess()
                }

                test("failure with ensurePast value") {
                    val result = tryValidate(config) { offsetDateTime.minusHours(1).ensureFuture() }
                    result.shouldBeFailure()
                }
            }

            context("ensurePast") {
                test("success") {
                    val result = tryValidate(config) { offsetDateTime.minusHours(1).ensurePast() }
                    result.shouldBeSuccess()
                }

                test("failure with ensureFuture value") {
                    val result = tryValidate(config) { offsetDateTime.plusHours(1).ensurePast() }
                    result.shouldBeFailure()
                }
            }

            context("ensureAtLeast") {
                val minOffsetDateTime = OffsetDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)

                test("success with equal value") {
                    val result = tryValidate { minOffsetDateTime.ensureAtLeast(minOffsetDateTime) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { minOffsetDateTime.plusHours(1).ensureAtLeast(minOffsetDateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { minOffsetDateTime.minusHours(1).ensureAtLeast(minOffsetDateTime) }
                    result.shouldBeFailure()
                }
            }

            context("ensureAtMost") {
                val maxOffsetDateTime = OffsetDateTime.of(2025, 12, 31, 23, 59, 59, 0, ZoneOffset.UTC)

                test("success with equal value") {
                    val result = tryValidate { maxOffsetDateTime.ensureAtMost(maxOffsetDateTime) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { maxOffsetDateTime.minusHours(1).ensureAtMost(maxOffsetDateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { maxOffsetDateTime.plusHours(1).ensureAtMost(maxOffsetDateTime) }
                    result.shouldBeFailure()
                }
            }
        }

        context("OffsetTime") {
            val date = LocalDate.of(2025, 1, 1)
            val offsetTime = OffsetTime.of(12, 0, 0, 0, ZoneOffset.UTC)
            val instant = date.atTime(offsetTime.toLocalTime()).toInstant(ZoneOffset.UTC)
            val clock = Clock.fixed(instant, ZoneOffset.UTC)
            val config = ValidationConfig(clock = clock)

            context("ensureFuture") {
                test("success") {
                    val result = tryValidate(config) { offsetTime.plusHours(1).ensureFuture() }
                    result.shouldBeSuccess()
                }

                test("failure with ensurePast value") {
                    val result = tryValidate(config) { offsetTime.minusHours(1).ensureFuture() }
                    result.shouldBeFailure()
                }
            }

            context("ensurePast") {
                test("success") {
                    val result = tryValidate(config) { offsetTime.minusHours(1).ensurePast() }
                    result.shouldBeSuccess()
                }

                test("failure with ensureFuture value") {
                    val result = tryValidate(config) { offsetTime.plusHours(1).ensurePast() }
                    result.shouldBeFailure()
                }
            }

            context("ensureAtLeast") {
                val minOffsetTime = OffsetTime.of(9, 0, 0, 0, ZoneOffset.UTC)

                test("success with equal value") {
                    val result = tryValidate { minOffsetTime.ensureAtLeast(minOffsetTime) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { minOffsetTime.plusHours(1).ensureAtLeast(minOffsetTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { minOffsetTime.minusHours(1).ensureAtLeast(minOffsetTime) }
                    result.shouldBeFailure()
                }
            }

            context("ensureAtMost") {
                val maxOffsetTime = OffsetTime.of(17, 0, 0, 0, ZoneOffset.UTC)

                test("success with equal value") {
                    val result = tryValidate { maxOffsetTime.ensureAtMost(maxOffsetTime) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { maxOffsetTime.minusHours(1).ensureAtMost(maxOffsetTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { maxOffsetTime.plusHours(1).ensureAtMost(maxOffsetTime) }
                    result.shouldBeFailure()
                }
            }
        }

        context("Year") {
            val date = LocalDate.of(2025, 1, 1)
            val zone = ZoneOffset.UTC
            val instant = date.atStartOfDay(zone).toInstant()
            val clock = Clock.fixed(instant, zone)
            val config = ValidationConfig(clock = clock)

            context("ensureFuture") {
                test("success") {
                    val result = tryValidate(config) { Year.of(2026).ensureFuture() }
                    result.shouldBeSuccess()
                }

                test("failure with ensurePast value") {
                    val result = tryValidate(config) { Year.of(2024).ensureFuture() }
                    result.shouldBeFailure()
                }
            }

            context("ensurePast") {
                test("success") {
                    val result = tryValidate(config) { Year.of(2024).ensurePast() }
                    result.shouldBeSuccess()
                }

                test("failure with ensureFuture value") {
                    val result = tryValidate(config) { Year.of(2026).ensurePast() }
                    result.shouldBeFailure()
                }
            }

            context("ensureAtLeast") {
                val minYear = Year.of(2020)

                test("success with equal value") {
                    val result = tryValidate { minYear.ensureAtLeast(minYear) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { Year.of(2021).ensureAtLeast(minYear) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { Year.of(2019).ensureAtLeast(minYear) }
                    result.shouldBeFailure()
                }
            }

            context("ensureAtMost") {
                val maxYear = Year.of(2030)

                test("success with equal value") {
                    val result = tryValidate { maxYear.ensureAtMost(maxYear) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { Year.of(2029).ensureAtMost(maxYear) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { Year.of(2031).ensureAtMost(maxYear) }
                    result.shouldBeFailure()
                }
            }
        }

        context("YearMonth") {
            val date = LocalDate.of(2025, 1, 1)
            val zone = ZoneOffset.UTC
            val instant = date.atStartOfDay(zone).toInstant()
            val clock = Clock.fixed(instant, zone)
            val config = ValidationConfig(clock = clock)

            context("ensureFuture") {
                test("success") {
                    val result = tryValidate(config) { YearMonth.of(2025, 2).ensureFuture() }
                    result.shouldBeSuccess()
                }

                test("failure with ensurePast value") {
                    val result = tryValidate(config) { YearMonth.of(2024, 12).ensureFuture() }
                    result.shouldBeFailure()
                }
            }

            context("ensurePast") {
                test("success") {
                    val result = tryValidate(config) { YearMonth.of(2024, 12).ensurePast() }
                    result.shouldBeSuccess()
                }

                test("failure with ensureFuture value") {
                    val result = tryValidate(config) { YearMonth.of(2025, 2).ensurePast() }
                    result.shouldBeFailure()
                }
            }

            context("ensureAtLeast") {
                val minYearMonth = YearMonth.of(2024, 1)

                test("success with equal value") {
                    val result = tryValidate { minYearMonth.ensureAtLeast(minYearMonth) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { YearMonth.of(2024, 2).ensureAtLeast(minYearMonth) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { YearMonth.of(2023, 12).ensureAtLeast(minYearMonth) }
                    result.shouldBeFailure()
                }
            }

            context("ensureAtMost") {
                val maxYearMonth = YearMonth.of(2025, 12)

                test("success with equal value") {
                    val result = tryValidate { maxYearMonth.ensureAtMost(maxYearMonth) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { YearMonth.of(2025, 11).ensureAtMost(maxYearMonth) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { YearMonth.of(2026, 1).ensureAtMost(maxYearMonth) }
                    result.shouldBeFailure()
                }
            }
        }

        context("ZonedDateTime") {
            val zonedDateTime = ZonedDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC)
            val clock = Clock.fixed(zonedDateTime.toInstant(), ZoneOffset.UTC)
            val config = ValidationConfig(clock = clock)

            context("ensureFuture") {
                test("success") {
                    val result = tryValidate(config) { zonedDateTime.plusHours(1).ensureFuture() }
                    result.shouldBeSuccess()
                }

                test("failure with ensurePast value") {
                    val result = tryValidate(config) { zonedDateTime.minusHours(1).ensureFuture() }
                    result.shouldBeFailure()
                }
            }

            context("ensurePast") {
                test("success") {
                    val result = tryValidate(config) { zonedDateTime.minusHours(1).ensurePast() }
                    result.shouldBeSuccess()
                }

                test("failure with ensureFuture value") {
                    val result = tryValidate(config) { zonedDateTime.plusHours(1).ensurePast() }
                    result.shouldBeFailure()
                }
            }

            context("ensureAtLeast") {
                val minZonedDateTime = ZonedDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)

                test("success with equal value") {
                    val result = tryValidate { minZonedDateTime.ensureAtLeast(minZonedDateTime) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { minZonedDateTime.plusHours(1).ensureAtLeast(minZonedDateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { minZonedDateTime.minusHours(1).ensureAtLeast(minZonedDateTime) }
                    result.shouldBeFailure()
                }
            }

            context("ensureAtMost") {
                val maxZonedDateTime = ZonedDateTime.of(2025, 12, 31, 23, 59, 59, 0, ZoneOffset.UTC)

                test("success with equal value") {
                    val result = tryValidate { maxZonedDateTime.ensureAtMost(maxZonedDateTime) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { maxZonedDateTime.minusHours(1).ensureAtMost(maxZonedDateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { maxZonedDateTime.plusHours(1).ensureAtMost(maxZonedDateTime) }
                    result.shouldBeFailure()
                }
            }
        }
    })
