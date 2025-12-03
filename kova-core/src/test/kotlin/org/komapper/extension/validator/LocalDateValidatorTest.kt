package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset

class LocalDateValidatorTest :
    FunSpec({

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
    })
