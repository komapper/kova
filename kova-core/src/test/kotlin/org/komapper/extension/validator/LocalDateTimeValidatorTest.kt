package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneOffset

class LocalDateTimeValidatorTest :
    FunSpec({

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
    })
