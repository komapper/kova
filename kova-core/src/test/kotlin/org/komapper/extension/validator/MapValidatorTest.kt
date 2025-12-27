package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec

class MapValidatorTest :
    FunSpec({

        context("plus") {
            context(_: Validation, _: Accumulate)
            fun Map<*, *>.validate() {
                min(this, 2)
                min(this, 3)
            }

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
                val result = tryValidate { max(mapOf("a" to "1", "b" to "2"), 2) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { max(mapOf("a" to "1", "b" to "2", "c" to "3"), 2) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.map.max"
            }
        }

        context("notEmpty") {
            test("success") {
                val result = tryValidate { notEmpty(mapOf("a" to "1")) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { notEmpty(emptyMap<Nothing, Nothing>()) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.map.notEmpty"
            }
        }

        context("length") {
            test("success") {
                val result = tryValidate { length(mapOf("a" to "1", "b" to "2"), 2) }
                result.shouldBeSuccess()
            }

            test("failure with too few entries") {
                val result = tryValidate { length(mapOf("a" to "1"), 2) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.map.length"
            }

            test("failure with too many entries") {
                val result = tryValidate { length(mapOf("a" to "1", "b" to "2", "c" to "3"), 2) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.map.length"
            }
        }

        context("constrain") {
            @IgnorableReturnValue
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
            @IgnorableReturnValue
            context(_: Validation, _: Accumulate)
            fun <T> Map<T, T>.validate() =
                onEach(this) { v ->
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
                val result = tryValidate { onEachKey(mapOf("a" to "1", "b" to "2")) { length(it, 1) } }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { onEachKey(mapOf("a" to "1", "bb" to "2", "ccc" to "3")) { length(it, 1) } }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.map.onEachKey"
            }
        }

        context("onEachValue") {
            test("success") {
                val result = tryValidate { onEachValue(mapOf("a" to "1", "b" to "2")) { length(it, 1) } }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { onEachValue(mapOf("a" to "1", "b" to "22", "c" to "333")) { length(it, 1) } }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.map.onEachValue"
            }
        }

        context("containsKey") {
            test("success") {
                val result = tryValidate { hasKey(mapOf("foo" to 1, "bar" to 2), "foo") }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { hasKey(mapOf("bar" to 2, "baz" to 3), "foo") }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.map.containsKey"
            }

            test("failure with empty map") {
                val result = tryValidate { hasKey(emptyMap<String, Nothing>(), "foo") }
                result.shouldBeFailure()
            }
        }

        context("notContainsKey") {
            test("success") {
                val result = tryValidate { notContainsKey(mapOf("bar" to 2, "baz" to 3), "foo") }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { notContainsKey(mapOf("foo" to 1, "bar" to 2), "foo") }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.map.notContainsKey"
            }

            test("success with empty map") {
                val result = tryValidate { notContainsKey(emptyMap<String, Nothing>(), "foo") }
                result.shouldBeSuccess()
            }
        }

        context("containsValue") {
            test("success") {
                val result = tryValidate { hasValue(mapOf("foo" to 42, "bar" to 2), 42) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { hasValue(mapOf("foo" to 1, "bar" to 2), 42) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.map.containsValue"
            }

            test("failure with empty map") {
                val result = tryValidate { hasValue(emptyMap<Nothing, Int>(), 42) }
                result.shouldBeFailure()
            }
        }

        context("notContainsValue") {
            test("success") {
                val result = tryValidate { notContainsValue(mapOf("foo" to 1, "bar" to 2), 42) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { notContainsValue(mapOf("foo" to 42, "bar" to 2), 42) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.map.notContainsValue"
            }

            test("success with empty map") {
                val result = tryValidate { notContainsValue(emptyMap<Nothing, Int>(), 42) }
                result.shouldBeSuccess()
            }
        }
    })
