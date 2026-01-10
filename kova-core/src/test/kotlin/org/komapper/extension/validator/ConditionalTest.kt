package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeInstanceOf
import java.util.Locale

class ConditionalTest :
    FunSpec({

        beforeSpec {
            Locale.setDefault(Locale.US)
        }

        context("if expression") {
            context(_: Validation)
            fun validate(i: Int) {
                if (i % 2 == 0) i.ensureAtLeast(3)
            }
            test("success when condition not met") {
                val result = tryValidate { validate(1) }
                result.shouldBeSuccess()
            }

            test("failure when condition met") {
                val result = tryValidate { validate(2) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.comparable.atLeast"
            }

            context("and") {
                context(_: Validation)
                fun validateAndMin1(i: Int) {
                    if (i % 2 == 0) i.ensureAtLeast(3)
                    i.ensureAtLeast(1)
                }

                test("success") {
                    val result = tryValidate { validateAndMin1(1) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { validateAndMin1(0) }
                    result.shouldBeFailure()
                    result.messages.size shouldBe 2
                    result.messages[0].constraintId shouldBe "kova.comparable.atLeast"
                    result.messages[1].constraintId shouldBe "kova.comparable.atLeast"
                }
            }
        }

        context("if expression -  early return") {
            context(_: Validation)
            fun nullableMin3(i: Int?): Int {
                if (i == null) return 0
                i.ensureAtLeast(3)
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
                result.messages[0].constraintId shouldBe "kova.comparable.atLeast"
            }
        }

        context("elvis operator") {
            context(_: Validation)
            fun nullableThenMin3AndMax3(i: Int?) =
                (i ?: 4).also {
                    it.ensureAtLeast(3)
                    it.ensureAtMost(5)
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
                result.messages[0].constraintId shouldBe "kova.comparable.atLeast"
            }

            test("failure when max5 constraint violated") {
                val result = tryValidate { nullableThenMin3AndMax3(6) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.comparable.atMost"
            }
        }

        context("and") {
            context(_: Validation)
            fun validate(i: Int) {
                i.ensureAtMost(2)
                i.ensureAtMost(3)
                i.ensureNegative()
            }

            test("success") {
                val result = tryValidate { validate(-1) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { validate(5) }
                result.shouldBeFailure()
                result.messages.size shouldBe 3
                result.messages[0].constraintId shouldBe "kova.comparable.atMost"
                result.messages[1].constraintId shouldBe "kova.comparable.atMost"
                result.messages[2].constraintId shouldBe "kova.number.negative"
            }
        }

        context("or") {
            context(_: Validation)
            fun validate(i: UInt) {
                val _ = or { i.ensureAtMost(10u) } orElse { i.ensureAtMost(20u) }
                i.ensureAtLeast(5u)
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
            context(_: Validation)
            fun nullableMax5OrMin3(i: Int?): Int {
                if (i == null) return 0
                return i.let {
                    val _ = or { it.ensureAtMost(5) } orElse { it.ensureAtLeast(3) }
                    it
                }
            }

            test("success: 3") {
                buildList {
                    val config = ValidationConfig(logger = { add(it) })
                    val result = tryValidate(config) { nullableMax5OrMin3(3) }
                    result.shouldBeSuccess()
                    result.value shouldBe 3
                } shouldBe listOf(LogEntry.Satisfied(constraintId = "kova.comparable.atMost", root = "", path = "", input = 3))
            }

            test("success: 2") {
                buildList {
                    val config = ValidationConfig(logger = { add(it) })
                    val result = tryValidate(config) { nullableMax5OrMin3(2) }
                    result.shouldBeSuccess()
                    result.value shouldBe 2
                } shouldBe listOf(LogEntry.Satisfied(constraintId = "kova.comparable.atMost", root = "", path = "", input = 2))
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
                            constraintId = "kova.comparable.atMost",
                            root = "",
                            path = "",
                            input = 6,
                            args = listOf(5),
                        ),
                        LogEntry.Satisfied(constraintId = "kova.comparable.atLeast", root = "", path = "", input = 6),
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
            context(_: Validation)
            fun length2or5(string: String) = or { string.ensureLength(2) } orElse { string.ensureLength(5) }

            test("success with ensureLength 2") {
                val result = tryValidate { length2or5("ab") }
                result.shouldBeSuccess()
            }
            test("success with ensureLength 5") {
                val result = tryValidate { length2or5("abcde") }
                result.shouldBeSuccess()
            }
            test("failure with ensureLength 3") {
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
            context(_: Validation)
            fun length2or5or7(string: String) =
                or { string.ensureLength(2) } or { string.ensureLength(5) } orElse {
                    string.ensureLength(7)
                }

            test("failure with ensureLength 3") {
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
