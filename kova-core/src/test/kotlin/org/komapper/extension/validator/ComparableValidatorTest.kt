package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import java.util.Locale

class ComparableValidatorTest :
    FunSpec({

        beforeSpec {
            Locale.setDefault(Locale.US)
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
