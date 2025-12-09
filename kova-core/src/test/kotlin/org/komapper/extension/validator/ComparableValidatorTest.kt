package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ComparableValidatorTest :
    FunSpec({

        context("plus") {
            val validator = Kova.uInt().max(2u) + Kova.uInt().max(3u)

            test("success") {
                val result = validator.tryValidate(1u)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 1u
            }

            test("failure") {
                val result = validator.tryValidate(5u)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 2
                result.messages[0].constraintId shouldBe "kova.comparable.max"
                result.messages[1].constraintId shouldBe "kova.comparable.max"
            }
        }

        context("constrain") {
            val validator =
                Kova.uInt().constrain("test") {
                    satisfies(it.input == 10u, "Constraint failed")
                }

            test("success") {
                val result = validator.tryValidate(10u)
                result.isSuccess().mustBeTrue()
            }

            test("failure") {
                val result = validator.tryValidate(20u)
                result.isFailure().mustBeTrue()
                result.messages.single().text shouldBe "Constraint failed"
            }
        }
    })
