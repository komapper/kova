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
                val result = tryValidate { 10.ensureEquals(42) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be equal to 42"
            }

            test("ensureEquals with message") {
                val result = tryValidate { 10.ensureEquals(42) { text("must be equal to 42") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be equal to 42"
            }

            test("ensureNotEquals") {
                val result = tryValidate { 0.ensureNotEquals(0) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not be equal to 0"
            }

            test("ensureNotEquals with message") {
                val result = tryValidate { 0.ensureNotEquals(0) { text("must not be equal to 0") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not be equal to 0"
            }

            test("ensureIn") {
                val result = tryValidate { 5.ensureIn(listOf(1, 2, 3)) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be one of: [1, 2, 3]"
            }

            test("ensureIn with message") {
                val result = tryValidate { 5.ensureIn(listOf(1, 2, 3)) { text("must be one of: ${listOf(1, 2, 3)}") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be one of: [1, 2, 3]"
            }
        }

        context("kova.boolean") {
            test("ensureTrue") {
                val result = tryValidate { false.ensureTrue() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be true"
            }

            test("ensureTrue with message") {
                val result = tryValidate { false.ensureTrue { text("must be true") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be true"
            }

            test("ensureFalse") {
                val result = tryValidate { true.ensureFalse() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be false"
            }

            test("ensureFalse with message") {
                val result = tryValidate { true.ensureFalse { text("must be false") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be false"
            }
        }

        context("kova.charSequence") {
            test("ensureLengthAtLeast") {
                val result = tryValidate { "abc".ensureLengthAtLeast(5) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be at least 5 characters"
            }

            test("ensureLengthAtLeast with message") {
                val result = tryValidate { "abc".ensureLengthAtLeast(5) { text("must be at least 5 characters") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be at least 5 characters"
            }

            test("ensureLengthAtMost") {
                val result = tryValidate { "abcdef".ensureLengthAtMost(5) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be at most 5 characters"
            }

            test("ensureLengthAtMost with message") {
                val result = tryValidate { "abcdef".ensureLengthAtMost(5) { text("must be at most 5 characters") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be at most 5 characters"
            }

            test("length") {
                val result = tryValidate { "abc".ensureLength(5) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be exactly 5 characters"
            }

            test("ensureLength with message") {
                val result = tryValidate { "abc".ensureLength(5) { text("must be exactly 5 characters") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be exactly 5 characters"
            }

            test("ensureLengthInRange") {
                val result = tryValidate { "".ensureLengthInRange(1..10) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must have length within range 1..10"
            }

            test("ensureLengthInRange with message") {
                val result = tryValidate { "".ensureLengthInRange(1..10) { text("must have length within range 1..10") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must have length within range 1..10"
            }

            test("ensureNotBlank") {
                val result = tryValidate { "  ".ensureNotBlank() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not be blank"
            }

            test("ensureBlank") {
                val result = tryValidate { "abc".ensureBlank() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be blank"
            }

            test("ensureNotEmpty") {
                val result = tryValidate { "".ensureNotEmpty() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not be empty"
            }

            test("empty") {
                val result = tryValidate { "abc".ensureEmpty() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be empty"
            }

            test("ensureStartsWith") {
                val result = tryValidate { "world".ensureStartsWith("hello") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must start with \"hello\""
            }

            test("ensureStartsWith with message") {
                val result = tryValidate { "world".ensureStartsWith("hello") { text("must start with \"hello\"") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must start with \"hello\""
            }

            test("ensureNotStartsWith") {
                val result = tryValidate { "hello world".ensureNotStartsWith("hello") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not start with \"hello\""
            }

            test("ensureNotStartsWith with message") {
                val result =
                    tryValidate {
                        "hello world".ensureNotStartsWith(
                            "hello",
                        ) { text("must not start with \"hello\"") }
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not start with \"hello\""
            }

            test("ensureEndsWith") {
                val result = tryValidate { "hello".ensureEndsWith("world") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must end with \"world\""
            }

            test("ensureEndsWith with message") {
                val result = tryValidate { "hello".ensureEndsWith("world") { text("must end with \"world\"") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must end with \"world\""
            }

            test("ensureNotEndsWith") {
                val result = tryValidate { "hello world".ensureNotEndsWith("world") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not end with \"world\""
            }

            test("ensureNotEndsWith with message") {
                val result = tryValidate { "hello world".ensureNotEndsWith("world") { text("must not end with \"world\"") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not end with \"world\""
            }

            test("ensureContains") {
                val result = tryValidate { "hello".ensureContains("test") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must contain \"test\""
            }

            test("ensureContains with message") {
                val result = tryValidate { "hello".ensureContains("test") { text("must contain \"test\"") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must contain \"test\""
            }

            test("ensureNotContains") {
                val result = tryValidate { "test value".ensureNotContains("test") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not contain \"test\""
            }

            test("ensureNotContains with message") {
                val result = tryValidate { "test value".ensureNotContains("test") { text("must not contain \"test\"") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not contain \"test\""
            }

            val regex = Regex("[0-9]+")
            test("ensureMatches") {
                val result = tryValidate { "abc".ensureMatches(regex) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must match pattern: [0-9]+"
            }

            test("ensureMatches with message") {
                val result = tryValidate { "abc".ensureMatches(regex) { text("must match pattern: $regex") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must match pattern: [0-9]+"
            }

            test("ensureNotMatches") {
                val result = tryValidate { "123".ensureNotMatches(regex) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not match pattern: [0-9]+"
            }

            test("ensureNotMatches with message") {
                val result = tryValidate { "123".ensureNotMatches(regex) { text("must not match pattern: $regex") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not match pattern: [0-9]+"
            }
        }

        context("kova.collection") {
            test("ensureSizeAtLeast") {
                val result = tryValidate { listOf("a", "b").ensureSizeAtLeast(3) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Collection (size 2) must have at least 3 elements"
            }

            test("ensureSizeAtLeast with message") {
                val result =
                    tryValidate {
                        listOf("a", "b").ensureSizeAtLeast(
                            3,
                        ) { text("Collection (size $it) must have at least 3 elements") }
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Collection (size 2) must have at least 3 elements"
            }

            test("ensureSizeAtMost") {
                val result = tryValidate { listOf("a", "b", "c", "d").ensureSizeAtMost(3) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Collection (size 4) must have at most 3 elements"
            }

            test("ensureSizeAtMost with message") {
                val result =
                    tryValidate {
                        listOf("a", "b", "c", "d").ensureSizeAtMost(
                            3,
                        ) { text("Collection (size $it) must have at most 3 elements") }
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Collection (size 4) must have at most 3 elements"
            }

            test("ensureSize") {
                val result = tryValidate { listOf("a", "b").ensureSize(3) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Collection (size 2) must have exactly 3 elements"
            }

            test("ensureSize with message") {
                val result =
                    tryValidate {
                        listOf("a", "b").ensureSize(
                            3,
                        ) { text("Collection (size $it) must have exactly 3 elements") }
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Collection (size 2) must have exactly 3 elements"
            }

            test("ensureSizeInRange") {
                val result = tryValidate { listOf("a", "b").ensureSizeInRange(3..5) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Collection size must be within range 3..5"
            }

            test("ensureSizeInRange with message") {
                val result = tryValidate { listOf("a", "b").ensureSizeInRange(3..5) { text("Collection size must be within range 3..5") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Collection size must be within range 3..5"
            }
        }

        context("kova.comparable") {
            test("min") {
                val result = tryValidate { 5.ensureAtLeast(10) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be greater than or equal to 10"
            }

            test("min with message") {
                val result = tryValidate { 5.ensureAtLeast(10) { text("must be greater than or equal to 10") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be greater than or equal to 10"
            }

            test("max") {
                val result = tryValidate { 15.ensureAtMost(10) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be less than or equal to 10"
            }

            test("max with message") {
                val result = tryValidate { 15.ensureAtMost(10) { text("must be less than or equal to 10") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be less than or equal to 10"
            }

            test("ensureGreaterThan") {
                val result = tryValidate { 10.ensureGreaterThan(10) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be greater than 10"
            }

            test("ensureGreaterThan with message") {
                val result =
                    tryValidate { 10.ensureGreaterThan(10) { text("must be greater than 10") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be greater than 10"
            }

            test("ensureGreaterThanOrEqual") {
                val result = tryValidate { 9.ensureGreaterThanOrEqual(10) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be greater than or equal to 10"
            }

            test("ensureGreaterThanOrEqual with message") {
                val result = tryValidate { 9.ensureGreaterThanOrEqual(10) { text("must be greater than or equal to 10") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be greater than or equal to 10"
            }

            test("ensureLessThan") {
                val result = tryValidate { 10.ensureLessThan(10) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be less than 10"
            }

            test("ensureLessThan with message") {
                val result = tryValidate { 10.ensureLessThan(10) { text("must be less than 10") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be less than 10"
            }

            test("ensureLessThanOrEqual") {
                val result = tryValidate { 11.ensureLessThanOrEqual(10) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be less than or equal to 10"
            }

            test("ensureLessThanOrEqual with message") {
                val result = tryValidate { 11.ensureLessThanOrEqual(10) { text("must be less than or equal to 10") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be less than or equal to 10"
            }

            test("ensureInRange") {
                val result = tryValidate { 15.ensureInRange(1..10) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be within range 1..10"
            }

            test("ensureInRange with message") {
                val result = tryValidate { 15.ensureInRange(1..10) { text("must be within range 1..10") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be within range 1..10"
            }

            test("ensureInClosedRange") {
                val result = tryValidate { 15.ensureInClosedRange(1..10) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be within range 1..10"
            }

            test("ensureInClosedRange with message") {
                val result = tryValidate { 15.ensureInClosedRange(1..10) { text("must be within range 1..10") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be within range 1..10"
            }

            test("ensureInOpenEndRange") {
                val result = tryValidate { 10.ensureInOpenEndRange(1..<10) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be within range 1..9"
            }

            test("ensureInOpenEndRange with message") {
                val result = tryValidate { 10.ensureInOpenEndRange(1..<10) { text("must be within range 1..<10") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be within range 1..<10"
            }
        }

        context("kova.iterable") {
            test("ensureEach") {
                val result = tryValidate { listOf(1, -2, 3).ensureEach { it.ensurePositive() } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Some elements do not satisfy the constraint: [must be positive]"
            }

            test("ensureNotEmpty") {
                val result = tryValidate { emptyList<Nothing>().ensureNotEmpty() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not be empty"
            }

            test("ensureContains") {
                val result = tryValidate { listOf("bar", "baz").ensureHas("foo") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must contain foo"
            }

            test("ensureContains with message") {
                val result = tryValidate { listOf("bar", "baz").ensureHas("foo") { text("must contain foo") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must contain foo"
            }

            test("ensureNotContains") {
                val result = tryValidate { listOf("foo", "bar").ensureNotContains("foo") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not contain foo"
            }

            test("ensureNotContains with message") {
                val result = tryValidate { listOf("foo", "bar").ensureNotContains("foo") { text("must not contain foo") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not contain foo"
            }
        }

        context("kova.map") {
            test("ensureSizeAtLeast") {
                val result = tryValidate { mapOf("a" to 1, "b" to 2).ensureSizeAtLeast(3) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Map (size 2) must have at least 3 entries"
            }

            test("ensureSizeAtLeast with message") {
                val result =
                    tryValidate {
                        mapOf("a" to 1, "b" to 2).ensureSizeAtLeast(
                            3,
                        ) { text("Map (size $it) must have at least 3 entries") }
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Map (size 2) must have at least 3 entries"
            }

            test("ensureSizeAtMost") {
                val result = tryValidate { mapOf("a" to 1, "b" to 2, "c" to 3).ensureSizeAtMost(2) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Map (size 3) must have at most 2 entries"
            }

            test("ensureSizeAtMost with message") {
                val result =
                    tryValidate {
                        mapOf(
                            "a" to 1,
                            "b" to 2,
                            "c" to 3,
                        ).ensureSizeAtMost(
                            2,
                        ) { text("Map (size $it) must have at most 2 entries") }
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Map (size 3) must have at most 2 entries"
            }

            test("ensureSize") {
                val result = tryValidate { mapOf("a" to 1, "b" to 2).ensureSize(3) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Map (size 2) must have exactly 3 entries"
            }

            test("ensureSize with message") {
                val result =
                    tryValidate {
                        mapOf("a" to 1, "b" to 2).ensureSize(
                            3,
                        ) { text("Map (size $it) must have exactly 3 entries") }
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Map (size 2) must have exactly 3 entries"
            }

            test("ensureSizeInRange") {
                val result = tryValidate { mapOf("a" to 1, "b" to 2).ensureSizeInRange(3..5) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Map size must be within range 3..5"
            }

            test("ensureSizeInRange with message") {
                val result =
                    tryValidate { mapOf("a" to 1, "b" to 2).ensureSizeInRange(3..5) { text("Map size must be within range 3..5") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Map size must be within range 3..5"
            }

            test("ensureEachKey") {
                val result = tryValidate { mapOf("a" to 1, "bb" to 2).ensureEachKey { it.ensureLengthAtLeast(2) } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Some keys do not satisfy the constraint: [must be at least 2 characters]"
            }

            test("ensureEachValue") {
                val result = tryValidate { mapOf("a" to 1, "b" to -2).ensureEachValue { it.ensurePositive() } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Some values do not satisfy the constraint: [must be positive]"
            }

            test("ensureNotEmpty") {
                val result = tryValidate { emptyMap<Nothing, Nothing>().ensureNotEmpty() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not be empty"
            }

            test("ensureContainsKey") {
                val result = tryValidate { mapOf("bar" to 2, "baz" to 3).ensureHasKey("foo") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must contain key foo"
            }

            test("ensureContainsKey with message") {
                val result =
                    tryValidate {
                        mapOf("bar" to 2, "baz" to 3).ensureHasKey(
                            "foo",
                        ) { text("must contain key foo") }
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must contain key foo"
            }

            test("ensureNotContainsKey") {
                val result = tryValidate { mapOf("foo" to 1, "bar" to 2).ensureNotContainsKey("foo") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not contain key foo"
            }

            test("ensureNotContainsKey with message") {
                val result =
                    tryValidate {
                        mapOf("foo" to 1, "bar" to 2).ensureNotContainsKey(
                            "foo",
                        ) { text("must not contain key foo") }
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not contain key foo"
            }

            test("ensureContainsValue") {
                val result = tryValidate { mapOf("foo" to 1, "bar" to 2).ensureHasValue(42) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must contain value 42"
            }

            test("ensureContainsValue with message") {
                val result = tryValidate { mapOf("foo" to 1, "bar" to 2).ensureHasValue(42) { text("must contain value 42") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must contain value 42"
            }

            test("ensureNotContainsValue") {
                val result =
                    tryValidate { mapOf("foo" to 42, "bar" to 2).ensureNotContainsValue(42) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not contain value 42"
            }

            test("ensureNotContainsValue with message") {
                val result =
                    tryValidate {
                        mapOf("foo" to 42, "bar" to 2).ensureNotContainsValue(42) { text("must not contain value 42") }
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not contain value 42"
            }
        }

        context("kova.nullable") {
            test("ensureNull") {
                val result = tryValidate { "value".ensureNull() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be null"
            }

            test("ensureNotNull") {
                val result = tryValidate { (null as String?).ensureNotNull() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not be null"
            }
        }

        context("kova.number") {
            test("ensurePositive") {
                val result = tryValidate { (-5).ensurePositive() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be positive"
            }

            test("ensureNegative") {
                val result = tryValidate { 5.ensureNegative() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be negative"
            }

            test("ensureNotPositive") {
                val result = tryValidate { 5.ensureNotPositive() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not be positive"
            }

            test("ensureNotNegative") {
                val result = tryValidate { (-5).ensureNotNegative() }
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
                        or { value.ensurePositive() } orElse { value.ensureLessThan(0) }
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "at least one constraint must be satisfied: [[must be positive], [must be less than 0]]"
            }
        }

        context("kova.string") {
            test("ensureInt") {
                val result = tryValidate { "abc".ensureInt() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be a valid integer"
            }

            test("ensureLong") {
                val result = tryValidate { "abc".ensureLong() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be a valid long"
            }

            test("ensureShort") {
                val result = tryValidate { "abc".ensureShort() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be a valid short"
            }

            test("ensureByte") {
                val result = tryValidate { "abc".ensureByte() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be a valid byte"
            }

            test("ensureDouble") {
                val result = tryValidate { "abc".ensureDouble() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be a valid double"
            }

            test("ensureFloat") {
                val result = tryValidate { "abc".ensureFloat() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be a valid float"
            }

            test("ensureBigDecimal") {
                val result = tryValidate { "abc".ensureBigDecimal() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be a valid decimal number"
            }

            test("ensureBigInteger") {
                val result = tryValidate { "abc".ensureBigInteger() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be a valid integer number"
            }

            test("ensureBoolean") {
                val result = tryValidate { "abc".ensureBoolean() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be \"true\" or \"false\""
            }

            test("ensureEnum") {
                val result = tryValidate { "INVALID".ensureEnum<TestEnum>() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be one of: [A, B, C]"
            }

            test("ensureUppercase") {
                val result = tryValidate { "abc".ensureUppercase() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be uppercase"
            }

            test("ensureLowercase") {
                val result = tryValidate { "ABC".ensureLowercase() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be lowercase"
            }
        }

        context("kova.temporal") {
            test("ensureFuture") {
                val result =
                    tryValidate {
                        LocalDate
                            .now()
                            .minusDays(1)
                            .ensureFuture()
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be in the future"
            }

            test("ensureFutureOrPresent") {
                val result =
                    tryValidate {
                        LocalDate
                            .now()
                            .minusDays(1)
                            .ensureFutureOrPresent()
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be in the future or present"
            }

            test("ensurePast") {
                val result =
                    tryValidate {
                        LocalDate
                            .now()
                            .plusDays(1)
                            .ensurePast()
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be in the past"
            }

            test("ensurePastOrPresent") {
                val result =
                    tryValidate {
                        LocalDate
                            .now()
                            .plusDays(1)
                            .ensurePastOrPresent()
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be in the past or present"
            }
        }
    }) {
    enum class TestEnum { A, B, C }
}
