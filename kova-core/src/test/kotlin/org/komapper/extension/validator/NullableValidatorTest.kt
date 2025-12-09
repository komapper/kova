package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class NullableValidatorTest :
    FunSpec({

        context("nullable") {
            val nullable = Kova.nullable<Int>()

            test("success - null") {
                val result = nullable.tryValidate(null)
                result.isSuccess().mustBeTrue()
                result.value shouldBe null
            }

            test("success - non null") {
                val result = nullable.tryValidate(123)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 123
            }
        }

        context("withDefault - literal") {
            val nullable = Kova.nullable<Int>().withDefault(0)

            test("success - null") {
                val result = nullable.tryValidate(null)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 0
            }

            test("success - non null") {
                val result = nullable.tryValidate(123)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 123
            }
        }

        context("withDefault - lambda") {
            val nullable = Kova.nullable<Int>().withDefault { 0 }

            test("success - null") {
                val result = nullable.tryValidate(null)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 0
            }

            test("success - non null") {
                val result = nullable.tryValidate(123)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 123
            }
        }

        context("isNull") {
            val isNull = Kova.nullable<Int>().isNull()

            test("success") {
                val result = isNull.tryValidate(null)
                result.isSuccess().mustBeTrue()
            }

            test("failure") {
                val result = isNull.tryValidate(4)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].text shouldBe "must be null"
            }
        }

        context("or") {
            val isNull = Kova.nullable<Int>().isNull()
            val isNullOrMin3Max3 =
                isNull.or(
                    Kova
                        .int()
                        .min(3)
                        .max(3),
                )

            test("success - null") {
                val result = isNullOrMin3Max3.tryValidate(null)
                result.isSuccess().mustBeTrue()
            }

            test("success - 3") {
                val result = isNullOrMin3Max3.tryValidate(3)
                result.isSuccess().mustBeTrue()
            }

            test("failure") {
                val result = isNullOrMin3Max3.tryValidate(5)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].let {
                    it.constraintId shouldBe "kova.or"
                    it.text shouldBe
                        "at least one constraint must be satisfied: [[must be null], [must be less than or equal to 3]]"
                }
            }
        }

        context("isNullOr") {
            val isNullOrMin3Max3 = Kova.nullable<Int>().isNullOr({ it.min(3).max(3) })

            test("success - null") {
                val result = isNullOrMin3Max3.tryValidate(null)
                result.isSuccess().mustBeTrue()
            }

            test("success - non-null") {
                val result = isNullOrMin3Max3.tryValidate(3)
                result.isSuccess().mustBeTrue()
            }

            test("failure - isNull and max3 constraints violated") {
                val result = isNullOrMin3Max3.tryValidate(5)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.or"
            }
        }

        context("notNull") {
            val notNull = Kova.nullable<Int>().notNull()

            test("success") {
                val result = notNull.tryValidate(4)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 4
            }

            test("failure") {
                val result = notNull.tryValidate(null)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].text shouldBe "must not be null"
            }
        }

        context("notNullAnd") {
            val notNullAndMin3 = Kova.nullable<Int>().notNullAnd({ it.min(3) })

            test("success") {
                val result = notNullAndMin3.tryValidate(4)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 4
            }

            test("failure") {
                val result = notNullAndMin3.tryValidate(null)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].text shouldBe "must not be null"
            }

            test("failure - min constraint violated") {
                val result = notNullAndMin3.tryValidate(2)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].text shouldBe "must be greater than or equal to 3"
            }
        }

        context("and") {
            val min3 = Kova.int().min(3)
            val nullableMin3 = Kova.nullable<Int>().and(min3)

            test("success - non-null") {
                val result = nullableMin3.tryValidate(4)
                result.isSuccess().mustBeTrue()
            }

            test("success - null") {
                val result = nullableMin3.tryValidate(null)
                result.isSuccess().mustBeTrue()
            }

            test("failure - min3 constraint violated") {
                val result = nullableMin3.tryValidate(2)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].text shouldBe "must be greater than or equal to 3"
            }
        }

        context("and - each List element") {
            val min3 = Kova.int().min(3)
            val nullableMin3 = Kova.nullable<Int>().and(min3)
            val onEachNullableMin3 = Kova.list<Int?>().onEach(nullableMin3)

            test("success - non-null") {
                val result = onEachNullableMin3.tryValidate(listOf(4, 5))
                result.isSuccess().mustBeTrue()
            }

            test("success - null") {
                val result = onEachNullableMin3.tryValidate(listOf(null, null))
                result.isSuccess().mustBeTrue()
            }

            test("failure - min3　constraint violated") {
                val result = onEachNullableMin3.tryValidate(listOf(2, null))
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].text shouldBe
                    "Some elements do not satisfy the constraint: [must be greater than or equal to 3]"
            }
        }

        context("toNonNullable") {
            val min3 = Kova.int().min(3)
            val nullableMin3 = min3.asNullable().toNonNullable()

            test("success - non-null") {
                val result = nullableMin3.tryValidate(4)
                result.isSuccess().mustBeTrue()
                val value: Int = result.value // The type is "Int" instead of "Int?"
                value shouldBe 4
            }

            test("failure - null") {
                val result = nullableMin3.tryValidate(null)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].text shouldBe "must not be null"
            }

            test("failure - min3 constraint is violated") {
                val result = nullableMin3.tryValidate(2)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].text shouldBe "must be greater than or equal to 3"
            }
        }

        context("toNonNullable - then") {
            val notNullAndMin3AndMax3 = Kova.nullable<Int>().toNonNullable().then { it.max(5).min(3) }

            test("success") {
                val result = notNullAndMin3AndMax3.tryValidate(4)
                result.isSuccess().mustBeTrue()
            }

            test("failure - notNull constraint is violated") {
                val result = notNullAndMin3AndMax3.tryValidate(null)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].text shouldBe "must not be null"
            }

            test("failure - min3 constraint is violated") {
                val result = notNullAndMin3AndMax3.tryValidate(2)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].text shouldBe "must be greater than or equal to 3"
            }

            test("failure - max5 constraint violated") {
                val result = notNullAndMin3AndMax3.tryValidate(6)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].text shouldBe "must be less than or equal to 5"
            }
        }

        context("logs") {
            val isNullOrMin3Max3 = Kova.nullable<Int>().isNullOr({ it.min(3) })

            test("success: 3") {
                val logs = mutableListOf<LogEntry>()
                val config = ValidationConfig(logger = { logs.add(it) })
                val result = isNullOrMin3Max3.tryValidate(3, config = config)
                result.isSuccess().mustBeTrue()
                logs shouldBe
                    listOf(
                        LogEntry.Violated(constraintId = "kova.nullable.isNull", root = "", path = "", input = 3),
                        LogEntry.Satisfied(constraintId = "kova.comparable.min", root = "", path = "", input = 3),
                    )
            }

            test("success: null") {
                val logs = mutableListOf<LogEntry>()
                val config = ValidationConfig(logger = { logs.add(it) })
                val result = isNullOrMin3Max3.tryValidate(null, config = config)
                result.isSuccess().mustBeTrue()
                logs shouldBe
                    listOf(
                        LogEntry.Satisfied(
                            constraintId = "kova.nullable.isNull",
                            root = "",
                            path = "",
                            input = null,
                        ),
                    )
            }
        }
    })
