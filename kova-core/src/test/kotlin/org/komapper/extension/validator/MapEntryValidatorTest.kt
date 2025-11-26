package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.test.assertTrue

class MapEntryValidatorTest :
    FunSpec({
        context("constraint") {
            val validator =
                Kova.mapEntry<String, String>().constraint {
                    if (it.input.key != it.input.value) {
                        ConstraintResult.Satisfied
                    } else {
                        ConstraintResult.Violated("Constraint failed: ${it.input.key}")
                    }
                }

            test("success") {
                val result = validator.tryValidate(mapOf("a" to "1").entries.first())
                assertTrue(result.isSuccess())
            }

            test("failure") {
                val result = validator.tryValidate(mapOf("a" to "a").entries.first())
                assertTrue(result.isFailure())
                result.messages.single().content shouldBe "Constraint failed: a"
            }
        }
    })
