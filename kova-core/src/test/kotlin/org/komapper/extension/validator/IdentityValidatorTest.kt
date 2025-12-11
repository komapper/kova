package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class IdentityValidatorTest :
    FunSpec({

        context("literal") {
            context("boolean") {
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

            context("int") {
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

            context("string") {
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

            context("vararg") {
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

            context("list") {
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
        }

        context("onlyIf") {
            test("success when condition not met") {
                val validator = Kova.int().min(3).onlyIf { it % 2 == 0 }
                val result = validator.tryValidate(1)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 1
            }

            test("failure when condition met") {
                val validator = Kova.int().min(3).onlyIf { it % 2 == 0 }
                val result = validator.tryValidate(2)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.comparable.min"
            }

            context("with plus") {
                val validator = Kova.int().min(3).onlyIf { it % 2 == 0 } + Kova.int().min(1)

                test("success") {
                    val result = validator.tryValidate(1)
                    result.isSuccess().mustBeTrue()
                    result.value shouldBe 1
                }

                test("failure") {
                    val result = validator.tryValidate(0)
                    result.isFailure().mustBeTrue()
                    result.messages.size shouldBe 2
                    result.messages[0].constraintId shouldBe "kova.comparable.min"
                    result.messages[1].constraintId shouldBe "kova.comparable.min"
                }
            }
        }

        context("constrain") {
            val validator =
                Kova.int().constrain("even") {
                    satisfies(it.input % 2 == 0, it.text("input must be even"))
                }

            test("failure") {
                val result = validator.tryValidate(1)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].text shouldBe "input must be even"
            }
        }

        context("constrain with extension function") {
            fun IdentityValidator<Int>.even() =
                constrain("even") {
                    satisfies(it.input % 2 == 0, it.text("input must be even"))
                }
            val validator = Kova.int().even()

            test("failure") {
                val result = validator.tryValidate(1)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].text shouldBe "input must be even"
            }
        }
    })
