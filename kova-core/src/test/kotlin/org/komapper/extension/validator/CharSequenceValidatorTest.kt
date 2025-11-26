package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class CharSequenceValidatorTest :
    FunSpec({

        context("stringBuilder") {
            context("min") {
                val min = Kova.charSequence<StringBuilder>().min(3)

                test("success") {
                    val buf = StringBuilder("abc")
                    val result = min.tryValidate(buf)
                    result.isSuccess().mustBeTrue()
                    result.value.toString() shouldBe buf.toString()
                }

                test("failure") {
                    val buf = StringBuilder("ab")
                    val result = min.tryValidate(buf)
                    result.isFailure().mustBeTrue()
                    result.messages[0].content shouldBe "\"ab\" must be at least 3 characters"
                }
            }
        }
    })
