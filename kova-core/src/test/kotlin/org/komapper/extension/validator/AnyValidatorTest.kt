package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import java.util.Locale

class AnyValidatorTest :
    FunSpec({

        beforeSpec {
            Locale.setDefault(Locale.US)
        }

        context("ensureEquals (equal)") {
            test("success with equal value") {
                val result = tryValidate { ensureEquals(5u, 5u) }
                result.shouldBeSuccess()
            }

            test("failure with value greater than threshold") {
                val result = tryValidate { ensureEquals(6u, 5u) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.any.equals"
            }

            test("failure with value less than threshold") {
                val result = tryValidate { ensureEquals(4u, 5u) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.any.equals"
            }
        }

        context("ensureNotEquals (not equal)") {
            test("success with value greater than threshold") {
                val result = tryValidate { ensureNotEquals(6u, 5u) }
                result.shouldBeSuccess()
            }

            test("success with value less than threshold") {
                val result = tryValidate { ensureNotEquals(4u, 5u) }
                result.shouldBeSuccess()
            }

            test("failure with equal value") {
                val result = tryValidate { ensureNotEquals(5u, 5u) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.any.notEquals"
            }
        }

        context("ensureIn") {
            test("success") {
                val result = tryValidate { ensureIn("bbb", listOf("aaa", "bbb", "ccc")) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { ensureIn("ddd", listOf("aaa", "bbb", "ccc")) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.any.in"
            }
        }
    })
