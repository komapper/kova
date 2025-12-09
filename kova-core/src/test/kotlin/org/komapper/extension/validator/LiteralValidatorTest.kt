package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class LiteralValidatorTest :
    FunSpec({

        context("literal - boolean") {
            val validator = Kova.literal(true)

            test("success") {
                val result = validator.tryValidate(true)
                result.isSuccess().mustBeTrue()
                result.value shouldBe true
            }

            test("failure") {
                val result = validator.tryValidate(false)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.literal.single"
            }
        }

        context("literal - int") {
            val validator = Kova.literal(123)

            test("success") {
                val result = validator.tryValidate(123)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 123
            }

            test("failure") {
                val result = validator.tryValidate(456)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.literal.single"
            }
        }

        context("literal - string") {
            val validator = Kova.literal("abc")

            test("success") {
                val result = validator.tryValidate("abc")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "abc"
            }

            test("failure") {
                val result = validator.tryValidate("de")
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.literal.single"
            }
        }

        context("literals - string vararg") {
            val validator = Kova.literal("aaa", "bbb", "ccc")

            test("success") {
                val result = validator.tryValidate("bbb")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "bbb"
            }

            test("failure") {
                val result = validator.tryValidate("ddd")
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.literal.list"
            }
        }

        context("literals - string list") {
            val validator = Kova.literal(listOf("aaa", "bbb", "ccc"))

            test("success") {
                val result = validator.tryValidate("bbb")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "bbb"
            }

            test("failure") {
                val result = validator.tryValidate("ddd")
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.literal.list"
            }
        }
    })
