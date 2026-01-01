package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import java.util.Locale

class BooleanValidatorTest :
    FunSpec({

        beforeSpec {
            Locale.setDefault(Locale.US)
        }

        context("isTrue") {
            test("success with true value") {
                val result = tryValidate { isTrue(true) }
                result.shouldBeSuccess()
            }

            test("failure with false value") {
                val result = tryValidate { isTrue(false) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.boolean.isTrue"
                result.messages[0].text shouldBe "must be true"
            }
        }

        context("isFalse") {
            test("success with false value") {
                val result = tryValidate { isFalse(false) }
                result.shouldBeSuccess()
            }

            test("failure with true value") {
                val result = tryValidate { isFalse(true) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.boolean.isFalse"
                result.messages[0].text shouldBe "must be false"
            }
        }
    })
