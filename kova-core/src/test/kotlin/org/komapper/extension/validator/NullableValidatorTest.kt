package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import java.util.Locale

class NullableValidatorTest :
    FunSpec({

        beforeSpec {
            Locale.setDefault(Locale.US)
        }

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
            context(_: Validation)
            fun nullableMin3(i: Int?) {
                if (i != null) i.ensureMin(3)
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
            context(_: Validation)
            fun nullableMin3(i: Int?) {
                if (i != null) i.ensureMin(3)
            }

            test("success with non-null value") {
                val result = tryValidate { listOf(4, 5).ensureEach { nullableMin3(it) } }
                result.shouldBeSuccess()
            }

            test("success with null value") {
                val result = tryValidate { listOf(null, null).ensureEach { nullableMin3(it) } }
                result.shouldBeSuccess()
            }

            test("failure when min3 constraint violated") {
                val result = tryValidate { listOf(2, null).ensureEach { nullableMin3(it) } }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.iterable.each"
            }
        }

        context("ensureNull") {
            test("success") {
                val result = tryValidate { (null as Int?).ensureNull() }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { (4 as Int?).ensureNull() }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.nullable.null"
            }
        }

        context("or ensureNull orElse") {
            context(_: Validation)
            fun isNullOrMin3Max3(i: Int?) =
                or { i.ensureNull() } orElse {
                    if (i == null) return@orElse
                    i.ensureMin(3)
                    i.ensureMax(3)
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

        context("ensureNullOr") {
            context(_: Validation)
            fun isNullOrMin3Max3(i: Int?) =
                i.ensureNullOr {
                    it.ensureMin(3)
                    it.ensureMax(3)
                }

            test("success with null value") {
                val result = tryValidate { isNullOrMin3Max3(null) }
                result.shouldBeSuccess()
            }

            test("success with non-null value") {
                val result = tryValidate { isNullOrMin3Max3(3) }
                result.shouldBeSuccess()
            }

            test("failure when ensureNull and max3 constraints violated") {
                val result = tryValidate { isNullOrMin3Max3(5) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.or"
            }
        }

        context("ensureNotNull") {
            test("success") {
                val result = tryValidate { (4 as Int?).ensureNotNull() }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { (null as Int?).ensureNotNull() }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.nullable.notNull"
            }
        }

        context("ensureNotNull and other constraints") {
            context(_: Validation)
            fun notNullAndMin3AndMax3(i: Int?): Int {
                i.ensureNotNull()
                i.ensureMax(5)
                i.ensureMin(3)
                return i
            }

            test("success") {
                val result = tryValidate { notNullAndMin3AndMax3(4) }
                result.shouldBeSuccess()
            }

            test("failure when ensureNotNull constraint is violated") {
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
                val result = tryValidate(config) { 3.ensureNullOr { it.ensureMin(3) } }
                result.shouldBeSuccess()
                logs shouldBe
                    listOf(
                        LogEntry.Violated(constraintId = "kova.nullable.null", root = "", path = "", input = 3, args = listOf()),
                        LogEntry.Satisfied(constraintId = "kova.comparable.min", root = "", path = "", input = 3),
                    )
            }

            test("success: null") {
                buildList {
                    val config = ValidationConfig(logger = { add(it) })
                    val result = tryValidate(config) { null.ensureNullOr<Int?> { it.ensureMin(3) } }
                    result.shouldBeSuccess()
                } shouldBe
                    listOf(
                        LogEntry.Satisfied(
                            constraintId = "kova.nullable.null",
                            root = "",
                            path = "",
                            input = null,
                        ),
                    )
            }
        }
    })
