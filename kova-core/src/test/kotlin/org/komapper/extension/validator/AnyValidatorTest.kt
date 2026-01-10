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
                val result = tryValidate { 5u.ensureEquals(5u) }
                result.shouldBeSuccess()
            }

            test("failure with value greater than threshold") {
                val result = tryValidate { 6u.ensureEquals(5u) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.any.equals"
            }

            test("failure with value less than threshold") {
                val result = tryValidate { 4u.ensureEquals(5u) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.any.equals"
            }
        }

        context("ensureNotEquals (not equal)") {
            test("success with value greater than threshold") {
                val result = tryValidate { 6u.ensureNotEquals(5u) }
                result.shouldBeSuccess()
            }

            test("success with value less than threshold") {
                val result = tryValidate { 4u.ensureNotEquals(5u) }
                result.shouldBeSuccess()
            }

            test("failure with equal value") {
                val result = tryValidate { 5u.ensureNotEquals(5u) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.any.notEquals"
            }
        }

        context("ensureIn") {
            test("success") {
                val result = tryValidate { "bbb".ensureIn(listOf("aaa", "bbb", "ccc")) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { "ddd".ensureIn(listOf("aaa", "bbb", "ccc")) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.any.in"
            }
        }
    })
