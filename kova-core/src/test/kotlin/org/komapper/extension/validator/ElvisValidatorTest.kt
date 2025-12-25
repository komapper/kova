package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec

class ElvisValidatorTest :
    FunSpec({
        context("and") {
            context(_: Validation, _: Accumulate)
            fun Int?.whenNotNullMin3() {
                this?.min(3)
            }

            test("success with non-null value") {
                val result = tryValidate { 4.whenNotNullMin3() }
                result.shouldBeSuccess()
            }

            test("success with null value") {
                val logs = mutableListOf<LogEntry>()
                val result = tryValidate(ValidationConfig(logger = { logs.add(it) })) { null.whenNotNullMin3() }
                result.shouldBeSuccess()
                logs shouldBe listOf()
            }

            test("failure when min 3 constraint violated") {
                val result = tryValidate { 2.whenNotNullMin3() }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.comparable.min"
            }
        }

        context("and with each List element") {
            context(_: Validation, _: Accumulate)
            fun Int?.min3OrSucceed() {
                this?.min(3)
            }
            test("success with non-null value") {
                val result = tryValidate { listOf(4, 5).onEach { it.min3OrSucceed() } }
                result.shouldBeSuccess()
            }

            test("success with null value") {
                val result = tryValidate { listOf<Int?>(null, null).onEach { it.min3OrSucceed() } }
                result.shouldBeSuccess()
            }

            test("failure when min3 constraint violated") {
                val result = tryValidate { listOf(2, null).onEach { it.min3OrSucceed() } }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.collection.onEach"
            }
        }

        context("asNullable") {
            context(_: Validation, _: Accumulate)
            fun Int?.nullableMin3(): Int {
                this?.min(3)
                return this ?: 0
            }

            test("success with non-null value") {
                val result = tryValidate { 4.nullableMin3() }
                result.shouldBeSuccess()
                val value: Int = result.value // The type is "Int" instead of "Int?"
                value shouldBe 4
            }

            test("success with null value") {
                val result = tryValidate { null.nullableMin3() }
                result.shouldBeSuccess()
                result.value shouldBe 0
            }

            test("failure when min3 constraint is violated") {
                val result = tryValidate { 2.nullableMin3() }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.comparable.min"
            }
        }

        context("then") {
            context(_: Validation, _: Accumulate)
            fun Int?.nullableThenMin3AndMax3() =
                (this ?: 4).also {
                    it.min(3)
                    it.max(5)
                }

            test("success") {
                val result = tryValidate { 4.nullableThenMin3AndMax3() }
                result.shouldBeSuccess()
            }

            test("success with null value") {
                val result = tryValidate { null.nullableThenMin3AndMax3() }
                result.shouldBeSuccess()
                result.value shouldBe 4
            }

            test("failure when min3 constraint is violated") {
                val result = tryValidate { 2.nullableThenMin3AndMax3() }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.comparable.min"
            }

            test("failure when max5 constraint violated") {
                val result = tryValidate { 6.nullableThenMin3AndMax3() }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.comparable.max"
            }
        }

        context("or") {
            context(_: Validation, _: Accumulate)
            fun Int?.nullableMax5OrMin3() =
                this?.let {
                    val _ = or { it.max(5) } orElse { it.min(3) }
                    it
                } ?: 0

            test("success: 3") {
                buildList {
                    val config = ValidationConfig(logger = { add(it) })
                    val result = tryValidate(config) { 3.nullableMax5OrMin3() }
                    result.shouldBeSuccess()
                    result.value shouldBe 3
                } shouldBe listOf(LogEntry.Satisfied(constraintId = "kova.comparable.max", root = "", path = "", input = 3))
            }

            test("success: 2") {
                buildList {
                    val config = ValidationConfig(logger = { add(it) })
                    val result = tryValidate(config) { 2.nullableMax5OrMin3() }
                    result.shouldBeSuccess()
                    result.value shouldBe 2
                } shouldBe listOf(LogEntry.Satisfied(constraintId = "kova.comparable.max", root = "", path = "", input = 2))
            }

            test("success: 6") {
                val logs = mutableListOf<LogEntry>()
                val config = ValidationConfig(logger = { logs.add(it) })
                val result = tryValidate(config) { 6.nullableMax5OrMin3() }
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
                val result = tryValidate(config) { null.nullableMax5OrMin3() }
                result.shouldBeSuccess()
                result.value shouldBe 0
                logs shouldBe listOf()
            }
        }
    })
