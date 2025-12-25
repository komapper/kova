package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec

class MapValidatorTest :
    FunSpec({

        context("plus") {
            context(_: Validation, _: Accumulate)
            fun Map<*, *>.validate() = min(2) + { min(3) }

            test("success") {
                val result = tryValidate { mapOf("a" to "1", "b" to "2", "c" to "3").validate() }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { mapOf("a" to "1").validate() }
                result.shouldBeFailure()
                result.messages.size shouldBe 2
                result.messages[0].constraintId shouldBe "kova.map.min"
                result.messages[1].constraintId shouldBe "kova.map.min"
            }
        }

        context("max") {
            test("success") {
                val result = tryValidate { mapOf("a" to "1", "b" to "2").max(2) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { mapOf("a" to "1", "b" to "2", "c" to "3").max(2) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.map.max"
            }
        }

        context("notEmpty") {
            test("success") {
                val result = tryValidate { mapOf("a" to "1").notEmpty() }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { emptyMap<Nothing, Nothing>().notEmpty() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.map.notEmpty"
            }
        }

        context("length") {
            test("success") {
                val result = tryValidate { mapOf("a" to "1", "b" to "2").length(2) }
                result.shouldBeSuccess()
            }

            test("failure with too few entries") {
                val result = tryValidate { mapOf("a" to "1").length(2) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.map.length"
            }

            test("failure with too many entries") {
                val result = tryValidate { mapOf("a" to "1", "b" to "2", "c" to "3").length(2) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.map.length"
            }
        }

        context("constrain") {
            context(_: Validation, _: Accumulate)
            fun Map<*, *>.validate() = constrain("test") { satisfies(it.size == 1) { text("Constraint failed") } }

            test("success") {
                val result = tryValidate { mapOf("a" to "1").validate() }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { mapOf("a" to "1", "b" to "2").validate() }
                result.shouldBeFailure()
                result.messages.single().text shouldBe "Constraint failed"
            }
        }

        context("onEach") {
            context(_: Validation, _: Accumulate)
            fun <T> Map<T, T>.validate() =
                onEach { v ->
                    v.constrain("test") { (key, value) ->
                        satisfies(key != value) { text("Constraint failed: $key") }
                    }
                }

            test("success") {
                val result = tryValidate { mapOf("a" to "1", "b" to "1").validate() }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { mapOf("a" to "a", "b" to "b").validate() }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.map.onEach"
            }
        }

        context("onEachKey") {
            test("success") {
                val result = tryValidate { mapOf("a" to "1", "b" to "2").onEachKey { it.length(1) } }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { mapOf("a" to "1", "bb" to "2", "ccc" to "3").onEachKey { it.length(1) } }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.map.onEachKey"
            }
        }

        context("onEachValue") {
            test("success") {
                val result = tryValidate { mapOf("a" to "1", "b" to "2").onEachValue { it.length(1) } }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { mapOf("a" to "1", "b" to "22", "c" to "333").onEachValue { it.length(1) } }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.map.onEachValue"
            }
        }

        context("containsKey") {
            test("success") {
                val result = tryValidate { mapOf("foo" to 1, "bar" to 2).hasKey("foo") }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { mapOf("bar" to 2, "baz" to 3).hasKey("foo") }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.map.containsKey"
            }

            test("failure with empty map") {
                val result = tryValidate { emptyMap<String, Nothing>().hasKey("foo") }
                result.shouldBeFailure()
            }
        }

        context("notContainsKey") {
            test("success") {
                val result = tryValidate { mapOf("bar" to 2, "baz" to 3).notContainsKey("foo") }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { mapOf("foo" to 1, "bar" to 2).notContainsKey("foo") }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.map.notContainsKey"
            }

            test("success with empty map") {
                val result = tryValidate { emptyMap<String, Nothing>().notContainsKey("foo") }
                result.shouldBeSuccess()
            }
        }

        context("containsValue") {
            test("success") {
                val result = tryValidate { mapOf("foo" to 42, "bar" to 2).hasValue(42) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { mapOf("foo" to 1, "bar" to 2).hasValue(42) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.map.containsValue"
            }

            test("failure with empty map") {
                val result = tryValidate { emptyMap<Nothing, Int>().hasValue(42) }
                result.shouldBeFailure()
            }
        }

        context("notContainsValue") {
            test("success") {
                val result = tryValidate { mapOf("foo" to 1, "bar" to 2).notContainsValue(42) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { mapOf("foo" to 42, "bar" to 2).notContainsValue(42) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.map.notContainsValue"
            }

            test("success with empty map") {
                val result = tryValidate { emptyMap<Nothing, Int>().notContainsValue(42) }
                result.shouldBeSuccess()
            }
        }
    })
