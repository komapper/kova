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

class TemporalValidatorTest :
    FunSpec({

        context("LocalDate") {
            val date = LocalDate.of(2025, 1, 1)
            val zone = ZoneOffset.UTC
            val instant = date.atStartOfDay(zone).toInstant()
            val clock = Clock.fixed(instant, zone)
            val config = ValidationConfig(clock = clock)

            context("future") {
                test("success") {
                    val result = tryValidate(config) { date.plusDays(1).future() }
                    result.shouldBeSuccess()
                }

                test("failure with present value") {
                    val result = tryValidate(config) { date.future() }
                    result.shouldBeFailure()
                }

                test("failure with past value") {
                    val result = tryValidate(config) { date.minusDays(1).future() }
                    result.shouldBeFailure()
                }
            }

            context("futureOrPresent") {
                test("success with future value") {
                    val result = tryValidate(config) { date.plusDays(1).futureOrPresent() }
                    result.shouldBeSuccess()
                }

                test("success with present value") {
                    val result = tryValidate(config) { date.futureOrPresent() }
                    result.shouldBeSuccess()
                }

                test("failure with past value") {
                    val result = tryValidate(config) { date.minusDays(1).futureOrPresent() }
                    result.shouldBeFailure()
                }
            }

            context("past") {
                test("success") {
                    val result = tryValidate(config) { date.minusDays(1).past() }
                    result.shouldBeSuccess()
                }

                test("failure with present value") {
                    val result = tryValidate(config) { date.past() }
                    result.shouldBeFailure()
                }

                test("failure with future value") {
                    val result = tryValidate(config) { date.plusDays(1).past() }
                    result.shouldBeFailure()
                }
            }

            context("pastOrPresent") {
                test("success with past value") {
                    val result = tryValidate(config) { date.minusDays(1).pastOrPresent() }
                    result.shouldBeSuccess()
                }

                test("success with present value") {
                    val result = tryValidate(config) { date.pastOrPresent() }
                    result.shouldBeSuccess()
                }

                test("failure with future value") {
                    val result = tryValidate(config) { date.plusDays(1).pastOrPresent() }
                    result.shouldBeFailure()
                }
            }

            context("min") {
                val minDate = LocalDate.of(2025, 1, 1)

                test("success with equal value") {
                    val result = tryValidate { minDate.min(minDate) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { minDate.plusDays(1).min(minDate) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { minDate.minusDays(1).min(minDate) }
                    result.shouldBeFailure()
                }
            }

            context("max") {
                val maxDate = LocalDate.of(2025, 12, 31)

                test("success with equal value") {
                    val result = tryValidate { maxDate.max(maxDate) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { maxDate.minusDays(1).max(maxDate) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { maxDate.plusDays(1).max(maxDate) }
                    result.shouldBeFailure()
                }
            }

            context("gt") {
                val date = LocalDate.of(2025, 6, 15)

                test("success") {
                    val result = tryValidate { date.plusDays(1).gt(date) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { date.gt(date) }
                    result.shouldBeFailure()
                }

                test("failure with smaller value") {
                    val result = tryValidate { date.minusDays(1).gt(date) }
                    result.shouldBeFailure()
                }
            }

            context("gte") {
                val date = LocalDate.of(2025, 6, 15)

                test("success with greater value") {
                    val result = tryValidate { date.plusDays(1).gte(date) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { date.gte(date) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { date.minusDays(1).gte(date) }
                    result.shouldBeFailure()
                }
            }

            context("lt") {
                val date = LocalDate.of(2025, 6, 15)

                test("success") {
                    val result = tryValidate { date.minusDays(1).lt(date) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { date.lt(date) }
                    result.shouldBeFailure()
                }

                test("failure with greater value") {
                    val result = tryValidate { date.plusDays(1).lt(date) }
                    result.shouldBeFailure()
                }
            }

            context("lte") {
                val date = LocalDate.of(2025, 6, 15)

                test("success with smaller value") {
                    val result = tryValidate { date.minusDays(1).lte(date) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { date.lte(date) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { date.plusDays(1).lte(date) }
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

            context("future") {
                test("success") {
                    val result = tryValidate(config) { time.plusHours(1).future() }
                    result.shouldBeSuccess()
                }

                test("failure with present value") {
                    val result = tryValidate(config) { time.future() }
                    result.shouldBeFailure()
                }

                test("failure with past value") {
                    val result = tryValidate(config) { time.minusHours(1).future() }
                    result.shouldBeFailure()
                }
            }

            context("futureOrPresent") {
                test("success with future value") {
                    val result = tryValidate(config) { time.plusHours(1).futureOrPresent() }
                    result.shouldBeSuccess()
                }

                test("success with present value") {
                    val result = tryValidate(config) { time.futureOrPresent() }
                    result.shouldBeSuccess()
                }

                test("failure with past value") {
                    val result = tryValidate(config) { time.minusHours(1).futureOrPresent() }
                    result.shouldBeFailure()
                }
            }

            context("past") {
                test("success") {
                    val result = tryValidate(config) { time.minusHours(1).past() }
                    result.shouldBeSuccess()
                }

                test("failure with present value") {
                    val result = tryValidate(config) { time.past() }
                    result.shouldBeFailure()
                }

                test("failure with future value") {
                    val result = tryValidate(config) { time.plusHours(1).past() }
                    result.shouldBeFailure()
                }
            }

            context("pastOrPresent") {
                test("success with past value") {
                    val result = tryValidate(config) { time.minusHours(1).pastOrPresent() }
                    result.shouldBeSuccess()
                }

                test("success with present value") {
                    val result = tryValidate(config) { time.pastOrPresent() }
                    result.shouldBeSuccess()
                }

                test("failure with future value") {
                    val result = tryValidate(config) { time.plusHours(1).pastOrPresent() }
                    result.shouldBeFailure()
                }
            }

            context("min") {
                val minTime = LocalTime.of(9, 0, 0)

                test("success with equal value") {
                    val result = tryValidate { minTime.min(minTime) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { minTime.plusHours(1).min(minTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { minTime.minusHours(1).min(minTime) }
                    result.shouldBeFailure()
                }
            }

            context("max") {
                val maxTime = LocalTime.of(17, 0, 0)

                test("success with equal value") {
                    val result = tryValidate { maxTime.max(maxTime) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { maxTime.minusHours(1).max(maxTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { maxTime.plusHours(1).max(maxTime) }
                    result.shouldBeFailure()
                }
            }

            context("gt") {
                val time = LocalTime.of(12, 0, 0)

                test("success") {
                    val result = tryValidate { time.plusHours(1).gt(time) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { time.gt(time) }
                    result.shouldBeFailure()
                }

                test("failure with smaller value") {
                    val result = tryValidate { time.minusHours(1).gt(time) }
                    result.shouldBeFailure()
                }
            }

            context("gte") {
                val time = LocalTime.of(12, 0, 0)

                test("success with greater value") {
                    val result = tryValidate { time.plusHours(1).gte(time) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { time.gte(time) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { time.minusHours(1).gte(time) }
                    result.shouldBeFailure()
                }
            }

            context("lt") {
                val time = LocalTime.of(12, 0, 0)

                test("success") {
                    val result = tryValidate { time.minusHours(1).lt(time) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { time.lt(time) }
                    result.shouldBeFailure()
                }

                test("failure with greater value") {
                    val result = tryValidate { time.plusHours(1).lt(time) }
                    result.shouldBeFailure()
                }
            }

            context("lte") {
                val time = LocalTime.of(12, 0, 0)

                test("success with smaller value") {
                    val result = tryValidate { time.minusHours(1).lte(time) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { time.lte(time) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { time.plusHours(1).lte(time) }
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

            context("future") {
                test("success") {
                    val result = tryValidate(config) { dateTime.plusHours(1).future() }
                    result.shouldBeSuccess()
                }

                test("failure with present value") {
                    val result = tryValidate(config) { dateTime.future() }
                    result.shouldBeFailure()
                }

                test("failure with past value") {
                    val result = tryValidate(config) { dateTime.minusHours(1).future() }
                    result.shouldBeFailure()
                }
            }

            context("futureOrPresent") {
                test("success with future value") {
                    val result = tryValidate(config) { dateTime.plusHours(1).futureOrPresent() }
                    result.shouldBeSuccess()
                }

                test("success with present value") {
                    val result = tryValidate(config) { dateTime.futureOrPresent() }
                    result.shouldBeSuccess()
                }

                test("failure with past value") {
                    val result = tryValidate(config) { dateTime.minusHours(1).futureOrPresent() }
                    result.shouldBeFailure()
                }
            }

            context("past") {
                test("success") {
                    val result = tryValidate(config) { dateTime.minusHours(1).past() }
                    result.shouldBeSuccess()
                }

                test("failure with present value") {
                    val result = tryValidate(config) { dateTime.past() }
                    result.shouldBeFailure()
                }

                test("failure with future value") {
                    val result = tryValidate(config) { dateTime.plusHours(1).past() }
                    result.shouldBeFailure()
                }
            }

            context("pastOrPresent") {
                test("success with past value") {
                    val result = tryValidate(config) { dateTime.minusHours(1).pastOrPresent() }
                    result.shouldBeSuccess()
                }

                test("success with present value") {
                    val result = tryValidate(config) { dateTime.pastOrPresent() }
                    result.shouldBeSuccess()
                }

                test("failure with future value") {
                    val result = tryValidate(config) { dateTime.plusHours(1).pastOrPresent() }
                    result.shouldBeFailure()
                }
            }

            context("min") {
                val minDateTime = LocalDateTime.of(2025, 1, 1, 12, 0, 0)

                test("success with equal value") {
                    val result = tryValidate { minDateTime.min(minDateTime) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { minDateTime.plusHours(1).min(minDateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { minDateTime.minusHours(1).min(minDateTime) }
                    result.shouldBeFailure()
                }
            }

            context("max") {
                val maxDateTime = LocalDateTime.of(2025, 12, 31, 23, 59, 59)

                test("success with equal value") {
                    val result = tryValidate { maxDateTime.max(maxDateTime) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { maxDateTime.minusHours(1).max(maxDateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { maxDateTime.plusHours(1).max(maxDateTime) }
                    result.shouldBeFailure()
                }
            }

            context("gt") {
                val dateTime = LocalDateTime.of(2025, 6, 15, 12, 0, 0)

                test("success") {
                    val result = tryValidate { dateTime.plusHours(1).gt(dateTime) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { dateTime.gt(dateTime) }
                    result.shouldBeFailure()
                }

                test("failure with smaller value") {
                    val result = tryValidate { dateTime.minusHours(1).gt(dateTime) }
                    result.shouldBeFailure()
                }
            }

            context("gte") {
                val dateTime = LocalDateTime.of(2025, 6, 15, 12, 0, 0)

                test("success with greater value") {
                    val result = tryValidate { dateTime.plusHours(1).gte(dateTime) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { dateTime.gte(dateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { dateTime.minusHours(1).gte(dateTime) }
                    result.shouldBeFailure()
                }
            }

            context("lt") {
                val dateTime = LocalDateTime.of(2025, 6, 15, 12, 0, 0)

                test("success") {
                    val result = tryValidate { dateTime.minusHours(1).lt(dateTime) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { dateTime.lt(dateTime) }
                    result.shouldBeFailure()
                }

                test("failure with greater value") {
                    val result = tryValidate { dateTime.plusHours(1).lt(dateTime) }
                    result.shouldBeFailure()
                }
            }

            context("lte") {
                val dateTime = LocalDateTime.of(2025, 6, 15, 12, 0, 0)

                test("success with smaller value") {
                    val result = tryValidate { dateTime.minusHours(1).lte(dateTime) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { dateTime.lte(dateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { dateTime.plusHours(1).lte(dateTime) }
                    result.shouldBeFailure()
                }
            }
        }

        context("Instant") {
            val instant = Instant.parse("2025-01-01T12:00:00Z")
            val clock = Clock.fixed(instant, ZoneOffset.UTC)
            val config = ValidationConfig(clock = clock)

            context("future") {
                test("success") {
                    val result = tryValidate(config) { instant.plusSeconds(3600).future() }
                    result.shouldBeSuccess()
                }

                test("failure with past value") {
                    val result = tryValidate(config) { instant.minusSeconds(3600).future() }
                    result.shouldBeFailure()
                }
            }

            context("past") {
                test("success") {
                    val result = tryValidate(config) { instant.minusSeconds(3600).past() }
                    result.shouldBeSuccess()
                }

                test("failure with future value") {
                    val result = tryValidate(config) { instant.plusSeconds(3600).past() }
                    result.shouldBeFailure()
                }
            }

            context("min") {
                val minInstant = Instant.parse("2025-01-01T00:00:00Z")

                test("success with equal value") {
                    val result = tryValidate { minInstant.min(minInstant) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { minInstant.plusSeconds(3600).min(minInstant) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { minInstant.minusSeconds(3600).min(minInstant) }
                    result.shouldBeFailure()
                }
            }

            context("max") {
                val maxInstant = Instant.parse("2025-12-31T23:59:59Z")

                test("success with equal value") {
                    val result = tryValidate { maxInstant.max(maxInstant) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { maxInstant.minusSeconds(3600).max(maxInstant) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { maxInstant.plusSeconds(3600).max(maxInstant) }
                    result.shouldBeFailure()
                }
            }
        }

        context("MonthDay") {
            context("min") {
                val minMonthDay = MonthDay.of(3, 1)

                test("success with equal value") {
                    val result = tryValidate { minMonthDay.min(minMonthDay) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { MonthDay.of(3, 2).min(minMonthDay) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { MonthDay.of(2, 28).min(minMonthDay) }
                    result.shouldBeFailure()
                }
            }

            context("max") {
                val maxMonthDay = MonthDay.of(10, 31)

                test("success with equal value") {
                    val result = tryValidate { maxMonthDay.max(maxMonthDay) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { MonthDay.of(10, 30).max(maxMonthDay) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { MonthDay.of(11, 1).max(maxMonthDay) }
                    result.shouldBeFailure()
                }
            }

            context("gt") {
                val monthDay = MonthDay.of(6, 15)

                test("success") {
                    val result = tryValidate { MonthDay.of(6, 16).gt(monthDay) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { monthDay.gt(monthDay) }
                    result.shouldBeFailure()
                }

                test("failure with smaller value") {
                    val result = tryValidate { MonthDay.of(6, 14).gt(monthDay) }
                    result.shouldBeFailure()
                }
            }

            context("gte") {
                val monthDay = MonthDay.of(6, 15)

                test("success with greater value") {
                    val result = tryValidate { MonthDay.of(6, 16).gte(monthDay) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { monthDay.gte(monthDay) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { MonthDay.of(6, 14).gte(monthDay) }
                    result.shouldBeFailure()
                }
            }

            context("lt") {
                val monthDay = MonthDay.of(6, 15)

                test("success") {
                    val result = tryValidate { MonthDay.of(6, 14).lt(monthDay) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { monthDay.lt(monthDay) }
                    result.shouldBeFailure()
                }

                test("failure with greater value") {
                    val result = tryValidate { MonthDay.of(6, 16).lt(monthDay) }
                    result.shouldBeFailure()
                }
            }

            context("lte") {
                val monthDay = MonthDay.of(6, 15)

                test("success with smaller value") {
                    val result = tryValidate { MonthDay.of(6, 14).lte(monthDay) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { monthDay.lte(monthDay) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { MonthDay.of(6, 16).lte(monthDay) }
                    result.shouldBeFailure()
                }
            }
        }

        context("OffsetDateTime") {
            val offsetDateTime = OffsetDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC)
            val clock = Clock.fixed(offsetDateTime.toInstant(), ZoneOffset.UTC)
            val config = ValidationConfig(clock = clock)

            context("future") {
                test("success") {
                    val result = tryValidate(config) { offsetDateTime.plusHours(1).future() }
                    result.shouldBeSuccess()
                }

                test("failure with past value") {
                    val result = tryValidate(config) { offsetDateTime.minusHours(1).future() }
                    result.shouldBeFailure()
                }
            }

            context("past") {
                test("success") {
                    val result = tryValidate(config) { offsetDateTime.minusHours(1).past() }
                    result.shouldBeSuccess()
                }

                test("failure with future value") {
                    val result = tryValidate(config) { offsetDateTime.plusHours(1).past() }
                    result.shouldBeFailure()
                }
            }

            context("min") {
                val minOffsetDateTime = OffsetDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)

                test("success with equal value") {
                    val result = tryValidate { minOffsetDateTime.min(minOffsetDateTime) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { minOffsetDateTime.plusHours(1).min(minOffsetDateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { minOffsetDateTime.minusHours(1).min(minOffsetDateTime) }
                    result.shouldBeFailure()
                }
            }

            context("max") {
                val maxOffsetDateTime = OffsetDateTime.of(2025, 12, 31, 23, 59, 59, 0, ZoneOffset.UTC)

                test("success with equal value") {
                    val result = tryValidate { maxOffsetDateTime.max(maxOffsetDateTime) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { maxOffsetDateTime.minusHours(1).max(maxOffsetDateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { maxOffsetDateTime.plusHours(1).max(maxOffsetDateTime) }
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

            context("future") {
                test("success") {
                    val result = tryValidate(config) { offsetTime.plusHours(1).future() }
                    result.shouldBeSuccess()
                }

                test("failure with past value") {
                    val result = tryValidate(config) { offsetTime.minusHours(1).future() }
                    result.shouldBeFailure()
                }
            }

            context("past") {
                test("success") {
                    val result = tryValidate(config) { offsetTime.minusHours(1).past() }
                    result.shouldBeSuccess()
                }

                test("failure with future value") {
                    val result = tryValidate(config) { offsetTime.plusHours(1).past() }
                    result.shouldBeFailure()
                }
            }

            context("min") {
                val minOffsetTime = OffsetTime.of(9, 0, 0, 0, ZoneOffset.UTC)

                test("success with equal value") {
                    val result = tryValidate { minOffsetTime.min(minOffsetTime) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { minOffsetTime.plusHours(1).min(minOffsetTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { minOffsetTime.minusHours(1).min(minOffsetTime) }
                    result.shouldBeFailure()
                }
            }

            context("max") {
                val maxOffsetTime = OffsetTime.of(17, 0, 0, 0, ZoneOffset.UTC)

                test("success with equal value") {
                    val result = tryValidate { maxOffsetTime.max(maxOffsetTime) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { maxOffsetTime.minusHours(1).max(maxOffsetTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { maxOffsetTime.plusHours(1).max(maxOffsetTime) }
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

            context("future") {
                test("success") {
                    val result = tryValidate(config) { Year.of(2026).future() }
                    result.shouldBeSuccess()
                }

                test("failure with past value") {
                    val result = tryValidate(config) { Year.of(2024).future() }
                    result.shouldBeFailure()
                }
            }

            context("past") {
                test("success") {
                    val result = tryValidate(config) { Year.of(2024).past() }
                    result.shouldBeSuccess()
                }

                test("failure with future value") {
                    val result = tryValidate(config) { Year.of(2026).past() }
                    result.shouldBeFailure()
                }
            }

            context("min") {
                val minYear = Year.of(2020)

                test("success with equal value") {
                    val result = tryValidate { minYear.min(minYear) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { Year.of(2021).min(minYear) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { Year.of(2019).min(minYear) }
                    result.shouldBeFailure()
                }
            }

            context("max") {
                val maxYear = Year.of(2030)

                test("success with equal value") {
                    val result = tryValidate { maxYear.max(maxYear) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { Year.of(2029).max(maxYear) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { Year.of(2031).max(maxYear) }
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

            context("future") {
                test("success") {
                    val result = tryValidate(config) { YearMonth.of(2025, 2).future() }
                    result.shouldBeSuccess()
                }

                test("failure with past value") {
                    val result = tryValidate(config) { YearMonth.of(2024, 12).future() }
                    result.shouldBeFailure()
                }
            }

            context("past") {
                test("success") {
                    val result = tryValidate(config) { YearMonth.of(2024, 12).past() }
                    result.shouldBeSuccess()
                }

                test("failure with future value") {
                    val result = tryValidate(config) { YearMonth.of(2025, 2).past() }
                    result.shouldBeFailure()
                }
            }

            context("min") {
                val minYearMonth = YearMonth.of(2024, 1)

                test("success with equal value") {
                    val result = tryValidate { minYearMonth.min(minYearMonth) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { YearMonth.of(2024, 2).min(minYearMonth) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { YearMonth.of(2023, 12).min(minYearMonth) }
                    result.shouldBeFailure()
                }
            }

            context("max") {
                val maxYearMonth = YearMonth.of(2025, 12)

                test("success with equal value") {
                    val result = tryValidate { maxYearMonth.max(maxYearMonth) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { YearMonth.of(2025, 11).max(maxYearMonth) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { YearMonth.of(2026, 1).max(maxYearMonth) }
                    result.shouldBeFailure()
                }
            }
        }

        context("ZonedDateTime") {
            val zonedDateTime = ZonedDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC)
            val clock = Clock.fixed(zonedDateTime.toInstant(), ZoneOffset.UTC)
            val config = ValidationConfig(clock = clock)

            context("future") {
                test("success") {
                    val result = tryValidate(config) { zonedDateTime.plusHours(1).future() }
                    result.shouldBeSuccess()
                }

                test("failure with past value") {
                    val result = tryValidate(config) { zonedDateTime.minusHours(1).future() }
                    result.shouldBeFailure()
                }
            }

            context("past") {
                test("success") {
                    val result = tryValidate(config) { zonedDateTime.minusHours(1).past() }
                    result.shouldBeSuccess()
                }

                test("failure with future value") {
                    val result = tryValidate(config) { zonedDateTime.plusHours(1).past() }
                    result.shouldBeFailure()
                }
            }

            context("min") {
                val minZonedDateTime = ZonedDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)

                test("success with equal value") {
                    val result = tryValidate { minZonedDateTime.min(minZonedDateTime) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { minZonedDateTime.plusHours(1).min(minZonedDateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { minZonedDateTime.minusHours(1).min(minZonedDateTime) }
                    result.shouldBeFailure()
                }
            }

            context("max") {
                val maxZonedDateTime = ZonedDateTime.of(2025, 12, 31, 23, 59, 59, 0, ZoneOffset.UTC)

                test("success with equal value") {
                    val result = tryValidate { maxZonedDateTime.max(maxZonedDateTime) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { maxZonedDateTime.minusHours(1).max(maxZonedDateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { maxZonedDateTime.plusHours(1).max(maxZonedDateTime) }
                    result.shouldBeFailure()
                }
            }
        }
    })
