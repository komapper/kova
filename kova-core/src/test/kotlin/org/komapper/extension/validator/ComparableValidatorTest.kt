package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec

class ComparableValidatorTest :
    FunSpec({

        context("plus") {
            context(_: Validation, _: Accumulate)
            fun UInt.validate() {
                max(10u)
                max(20u)
                min(5u)
            }

            test("success") {
                val result = tryValidate { 8u.validate() }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { 15u.validate() }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.comparable.max"
            }
        }

        context("or") {
            context(_: Validation, _: Accumulate)
            fun UInt.validate() {
                or { max(10u) } orElse { max(20u) }
                min(5u)
            }

            test("success : 10") {
                val result = tryValidate { 10u.validate() }
                result.shouldBeSuccess()
            }

            test("success : 20") {
                val result = tryValidate { 20u.validate() }
                result.shouldBeSuccess()
            }

            test("failure : 25") {
                val result = tryValidate { 25u.validate() }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.or"
            }
        }

        context("constrain") {
            context(_: Validation, _: Accumulate)
            fun UInt.validate() = constrain("test") { satisfies(it == 10u) { text("Constraint failed") } }
            test("success") {
                val result = tryValidate { 10u.validate() }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { 20u.validate() }
                result.shouldBeFailure()
                result.messages.single().text shouldBe "Constraint failed"
            }
        }

        context("uInt") {
            context("min") {
                test("success with value greater than threshold") {
                    val result = tryValidate { 6u.min(5u) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { 5u.min(5u) }
                    result.shouldBeSuccess()
                }

                test("failure with value less than threshold") {
                    val result = tryValidate { 4u.min(5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.min"
                }
            }

            context("max") {
                test("success with value less than threshold") {
                    val result = tryValidate { 9u.max(10u) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { 10u.max(10u) }
                    result.shouldBeSuccess()
                }

                test("failure with value greater than threshold") {
                    val result = tryValidate { 11u.max(10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.max"
                }
            }

            context("gt (greater than)") {
                test("success with value greater than threshold") {
                    val result = tryValidate { 6u.gt(5u) }
                    result.shouldBeSuccess()
                }

                test("success with large value") {
                    val result = tryValidate { 100u.gt(5u) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { 5u.gt(5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.gt"
                }

                test("failure with value less than threshold") {
                    val result = tryValidate { 4u.gt(5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.gt"
                }
            }

            context("gte (greater than or equal)") {
                test("success with value greater than threshold") {
                    val result = tryValidate { 6u.gte(5u) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { 5u.gte(5u) }
                    result.shouldBeSuccess()
                }

                test("failure with value less than threshold") {
                    val result = tryValidate { 4u.gte(5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.gte"
                }
            }

            context("lt (less than)") {
                test("success with value less than threshold") {
                    val result = tryValidate { 4u.lt(5u) }
                    result.shouldBeSuccess()
                }

                test("success with zero") {
                    val result = tryValidate { 0u.lt(5u) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { 5u.lt(5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.lt"
                }

                test("failure with value greater than threshold") {
                    val result = tryValidate { 6u.lt(5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.lt"
                }
            }

            context("lte (less than or equal)") {
                test("success with value less than threshold") {
                    val result = tryValidate { 4u.lte(5u) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { 5u.lte(5u) }
                    result.shouldBeSuccess()
                }

                test("failure with value greater than threshold") {
                    val result = tryValidate { 6u.lte(5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.lte"
                }
            }

            context("eq (equal)") {
                test("success with equal value") {
                    val result = tryValidate { 5u.eq(5u) }
                    result.shouldBeSuccess()
                }

                test("failure with value greater than threshold") {
                    val result = tryValidate { 6u.eq(5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.eq"
                }

                test("failure with value less than threshold") {
                    val result = tryValidate { 4u.eq(5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.eq"
                }
            }

            context("notEq (not equal)") {
                test("success with value greater than threshold") {
                    val result = tryValidate { 6u.notEq(5u) }
                    result.shouldBeSuccess()
                }

                test("success with value less than threshold") {
                    val result = tryValidate { 4u.notEq(5u) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { 5u.notEq(5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.notEq"
                }
            }

            context("chaining multiple validators") {
                context(_: Validation, _: Accumulate)
                fun UInt.validate() {
                    min(5u)
                    max(10u)
                    gt(6u)
                    lte(9u)
                }

                test("success with value 7") {
                    val result = tryValidate { 7u.validate() }
                    result.shouldBeSuccess()
                }

                test("success with value 9") {
                    val result = tryValidate { 9u.validate() }
                    result.shouldBeSuccess()
                }

                test("failure with value 5") {
                    val result = tryValidate { 5u.validate() }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.gt"
                }

                test("failure with value 10") {
                    val result = tryValidate { 10u.validate() }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.lte"
                }
            }
        }

        context("uLong") {
            context("min") {
                test("success") {
                    val result = tryValidate { 6uL.min(5uL) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { 4uL.min(5uL) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.min"
                }
            }

            context("max") {
                test("success") {
                    val result = tryValidate { 9uL.max(10uL) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { 11uL.max(10uL) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.max"
                }
            }
        }

        context("uByte") {
            context("min") {
                test("success") {
                    val result = tryValidate { 6u.min(5u) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { 4u.min(5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.min"
                }
            }

            context("max") {
                test("success") {
                    val result = tryValidate { 9u.max(10u) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { 11u.max(10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.max"
                }
            }
        }

        context("uShort") {
            context("min") {
                test("success") {
                    val result = tryValidate { 6u.min(5u) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { 4u.min(5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.min"
                }
            }

            context("max") {
                test("success") {
                    val result = tryValidate { 9u.max(10u) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { 11u.max(10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.max"
                }
            }
        }
    })
