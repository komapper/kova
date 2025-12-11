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
                result.messages[0].constraintId shouldBe "kova.map.min"
                result.messages[1].constraintId shouldBe "kova.map.min"
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
                result.messages.single().constraintId shouldBe "kova.map.max"
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
                result.messages.single().constraintId shouldBe "kova.map.notEmpty"
            }
        }

        context("length") {
            val validator = Kova.map<String, String>().length(2)

            test("success") {
                val result = validator.tryValidate(mapOf("a" to "1", "b" to "2"))
                result.isSuccess().mustBeTrue()
                result.value shouldBe mapOf("a" to "1", "b" to "2")
            }

            test("failure with too few entries") {
                val result = validator.tryValidate(mapOf("a" to "1"))
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.map.length"
            }

            test("failure with too many entries") {
                val result = validator.tryValidate(mapOf("a" to "1", "b" to "2", "c" to "3"))
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.map.length"
            }
        }

        context("constrain") {
            val validator =
                Kova.map<String, String>().constrain("test") {
                    satisfies(it.input.size == 1, it.text("Constraint failed"))
                }

            test("success") {
                val result = validator.tryValidate(mapOf("a" to "1"))
                result.isSuccess().mustBeTrue()
                result.value shouldBe mapOf("a" to "1")
            }

            test("failure") {
                val result = validator.tryValidate(mapOf("a" to "1", "b" to "2"))
                result.isFailure().mustBeTrue()
                result.messages.single().text shouldBe "Constraint failed"
            }
        }

        context("onEach") {
            val validator =
                Kova.map<String, String>().onEach { v ->
                    v.constrain("test") {
                        satisfies(it.input.key != it.input.value, it.text("Constraint failed: ${it.input.key}"))
                    }
                }

            test("success") {
                val result = validator.tryValidate(mapOf("a" to "1", "b" to "1"))
                result.isSuccess().mustBeTrue()
                result.value shouldBe mapOf("a" to "1", "b" to "1")
            }

            test("failure") {
                val result = validator.tryValidate(mapOf("a" to "a", "b" to "b"))
                result.isFailure().mustBeTrue()
                result.messages[0].constraintId shouldBe "kova.map.onEach"
            }
        }

        context("onEachKey") {
            val validator = Kova.map<String, String>().onEachKey { it.length(1) }

            test("success") {
                val result = validator.tryValidate(mapOf("a" to "1", "b" to "2"))
                result.isSuccess().mustBeTrue()
                result.value shouldBe mapOf("a" to "1", "b" to "2")
            }

            test("failure") {
                val result = validator.tryValidate(mapOf("a" to "1", "bb" to "2", "ccc" to "3"))
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.map.onEachKey"
            }
        }

        context("onEachValue") {
            val validator = Kova.map<String, String>().onEachValue { it.length(1) }

            test("success") {
                val result = validator.tryValidate(mapOf("a" to "1", "b" to "2"))
                result.isSuccess().mustBeTrue()
                result.value shouldBe mapOf("a" to "1", "b" to "2")
            }

            test("failure") {
                val result = validator.tryValidate(mapOf("a" to "1", "b" to "22", "c" to "333"))
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.map.onEachValue"
            }
        }

        context("containsKey") {
            val validator = Kova.map<String, Int>().containsKey("foo")

            test("success") {
                val result = validator.tryValidate(mapOf("foo" to 1, "bar" to 2))
                result.isSuccess().mustBeTrue()
                result.value shouldBe mapOf("foo" to 1, "bar" to 2)
            }

            test("failure") {
                val result = validator.tryValidate(mapOf("bar" to 2, "baz" to 3))
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.map.containsKey"
            }

            test("failure with empty map") {
                val result = validator.tryValidate(emptyMap())
                result.isFailure().mustBeTrue()
            }
        }

        context("notContainsKey") {
            val validator = Kova.map<String, Int>().notContainsKey("foo")

            test("success") {
                val result = validator.tryValidate(mapOf("bar" to 2, "baz" to 3))
                result.isSuccess().mustBeTrue()
                result.value shouldBe mapOf("bar" to 2, "baz" to 3)
            }

            test("failure") {
                val result = validator.tryValidate(mapOf("foo" to 1, "bar" to 2))
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.map.notContainsKey"
            }

            test("success with empty map") {
                val result = validator.tryValidate(emptyMap())
                result.isSuccess().mustBeTrue()
            }
        }

        context("containsValue") {
            val validator = Kova.map<String, Int>().containsValue(42)

            test("success") {
                val result = validator.tryValidate(mapOf("foo" to 42, "bar" to 2))
                result.isSuccess().mustBeTrue()
                result.value shouldBe mapOf("foo" to 42, "bar" to 2)
            }

            test("failure") {
                val result = validator.tryValidate(mapOf("foo" to 1, "bar" to 2))
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.map.containsValue"
            }

            test("failure with empty map") {
                val result = validator.tryValidate(emptyMap())
                result.isFailure().mustBeTrue()
            }
        }

        context("notContainsValue") {
            val validator = Kova.map<String, Int>().notContainsValue(42)

            test("success") {
                val result = validator.tryValidate(mapOf("foo" to 1, "bar" to 2))
                result.isSuccess().mustBeTrue()
                result.value shouldBe mapOf("foo" to 1, "bar" to 2)
            }

            test("failure") {
                val result = validator.tryValidate(mapOf("foo" to 42, "bar" to 2))
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.map.notContainsValue"
            }

            test("success with empty map") {
                val result = validator.tryValidate(emptyMap())
                result.isSuccess().mustBeTrue()
            }
        }
    })
