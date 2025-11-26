package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.util.EnumSet

class EnumValidatorTest :
    FunSpec({

        context("contains") {
            val validator = Kova.enum<Color>().contains(EnumSet.of(Color.RED, Color.GREEN))

            test("success") {
                val result = validator.tryValidate(Color.RED)
                result.isSuccess().mustBeTrue()
                result.value shouldBe Color.RED
            }

            test("failure") {
                val result = validator.tryValidate(Color.BLUE)
                result.isFailure().mustBeTrue()
                result.messages[0].content shouldBe "Enum BLUE must be one of [RED, GREEN]"
            }
        }
    }) {
    enum class Color {
        RED,
        GREEN,
        BLUE,
    }
}
