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
                    val result = tryValidate(config) { future(date.plusDays(1)) }
                    result.shouldBeSuccess()
                }

                test("failure with present value") {
                    val result = tryValidate(config) { future(date) }
                    result.shouldBeFailure()
                }

                test("failure with past value") {
                    val result = tryValidate(config) { future(date.minusDays(1)) }
                    result.shouldBeFailure()
                }
            }

            context("futureOrPresent") {
                test("success with future value") {
                    val result = tryValidate(config) { futureOrPresent(date.plusDays(1)) }
                    result.shouldBeSuccess()
                }

                test("success with present value") {
                    val result = tryValidate(config) { futureOrPresent(date) }
                    result.shouldBeSuccess()
                }

                test("failure with past value") {
                    val result = tryValidate(config) { futureOrPresent(date.minusDays(1)) }
                    result.shouldBeFailure()
                }
            }

            context("past") {
                test("success") {
                    val result = tryValidate(config) { past(date.minusDays(1)) }
                    result.shouldBeSuccess()
                }

                test("failure with present value") {
                    val result = tryValidate(config) { past(date) }
                    result.shouldBeFailure()
                }

                test("failure with future value") {
                    val result = tryValidate(config) { past(date.plusDays(1)) }
                    result.shouldBeFailure()
                }
            }

            context("pastOrPresent") {
                test("success with past value") {
                    val result = tryValidate(config) { pastOrPresent(date.minusDays(1)) }
                    result.shouldBeSuccess()
                }

                test("success with present value") {
                    val result = tryValidate(config) { pastOrPresent(date) }
                    result.shouldBeSuccess()
                }

                test("failure with future value") {
                    val result = tryValidate(config) { pastOrPresent(date.plusDays(1)) }
                    result.shouldBeFailure()
                }
            }

            context("min") {
                val minDate = LocalDate.of(2025, 1, 1)

                test("success with equal value") {
                    val result = tryValidate { min(minDate, minDate) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { min(minDate.plusDays(1), minDate) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { min(minDate.minusDays(1), minDate) }
                    result.shouldBeFailure()
                }
            }

            context("max") {
                val maxDate = LocalDate.of(2025, 12, 31)

                test("success with equal value") {
                    val result = tryValidate { max(maxDate, maxDate) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { max(maxDate.minusDays(1), maxDate) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { max(maxDate.plusDays(1), maxDate) }
                    result.shouldBeFailure()
                }
            }

            context("gt") {
                val date = LocalDate.of(2025, 6, 15)

                test("success") {
                    val result = tryValidate { gt(date.plusDays(1), date) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { gt(date, date) }
                    result.shouldBeFailure()
                }

                test("failure with smaller value") {
                    val result = tryValidate { gt(date.minusDays(1), date) }
                    result.shouldBeFailure()
                }
            }

            context("gte") {
                val date = LocalDate.of(2025, 6, 15)

                test("success with greater value") {
                    val result = tryValidate { gte(date.plusDays(1), date) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { gte(date, date) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { gte(date.minusDays(1), date) }
                    result.shouldBeFailure()
                }
            }

            context("lt") {
                val date = LocalDate.of(2025, 6, 15)

                test("success") {
                    val result = tryValidate { lt(date.minusDays(1), date) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { lt(date, date) }
                    result.shouldBeFailure()
                }

                test("failure with greater value") {
                    val result = tryValidate { lt(date.plusDays(1), date) }
                    result.shouldBeFailure()
                }
            }

            context("lte") {
                val date = LocalDate.of(2025, 6, 15)

                test("success with smaller value") {
                    val result = tryValidate { lte(date.minusDays(1), date) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { lte(date, date) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { lte(date.plusDays(1), date) }
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
                    val result = tryValidate(config) { future(time.plusHours(1)) }
                    result.shouldBeSuccess()
                }

                test("failure with present value") {
                    val result = tryValidate(config) { future(time) }
                    result.shouldBeFailure()
                }

                test("failure with past value") {
                    val result = tryValidate(config) { future(time.minusHours(1)) }
                    result.shouldBeFailure()
                }
            }

            context("futureOrPresent") {
                test("success with future value") {
                    val result = tryValidate(config) { futureOrPresent(time.plusHours(1)) }
                    result.shouldBeSuccess()
                }

                test("success with present value") {
                    val result = tryValidate(config) { futureOrPresent(time) }
                    result.shouldBeSuccess()
                }

                test("failure with past value") {
                    val result = tryValidate(config) { futureOrPresent(time.minusHours(1)) }
                    result.shouldBeFailure()
                }
            }

            context("past") {
                test("success") {
                    val result = tryValidate(config) { past(time.minusHours(1)) }
                    result.shouldBeSuccess()
                }

                test("failure with present value") {
                    val result = tryValidate(config) { past(time) }
                    result.shouldBeFailure()
                }

                test("failure with future value") {
                    val result = tryValidate(config) { past(time.plusHours(1)) }
                    result.shouldBeFailure()
                }
            }

            context("pastOrPresent") {
                test("success with past value") {
                    val result = tryValidate(config) { pastOrPresent(time.minusHours(1)) }
                    result.shouldBeSuccess()
                }

                test("success with present value") {
                    val result = tryValidate(config) { pastOrPresent(time) }
                    result.shouldBeSuccess()
                }

                test("failure with future value") {
                    val result = tryValidate(config) { pastOrPresent(time.plusHours(1)) }
                    result.shouldBeFailure()
                }
            }

            context("min") {
                val minTime = LocalTime.of(9, 0, 0)

                test("success with equal value") {
                    val result = tryValidate { min(minTime, minTime) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { min(minTime.plusHours(1), minTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { min(minTime.minusHours(1), minTime) }
                    result.shouldBeFailure()
                }
            }

            context("max") {
                val maxTime = LocalTime.of(17, 0, 0)

                test("success with equal value") {
                    val result = tryValidate { max(maxTime, maxTime) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { max(maxTime.minusHours(1), maxTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { max(maxTime.plusHours(1), maxTime) }
                    result.shouldBeFailure()
                }
            }

            context("gt") {
                val time = LocalTime.of(12, 0, 0)

                test("success") {
                    val result = tryValidate { gt(time.plusHours(1), time) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { gt(time, time) }
                    result.shouldBeFailure()
                }

                test("failure with smaller value") {
                    val result = tryValidate { gt(time.minusHours(1), time) }
                    result.shouldBeFailure()
                }
            }

            context("gte") {
                val time = LocalTime.of(12, 0, 0)

                test("success with greater value") {
                    val result = tryValidate { gte(time.plusHours(1), time) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { gte(time, time) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { gte(time.minusHours(1), time) }
                    result.shouldBeFailure()
                }
            }

            context("lt") {
                val time = LocalTime.of(12, 0, 0)

                test("success") {
                    val result = tryValidate { lt(time.minusHours(1), time) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { lt(time, time) }
                    result.shouldBeFailure()
                }

                test("failure with greater value") {
                    val result = tryValidate { lt(time.plusHours(1), time) }
                    result.shouldBeFailure()
                }
            }

            context("lte") {
                val time = LocalTime.of(12, 0, 0)

                test("success with smaller value") {
                    val result = tryValidate { lte(time.minusHours(1), time) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { lte(time, time) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { lte(time.plusHours(1), time) }
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
                    val result = tryValidate(config) { future(dateTime.plusHours(1)) }
                    result.shouldBeSuccess()
                }

                test("failure with present value") {
                    val result = tryValidate(config) { future(dateTime) }
                    result.shouldBeFailure()
                }

                test("failure with past value") {
                    val result = tryValidate(config) { future(dateTime.minusHours(1)) }
                    result.shouldBeFailure()
                }
            }

            context("futureOrPresent") {
                test("success with future value") {
                    val result = tryValidate(config) { futureOrPresent(dateTime.plusHours(1)) }
                    result.shouldBeSuccess()
                }

                test("success with present value") {
                    val result = tryValidate(config) { futureOrPresent(dateTime) }
                    result.shouldBeSuccess()
                }

                test("failure with past value") {
                    val result = tryValidate(config) { futureOrPresent(dateTime.minusHours(1)) }
                    result.shouldBeFailure()
                }
            }

            context("past") {
                test("success") {
                    val result = tryValidate(config) { past(dateTime.minusHours(1)) }
                    result.shouldBeSuccess()
                }

                test("failure with present value") {
                    val result = tryValidate(config) { past(dateTime) }
                    result.shouldBeFailure()
                }

                test("failure with future value") {
                    val result = tryValidate(config) { past(dateTime.plusHours(1)) }
                    result.shouldBeFailure()
                }
            }

            context("pastOrPresent") {
                test("success with past value") {
                    val result = tryValidate(config) { pastOrPresent(dateTime.minusHours(1)) }
                    result.shouldBeSuccess()
                }

                test("success with present value") {
                    val result = tryValidate(config) { pastOrPresent(dateTime) }
                    result.shouldBeSuccess()
                }

                test("failure with future value") {
                    val result = tryValidate(config) { pastOrPresent(dateTime.plusHours(1)) }
                    result.shouldBeFailure()
                }
            }

            context("min") {
                val minDateTime = LocalDateTime.of(2025, 1, 1, 12, 0, 0)

                test("success with equal value") {
                    val result = tryValidate { min(minDateTime, minDateTime) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { min(minDateTime.plusHours(1), minDateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { min(minDateTime.minusHours(1), minDateTime) }
                    result.shouldBeFailure()
                }
            }

            context("max") {
                val maxDateTime = LocalDateTime.of(2025, 12, 31, 23, 59, 59)

                test("success with equal value") {
                    val result = tryValidate { max(maxDateTime, maxDateTime) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { max(maxDateTime.minusHours(1), maxDateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { max(maxDateTime.plusHours(1), maxDateTime) }
                    result.shouldBeFailure()
                }
            }

            context("gt") {
                val dateTime = LocalDateTime.of(2025, 6, 15, 12, 0, 0)

                test("success") {
                    val result = tryValidate { gt(dateTime.plusHours(1), dateTime) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { gt(dateTime, dateTime) }
                    result.shouldBeFailure()
                }

                test("failure with smaller value") {
                    val result = tryValidate { gt(dateTime.minusHours(1), dateTime) }
                    result.shouldBeFailure()
                }
            }

            context("gte") {
                val dateTime = LocalDateTime.of(2025, 6, 15, 12, 0, 0)

                test("success with greater value") {
                    val result = tryValidate { gte(dateTime.plusHours(1), dateTime) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { gte(dateTime, dateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { gte(dateTime.minusHours(1), dateTime) }
                    result.shouldBeFailure()
                }
            }

            context("lt") {
                val dateTime = LocalDateTime.of(2025, 6, 15, 12, 0, 0)

                test("success") {
                    val result = tryValidate { lt(dateTime.minusHours(1), dateTime) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { lt(dateTime, dateTime) }
                    result.shouldBeFailure()
                }

                test("failure with greater value") {
                    val result = tryValidate { lt(dateTime.plusHours(1), dateTime) }
                    result.shouldBeFailure()
                }
            }

            context("lte") {
                val dateTime = LocalDateTime.of(2025, 6, 15, 12, 0, 0)

                test("success with smaller value") {
                    val result = tryValidate { lte(dateTime.minusHours(1), dateTime) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { lte(dateTime, dateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { lte(dateTime.plusHours(1), dateTime) }
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
                    val result = tryValidate(config) { future(instant.plusSeconds(3600)) }
                    result.shouldBeSuccess()
                }

                test("failure with past value") {
                    val result = tryValidate(config) { future(instant.minusSeconds(3600)) }
                    result.shouldBeFailure()
                }
            }

            context("past") {
                test("success") {
                    val result = tryValidate(config) { past(instant.minusSeconds(3600)) }
                    result.shouldBeSuccess()
                }

                test("failure with future value") {
                    val result = tryValidate(config) { past(instant.plusSeconds(3600)) }
                    result.shouldBeFailure()
                }
            }

            context("min") {
                val minInstant = Instant.parse("2025-01-01T00:00:00Z")

                test("success with equal value") {
                    val result = tryValidate { min(minInstant, minInstant) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { min(minInstant.plusSeconds(3600), minInstant) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { min(minInstant.minusSeconds(3600), minInstant) }
                    result.shouldBeFailure()
                }
            }

            context("max") {
                val maxInstant = Instant.parse("2025-12-31T23:59:59Z")

                test("success with equal value") {
                    val result = tryValidate { max(maxInstant, maxInstant) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { max(maxInstant.minusSeconds(3600), maxInstant) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { max(maxInstant.plusSeconds(3600), maxInstant) }
                    result.shouldBeFailure()
                }
            }
        }

        context("MonthDay") {
            context("min") {
                val minMonthDay = MonthDay.of(3, 1)

                test("success with equal value") {
                    val result = tryValidate { min(minMonthDay, minMonthDay) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { min(MonthDay.of(3, 2), minMonthDay) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { min(MonthDay.of(2, 28), minMonthDay) }
                    result.shouldBeFailure()
                }
            }

            context("max") {
                val maxMonthDay = MonthDay.of(10, 31)

                test("success with equal value") {
                    val result = tryValidate { max(maxMonthDay, maxMonthDay) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { max(MonthDay.of(10, 30), maxMonthDay) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { max(MonthDay.of(11, 1), maxMonthDay) }
                    result.shouldBeFailure()
                }
            }

            context("gt") {
                val monthDay = MonthDay.of(6, 15)

                test("success") {
                    val result = tryValidate { gt(MonthDay.of(6, 16), monthDay) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { gt(monthDay, monthDay) }
                    result.shouldBeFailure()
                }

                test("failure with smaller value") {
                    val result = tryValidate { gt(MonthDay.of(6, 14), monthDay) }
                    result.shouldBeFailure()
                }
            }

            context("gte") {
                val monthDay = MonthDay.of(6, 15)

                test("success with greater value") {
                    val result = tryValidate { gte(MonthDay.of(6, 16), monthDay) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { gte(monthDay, monthDay) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { gte(MonthDay.of(6, 14), monthDay) }
                    result.shouldBeFailure()
                }
            }

            context("lt") {
                val monthDay = MonthDay.of(6, 15)

                test("success") {
                    val result = tryValidate { lt(MonthDay.of(6, 14), monthDay) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { lt(monthDay, monthDay) }
                    result.shouldBeFailure()
                }

                test("failure with greater value") {
                    val result = tryValidate { lt(MonthDay.of(6, 16), monthDay) }
                    result.shouldBeFailure()
                }
            }

            context("lte") {
                val monthDay = MonthDay.of(6, 15)

                test("success with smaller value") {
                    val result = tryValidate { lte(MonthDay.of(6, 14), monthDay) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { lte(monthDay, monthDay) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { lte(MonthDay.of(6, 16), monthDay) }
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
                    val result = tryValidate(config) { future(offsetDateTime.plusHours(1)) }
                    result.shouldBeSuccess()
                }

                test("failure with past value") {
                    val result = tryValidate(config) { future(offsetDateTime.minusHours(1)) }
                    result.shouldBeFailure()
                }
            }

            context("past") {
                test("success") {
                    val result = tryValidate(config) { past(offsetDateTime.minusHours(1)) }
                    result.shouldBeSuccess()
                }

                test("failure with future value") {
                    val result = tryValidate(config) { past(offsetDateTime.plusHours(1)) }
                    result.shouldBeFailure()
                }
            }

            context("min") {
                val minOffsetDateTime = OffsetDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)

                test("success with equal value") {
                    val result = tryValidate { min(minOffsetDateTime, minOffsetDateTime) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { min(minOffsetDateTime.plusHours(1), minOffsetDateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { min(minOffsetDateTime.minusHours(1), minOffsetDateTime) }
                    result.shouldBeFailure()
                }
            }

            context("max") {
                val maxOffsetDateTime = OffsetDateTime.of(2025, 12, 31, 23, 59, 59, 0, ZoneOffset.UTC)

                test("success with equal value") {
                    val result = tryValidate { max(maxOffsetDateTime, maxOffsetDateTime) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { max(maxOffsetDateTime.minusHours(1), maxOffsetDateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { max(maxOffsetDateTime.plusHours(1), maxOffsetDateTime) }
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
                    val result = tryValidate(config) { future(offsetTime.plusHours(1)) }
                    result.shouldBeSuccess()
                }

                test("failure with past value") {
                    val result = tryValidate(config) { future(offsetTime.minusHours(1)) }
                    result.shouldBeFailure()
                }
            }

            context("past") {
                test("success") {
                    val result = tryValidate(config) { past(offsetTime.minusHours(1)) }
                    result.shouldBeSuccess()
                }

                test("failure with future value") {
                    val result = tryValidate(config) { past(offsetTime.plusHours(1)) }
                    result.shouldBeFailure()
                }
            }

            context("min") {
                val minOffsetTime = OffsetTime.of(9, 0, 0, 0, ZoneOffset.UTC)

                test("success with equal value") {
                    val result = tryValidate { min(minOffsetTime, minOffsetTime) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { min(minOffsetTime.plusHours(1), minOffsetTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { min(minOffsetTime.minusHours(1), minOffsetTime) }
                    result.shouldBeFailure()
                }
            }

            context("max") {
                val maxOffsetTime = OffsetTime.of(17, 0, 0, 0, ZoneOffset.UTC)

                test("success with equal value") {
                    val result = tryValidate { max(maxOffsetTime, maxOffsetTime) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { max(maxOffsetTime.minusHours(1), maxOffsetTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { max(maxOffsetTime.plusHours(1), maxOffsetTime) }
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
                    val result = tryValidate(config) { future(Year.of(2026)) }
                    result.shouldBeSuccess()
                }

                test("failure with past value") {
                    val result = tryValidate(config) { future(Year.of(2024)) }
                    result.shouldBeFailure()
                }
            }

            context("past") {
                test("success") {
                    val result = tryValidate(config) { past(Year.of(2024)) }
                    result.shouldBeSuccess()
                }

                test("failure with future value") {
                    val result = tryValidate(config) { past(Year.of(2026)) }
                    result.shouldBeFailure()
                }
            }

            context("min") {
                val minYear = Year.of(2020)

                test("success with equal value") {
                    val result = tryValidate { min(minYear, minYear) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { min(Year.of(2021), minYear) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { min(Year.of(2019), minYear) }
                    result.shouldBeFailure()
                }
            }

            context("max") {
                val maxYear = Year.of(2030)

                test("success with equal value") {
                    val result = tryValidate { max(maxYear, maxYear) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { max(Year.of(2029), maxYear) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { max(Year.of(2031), maxYear) }
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
                    val result = tryValidate(config) { future(YearMonth.of(2025, 2)) }
                    result.shouldBeSuccess()
                }

                test("failure with past value") {
                    val result = tryValidate(config) { future(YearMonth.of(2024, 12)) }
                    result.shouldBeFailure()
                }
            }

            context("past") {
                test("success") {
                    val result = tryValidate(config) { past(YearMonth.of(2024, 12)) }
                    result.shouldBeSuccess()
                }

                test("failure with future value") {
                    val result = tryValidate(config) { past(YearMonth.of(2025, 2)) }
                    result.shouldBeFailure()
                }
            }

            context("min") {
                val minYearMonth = YearMonth.of(2024, 1)

                test("success with equal value") {
                    val result = tryValidate { min(minYearMonth, minYearMonth) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { min(YearMonth.of(2024, 2), minYearMonth) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { min(YearMonth.of(2023, 12), minYearMonth) }
                    result.shouldBeFailure()
                }
            }

            context("max") {
                val maxYearMonth = YearMonth.of(2025, 12)

                test("success with equal value") {
                    val result = tryValidate { max(maxYearMonth, maxYearMonth) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { max(YearMonth.of(2025, 11), maxYearMonth) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { max(YearMonth.of(2026, 1), maxYearMonth) }
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
                    val result = tryValidate(config) { future(zonedDateTime.plusHours(1)) }
                    result.shouldBeSuccess()
                }

                test("failure with past value") {
                    val result = tryValidate(config) { future(zonedDateTime.minusHours(1)) }
                    result.shouldBeFailure()
                }
            }

            context("past") {
                test("success") {
                    val result = tryValidate(config) { past(zonedDateTime.minusHours(1)) }
                    result.shouldBeSuccess()
                }

                test("failure with future value") {
                    val result = tryValidate(config) { past(zonedDateTime.plusHours(1)) }
                    result.shouldBeFailure()
                }
            }

            context("min") {
                val minZonedDateTime = ZonedDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)

                test("success with equal value") {
                    val result = tryValidate { min(minZonedDateTime, minZonedDateTime) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { min(minZonedDateTime.plusHours(1), minZonedDateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { min(minZonedDateTime.minusHours(1), minZonedDateTime) }
                    result.shouldBeFailure()
                }
            }

            context("max") {
                val maxZonedDateTime = ZonedDateTime.of(2025, 12, 31, 23, 59, 59, 0, ZoneOffset.UTC)

                test("success with equal value") {
                    val result = tryValidate { max(maxZonedDateTime, maxZonedDateTime) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { max(maxZonedDateTime.minusHours(1), maxZonedDateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { max(maxZonedDateTime.plusHours(1), maxZonedDateTime) }
                    result.shouldBeFailure()
                }
            }
        }
    })
