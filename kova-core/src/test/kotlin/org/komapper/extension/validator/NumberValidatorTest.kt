package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import java.util.Locale

class NumberValidatorTest :
    FunSpec({

        beforeSpec {
            Locale.setDefault(Locale.US)
        }

        context("positive") {
            test("success with positive number") {
                val result = tryValidate { positive(1) }
                result.shouldBeSuccess()
            }

            test("success with large positive number") {
                val result = tryValidate { positive(100) }
                result.shouldBeSuccess()
            }

            test("failure with zero") {
                val result = tryValidate { positive(0) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.positive"
            }

            test("failure with negative number") {
                val result = tryValidate { positive(-1) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.positive"
            }
        }

        context("positive with double") {
            test("success with positive number") {
                val result = tryValidate { positive(0.1) }
                result.shouldBeSuccess()
            }

            test("failure with negative number") {
                val result = tryValidate { positive(-0.1) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.positive"
            }
        }

        context("negative") {
            test("success with negative number") {
                val result = tryValidate { negative(-1) }
                result.shouldBeSuccess()
            }

            test("success with large negative number") {
                val result = tryValidate { negative(-100) }
                result.shouldBeSuccess()
            }

            test("failure with zero") {
                val result = tryValidate { negative(0) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.negative"
            }

            test("failure with positive number") {
                val result = tryValidate { negative(1) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.negative"
            }
        }

        context("negative with double") {
            test("success with negative number") {
                val result = tryValidate { negative(-0.1) }
                result.shouldBeSuccess()
            }

            test("failure with positive number") {
                val result = tryValidate { negative(0.1) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.negative"
            }
        }

        context("notPositive") {
            test("success with zero") {
                val result = tryValidate { notPositive(0) }
                result.shouldBeSuccess()
            }

            test("success with negative number") {
                val result = tryValidate { notPositive(-1) }
                result.shouldBeSuccess()
            }

            test("success with large negative number") {
                val result = tryValidate { notPositive(-100) }
                result.shouldBeSuccess()
            }

            test("failure with positive number") {
                val result = tryValidate { notPositive(1) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.notPositive"
            }
        }

        context("notPositive with double") {
            test("success with zero") {
                val result = tryValidate { notPositive(0.0) }
                result.shouldBeSuccess()
            }

            test("success with negative number") {
                val result = tryValidate { notPositive(-0.1) }
                result.shouldBeSuccess()
            }

            test("failure with positive number") {
                val result = tryValidate { notPositive(0.1) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.notPositive"
            }
        }

        context("gt (greater than)") {
            test("success with value greater than threshold") {
                val result = tryValidate { gt(6, 5) }
                result.shouldBeSuccess()
            }

            test("success with large value") {
                val result = tryValidate { gt(100, 5) }
                result.shouldBeSuccess()
            }

            test("failure with equal value") {
                val result = tryValidate { gt(5, 5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.gt"
            }

            test("failure with value less than threshold") {
                val result = tryValidate { gt(4, 5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.gt"
            }
        }

        context("gt with double") {
            test("success with value greater than threshold") {
                val result = tryValidate { gt(5.6, 5.5) }
                result.shouldBeSuccess()
            }

            test("failure with equal value") {
                val result = tryValidate { gt(5.5, 5.5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.gt"
            }

            test("failure with smaller value") {
                val result = tryValidate { gt(5.4, 5.5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.gt"
            }
        }

        context("gte (greater than or equal)") {
            test("success with value greater than threshold") {
                val result = tryValidate { gte(6, 5) }
                result.shouldBeSuccess()
            }

            test("success with equal value") {
                val result = tryValidate { gte(5, 5) }
                result.shouldBeSuccess()
            }

            test("failure with value less than threshold") {
                val result = tryValidate { gte(4, 5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.gte"
            }
        }

        context("gte with double") {
            test("success with greater value") {
                val result = tryValidate { gte(5.6, 5.5) }
                result.shouldBeSuccess()
            }

            test("success with equal value") {
                val result = tryValidate { gte(5.5, 5.5) }
                result.shouldBeSuccess()
            }

            test("failure with value less than threshold") {
                val result = tryValidate { gte(5.4, 5.5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.gte"
            }
        }

        context("lt (less than)") {
            test("success with value less than threshold") {
                val result = tryValidate { lt(4, 5) }
                result.shouldBeSuccess()
            }

            test("success with large negative value") {
                val result = tryValidate { lt(-100, 5) }
                result.shouldBeSuccess()
            }

            test("failure with equal value") {
                val result = tryValidate { lt(5, 5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.lt"
            }

            test("failure with value greater than threshold") {
                val result = tryValidate { lt(6, 5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.lt"
            }
        }

        context("lt with double") {
            test("success with value less than threshold") {
                val result = tryValidate { lt(5.4, 5.5) }
                result.shouldBeSuccess()
            }

            test("failure with equal value") {
                val result = tryValidate { lt(5.5, 5.5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.lt"
            }

            test("failure with greater value") {
                val result = tryValidate { lt(5.6, 5.5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.lt"
            }
        }

        context("lte (less than or equal)") {
            test("success with value less than threshold") {
                val result = tryValidate { lte(4, 5) }
                result.shouldBeSuccess()
            }

            test("success with equal value") {
                val result = tryValidate { lte(5, 5) }
                result.shouldBeSuccess()
            }

            test("failure with value greater than threshold") {
                val result = tryValidate { lte(6, 5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.lte"
            }
        }

        context("lte with double") {
            test("success with smaller value") {
                val result = tryValidate { lte(5.4, 5.5) }
                result.shouldBeSuccess()
            }

            test("success with equal value") {
                val result = tryValidate { lte(5.5, 5.5) }
                result.shouldBeSuccess()
            }

            test("failure with value greater than threshold") {
                val result = tryValidate { lte(5.6, 5.5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.lte"
            }
        }

        context("positive with float") {
            test("success with positive number") {
                val result = tryValidate { positive(1.5f) }
                result.shouldBeSuccess()
            }

            test("failure with negative number") {
                val result = tryValidate { positive(-1.5f) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.positive"
            }
        }

        context("negative with byte") {
            test("success with negative number") {
                val result = tryValidate { negative(-10) }
                result.shouldBeSuccess()
            }

            test("failure with positive number") {
                val result = tryValidate { negative(10) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.negative"
            }
        }

        context("gt with bigDecimal") {
            test("success with value greater than threshold") {
                val result = tryValidate { gt(150.toBigDecimal(), 100.toBigDecimal()) }
                result.shouldBeSuccess()
            }

            test("failure with equal value") {
                val result = tryValidate { gt(100.toBigDecimal(), 100.toBigDecimal()) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.gt"
            }

            test("failure with smaller value") {
                val result = tryValidate { gt(50.toBigDecimal(), 100.toBigDecimal()) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.gt"
            }
        }

        context("lt with bigInteger") {
            test("success with value less than threshold") {
                val result = tryValidate { lt(50.toBigInteger(), 100.toBigInteger()) }
                result.shouldBeSuccess()
            }

            test("failure with equal value") {
                val result = tryValidate { lt(100.toBigInteger(), 100.toBigInteger()) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.lt"
            }

            test("failure with greater value") {
                val result = tryValidate { lt(150.toBigInteger(), 100.toBigInteger()) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.lt"
            }
        }
    })
