package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MapEntryValidatorTest :
    FunSpec({
        context("constrain") {
            val validator =
                Kova.mapEntry<String, String>().constrain("test") {
                    satisfies(it.input.key != it.input.value, "Constraint failed: ${it.input.key}")
                }

            test("success") {
                val result = validator.tryValidate(mapOf("a" to "1").entries.first())
                result.isSuccess().mustBeTrue()
            }

            test("failure") {
                val result = validator.tryValidate(mapOf("a" to "a").entries.first())
                result.isFailure().mustBeTrue()
                result.messages.single().text shouldBe "Constraint failed: a"
            }
        }
    })
