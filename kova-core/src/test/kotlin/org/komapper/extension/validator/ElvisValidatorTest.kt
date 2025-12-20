package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ElvisValidatorTest :
    FunSpec({

        context("nullable") {
            val nullable = Kova.nullable(0)

            test("success with null value") {
                val result = nullable.tryValidate(null)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 0
            }

            test("success with non-null value") {
                val result = nullable.tryValidate(123)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 123
            }
        }

        context("nullable with default value from lambda") {
            val nullable = Kova.nullable { 0 }

            test("success with null value") {
                val result = nullable.tryValidate(null)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 0
            }

            test("success with non-null value") {
                val result = nullable.tryValidate(123)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 123
            }
        }

        context("and") {
            val min3 = Kova.int().min(3)
            val whenNotNullMin3 = Kova.nullable(3).and(min3)

            test("success with non-null value") {
                val result = whenNotNullMin3.tryValidate(4)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 4
            }

            test("success with null value") {
                val logs = mutableListOf<LogEntry>()
                val result = whenNotNullMin3.tryValidate(null, config = ValidationConfig(logger = { logs.add(it) }))
                result.isSuccess().mustBeTrue(result)
                result.value shouldBe 3
                logs shouldBe listOf()
            }

            test("failure when min 3 constraint violated") {
                val result = whenNotNullMin3.tryValidate(2)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.comparable.min"
            }
        }

        context("and with each List element") {
            val min3 = Kova.int().min(3)
            val nullableMin3 = Kova.nullable(0).and(min3)
            val onEachNullableMin3 = Kova.list<Int?>().onEach(nullableMin3)

            test("success with non-null value") {
                val result = onEachNullableMin3.tryValidate(listOf(4, 5))
                result.isSuccess().mustBeTrue()
            }

            test("success with null value") {
                val result = onEachNullableMin3.tryValidate(listOf(null, null))
                result.isSuccess().mustBeTrue()
            }

            test("failure when min3 constraint violated") {
                val result = onEachNullableMin3.tryValidate(listOf(2, null))
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.collection.onEach"
            }
        }

        context("asNullable") {
            val min3 = Kova.int().min(3)
            val nullableMin3 = min3.asNullable(0)

            test("success with non-null value") {
                val result = nullableMin3.tryValidate(4)
                result.isSuccess().mustBeTrue()
                val value: Int = result.value // The type is "Int" instead of "Int?"
                value shouldBe 4
            }

            test("success with null value") {
                val result = nullableMin3.tryValidate(null)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 0
            }

            test("failure when min3 constraint is violated") {
                val result = nullableMin3.tryValidate(2)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.comparable.min"
            }
        }

        context("then") {
            val max5 = Kova.int().max(5)
            val min3 = Kova.int().min(3)
            val nullableThenMin3AndMax3 = Kova.nullable(4).then(min3 and max5)

            test("success") {
                val result = nullableThenMin3AndMax3.tryValidate(4)
                result.isSuccess().mustBeTrue()
            }

            test("success with null value") {
                val result = nullableThenMin3AndMax3.tryValidate(null)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 4
            }

            test("failure when min3 constraint is violated") {
                val result = nullableThenMin3AndMax3.tryValidate(2)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.comparable.min"
            }

            test("failure when max5 constraint violated") {
                val result = nullableThenMin3AndMax3.tryValidate(6)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.comparable.max"
            }
        }

        context("or") {
            val min3 = Kova.int().min(3)
            val nullableMax5OrMin3 =
                Kova
                    .int()
                    .max(5)
                    .asNullable(0)
                    .or(min3)

            test("success: 3") {
                val logs = mutableListOf<LogEntry>()
                val config = ValidationConfig(logger = { logs.add(it) })
                val result = nullableMax5OrMin3.tryValidate(3, config = config)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 3
                logs shouldBe listOf(LogEntry.Satisfied(constraintId = "kova.comparable.max", root = "", path = "", input = 3))
            }

            test("success: 2") {
                val logs = mutableListOf<LogEntry>()
                val config = ValidationConfig(logger = { logs.add(it) })
                val result = nullableMax5OrMin3.tryValidate(2, config = config)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 2
                logs shouldBe listOf(LogEntry.Satisfied(constraintId = "kova.comparable.max", root = "", path = "", input = 2))
            }

            test("success: 6") {
                val logs = mutableListOf<LogEntry>()
                val config = ValidationConfig(logger = { logs.add(it) })
                val result = nullableMax5OrMin3.tryValidate(6, config = config)
                result.isSuccess().mustBeTrue(result)
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
                val result = nullableMax5OrMin3.tryValidate(null, config = config)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 0
                logs shouldBe listOf()
            }
        }
    })
