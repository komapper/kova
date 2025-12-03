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
    })
