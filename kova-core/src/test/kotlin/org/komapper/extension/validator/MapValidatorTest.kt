package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MapValidatorTest :
    FunSpec({

        context("plus") {
            val validator = Kova.map<String, String>().min(2).min(3)

            test("success") {
                val result = validator.tryValidate(mapOf("a" to "1", "b" to "2", "c" to "3"))
                result.isSuccess().mustBeTrue()
                result.value shouldBe
                    mapOf(
                        "a" to "1",
                        "b" to "2",
                        "c" to "3",
                    )
            }

            test("failure") {
                val result = validator.tryValidate(mapOf("a" to "1"))
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 2
                result.messages[0].content shouldBe "Map(size=1) must have at least 2 entries"
                result.messages[1].content shouldBe "Map(size=1) must have at least 3 entries"
            }
        }

        context("max") {
            val validator = Kova.map<String, String>().max(2)

            test("success") {
                val result = validator.tryValidate(mapOf("a" to "1", "b" to "2"))
                result.isSuccess().mustBeTrue()
                result.value shouldBe mapOf("a" to "1", "b" to "2")
            }

            test("failure") {
                val result = validator.tryValidate(mapOf("a" to "1", "b" to "2", "c" to "3"))
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "Map(size=3) must have at most 2 entries"
            }
        }

        context("notEmpty") {
            val validator = Kova.map<String, String>().notEmpty()

            test("success") {
                val result = validator.tryValidate(mapOf("a" to "1"))
                result.isSuccess().mustBeTrue()
                result.value shouldBe mapOf("a" to "1")
            }

            test("failure") {
                val result = validator.tryValidate(emptyMap())
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "Map {} must not be empty"
            }
        }

        context("length") {
            val validator = Kova.map<String, String>().length(2)

            test("success") {
                val result = validator.tryValidate(mapOf("a" to "1", "b" to "2"))
                result.isSuccess().mustBeTrue()
                result.value shouldBe mapOf("a" to "1", "b" to "2")
            }

            test("failure - too few") {
                val result = validator.tryValidate(mapOf("a" to "1"))
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "Map {a=1} must have exactly 2 entries"
            }

            test("failure - too many") {
                val result = validator.tryValidate(mapOf("a" to "1", "b" to "2", "c" to "3"))
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "Map {a=1, b=2, c=3} must have exactly 2 entries"
            }
        }

        context("constrain") {
            val validator =
                MapValidator<String, String>().constrain("test") {
                    satisfies(it.input.size == 1, "Constraint failed")
                }

            test("success") {
                val result = validator.tryValidate(mapOf("a" to "1"))
                result.isSuccess().mustBeTrue()
                result.value shouldBe mapOf("a" to "1")
            }

            test("failure") {
                val result = validator.tryValidate(mapOf("a" to "1", "b" to "2"))
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "Constraint failed"
            }
        }

        context("onEach") {
            val validator =
                MapValidator<String, String>().onEach(
                    MapEntryValidator<String, String>().constrain("test") {
                        satisfies(it.input.key != it.input.value, "Constraint failed: ${it.input.key}")
                    },
                )

            test("success") {
                val result = validator.tryValidate(mapOf("a" to "1", "b" to "1"))
                result.isSuccess().mustBeTrue()
                result.value shouldBe mapOf("a" to "1", "b" to "1")
            }

            test("failure") {
                val result = validator.tryValidate(mapOf("a" to "a", "b" to "b"))
                result.isFailure().mustBeTrue()
                result.messages[0].content shouldBe "Constraint failed: a"
                result.messages[1].content shouldBe "Constraint failed: b"
            }
        }

        context("onEachKey") {
            val validator = MapValidator<String, String>().onEachKey(Kova.string().length(1))

            test("success") {
                val result = validator.tryValidate(mapOf("a" to "1", "b" to "2"))
                result.isSuccess().mustBeTrue()
                result.value shouldBe mapOf("a" to "1", "b" to "2")
            }

            test("failure") {
                val result = validator.tryValidate(mapOf("a" to "1", "bb" to "2", "ccc" to "3"))
                result.isFailure().mustBeTrue()
                result.details.size shouldBe 2
                result.details[0].let {
                    it.root shouldBe ""
                    it.path.fullName shouldBe "<map key>"
                    it.message.content shouldBe "\"bb\" must be exactly 1 characters"
                }
                result.details[1].let {
                    it.root shouldBe ""
                    it.path.fullName shouldBe "<map key>"
                    it.message.content shouldBe "\"ccc\" must be exactly 1 characters"
                }
            }
        }

        context("onEachValue") {
            val validator = MapValidator<String, String>().onEachValue(Kova.string().length(1))

            test("success") {
                val result = validator.tryValidate(mapOf("a" to "1", "b" to "2"))
                result.isSuccess().mustBeTrue()
                result.value shouldBe mapOf("a" to "1", "b" to "2")
            }

            test("failure") {
                val result = validator.tryValidate(mapOf("a" to "1", "b" to "22", "c" to "333"))
                result.isFailure().mustBeTrue()
                result.details.size shouldBe 2
                result.details[0].let {
                    it.root shouldBe ""
                    it.path.fullName shouldBe "[b]<map value>"
                    it.message.content shouldBe "\"22\" must be exactly 1 characters"
                }
                result.details[1].let {
                    it.root shouldBe ""
                    it.path.fullName shouldBe "[c]<map value>"
                    it.message.content shouldBe "\"333\" must be exactly 1 characters"
                }
            }
        }
    })
