package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class IntValidatorTest :
    FunSpec({

        context("plus") {
            val validator = Kova.int().max(2) + Kova.int().max(3)

            test("success") {
                val result = validator.tryValidate(1)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 1
            }

            test("failure") {
                val result = validator.tryValidate(5)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 2
                result.messages[0].content shouldBe "Number 5 must be less than or equal to 2"
                result.messages[1].content shouldBe "Number 5 must be less than or equal to 3"
            }
        }

        context("constraint") {
            val validator =
                Kova.int().constraint {
                    Constraint.satisfies(it.input == 10, Message.Text("Constraint failed"))
                }

            test("success") {
                val result = validator.tryValidate(10)
                result.isSuccess().mustBeTrue()
            }

            test("failure") {
                val result = validator.tryValidate(20)
                result.isFailure().mustBeTrue()
                result.messages[0].content shouldBe "Constraint failed"
            }
        }
    })
