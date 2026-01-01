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

        context("gtValue (greater than)") {
            test("success with value greater than threshold") {
                val result = tryValidate { gtValue(6, 5) }
                result.shouldBeSuccess()
            }

            test("success with large value") {
                val result = tryValidate { gtValue(100, 5) }
                result.shouldBeSuccess()
            }

            test("failure with equal value") {
                val result = tryValidate { gtValue(5, 5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.gtValue"
            }

            test("failure with value less than threshold") {
                val result = tryValidate { gtValue(4, 5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.gtValue"
            }
        }

        context("gtValue with double") {
            test("success with value greater than threshold") {
                val result = tryValidate { gtValue(5.6, 5.5) }
                result.shouldBeSuccess()
            }

            test("failure with equal value") {
                val result = tryValidate { gtValue(5.5, 5.5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.gtValue"
            }

            test("failure with smaller value") {
                val result = tryValidate { gtValue(5.4, 5.5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.gtValue"
            }
        }

        context("gtEqValue (greater than or equal)") {
            test("success with value greater than threshold") {
                val result = tryValidate { gtEqValue(6, 5) }
                result.shouldBeSuccess()
            }

            test("success with equal value") {
                val result = tryValidate { gtEqValue(5, 5) }
                result.shouldBeSuccess()
            }

            test("failure with value less than threshold") {
                val result = tryValidate { gtEqValue(4, 5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.gtEqValue"
            }
        }

        context("gtEqValue with double") {
            test("success with greater value") {
                val result = tryValidate { gtEqValue(5.6, 5.5) }
                result.shouldBeSuccess()
            }

            test("success with equal value") {
                val result = tryValidate { gtEqValue(5.5, 5.5) }
                result.shouldBeSuccess()
            }

            test("failure with value less than threshold") {
                val result = tryValidate { gtEqValue(5.4, 5.5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.gtEqValue"
            }
        }

        context("ltValue (less than)") {
            test("success with value less than threshold") {
                val result = tryValidate { ltValue(4, 5) }
                result.shouldBeSuccess()
            }

            test("success with large negative value") {
                val result = tryValidate { ltValue(-100, 5) }
                result.shouldBeSuccess()
            }

            test("failure with equal value") {
                val result = tryValidate { ltValue(5, 5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.ltValue"
            }

            test("failure with value greater than threshold") {
                val result = tryValidate { ltValue(6, 5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.ltValue"
            }
        }

        context("ltValue with double") {
            test("success with value less than threshold") {
                val result = tryValidate { ltValue(5.4, 5.5) }
                result.shouldBeSuccess()
            }

            test("failure with equal value") {
                val result = tryValidate { ltValue(5.5, 5.5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.ltValue"
            }

            test("failure with greater value") {
                val result = tryValidate { ltValue(5.6, 5.5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.ltValue"
            }
        }

        context("ltEqValue (less than or equal)") {
            test("success with value less than threshold") {
                val result = tryValidate { ltEqValue(4, 5) }
                result.shouldBeSuccess()
            }

            test("success with equal value") {
                val result = tryValidate { ltEqValue(5, 5) }
                result.shouldBeSuccess()
            }

            test("failure with value greater than threshold") {
                val result = tryValidate { ltEqValue(6, 5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.ltEqValue"
            }
        }

        context("ltEqValue with double") {
            test("success with smaller value") {
                val result = tryValidate { ltEqValue(5.4, 5.5) }
                result.shouldBeSuccess()
            }

            test("success with equal value") {
                val result = tryValidate { ltEqValue(5.5, 5.5) }
                result.shouldBeSuccess()
            }

            test("failure with value greater than threshold") {
                val result = tryValidate { ltEqValue(5.6, 5.5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.ltEqValue"
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

        context("gtValue with bigDecimal") {
            test("success with value greater than threshold") {
                val result = tryValidate { gtValue(150.toBigDecimal(), 100.toBigDecimal()) }
                result.shouldBeSuccess()
            }

            test("failure with equal value") {
                val result = tryValidate { gtValue(100.toBigDecimal(), 100.toBigDecimal()) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.gtValue"
            }

            test("failure with smaller value") {
                val result = tryValidate { gtValue(50.toBigDecimal(), 100.toBigDecimal()) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.gtValue"
            }
        }

        context("ltValue with bigInteger") {
            test("success with value less than threshold") {
                val result = tryValidate { ltValue(50.toBigInteger(), 100.toBigInteger()) }
                result.shouldBeSuccess()
            }

            test("failure with equal value") {
                val result = tryValidate { ltValue(100.toBigInteger(), 100.toBigInteger()) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.ltValue"
            }

            test("failure with greater value") {
                val result = tryValidate { ltValue(150.toBigInteger(), 100.toBigInteger()) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.ltValue"
            }
        }
    })
