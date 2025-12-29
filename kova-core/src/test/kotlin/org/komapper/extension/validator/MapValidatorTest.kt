package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import java.util.Locale

class MapValidatorTest :
    FunSpec({

        beforeSpec {
            Locale.setDefault(Locale.US)
        }

        context("maxSize") {
            test("success") {
                val result = tryValidate { maxSize(mapOf("a" to "1", "b" to "2"), 2) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { maxSize(mapOf("a" to "1", "b" to "2", "c" to "3"), 2) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.map.maxSize"
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

        context("size") {
            test("success") {
                val result = tryValidate { size(mapOf("a" to "1", "b" to "2"), 2) }
                result.shouldBeSuccess()
            }

            test("failure with too few entries") {
                val result = tryValidate { size(mapOf("a" to "1"), 2) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.map.size"
            }

            test("failure with too many entries") {
                val result = tryValidate { size(mapOf("a" to "1", "b" to "2", "c" to "3"), 2) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.map.size"
            }
        }

        context("constrain") {
            @IgnorableReturnValue
            fun Validation.validate(map: Map<*, *>) = map.constrain("test") { satisfies(it.size == 1) { text("Constraint failed") } }

            test("success") {
                val result = tryValidate { validate(mapOf("a" to "1")) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { validate(mapOf("a" to "1", "b" to "2")) }
                result.shouldBeFailure()
                result.messages.single().text shouldBe "Constraint failed"
            }
        }

        context("onEach") {
            @IgnorableReturnValue
            fun <T> Validation.validate(map: Map<T, T>) =
                onEach(map) { v ->
                    v.constrain("test") { (key, value) ->
                        satisfies(key != value) { text("Constraint failed: $key") }
                    }
                }

            test("success") {
                val result = tryValidate { validate(mapOf("a" to "1", "b" to "1")) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { validate(mapOf("a" to "a", "b" to "b")) }
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

        context("hasKey") {
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

        context("containsKey") {
            test("success") {
                val result = tryValidate { containsKey(mapOf("foo" to 1, "bar" to 2), "foo") }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { containsKey(mapOf("bar" to 2, "baz" to 3), "foo") }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.map.containsKey"
            }

            test("failure with empty map") {
                val result = tryValidate { containsKey(emptyMap<String, Nothing>(), "foo") }
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

        context("hasValue") {
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

        context("containsValue") {
            test("success") {
                val result = tryValidate { containsValue(mapOf("foo" to 42, "bar" to 2), 42) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { containsValue(mapOf("foo" to 1, "bar" to 2), 42) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.map.containsValue"
            }

            test("failure with empty map") {
                val result = tryValidate { containsValue(emptyMap<Nothing, Int>(), 42) }
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
