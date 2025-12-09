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
            val kova = Kova(clock)

            context("future") {
                val validator = kova.localDate().future()

                test("success") {
                    val result = validator.tryValidate(date.plusDays(1))
                    result.isSuccess().mustBeTrue()
                }

                test("failure - present") {
                    val result = validator.tryValidate(date)
                    result.isFailure().mustBeTrue()
                }

                test("failure - past") {
                    val result = validator.tryValidate(date.minusDays(1))
                    result.isFailure().mustBeTrue()
                }
            }

            context("futureOrPresent") {
                val validator = kova.localDate().futureOrPresent()

                test("success - future") {
                    val result = validator.tryValidate(date.plusDays(1))
                    result.isSuccess().mustBeTrue()
                }

                test("success - present") {
                    val result = validator.tryValidate(date)
                    result.isSuccess().mustBeTrue()
                }

                test("failure - past") {
                    val result = validator.tryValidate(date.minusDays(1))
                    result.isFailure().mustBeTrue()
                }
            }

            context("past") {
                val validator = kova.localDate().past()

                test("success") {
                    val result = validator.tryValidate(date.minusDays(1))
                    result.isSuccess().mustBeTrue()
                }

                test("failure - present") {
                    val result = validator.tryValidate(date)
                    result.isFailure().mustBeTrue()
                }

                test("failure - future") {
                    val result = validator.tryValidate(date.plusDays(1))
                    result.isFailure().mustBeTrue()
                }
            }

            context("pastOrPresent") {
                val validator = kova.localDate().pastOrPresent()

                test("success - past") {
                    val result = validator.tryValidate(date.minusDays(1))
                    result.isSuccess().mustBeTrue()
                }

                test("success - present") {
                    val result = validator.tryValidate(date)
                    result.isSuccess().mustBeTrue()
                }

                test("failure - future") {
                    val result = validator.tryValidate(date.plusDays(1))
                    result.isFailure().mustBeTrue()
                }
            }

            context("min") {
                val minDate = LocalDate.of(2025, 1, 1)
                val validator = Kova.localDate().min(minDate)

                test("success - equal") {
                    val result = validator.tryValidate(minDate)
                    result.isSuccess().mustBeTrue()
                }

                test("success - greater") {
                    val result = validator.tryValidate(minDate.plusDays(1))
                    result.isSuccess().mustBeTrue()
                }

                test("failure") {
                    val result = validator.tryValidate(minDate.minusDays(1))
                    result.isFailure().mustBeTrue()
                }
            }

            context("max") {
                val maxDate = LocalDate.of(2025, 12, 31)
                val validator = Kova.localDate().max(maxDate)

                test("success - equal") {
                    val result = validator.tryValidate(maxDate)
                    result.isSuccess().mustBeTrue()
                }

                test("success - less") {
                    val result = validator.tryValidate(maxDate.minusDays(1))
                    result.isSuccess().mustBeTrue()
                }

                test("failure") {
                    val result = validator.tryValidate(maxDate.plusDays(1))
                    result.isFailure().mustBeTrue()
                }
            }

            context("gt") {
                val date = LocalDate.of(2025, 6, 15)
                val validator = Kova.localDate().gt(date)

                test("success") {
                    val result = validator.tryValidate(date.plusDays(1))
                    result.isSuccess().mustBeTrue()
                }

                test("failure - equal") {
                    val result = validator.tryValidate(date)
                    result.isFailure().mustBeTrue()
                }

                test("failure - less") {
                    val result = validator.tryValidate(date.minusDays(1))
                    result.isFailure().mustBeTrue()
                }
            }

            context("gte") {
                val date = LocalDate.of(2025, 6, 15)
                val validator = Kova.localDate().gte(date)

                test("success - greater") {
                    val result = validator.tryValidate(date.plusDays(1))
                    result.isSuccess().mustBeTrue()
                }

                test("success - equal") {
                    val result = validator.tryValidate(date)
                    result.isSuccess().mustBeTrue()
                }

                test("failure") {
                    val result = validator.tryValidate(date.minusDays(1))
                    result.isFailure().mustBeTrue()
                }
            }

            context("lt") {
                val date = LocalDate.of(2025, 6, 15)
                val validator = Kova.localDate().lt(date)

                test("success") {
                    val result = validator.tryValidate(date.minusDays(1))
                    result.isSuccess().mustBeTrue()
                }

                test("failure - equal") {
                    val result = validator.tryValidate(date)
                    result.isFailure().mustBeTrue()
                }

                test("failure - greater") {
                    val result = validator.tryValidate(date.plusDays(1))
                    result.isFailure().mustBeTrue()
                }
            }

            context("lte") {
                val date = LocalDate.of(2025, 6, 15)
                val validator = Kova.localDate().lte(date)

                test("success - less") {
                    val result = validator.tryValidate(date.minusDays(1))
                    result.isSuccess().mustBeTrue()
                }

                test("success - equal") {
                    val result = validator.tryValidate(date)
                    result.isSuccess().mustBeTrue()
                }

                test("failure") {
                    val result = validator.tryValidate(date.plusDays(1))
                    result.isFailure().mustBeTrue()
                }
            }
        }

        context("LocalTime") {
            val date = LocalDate.of(2025, 1, 1)
            val time = LocalTime.of(12, 0, 0)
            val zone = ZoneOffset.UTC
            val instant = date.atTime(time).toInstant(zone)
            val clock = Clock.fixed(instant, zone)
            val kova = Kova(clock)

            context("future") {
                val validator = kova.localTime().future()

                test("success") {
                    val result = validator.tryValidate(time.plusHours(1))
                    result.isSuccess().mustBeTrue()
                }

                test("failure - present") {
                    val result = validator.tryValidate(time)
                    result.isFailure().mustBeTrue()
                }

                test("failure - past") {
                    val result = validator.tryValidate(time.minusHours(1))
                    result.isFailure().mustBeTrue()
                }
            }

            context("futureOrPresent") {
                val validator = kova.localTime().futureOrPresent()

                test("success - future") {
                    val result = validator.tryValidate(time.plusHours(1))
                    result.isSuccess().mustBeTrue()
                }

                test("success - present") {
                    val result = validator.tryValidate(time)
                    result.isSuccess().mustBeTrue()
                }

                test("failure - past") {
                    val result = validator.tryValidate(time.minusHours(1))
                    result.isFailure().mustBeTrue()
                }
            }

            context("past") {
                val validator = kova.localTime().past()

                test("success") {
                    val result = validator.tryValidate(time.minusHours(1))
                    result.isSuccess().mustBeTrue()
                }

                test("failure - present") {
                    val result = validator.tryValidate(time)
                    result.isFailure().mustBeTrue()
                }

                test("failure - future") {
                    val result = validator.tryValidate(time.plusHours(1))
                    result.isFailure().mustBeTrue()
                }
            }

            context("pastOrPresent") {
                val validator = kova.localTime().pastOrPresent()

                test("success - past") {
                    val result = validator.tryValidate(time.minusHours(1))
                    result.isSuccess().mustBeTrue()
                }

                test("success - present") {
                    val result = validator.tryValidate(time)
                    result.isSuccess().mustBeTrue()
                }

                test("failure - future") {
                    val result = validator.tryValidate(time.plusHours(1))
                    result.isFailure().mustBeTrue()
                }
            }

            context("min") {
                val minTime = LocalTime.of(9, 0, 0)
                val validator = Kova.localTime().min(minTime)

                test("success - equal") {
                    val result = validator.tryValidate(minTime)
                    result.isSuccess().mustBeTrue()
                }

                test("success - greater") {
                    val result = validator.tryValidate(minTime.plusHours(1))
                    result.isSuccess().mustBeTrue()
                }

                test("failure") {
                    val result = validator.tryValidate(minTime.minusHours(1))
                    result.isFailure().mustBeTrue()
                }
            }

            context("max") {
                val maxTime = LocalTime.of(17, 0, 0)
                val validator = Kova.localTime().max(maxTime)

                test("success - equal") {
                    val result = validator.tryValidate(maxTime)
                    result.isSuccess().mustBeTrue()
                }

                test("success - less") {
                    val result = validator.tryValidate(maxTime.minusHours(1))
                    result.isSuccess().mustBeTrue()
                }

                test("failure") {
                    val result = validator.tryValidate(maxTime.plusHours(1))
                    result.isFailure().mustBeTrue()
                }
            }

            context("gt") {
                val time = LocalTime.of(12, 0, 0)
                val validator = Kova.localTime().gt(time)

                test("success") {
                    val result = validator.tryValidate(time.plusHours(1))
                    result.isSuccess().mustBeTrue()
                }

                test("failure - equal") {
                    val result = validator.tryValidate(time)
                    result.isFailure().mustBeTrue()
                }

                test("failure - less") {
                    val result = validator.tryValidate(time.minusHours(1))
                    result.isFailure().mustBeTrue()
                }
            }

            context("gte") {
                val time = LocalTime.of(12, 0, 0)
                val validator = Kova.localTime().gte(time)

                test("success - greater") {
                    val result = validator.tryValidate(time.plusHours(1))
                    result.isSuccess().mustBeTrue()
                }

                test("success - equal") {
                    val result = validator.tryValidate(time)
                    result.isSuccess().mustBeTrue()
                }

                test("failure") {
                    val result = validator.tryValidate(time.minusHours(1))
                    result.isFailure().mustBeTrue()
                }
            }

            context("lt") {
                val time = LocalTime.of(12, 0, 0)
                val validator = Kova.localTime().lt(time)

                test("success") {
                    val result = validator.tryValidate(time.minusHours(1))
                    result.isSuccess().mustBeTrue()
                }

                test("failure - equal") {
                    val result = validator.tryValidate(time)
                    result.isFailure().mustBeTrue()
                }

                test("failure - greater") {
                    val result = validator.tryValidate(time.plusHours(1))
                    result.isFailure().mustBeTrue()
                }
            }

            context("lte") {
                val time = LocalTime.of(12, 0, 0)
                val validator = Kova.localTime().lte(time)

                test("success - less") {
                    val result = validator.tryValidate(time.minusHours(1))
                    result.isSuccess().mustBeTrue()
                }

                test("success - equal") {
                    val result = validator.tryValidate(time)
                    result.isSuccess().mustBeTrue()
                }

                test("failure") {
                    val result = validator.tryValidate(time.plusHours(1))
                    result.isFailure().mustBeTrue()
                }
            }
        }

        context("LocalDateTime") {
            val dateTime = LocalDateTime.of(2025, 1, 1, 12, 0, 0)
            val zone = ZoneOffset.UTC
            val instant = dateTime.toInstant(zone)
            val clock = Clock.fixed(instant, zone)
            val kova = Kova(clock)

            context("future") {
                val validator = kova.localDateTime().future()

                test("success") {
                    val result = validator.tryValidate(dateTime.plusHours(1))
                    result.isSuccess().mustBeTrue()
                }

                test("failure - present") {
                    val result = validator.tryValidate(dateTime)
                    result.isFailure().mustBeTrue()
                }

                test("failure - past") {
                    val result = validator.tryValidate(dateTime.minusHours(1))
                    result.isFailure().mustBeTrue()
                }
            }

            context("futureOrPresent") {
                val validator = kova.localDateTime().futureOrPresent()

                test("success - future") {
                    val result = validator.tryValidate(dateTime.plusHours(1))
                    result.isSuccess().mustBeTrue()
                }

                test("success - present") {
                    val result = validator.tryValidate(dateTime)
                    result.isSuccess().mustBeTrue()
                }

                test("failure - past") {
                    val result = validator.tryValidate(dateTime.minusHours(1))
                    result.isFailure().mustBeTrue()
                }
            }

            context("past") {
                val validator = kova.localDateTime().past()

                test("success") {
                    val result = validator.tryValidate(dateTime.minusHours(1))
                    result.isSuccess().mustBeTrue()
                }

                test("failure - present") {
                    val result = validator.tryValidate(dateTime)
                    result.isFailure().mustBeTrue()
                }

                test("failure - future") {
                    val result = validator.tryValidate(dateTime.plusHours(1))
                    result.isFailure().mustBeTrue()
                }
            }

            context("pastOrPresent") {
                val validator = kova.localDateTime().pastOrPresent()

                test("success - past") {
                    val result = validator.tryValidate(dateTime.minusHours(1))
                    result.isSuccess().mustBeTrue()
                }

                test("success - present") {
                    val result = validator.tryValidate(dateTime)
                    result.isSuccess().mustBeTrue()
                }

                test("failure - future") {
                    val result = validator.tryValidate(dateTime.plusHours(1))
                    result.isFailure().mustBeTrue()
                }
            }

            context("min") {
                val minDateTime = LocalDateTime.of(2025, 1, 1, 12, 0, 0)
                val validator = Kova.localDateTime().min(minDateTime)

                test("success - equal") {
                    val result = validator.tryValidate(minDateTime)
                    result.isSuccess().mustBeTrue()
                }

                test("success - greater") {
                    val result = validator.tryValidate(minDateTime.plusHours(1))
                    result.isSuccess().mustBeTrue()
                }

                test("failure") {
                    val result = validator.tryValidate(minDateTime.minusHours(1))
                    result.isFailure().mustBeTrue()
                }
            }

            context("max") {
                val maxDateTime = LocalDateTime.of(2025, 12, 31, 23, 59, 59)
                val validator = Kova.localDateTime().max(maxDateTime)

                test("success - equal") {
                    val result = validator.tryValidate(maxDateTime)
                    result.isSuccess().mustBeTrue()
                }

                test("success - less") {
                    val result = validator.tryValidate(maxDateTime.minusHours(1))
                    result.isSuccess().mustBeTrue()
                }

                test("failure") {
                    val result = validator.tryValidate(maxDateTime.plusHours(1))
                    result.isFailure().mustBeTrue()
                }
            }

            context("gt") {
                val dateTime = LocalDateTime.of(2025, 6, 15, 12, 0, 0)
                val validator = Kova.localDateTime().gt(dateTime)

                test("success") {
                    val result = validator.tryValidate(dateTime.plusHours(1))
                    result.isSuccess().mustBeTrue()
                }

                test("failure - equal") {
                    val result = validator.tryValidate(dateTime)
                    result.isFailure().mustBeTrue()
                }

                test("failure - less") {
                    val result = validator.tryValidate(dateTime.minusHours(1))
                    result.isFailure().mustBeTrue()
                }
            }

            context("gte") {
                val dateTime = LocalDateTime.of(2025, 6, 15, 12, 0, 0)
                val validator = Kova.localDateTime().gte(dateTime)

                test("success - greater") {
                    val result = validator.tryValidate(dateTime.plusHours(1))
                    result.isSuccess().mustBeTrue()
                }

                test("success - equal") {
                    val result = validator.tryValidate(dateTime)
                    result.isSuccess().mustBeTrue()
                }

                test("failure") {
                    val result = validator.tryValidate(dateTime.minusHours(1))
                    result.isFailure().mustBeTrue()
                }
            }

            context("lt") {
                val dateTime = LocalDateTime.of(2025, 6, 15, 12, 0, 0)
                val validator = Kova.localDateTime().lt(dateTime)

                test("success") {
                    val result = validator.tryValidate(dateTime.minusHours(1))
                    result.isSuccess().mustBeTrue()
                }

                test("failure - equal") {
                    val result = validator.tryValidate(dateTime)
                    result.isFailure().mustBeTrue()
                }

                test("failure - greater") {
                    val result = validator.tryValidate(dateTime.plusHours(1))
                    result.isFailure().mustBeTrue()
                }
            }

            context("lte") {
                val dateTime = LocalDateTime.of(2025, 6, 15, 12, 0, 0)
                val validator = Kova.localDateTime().lte(dateTime)

                test("success - less") {
                    val result = validator.tryValidate(dateTime.minusHours(1))
                    result.isSuccess().mustBeTrue()
                }

                test("success - equal") {
                    val result = validator.tryValidate(dateTime)
                    result.isSuccess().mustBeTrue()
                }

                test("failure") {
                    val result = validator.tryValidate(dateTime.plusHours(1))
                    result.isFailure().mustBeTrue()
                }
            }
        }

        context("Instant") {
            val instant = Instant.parse("2025-01-01T12:00:00Z")
            val clock = Clock.fixed(instant, ZoneOffset.UTC)
            val kova = Kova(clock)

            context("future") {
                val validator = kova.instant().future()

                test("success") {
                    val result = validator.tryValidate(instant.plusSeconds(3600))
                    result.isSuccess().mustBeTrue()
                }

                test("failure - past") {
                    val result = validator.tryValidate(instant.minusSeconds(3600))
                    result.isFailure().mustBeTrue()
                }
            }

            context("past") {
                val validator = kova.instant().past()

                test("success") {
                    val result = validator.tryValidate(instant.minusSeconds(3600))
                    result.isSuccess().mustBeTrue()
                }

                test("failure - future") {
                    val result = validator.tryValidate(instant.plusSeconds(3600))
                    result.isFailure().mustBeTrue()
                }
            }

            context("min") {
                val minInstant = Instant.parse("2025-01-01T00:00:00Z")
                val validator = Kova.instant().min(minInstant)

                test("success - equal") {
                    val result = validator.tryValidate(minInstant)
                    result.isSuccess().mustBeTrue()
                }

                test("success - greater") {
                    val result = validator.tryValidate(minInstant.plusSeconds(3600))
                    result.isSuccess().mustBeTrue()
                }

                test("failure") {
                    val result = validator.tryValidate(minInstant.minusSeconds(3600))
                    result.isFailure().mustBeTrue()
                }
            }

            context("max") {
                val maxInstant = Instant.parse("2025-12-31T23:59:59Z")
                val validator = Kova.instant().max(maxInstant)

                test("success - equal") {
                    val result = validator.tryValidate(maxInstant)
                    result.isSuccess().mustBeTrue()
                }

                test("success - less") {
                    val result = validator.tryValidate(maxInstant.minusSeconds(3600))
                    result.isSuccess().mustBeTrue()
                }

                test("failure") {
                    val result = validator.tryValidate(maxInstant.plusSeconds(3600))
                    result.isFailure().mustBeTrue()
                }
            }
        }

        context("MonthDay") {
            context("min") {
                val minMonthDay = MonthDay.of(3, 1)
                val validator = Kova.monthDay().min(minMonthDay)

                test("success - equal") {
                    val result = validator.tryValidate(minMonthDay)
                    result.isSuccess().mustBeTrue()
                }

                test("success - greater") {
                    val result = validator.tryValidate(MonthDay.of(3, 2))
                    result.isSuccess().mustBeTrue()
                }

                test("failure") {
                    val result = validator.tryValidate(MonthDay.of(2, 28))
                    result.isFailure().mustBeTrue()
                }
            }

            context("max") {
                val maxMonthDay = MonthDay.of(10, 31)
                val validator = Kova.monthDay().max(maxMonthDay)

                test("success - equal") {
                    val result = validator.tryValidate(maxMonthDay)
                    result.isSuccess().mustBeTrue()
                }

                test("success - less") {
                    val result = validator.tryValidate(MonthDay.of(10, 30))
                    result.isSuccess().mustBeTrue()
                }

                test("failure") {
                    val result = validator.tryValidate(MonthDay.of(11, 1))
                    result.isFailure().mustBeTrue()
                }
            }

            context("gt") {
                val monthDay = MonthDay.of(6, 15)
                val validator = Kova.monthDay().gt(monthDay)

                test("success") {
                    val result = validator.tryValidate(MonthDay.of(6, 16))
                    result.isSuccess().mustBeTrue()
                }

                test("failure - equal") {
                    val result = validator.tryValidate(monthDay)
                    result.isFailure().mustBeTrue()
                }

                test("failure - less") {
                    val result = validator.tryValidate(MonthDay.of(6, 14))
                    result.isFailure().mustBeTrue()
                }
            }

            context("gte") {
                val monthDay = MonthDay.of(6, 15)
                val validator = Kova.monthDay().gte(monthDay)

                test("success - greater") {
                    val result = validator.tryValidate(MonthDay.of(6, 16))
                    result.isSuccess().mustBeTrue()
                }

                test("success - equal") {
                    val result = validator.tryValidate(monthDay)
                    result.isSuccess().mustBeTrue()
                }

                test("failure") {
                    val result = validator.tryValidate(MonthDay.of(6, 14))
                    result.isFailure().mustBeTrue()
                }
            }

            context("lt") {
                val monthDay = MonthDay.of(6, 15)
                val validator = Kova.monthDay().lt(monthDay)

                test("success") {
                    val result = validator.tryValidate(MonthDay.of(6, 14))
                    result.isSuccess().mustBeTrue()
                }

                test("failure - equal") {
                    val result = validator.tryValidate(monthDay)
                    result.isFailure().mustBeTrue()
                }

                test("failure - greater") {
                    val result = validator.tryValidate(MonthDay.of(6, 16))
                    result.isFailure().mustBeTrue()
                }
            }

            context("lte") {
                val monthDay = MonthDay.of(6, 15)
                val validator = Kova.monthDay().lte(monthDay)

                test("success - less") {
                    val result = validator.tryValidate(MonthDay.of(6, 14))
                    result.isSuccess().mustBeTrue()
                }

                test("success - equal") {
                    val result = validator.tryValidate(monthDay)
                    result.isSuccess().mustBeTrue()
                }

                test("failure") {
                    val result = validator.tryValidate(MonthDay.of(6, 16))
                    result.isFailure().mustBeTrue()
                }
            }
        }

        context("OffsetDateTime") {
            val offsetDateTime = OffsetDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC)
            val clock = Clock.fixed(offsetDateTime.toInstant(), ZoneOffset.UTC)
            val kova = Kova(clock)

            context("future") {
                val validator = kova.offsetDateTime().future()

                test("success") {
                    val result = validator.tryValidate(offsetDateTime.plusHours(1))
                    result.isSuccess().mustBeTrue()
                }

                test("failure - past") {
                    val result = validator.tryValidate(offsetDateTime.minusHours(1))
                    result.isFailure().mustBeTrue()
                }
            }

            context("past") {
                val validator = kova.offsetDateTime().past()

                test("success") {
                    val result = validator.tryValidate(offsetDateTime.minusHours(1))
                    result.isSuccess().mustBeTrue()
                }

                test("failure - future") {
                    val result = validator.tryValidate(offsetDateTime.plusHours(1))
                    result.isFailure().mustBeTrue()
                }
            }

            context("min") {
                val minOffsetDateTime = OffsetDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
                val validator = Kova.offsetDateTime().min(minOffsetDateTime)

                test("success - equal") {
                    val result = validator.tryValidate(minOffsetDateTime)
                    result.isSuccess().mustBeTrue()
                }

                test("success - greater") {
                    val result = validator.tryValidate(minOffsetDateTime.plusHours(1))
                    result.isSuccess().mustBeTrue()
                }

                test("failure") {
                    val result = validator.tryValidate(minOffsetDateTime.minusHours(1))
                    result.isFailure().mustBeTrue()
                }
            }

            context("max") {
                val maxOffsetDateTime = OffsetDateTime.of(2025, 12, 31, 23, 59, 59, 0, ZoneOffset.UTC)
                val validator = Kova.offsetDateTime().max(maxOffsetDateTime)

                test("success - equal") {
                    val result = validator.tryValidate(maxOffsetDateTime)
                    result.isSuccess().mustBeTrue()
                }

                test("success - less") {
                    val result = validator.tryValidate(maxOffsetDateTime.minusHours(1))
                    result.isSuccess().mustBeTrue()
                }

                test("failure") {
                    val result = validator.tryValidate(maxOffsetDateTime.plusHours(1))
                    result.isFailure().mustBeTrue()
                }
            }
        }

        context("OffsetTime") {
            val date = LocalDate.of(2025, 1, 1)
            val offsetTime = OffsetTime.of(12, 0, 0, 0, ZoneOffset.UTC)
            val instant = date.atTime(offsetTime.toLocalTime()).toInstant(ZoneOffset.UTC)
            val clock = Clock.fixed(instant, ZoneOffset.UTC)
            val kova = Kova(clock)

            context("future") {
                val validator = kova.offsetTime().future()

                test("success") {
                    val result = validator.tryValidate(offsetTime.plusHours(1))
                    result.isSuccess().mustBeTrue()
                }

                test("failure - past") {
                    val result = validator.tryValidate(offsetTime.minusHours(1))
                    result.isFailure().mustBeTrue()
                }
            }

            context("past") {
                val validator = kova.offsetTime().past()

                test("success") {
                    val result = validator.tryValidate(offsetTime.minusHours(1))
                    result.isSuccess().mustBeTrue()
                }

                test("failure - future") {
                    val result = validator.tryValidate(offsetTime.plusHours(1))
                    result.isFailure().mustBeTrue()
                }
            }

            context("min") {
                val minOffsetTime = OffsetTime.of(9, 0, 0, 0, ZoneOffset.UTC)
                val validator = Kova.offsetTime().min(minOffsetTime)

                test("success - equal") {
                    val result = validator.tryValidate(minOffsetTime)
                    result.isSuccess().mustBeTrue()
                }

                test("success - greater") {
                    val result = validator.tryValidate(minOffsetTime.plusHours(1))
                    result.isSuccess().mustBeTrue()
                }

                test("failure") {
                    val result = validator.tryValidate(minOffsetTime.minusHours(1))
                    result.isFailure().mustBeTrue()
                }
            }

            context("max") {
                val maxOffsetTime = OffsetTime.of(17, 0, 0, 0, ZoneOffset.UTC)
                val validator = Kova.offsetTime().max(maxOffsetTime)

                test("success - equal") {
                    val result = validator.tryValidate(maxOffsetTime)
                    result.isSuccess().mustBeTrue()
                }

                test("success - less") {
                    val result = validator.tryValidate(maxOffsetTime.minusHours(1))
                    result.isSuccess().mustBeTrue()
                }

                test("failure") {
                    val result = validator.tryValidate(maxOffsetTime.plusHours(1))
                    result.isFailure().mustBeTrue()
                }
            }
        }

        context("Year") {
            val date = LocalDate.of(2025, 1, 1)
            val zone = ZoneOffset.UTC
            val instant = date.atStartOfDay(zone).toInstant()
            val clock = Clock.fixed(instant, zone)
            val kova = Kova(clock)

            context("future") {
                val validator = kova.year().future()

                test("success") {
                    val result = validator.tryValidate(Year.of(2026))
                    result.isSuccess().mustBeTrue()
                }

                test("failure - past") {
                    val result = validator.tryValidate(Year.of(2024))
                    result.isFailure().mustBeTrue()
                }
            }

            context("past") {
                val validator = kova.year().past()

                test("success") {
                    val result = validator.tryValidate(Year.of(2024))
                    result.isSuccess().mustBeTrue()
                }

                test("failure - future") {
                    val result = validator.tryValidate(Year.of(2026))
                    result.isFailure().mustBeTrue()
                }
            }

            context("min") {
                val minYear = Year.of(2020)
                val validator = Kova.year().min(minYear)

                test("success - equal") {
                    val result = validator.tryValidate(minYear)
                    result.isSuccess().mustBeTrue()
                }

                test("success - greater") {
                    val result = validator.tryValidate(Year.of(2021))
                    result.isSuccess().mustBeTrue()
                }

                test("failure") {
                    val result = validator.tryValidate(Year.of(2019))
                    result.isFailure().mustBeTrue()
                }
            }

            context("max") {
                val maxYear = Year.of(2030)
                val validator = Kova.year().max(maxYear)

                test("success - equal") {
                    val result = validator.tryValidate(maxYear)
                    result.isSuccess().mustBeTrue()
                }

                test("success - less") {
                    val result = validator.tryValidate(Year.of(2029))
                    result.isSuccess().mustBeTrue()
                }

                test("failure") {
                    val result = validator.tryValidate(Year.of(2031))
                    result.isFailure().mustBeTrue()
                }
            }
        }

        context("YearMonth") {
            val date = LocalDate.of(2025, 1, 1)
            val zone = ZoneOffset.UTC
            val instant = date.atStartOfDay(zone).toInstant()
            val clock = Clock.fixed(instant, zone)
            val kova = Kova(clock)

            context("future") {
                val validator = kova.yearMonth().future()

                test("success") {
                    val result = validator.tryValidate(YearMonth.of(2025, 2))
                    result.isSuccess().mustBeTrue()
                }

                test("failure - past") {
                    val result = validator.tryValidate(YearMonth.of(2024, 12))
                    result.isFailure().mustBeTrue()
                }
            }

            context("past") {
                val validator = kova.yearMonth().past()

                test("success") {
                    val result = validator.tryValidate(YearMonth.of(2024, 12))
                    result.isSuccess().mustBeTrue()
                }

                test("failure - future") {
                    val result = validator.tryValidate(YearMonth.of(2025, 2))
                    result.isFailure().mustBeTrue()
                }
            }

            context("min") {
                val minYearMonth = YearMonth.of(2024, 1)
                val validator = Kova.yearMonth().min(minYearMonth)

                test("success - equal") {
                    val result = validator.tryValidate(minYearMonth)
                    result.isSuccess().mustBeTrue()
                }

                test("success - greater") {
                    val result = validator.tryValidate(YearMonth.of(2024, 2))
                    result.isSuccess().mustBeTrue()
                }

                test("failure") {
                    val result = validator.tryValidate(YearMonth.of(2023, 12))
                    result.isFailure().mustBeTrue()
                }
            }

            context("max") {
                val maxYearMonth = YearMonth.of(2025, 12)
                val validator = Kova.yearMonth().max(maxYearMonth)

                test("success - equal") {
                    val result = validator.tryValidate(maxYearMonth)
                    result.isSuccess().mustBeTrue()
                }

                test("success - less") {
                    val result = validator.tryValidate(YearMonth.of(2025, 11))
                    result.isSuccess().mustBeTrue()
                }

                test("failure") {
                    val result = validator.tryValidate(YearMonth.of(2026, 1))
                    result.isFailure().mustBeTrue()
                }
            }
        }

        context("ZonedDateTime") {
            val zonedDateTime = ZonedDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC)
            val clock = Clock.fixed(zonedDateTime.toInstant(), ZoneOffset.UTC)
            val kova = Kova(clock)

            context("future") {
                val validator = kova.zonedDateTime().future()

                test("success") {
                    val result = validator.tryValidate(zonedDateTime.plusHours(1))
                    result.isSuccess().mustBeTrue()
                }

                test("failure - past") {
                    val result = validator.tryValidate(zonedDateTime.minusHours(1))
                    result.isFailure().mustBeTrue()
                }
            }

            context("past") {
                val validator = kova.zonedDateTime().past()

                test("success") {
                    val result = validator.tryValidate(zonedDateTime.minusHours(1))
                    result.isSuccess().mustBeTrue()
                }

                test("failure - future") {
                    val result = validator.tryValidate(zonedDateTime.plusHours(1))
                    result.isFailure().mustBeTrue()
                }
            }

            context("min") {
                val minZonedDateTime = ZonedDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
                val validator = Kova.zonedDateTime().min(minZonedDateTime)

                test("success - equal") {
                    val result = validator.tryValidate(minZonedDateTime)
                    result.isSuccess().mustBeTrue()
                }

                test("success - greater") {
                    val result = validator.tryValidate(minZonedDateTime.plusHours(1))
                    result.isSuccess().mustBeTrue()
                }

                test("failure") {
                    val result = validator.tryValidate(minZonedDateTime.minusHours(1))
                    result.isFailure().mustBeTrue()
                }
            }

            context("max") {
                val maxZonedDateTime = ZonedDateTime.of(2025, 12, 31, 23, 59, 59, 0, ZoneOffset.UTC)
                val validator = Kova.zonedDateTime().max(maxZonedDateTime)

                test("success - equal") {
                    val result = validator.tryValidate(maxZonedDateTime)
                    result.isSuccess().mustBeTrue()
                }

                test("success - less") {
                    val result = validator.tryValidate(maxZonedDateTime.minusHours(1))
                    result.isSuccess().mustBeTrue()
                }

                test("failure") {
                    val result = validator.tryValidate(maxZonedDateTime.plusHours(1))
                    result.isFailure().mustBeTrue()
                }
            }
        }
    })
