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
                    val result = tryValidate(config) { ensureFuture(date.plusDays(1)) }
                    result.shouldBeSuccess()
                }

                test("failure with present value") {
                    val result = tryValidate(config) { ensureFuture(date) }
                    result.shouldBeFailure()
                }

                test("failure with ensurePast value") {
                    val result = tryValidate(config) { ensureFuture(date.minusDays(1)) }
                    result.shouldBeFailure()
                }
            }

            context("ensureFutureOrPresent") {
                test("success with ensureFuture value") {
                    val result = tryValidate(config) { ensureFutureOrPresent(date.plusDays(1)) }
                    result.shouldBeSuccess()
                }

                test("success with present value") {
                    val result = tryValidate(config) { ensureFutureOrPresent(date) }
                    result.shouldBeSuccess()
                }

                test("failure with ensurePast value") {
                    val result = tryValidate(config) { ensureFutureOrPresent(date.minusDays(1)) }
                    result.shouldBeFailure()
                }
            }

            context("ensurePast") {
                test("success") {
                    val result = tryValidate(config) { ensurePast(date.minusDays(1)) }
                    result.shouldBeSuccess()
                }

                test("failure with present value") {
                    val result = tryValidate(config) { ensurePast(date) }
                    result.shouldBeFailure()
                }

                test("failure with ensureFuture value") {
                    val result = tryValidate(config) { ensurePast(date.plusDays(1)) }
                    result.shouldBeFailure()
                }
            }

            context("ensurePastOrPresent") {
                test("success with ensurePast value") {
                    val result = tryValidate(config) { ensurePastOrPresent(date.minusDays(1)) }
                    result.shouldBeSuccess()
                }

                test("success with present value") {
                    val result = tryValidate(config) { ensurePastOrPresent(date) }
                    result.shouldBeSuccess()
                }

                test("failure with ensureFuture value") {
                    val result = tryValidate(config) { ensurePastOrPresent(date.plusDays(1)) }
                    result.shouldBeFailure()
                }
            }

            context("ensureMin") {
                val minDate = LocalDate.of(2025, 1, 1)

                test("success with equal value") {
                    val result = tryValidate { ensureMin(minDate, minDate) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { ensureMin(minDate.plusDays(1), minDate) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { ensureMin(minDate.minusDays(1), minDate) }
                    result.shouldBeFailure()
                }
            }

            context("ensureMax") {
                val maxDate = LocalDate.of(2025, 12, 31)

                test("success with equal value") {
                    val result = tryValidate { ensureMax(maxDate, maxDate) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { ensureMax(maxDate.minusDays(1), maxDate) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { ensureMax(maxDate.plusDays(1), maxDate) }
                    result.shouldBeFailure()
                }
            }

            context("ensureGreaterThan") {
                val date = LocalDate.of(2025, 6, 15)

                test("success") {
                    val result = tryValidate { ensureGreaterThan(date.plusDays(1), date) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { ensureGreaterThan(date, date) }
                    result.shouldBeFailure()
                }

                test("failure with smaller value") {
                    val result = tryValidate { ensureGreaterThan(date.minusDays(1), date) }
                    result.shouldBeFailure()
                }
            }

            context("ensureGreaterThanOrEqual") {
                val date = LocalDate.of(2025, 6, 15)

                test("success with greater value") {
                    val result = tryValidate { ensureGreaterThanOrEqual(date.plusDays(1), date) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { ensureGreaterThanOrEqual(date, date) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { ensureGreaterThanOrEqual(date.minusDays(1), date) }
                    result.shouldBeFailure()
                }
            }

            context("ensureLessThan") {
                val date = LocalDate.of(2025, 6, 15)

                test("success") {
                    val result = tryValidate { ensureLessThan(date.minusDays(1), date) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { ensureLessThan(date, date) }
                    result.shouldBeFailure()
                }

                test("failure with greater value") {
                    val result = tryValidate { ensureLessThan(date.plusDays(1), date) }
                    result.shouldBeFailure()
                }
            }

            context("ensureLessThanOrEqual") {
                val date = LocalDate.of(2025, 6, 15)

                test("success with smaller value") {
                    val result = tryValidate { ensureLessThanOrEqual(date.minusDays(1), date) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { ensureLessThanOrEqual(date, date) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { ensureLessThanOrEqual(date.plusDays(1), date) }
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
                    val result = tryValidate(config) { ensureFuture(time.plusHours(1)) }
                    result.shouldBeSuccess()
                }

                test("failure with present value") {
                    val result = tryValidate(config) { ensureFuture(time) }
                    result.shouldBeFailure()
                }

                test("failure with ensurePast value") {
                    val result = tryValidate(config) { ensureFuture(time.minusHours(1)) }
                    result.shouldBeFailure()
                }
            }

            context("ensureFutureOrPresent") {
                test("success with ensureFuture value") {
                    val result = tryValidate(config) { ensureFutureOrPresent(time.plusHours(1)) }
                    result.shouldBeSuccess()
                }

                test("success with present value") {
                    val result = tryValidate(config) { ensureFutureOrPresent(time) }
                    result.shouldBeSuccess()
                }

                test("failure with ensurePast value") {
                    val result = tryValidate(config) { ensureFutureOrPresent(time.minusHours(1)) }
                    result.shouldBeFailure()
                }
            }

            context("ensurePast") {
                test("success") {
                    val result = tryValidate(config) { ensurePast(time.minusHours(1)) }
                    result.shouldBeSuccess()
                }

                test("failure with present value") {
                    val result = tryValidate(config) { ensurePast(time) }
                    result.shouldBeFailure()
                }

                test("failure with ensureFuture value") {
                    val result = tryValidate(config) { ensurePast(time.plusHours(1)) }
                    result.shouldBeFailure()
                }
            }

            context("ensurePastOrPresent") {
                test("success with ensurePast value") {
                    val result = tryValidate(config) { ensurePastOrPresent(time.minusHours(1)) }
                    result.shouldBeSuccess()
                }

                test("success with present value") {
                    val result = tryValidate(config) { ensurePastOrPresent(time) }
                    result.shouldBeSuccess()
                }

                test("failure with ensureFuture value") {
                    val result = tryValidate(config) { ensurePastOrPresent(time.plusHours(1)) }
                    result.shouldBeFailure()
                }
            }

            context("ensureMin") {
                val minTime = LocalTime.of(9, 0, 0)

                test("success with equal value") {
                    val result = tryValidate { ensureMin(minTime, minTime) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { ensureMin(minTime.plusHours(1), minTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { ensureMin(minTime.minusHours(1), minTime) }
                    result.shouldBeFailure()
                }
            }

            context("ensureMax") {
                val maxTime = LocalTime.of(17, 0, 0)

                test("success with equal value") {
                    val result = tryValidate { ensureMax(maxTime, maxTime) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { ensureMax(maxTime.minusHours(1), maxTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { ensureMax(maxTime.plusHours(1), maxTime) }
                    result.shouldBeFailure()
                }
            }

            context("ensureGreaterThan") {
                val time = LocalTime.of(12, 0, 0)

                test("success") {
                    val result = tryValidate { ensureGreaterThan(time.plusHours(1), time) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { ensureGreaterThan(time, time) }
                    result.shouldBeFailure()
                }

                test("failure with smaller value") {
                    val result = tryValidate { ensureGreaterThan(time.minusHours(1), time) }
                    result.shouldBeFailure()
                }
            }

            context("ensureGreaterThanOrEqual") {
                val time = LocalTime.of(12, 0, 0)

                test("success with greater value") {
                    val result = tryValidate { ensureGreaterThanOrEqual(time.plusHours(1), time) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { ensureGreaterThanOrEqual(time, time) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { ensureGreaterThanOrEqual(time.minusHours(1), time) }
                    result.shouldBeFailure()
                }
            }

            context("ensureLessThan") {
                val time = LocalTime.of(12, 0, 0)

                test("success") {
                    val result = tryValidate { ensureLessThan(time.minusHours(1), time) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { ensureLessThan(time, time) }
                    result.shouldBeFailure()
                }

                test("failure with greater value") {
                    val result = tryValidate { ensureLessThan(time.plusHours(1), time) }
                    result.shouldBeFailure()
                }
            }

            context("ensureLessThanOrEqual") {
                val time = LocalTime.of(12, 0, 0)

                test("success with smaller value") {
                    val result = tryValidate { ensureLessThanOrEqual(time.minusHours(1), time) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { ensureLessThanOrEqual(time, time) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { ensureLessThanOrEqual(time.plusHours(1), time) }
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
                    val result = tryValidate(config) { ensureFuture(dateTime.plusHours(1)) }
                    result.shouldBeSuccess()
                }

                test("failure with present value") {
                    val result = tryValidate(config) { ensureFuture(dateTime) }
                    result.shouldBeFailure()
                }

                test("failure with ensurePast value") {
                    val result = tryValidate(config) { ensureFuture(dateTime.minusHours(1)) }
                    result.shouldBeFailure()
                }
            }

            context("ensureFutureOrPresent") {
                test("success with ensureFuture value") {
                    val result = tryValidate(config) { ensureFutureOrPresent(dateTime.plusHours(1)) }
                    result.shouldBeSuccess()
                }

                test("success with present value") {
                    val result = tryValidate(config) { ensureFutureOrPresent(dateTime) }
                    result.shouldBeSuccess()
                }

                test("failure with ensurePast value") {
                    val result = tryValidate(config) { ensureFutureOrPresent(dateTime.minusHours(1)) }
                    result.shouldBeFailure()
                }
            }

            context("ensurePast") {
                test("success") {
                    val result = tryValidate(config) { ensurePast(dateTime.minusHours(1)) }
                    result.shouldBeSuccess()
                }

                test("failure with present value") {
                    val result = tryValidate(config) { ensurePast(dateTime) }
                    result.shouldBeFailure()
                }

                test("failure with ensureFuture value") {
                    val result = tryValidate(config) { ensurePast(dateTime.plusHours(1)) }
                    result.shouldBeFailure()
                }
            }

            context("ensurePastOrPresent") {
                test("success with ensurePast value") {
                    val result = tryValidate(config) { ensurePastOrPresent(dateTime.minusHours(1)) }
                    result.shouldBeSuccess()
                }

                test("success with present value") {
                    val result = tryValidate(config) { ensurePastOrPresent(dateTime) }
                    result.shouldBeSuccess()
                }

                test("failure with ensureFuture value") {
                    val result = tryValidate(config) { ensurePastOrPresent(dateTime.plusHours(1)) }
                    result.shouldBeFailure()
                }
            }

            context("ensureMin") {
                val minDateTime = LocalDateTime.of(2025, 1, 1, 12, 0, 0)

                test("success with equal value") {
                    val result = tryValidate { ensureMin(minDateTime, minDateTime) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { ensureMin(minDateTime.plusHours(1), minDateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { ensureMin(minDateTime.minusHours(1), minDateTime) }
                    result.shouldBeFailure()
                }
            }

            context("ensureMax") {
                val maxDateTime = LocalDateTime.of(2025, 12, 31, 23, 59, 59)

                test("success with equal value") {
                    val result = tryValidate { ensureMax(maxDateTime, maxDateTime) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { ensureMax(maxDateTime.minusHours(1), maxDateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { ensureMax(maxDateTime.plusHours(1), maxDateTime) }
                    result.shouldBeFailure()
                }
            }

            context("ensureGreaterThan") {
                val dateTime = LocalDateTime.of(2025, 6, 15, 12, 0, 0)

                test("success") {
                    val result = tryValidate { ensureGreaterThan(dateTime.plusHours(1), dateTime) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { ensureGreaterThan(dateTime, dateTime) }
                    result.shouldBeFailure()
                }

                test("failure with smaller value") {
                    val result = tryValidate { ensureGreaterThan(dateTime.minusHours(1), dateTime) }
                    result.shouldBeFailure()
                }
            }

            context("ensureGreaterThanOrEqual") {
                val dateTime = LocalDateTime.of(2025, 6, 15, 12, 0, 0)

                test("success with greater value") {
                    val result = tryValidate { ensureGreaterThanOrEqual(dateTime.plusHours(1), dateTime) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { ensureGreaterThanOrEqual(dateTime, dateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { ensureGreaterThanOrEqual(dateTime.minusHours(1), dateTime) }
                    result.shouldBeFailure()
                }
            }

            context("ensureLessThan") {
                val dateTime = LocalDateTime.of(2025, 6, 15, 12, 0, 0)

                test("success") {
                    val result = tryValidate { ensureLessThan(dateTime.minusHours(1), dateTime) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { ensureLessThan(dateTime, dateTime) }
                    result.shouldBeFailure()
                }

                test("failure with greater value") {
                    val result = tryValidate { ensureLessThan(dateTime.plusHours(1), dateTime) }
                    result.shouldBeFailure()
                }
            }

            context("ensureLessThanOrEqual") {
                val dateTime = LocalDateTime.of(2025, 6, 15, 12, 0, 0)

                test("success with smaller value") {
                    val result = tryValidate { ensureLessThanOrEqual(dateTime.minusHours(1), dateTime) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { ensureLessThanOrEqual(dateTime, dateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { ensureLessThanOrEqual(dateTime.plusHours(1), dateTime) }
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
                    val result = tryValidate(config) { ensureFuture(instant.plusSeconds(3600)) }
                    result.shouldBeSuccess()
                }

                test("failure with ensurePast value") {
                    val result = tryValidate(config) { ensureFuture(instant.minusSeconds(3600)) }
                    result.shouldBeFailure()
                }
            }

            context("ensurePast") {
                test("success") {
                    val result = tryValidate(config) { ensurePast(instant.minusSeconds(3600)) }
                    result.shouldBeSuccess()
                }

                test("failure with ensureFuture value") {
                    val result = tryValidate(config) { ensurePast(instant.plusSeconds(3600)) }
                    result.shouldBeFailure()
                }
            }

            context("ensureMin") {
                val minInstant = Instant.parse("2025-01-01T00:00:00Z")

                test("success with equal value") {
                    val result = tryValidate { ensureMin(minInstant, minInstant) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { ensureMin(minInstant.plusSeconds(3600), minInstant) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { ensureMin(minInstant.minusSeconds(3600), minInstant) }
                    result.shouldBeFailure()
                }
            }

            context("ensureMax") {
                val maxInstant = Instant.parse("2025-12-31T23:59:59Z")

                test("success with equal value") {
                    val result = tryValidate { ensureMax(maxInstant, maxInstant) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { ensureMax(maxInstant.minusSeconds(3600), maxInstant) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { ensureMax(maxInstant.plusSeconds(3600), maxInstant) }
                    result.shouldBeFailure()
                }
            }
        }

        context("MonthDay") {
            context("ensureMin") {
                val minMonthDay = MonthDay.of(3, 1)

                test("success with equal value") {
                    val result = tryValidate { ensureMin(minMonthDay, minMonthDay) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { ensureMin(MonthDay.of(3, 2), minMonthDay) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { ensureMin(MonthDay.of(2, 28), minMonthDay) }
                    result.shouldBeFailure()
                }
            }

            context("ensureMax") {
                val maxMonthDay = MonthDay.of(10, 31)

                test("success with equal value") {
                    val result = tryValidate { ensureMax(maxMonthDay, maxMonthDay) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { ensureMax(MonthDay.of(10, 30), maxMonthDay) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { ensureMax(MonthDay.of(11, 1), maxMonthDay) }
                    result.shouldBeFailure()
                }
            }

            context("ensureGreaterThan") {
                val monthDay = MonthDay.of(6, 15)

                test("success") {
                    val result = tryValidate { ensureGreaterThan(MonthDay.of(6, 16), monthDay) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { ensureGreaterThan(monthDay, monthDay) }
                    result.shouldBeFailure()
                }

                test("failure with smaller value") {
                    val result = tryValidate { ensureGreaterThan(MonthDay.of(6, 14), monthDay) }
                    result.shouldBeFailure()
                }
            }

            context("ensureGreaterThanOrEqual") {
                val monthDay = MonthDay.of(6, 15)

                test("success with greater value") {
                    val result = tryValidate { ensureGreaterThanOrEqual(MonthDay.of(6, 16), monthDay) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { ensureGreaterThanOrEqual(monthDay, monthDay) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { ensureGreaterThanOrEqual(MonthDay.of(6, 14), monthDay) }
                    result.shouldBeFailure()
                }
            }

            context("ensureLessThan") {
                val monthDay = MonthDay.of(6, 15)

                test("success") {
                    val result = tryValidate { ensureLessThan(MonthDay.of(6, 14), monthDay) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { ensureLessThan(monthDay, monthDay) }
                    result.shouldBeFailure()
                }

                test("failure with greater value") {
                    val result = tryValidate { ensureLessThan(MonthDay.of(6, 16), monthDay) }
                    result.shouldBeFailure()
                }
            }

            context("ensureLessThanOrEqual") {
                val monthDay = MonthDay.of(6, 15)

                test("success with smaller value") {
                    val result = tryValidate { ensureLessThanOrEqual(MonthDay.of(6, 14), monthDay) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { ensureLessThanOrEqual(monthDay, monthDay) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { ensureLessThanOrEqual(MonthDay.of(6, 16), monthDay) }
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
                    val result = tryValidate(config) { ensureFuture(offsetDateTime.plusHours(1)) }
                    result.shouldBeSuccess()
                }

                test("failure with ensurePast value") {
                    val result = tryValidate(config) { ensureFuture(offsetDateTime.minusHours(1)) }
                    result.shouldBeFailure()
                }
            }

            context("ensurePast") {
                test("success") {
                    val result = tryValidate(config) { ensurePast(offsetDateTime.minusHours(1)) }
                    result.shouldBeSuccess()
                }

                test("failure with ensureFuture value") {
                    val result = tryValidate(config) { ensurePast(offsetDateTime.plusHours(1)) }
                    result.shouldBeFailure()
                }
            }

            context("ensureMin") {
                val minOffsetDateTime = OffsetDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)

                test("success with equal value") {
                    val result = tryValidate { ensureMin(minOffsetDateTime, minOffsetDateTime) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { ensureMin(minOffsetDateTime.plusHours(1), minOffsetDateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { ensureMin(minOffsetDateTime.minusHours(1), minOffsetDateTime) }
                    result.shouldBeFailure()
                }
            }

            context("ensureMax") {
                val maxOffsetDateTime = OffsetDateTime.of(2025, 12, 31, 23, 59, 59, 0, ZoneOffset.UTC)

                test("success with equal value") {
                    val result = tryValidate { ensureMax(maxOffsetDateTime, maxOffsetDateTime) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { ensureMax(maxOffsetDateTime.minusHours(1), maxOffsetDateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { ensureMax(maxOffsetDateTime.plusHours(1), maxOffsetDateTime) }
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
                    val result = tryValidate(config) { ensureFuture(offsetTime.plusHours(1)) }
                    result.shouldBeSuccess()
                }

                test("failure with ensurePast value") {
                    val result = tryValidate(config) { ensureFuture(offsetTime.minusHours(1)) }
                    result.shouldBeFailure()
                }
            }

            context("ensurePast") {
                test("success") {
                    val result = tryValidate(config) { ensurePast(offsetTime.minusHours(1)) }
                    result.shouldBeSuccess()
                }

                test("failure with ensureFuture value") {
                    val result = tryValidate(config) { ensurePast(offsetTime.plusHours(1)) }
                    result.shouldBeFailure()
                }
            }

            context("ensureMin") {
                val minOffsetTime = OffsetTime.of(9, 0, 0, 0, ZoneOffset.UTC)

                test("success with equal value") {
                    val result = tryValidate { ensureMin(minOffsetTime, minOffsetTime) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { ensureMin(minOffsetTime.plusHours(1), minOffsetTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { ensureMin(minOffsetTime.minusHours(1), minOffsetTime) }
                    result.shouldBeFailure()
                }
            }

            context("ensureMax") {
                val maxOffsetTime = OffsetTime.of(17, 0, 0, 0, ZoneOffset.UTC)

                test("success with equal value") {
                    val result = tryValidate { ensureMax(maxOffsetTime, maxOffsetTime) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { ensureMax(maxOffsetTime.minusHours(1), maxOffsetTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { ensureMax(maxOffsetTime.plusHours(1), maxOffsetTime) }
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
                    val result = tryValidate(config) { ensureFuture(Year.of(2026)) }
                    result.shouldBeSuccess()
                }

                test("failure with ensurePast value") {
                    val result = tryValidate(config) { ensureFuture(Year.of(2024)) }
                    result.shouldBeFailure()
                }
            }

            context("ensurePast") {
                test("success") {
                    val result = tryValidate(config) { ensurePast(Year.of(2024)) }
                    result.shouldBeSuccess()
                }

                test("failure with ensureFuture value") {
                    val result = tryValidate(config) { ensurePast(Year.of(2026)) }
                    result.shouldBeFailure()
                }
            }

            context("ensureMin") {
                val minYear = Year.of(2020)

                test("success with equal value") {
                    val result = tryValidate { ensureMin(minYear, minYear) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { ensureMin(Year.of(2021), minYear) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { ensureMin(Year.of(2019), minYear) }
                    result.shouldBeFailure()
                }
            }

            context("ensureMax") {
                val maxYear = Year.of(2030)

                test("success with equal value") {
                    val result = tryValidate { ensureMax(maxYear, maxYear) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { ensureMax(Year.of(2029), maxYear) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { ensureMax(Year.of(2031), maxYear) }
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
                    val result = tryValidate(config) { ensureFuture(YearMonth.of(2025, 2)) }
                    result.shouldBeSuccess()
                }

                test("failure with ensurePast value") {
                    val result = tryValidate(config) { ensureFuture(YearMonth.of(2024, 12)) }
                    result.shouldBeFailure()
                }
            }

            context("ensurePast") {
                test("success") {
                    val result = tryValidate(config) { ensurePast(YearMonth.of(2024, 12)) }
                    result.shouldBeSuccess()
                }

                test("failure with ensureFuture value") {
                    val result = tryValidate(config) { ensurePast(YearMonth.of(2025, 2)) }
                    result.shouldBeFailure()
                }
            }

            context("ensureMin") {
                val minYearMonth = YearMonth.of(2024, 1)

                test("success with equal value") {
                    val result = tryValidate { ensureMin(minYearMonth, minYearMonth) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { ensureMin(YearMonth.of(2024, 2), minYearMonth) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { ensureMin(YearMonth.of(2023, 12), minYearMonth) }
                    result.shouldBeFailure()
                }
            }

            context("ensureMax") {
                val maxYearMonth = YearMonth.of(2025, 12)

                test("success with equal value") {
                    val result = tryValidate { ensureMax(maxYearMonth, maxYearMonth) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { ensureMax(YearMonth.of(2025, 11), maxYearMonth) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { ensureMax(YearMonth.of(2026, 1), maxYearMonth) }
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
                    val result = tryValidate(config) { ensureFuture(zonedDateTime.plusHours(1)) }
                    result.shouldBeSuccess()
                }

                test("failure with ensurePast value") {
                    val result = tryValidate(config) { ensureFuture(zonedDateTime.minusHours(1)) }
                    result.shouldBeFailure()
                }
            }

            context("ensurePast") {
                test("success") {
                    val result = tryValidate(config) { ensurePast(zonedDateTime.minusHours(1)) }
                    result.shouldBeSuccess()
                }

                test("failure with ensureFuture value") {
                    val result = tryValidate(config) { ensurePast(zonedDateTime.plusHours(1)) }
                    result.shouldBeFailure()
                }
            }

            context("ensureMin") {
                val minZonedDateTime = ZonedDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)

                test("success with equal value") {
                    val result = tryValidate { ensureMin(minZonedDateTime, minZonedDateTime) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { ensureMin(minZonedDateTime.plusHours(1), minZonedDateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { ensureMin(minZonedDateTime.minusHours(1), minZonedDateTime) }
                    result.shouldBeFailure()
                }
            }

            context("ensureMax") {
                val maxZonedDateTime = ZonedDateTime.of(2025, 12, 31, 23, 59, 59, 0, ZoneOffset.UTC)

                test("success with equal value") {
                    val result = tryValidate { ensureMax(maxZonedDateTime, maxZonedDateTime) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { ensureMax(maxZonedDateTime.minusHours(1), maxZonedDateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { ensureMax(maxZonedDateTime.plusHours(1), maxZonedDateTime) }
                    result.shouldBeFailure()
                }
            }
        }
    })
