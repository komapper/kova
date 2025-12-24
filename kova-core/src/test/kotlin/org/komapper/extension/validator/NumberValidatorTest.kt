package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec

class NumberValidatorTest :
    FunSpec({

        context("plus") {
            context(_: Validation)
            fun Int.validate() = max(2) + { max(3) } + { negative() }

            test("success") {
                val result = tryValidate { (-1).validate() }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { 5.validate() }
                result.shouldBeFailure()
                result.messages.size shouldBe 3
                result.messages[0].constraintId shouldBe "kova.comparable.max"
                result.messages[1].constraintId shouldBe "kova.comparable.max"
                result.messages[2].constraintId shouldBe "kova.number.negative"
            }
        }

        context("or") {
            context(_: Validation)
            fun Int.validate() = or { max(2) } orElse { max(3) } and { min(1) }

            test("success with value 2") {
                val result = tryValidate { 2.validate() }
                result.shouldBeSuccess()
            }
            test("success with value 3") {
                val result = tryValidate { 3.validate() }
                result.shouldBeSuccess()
            }

            test("failure with value 4") {
                val result = tryValidate { 4.validate() }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.or"
            }
        }

        context("constrain") {
            context(_: Validation)
            fun Int.validate() = constrain("test") { satisfies(it == 10) { text("Constraint failed") } }

            test("success") {
                val result = tryValidate { 10.validate() }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { 20.validate() }
                result.shouldBeFailure()
                result.messages[0].text shouldBe "Constraint failed"
            }
        }

        context("positive") {
            test("success with positive number") {
                val result = tryValidate { 1.positive() }
                result.shouldBeSuccess()
            }

            test("success with large positive number") {
                val result = tryValidate { 100.positive() }
                result.shouldBeSuccess()
            }

            test("failure with zero") {
                val result = tryValidate { 0.positive() }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.positive"
            }

            test("failure with negative number") {
                val result = tryValidate { (-1).positive() }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.positive"
            }
        }

        context("positive with double") {
            test("success with positive number") {
                val result = tryValidate { 0.1.positive() }
                result.shouldBeSuccess()
            }

            test("failure with negative number") {
                val result = tryValidate { (-0.1).positive() }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.positive"
            }
        }

        context("negative") {
            test("success with negative number") {
                val result = tryValidate { (-1).negative() }
                result.shouldBeSuccess()
            }

            test("success with large negative number") {
                val result = tryValidate { (-100).negative() }
                result.shouldBeSuccess()
            }

            test("failure with zero") {
                val result = tryValidate { 0.negative() }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.negative"
            }

            test("failure with positive number") {
                val result = tryValidate { 1.negative() }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.negative"
            }
        }

        context("negative with double") {
            test("success with negative number") {
                val result = tryValidate { (-0.1).negative() }
                result.shouldBeSuccess()
            }

            test("failure with positive number") {
                val result = tryValidate { 0.1.negative() }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.negative"
            }
        }

        context("notPositive") {
            test("success with zero") {
                val result = tryValidate { 0.notPositive() }
                result.shouldBeSuccess()
            }

            test("success with negative number") {
                val result = tryValidate { (-1).notPositive() }
                result.shouldBeSuccess()
            }

            test("success with large negative number") {
                val result = tryValidate { (-100).notPositive() }
                result.shouldBeSuccess()
            }

            test("failure with positive number") {
                val result = tryValidate { 1.notPositive() }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.notPositive"
            }
        }

        context("notPositive with double") {
            test("success with zero") {
                val result = tryValidate { 0.0.notPositive() }
                result.shouldBeSuccess()
            }

            test("success with negative number") {
                val result = tryValidate { (-0.1).notPositive() }
                result.shouldBeSuccess()
            }

            test("failure with positive number") {
                val result = tryValidate { 0.1.notPositive() }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.notPositive"
            }
        }

        context("gt (greater than)") {
            test("success with value greater than threshold") {
                val result = tryValidate { 6.gt(5) }
                result.shouldBeSuccess()
            }

            test("success with large value") {
                val result = tryValidate { 100.gt(5) }
                result.shouldBeSuccess()
            }

            test("failure with equal value") {
                val result = tryValidate { 5.gt(5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.gt"
            }

            test("failure with value less than threshold") {
                val result = tryValidate { 4.gt(5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.gt"
            }
        }

        context("gt with double") {
            test("success with value greater than threshold") {
                val result = tryValidate { 5.6.gt(5.5) }
                result.shouldBeSuccess()
            }

            test("failure with equal value") {
                val result = tryValidate { 5.5.gt(5.5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.gt"
            }

            test("failure with smaller value") {
                val result = tryValidate { 5.4.gt(5.5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.gt"
            }
        }

        context("gte (greater than or equal)") {
            test("success with value greater than threshold") {
                val result = tryValidate { 6.gte(5) }
                result.shouldBeSuccess()
            }

            test("success with equal value") {
                val result = tryValidate { 5.gte(5) }
                result.shouldBeSuccess()
            }

            test("failure with value less than threshold") {
                val result = tryValidate { 4.gte(5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.gte"
            }
        }

        context("gte with double") {
            test("success with greater value") {
                val result = tryValidate { 5.6.gte(5.5) }
                result.shouldBeSuccess()
            }

            test("success with equal value") {
                val result = tryValidate { 5.5.gte(5.5) }
                result.shouldBeSuccess()
            }

            test("failure with value less than threshold") {
                val result = tryValidate { 5.4.gte(5.5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.gte"
            }
        }

        context("lt (less than)") {
            test("success with value less than threshold") {
                val result = tryValidate { 4.lt(5) }
                result.shouldBeSuccess()
            }

            test("success with large negative value") {
                val result = tryValidate { (-100).lt(5) }
                result.shouldBeSuccess()
            }

            test("failure with equal value") {
                val result = tryValidate { 5.lt(5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.lt"
            }

            test("failure with value greater than threshold") {
                val result = tryValidate { 6.lt(5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.lt"
            }
        }

        context("lt with double") {
            test("success with value less than threshold") {
                val result = tryValidate { 5.4.lt(5.5) }
                result.shouldBeSuccess()
            }

            test("failure with equal value") {
                val result = tryValidate { 5.5.lt(5.5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.lt"
            }

            test("failure with greater value") {
                val result = tryValidate { 5.6.lt(5.5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.lt"
            }
        }

        context("lte (less than or equal)") {
            test("success with value less than threshold") {
                val result = tryValidate { 4.lte(5) }
                result.shouldBeSuccess()
            }

            test("success with equal value") {
                val result = tryValidate { 5.lte(5) }
                result.shouldBeSuccess()
            }

            test("failure with value greater than threshold") {
                val result = tryValidate { 6.lte(5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.lte"
            }
        }

        context("lte with double") {
            test("success with smaller value") {
                val result = tryValidate { 5.4.lte(5.5) }
                result.shouldBeSuccess()
            }

            test("success with equal value") {
                val result = tryValidate { 5.5.lte(5.5) }
                result.shouldBeSuccess()
            }

            test("failure with value greater than threshold") {
                val result = tryValidate { 5.6.lte(5.5) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.lte"
            }
        }

        context("positive with float") {
            test("success with positive number") {
                val result = tryValidate { 1.5f.positive() }
                result.shouldBeSuccess()
            }

            test("failure with negative number") {
                val result = tryValidate { (-1.5f).positive() }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.positive"
            }
        }

        context("negative with byte") {
            test("success with negative number") {
                val result = tryValidate { (-10).negative() }
                result.shouldBeSuccess()
            }

            test("failure with positive number") {
                val result = tryValidate { 10.negative() }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.number.negative"
            }
        }

        context("gt with bigDecimal") {
            test("success with value greater than threshold") {
                val result = tryValidate { 150.toBigDecimal().gt(100.toBigDecimal()) }
                result.shouldBeSuccess()
            }

            test("failure with equal value") {
                val result = tryValidate { 100.toBigDecimal().gt(100.toBigDecimal()) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.gt"
            }

            test("failure with smaller value") {
                val result = tryValidate { 50.toBigDecimal().gt(100.toBigDecimal()) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.gt"
            }
        }

        context("lt with bigInteger") {
            test("success with value less than threshold") {
                val result = tryValidate { 50.toBigInteger().lt(100.toBigInteger()) }
                result.shouldBeSuccess()
            }

            test("failure with equal value") {
                val result = tryValidate { 100.toBigInteger().lt(100.toBigInteger()) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.lt"
            }

            test("failure with greater value") {
                val result = tryValidate { 150.toBigInteger().lt(100.toBigInteger()) }
                result.shouldBeFailure()
                result.messages[0].constraintId shouldBe "kova.comparable.lt"
            }
        }
    })
