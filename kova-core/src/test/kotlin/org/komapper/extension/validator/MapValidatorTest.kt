package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import java.util.Locale

class MapValidatorTest :
    FunSpec({

        beforeSpec {
            Locale.setDefault(Locale.US)
        }

        context("ensureMaxSize") {
            test("success") {
                val result = tryValidate { ensureMaxSize(mapOf("a" to "1", "b" to "2"), 2) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { ensureMaxSize(mapOf("a" to "1", "b" to "2", "c" to "3"), 2) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.map.maxSize"
            }
        }

        context("ensureNotEmpty") {
            test("success") {
                val result = tryValidate { ensureNotEmpty(mapOf("a" to "1")) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { ensureNotEmpty(emptyMap<Nothing, Nothing>()) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.map.notEmpty"
            }
        }

        context("ensureSize") {
            test("success") {
                val result = tryValidate { ensureSize(mapOf("a" to "1", "b" to "2"), 2) }
                result.shouldBeSuccess()
            }

            test("failure with too few entries") {
                val result = tryValidate { ensureSize(mapOf("a" to "1"), 2) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.map.size"
            }

            test("failure with too many entries") {
                val result = tryValidate { ensureSize(mapOf("a" to "1", "b" to "2", "c" to "3"), 2) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.map.size"
            }
        }

        context("ensureSizeInRange") {
            test("success with closed range") {
                val result = tryValidate { ensureSizeInRange(mapOf("a" to "1", "b" to "2", "c" to "3"), 2..5) }
                result.shouldBeSuccess()
            }

            test("success at lower bound of closed range") {
                val result = tryValidate { ensureSizeInRange(mapOf("a" to "1", "b" to "2"), 2..5) }
                result.shouldBeSuccess()
            }

            test("success at upper bound of closed range") {
                val result = tryValidate { ensureSizeInRange(mapOf("a" to "1", "b" to "2", "c" to "3", "d" to "4", "e" to "5"), 2..5) }
                result.shouldBeSuccess()
            }

            test("success with open-ended range") {
                val result = tryValidate { ensureSizeInRange(mapOf("a" to "1", "b" to "2", "c" to "3"), 2..<5) }
                result.shouldBeSuccess()
            }

            test("success at lower bound of open-ended range") {
                val result = tryValidate { ensureSizeInRange(mapOf("a" to "1", "b" to "2"), 2..<5) }
                result.shouldBeSuccess()
            }

            test("failure below range") {
                val result = tryValidate { ensureSizeInRange(mapOf("a" to "1"), 2..5) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.map.sizeInRange"
            }

            test("failure above closed range") {
                val result =
                    tryValidate {
                        ensureSizeInRange(
                            mapOf("a" to "1", "b" to "2", "c" to "3", "d" to "4", "e" to "5", "f" to "6"),
                            2..5,
                        )
                    }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.map.sizeInRange"
            }

            test("failure at upper bound of open-ended range") {
                val result = tryValidate { ensureSizeInRange(mapOf("a" to "1", "b" to "2", "c" to "3", "d" to "4", "e" to "5"), 2..<5) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.map.sizeInRange"
            }

            test("success with custom message") {
                val result =
                    tryValidate {
                        ensureSizeInRange(
                            mapOf("a" to "1", "b" to "2", "c" to "3"),
                            2..5,
                        ) { text("Custom message") }
                    }
                result.shouldBeSuccess()
            }

            test("failure with custom message") {
                val result =
                    tryValidate {
                        ensureSizeInRange(
                            mapOf("a" to "1"),
                            2..5,
                        ) { text("Custom message") }
                    }
                result.shouldBeFailure()
                result.messages.single().text shouldBe "Custom message"
            }
        }

        context("constrain") {
            @IgnorableReturnValue
            context(_: Validation)
            fun validate(map: Map<*, *>) = map.constrain("test") { satisfies(it.size == 1) { text("Constraint failed") } }

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

        context("ensureEach") {
            @IgnorableReturnValue
            context(_: Validation)
            fun <T> validate(map: Map<T, T>) =
                ensureEach(map) { v ->
                    v.constrain("test") { (key, value) ->
                        satisfies(key != value) { text("Constraint failed: $key") }
                    }
                }

            test("success") {
                val result = tryValidate { validate(mapOf("a" to "1", "b" to "1")) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val input = mapOf("a" to "a", "b" to "b")
                val result = tryValidate { validate(input) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.map.each"
                result.messages[0].input shouldBe input
            }
        }

        context("ensureEachKey") {
            test("success") {
                val result = tryValidate { ensureEachKey(mapOf("a" to "1", "b" to "2")) { ensureLength(it, 1) } }
                result.shouldBeSuccess()
            }

            test("failure") {
                val input = mapOf("a" to "1", "bb" to "2", "ccc" to "3")
                val result = tryValidate { ensureEachKey(input) { ensureLength(it, 1) } }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.map.eachKey"
                result.messages[0].input shouldBe input
            }
        }

        context("ensureEachValue") {
            test("success") {
                val result = tryValidate { ensureEachValue(mapOf("a" to "1", "b" to "2")) { ensureLength(it, 1) } }
                result.shouldBeSuccess()
            }

            test("failure") {
                val input = mapOf("a" to "1", "b" to "22", "c" to "333")
                val result = tryValidate { ensureEachValue(input) { ensureLength(it, 1) } }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.map.eachValue"
                result.messages[0].input shouldBe input
            }
        }

        context("ensureHasKey") {
            test("success") {
                val result = tryValidate { ensureHasKey(mapOf("foo" to 1, "bar" to 2), "foo") }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { ensureHasKey(mapOf("bar" to 2, "baz" to 3), "foo") }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.map.containsKey"
            }

            test("failure with ensureEmpty map") {
                val result = tryValidate { ensureHasKey(emptyMap<String, Nothing>(), "foo") }
                result.shouldBeFailure()
            }
        }

        context("ensureContainsKey") {
            test("success") {
                val result = tryValidate { ensureContainsKey(mapOf("foo" to 1, "bar" to 2), "foo") }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { ensureContainsKey(mapOf("bar" to 2, "baz" to 3), "foo") }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.map.containsKey"
            }

            test("failure with ensureEmpty map") {
                val result = tryValidate { ensureContainsKey(emptyMap<String, Nothing>(), "foo") }
                result.shouldBeFailure()
            }
        }

        context("ensureNotContainsKey") {
            test("success") {
                val result = tryValidate { ensureNotContainsKey(mapOf("bar" to 2, "baz" to 3), "foo") }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { ensureNotContainsKey(mapOf("foo" to 1, "bar" to 2), "foo") }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.map.notContainsKey"
            }

            test("success with ensureEmpty map") {
                val result = tryValidate { ensureNotContainsKey(emptyMap<String, Nothing>(), "foo") }
                result.shouldBeSuccess()
            }
        }

        context("ensureHasValue") {
            test("success") {
                val result = tryValidate { ensureHasValue(mapOf("foo" to 42, "bar" to 2), 42) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { ensureHasValue(mapOf("foo" to 1, "bar" to 2), 42) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.map.containsValue"
            }

            test("failure with ensureEmpty map") {
                val result = tryValidate { ensureHasValue(emptyMap<Nothing, Int>(), 42) }
                result.shouldBeFailure()
            }
        }

        context("ensureContainsValue") {
            test("success") {
                val result = tryValidate { ensureContainsValue(mapOf("foo" to 42, "bar" to 2), 42) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { ensureContainsValue(mapOf("foo" to 1, "bar" to 2), 42) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.map.containsValue"
            }

            test("failure with ensureEmpty map") {
                val result = tryValidate { ensureContainsValue(emptyMap<Nothing, Int>(), 42) }
                result.shouldBeFailure()
            }
        }

        context("ensureNotContainsValue") {
            test("success") {
                val result = tryValidate { ensureNotContainsValue(mapOf("foo" to 1, "bar" to 2), 42) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { ensureNotContainsValue(mapOf("foo" to 42, "bar" to 2), 42) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.map.notContainsValue"
            }

            test("success with ensureEmpty map") {
                val result = tryValidate { ensureNotContainsValue(emptyMap<Nothing, Int>(), 42) }
                result.shouldBeSuccess()
            }
        }
    })
