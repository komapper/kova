package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

class TemporalValidatorTest :
    FunSpec({

        context("LocalDate") {
            context("future") {
                val date = LocalDate.of(2025, 1, 1)
                val zone = ZoneOffset.UTC
                val instant = date.atStartOfDay(zone).toInstant()
                val clock = Clock.fixed(instant, zone)
                val validator = Kova.localDate(clock).future()

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
                val date = LocalDate.of(2025, 1, 1)
                val zone = ZoneOffset.UTC
                val instant = date.atStartOfDay(zone).toInstant()
                val clock = Clock.fixed(instant, zone)
                val validator = Kova.localDate(clock).futureOrPresent()

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
                val date = LocalDate.of(2025, 1, 1)
                val zone = ZoneOffset.UTC
                val instant = date.atStartOfDay(zone).toInstant()
                val clock = Clock.fixed(instant, zone)
                val validator = Kova.localDate(clock).past()

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
                val date = LocalDate.of(2025, 1, 1)
                val zone = ZoneOffset.UTC
                val instant = date.atStartOfDay(zone).toInstant()
                val clock = Clock.fixed(instant, zone)
                val validator = Kova.localDate(clock).pastOrPresent()

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
            context("future") {
                val date = LocalDate.of(2025, 1, 1)
                val time = LocalTime.of(12, 0, 0)
                val zone = ZoneOffset.UTC
                val instant = date.atTime(time).toInstant(zone)
                val clock = Clock.fixed(instant, zone)
                val validator = Kova.localTime(clock).future()

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
                val date = LocalDate.of(2025, 1, 1)
                val time = LocalTime.of(12, 0, 0)
                val zone = ZoneOffset.UTC
                val instant = date.atTime(time).toInstant(zone)
                val clock = Clock.fixed(instant, zone)
                val validator = Kova.localTime(clock).futureOrPresent()

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
                val date = LocalDate.of(2025, 1, 1)
                val time = LocalTime.of(12, 0, 0)
                val zone = ZoneOffset.UTC
                val instant = date.atTime(time).toInstant(zone)
                val clock = Clock.fixed(instant, zone)
                val validator = Kova.localTime(clock).past()

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
                val date = LocalDate.of(2025, 1, 1)
                val time = LocalTime.of(12, 0, 0)
                val zone = ZoneOffset.UTC
                val instant = date.atTime(time).toInstant(zone)
                val clock = Clock.fixed(instant, zone)
                val validator = Kova.localTime(clock).pastOrPresent()

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
            context("future") {
                val dateTime = LocalDateTime.of(2025, 1, 1, 12, 0, 0)
                val zone = ZoneOffset.UTC
                val instant = dateTime.toInstant(zone)
                val clock = Clock.fixed(instant, zone)
                val validator = Kova.localDateTime(clock).future()

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
                val dateTime = LocalDateTime.of(2025, 1, 1, 12, 0, 0)
                val zone = ZoneOffset.UTC
                val instant = dateTime.toInstant(zone)
                val clock = Clock.fixed(instant, zone)
                val validator = Kova.localDateTime(clock).futureOrPresent()

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
                val dateTime = LocalDateTime.of(2025, 1, 1, 12, 0, 0)
                val zone = ZoneOffset.UTC
                val instant = dateTime.toInstant(zone)
                val clock = Clock.fixed(instant, zone)
                val validator = Kova.localDateTime(clock).past()

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
                val dateTime = LocalDateTime.of(2025, 1, 1, 12, 0, 0)
                val zone = ZoneOffset.UTC
                val instant = dateTime.toInstant(zone)
                val clock = Clock.fixed(instant, zone)
                val validator = Kova.localDateTime(clock).pastOrPresent()

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
    })
