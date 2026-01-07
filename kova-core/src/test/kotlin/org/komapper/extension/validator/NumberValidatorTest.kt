package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import java.util.Locale

class NumberValidatorTest :
    FunSpec({

        beforeSpec {
            Locale.setDefault(Locale.US)
        }

        context("ensurePositive") {
            test("success with ensurePositive number") {
                val result = tryValidate { ensurePositive(1) }
                result.shouldBeSuccess()
            }

            test("success with large ensurePositive number") {
                val result = tryValidate { ensurePositive(100) }
                result.shouldBeSuccess()
            }

            test("failure with zero") {
                val result = tryValidate { ensurePositive(0) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.positive"
            }

            test("failure with ensureNegative number") {
                val result = tryValidate { ensurePositive(-1) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.positive"
            }
        }

        context("ensurePositive with double") {
            test("success with ensurePositive number") {
                val result = tryValidate { ensurePositive(0.1) }
                result.shouldBeSuccess()
            }

            test("failure with ensureNegative number") {
                val result = tryValidate { ensurePositive(-0.1) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.positive"
            }
        }

        context("ensureNegative") {
            test("success with ensureNegative number") {
                val result = tryValidate { ensureNegative(-1) }
                result.shouldBeSuccess()
            }

            test("success with large ensureNegative number") {
                val result = tryValidate { ensureNegative(-100) }
                result.shouldBeSuccess()
            }

            test("failure with zero") {
                val result = tryValidate { ensureNegative(0) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.negative"
            }

            test("failure with ensurePositive number") {
                val result = tryValidate { ensureNegative(1) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.negative"
            }
        }

        context("ensureNegative with double") {
            test("success with ensureNegative number") {
                val result = tryValidate { ensureNegative(-0.1) }
                result.shouldBeSuccess()
            }

            test("failure with ensurePositive number") {
                val result = tryValidate { ensureNegative(0.1) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.negative"
            }
        }

        context("ensureNotPositive") {
            test("success with zero") {
                val result = tryValidate { ensureNotPositive(0) }
                result.shouldBeSuccess()
            }

            test("success with ensureNegative number") {
                val result = tryValidate { ensureNotPositive(-1) }
                result.shouldBeSuccess()
            }

            test("success with large ensureNegative number") {
                val result = tryValidate { ensureNotPositive(-100) }
                result.shouldBeSuccess()
            }

            test("failure with ensurePositive number") {
                val result = tryValidate { ensureNotPositive(1) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.notPositive"
            }
        }

        context("ensureNotPositive with double") {
            test("success with zero") {
                val result = tryValidate { ensureNotPositive(0.0) }
                result.shouldBeSuccess()
            }

            test("success with ensureNegative number") {
                val result = tryValidate { ensureNotPositive(-0.1) }
                result.shouldBeSuccess()
            }

            test("failure with ensurePositive number") {
                val result = tryValidate { ensureNotPositive(0.1) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.notPositive"
            }
        }

        context("ensureGreaterThan (greater than)") {
            test("success with value greater than threshold") {
                val result = tryValidate { ensureGreaterThan(6, 5) }
                result.shouldBeSuccess()
            }

            test("success with large value") {
                val result = tryValidate { ensureGreaterThan(100, 5) }
                result.shouldBeSuccess()
            }

            test("failure with equal value") {
                val result = tryValidate { ensureGreaterThan(5, 5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.greaterThan"
            }

            test("failure with value less than threshold") {
                val result = tryValidate { ensureGreaterThan(4, 5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.greaterThan"
            }
        }

        context("ensureGreaterThan with double") {
            test("success with value greater than threshold") {
                val result = tryValidate { ensureGreaterThan(5.6, 5.5) }
                result.shouldBeSuccess()
            }

            test("failure with equal value") {
                val result = tryValidate { ensureGreaterThan(5.5, 5.5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.greaterThan"
            }

            test("failure with smaller value") {
                val result = tryValidate { ensureGreaterThan(5.4, 5.5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.greaterThan"
            }
        }

        context("ensureGreaterThanOrEqual (greater than or equal)") {
            test("success with value greater than threshold") {
                val result = tryValidate { ensureGreaterThanOrEqual(6, 5) }
                result.shouldBeSuccess()
            }

            test("success with equal value") {
                val result = tryValidate { ensureGreaterThanOrEqual(5, 5) }
                result.shouldBeSuccess()
            }

            test("failure with value less than threshold") {
                val result = tryValidate { ensureGreaterThanOrEqual(4, 5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.greaterThanOrEquals"
            }
        }

        context("ensureGreaterThanOrEqual with double") {
            test("success with greater value") {
                val result = tryValidate { ensureGreaterThanOrEqual(5.6, 5.5) }
                result.shouldBeSuccess()
            }

            test("success with equal value") {
                val result = tryValidate { ensureGreaterThanOrEqual(5.5, 5.5) }
                result.shouldBeSuccess()
            }

            test("failure with value less than threshold") {
                val result = tryValidate { ensureGreaterThanOrEqual(5.4, 5.5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.greaterThanOrEquals"
            }
        }

        context("ensureLessThan (less than)") {
            test("success with value less than threshold") {
                val result = tryValidate { ensureLessThan(4, 5) }
                result.shouldBeSuccess()
            }

            test("success with large ensureNegative value") {
                val result = tryValidate { ensureLessThan(-100, 5) }
                result.shouldBeSuccess()
            }

            test("failure with equal value") {
                val result = tryValidate { ensureLessThan(5, 5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.lessThan"
            }

            test("failure with value greater than threshold") {
                val result = tryValidate { ensureLessThan(6, 5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.lessThan"
            }
        }

        context("ensureLessThan with double") {
            test("success with value less than threshold") {
                val result = tryValidate { ensureLessThan(5.4, 5.5) }
                result.shouldBeSuccess()
            }

            test("failure with equal value") {
                val result = tryValidate { ensureLessThan(5.5, 5.5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.lessThan"
            }

            test("failure with greater value") {
                val result = tryValidate { ensureLessThan(5.6, 5.5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.lessThan"
            }
        }

        context("ensureLessThanOrEqual (less than or equal)") {
            test("success with value less than threshold") {
                val result = tryValidate { ensureLessThanOrEqual(4, 5) }
                result.shouldBeSuccess()
            }

            test("success with equal value") {
                val result = tryValidate { ensureLessThanOrEqual(5, 5) }
                result.shouldBeSuccess()
            }

            test("failure with value greater than threshold") {
                val result = tryValidate { ensureLessThanOrEqual(6, 5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.lessThanOrEquals"
            }
        }

        context("ensureLessThanOrEqual with double") {
            test("success with smaller value") {
                val result = tryValidate { ensureLessThanOrEqual(5.4, 5.5) }
                result.shouldBeSuccess()
            }

            test("success with equal value") {
                val result = tryValidate { ensureLessThanOrEqual(5.5, 5.5) }
                result.shouldBeSuccess()
            }

            test("failure with value greater than threshold") {
                val result = tryValidate { ensureLessThanOrEqual(5.6, 5.5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.lessThanOrEquals"
            }
        }

        context("ensurePositive with float") {
            test("success with ensurePositive number") {
                val result = tryValidate { ensurePositive(1.5f) }
                result.shouldBeSuccess()
            }

            test("failure with ensureNegative number") {
                val result = tryValidate { ensurePositive(-1.5f) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.positive"
            }
        }

        context("ensureNegative with byte") {
            test("success with ensureNegative number") {
                val result = tryValidate { ensureNegative(-10) }
                result.shouldBeSuccess()
            }

            test("failure with ensurePositive number") {
                val result = tryValidate { ensureNegative(10) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.negative"
            }
        }

        context("ensureGreaterThan with bigDecimal") {
            test("success with value greater than threshold") {
                val result = tryValidate { ensureGreaterThan(150.toBigDecimal(), 100.toBigDecimal()) }
                result.shouldBeSuccess()
            }

            test("failure with equal value") {
                val result = tryValidate { ensureGreaterThan(100.toBigDecimal(), 100.toBigDecimal()) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.greaterThan"
            }

            test("failure with smaller value") {
                val result = tryValidate { ensureGreaterThan(50.toBigDecimal(), 100.toBigDecimal()) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.greaterThan"
            }
        }

        context("ensureLessThan with bigInteger") {
            test("success with value less than threshold") {
                val result = tryValidate { ensureLessThan(50.toBigInteger(), 100.toBigInteger()) }
                result.shouldBeSuccess()
            }

            test("failure with equal value") {
                val result = tryValidate { ensureLessThan(100.toBigInteger(), 100.toBigInteger()) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.lessThan"
            }

            test("failure with greater value") {
                val result = tryValidate { ensureLessThan(150.toBigInteger(), 100.toBigInteger()) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.lessThan"
            }
        }
    })
