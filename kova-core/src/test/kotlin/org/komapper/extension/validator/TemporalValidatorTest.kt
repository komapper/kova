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

            context("ensureMin") {
                val minDate = LocalDate.of(2025, 1, 1)

                test("success with equal value") {
                    val result = tryValidate { minDate.ensureMin(minDate) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { minDate.plusDays(1).ensureMin(minDate) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { minDate.minusDays(1).ensureMin(minDate) }
                    result.shouldBeFailure()
                }
            }

            context("ensureMax") {
                val maxDate = LocalDate.of(2025, 12, 31)

                test("success with equal value") {
                    val result = tryValidate { maxDate.ensureMax(maxDate) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { maxDate.minusDays(1).ensureMax(maxDate) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { maxDate.plusDays(1).ensureMax(maxDate) }
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

            context("ensureMin") {
                val minTime = LocalTime.of(9, 0, 0)

                test("success with equal value") {
                    val result = tryValidate { minTime.ensureMin(minTime) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { minTime.plusHours(1).ensureMin(minTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { minTime.minusHours(1).ensureMin(minTime) }
                    result.shouldBeFailure()
                }
            }

            context("ensureMax") {
                val maxTime = LocalTime.of(17, 0, 0)

                test("success with equal value") {
                    val result = tryValidate { maxTime.ensureMax(maxTime) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { maxTime.minusHours(1).ensureMax(maxTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { maxTime.plusHours(1).ensureMax(maxTime) }
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

            context("ensureMin") {
                val minDateTime = LocalDateTime.of(2025, 1, 1, 12, 0, 0)

                test("success with equal value") {
                    val result = tryValidate { minDateTime.ensureMin(minDateTime) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { minDateTime.plusHours(1).ensureMin(minDateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { minDateTime.minusHours(1).ensureMin(minDateTime) }
                    result.shouldBeFailure()
                }
            }

            context("ensureMax") {
                val maxDateTime = LocalDateTime.of(2025, 12, 31, 23, 59, 59)

                test("success with equal value") {
                    val result = tryValidate { maxDateTime.ensureMax(maxDateTime) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { maxDateTime.minusHours(1).ensureMax(maxDateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { maxDateTime.plusHours(1).ensureMax(maxDateTime) }
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

            context("ensureMin") {
                val minInstant = Instant.parse("2025-01-01T00:00:00Z")

                test("success with equal value") {
                    val result = tryValidate { minInstant.ensureMin(minInstant) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { minInstant.plusSeconds(3600).ensureMin(minInstant) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { minInstant.minusSeconds(3600).ensureMin(minInstant) }
                    result.shouldBeFailure()
                }
            }

            context("ensureMax") {
                val maxInstant = Instant.parse("2025-12-31T23:59:59Z")

                test("success with equal value") {
                    val result = tryValidate { maxInstant.ensureMax(maxInstant) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { maxInstant.minusSeconds(3600).ensureMax(maxInstant) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { maxInstant.plusSeconds(3600).ensureMax(maxInstant) }
                    result.shouldBeFailure()
                }
            }
        }

        context("MonthDay") {
            context("ensureMin") {
                val minMonthDay = MonthDay.of(3, 1)

                test("success with equal value") {
                    val result = tryValidate { minMonthDay.ensureMin(minMonthDay) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { MonthDay.of(3, 2).ensureMin(minMonthDay) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { MonthDay.of(2, 28).ensureMin(minMonthDay) }
                    result.shouldBeFailure()
                }
            }

            context("ensureMax") {
                val maxMonthDay = MonthDay.of(10, 31)

                test("success with equal value") {
                    val result = tryValidate { maxMonthDay.ensureMax(maxMonthDay) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { MonthDay.of(10, 30).ensureMax(maxMonthDay) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { MonthDay.of(11, 1).ensureMax(maxMonthDay) }
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

            context("ensureMin") {
                val minOffsetDateTime = OffsetDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)

                test("success with equal value") {
                    val result = tryValidate { minOffsetDateTime.ensureMin(minOffsetDateTime) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { minOffsetDateTime.plusHours(1).ensureMin(minOffsetDateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { minOffsetDateTime.minusHours(1).ensureMin(minOffsetDateTime) }
                    result.shouldBeFailure()
                }
            }

            context("ensureMax") {
                val maxOffsetDateTime = OffsetDateTime.of(2025, 12, 31, 23, 59, 59, 0, ZoneOffset.UTC)

                test("success with equal value") {
                    val result = tryValidate { maxOffsetDateTime.ensureMax(maxOffsetDateTime) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { maxOffsetDateTime.minusHours(1).ensureMax(maxOffsetDateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { maxOffsetDateTime.plusHours(1).ensureMax(maxOffsetDateTime) }
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

            context("ensureMin") {
                val minOffsetTime = OffsetTime.of(9, 0, 0, 0, ZoneOffset.UTC)

                test("success with equal value") {
                    val result = tryValidate { minOffsetTime.ensureMin(minOffsetTime) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { minOffsetTime.plusHours(1).ensureMin(minOffsetTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { minOffsetTime.minusHours(1).ensureMin(minOffsetTime) }
                    result.shouldBeFailure()
                }
            }

            context("ensureMax") {
                val maxOffsetTime = OffsetTime.of(17, 0, 0, 0, ZoneOffset.UTC)

                test("success with equal value") {
                    val result = tryValidate { maxOffsetTime.ensureMax(maxOffsetTime) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { maxOffsetTime.minusHours(1).ensureMax(maxOffsetTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { maxOffsetTime.plusHours(1).ensureMax(maxOffsetTime) }
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

            context("ensureMin") {
                val minYear = Year.of(2020)

                test("success with equal value") {
                    val result = tryValidate { minYear.ensureMin(minYear) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { Year.of(2021).ensureMin(minYear) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { Year.of(2019).ensureMin(minYear) }
                    result.shouldBeFailure()
                }
            }

            context("ensureMax") {
                val maxYear = Year.of(2030)

                test("success with equal value") {
                    val result = tryValidate { maxYear.ensureMax(maxYear) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { Year.of(2029).ensureMax(maxYear) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { Year.of(2031).ensureMax(maxYear) }
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

            context("ensureMin") {
                val minYearMonth = YearMonth.of(2024, 1)

                test("success with equal value") {
                    val result = tryValidate { minYearMonth.ensureMin(minYearMonth) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { YearMonth.of(2024, 2).ensureMin(minYearMonth) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { YearMonth.of(2023, 12).ensureMin(minYearMonth) }
                    result.shouldBeFailure()
                }
            }

            context("ensureMax") {
                val maxYearMonth = YearMonth.of(2025, 12)

                test("success with equal value") {
                    val result = tryValidate { maxYearMonth.ensureMax(maxYearMonth) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { YearMonth.of(2025, 11).ensureMax(maxYearMonth) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { YearMonth.of(2026, 1).ensureMax(maxYearMonth) }
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

            context("ensureMin") {
                val minZonedDateTime = ZonedDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)

                test("success with equal value") {
                    val result = tryValidate { minZonedDateTime.ensureMin(minZonedDateTime) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { minZonedDateTime.plusHours(1).ensureMin(minZonedDateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { minZonedDateTime.minusHours(1).ensureMin(minZonedDateTime) }
                    result.shouldBeFailure()
                }
            }

            context("ensureMax") {
                val maxZonedDateTime = ZonedDateTime.of(2025, 12, 31, 23, 59, 59, 0, ZoneOffset.UTC)

                test("success with equal value") {
                    val result = tryValidate { maxZonedDateTime.ensureMax(maxZonedDateTime) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { maxZonedDateTime.minusHours(1).ensureMax(maxZonedDateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { maxZonedDateTime.plusHours(1).ensureMax(maxZonedDateTime) }
                    result.shouldBeFailure()
                }
            }
        }
    })
