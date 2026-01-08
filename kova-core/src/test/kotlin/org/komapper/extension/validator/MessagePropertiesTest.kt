package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import java.time.LocalDate
import java.util.Locale

class MessagePropertiesTest :
    FunSpec({

        beforeSpec {
            Locale.setDefault(Locale.US)
        }

        context("kova.any") {

            test("ensureEquals") {
                val result = tryValidate { ensureEquals(10, 42) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be equal to 42"
            }

            test("ensureEquals with message") {
                val result = tryValidate { ensureEquals(10, 42) { text("must be equal to 42") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be equal to 42"
            }

            test("ensureNotEquals") {
                val result = tryValidate { ensureNotEquals(0, 0) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not be equal to 0"
            }

            test("ensureNotEquals with message") {
                val result = tryValidate { ensureNotEquals(0, 0) { text("must not be equal to 0") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not be equal to 0"
            }

            test("ensureIn") {
                val result = tryValidate { ensureIn(5, listOf(1, 2, 3)) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be one of: [1, 2, 3]"
            }

            test("ensureIn with message") {
                val result = tryValidate { ensureIn(5, listOf(1, 2, 3)) { text("must be one of: ${listOf(1, 2, 3)}") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be one of: [1, 2, 3]"
            }
        }

        context("kova.boolean") {
            test("ensureTrue") {
                val result = tryValidate { ensureTrue(false) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be true"
            }

            test("ensureTrue with message") {
                val result = tryValidate { ensureTrue(false) { text("must be true") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be true"
            }

            test("ensureFalse") {
                val result = tryValidate { ensureFalse(true) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be false"
            }

            test("ensureFalse with message") {
                val result = tryValidate { ensureFalse(true) { text("must be false") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be false"
            }
        }

        context("kova.charSequence") {
            test("ensureMinLength") {
                val result = tryValidate { ensureMinLength("abc", 5) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be at least 5 characters"
            }

            test("ensureMinLength with message") {
                val result = tryValidate { ensureMinLength("abc", 5) { text("must be at least 5 characters") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be at least 5 characters"
            }

            test("ensureMaxLength") {
                val result = tryValidate { ensureMaxLength("abcdef", 5) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be at most 5 characters"
            }

            test("ensureMaxLength with message") {
                val result = tryValidate { ensureMaxLength("abcdef", 5) { text("must be at most 5 characters") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be at most 5 characters"
            }

            test("length") {
                val result = tryValidate { ensureLength("abc", 5) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be exactly 5 characters"
            }

            test("ensureLength with message") {
                val result = tryValidate { ensureLength("abc", 5) { text("must be exactly 5 characters") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be exactly 5 characters"
            }

            test("ensureLengthInRange") {
                val result = tryValidate { ensureLengthInRange("", 1..10) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must have length within range 1..10"
            }

            test("ensureLengthInRange with message") {
                val result = tryValidate { ensureLengthInRange("", 1..10) { text("must have length within range 1..10") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must have length within range 1..10"
            }

            test("ensureNotBlank") {
                val result = tryValidate { ensureNotBlank("  ") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not be blank"
            }

            test("ensureBlank") {
                val result = tryValidate { ensureBlank("abc") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be blank"
            }

            test("ensureNotEmpty") {
                val result = tryValidate { ensureNotEmpty("") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not be empty"
            }

            test("empty") {
                val result = tryValidate { ensureEmpty("abc") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be empty"
            }

            test("ensureStartsWith") {
                val result = tryValidate { ensureStartsWith("world", "hello") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must start with \"hello\""
            }

            test("ensureStartsWith with message") {
                val result = tryValidate { ensureStartsWith("world", "hello") { text("must start with \"hello\"") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must start with \"hello\""
            }

            test("ensureNotStartsWith") {
                val result = tryValidate { ensureNotStartsWith("hello world", "hello") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not start with \"hello\""
            }

            test("ensureNotStartsWith with message") {
                val result =
                    tryValidate {
                        ensureNotStartsWith(
                            "hello world",
                            "hello",
                        ) { text("must not start with \"hello\"") }
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not start with \"hello\""
            }

            test("ensureEndsWith") {
                val result = tryValidate { ensureEndsWith("hello", "world") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must end with \"world\""
            }

            test("ensureEndsWith with message") {
                val result = tryValidate { ensureEndsWith("hello", "world") { text("must end with \"world\"") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must end with \"world\""
            }

            test("ensureNotEndsWith") {
                val result = tryValidate { ensureNotEndsWith("hello world", "world") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not end with \"world\""
            }

            test("ensureNotEndsWith with message") {
                val result = tryValidate { ensureNotEndsWith("hello world", "world") { text("must not end with \"world\"") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not end with \"world\""
            }

            test("ensureContains") {
                val result = tryValidate { ensureContains("hello", "test") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must contain \"test\""
            }

            test("ensureContains with message") {
                val result = tryValidate { ensureContains("hello", "test") { text("must contain \"test\"") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must contain \"test\""
            }

            test("ensureNotContains") {
                val result = tryValidate { ensureNotContains("test value", "test") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not contain \"test\""
            }

            test("ensureNotContains with message") {
                val result = tryValidate { ensureNotContains("test value", "test") { text("must not contain \"test\"") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not contain \"test\""
            }

            val regex = Regex("[0-9]+")
            test("ensureMatches") {
                val result = tryValidate { ensureMatches("abc", regex) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must match pattern: [0-9]+"
            }

            test("ensureMatches with message") {
                val result = tryValidate { ensureMatches("abc", regex) { text("must match pattern: $regex") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must match pattern: [0-9]+"
            }

            test("ensureNotMatches") {
                val result = tryValidate { ensureNotMatches("123", regex) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not match pattern: [0-9]+"
            }

            test("ensureNotMatches with message") {
                val result = tryValidate { ensureNotMatches("123", regex) { text("must not match pattern: $regex") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not match pattern: [0-9]+"
            }
        }

        context("kova.collection") {
            test("ensureMinSize") {
                val result = tryValidate { ensureMinSize(listOf("a", "b"), 3) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Collection (size 2) must have at least 3 elements"
            }

            test("ensureMinSize with message") {
                val result =
                    tryValidate {
                        ensureMinSize(
                            listOf("a", "b"),
                            3,
                        ) { text("Collection (size $it) must have at least 3 elements") }
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Collection (size 2) must have at least 3 elements"
            }

            test("ensureMaxSize") {
                val result = tryValidate { ensureMaxSize(listOf("a", "b", "c", "d"), 3) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Collection (size 4) must have at most 3 elements"
            }

            test("ensureMaxSize with message") {
                val result =
                    tryValidate {
                        ensureMaxSize(
                            listOf("a", "b", "c", "d"),
                            3,
                        ) { text("Collection (size $it) must have at most 3 elements") }
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Collection (size 4) must have at most 3 elements"
            }

            test("ensureSize") {
                val result = tryValidate { ensureSize(listOf("a", "b"), 3) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Collection (size 2) must have exactly 3 elements"
            }

            test("ensureSize with message") {
                val result =
                    tryValidate {
                        ensureSize(
                            listOf("a", "b"),
                            3,
                        ) { text("Collection (size $it) must have exactly 3 elements") }
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Collection (size 2) must have exactly 3 elements"
            }
        }

        context("kova.comparable") {
            test("min") {
                val result = tryValidate { ensureMin(5, 10) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be greater than or equal to 10"
            }

            test("min with message") {
                val result = tryValidate { ensureMin(5, 10) { text("must be greater than or equal to 10") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be greater than or equal to 10"
            }

            test("max") {
                val result = tryValidate { ensureMax(15, 10) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be less than or equal to 10"
            }

            test("max with message") {
                val result = tryValidate { ensureMax(15, 10) { text("must be less than or equal to 10") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be less than or equal to 10"
            }

            test("ensureGreaterThan") {
                val result = tryValidate { ensureGreaterThan(10, 10) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be greater than 10"
            }

            test("ensureGreaterThan with message") {
                val result =
                    tryValidate { ensureGreaterThan(10, 10) { text("must be greater than 10") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be greater than 10"
            }

            test("ensureGreaterThanOrEqual") {
                val result = tryValidate { ensureGreaterThanOrEqual(9, 10) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be greater than or equal to 10"
            }

            test("ensureGreaterThanOrEqual with message") {
                val result = tryValidate { ensureGreaterThanOrEqual(9, 10) { text("must be greater than or equal to 10") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be greater than or equal to 10"
            }

            test("ensureLessThan") {
                val result = tryValidate { ensureLessThan(10, 10) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be less than 10"
            }

            test("ensureLessThan with message") {
                val result = tryValidate { ensureLessThan(10, 10) { text("must be less than 10") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be less than 10"
            }

            test("ensureLessThanOrEqual") {
                val result = tryValidate { ensureLessThanOrEqual(11, 10) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be less than or equal to 10"
            }

            test("ensureLessThanOrEqual with message") {
                val result = tryValidate { ensureLessThanOrEqual(11, 10) { text("must be less than or equal to 10") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be less than or equal to 10"
            }

            test("ensureInRange") {
                val result = tryValidate { ensureInRange(15, 1..10) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be within range 1..10"
            }

            test("ensureInRange with message") {
                val result = tryValidate { ensureInRange(15, 1..10) { text("must be within range 1..10") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be within range 1..10"
            }

            test("ensureInClosedRange") {
                val result = tryValidate { ensureInClosedRange(15, 1..10) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be within range 1..10"
            }

            test("ensureInClosedRange with message") {
                val result = tryValidate { ensureInClosedRange(15, 1..10) { text("must be within range 1..10") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be within range 1..10"
            }

            test("ensureInOpenEndRange") {
                val result = tryValidate { ensureInOpenEndRange(10, 1..<10) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be within range 1..9"
            }

            test("ensureInOpenEndRange with message") {
                val result = tryValidate { ensureInOpenEndRange(10, 1..<10) { text("must be within range 1..<10") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be within range 1..<10"
            }
        }

        context("kova.iterable") {
            test("ensureEach") {
                val result = tryValidate { ensureEach(listOf(1, -2, 3)) { ensurePositive(it) } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Some elements do not satisfy the constraint: [must be positive]"
            }

            test("ensureNotEmpty") {
                val result = tryValidate { ensureNotEmpty(emptyList<Nothing>()) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not be empty"
            }

            test("ensureContains") {
                val result = tryValidate { ensureHas(listOf("bar", "baz"), "foo") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must contain foo"
            }

            test("ensureContains with message") {
                val result = tryValidate { ensureHas(listOf("bar", "baz"), "foo") { text("must contain foo") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must contain foo"
            }

            test("ensureNotContains") {
                val result = tryValidate { ensureNotContains(listOf("foo", "bar"), "foo") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not contain foo"
            }

            test("ensureNotContains with message") {
                val result = tryValidate { ensureNotContains(listOf("foo", "bar"), "foo") { text("must not contain foo") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not contain foo"
            }
        }

        context("kova.map") {
            test("ensureMinSize") {
                val result = tryValidate { ensureMinSize(mapOf("a" to 1, "b" to 2), 3) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Map (size 2) must have at least 3 entries"
            }

            test("ensureMinSize with message") {
                val result =
                    tryValidate {
                        ensureMinSize(
                            mapOf("a" to 1, "b" to 2),
                            3,
                        ) { text("Map (size $it) must have at least 3 entries") }
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Map (size 2) must have at least 3 entries"
            }

            test("ensureMaxSize") {
                val result = tryValidate { ensureMaxSize(mapOf("a" to 1, "b" to 2, "c" to 3), 2) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Map (size 3) must have at most 2 entries"
            }

            test("ensureMaxSize with message") {
                val result =
                    tryValidate {
                        ensureMaxSize(
                            mapOf(
                                "a" to 1,
                                "b" to 2,
                                "c" to 3,
                            ),
                            2,
                        ) { text("Map (size $it) must have at most 2 entries") }
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Map (size 3) must have at most 2 entries"
            }

            test("ensureSize") {
                val result = tryValidate { ensureSize(mapOf("a" to 1, "b" to 2), 3) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Map (size 2) must have exactly 3 entries"
            }

            test("ensureSize with message") {
                val result =
                    tryValidate {
                        ensureSize(
                            mapOf("a" to 1, "b" to 2),
                            3,
                        ) { text("Map (size $it) must have exactly 3 entries") }
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Map (size 2) must have exactly 3 entries"
            }

            test("ensureEachKey") {
                val result = tryValidate { ensureEachKey(mapOf("a" to 1, "bb" to 2)) { ensureMinLength(it, 2) } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Some keys do not satisfy the constraint: [must be at least 2 characters]"
            }

            test("ensureEachValue") {
                val result = tryValidate { ensureEachValue(mapOf("a" to 1, "b" to -2)) { ensurePositive(it) } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Some values do not satisfy the constraint: [must be positive]"
            }

            test("ensureNotEmpty") {
                val result = tryValidate { ensureNotEmpty(emptyMap<Nothing, Nothing>()) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not be empty"
            }

            test("ensureContainsKey") {
                val result = tryValidate { ensureHasKey(mapOf("bar" to 2, "baz" to 3), "foo") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must contain key foo"
            }

            test("ensureContainsKey with message") {
                val result =
                    tryValidate {
                        ensureHasValue(
                            mapOf("bar" to 2, "baz" to 3),
                            "foo",
                        ) { text("must contain key foo") }
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must contain key foo"
            }

            test("notContainsKey") {
                val result = tryValidate { notContainsKey(mapOf("foo" to 1, "bar" to 2), "foo") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not contain key foo"
            }

            test("notContainsKey with message") {
                val result =
                    tryValidate {
                        notContainsKey(
                            mapOf("foo" to 1, "bar" to 2),
                            "foo",
                        ) { text("must not contain key foo") }
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not contain key foo"
            }

            test("ensureCcontainsValue") {
                val result = tryValidate { ensureHasValue(mapOf("foo" to 1, "bar" to 2), 42) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must contain value 42"
            }

            test("ensureCcontainsValue with message") {
                val result = tryValidate { ensureHasValue(mapOf("foo" to 1, "bar" to 2), 42) { text("must contain value 42") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must contain value 42"
            }

            test("ensureNotContainsValue") {
                val result =
                    tryValidate { ensureNotContainsValue(mapOf("foo" to 42, "bar" to 2), 42) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not contain value 42"
            }

            test("ensureNotContainsValue with message") {
                val result =
                    tryValidate {
                        ensureNotContainsValue(mapOf("foo" to 42, "bar" to 2), 42) { text("must not contain value 42") }
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not contain value 42"
            }
        }

        context("kova.nullable") {
            test("ensureNull") {
                val result = tryValidate { ensureNull("value") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be null"
            }

            test("ensureNotNull") {
                val result = tryValidate { ensureNotNull(null) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not be null"
            }
        }

        context("kova.number") {
            test("ensurePositive") {
                val result = tryValidate { ensurePositive(-5) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be positive"
            }

            test("ensureNegative") {
                val result = tryValidate { ensureNegative(5) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be negative"
            }

            test("ensureNotPositive") {
                val result = tryValidate { ensureNotPositive(5) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not be positive"
            }

            test("ensureNotNegative") {
                val result = tryValidate { ensureNotNegative(-5) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not be negative"
            }
        }

        context("kova.or") {
            test("or") {
                val result =
                    tryValidate {
                        val value = 0
                        or { ensurePositive(value) } orElse { ensureLessThan(value, 0) }
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "at least one constraint must be satisfied: [[must be positive], [must be less than 0]]"
            }
        }

        context("kova.string") {
            test("ensureInt") {
                val result = tryValidate { ensureInt("abc") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be a valid integer"
            }

            test("ensureLong") {
                val result = tryValidate { ensureLong("abc") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be a valid long"
            }

            test("ensureShort") {
                val result = tryValidate { ensureShort("abc") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be a valid short"
            }

            test("ensureByte") {
                val result = tryValidate { ensureByte("abc") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be a valid byte"
            }

            test("ensureDouble") {
                val result = tryValidate { ensureDouble("abc") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be a valid double"
            }

            test("ensureFloat") {
                val result = tryValidate { ensureFloat("abc") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be a valid float"
            }

            test("ensureBigDecimal") {
                val result = tryValidate { ensureBigDecimal("abc") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be a valid decimal number"
            }

            test("ensureBigInteger") {
                val result = tryValidate { ensureBigInteger("abc") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be a valid integer number"
            }

            test("ensureBoolean") {
                val result = tryValidate { ensureBoolean("abc") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be \"true\" or \"false\""
            }

            test("ensureEnum") {
                val result = tryValidate { ensureEnum<TestEnum>("INVALID") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be one of: [A, B, C]"
            }

            test("ensureUppercase") {
                val result = tryValidate { ensureUppercase("abc") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be uppercase"
            }

            test("ensureLowercase") {
                val result = tryValidate { ensureLowercase("ABC") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be lowercase"
            }
        }

        context("kova.temporal") {
            test("ensureFuture") {
                val result =
                    tryValidate {
                        ensureFuture(
                            LocalDate
                                .now()
                                .minusDays(1),
                        )
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be in the future"
            }

            test("ensureFutureOrPresent") {
                val result =
                    tryValidate {
                        ensureFutureOrPresent(
                            LocalDate
                                .now()
                                .minusDays(1),
                        )
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be in the future or present"
            }

            test("ensurePast") {
                val result =
                    tryValidate {
                        ensurePast(
                            LocalDate
                                .now()
                                .plusDays(1),
                        )
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be in the past"
            }

            test("ensurePastOrPresent") {
                val result =
                    tryValidate {
                        ensurePastOrPresent(
                            LocalDate
                                .now()
                                .plusDays(1),
                        )
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be in the past or present"
            }
        }
    }) {
    enum class TestEnum { A, B, C }
}
