package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.test.assertTrue

class ConditionalValidatorTest :
    FunSpec({
        context("onlyIf") {
            val validator = Kova.int().min(3).onlyWhen { it % 2 == 0 }

            test("success") {
                val result = validator.tryValidate(1)
                assertTrue(result.isSuccess())
                result.value shouldBe 1
            }

            test("failure") {
                val result = validator.tryValidate(2)
                assertTrue(result.isFailure())
                result.messages.size shouldBe 1
                result.messages[0] shouldBe "Number 2 must be greater than or equal to 3"
            }
        }

        context("onlyIf and plus") {
            val validator = Kova.int().min(3).onlyWhen { it % 2 == 0 } + Kova.int().min(1)

            test("success - plus") {
                val result = validator.tryValidate(1)
                assertTrue(result.isSuccess())
                result.value shouldBe 1
            }

            test("failure - plus") {
                val result = validator.tryValidate(0)
                assertTrue(result.isFailure())
                result.messages.size shouldBe 2
                result.messages[0] shouldBe "Number 0 must be greater than or equal to 3"
                result.messages[1] shouldBe "Number 0 must be greater than or equal to 1"
            }
        }
    })
