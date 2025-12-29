package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import java.util.Locale

class LiteralValidatorTest :
    FunSpec({

        beforeSpec {
            Locale.setDefault(Locale.US)
        }

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
    })
