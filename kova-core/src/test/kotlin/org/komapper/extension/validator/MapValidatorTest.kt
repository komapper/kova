package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MapValidatorTest :
    FunSpec({

        context("plus") {
            val validator = Kova.map<String, String>().min(2).min(3)

            test("success") {
                val result = validator.tryValidate(mapOf("a" to "1", "b" to "2", "c" to "3"))
                assertTrue(result.isSuccess())
                result.value shouldBe
                    mapOf(
                        "a" to "1",
                        "b" to "2",
                        "c" to "3",
                    )
            }

            test("failure") {
                val result = validator.tryValidate(mapOf("a" to "1"))
                assertTrue(result.isFailure())
                result.messages.size shouldBe 2
                result.messages[0].content shouldBe "Map(size=1) must have at least 2 entries"
                result.messages[1].content shouldBe "Map(size=1) must have at least 3 entries"
            }
        }

        context("constraint") {
            val validator =
                MapValidator<String, String>().constraint {
                    if (it.input.size == 1) {
                        ConstraintResult.Satisfied
                    } else {
                        ConstraintResult.Violated("Constraint failed")
                    }
                }

            test("success") {
                val result = validator.tryValidate(mapOf("a" to "1"))
                assertTrue(result.isSuccess())
                result.value shouldBe mapOf("a" to "1")
            }

            test("failure") {
                val result = validator.tryValidate(mapOf("a" to "1", "b" to "2"))
                assertTrue(result.isFailure())
                result.messages.single().content shouldBe "Constraint failed"
            }
        }

        context("onEach") {
            val validator =
                MapValidator<String, String>().onEach(
                    MapEntryValidator<String, String>().constraint {
                        if (it.input.key != it.input.value) {
                            ConstraintResult.Satisfied
                        } else {
                            ConstraintResult.Violated("Constraint failed: ${it.input.key}")
                        }
                    },
                )

            test("success") {
                val result = validator.tryValidate(mapOf("a" to "1", "b" to "1"))
                assertTrue(result.isSuccess())
                result.value shouldBe mapOf("a" to "1", "b" to "1")
            }

            test("failure") {
                val result = validator.tryValidate(mapOf("a" to "a", "b" to "b"))
                assertTrue(result.isFailure())
                result.messages[0].content shouldBe "Constraint failed: a"
                result.messages[1].content shouldBe "Constraint failed: b"
            }
        }

        context("onEachKey") {
            val validator = MapValidator<String, String>().onEachKey(Kova.string().length(1))

            test("success") {
                val result = validator.tryValidate(mapOf("a" to "1", "b" to "2"))
                assertTrue(result.isSuccess())
                result.value shouldBe mapOf("a" to "1", "b" to "2")
            }

            test("failure") {
                val result = validator.tryValidate(mapOf("a" to "1", "bb" to "2", "ccc" to "3"))
                assertTrue(result.isFailure())
                result.details.size shouldBe 2
                result.details[0].let {
                    it.root shouldBe ""
                    it.path shouldBe "<map key>"
                    it.message.content shouldBe "\"bb\" must be exactly 1 characters"
                }
                result.details[1].let {
                    it.root shouldBe ""
                    it.path shouldBe "<map key>"
                    it.message.content shouldBe "\"ccc\" must be exactly 1 characters"
                }
            }
        }

        context("onEachValue") {
            val validator = MapValidator<String, String>().onEachValue(Kova.string().length(1))

            test("success") {
                val result = validator.tryValidate(mapOf("a" to "1", "b" to "2"))
                assertTrue(result.isSuccess())
                result.value shouldBe mapOf("a" to "1", "b" to "2")
            }

            test("failure") {
                val result = validator.tryValidate(mapOf("a" to "1", "b" to "22", "c" to "333"))
                assertTrue(result.isFailure())
                result.details.size shouldBe 2
                result.details[0].let {
                    it.root shouldBe ""
                    it.path shouldBe "[b]<map value>"
                    it.message.content shouldBe "\"22\" must be exactly 1 characters"
                }
                result.details[1].let {
                    it.root shouldBe ""
                    it.path shouldBe "[c]<map value>"
                    it.message.content shouldBe "\"333\" must be exactly 1 characters"
                }
            }
        }
    })
