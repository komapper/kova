package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec

class IdentityValidatorTest :
    FunSpec({

        context("literal") {
            context("boolean") {
                test("success") {
                    val result = tryValidate { literal(input = true, value = true) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { literal(input = false, value = true) }
                    result.shouldBeFailure()
                    result.messages.size shouldBe 1
                    result.messages[0].constraintId shouldBe "kova.literal.single"
                }
            }

            context("int") {
                test("success") {
                    val result = tryValidate { literal(123, 123) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { literal(456, 123) }
                    result.shouldBeFailure()
                    result.messages.size shouldBe 1
                    result.messages[0].constraintId shouldBe "kova.literal.single"
                }
            }

            context("string") {
                test("success") {
                    val result = tryValidate { literal("abc", "abc") }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { literal("de", "abc") }
                    result.shouldBeFailure()
                    result.messages.size shouldBe 1
                    result.messages[0].constraintId shouldBe "kova.literal.single"
                }
            }

            context("vararg") {
                test("success") {
                    val result = tryValidate { literal("bbb", "aaa", "bbb", "ccc") }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { literal("ddd", "aaa", "bbb", "ccc") }
                    result.shouldBeFailure()
                    result.messages.size shouldBe 1
                    result.messages[0].constraintId shouldBe "kova.literal.list"
                }
            }

            context("list") {
                test("success") {
                    val result = tryValidate { literal("bbb", listOf("aaa", "bbb", "ccc")) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { literal("ddd", listOf("aaa", "bbb", "ccc")) }
                    result.shouldBeFailure()
                    result.messages.size shouldBe 1
                    result.messages[0].constraintId shouldBe "kova.literal.list"
                }
            }
        }

        context("onlyIf") {
            fun Validation.validate(i: Int) {
                if (i % 2 == 0) min(i, 3)
            }
            test("success when condition not met") {
                val result = tryValidate { validate(1) }
                result.shouldBeSuccess()
            }

            test("failure when condition met") {
                val result = tryValidate { validate(2) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.comparable.min"
            }

            context("with plus") {
                fun Validation.validateAndMin1(i: Int) {
                    if (i % 2 == 0) min(i, 3)
                    min(i, 1)
                }

                test("success") {
                    val result = tryValidate { validateAndMin1(1) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { validateAndMin1(0) }
                    result.shouldBeFailure()
                    result.messages.size shouldBe 2
                    result.messages[0].constraintId shouldBe "kova.comparable.min"
                    result.messages[1].constraintId shouldBe "kova.comparable.min"
                }
            }
        }

        context("constrain") {
            @IgnorableReturnValue
            fun Validation.validate(i: Int) = i.constrain("even") { satisfies(it % 2 == 0) { text("input must be even") } }

            test("failure") {
                val result = tryValidate { validate(1) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].text shouldBe "input must be even"
            }
        }

        context("constrain with extension function") {
            @IgnorableReturnValue
            fun Validation.even(i: Int) = i.constrain("even") { satisfies(it % 2 == 0) { text("input must be even") } }

            test("failure") {
                val result = tryValidate { even(1) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].text shouldBe "input must be even"
            }
        }
    })
