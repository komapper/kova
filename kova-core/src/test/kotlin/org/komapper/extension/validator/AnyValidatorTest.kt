package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import java.util.Locale

class AnyValidatorTest :
    FunSpec({

        beforeSpec {
            Locale.setDefault(Locale.US)
        }

        context("eqValue (equal)") {
            test("success with equal value") {
                val result = tryValidate { eqValue(5u, 5u) }
                result.shouldBeSuccess()
            }

            test("failure with value greater than threshold") {
                val result = tryValidate { eqValue(6u, 5u) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.any.eqValue"
            }

            test("failure with value less than threshold") {
                val result = tryValidate { eqValue(4u, 5u) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.any.eqValue"
            }
        }

        context("notEqValue (not equal)") {
            test("success with value greater than threshold") {
                val result = tryValidate { notEqValue(6u, 5u) }
                result.shouldBeSuccess()
            }

            test("success with value less than threshold") {
                val result = tryValidate { notEqValue(4u, 5u) }
                result.shouldBeSuccess()
            }

            test("failure with equal value") {
                val result = tryValidate { notEqValue(5u, 5u) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.any.notEqValue"
            }
        }

        context("inIterable") {
            test("success") {
                val result = tryValidate { inIterable("bbb", listOf("aaa", "bbb", "ccc")) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { inIterable("ddd", listOf("aaa", "bbb", "ccc")) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.any.inIterable"
            }
        }
    })
