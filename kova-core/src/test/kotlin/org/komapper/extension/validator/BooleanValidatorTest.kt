package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class BooleanValidatorTest :
    FunSpec({

        context("isTrue") {
            val validator = Kova.boolean().isTrue()

            test("success") {
                val result = validator.tryValidate(true)
                result.isSuccess().mustBeTrue()
                result.value shouldBe true
            }

            test("failure") {
                val result = validator.tryValidate(false)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Boolean false must be true"
            }
        }

        context("isFalse") {
            val validator = Kova.boolean().isFalse()

            test("success") {
                val result = validator.tryValidate(false)
                result.isSuccess().mustBeTrue()
                result.value shouldBe false
            }

            test("failure") {
                val result = validator.tryValidate(true)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Boolean true must be false"
            }
        }
    })
