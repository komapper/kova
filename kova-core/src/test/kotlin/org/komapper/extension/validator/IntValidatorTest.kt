package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.test.assertTrue

class IntValidatorTest :
    FunSpec({

        context("plus") {
            val validator = Kova.int().max(2) + Kova.int().max(3)

            test("success") {
                val result = validator.tryValidate(1)
                assertTrue(result.isSuccess())
                result.value shouldBe 1
            }

            test("failure") {
                val result = validator.tryValidate(5)
                assertTrue(result.isFailure())
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
                assertTrue(result.isSuccess())
            }

            test("failure") {
                val result = validator.tryValidate(20)
                assertTrue(result.isFailure())
                result.messages[0].content shouldBe "Constraint failed"
            }
        }
    })
