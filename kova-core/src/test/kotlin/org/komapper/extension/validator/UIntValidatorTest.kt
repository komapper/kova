package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.test.assertTrue

class UIntValidatorTest :
    FunSpec({

        context("plus") {
            val validator = Kova.uInt().max(2u) + Kova.uInt().max(3u)

            test("success") {
                val result = validator.tryValidate(1u)
                assertTrue(result.isSuccess())
                result.value shouldBe 1u
            }

            test("failure") {
                val result = validator.tryValidate(5u)
                assertTrue(result.isFailure())
                result.messages.size shouldBe 2
                result.messages[0].content shouldBe "Comparable 5 must be less than or equal to 2"
                result.messages[1].content shouldBe "Comparable 5 must be less than or equal to 3"
            }
        }

        context("constraint") {
            val validator =
                Kova.uInt().constraint {
                    Constraint.satisfies(it.input == 10u, Message.Text("Constraint failed"))
                }

            test("success") {
                val result = validator.tryValidate(10u)
                assertTrue(result.isSuccess())
            }

            test("failure") {
                val result = validator.tryValidate(20u)
                assertTrue(result.isFailure())
                result.messages.single().content shouldBe "Constraint failed"
            }
        }
    })
