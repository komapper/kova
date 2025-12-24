package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec

class NullableValidatorTest :
    FunSpec({

        context("nullable") {
            test("success with null value") {
                val result = tryValidate { null.success() }
                result.shouldBeSuccess()
                result.value shouldBe null
            }

            test("success with non-null value") {
                val result = tryValidate { 123.success() }
                result.shouldBeSuccess()
                result.value shouldBe 123
            }
        }

        context("withDefault with literal") {
            test("success with null value") {
                val result = tryValidate { null withDefault 0 }
                result.shouldBeSuccess()
                result.value shouldBe 0
            }

            test("success with non-null value") {
                val result = tryValidate { 123 withDefault 0 }
                result.shouldBeSuccess()
                result.value shouldBe 123
            }
        }

        context("withDefault with lambda") {
            test("success with null value") {
                val result = tryValidate { null withDefault { 0 } }
                result.shouldBeSuccess()
                result.value shouldBe 0
            }

            test("success with non-null value") {
                val result = tryValidate { 123 withDefault { 0 } }
                result.shouldBeSuccess()
                result.value shouldBe 123
            }
        }

        context("withDefaultThen with literal") {
            test("success with null value") {
                val result = tryValidate { null withDefault 10 alsoThen { it.min(3) } }
                result.shouldBeSuccess()
                result.value shouldBe 10
            }

            test("success with non-null value") {
                val result = tryValidate { 123 withDefault 10 alsoThen { it.min(3) } }
                result.shouldBeSuccess()
                result.value shouldBe 123
            }

            test("failure with non-null value") {
                val result = tryValidate { 1 withDefault 10 alsoThen { it.min(3) } }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.comparable.min"
            }
        }

        context("withDefaultThen with lambda") {
            test("success with null value") {
                val result = tryValidate { null withDefault { 10 } alsoThen { it.min(3) } }
                result.shouldBeSuccess()
                result.value shouldBe 10
            }

            test("success with non-null value") {
                val result = tryValidate { 123 withDefault { 10 } alsoThen { it.min(3) } }
                result.shouldBeSuccess()
                result.value shouldBe 123
            }

            test("failure with non-null value") {
                val result = tryValidate { 1 withDefault { 10 } alsoThen { it.min(3) } }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.comparable.min"
            }
        }

        context("isNull") {
            test("success") {
                val result = tryValidate { null.isNull() }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { 4.isNull() }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.nullable.isNull"
            }
        }

        context("or") {
            context(_: Validation)
            fun Int?.isNullOrMin3Max3() = or { isNull() } orElse { this?.min(3)?.and { max(3) }.orSucceed() }

            test("success with null value") {
                val result = tryValidate { null.isNullOrMin3Max3() }
                result.shouldBeSuccess()
            }

            test("success with value 3") {
                val result = tryValidate { 3.isNullOrMin3Max3() }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { 5.isNullOrMin3Max3() }
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
            context(_: Validation)
            fun Int?.isNullOrMin3Max3() = isNullOr { it.min(3) and { it.max(3) } }

            test("success with null value") {
                val result = tryValidate { null.isNullOrMin3Max3() }
                result.shouldBeSuccess()
            }

            test("success with non-null value") {
                val result = tryValidate { 3.isNullOrMin3Max3() }
                result.shouldBeSuccess()
            }

            test("failure when isNull and max3 constraints violated") {
                val result = tryValidate { 5.isNullOrMin3Max3() }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.or"
            }
        }

        context("notNull") {
            test("success") {
                val result = tryValidate { 4.notNull() }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { null.notNull() }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.nullable.notNull"
            }
        }

        context("notNullAnd") {
            test("success") {
                val result = tryValidate { 4.notNullAnd { it.min(3) } }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { null.notNullAnd<Int?> { it.min(3) } }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.nullable.notNull"
            }

            test("failure when min constraint violated") {
                val result = tryValidate { 2.notNullAnd { it.min(3) } }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.comparable.min"
            }
        }

        context("and") {
            context(_: Validation)
            fun Int?.nullableMin3() = this?.min(3).orSucceed()

            test("success with non-null value") {
                val result = tryValidate { 4.nullableMin3() }
                result.shouldBeSuccess()
            }

            test("success with null value") {
                val result = tryValidate { null.nullableMin3() }
                result.shouldBeSuccess()
            }

            test("failure when min3 constraint violated") {
                val result = tryValidate { 2.nullableMin3() }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.comparable.min"
            }
        }

        context("and - each List element") {
            context(_: Validation)
            fun Int?.nullableMin3() = this?.min(3).orSucceed()

            test("success with non-null value") {
                val result = tryValidate { listOf(4, 5).onEach { it.nullableMin3() } }
                result.shouldBeSuccess()
            }

            test("success with null value") {
                val result = tryValidate { listOf(null, null).onEach { it.nullableMin3() } }
                result.shouldBeSuccess()
            }

            test("failure when min3 constraint violated") {
                val result = tryValidate { listOf(2, null).onEach { it.nullableMin3() } }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.collection.onEach"
            }
        }

        context("toNonNullable") {
            context(_: Validation)
            fun Int?.nullableMin3() = this?.min(3).orSucceed() then { toNonNullable() }

            test("success with non-null value") {
                val result = tryValidate { 4.nullableMin3() }
                result.shouldBeSuccess()
                val value: Int = result.value // The type is "Int" instead of "Int?"
                value shouldBe 4
            }

            test("failure with null value") {
                val result = tryValidate { null.nullableMin3() }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.nullable.notNull"
            }

            test("failure when min3 constraint is violated") {
                val result = tryValidate { 2.nullableMin3() }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.comparable.min"
            }
        }

        context("toNonNullable - then") {
            context(_: Validation)
            fun Int?.notNullAndMin3AndMax3() = toNonNullable().alsoThen { it.max(5) }.alsoThen { it.min(3) }

            test("success") {
                val result = tryValidate { 4.notNullAndMin3AndMax3() }
                result.shouldBeSuccess()
            }

            test("failure when notNull constraint is violated") {
                val result = tryValidate { null.notNullAndMin3AndMax3() }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.nullable.notNull"
            }

            test("failure when min3 constraint is violated") {
                val result = tryValidate { 2.notNullAndMin3AndMax3() }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.comparable.min"
            }

            test("failure when max5 constraint violated") {
                val result = tryValidate { 6.notNullAndMin3AndMax3() }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.comparable.max"
            }
        }

        context("logs") {
            test("success: 3") {
                val logs = mutableListOf<LogEntry>()
                val config = ValidationConfig(logger = { logs.add(it) })
                val result = tryValidate(config) { 3.isNullOr { it.min(3) } }
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
                    val result = tryValidate(config) { null.isNullOr<Int?> { it.min(3) } }
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
