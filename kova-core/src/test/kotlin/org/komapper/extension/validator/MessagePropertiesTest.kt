package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import java.time.LocalDate
import java.util.Locale

class MessagePropertiesTest :
    FunSpec({

        beforeSpec {
            Locale.setDefault(Locale.US)
        }

        context("kova.charSequence") {
            test("minLength") {
                val result = tryValidate { minLength("abc", 5) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be at least 5 characters"
            }

            test("minLength with message") {
                val result = tryValidate { minLength("abc", 5) { text("must be at least 5 characters") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be at least 5 characters"
            }

            test("maxLength") {
                val result = tryValidate { maxLength("abcdef", 5) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be at most 5 characters"
            }

            test("maxLength with message") {
                val result = tryValidate { maxLength("abcdef", 5) { text("must be at most 5 characters") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be at most 5 characters"
            }

            test("length") {
                val result = tryValidate { length("abc", 5) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be exactly 5 characters"
            }

            test("length with message") {
                val result = tryValidate { length("abc", 5) { text("must be exactly 5 characters") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be exactly 5 characters"
            }

            test("notBlank") {
                val result = tryValidate { notBlank("  ") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not be blank"
            }

            test("blank") {
                val result = tryValidate { blank("abc") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be blank"
            }

            test("notEmpty") {
                val result = tryValidate { notEmpty("") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not be empty"
            }

            test("empty") {
                val result = tryValidate { empty("abc") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be empty"
            }

            test("startsWith") {
                val result = tryValidate { startsWith("world", "hello") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must start with \"hello\""
            }

            test("startsWith with message") {
                val result = tryValidate { startsWith("world", "hello") { text("must start with \"hello\"") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must start with \"hello\""
            }

            test("notStartsWith") {
                val result = tryValidate { notStartsWith("hello world", "hello") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not start with \"hello\""
            }

            test("notStartsWith with message") {
                val result =
                    tryValidate {
                        notStartsWith(
                            "hello world",
                            "hello",
                        ) { text("must not start with \"hello\"") }
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not start with \"hello\""
            }

            test("endsWith") {
                val result = tryValidate { endsWith("hello", "world") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must end with \"world\""
            }

            test("endsWith with message") {
                val result = tryValidate { endsWith("hello", "world") { text("must end with \"world\"") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must end with \"world\""
            }

            test("notEndsWith") {
                val result = tryValidate { notEndsWith("hello world", "world") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not end with \"world\""
            }

            test("notEndsWith with message") {
                val result = tryValidate { notEndsWith("hello world", "world") { text("must not end with \"world\"") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not end with \"world\""
            }

            test("contains") {
                val result = tryValidate { contains("hello", "test") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must contain \"test\""
            }

            test("contains with message") {
                val result = tryValidate { contains("hello", "test") { text("must contain \"test\"") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must contain \"test\""
            }

            test("notContains") {
                val result = tryValidate { notContains("test value", "test") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not contain \"test\""
            }

            test("notContains with message") {
                val result = tryValidate { notContains("test value", "test") { text("must not contain \"test\"") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not contain \"test\""
            }

            val regex = Regex("[0-9]+")
            test("matches") {
                val result = tryValidate { matches("abc", regex) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must match pattern: [0-9]+"
            }

            test("matches with message") {
                val result = tryValidate { matches("abc", regex) { text("must match pattern: $regex") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must match pattern: [0-9]+"
            }

            test("notMatches") {
                val result = tryValidate { notMatches("123", regex) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not match pattern: [0-9]+"
            }

            test("notMatches with message") {
                val result = tryValidate { notMatches("123", regex) { text("must not match pattern: $regex") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not match pattern: [0-9]+"
            }
        }

        context("kova.collection") {
            test("minSize") {
                val result = tryValidate { minSize(listOf("a", "b"), 3) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Collection (size 2) must have at least 3 elements"
            }

            test("minSize with message") {
                val result =
                    tryValidate {
                        minSize(
                            listOf("a", "b"),
                            3,
                        ) { text("Collection (size $it) must have at least 3 elements") }
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Collection (size 2) must have at least 3 elements"
            }

            test("maxSize") {
                val result = tryValidate { maxSize(listOf("a", "b", "c", "d"), 3) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Collection (size 4) must have at most 3 elements"
            }

            test("maxSize with message") {
                val result =
                    tryValidate {
                        maxSize(
                            listOf("a", "b", "c", "d"),
                            3,
                        ) { text("Collection (size $it) must have at most 3 elements") }
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Collection (size 4) must have at most 3 elements"
            }

            test("size") {
                val result = tryValidate { size(listOf("a", "b"), 3) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Collection (size 2) must have exactly 3 elements"
            }

            test("size with message") {
                val result =
                    tryValidate {
                        size(
                            listOf("a", "b"),
                            3,
                        ) { text("Collection (size $it) must have exactly 3 elements") }
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Collection (size 2) must have exactly 3 elements"
            }

            test("onEach") {
                val result = tryValidate { onEach(listOf(1, -2, 3)) { positive(it) } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Some elements do not satisfy the constraint: [must be positive]"
            }

            test("notEmpty") {
                val result = tryValidate { notEmpty(emptyList<Nothing>()) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not be empty"
            }

            test("contains") {
                val result = tryValidate { has(listOf("bar", "baz"), "foo") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must contain foo"
            }

            test("contains with message") {
                val result = tryValidate { has(listOf("bar", "baz"), "foo") { text("must contain foo") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must contain foo"
            }

            test("notContains") {
                val result = tryValidate { notContains(listOf("foo", "bar"), "foo") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not contain foo"
            }

            test("notContains with message") {
                val result = tryValidate { notContains(listOf("foo", "bar"), "foo") { text("must not contain foo") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not contain foo"
            }
        }

        context("kova.comparable") {
            test("min") {
                val result = tryValidate { minValue(5, 10) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be greater than or equal to 10"
            }

            test("min with message") {
                val result = tryValidate { minValue(5, 10) { text("must be greater than or equal to 10") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be greater than or equal to 10"
            }

            test("max") {
                val result = tryValidate { maxValue(15, 10) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be less than or equal to 10"
            }

            test("max with message") {
                val result = tryValidate { maxValue(15, 10) { text("must be less than or equal to 10") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be less than or equal to 10"
            }

            test("gtValue") {
                val result = tryValidate { gtValue(10, 10) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be greater than 10"
            }

            test("gtValue with message") {
                val result =
                    tryValidate { gtValue(10, 10) { text("must be greater than 10") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be greater than 10"
            }

            test("gteValue") {
                val result = tryValidate { gteValue(9, 10) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be greater than or equal to 10"
            }

            test("gteValue with message") {
                val result = tryValidate { gteValue(9, 10) { text("must be greater than or equal to 10") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be greater than or equal to 10"
            }

            test("ltValue") {
                val result = tryValidate { ltValue(10, 10) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be less than 10"
            }

            test("ltValue with message") {
                val result = tryValidate { ltValue(10, 10) { text("must be less than 10") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be less than 10"
            }

            test("lteValue") {
                val result = tryValidate { lteValue(11, 10) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be less than or equal to 10"
            }

            test("lteValue with message") {
                val result = tryValidate { lteValue(11, 10) { text("must be less than or equal to 10") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be less than or equal to 10"
            }

            test("eq") {
                val result = tryValidate { eq(10, 42) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be equal to 42"
            }

            test("eq with message") {
                val result = tryValidate { eq(10, 42) { text("must be equal to 42") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be equal to 42"
            }

            test("notEq") {
                val result = tryValidate { notEq(0, 0) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not be equal to 0"
            }

            test("notEq with message") {
                val result = tryValidate { notEq(0, 0) { text("must not be equal to 0") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not be equal to 0"
            }
        }

        context("kova.literal") {
            test("single") {
                val result = tryValidate { literal(10, 42) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be 42"
            }

            test("single with message") {
                val result =
                    tryValidate { literal(10, 42) { text("must be 42") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be 42"
            }

            test("list") {
                val result = tryValidate { literal(5, listOf(1, 2, 3)) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be one of: [1, 2, 3]"
            }

            test("list with message") {
                val result = tryValidate { literal(5, listOf(1, 2, 3)) { text("must be one of: ${listOf(1, 2, 3)}") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be one of: [1, 2, 3]"
            }
        }

        context("kova.map") {
            test("minSize") {
                val result = tryValidate { minSize(mapOf("a" to 1, "b" to 2), 3) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Map (size 2) must have at least 3 entries"
            }

            test("minSize with message") {
                val result =
                    tryValidate {
                        minSize(
                            mapOf("a" to 1, "b" to 2),
                            3,
                        ) { text("Map (size $it) must have at least 3 entries") }
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Map (size 2) must have at least 3 entries"
            }

            test("maxSize") {
                val result = tryValidate { maxSize(mapOf("a" to 1, "b" to 2, "c" to 3), 2) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Map (size 3) must have at most 2 entries"
            }

            test("maxSize with message") {
                val result =
                    tryValidate {
                        maxSize(
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

            test("size") {
                val result = tryValidate { size(mapOf("a" to 1, "b" to 2), 3) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Map (size 2) must have exactly 3 entries"
            }

            test("size with message") {
                val result =
                    tryValidate {
                        size(
                            mapOf("a" to 1, "b" to 2),
                            3,
                        ) { text("Map (size $it) must have exactly 3 entries") }
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Map (size 2) must have exactly 3 entries"
            }

            test("onEachKey") {
                val result = tryValidate { onEachKey(mapOf("a" to 1, "bb" to 2)) { minLength(it, 2) } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Some keys do not satisfy the constraint: [must be at least 2 characters]"
            }

            test("onEachValue") {
                val result = tryValidate { onEachValue(mapOf("a" to 1, "b" to -2)) { positive(it) } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Some values do not satisfy the constraint: [must be positive]"
            }

            test("notEmpty") {
                val result = tryValidate { notEmpty(emptyMap<Nothing, Nothing>()) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not be empty"
            }

            test("containsKey") {
                val result = tryValidate { hasKey(mapOf("bar" to 2, "baz" to 3), "foo") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must contain key foo"
            }

            test("containsKey with message") {
                val result =
                    tryValidate {
                        hasValue(
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

            test("containsValue") {
                val result = tryValidate { hasValue(mapOf("foo" to 1, "bar" to 2), 42) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must contain value 42"
            }

            test("containsValue with message") {
                val result = tryValidate { hasValue(mapOf("foo" to 1, "bar" to 2), 42) { text("must contain value 42") } }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must contain value 42"
            }

            test("notContainsValue") {
                val result =
                    tryValidate { notContainsValue(mapOf("foo" to 42, "bar" to 2), 42) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not contain value 42"
            }

            test("notContainsValue with message") {
                val result =
                    tryValidate {
                        notContainsValue(mapOf("foo" to 42, "bar" to 2), 42) { text("must not contain value 42") }
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not contain value 42"
            }
        }

        context("kova.nullable") {
            test("isNull") {
                val result = tryValidate { isNull("value") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be null"
            }

            test("notNull") {
                val result = tryValidate { notNull(null) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not be null"
            }
        }

        context("kova.number") {
            test("positive") {
                val result = tryValidate { positive(-5) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be positive"
            }

            test("negative") {
                val result = tryValidate { negative(5) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be negative"
            }

            test("notPositive") {
                val result = tryValidate { notPositive(5) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not be positive"
            }

            test("notNegative") {
                val result = tryValidate { notNegative(-5) }
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
                        or { positive(value) } orElse { ltValue(value, 0) }
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "at least one constraint must be satisfied: [[must be positive], [must be less than 0]]"
            }
        }

        context("kova.string") {
            test("isInt") {
                val result = tryValidate { isInt("abc") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be a valid integer"
            }

            test("isLong") {
                val result = tryValidate { isLong("abc") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be a valid long"
            }

            test("isShort") {
                val result = tryValidate { isShort("abc") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be a valid short"
            }

            test("isByte") {
                val result = tryValidate { isByte("abc") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be a valid byte"
            }

            test("isDouble") {
                val result = tryValidate { isDouble("abc") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be a valid double"
            }

            test("isFloat") {
                val result = tryValidate { isFloat("abc") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be a valid float"
            }

            test("isBigDecimal") {
                val result = tryValidate { isBigDecimal("abc") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be a valid decimal number"
            }

            test("isBigInteger") {
                val result = tryValidate { isBigInteger("abc") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be a valid integer number"
            }

            test("isBoolean") {
                val result = tryValidate { isBoolean("abc") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be \"true\" or \"false\""
            }

            test("isEnum") {
                val result = tryValidate { isEnum<TestEnum>("INVALID") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be one of: [A, B, C]"
            }

            test("uppercase") {
                val result = tryValidate { uppercase("abc") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be uppercase"
            }

            test("lowercase") {
                val result = tryValidate { lowercase("ABC") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be lowercase"
            }
        }

        context("kova.temporal") {
            test("future") {
                val result =
                    tryValidate {
                        future(
                            LocalDate
                                .now()
                                .minusDays(1),
                        )
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be in the future"
            }

            test("futureOrPresent") {
                val result =
                    tryValidate {
                        futureOrPresent(
                            LocalDate
                                .now()
                                .minusDays(1),
                        )
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be in the future or present"
            }

            test("past") {
                val result =
                    tryValidate {
                        past(
                            LocalDate
                                .now()
                                .plusDays(1),
                        )
                    }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be in the past"
            }

            test("pastOrPresent") {
                val result =
                    tryValidate {
                        pastOrPresent(
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
