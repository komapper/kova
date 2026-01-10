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
                val result = tryValidate { 1.ensurePositive() }
                result.shouldBeSuccess()
            }

            test("success with large ensurePositive number") {
                val result = tryValidate { 100.ensurePositive() }
                result.shouldBeSuccess()
            }

            test("failure with zero") {
                val result = tryValidate { 0.ensurePositive() }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.positive"
            }

            test("failure with ensureNegative number") {
                val result = tryValidate { (-1).ensurePositive() }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.positive"
            }
        }

        context("ensurePositive with double") {
            test("success with ensurePositive number") {
                val result = tryValidate { 0.1.ensurePositive() }
                result.shouldBeSuccess()
            }

            test("failure with ensureNegative number") {
                val result = tryValidate { (-0.1).ensurePositive() }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.positive"
            }
        }

        context("ensureNegative") {
            test("success with ensureNegative number") {
                val result = tryValidate { (-1).ensureNegative() }
                result.shouldBeSuccess()
            }

            test("success with large ensureNegative number") {
                val result = tryValidate { (-100).ensureNegative() }
                result.shouldBeSuccess()
            }

            test("failure with zero") {
                val result = tryValidate { 0.ensureNegative() }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.negative"
            }

            test("failure with ensurePositive number") {
                val result = tryValidate { 1.ensureNegative() }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.negative"
            }
        }

        context("ensureNegative with double") {
            test("success with ensureNegative number") {
                val result = tryValidate { (-0.1).ensureNegative() }
                result.shouldBeSuccess()
            }

            test("failure with ensurePositive number") {
                val result = tryValidate { 0.1.ensureNegative() }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.negative"
            }
        }

        context("ensureNotPositive") {
            test("success with zero") {
                val result = tryValidate { 0.ensureNotPositive() }
                result.shouldBeSuccess()
            }

            test("success with ensureNegative number") {
                val result = tryValidate { (-1).ensureNotPositive() }
                result.shouldBeSuccess()
            }

            test("success with large ensureNegative number") {
                val result = tryValidate { (-100).ensureNotPositive() }
                result.shouldBeSuccess()
            }

            test("failure with ensurePositive number") {
                val result = tryValidate { 1.ensureNotPositive() }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.notPositive"
            }
        }

        context("ensureNotPositive with double") {
            test("success with zero") {
                val result = tryValidate { 0.0.ensureNotPositive() }
                result.shouldBeSuccess()
            }

            test("success with ensureNegative number") {
                val result = tryValidate { (-0.1).ensureNotPositive() }
                result.shouldBeSuccess()
            }

            test("failure with ensurePositive number") {
                val result = tryValidate { 0.1.ensureNotPositive() }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.notPositive"
            }
        }

        context("ensureGreaterThan (greater than)") {
            test("success with value greater than threshold") {
                val result = tryValidate { 6.ensureGreaterThan(5) }
                result.shouldBeSuccess()
            }

            test("success with large value") {
                val result = tryValidate { 100.ensureGreaterThan(5) }
                result.shouldBeSuccess()
            }

            test("failure with equal value") {
                val result = tryValidate { 5.ensureGreaterThan(5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.greaterThan"
            }

            test("failure with value less than threshold") {
                val result = tryValidate { 4.ensureGreaterThan(5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.greaterThan"
            }
        }

        context("ensureGreaterThan with double") {
            test("success with value greater than threshold") {
                val result = tryValidate { 5.6.ensureGreaterThan(5.5) }
                result.shouldBeSuccess()
            }

            test("failure with equal value") {
                val result = tryValidate { 5.5.ensureGreaterThan(5.5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.greaterThan"
            }

            test("failure with smaller value") {
                val result = tryValidate { 5.4.ensureGreaterThan(5.5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.greaterThan"
            }
        }

        context("ensureGreaterThanOrEqual (greater than or equal)") {
            test("success with value greater than threshold") {
                val result = tryValidate { 6.ensureGreaterThanOrEqual(5) }
                result.shouldBeSuccess()
            }

            test("success with equal value") {
                val result = tryValidate { 5.ensureGreaterThanOrEqual(5) }
                result.shouldBeSuccess()
            }

            test("failure with value less than threshold") {
                val result = tryValidate { 4.ensureGreaterThanOrEqual(5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.greaterThanOrEqual"
            }
        }

        context("ensureGreaterThanOrEqual with double") {
            test("success with greater value") {
                val result = tryValidate { 5.6.ensureGreaterThanOrEqual(5.5) }
                result.shouldBeSuccess()
            }

            test("success with equal value") {
                val result = tryValidate { 5.5.ensureGreaterThanOrEqual(5.5) }
                result.shouldBeSuccess()
            }

            test("failure with value less than threshold") {
                val result = tryValidate { 5.4.ensureGreaterThanOrEqual(5.5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.greaterThanOrEqual"
            }
        }

        context("ensureLessThan (less than)") {
            test("success with value less than threshold") {
                val result = tryValidate { 4.ensureLessThan(5) }
                result.shouldBeSuccess()
            }

            test("success with large ensureNegative value") {
                val result = tryValidate { (-100).ensureLessThan(5) }
                result.shouldBeSuccess()
            }

            test("failure with equal value") {
                val result = tryValidate { 5.ensureLessThan(5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.lessThan"
            }

            test("failure with value greater than threshold") {
                val result = tryValidate { 6.ensureLessThan(5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.lessThan"
            }
        }

        context("ensureLessThan with double") {
            test("success with value less than threshold") {
                val result = tryValidate { 5.4.ensureLessThan(5.5) }
                result.shouldBeSuccess()
            }

            test("failure with equal value") {
                val result = tryValidate { 5.5.ensureLessThan(5.5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.lessThan"
            }

            test("failure with greater value") {
                val result = tryValidate { 5.6.ensureLessThan(5.5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.lessThan"
            }
        }

        context("ensureLessThanOrEqual (less than or equal)") {
            test("success with value less than threshold") {
                val result = tryValidate { 4.ensureLessThanOrEqual(5) }
                result.shouldBeSuccess()
            }

            test("success with equal value") {
                val result = tryValidate { 5.ensureLessThanOrEqual(5) }
                result.shouldBeSuccess()
            }

            test("failure with value greater than threshold") {
                val result = tryValidate { 6.ensureLessThanOrEqual(5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.lessThanOrEqual"
            }
        }

        context("ensureLessThanOrEqual with double") {
            test("success with smaller value") {
                val result = tryValidate { 5.4.ensureLessThanOrEqual(5.5) }
                result.shouldBeSuccess()
            }

            test("success with equal value") {
                val result = tryValidate { 5.5.ensureLessThanOrEqual(5.5) }
                result.shouldBeSuccess()
            }

            test("failure with value greater than threshold") {
                val result = tryValidate { 5.6.ensureLessThanOrEqual(5.5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.lessThanOrEqual"
            }
        }

        context("ensurePositive with float") {
            test("success with ensurePositive number") {
                val result = tryValidate { 1.5f.ensurePositive() }
                result.shouldBeSuccess()
            }

            test("failure with ensureNegative number") {
                val result = tryValidate { (-1.5f).ensurePositive() }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.positive"
            }
        }

        context("ensureNegative with byte") {
            test("success with ensureNegative number") {
                val result = tryValidate { (-10).ensureNegative() }
                result.shouldBeSuccess()
            }

            test("failure with ensurePositive number") {
                val result = tryValidate { 10.ensureNegative() }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.negative"
            }
        }

        context("ensureGreaterThan with bigDecimal") {
            test("success with value greater than threshold") {
                val result = tryValidate { 150.toBigDecimal().ensureGreaterThan(100.toBigDecimal()) }
                result.shouldBeSuccess()
            }

            test("failure with equal value") {
                val result = tryValidate { 100.toBigDecimal().ensureGreaterThan(100.toBigDecimal()) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.greaterThan"
            }

            test("failure with smaller value") {
                val result = tryValidate { 50.toBigDecimal().ensureGreaterThan(100.toBigDecimal()) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.greaterThan"
            }
        }

        context("ensureLessThan with bigInteger") {
            test("success with value less than threshold") {
                val result = tryValidate { 50.toBigInteger().ensureLessThan(100.toBigInteger()) }
                result.shouldBeSuccess()
            }

            test("failure with equal value") {
                val result = tryValidate { 100.toBigInteger().ensureLessThan(100.toBigInteger()) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.lessThan"
            }

            test("failure with greater value") {
                val result = tryValidate { 150.toBigInteger().ensureLessThan(100.toBigInteger()) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.lessThan"
            }
        }
    })
