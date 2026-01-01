package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import java.util.Locale

class AnyValidatorTest :
    FunSpec({

        beforeSpec {
            Locale.setDefault(Locale.US)
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
