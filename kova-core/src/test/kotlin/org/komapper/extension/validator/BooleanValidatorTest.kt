package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import java.util.Locale

class BooleanValidatorTest :
    FunSpec({

        beforeSpec {
            Locale.setDefault(Locale.US)
        }

        context("ensureTrue") {
            test("success with true value") {
                val result = tryValidate { ensureTrue(true) }
                result.shouldBeSuccess()
            }

            test("failure with false value") {
                val result = tryValidate { ensureTrue(false) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.boolean.true"
                result.messages[0].text shouldBe "must be true"
            }
        }

        context("ensureFalse") {
            test("success with false value") {
                val result = tryValidate { ensureFalse(false) }
                result.shouldBeSuccess()
            }

            test("failure with true value") {
                val result = tryValidate { ensureFalse(true) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.boolean.false"
                result.messages[0].text shouldBe "must be false"
            }
        }
    })
