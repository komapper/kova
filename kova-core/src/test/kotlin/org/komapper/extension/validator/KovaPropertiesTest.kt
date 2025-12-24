package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import java.time.LocalDate

class KovaPropertiesTest :
    FunSpec({

        context("kova.charSequence") {
            test("min") {
                val result = tryValidate { "abc".min(5) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be at least 5 characters"
            }

            test("min with message") {
                val result = tryValidate { "abc".min(5) { text("must be at least 5 characters") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be at least 5 characters"
            }

            test("max") {
                val result = tryValidate { "abcdef".max(5) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be at most 5 characters"
            }

            test("max with message") {
                val result = tryValidate { "abcdef".max(5) { text("must be at most 5 characters") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be at most 5 characters"
            }

            test("length") {
                val result = tryValidate { "abc".length(5) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be exactly 5 characters"
            }

            test("length with message") {
                val result = tryValidate { "abc".length(5) { text("must be exactly 5 characters") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be exactly 5 characters"
            }

            test("notBlank") {
                val result = tryValidate { "  ".notBlank() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not be blank"
            }

            test("blank") {
                val result = tryValidate { "abc".blank() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be blank"
            }

            test("notEmpty") {
                val result = tryValidate { "".notEmpty() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not be empty"
            }

            test("empty") {
                val result = tryValidate { "abc".empty() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be empty"
            }

            test("startsWith") {
                val result = tryValidate { "world".startsWith("hello") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must start with \"hello\""
            }

            test("startsWith with message") {
                val result = tryValidate { "world".startsWith("hello") { text("must start with \"hello\"") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must start with \"hello\""
            }

            test("notStartsWith") {
                val result = tryValidate { "hello world".notStartsWith("hello") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not start with \"hello\""
            }

            test("notStartsWith with message") {
                val result = tryValidate { "hello world".notStartsWith("hello") { text("must not start with \"hello\"") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not start with \"hello\""
            }

            test("endsWith") {
                val result = tryValidate { "hello".endsWith("world") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must end with \"world\""
            }

            test("endsWith with message") {
                val result = tryValidate { "hello".endsWith("world") { text("must end with \"world\"") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must end with \"world\""
            }

            test("notEndsWith") {
                val result = tryValidate { "hello world".notEndsWith("world") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not end with \"world\""
            }

            test("notEndsWith with message") {
                val result = tryValidate { "hello world".notEndsWith("world") { text("must not end with \"world\"") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not end with \"world\""
            }

            test("contains") {
                val result = tryValidate { "hello".contains("test") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must contain \"test\""
            }

            test("contains with message") {
                val result = tryValidate { "hello".contains("test") { text("must contain \"test\"") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must contain \"test\""
            }

            test("notContains") {
                val result = tryValidate { "test value".notContains("test") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not contain \"test\""
            }

            test("notContains with message") {
                val result = tryValidate { "test value".notContains("test") { text("must not contain \"test\"") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not contain \"test\""
            }

            val regex = Regex("[0-9]+")
            test("matches") {
                val result = tryValidate { "abc".matches(regex) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must match pattern: [0-9]+"
            }

            test("matches with message") {
                val result = tryValidate { "abc".matches(regex) { text("must match pattern: $regex") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must match pattern: [0-9]+"
            }

            test("notMatches") {
                val result = tryValidate { "123".notMatches(regex) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not match pattern: [0-9]+"
            }

            test("notMatches with message") {
                val result = tryValidate { "123".notMatches(regex) { text("must not match pattern: $regex") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not match pattern: [0-9]+"
            }
        }

        context("kova.collection") {
            test("min") {
                val result = tryValidate { listOf("a", "b").min(3) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Collection (size 2) must have at least 3 elements"
            }

            test("min with message") {
                val result = tryValidate { listOf("a", "b").min(3) { text("Collection (size $it) must have at least 3 elements") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Collection (size 2) must have at least 3 elements"
            }

            test("max") {
                val result = tryValidate { listOf("a", "b", "c", "d").max(3) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Collection (size 4) must have at most 3 elements"
            }

            test("max with message") {
                val result =
                    tryValidate { listOf("a", "b", "c", "d").max(3) { text("Collection (size $it) must have at most 3 elements") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Collection (size 4) must have at most 3 elements"
            }

            test("length") {
                val result = tryValidate { listOf("a", "b").length(3) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Collection (size 2) must have exactly 3 elements"
            }

            test("length with message") {
                val result = tryValidate { listOf("a", "b").length(3) { text("Collection (size $it) must have exactly 3 elements") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Collection (size 2) must have exactly 3 elements"
            }

            test("onEach") {
                val result = tryValidate { listOf(1, -2, 3).onEach { it.positive() } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Some elements do not satisfy the constraint: [must be positive]"
            }

            test("notEmpty") {
                val result = tryValidate { emptyList<Nothing>().notEmpty() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not be empty"
            }

            test("contains") {
                val result = tryValidate { listOf("bar", "baz").has("foo") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must contain foo"
            }

            test("contains with message") {
                val result = tryValidate { listOf("bar", "baz").has("foo") { text("must contain foo") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must contain foo"
            }

            test("notContains") {
                val result = tryValidate { listOf("foo", "bar").notContains("foo") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not contain foo"
            }

            test("notContains with message") {
                val result = tryValidate { listOf("foo", "bar").notContains("foo") { text("must not contain foo") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not contain foo"
            }
        }

        context("kova.comparable") {
            test("min") {
                val result = tryValidate { 5.min(10) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be greater than or equal to 10"
            }

            test("min with message") {
                val result = tryValidate { 5.min(10) { text("must be greater than or equal to 10") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be greater than or equal to 10"
            }

            test("max") {
                val result = tryValidate { 15.max(10) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be less than or equal to 10"
            }

            test("max with message") {
                val result = tryValidate { 15.max(10) { text("must be less than or equal to 10") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be less than or equal to 10"
            }

            test("gt") {
                val result = tryValidate { 10.gt(10) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be greater than 10"
            }

            test("gt with message") {
                val result =
                    tryValidate { 10.gt(10) { text("must be greater than 10") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be greater than 10"
            }

            test("gte") {
                val result = tryValidate { 9.gte(10) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be greater than or equal to 10"
            }

            test("gte with message") {
                val result = tryValidate { 9.gte(10) { text("must be greater than or equal to 10") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be greater than or equal to 10"
            }

            test("lt") {
                val result = tryValidate { 10.lt(10) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be less than 10"
            }

            test("lt with message") {
                val result = tryValidate { 10.lt(10) { text("must be less than 10") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be less than 10"
            }

            test("lte") {
                val result = tryValidate { 11.lte(10) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be less than or equal to 10"
            }

            test("lte with message") {
                val result = tryValidate { 11.lte(10) { text("must be less than or equal to 10") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be less than or equal to 10"
            }

            test("eq") {
                val result = tryValidate { 10.eq(42) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be equal to 42"
            }

            test("eq with message") {
                val result = tryValidate { 10.eq(42) { text("must be equal to 42") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be equal to 42"
            }

            test("notEq") {
                val result = tryValidate { 0.notEq(0) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not be equal to 0"
            }

            test("notEq with message") {
                val result = tryValidate { 0.notEq(0) { text("must not be equal to 0") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not be equal to 0"
            }
        }

        context("kova.literal") {
            test("single") {
                val result = tryValidate { 10.literal(42) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be 42"
            }

            test("single with message") {
                val result =
                    tryValidate { 10.literal(42) { text("must be 42") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be 42"
            }

            test("list") {
                val result = tryValidate { 5.literal(listOf(1, 2, 3)) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be one of: [1, 2, 3]"
            }

            test("list with message") {
                val result = tryValidate { 5.literal(listOf(1, 2, 3)) { text("must be one of: ${listOf(1, 2, 3)}") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be one of: [1, 2, 3]"
            }
        }

        context("kova.map") {
            test("min") {
                val result = tryValidate { mapOf("a" to 1, "b" to 2).min(3) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Map (size 2) must have at least 3 entries"
            }

            test("min with message") {
                val result = tryValidate { mapOf("a" to 1, "b" to 2).min(3) { text("Map (size $it) must have at least 3 entries") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Map (size 2) must have at least 3 entries"
            }

            test("max") {
                val result = tryValidate { mapOf("a" to 1, "b" to 2, "c" to 3).max(2) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Map (size 3) must have at most 2 entries"
            }

            test("max with message") {
                val result =
                    tryValidate {
                        mapOf(
                            "a" to 1,
                            "b" to 2,
                            "c" to 3,
                        ).max(2) { text("Map (size $it) must have at most 2 entries") }
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Map (size 3) must have at most 2 entries"
            }

            test("length") {
                val result = tryValidate { mapOf("a" to 1, "b" to 2).length(3) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Map (size 2) must have exactly 3 entries"
            }

            test("length with message") {
                val result = tryValidate { mapOf("a" to 1, "b" to 2).length(3) { text("Map (size $it) must have exactly 3 entries") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Map (size 2) must have exactly 3 entries"
            }

            test("onEachKey") {
                val result = tryValidate { mapOf("a" to 1, "bb" to 2).onEachKey { it.min(2) } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Some keys do not satisfy the constraint: [must be at least 2 characters]"
            }

            test("onEachValue") {
                val result = tryValidate { mapOf("a" to 1, "b" to -2).onEachValue { it.positive() } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Some values do not satisfy the constraint: [must be positive]"
            }

            test("notEmpty") {
                val result = tryValidate { emptyMap<Nothing, Nothing>().notEmpty() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not be empty"
            }

            test("containsKey") {
                val result = tryValidate { mapOf("bar" to 2, "baz" to 3).hasKey("foo") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must contain key foo"
            }

            test("containsKey with message") {
                val result = tryValidate { mapOf("bar" to 2, "baz" to 3).hasValue("foo") { text("must contain key foo") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must contain key foo"
            }

            test("notContainsKey") {
                val result = tryValidate { mapOf("foo" to 1, "bar" to 2).notContainsKey("foo") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not contain key foo"
            }

            test("notContainsKey with message") {
                val result = tryValidate { mapOf("foo" to 1, "bar" to 2).notContainsKey("foo") { text("must not contain key foo") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not contain key foo"
            }

            test("containsValue") {
                val result = tryValidate { mapOf("foo" to 1, "bar" to 2).hasValue(42) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must contain value 42"
            }

            test("containsValue with message") {
                val result = tryValidate { mapOf("foo" to 1, "bar" to 2).hasValue(42) { text("must contain value 42") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must contain value 42"
            }

            test("notContainsValue") {
                val result =
                    tryValidate { mapOf("foo" to 42, "bar" to 2).notContainsValue(42) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not contain value 42"
            }

            test("notContainsValue with message") {
                val result =
                    tryValidate {
                        mapOf("foo" to 42, "bar" to 2).notContainsValue(42) { text("must not contain value 42") }
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not contain value 42"
            }
        }

        context("kova.nullable") {
            test("isNull") {
                val result = tryValidate { "value".isNull() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be null"
            }

            test("notNull") {
                val result = tryValidate { null.notNull() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not be null"
            }
        }

        context("kova.number") {
            test("positive") {
                val result = tryValidate { (-5).positive() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be positive"
            }

            test("negative") {
                val result = tryValidate { 5.negative() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be negative"
            }

            test("notPositive") {
                val result = tryValidate { 5.notPositive() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not be positive"
            }

            test("notNegative") {
                val result = tryValidate { (-5).notNegative() }
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
                        or { value.positive() } orElse { value.lt(0) }
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "at least one constraint must be satisfied: [[must be positive], [must be less than 0]]"
            }
        }

        context("kova.string") {
            test("isInt") {
                val result = tryValidate { "abc".isInt() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be a valid integer"
            }

            test("isLong") {
                val result = tryValidate { "abc".isLong() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be a valid long"
            }

            test("isShort") {
                val result = tryValidate { "abc".isShort() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be a valid short"
            }

            test("isByte") {
                val result = tryValidate { "abc".isByte() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be a valid byte"
            }

            test("isDouble") {
                val result = tryValidate { "abc".isDouble() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be a valid double"
            }

            test("isFloat") {
                val result = tryValidate { "abc".isFloat() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be a valid float"
            }

            test("isBigDecimal") {
                val result = tryValidate { "abc".isBigDecimal() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be a valid decimal number"
            }

            test("isBigInteger") {
                val result = tryValidate { "abc".isBigInteger() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be a valid integer number"
            }

            test("isBoolean") {
                val result = tryValidate { "abc".isBoolean() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be \"true\" or \"false\""
            }

            test("isEnum") {
                val result = tryValidate { "INVALID".isEnum<TestEnum>() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be one of: [A, B, C]"
            }

            test("uppercase") {
                val result = tryValidate { "abc".uppercase() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be uppercase"
            }

            test("lowercase") {
                val result = tryValidate { "ABC".lowercase() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be lowercase"
            }
        }

        context("kova.temporal") {
            test("future") {
                val result =
                    tryValidate {
                        LocalDate
                            .now()
                            .minusDays(1)
                            .future()
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be in the future"
            }

            test("futureOrPresent") {
                val result =
                    tryValidate {
                        LocalDate
                            .now()
                            .minusDays(1)
                            .futureOrPresent()
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be in the future or present"
            }

            test("past") {
                val result =
                    tryValidate {
                        LocalDate
                            .now()
                            .plusDays(1)
                            .past()
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be in the past"
            }

            test("pastOrPresent") {
                val result =
                    tryValidate {
                        LocalDate
                            .now()
                            .plusDays(1)
                            .pastOrPresent()
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be in the past or present"
            }
        }
    }) {
    enum class TestEnum { A, B, C }
}
