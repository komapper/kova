package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.text.MessageFormat

class CoreValidatorTest :
    FunSpec({

        test("getPattern") {
            val pattern = CoreValidator.getPattern("kova.number.min")
            val formatted = MessageFormat.format(pattern, 10, 0)
            formatted shouldBe "Number 10 must be greater than or equal to 0"
        }
    })
