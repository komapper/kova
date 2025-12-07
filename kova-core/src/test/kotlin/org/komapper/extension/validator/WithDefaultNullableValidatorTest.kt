package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class WithDefaultNullableValidatorTest :
    FunSpec({

        context("nullable") {
            val nullable = Kova.nullable(0)

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

        context("nullable - default value with lambda") {
            val nullable = Kova.nullable { 0 }

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
            val isNull = Kova.nullable(0).isNull()

            test("success") {
                val result = isNull.tryValidate(null)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 0
            }

            test("failure") {
                val result = isNull.tryValidate(4)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].text shouldBe "must be null"
            }

            test("failure - min constraint violated") {
                val result = isNull.tryValidate(2)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].text shouldBe "must be null"
            }
        }

        context("isNull or nullable") {
            val isNull = Kova.nullable(0).isNull()
            val isNullOrMin3Max3 =
                isNull.or(
                    Kova
                        .int()
                        .min(3)
                        .max(3)
                        .asNullable(0),
                )

            test("success - null") {
                val result = isNullOrMin3Max3.tryValidate(null)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 0
            }

            test("success - 3") {
                val result = isNullOrMin3Max3.tryValidate(3)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 3
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

        context("isNull or nonNullable") {
            val min3 = Kova.int().min(3)
            val max3 = Kova.int().max(3)
            val isNullOrMin3Max3 = Kova.nullable(0).isNull().or((min3 and max3).asNullable(0))

            test("success - null") {
                val result = isNullOrMin3Max3.tryValidate(null)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 0
            }

            test("success - non-null") {
                val result = isNullOrMin3Max3.tryValidate(3)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 3
            }

            test("failure - isNull and max3 constraints violated") {
                val result = isNullOrMin3Max3.tryValidate(5)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.or"
            }
        }

        context("isNull or then") {
            val min3 = Kova.int().min(3)
            val min5 = Kova.int().min(5)
            val max4 = Kova.int().max(4)
            val isNullOrMin3OrMin5AndThenMax4 =
                Kova
                    .int()
                    .asNullable(0)
                    .isNull()
                    .or((min3 or min5).asNullable(0))
                    .then(max4)

            test("success - isNull constraint satisfied") {
                val result = isNullOrMin3OrMin5AndThenMax4.tryValidate(null)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 0
            }

            test("success - min3 constraint satisfied") {
                val result = isNullOrMin3OrMin5AndThenMax4.tryValidate(3)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 3
            }

            test("success - max4 constraint failed") {
                val result = isNullOrMin3OrMin5AndThenMax4.tryValidate(5)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].text shouldBe "must be less than or equal to 4"
            }

            test("failure - all constraints violated") {
                val result = isNullOrMin3OrMin5AndThenMax4.tryValidate(2)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.or"
            }
        }

        context("and") {
            val min3 = Kova.int().min(3)
            val whenNotNullMin3 = Kova.nullable(3).and(min3.asNullable(0))

            test("success - non-null") {
                val result = whenNotNullMin3.tryValidate(4)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 4
            }

            test("success - null") {
                val logs = mutableListOf<String>()
                val result = whenNotNullMin3.tryValidate(null, config = ValidationConfig(logger = { logs.add(it) }))
                result.isSuccess().mustBeTrue(result)
                result.value shouldBe 0
                logs shouldBe listOf()
            }

            test("failure - min 3constraint violated") {
                val result = whenNotNullMin3.tryValidate(2)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].text shouldBe "must be greater than or equal to 3"
            }
        }

        context("and - each List element") {
            val min3 = Kova.int().min(3)
            val nullableMin3 = Kova.nullable(0).and(min3.asNullable(0))
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
            val nullableMin3 = min3.asNullable(0).toNonNullable()

            test("success - non-null") {
                val result = nullableMin3.tryValidate(4)
                result.isSuccess().mustBeTrue()
                val value: Int = result.value // The type is "Int" instead of "Int?"
                value shouldBe 4
            }

            test("success - null") {
                val result = nullableMin3.tryValidate(null)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 0
            }

            test("failure - min3 constraint is violated") {
                val result = nullableMin3.tryValidate(2)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].text shouldBe "must be greater than or equal to 3"
            }
        }

        context("toNonNullable - then") {
            val max5 = Kova.int().max(5)
            val min3 = Kova.int().min(3)
            val notNullAndMin3AndMax3 = Kova.nullable(4).toNonNullable().then(min3 and max5)

            test("success") {
                val result = notNullAndMin3AndMax3.tryValidate(4)
                result.isSuccess().mustBeTrue()
            }

            test("success - null") {
                val result = notNullAndMin3AndMax3.tryValidate(null)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 4
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
            val min3 = Kova.int().min(3)
            val isNullOrMin3Max3 = Kova.nullable(0).isNull().or(min3.asNullable(0))

            test("success: 3") {
                val logs = mutableListOf<String>()
                val config = ValidationConfig(logger = { logs.add(it) })
                val result = isNullOrMin3Max3.tryValidate(3, config = config)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 3
                logs shouldBe
                    listOf(
                        "Violated(constraintId=kova.nullable.isNull, root=, path=, input=3)",
                        "Satisfied(constraintId=kova.comparable.min, root=, path=, input=3)",
                    )
            }

            test("success: null") {
                val logs = mutableListOf<String>()
                val config = ValidationConfig(logger = { logs.add(it) })
                val result = isNullOrMin3Max3.tryValidate(null, config = config)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 0
                logs shouldBe
                    listOf(
                        "Satisfied(constraintId=kova.nullable.isNull, root=, path=, input=null)",
                    )
            }
        }
    })
