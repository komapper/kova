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
    })
