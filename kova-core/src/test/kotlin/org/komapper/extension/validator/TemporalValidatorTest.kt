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

            context("minValue") {
                val minDate = LocalDate.of(2025, 1, 1)

                test("success with equal value") {
                    val result = tryValidate { minValue(minDate, minDate) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { minValue(minDate.plusDays(1), minDate) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { minValue(minDate.minusDays(1), minDate) }
                    result.shouldBeFailure()
                }
            }

            context("maxValue") {
                val maxDate = LocalDate.of(2025, 12, 31)

                test("success with equal value") {
                    val result = tryValidate { maxValue(maxDate, maxDate) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { maxValue(maxDate.minusDays(1), maxDate) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { maxValue(maxDate.plusDays(1), maxDate) }
                    result.shouldBeFailure()
                }
            }

            context("gtValue") {
                val date = LocalDate.of(2025, 6, 15)

                test("success") {
                    val result = tryValidate { gtValue(date.plusDays(1), date) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { gtValue(date, date) }
                    result.shouldBeFailure()
                }

                test("failure with smaller value") {
                    val result = tryValidate { gtValue(date.minusDays(1), date) }
                    result.shouldBeFailure()
                }
            }

            context("gteValue") {
                val date = LocalDate.of(2025, 6, 15)

                test("success with greater value") {
                    val result = tryValidate { gteValue(date.plusDays(1), date) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { gteValue(date, date) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { gteValue(date.minusDays(1), date) }
                    result.shouldBeFailure()
                }
            }

            context("ltValue") {
                val date = LocalDate.of(2025, 6, 15)

                test("success") {
                    val result = tryValidate { ltValue(date.minusDays(1), date) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { ltValue(date, date) }
                    result.shouldBeFailure()
                }

                test("failure with greater value") {
                    val result = tryValidate { ltValue(date.plusDays(1), date) }
                    result.shouldBeFailure()
                }
            }

            context("lteValue") {
                val date = LocalDate.of(2025, 6, 15)

                test("success with smaller value") {
                    val result = tryValidate { lteValue(date.minusDays(1), date) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { lteValue(date, date) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { lteValue(date.plusDays(1), date) }
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

            context("minValue") {
                val minTime = LocalTime.of(9, 0, 0)

                test("success with equal value") {
                    val result = tryValidate { minValue(minTime, minTime) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { minValue(minTime.plusHours(1), minTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { minValue(minTime.minusHours(1), minTime) }
                    result.shouldBeFailure()
                }
            }

            context("maxValue") {
                val maxTime = LocalTime.of(17, 0, 0)

                test("success with equal value") {
                    val result = tryValidate { maxValue(maxTime, maxTime) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { maxValue(maxTime.minusHours(1), maxTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { maxValue(maxTime.plusHours(1), maxTime) }
                    result.shouldBeFailure()
                }
            }

            context("gtValue") {
                val time = LocalTime.of(12, 0, 0)

                test("success") {
                    val result = tryValidate { gtValue(time.plusHours(1), time) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { gtValue(time, time) }
                    result.shouldBeFailure()
                }

                test("failure with smaller value") {
                    val result = tryValidate { gtValue(time.minusHours(1), time) }
                    result.shouldBeFailure()
                }
            }

            context("gteValue") {
                val time = LocalTime.of(12, 0, 0)

                test("success with greater value") {
                    val result = tryValidate { gteValue(time.plusHours(1), time) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { gteValue(time, time) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { gteValue(time.minusHours(1), time) }
                    result.shouldBeFailure()
                }
            }

            context("ltValue") {
                val time = LocalTime.of(12, 0, 0)

                test("success") {
                    val result = tryValidate { ltValue(time.minusHours(1), time) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { ltValue(time, time) }
                    result.shouldBeFailure()
                }

                test("failure with greater value") {
                    val result = tryValidate { ltValue(time.plusHours(1), time) }
                    result.shouldBeFailure()
                }
            }

            context("lteValue") {
                val time = LocalTime.of(12, 0, 0)

                test("success with smaller value") {
                    val result = tryValidate { lteValue(time.minusHours(1), time) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { lteValue(time, time) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { lteValue(time.plusHours(1), time) }
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

            context("minValue") {
                val minDateTime = LocalDateTime.of(2025, 1, 1, 12, 0, 0)

                test("success with equal value") {
                    val result = tryValidate { minValue(minDateTime, minDateTime) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { minValue(minDateTime.plusHours(1), minDateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { minValue(minDateTime.minusHours(1), minDateTime) }
                    result.shouldBeFailure()
                }
            }

            context("maxValue") {
                val maxDateTime = LocalDateTime.of(2025, 12, 31, 23, 59, 59)

                test("success with equal value") {
                    val result = tryValidate { maxValue(maxDateTime, maxDateTime) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { maxValue(maxDateTime.minusHours(1), maxDateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { maxValue(maxDateTime.plusHours(1), maxDateTime) }
                    result.shouldBeFailure()
                }
            }

            context("gtValue") {
                val dateTime = LocalDateTime.of(2025, 6, 15, 12, 0, 0)

                test("success") {
                    val result = tryValidate { gtValue(dateTime.plusHours(1), dateTime) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { gtValue(dateTime, dateTime) }
                    result.shouldBeFailure()
                }

                test("failure with smaller value") {
                    val result = tryValidate { gtValue(dateTime.minusHours(1), dateTime) }
                    result.shouldBeFailure()
                }
            }

            context("gteValue") {
                val dateTime = LocalDateTime.of(2025, 6, 15, 12, 0, 0)

                test("success with greater value") {
                    val result = tryValidate { gteValue(dateTime.plusHours(1), dateTime) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { gteValue(dateTime, dateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { gteValue(dateTime.minusHours(1), dateTime) }
                    result.shouldBeFailure()
                }
            }

            context("ltValue") {
                val dateTime = LocalDateTime.of(2025, 6, 15, 12, 0, 0)

                test("success") {
                    val result = tryValidate { ltValue(dateTime.minusHours(1), dateTime) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { ltValue(dateTime, dateTime) }
                    result.shouldBeFailure()
                }

                test("failure with greater value") {
                    val result = tryValidate { ltValue(dateTime.plusHours(1), dateTime) }
                    result.shouldBeFailure()
                }
            }

            context("lteValue") {
                val dateTime = LocalDateTime.of(2025, 6, 15, 12, 0, 0)

                test("success with smaller value") {
                    val result = tryValidate { lteValue(dateTime.minusHours(1), dateTime) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { lteValue(dateTime, dateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { lteValue(dateTime.plusHours(1), dateTime) }
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

            context("minValue") {
                val minInstant = Instant.parse("2025-01-01T00:00:00Z")

                test("success with equal value") {
                    val result = tryValidate { minValue(minInstant, minInstant) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { minValue(minInstant.plusSeconds(3600), minInstant) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { minValue(minInstant.minusSeconds(3600), minInstant) }
                    result.shouldBeFailure()
                }
            }

            context("maxValue") {
                val maxInstant = Instant.parse("2025-12-31T23:59:59Z")

                test("success with equal value") {
                    val result = tryValidate { maxValue(maxInstant, maxInstant) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { maxValue(maxInstant.minusSeconds(3600), maxInstant) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { maxValue(maxInstant.plusSeconds(3600), maxInstant) }
                    result.shouldBeFailure()
                }
            }
        }

        context("MonthDay") {
            context("minValue") {
                val minMonthDay = MonthDay.of(3, 1)

                test("success with equal value") {
                    val result = tryValidate { minValue(minMonthDay, minMonthDay) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { minValue(MonthDay.of(3, 2), minMonthDay) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { minValue(MonthDay.of(2, 28), minMonthDay) }
                    result.shouldBeFailure()
                }
            }

            context("maxValue") {
                val maxMonthDay = MonthDay.of(10, 31)

                test("success with equal value") {
                    val result = tryValidate { maxValue(maxMonthDay, maxMonthDay) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { maxValue(MonthDay.of(10, 30), maxMonthDay) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { maxValue(MonthDay.of(11, 1), maxMonthDay) }
                    result.shouldBeFailure()
                }
            }

            context("gtValue") {
                val monthDay = MonthDay.of(6, 15)

                test("success") {
                    val result = tryValidate { gtValue(MonthDay.of(6, 16), monthDay) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { gtValue(monthDay, monthDay) }
                    result.shouldBeFailure()
                }

                test("failure with smaller value") {
                    val result = tryValidate { gtValue(MonthDay.of(6, 14), monthDay) }
                    result.shouldBeFailure()
                }
            }

            context("gteValue") {
                val monthDay = MonthDay.of(6, 15)

                test("success with greater value") {
                    val result = tryValidate { gteValue(MonthDay.of(6, 16), monthDay) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { gteValue(monthDay, monthDay) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { gteValue(MonthDay.of(6, 14), monthDay) }
                    result.shouldBeFailure()
                }
            }

            context("ltValue") {
                val monthDay = MonthDay.of(6, 15)

                test("success") {
                    val result = tryValidate { ltValue(MonthDay.of(6, 14), monthDay) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { ltValue(monthDay, monthDay) }
                    result.shouldBeFailure()
                }

                test("failure with greater value") {
                    val result = tryValidate { ltValue(MonthDay.of(6, 16), monthDay) }
                    result.shouldBeFailure()
                }
            }

            context("lteValue") {
                val monthDay = MonthDay.of(6, 15)

                test("success with smaller value") {
                    val result = tryValidate { lteValue(MonthDay.of(6, 14), monthDay) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { lteValue(monthDay, monthDay) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { lteValue(MonthDay.of(6, 16), monthDay) }
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

            context("minValue") {
                val minOffsetDateTime = OffsetDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)

                test("success with equal value") {
                    val result = tryValidate { minValue(minOffsetDateTime, minOffsetDateTime) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { minValue(minOffsetDateTime.plusHours(1), minOffsetDateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { minValue(minOffsetDateTime.minusHours(1), minOffsetDateTime) }
                    result.shouldBeFailure()
                }
            }

            context("maxValue") {
                val maxOffsetDateTime = OffsetDateTime.of(2025, 12, 31, 23, 59, 59, 0, ZoneOffset.UTC)

                test("success with equal value") {
                    val result = tryValidate { maxValue(maxOffsetDateTime, maxOffsetDateTime) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { maxValue(maxOffsetDateTime.minusHours(1), maxOffsetDateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { maxValue(maxOffsetDateTime.plusHours(1), maxOffsetDateTime) }
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

            context("minValue") {
                val minOffsetTime = OffsetTime.of(9, 0, 0, 0, ZoneOffset.UTC)

                test("success with equal value") {
                    val result = tryValidate { minValue(minOffsetTime, minOffsetTime) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { minValue(minOffsetTime.plusHours(1), minOffsetTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { minValue(minOffsetTime.minusHours(1), minOffsetTime) }
                    result.shouldBeFailure()
                }
            }

            context("maxValue") {
                val maxOffsetTime = OffsetTime.of(17, 0, 0, 0, ZoneOffset.UTC)

                test("success with equal value") {
                    val result = tryValidate { maxValue(maxOffsetTime, maxOffsetTime) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { maxValue(maxOffsetTime.minusHours(1), maxOffsetTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { maxValue(maxOffsetTime.plusHours(1), maxOffsetTime) }
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

            context("minValue") {
                val minYear = Year.of(2020)

                test("success with equal value") {
                    val result = tryValidate { minValue(minYear, minYear) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { minValue(Year.of(2021), minYear) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { minValue(Year.of(2019), minYear) }
                    result.shouldBeFailure()
                }
            }

            context("maxValue") {
                val maxYear = Year.of(2030)

                test("success with equal value") {
                    val result = tryValidate { maxValue(maxYear, maxYear) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { maxValue(Year.of(2029), maxYear) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { maxValue(Year.of(2031), maxYear) }
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

            context("minValue") {
                val minYearMonth = YearMonth.of(2024, 1)

                test("success with equal value") {
                    val result = tryValidate { minValue(minYearMonth, minYearMonth) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { minValue(YearMonth.of(2024, 2), minYearMonth) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { minValue(YearMonth.of(2023, 12), minYearMonth) }
                    result.shouldBeFailure()
                }
            }

            context("maxValue") {
                val maxYearMonth = YearMonth.of(2025, 12)

                test("success with equal value") {
                    val result = tryValidate { maxValue(maxYearMonth, maxYearMonth) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { maxValue(YearMonth.of(2025, 11), maxYearMonth) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { maxValue(YearMonth.of(2026, 1), maxYearMonth) }
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

            context("minValue") {
                val minZonedDateTime = ZonedDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)

                test("success with equal value") {
                    val result = tryValidate { minValue(minZonedDateTime, minZonedDateTime) }
                    result.shouldBeSuccess()
                }

                test("success with greater value") {
                    val result = tryValidate { minValue(minZonedDateTime.plusHours(1), minZonedDateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { minValue(minZonedDateTime.minusHours(1), minZonedDateTime) }
                    result.shouldBeFailure()
                }
            }

            context("maxValue") {
                val maxZonedDateTime = ZonedDateTime.of(2025, 12, 31, 23, 59, 59, 0, ZoneOffset.UTC)

                test("success with equal value") {
                    val result = tryValidate { maxValue(maxZonedDateTime, maxZonedDateTime) }
                    result.shouldBeSuccess()
                }

                test("success with smaller value") {
                    val result = tryValidate { maxValue(maxZonedDateTime.minusHours(1), maxZonedDateTime) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { maxValue(maxZonedDateTime.plusHours(1), maxZonedDateTime) }
                    result.shouldBeFailure()
                }
            }
        }
    })
