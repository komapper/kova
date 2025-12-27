package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec

class ElvisValidatorTest :
    FunSpec({
        context("and") {
            fun Validation.whenNotNullMin3(i: Int?) {
                if (i != null) min(i, 3)
            }

            test("success with non-null value") {
                val result = tryValidate { whenNotNullMin3(4) }
                result.shouldBeSuccess()
            }

            test("success with null value") {
                val logs = mutableListOf<LogEntry>()
                val result = tryValidate(ValidationConfig(logger = { logs.add(it) })) { whenNotNullMin3(null) }
                result.shouldBeSuccess()
                logs shouldBe listOf()
            }

            test("failure when min 3 constraint violated") {
                val result = tryValidate { whenNotNullMin3(2) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.comparable.min"
            }
        }

        context("and with each List element") {
            fun Validation.min3OrSucceed(i: Int?) {
                if (i != null) min(i, 3)
            }
            test("success with non-null value") {
                val result = tryValidate { onEach(listOf(4, 5)) { min3OrSucceed(it) } }
                result.shouldBeSuccess()
            }

            test("success with null value") {
                val result = tryValidate { onEach(listOf<Int?>(null, null)) { min3OrSucceed(it) } }
                result.shouldBeSuccess()
            }

            test("failure when min3 constraint violated") {
                val result = tryValidate { onEach(listOf(2, null)) { min3OrSucceed(it) } }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.collection.onEach"
            }
        }

        context("asNullable") {
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

        context("then") {
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

        context("or") {
            fun Validation.nullableMax5OrMin3(i: Int?) =
                i?.let {
                    val _ = or { max(it, 5) } orElse { min(it, 3) }
                    it
                } ?: 0

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
    })
