package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import java.util.Locale

class NumberValidatorTest :
    FunSpec({

        beforeSpec {
            Locale.setDefault(Locale.US)
        }

        context("ensurePositive") {
            test("success with positive number") {
                val result = tryValidate { 1.ensurePositive() }
                result.shouldBeSuccess()
            }

            test("success with large positive number") {
                val result = tryValidate { 100.ensurePositive() }
                result.shouldBeSuccess()
            }

            test("failure with zero") {
                val result = tryValidate { 0.ensurePositive() }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.positive"
            }

            test("failure with negative number") {
                val result = tryValidate { (-1).ensurePositive() }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.positive"
            }
        }

        context("ensurePositive with double") {
            test("success with positive number") {
                val result = tryValidate { 0.1.ensurePositive() }
                result.shouldBeSuccess()
            }

            test("failure with negative number") {
                val result = tryValidate { (-0.1).ensurePositive() }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.positive"
            }
        }

        context("ensurePositive with float") {
            test("success with positive number") {
                val result = tryValidate { 1.5f.ensurePositive() }
                result.shouldBeSuccess()
            }

            test("failure with negative number") {
                val result = tryValidate { (-1.5f).ensurePositive() }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.positive"
            }
        }

        context("ensureNegative") {
            test("success with negative number") {
                val result = tryValidate { (-1).ensureNegative() }
                result.shouldBeSuccess()
            }

            test("success with large negative number") {
                val result = tryValidate { (-100).ensureNegative() }
                result.shouldBeSuccess()
            }

            test("failure with zero") {
                val result = tryValidate { 0.ensureNegative() }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.negative"
            }

            test("failure with positive number") {
                val result = tryValidate { 1.ensureNegative() }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.negative"
            }
        }

        context("ensureNegative with double") {
            test("success with negative number") {
                val result = tryValidate { (-0.1).ensureNegative() }
                result.shouldBeSuccess()
            }

            test("failure with positive number") {
                val result = tryValidate { 0.1.ensureNegative() }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.negative"
            }
        }

        context("ensureNegative with byte") {
            test("success with negative number") {
                val result = tryValidate { (-10).ensureNegative() }
                result.shouldBeSuccess()
            }

            test("failure with positive number") {
                val result = tryValidate { 10.ensureNegative() }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.negative"
            }
        }

        context("ensurePositiveOrZero") {
            test("success with zero") {
                val result = tryValidate { 0.ensurePositiveOrZero() }
                result.shouldBeSuccess()
            }

            test("success with positive number") {
                val result = tryValidate { 1.ensurePositiveOrZero() }
                result.shouldBeSuccess()
            }

            test("success with large positive number") {
                val result = tryValidate { 100.ensurePositiveOrZero() }
                result.shouldBeSuccess()
            }

            test("failure with negative number") {
                val result = tryValidate { (-1).ensurePositiveOrZero() }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.positiveOrZero"
            }
        }

        context("ensurePositiveOrZero with double") {
            test("success with zero") {
                val result = tryValidate { 0.0.ensurePositiveOrZero() }
                result.shouldBeSuccess()
            }

            test("success with positive number") {
                val result = tryValidate { 0.1.ensurePositiveOrZero() }
                result.shouldBeSuccess()
            }

            test("failure with negative number") {
                val result = tryValidate { (-0.1).ensurePositiveOrZero() }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.positiveOrZero"
            }
        }

        context("ensureNegativeOrZero") {
            test("success with zero") {
                val result = tryValidate { 0.ensureNegativeOrZero() }
                result.shouldBeSuccess()
            }

            test("success with negative number") {
                val result = tryValidate { (-1).ensureNegativeOrZero() }
                result.shouldBeSuccess()
            }

            test("success with large negative number") {
                val result = tryValidate { (-100).ensureNegativeOrZero() }
                result.shouldBeSuccess()
            }

            test("failure with positive number") {
                val result = tryValidate { 1.ensureNegativeOrZero() }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.negativeOrZero"
            }
        }

        context("ensureNegativeOrZero with double") {
            test("success with zero") {
                val result = tryValidate { 0.0.ensureNegativeOrZero() }
                result.shouldBeSuccess()
            }

            test("success with negative number") {
                val result = tryValidate { (-0.1).ensureNegativeOrZero() }
                result.shouldBeSuccess()
            }

            test("failure with positive number") {
                val result = tryValidate { 0.1.ensureNegativeOrZero() }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.negativeOrZero"
            }
        }

        context("ensureGreaterThan") {
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

        context("ensureLessThan") {
            test("success with value less than threshold") {
                val result = tryValidate { 4.ensureLessThan(5) }
                result.shouldBeSuccess()
            }

            test("success with large negative value") {
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

        context("ensureDigits with BigDecimal") {
            test("success with valid integer and fraction digits") {
                val result = tryValidate { "123456.78".toBigDecimal().ensureDigits(integer = 6, fraction = 2) }
                result.shouldBeSuccess()
            }

            test("success with fewer digits than maximum") {
                val result = tryValidate { "12.3".toBigDecimal().ensureDigits(integer = 6, fraction = 2) }
                result.shouldBeSuccess()
            }

            test("success with zero fractional part") {
                val result = tryValidate { "1234".toBigDecimal().ensureDigits(integer = 6, fraction = 2) }
                result.shouldBeSuccess()
            }

            test("success with only fractional part") {
                val result = tryValidate { "0.99".toBigDecimal().ensureDigits(integer = 6, fraction = 2) }
                result.shouldBeSuccess()
            }

            test("failure with too many integer digits") {
                val result = tryValidate { "1234567.89".toBigDecimal().ensureDigits(integer = 6, fraction = 2) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.digits"
            }

            test("failure with too many fraction digits") {
                val result = tryValidate { "123456.789".toBigDecimal().ensureDigits(integer = 6, fraction = 2) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.digits"
            }

            test("success with zero value") {
                val result =
                    tryValidate {
                        java.math.BigDecimal.ZERO
                            .ensureDigits(integer = 6, fraction = 2)
                    }
                result.shouldBeSuccess()
            }

            test("success with negative value within bounds") {
                val result = tryValidate { "-123.45".toBigDecimal().ensureDigits(integer = 6, fraction = 2) }
                result.shouldBeSuccess()
            }

            test("failure with negative value exceeding integer bounds") {
                val result = tryValidate { "-1234567.89".toBigDecimal().ensureDigits(integer = 6, fraction = 2) }
                result.shouldBeFailure()
            }

            test("success with very small number") {
                val result = tryValidate { "0.001".toBigDecimal().ensureDigits(integer = 1, fraction = 3) }
                result.shouldBeSuccess()
            }

            test("failure with very small number exceeding fraction") {
                val result = tryValidate { "0.0001".toBigDecimal().ensureDigits(integer = 1, fraction = 3) }
                result.shouldBeFailure()
            }

            test("success with scientific notation within bounds (1E3 = 1000)") {
                val result = tryValidate { "1E3".toBigDecimal().ensureDigits(integer = 4, fraction = 0) }
                result.shouldBeSuccess()
            }

            test("failure with scientific notation exceeding integer bounds (1E3 = 1000)") {
                val result = tryValidate { "1E3".toBigDecimal().ensureDigits(integer = 3, fraction = 0) }
                result.shouldBeFailure()
            }

            test("success with scientific notation with decimal (1.5E2 = 150)") {
                val result = tryValidate { "1.5E2".toBigDecimal().ensureDigits(integer = 3, fraction = 0) }
                result.shouldBeSuccess()
            }

            test("success with negative exponent (1E-3 = 0.001)") {
                val result = tryValidate { "1E-3".toBigDecimal().ensureDigits(integer = 1, fraction = 3) }
                result.shouldBeSuccess()
            }

            test("failure with negative exponent exceeding fraction (1E-4 = 0.0001)") {
                val result = tryValidate { "1E-4".toBigDecimal().ensureDigits(integer = 1, fraction = 3) }
                result.shouldBeFailure()
            }
        }

        context("ensureDigits with Int") {
            test("success with integer within bounds") {
                val result = tryValidate { 12345.ensureDigits(integer = 6) }
                result.shouldBeSuccess()
            }

            test("failure with integer exceeding bounds") {
                val result = tryValidate { 1234567.ensureDigits(integer = 6) }
                result.shouldBeFailure()
            }
        }

        context("ensureDigits with Double") {
            test("success with double within bounds") {
                val result = tryValidate { 123.45.ensureDigits(integer = 6, fraction = 2) }
                result.shouldBeSuccess()
            }

            test("failure with double exceeding fraction bounds") {
                val result = tryValidate { 123.456.ensureDigits(integer = 6, fraction = 2) }
                result.shouldBeFailure()
            }
        }

        context("ensureDigits with Long") {
            test("success with long within bounds") {
                val result = tryValidate { 123456L.ensureDigits(integer = 6) }
                result.shouldBeSuccess()
            }

            test("failure with long exceeding bounds") {
                val result = tryValidate { 1234567L.ensureDigits(integer = 6) }
                result.shouldBeFailure()
            }
        }
    })
