package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

class LocalTimeValidatorTest :
    FunSpec({

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
    })
