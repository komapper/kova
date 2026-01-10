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
                val result = tryValidate { mapOf("a" to "1", "b" to "2").ensureMaxSize(2) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { mapOf("a" to "1", "b" to "2", "c" to "3").ensureMaxSize(2) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.map.maxSize"
            }
        }

        context("ensureNotEmpty") {
            test("success") {
                val result = tryValidate { mapOf("a" to "1").ensureNotEmpty() }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { emptyMap<Nothing, Nothing>().ensureNotEmpty() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.map.notEmpty"
            }
        }

        context("ensureSize") {
            test("success") {
                val result = tryValidate { mapOf("a" to "1", "b" to "2").ensureSize(2) }
                result.shouldBeSuccess()
            }

            test("failure with too few entries") {
                val result = tryValidate { mapOf("a" to "1").ensureSize(2) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.map.size"
            }

            test("failure with too many entries") {
                val result = tryValidate { mapOf("a" to "1", "b" to "2", "c" to "3").ensureSize(2) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.map.size"
            }
        }

        context("ensureSizeInRange") {
            test("success with closed range") {
                val result = tryValidate { mapOf("a" to "1", "b" to "2", "c" to "3").ensureSizeInRange(2..5) }
                result.shouldBeSuccess()
            }

            test("success at lower bound of closed range") {
                val result = tryValidate { mapOf("a" to "1", "b" to "2").ensureSizeInRange(2..5) }
                result.shouldBeSuccess()
            }

            test("success at upper bound of closed range") {
                val result = tryValidate { mapOf("a" to "1", "b" to "2", "c" to "3", "d" to "4", "e" to "5").ensureSizeInRange(2..5) }
                result.shouldBeSuccess()
            }

            test("success with open-ended range") {
                val result = tryValidate { mapOf("a" to "1", "b" to "2", "c" to "3").ensureSizeInRange(2..<5) }
                result.shouldBeSuccess()
            }

            test("success at lower bound of open-ended range") {
                val result = tryValidate { mapOf("a" to "1", "b" to "2").ensureSizeInRange(2..<5) }
                result.shouldBeSuccess()
            }

            test("failure below range") {
                val result = tryValidate { mapOf("a" to "1").ensureSizeInRange(2..5) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.map.sizeInRange"
            }

            test("failure above closed range") {
                val result =
                    tryValidate {
                        mapOf("a" to "1", "b" to "2", "c" to "3", "d" to "4", "e" to "5", "f" to "6").ensureSizeInRange(
                            2..5,
                        )
                    }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.map.sizeInRange"
            }

            test("failure at upper bound of open-ended range") {
                val result = tryValidate { mapOf("a" to "1", "b" to "2", "c" to "3", "d" to "4", "e" to "5").ensureSizeInRange(2..<5) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.map.sizeInRange"
            }

            test("success with custom message") {
                val result =
                    tryValidate {
                        mapOf("a" to "1", "b" to "2", "c" to "3").ensureSizeInRange(
                            2..5,
                        ) { text("Custom message") }
                    }
                result.shouldBeSuccess()
            }

            test("failure with custom message") {
                val result =
                    tryValidate {
                        mapOf("a" to "1").ensureSizeInRange(
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
                map.ensureEach { v ->
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
                val result = tryValidate { mapOf("a" to "1", "b" to "2").ensureEachKey { it.ensureLength(1) } }
                result.shouldBeSuccess()
            }

            test("failure") {
                val input = mapOf("a" to "1", "bb" to "2", "ccc" to "3")
                val result = tryValidate { input.ensureEachKey { it.ensureLength(1) } }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.map.eachKey"
                result.messages[0].input shouldBe input
            }
        }

        context("ensureEachValue") {
            test("success") {
                val result = tryValidate { mapOf("a" to "1", "b" to "2").ensureEachValue { it.ensureLength(1) } }
                result.shouldBeSuccess()
            }

            test("failure") {
                val input = mapOf("a" to "1", "b" to "22", "c" to "333")
                val result = tryValidate { input.ensureEachValue { it.ensureLength(1) } }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.map.eachValue"
                result.messages[0].input shouldBe input
            }
        }

        context("ensureHasKey") {
            test("success") {
                val result = tryValidate { mapOf("foo" to 1, "bar" to 2).ensureHasKey("foo") }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { mapOf("bar" to 2, "baz" to 3).ensureHasKey("foo") }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.map.containsKey"
            }

            test("failure with ensureEmpty map") {
                val result = tryValidate { emptyMap<String, Nothing>().ensureHasKey("foo") }
                result.shouldBeFailure()
            }
        }

        context("ensureContainsKey") {
            test("success") {
                val result = tryValidate { mapOf("foo" to 1, "bar" to 2).ensureContainsKey("foo") }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { mapOf("bar" to 2, "baz" to 3).ensureContainsKey("foo") }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.map.containsKey"
            }

            test("failure with ensureEmpty map") {
                val result = tryValidate { emptyMap<String, Nothing>().ensureContainsKey("foo") }
                result.shouldBeFailure()
            }
        }

        context("ensureNotContainsKey") {
            test("success") {
                val result = tryValidate { mapOf("bar" to 2, "baz" to 3).ensureNotContainsKey("foo") }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { mapOf("foo" to 1, "bar" to 2).ensureNotContainsKey("foo") }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.map.notContainsKey"
            }

            test("success with ensureEmpty map") {
                val result = tryValidate { emptyMap<String, Nothing>().ensureNotContainsKey("foo") }
                result.shouldBeSuccess()
            }
        }

        context("ensureHasValue") {
            test("success") {
                val result = tryValidate { mapOf("foo" to 42, "bar" to 2).ensureHasValue(42) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { mapOf("foo" to 1, "bar" to 2).ensureHasValue(42) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.map.containsValue"
            }

            test("failure with ensureEmpty map") {
                val result = tryValidate { emptyMap<Nothing, Int>().ensureHasValue(42) }
                result.shouldBeFailure()
            }
        }

        context("ensureContainsValue") {
            test("success") {
                val result = tryValidate { mapOf("foo" to 42, "bar" to 2).ensureContainsValue(42) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { mapOf("foo" to 1, "bar" to 2).ensureContainsValue(42) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.map.containsValue"
            }

            test("failure with ensureEmpty map") {
                val result = tryValidate { emptyMap<Nothing, Int>().ensureContainsValue(42) }
                result.shouldBeFailure()
            }
        }

        context("ensureNotContainsValue") {
            test("success") {
                val result = tryValidate { mapOf("foo" to 1, "bar" to 2).ensureNotContainsValue(42) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { mapOf("foo" to 42, "bar" to 2).ensureNotContainsValue(42) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.map.notContainsValue"
            }

            test("success with ensureEmpty map") {
                val result = tryValidate { emptyMap<Nothing, Int>().ensureNotContainsValue(42) }
                result.shouldBeSuccess()
            }
        }
    })
