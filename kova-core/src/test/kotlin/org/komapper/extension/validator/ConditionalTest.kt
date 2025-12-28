package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeInstanceOf

class ConditionalTest :
    FunSpec({

        context("if expression") {
            fun Validation.validate(i: Int) {
                if (i % 2 == 0) min(i, 3)
            }
            test("success when condition not met") {
                val result = tryValidate { validate(1) }
                result.shouldBeSuccess()
            }

            test("failure when condition met") {
                val result = tryValidate { validate(2) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.comparable.min"
            }

            context("and") {
                fun Validation.validateAndMin1(i: Int) {
                    if (i % 2 == 0) min(i, 3)
                    min(i, 1)
                }

                test("success") {
                    val result = tryValidate { validateAndMin1(1) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { validateAndMin1(0) }
                    result.shouldBeFailure()
                    result.messages.size shouldBe 2
                    result.messages[0].constraintId shouldBe "kova.comparable.min"
                    result.messages[1].constraintId shouldBe "kova.comparable.min"
                }
            }
        }

        context("if expression -  early return") {
            fun Validation.nullableMin3(i: Int?): Int {
                if (i == null) return 0
                min(i, 3)
                return i
            }

            test("success with non-null value") {
                val result = tryValidate { nullableMin3(4) }
                result.shouldBeSuccess()
                val value: Int = result.value // The type is "Int" instead of "Int?"
                value shouldBe 4
            }

            test("success with null value") {
                val result = tryValidate { nullableMin3(null) }
                result.shouldBeSuccess()
                result.value shouldBe 0
            }

            test("failure when min3 constraint is violated") {
                val result = tryValidate { nullableMin3(2) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.comparable.min"
            }
        }

        context("elvis operator") {
            fun Validation.nullableThenMin3AndMax3(i: Int?) =
                (i ?: 4).also {
                    min(it, 3)
                    max(it, 5)
                }

            test("success") {
                val result = tryValidate { nullableThenMin3AndMax3(4) }
                result.shouldBeSuccess()
            }

            test("success with null value") {
                val result = tryValidate { nullableThenMin3AndMax3(null) }
                result.shouldBeSuccess()
                result.value shouldBe 4
            }

            test("failure when min3 constraint is violated") {
                val result = tryValidate { nullableThenMin3AndMax3(2) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.comparable.min"
            }

            test("failure when max5 constraint violated") {
                val result = tryValidate { nullableThenMin3AndMax3(6) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.comparable.max"
            }
        }

        context("and") {
            fun Validation.validate(i: Int) {
                max(i, 2)
                max(i, 3)
                negative(i)
            }

            test("success") {
                val result = tryValidate { validate(-1) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { validate(5) }
                result.shouldBeFailure()
                result.messages.size shouldBe 3
                result.messages[0].constraintId shouldBe "kova.comparable.max"
                result.messages[1].constraintId shouldBe "kova.comparable.max"
                result.messages[2].constraintId shouldBe "kova.number.negative"
            }
        }

        context("or") {
            fun Validation.validate(i: UInt) {
                val _ = or { max(i, 10u) } orElse { max(i, 20u) }
                min(i, 5u)
            }

            test("success : 10") {
                val result = tryValidate { validate(10u) }
                result.shouldBeSuccess()
            }

            test("success : 20") {
                val result = tryValidate { validate(20u) }
                result.shouldBeSuccess()
            }

            test("failure : 25") {
                val result = tryValidate { validate(25u) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.or"
            }
        }

        context("or - nullable") {
            fun Validation.nullableMax5OrMin3(i: Int?): Int {
                if (i == null) return 0
                return i.let {
                    val _ = or { max(it, 5) } orElse { min(it, 3) }
                    it
                }
            }

            test("success: 3") {
                buildList {
                    val config = ValidationConfig(logger = { add(it) })
                    val result = tryValidate(config) { nullableMax5OrMin3(3) }
                    result.shouldBeSuccess()
                    result.value shouldBe 3
                } shouldBe listOf(LogEntry.Satisfied(constraintId = "kova.comparable.max", root = "", path = "", input = 3))
            }

            test("success: 2") {
                buildList {
                    val config = ValidationConfig(logger = { add(it) })
                    val result = tryValidate(config) { nullableMax5OrMin3(2) }
                    result.shouldBeSuccess()
                    result.value shouldBe 2
                } shouldBe listOf(LogEntry.Satisfied(constraintId = "kova.comparable.max", root = "", path = "", input = 2))
            }

            test("success: 6") {
                val logs = mutableListOf<LogEntry>()
                val config = ValidationConfig(logger = { logs.add(it) })
                val result = tryValidate(config) { nullableMax5OrMin3(6) }
                result.shouldBeSuccess()
                result.value shouldBe 6
                logs shouldBe
                    listOf(
                        LogEntry.Violated(
                            constraintId = "kova.comparable.max",
                            root = "",
                            path = "",
                            input = 6,
                            args = listOf(5),
                        ),
                        LogEntry.Satisfied(constraintId = "kova.comparable.min", root = "", path = "", input = 6),
                    )
            }

            test("success: null") {
                val logs = mutableListOf<LogEntry>()
                val config = ValidationConfig(logger = { logs.add(it) })
                val result = tryValidate(config) { nullableMax5OrMin3(null) }
                result.shouldBeSuccess()
                result.value shouldBe 0
                logs shouldBe listOf()
            }
        }

        context("or: 2 branches") {
            fun Validation.length2or5(string: String) = or { length(string, 2) } orElse { length(string, 5) }

            test("success with length 2") {
                val result = tryValidate { length2or5("ab") }
                result.shouldBeSuccess()
            }
            test("success with length 5") {
                val result = tryValidate { length2or5("abcde") }
                result.shouldBeSuccess()
            }
            test("failure with length 3") {
                val result = tryValidate { length2or5("abc") }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].let {
                    it.constraintId shouldBe "kova.or"
                    it.args.size shouldBe 2
                    it.args[0]
                        .shouldBeInstanceOf<List<Message>>()
                        .single()
                        .constraintId shouldBe "kova.charSequence.length"
                    it.args[1]
                        .shouldBeInstanceOf<List<Message>>()
                        .single()
                        .constraintId shouldBe "kova.charSequence.length"
                }
            }
        }

        context("or: 3 branches") {
            fun Validation.length2or5or7(string: String) =
                or { length(string, 2) } or { length(string, 5) } orElse {
                    length(string, 7)
                }

            test("failure with length 3") {
                val result = tryValidate { length2or5or7("abc") }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].let {
                    it.constraintId shouldBe "kova.or"
                    it.args.size shouldBe 2
                    it.args[0]
                        .shouldBeInstanceOf<List<Message>>()
                        .single()
                        .constraintId shouldBe "kova.or"
                    it.args[1]
                        .shouldBeInstanceOf<List<Message>>()
                        .single()
                        .constraintId shouldBe "kova.charSequence.length"
                    println(it)
                }
            }
        }

    })
