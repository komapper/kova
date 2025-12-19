package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate

class KovaPropertiesTest :
    FunSpec({

        context("kova.charSequence") {
            test("min") {
                val result = Kova.string().min(5).tryValidate("abc")
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be at least 5 characters"
            }

            test("min with message") {
                val result =
                    Kova
                        .string()
                        .min(5) { text("must be at least $it characters") }
                        .tryValidate("abc")
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be at least 5 characters"
            }

            test("max") {
                val result = Kova.string().max(5).tryValidate("abcdef")
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be at most 5 characters"
            }

            test("max with message") {
                val result =
                    Kova
                        .string()
                        .max(5) { text("must be at most $it characters") }
                        .tryValidate("abcdef")
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be at most 5 characters"
            }

            test("length") {
                val result = Kova.string().length(5).tryValidate("abc")
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be exactly 5 characters"
            }

            test("length with message") {
                val result =
                    Kova
                        .string()
                        .length(5) { text("must be exactly $it characters") }
                        .tryValidate("abc")
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be exactly 5 characters"
            }

            test("notBlank") {
                val result = Kova.string().notBlank().tryValidate("  ")
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must not be blank"
            }

            test("blank") {
                val result = Kova.string().blank().tryValidate("abc")
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be blank"
            }

            test("notEmpty") {
                val result = Kova.string().notEmpty().tryValidate("")
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must not be empty"
            }

            test("empty") {
                val result = Kova.string().empty().tryValidate("abc")
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be empty"
            }

            test("startsWith") {
                val result = Kova.string().startsWith("hello").tryValidate("world")
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must start with \"hello\""
            }

            test("startsWith with message") {
                val result =
                    Kova
                        .string()
                        .startsWith("hello") { text("must start with \"$it\"") }
                        .tryValidate("world")
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must start with \"hello\""
            }

            test("notStartsWith") {
                val result = Kova.string().notStartsWith("hello").tryValidate("hello world")
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must not start with \"hello\""
            }

            test("notStartsWith with message") {
                val result =
                    Kova
                        .string()
                        .notStartsWith("hello") { text("must not start with \"$it\"") }
                        .tryValidate("hello world")
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must not start with \"hello\""
            }

            test("endsWith") {
                val result = Kova.string().endsWith("world").tryValidate("hello")
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must end with \"world\""
            }

            test("endsWith with message") {
                val result =
                    Kova
                        .string()
                        .endsWith("world") { text("must end with \"$it\"") }
                        .tryValidate("hello")
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must end with \"world\""
            }

            test("notEndsWith") {
                val result = Kova.string().notEndsWith("world").tryValidate("hello world")
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must not end with \"world\""
            }

            test("notEndsWith with message") {
                val result =
                    Kova
                        .string()
                        .notEndsWith("world") { text("must not end with \"$it\"") }
                        .tryValidate("hello world")
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must not end with \"world\""
            }

            test("contains") {
                val result = Kova.string().contains("test").tryValidate("hello")
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must contain \"test\""
            }

            test("contains with message") {
                val result =
                    Kova
                        .string()
                        .contains("test") { text("must contain \"$it\"") }
                        .tryValidate("hello")
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must contain \"test\""
            }

            test("notContains") {
                val result = Kova.string().notContains("test").tryValidate("test value")
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must not contain \"test\""
            }

            test("notContains with message") {
                val result =
                    Kova
                        .string()
                        .notContains("test") { text("must not contain \"$it\"") }
                        .tryValidate("test value")
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must not contain \"test\""
            }

            test("matches") {
                val result = Kova.string().matches(Regex("[0-9]+")).tryValidate("abc")
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must match pattern: [0-9]+"
            }

            test("matches with message") {
                val result =
                    Kova
                        .string()
                        .matches(Regex("[0-9]+")) { text("must match pattern: $it") }
                        .tryValidate("abc")
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must match pattern: [0-9]+"
            }

            test("notMatches") {
                val result = Kova.string().notMatches(Regex("[0-9]+")).tryValidate("123")
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must not match pattern: [0-9]+"
            }

            test("notMatches with message") {
                val result =
                    Kova
                        .string()
                        .notMatches(Regex("[0-9]+")) { text("must not match pattern: $it") }
                        .tryValidate("123")
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must not match pattern: [0-9]+"
            }
        }

        context("kova.collection") {
            test("min") {
                val result = Kova.list<String>().min(3).tryValidate(listOf("a", "b"))
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "Collection (size 2) must have at least 3 elements"
            }

            test("min with message") {
                val result =
                    Kova
                        .list<String>()
                        .min(3) { actual, min ->
                            text("Collection (size $actual) must have at least $min elements")
                        }
                        .tryValidate(listOf("a", "b"))
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "Collection (size 2) must have at least 3 elements"
            }

            test("max") {
                val result = Kova.list<String>().max(3).tryValidate(listOf("a", "b", "c", "d"))
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "Collection (size 4) must have at most 3 elements"
            }

            test("max with message") {
                val result =
                    Kova
                        .list<String>()
                        .max(3) { actual, max ->
                            text("Collection (size $actual) must have at most $max elements")
                        }
                        .tryValidate(listOf("a", "b", "c", "d"))
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "Collection (size 4) must have at most 3 elements"
            }

            test("length") {
                val result = Kova.list<String>().length(3).tryValidate(listOf("a", "b"))
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "Collection (size 2) must have exactly 3 elements"
            }

            test("length with message") {
                val result =
                    Kova
                        .list<String>()
                        .length(3) { actual, expected ->
                            text("Collection (size $actual) must have exactly $expected elements")
                        }
                        .tryValidate(listOf("a", "b"))
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "Collection (size 2) must have exactly 3 elements"
            }

            test("onEach") {
                val result = Kova.list<Int>().onEach { it.positive() }.tryValidate(listOf(1, -2, 3))
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "Some elements do not satisfy the constraint: [must be positive]"
            }

            test("notEmpty") {
                val result = Kova.list<String>().notEmpty().tryValidate(emptyList())
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must not be empty"
            }

            test("contains") {
                val result = Kova.list<String>().contains("foo").tryValidate(listOf("bar", "baz"))
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must contain foo"
            }

            test("contains with message") {
                val result =
                    Kova
                        .list<String>()
                        .contains("foo") { text("must contain $it") }
                        .tryValidate(listOf("bar", "baz"))
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must contain foo"
            }

            test("notContains") {
                val result = Kova.list<String>().notContains("foo").tryValidate(listOf("foo", "bar"))
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must not contain foo"
            }

            test("notContains with message") {
                val result =
                    Kova
                        .list<String>()
                        .notContains("foo") { text("must not contain $it") }
                        .tryValidate(listOf("foo", "bar"))
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must not contain foo"
            }
        }

        context("kova.comparable") {
            test("min") {
                val result = Kova.int().min(10).tryValidate(5)
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be greater than or equal to 10"
            }

            test("min with message") {
                val result =
                    Kova
                        .int()
                        .min(10) { text("must be greater than or equal to $it") }
                        .tryValidate(5)
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be greater than or equal to 10"
            }

            test("max") {
                val result = Kova.int().max(10).tryValidate(15)
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be less than or equal to 10"
            }

            test("max with message") {
                val result =
                    Kova
                        .int()
                        .max(10) { text("must be less than or equal to $it") }
                        .tryValidate(15)
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be less than or equal to 10"
            }

            test("gt") {
                val result = Kova.int().gt(10).tryValidate(10)
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be greater than 10"
            }

            test("gt with message") {
                val result =
                    Kova.int().gt(10) { text("must be greater than $it") }.tryValidate(10)
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be greater than 10"
            }

            test("gte") {
                val result = Kova.int().gte(10).tryValidate(9)
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be greater than or equal to 10"
            }

            test("gte with message") {
                val result =
                    Kova
                        .int()
                        .gte(10) { text("must be greater than or equal to $it") }
                        .tryValidate(9)
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be greater than or equal to 10"
            }

            test("lt") {
                val result = Kova.int().lt(10).tryValidate(10)
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be less than 10"
            }

            test("lt with message") {
                val result =
                    Kova.int().lt(10) { text("must be less than $it") }.tryValidate(10)
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be less than 10"
            }

            test("lte") {
                val result = Kova.int().lte(10).tryValidate(11)
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be less than or equal to 10"
            }

            test("lte with message") {
                val result =
                    Kova
                        .int()
                        .lte(10) { text("must be less than or equal to $it") }
                        .tryValidate(11)
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be less than or equal to 10"
            }

            test("eq") {
                val result = Kova.int().eq(42).tryValidate(10)
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be equal to 42"
            }

            test("eq with message") {
                val result =
                    Kova.int().eq(42) { text("must be equal to $it") }.tryValidate(10)
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be equal to 42"
            }

            test("notEq") {
                val result = Kova.int().notEq(0).tryValidate(0)
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must not be equal to 0"
            }

            test("notEq with message") {
                val result =
                    Kova.int().notEq(0) { text("must not be equal to $it") }.tryValidate(0)
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must not be equal to 0"
            }
        }

        context("kova.literal") {
            test("single") {
                val result = Kova.int().literal(42).tryValidate(10)
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be 42"
            }

            test("single with message") {
                val result =
                    Kova.int().literal(42) { text("must be $it") }.tryValidate(10)
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be 42"
            }

            test("list") {
                val result = Kova.int().literal(listOf(1, 2, 3)).tryValidate(5)
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be one of: [1, 2, 3]"
            }

            test("list with message") {
                val result =
                    Kova
                        .int()
                        .literal(listOf(1, 2, 3)) { text("must be one of: $it") }
                        .tryValidate(5)
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be one of: [1, 2, 3]"
            }
        }

        context("kova.map") {
            test("min") {
                val result = Kova.map<String, Int>().min(3).tryValidate(mapOf("a" to 1, "b" to 2))
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "Map (size 2) must have at least 3 entries"
            }

            test("min with message") {
                val result =
                    Kova
                        .map<String, Int>()
                        .min(3) { actual, min ->
                            text("Map (size $actual) must have at least $min entries")
                        }
                        .tryValidate(mapOf("a" to 1, "b" to 2))
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "Map (size 2) must have at least 3 entries"
            }

            test("max") {
                val result = Kova.map<String, Int>().max(2).tryValidate(mapOf("a" to 1, "b" to 2, "c" to 3))
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "Map (size 3) must have at most 2 entries"
            }

            test("max with message") {
                val result =
                    Kova
                        .map<String, Int>()
                        .max(2) { actual, max -> text("Map (size $actual) must have at most $max entries") }
                        .tryValidate(mapOf("a" to 1, "b" to 2, "c" to 3))
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "Map (size 3) must have at most 2 entries"
            }

            test("length") {
                val result = Kova.map<String, Int>().length(3).tryValidate(mapOf("a" to 1, "b" to 2))
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "Map (size 2) must have exactly 3 entries"
            }

            test("length with message") {
                val result =
                    Kova
                        .map<String, Int>()
                        .length(3) { actual, expected ->
                            text("Map (size $actual) must have exactly $expected entries")
                        }
                        .tryValidate(mapOf("a" to 1, "b" to 2))
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "Map (size 2) must have exactly 3 entries"
            }

            test("onEachKey") {
                val result =
                    Kova
                        .map<String, Int>()
                        .onEachKey { it.min(2) }
                        .tryValidate(mapOf("a" to 1, "bb" to 2))
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "Some keys do not satisfy the constraint: [must be at least 2 characters]"
            }

            test("onEachValue") {
                val result =
                    Kova
                        .map<String, Int>()
                        .onEachValue { it.positive() }
                        .tryValidate(mapOf("a" to 1, "b" to -2))
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "Some values do not satisfy the constraint: [must be positive]"
            }

            test("notEmpty") {
                val result = Kova.map<String, Int>().notEmpty().tryValidate(emptyMap())
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must not be empty"
            }

            test("containsKey") {
                val result = Kova.map<String, Int>().containsKey("foo").tryValidate(mapOf("bar" to 2, "baz" to 3))
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must contain key foo"
            }

            test("containsKey with message") {
                val result =
                    Kova
                        .map<String, Int>()
                        .containsKey("foo") { text("must contain key $it") }
                        .tryValidate(mapOf("bar" to 2, "baz" to 3))
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must contain key foo"
            }

            test("notContainsKey") {
                val result = Kova.map<String, Int>().notContainsKey("foo").tryValidate(mapOf("foo" to 1, "bar" to 2))
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must not contain key foo"
            }

            test("notContainsKey with message") {
                val result =
                    Kova
                        .map<String, Int>()
                        .notContainsKey("foo") { text("must not contain key $it") }
                        .tryValidate(mapOf("foo" to 1, "bar" to 2))
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must not contain key foo"
            }

            test("containsValue") {
                val result = Kova.map<String, Int>().containsValue(42).tryValidate(mapOf("foo" to 1, "bar" to 2))
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must contain value 42"
            }

            test("containsValue with message") {
                val result =
                    Kova
                        .map<String, Int>()
                        .containsValue(42) { text("must contain value $it") }
                        .tryValidate(mapOf("foo" to 1, "bar" to 2))
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must contain value 42"
            }

            test("notContainsValue") {
                val result = Kova.map<String, Int>().notContainsValue(42).tryValidate(mapOf("foo" to 42, "bar" to 2))
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must not contain value 42"
            }

            test("notContainsValue with message") {
                val result =
                    Kova
                        .map<String, Int>()
                        .notContainsValue(42) { text("must not contain value $it") }
                        .tryValidate(mapOf("foo" to 42, "bar" to 2))
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must not contain value 42"
            }
        }

        context("kova.nullable") {
            test("isNull") {
                val result = Kova.nullable<String>().isNull().tryValidate("value")
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be null"
            }

            test("notNull") {
                val result = Kova.nullable<String>().notNull().tryValidate(null)
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must not be null"
            }
        }

        context("kova.number") {
            test("positive") {
                val result = Kova.int().positive().tryValidate(-5)
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be positive"
            }

            test("negative") {
                val result = Kova.int().negative().tryValidate(5)
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be negative"
            }

            test("notPositive") {
                val result = Kova.int().notPositive().tryValidate(5)
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must not be positive"
            }

            test("notNegative") {
                val result = Kova.int().notNegative().tryValidate(-5)
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must not be negative"
            }
        }

        context("kova.or") {
            test("or") {
                val result =
                    Kova
                        .int()
                        .positive()
                        .or { Kova.int().lt(0) }
                        .tryValidate(0)
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "at least one constraint must be satisfied: [[must be positive], [must be less than 0]]"
            }
        }

        context("kova.string") {
            test("isInt") {
                val result = Kova.string().isInt().tryValidate("abc")
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be a valid integer"
            }

            test("isLong") {
                val result = Kova.string().isLong().tryValidate("abc")
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be a valid long"
            }

            test("isShort") {
                val result = Kova.string().isShort().tryValidate("abc")
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be a valid short"
            }

            test("isByte") {
                val result = Kova.string().isByte().tryValidate("abc")
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be a valid byte"
            }

            test("isDouble") {
                val result = Kova.string().isDouble().tryValidate("abc")
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be a valid double"
            }

            test("isFloat") {
                val result = Kova.string().isFloat().tryValidate("abc")
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be a valid float"
            }

            test("isBigDecimal") {
                val result = Kova.string().isBigDecimal().tryValidate("abc")
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be a valid decimal number"
            }

            test("isBigInteger") {
                val result = Kova.string().isBigInteger().tryValidate("abc")
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be a valid integer number"
            }

            test("isBoolean") {
                val result = Kova.string().isBoolean().tryValidate("abc")
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be \"true\" or \"false\""
            }

            test("isEnum") {
                val result = Kova.string().isEnum<TestEnum>().tryValidate("INVALID")
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be one of: [A, B, C]"
            }

            test("uppercase") {
                val result = Kova.string().uppercase().tryValidate("abc")
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be uppercase"
            }

            test("lowercase") {
                val result = Kova.string().lowercase().tryValidate("ABC")
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be lowercase"
            }
        }

        context("kova.temporal") {
            test("future") {
                val result = Kova.localDate().future().tryValidate(LocalDate.now().minusDays(1))
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be in the future"
            }

            test("futureOrPresent") {
                val result = Kova.localDate().futureOrPresent().tryValidate(LocalDate.now().minusDays(1))
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be in the future or present"
            }

            test("past") {
                val result = Kova.localDate().past().tryValidate(LocalDate.now().plusDays(1))
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be in the past"
            }

            test("pastOrPresent") {
                val result = Kova.localDate().pastOrPresent().tryValidate(LocalDate.now().plusDays(1))
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.text shouldBe "must be in the past or present"
            }
        }
    }) {
    enum class TestEnum { A, B, C }
}
