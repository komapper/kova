package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec

class NullableValidatorTest :
    FunSpec({

        context("nullable") {
            test("success with null value") {
                val result = tryValidate { null }
                result.shouldBeSuccess()
                result.value shouldBe null
            }

            test("success with non-null value") {
                val result = tryValidate { 123 }
                result.shouldBeSuccess()
                result.value shouldBe 123
            }
        }

        context("nullable with constraint") {
            fun Validation.nullableMin3(i: Int?) {
                if (i != null) min(i, 3)
            }

            test("success with non-null value") {
                val result = tryValidate { nullableMin3(4) }
                result.shouldBeSuccess()
            }

            test("success with null value") {
                val result = tryValidate { nullableMin3(null) }
                result.shouldBeSuccess()
            }

            test("failure when min3 constraint violated") {
                val result = tryValidate { nullableMin3(2) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.comparable.min"
            }
        }

        context("nullable with constraint - for each List element") {
            fun Validation.nullableMin3(i: Int?) {
                if (i != null) min(i, 3)
            }

            test("success with non-null value") {
                val result = tryValidate { onEach(listOf(4, 5)) { nullableMin3(it) } }
                result.shouldBeSuccess()
            }

            test("success with null value") {
                val result = tryValidate { onEach(listOf(null, null)) { nullableMin3(it) } }
                result.shouldBeSuccess()
            }

            test("failure when min3 constraint violated") {
                val result = tryValidate { onEach(listOf(2, null)) { nullableMin3(it) } }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.collection.onEach"
            }
        }

        context("isNull") {
            test("success") {
                val result = tryValidate { isNull(null) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { isNull(4) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.nullable.isNull"
            }
        }

        context("or isNull orElse") {
            fun Validation.isNullOrMin3Max3(i: Int?) =
                or { isNull(i) } orElse {
                    if (i == null) return@orElse
                    min(i, 3)
                    max(i, 3)
                }

            test("success with null value") {
                val result = tryValidate { isNullOrMin3Max3(null) }
                result.shouldBeSuccess()
            }

            test("success with value 3") {
                val result = tryValidate { isNullOrMin3Max3(3) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { isNullOrMin3Max3(5) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].let {
                    it.constraintId shouldBe "kova.or"
                    it.text shouldBe
                        "at least one constraint must be satisfied: [[must be null], [must be less than or equal to 3]]"
                }
            }
        }

        context("isNullOr") {
            fun Validation.isNullOrMin3Max3(i: Int?) =
                isNullOr(i) {
                    min(it, 3)
                    max(it, 3)
                }

            test("success with null value") {
                val result = tryValidate { isNullOrMin3Max3(null) }
                result.shouldBeSuccess()
            }

            test("success with non-null value") {
                val result = tryValidate { isNullOrMin3Max3(3) }
                result.shouldBeSuccess()
            }

            test("failure when isNull and max3 constraints violated") {
                val result = tryValidate { isNullOrMin3Max3(5) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.or"
            }
        }

        context("notNull") {
            test("success") {
                val result = tryValidate { notNull(4) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { notNull(null) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.nullable.notNull"
            }
        }

        context("toNonNullable") {
            fun Validation.nullableMin3(i: Int?): Int {
                if (i != null) min(i, 3)
                return toNonNullable(i)
            }

            test("success with non-null value") {
                val result = tryValidate { nullableMin3(4) }
                result.shouldBeSuccess()
                val value: Int = result.value // The type is "Int" instead of "Int?"
                value shouldBe 4
            }

            test("failure with null value") {
                val result = tryValidate { nullableMin3(null) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.nullable.notNull"
            }

            test("failure when min3 constraint is violated") {
                val result = tryValidate { nullableMin3(2) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.comparable.min"
            }
        }

        context("toNonNullable - also") {
            fun Validation.notNullAndMin3AndMax3(i: Int?) = toNonNullable(i).also { max(it, 5) }.also { min(it, 3) }

            test("success") {
                val result = tryValidate { notNullAndMin3AndMax3(4) }
                result.shouldBeSuccess()
            }

            test("failure when notNull constraint is violated") {
                val result = tryValidate { notNullAndMin3AndMax3(null) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.nullable.notNull"
            }

            test("failure when min3 constraint is violated") {
                val result = tryValidate { notNullAndMin3AndMax3(2) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.comparable.min"
            }

            test("failure when max5 constraint violated") {
                val result = tryValidate { notNullAndMin3AndMax3(6) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.comparable.max"
            }
        }

        context("logs") {
            test("success: 3") {
                val logs = mutableListOf<LogEntry>()
                val config = ValidationConfig(logger = { logs.add(it) })
                val result = tryValidate(config) { isNullOr(3) { min(it, 3) } }
                result.shouldBeSuccess()
                logs shouldBe
                    listOf(
                        LogEntry.Violated(constraintId = "kova.nullable.isNull", root = "", path = "", input = 3, args = listOf()),
                        LogEntry.Satisfied(constraintId = "kova.comparable.min", root = "", path = "", input = 3),
                    )
            }

            test("success: null") {
                buildList {
                    val config = ValidationConfig(logger = { add(it) })
                    val result = tryValidate(config) { isNullOr<Int?>(null) { min(it, 3) } }
                    result.shouldBeSuccess()
                } shouldBe
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
