package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec

class ComparableValidatorTest :
    FunSpec({

        context("plus") {
            fun Validation.validate(i: UInt) {
                max(i, 10u)
                max(i, 20u)
                min(i, 5u)
            }

            test("success") {
                val result = tryValidate { validate(8u) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { validate(15u) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.comparable.max"
            }
        }

        context("or") {
            fun Validation.validate(i: UInt) {
                val _ = or { max(i, 10u) } orElse { max(i, 20u) }
                min(i, 5u)
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

        context("constrain") {
            @IgnorableReturnValue
            fun Validation.validate(i: UInt) = i.constrain("test") { satisfies(it == 10u) { text("Constraint failed") } }
            test("success") {
                val result = tryValidate { validate(10u) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { validate(20u) }
                result.shouldBeFailure()
                result.messages.single().text shouldBe "Constraint failed"
            }
        }

        context("uInt") {
            context("min") {
                test("success with value greater than threshold") {
                    val result = tryValidate { min(6u, 5u) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { min(5u, 5u) }
                    result.shouldBeSuccess()
                }

                test("failure with value less than threshold") {
                    val result = tryValidate { min(4u, 5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.min"
                }
            }

            context("max") {
                test("success with value less than threshold") {
                    val result = tryValidate { max(9u, 10u) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { max(10u, 10u) }
                    result.shouldBeSuccess()
                }

                test("failure with value greater than threshold") {
                    val result = tryValidate { max(11u, 10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.max"
                }
            }

            context("gt (greater than)") {
                test("success with value greater than threshold") {
                    val result = tryValidate { gt(6u, 5u) }
                    result.shouldBeSuccess()
                }

                test("success with large value") {
                    val result = tryValidate { gt(100u, 5u) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { gt(5u, 5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.gt"
                }

                test("failure with value less than threshold") {
                    val result = tryValidate { gt(4u, 5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.gt"
                }
            }

            context("gte (greater than or equal)") {
                test("success with value greater than threshold") {
                    val result = tryValidate { gte(6u, 5u) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { gte(5u, 5u) }
                    result.shouldBeSuccess()
                }

                test("failure with value less than threshold") {
                    val result = tryValidate { gte(4u, 5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.gte"
                }
            }

            context("lt (less than)") {
                test("success with value less than threshold") {
                    val result = tryValidate { lt(4u, 5u) }
                    result.shouldBeSuccess()
                }

                test("success with zero") {
                    val result = tryValidate { lt(0u, 5u) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { lt(5u, 5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.lt"
                }

                test("failure with value greater than threshold") {
                    val result = tryValidate { lt(6u, 5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.lt"
                }
            }

            context("lte (less than or equal)") {
                test("success with value less than threshold") {
                    val result = tryValidate { lte(4u, 5u) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { lte(5u, 5u) }
                    result.shouldBeSuccess()
                }

                test("failure with value greater than threshold") {
                    val result = tryValidate { lte(6u, 5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.lte"
                }
            }

            context("eq (equal)") {
                test("success with equal value") {
                    val result = tryValidate { eq(5u, 5u) }
                    result.shouldBeSuccess()
                }

                test("failure with value greater than threshold") {
                    val result = tryValidate { eq(6u, 5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.eq"
                }

                test("failure with value less than threshold") {
                    val result = tryValidate { eq(4u, 5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.eq"
                }
            }

            context("notEq (not equal)") {
                test("success with value greater than threshold") {
                    val result = tryValidate { notEq(6u, 5u) }
                    result.shouldBeSuccess()
                }

                test("success with value less than threshold") {
                    val result = tryValidate { notEq(4u, 5u) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { notEq(5u, 5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.notEq"
                }
            }

            context("chaining multiple validators") {
                fun Validation.validate(i: UInt) {
                    min(i, 5u)
                    max(i, 10u)
                    gt(i, 6u)
                    lte(i, 9u)
                }

                test("success with value 7") {
                    val result = tryValidate { validate(7u) }
                    result.shouldBeSuccess()
                }

                test("success with value 9") {
                    val result = tryValidate { validate(9u) }
                    result.shouldBeSuccess()
                }

                test("failure with value 5") {
                    val result = tryValidate { validate(5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.gt"
                }

                test("failure with value 10") {
                    val result = tryValidate { validate(10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.lte"
                }
            }
        }

        context("uLong") {
            context("min") {
                test("success") {
                    val result = tryValidate { min(6uL, 5uL) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { min(4uL, 5uL) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.min"
                }
            }

            context("max") {
                test("success") {
                    val result = tryValidate { max(9uL, 10uL) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { max(11uL, 10uL) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.max"
                }
            }
        }

        context("uByte") {
            context("min") {
                test("success") {
                    val result = tryValidate { min(6u, 5u) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { min(4u, 5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.min"
                }
            }

            context("max") {
                test("success") {
                    val result = tryValidate { max(9u, 10u) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { max(11u, 10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.max"
                }
            }
        }

        context("uShort") {
            context("min") {
                test("success") {
                    val result = tryValidate { min(6u, 5u) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { min(4u, 5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.min"
                }
            }

            context("max") {
                test("success") {
                    val result = tryValidate { max(9u, 10u) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { max(11u, 10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.max"
                }
            }
        }
    })
