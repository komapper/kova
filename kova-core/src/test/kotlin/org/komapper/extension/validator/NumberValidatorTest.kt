package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class NumberValidatorTest :
    FunSpec({

        context("plus") {
            val validator = (Kova.int().max(2) + Kova.int().max(3)).negative()

            test("success") {
                val result = validator.tryValidate(-1)
                result.isSuccess().mustBeTrue()
                result.value shouldBe -1
            }

            test("failure") {
                val result = validator.tryValidate(5)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 3
                result.messages[0].constraintId shouldBe "kova.comparable.max"
                result.messages[1].constraintId shouldBe "kova.comparable.max"
                result.messages[2].constraintId shouldBe "kova.number.negative"
            }
        }

        context("or") {
            val validator = (Kova.int().max(2) or Kova.int().max(3)).min(1)

            test("success with value 2") {
                val result = validator.tryValidate(2)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 2
            }
            test("success with value 3") {
                val result = validator.tryValidate(3)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 3
            }

            test("failure with value 4") {
                val result = validator.tryValidate(4)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.or"
            }
        }

        context("constrain") {
            val validator =
                Kova.int().constrain {
                    satisfies(it == 10, text("Constraint failed"))
                }

            test("success") {
                val result = validator.tryValidate(10)
                result.isSuccess().mustBeTrue()
            }

            test("failure") {
                val result = validator.tryValidate(20)
                result.isFailure().mustBeTrue()
                result.messages[0].text shouldBe "Constraint failed"
            }
        }

        context("positive") {
            val validator = Kova.int().positive()

            test("success with positive number") {
                val result = validator.tryValidate(1)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 1
            }

            test("success with large positive number") {
                val result = validator.tryValidate(100)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 100
            }

            test("failure with zero") {
                val result = validator.tryValidate(0)
                result.isFailure().mustBeTrue()
                result.messages[0].constraintId shouldBe "kova.number.positive"
            }

            test("failure with negative number") {
                val result = validator.tryValidate(-1)
                result.isFailure().mustBeTrue()
                result.messages[0].constraintId shouldBe "kova.number.positive"
            }
        }

        context("positive with double") {
            val validator = Kova.double().positive()

            test("success with positive number") {
                val result = validator.tryValidate(0.1)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 0.1
            }

            test("failure with negative number") {
                val result = validator.tryValidate(-0.1)
                result.isFailure().mustBeTrue()
                result.messages[0].constraintId shouldBe "kova.number.positive"
            }
        }

        context("negative") {
            val validator = Kova.int().negative()

            test("success with negative number") {
                val result = validator.tryValidate(-1)
                result.isSuccess().mustBeTrue()
                result.value shouldBe -1
            }

            test("success with large negative number") {
                val result = validator.tryValidate(-100)
                result.isSuccess().mustBeTrue()
                result.value shouldBe -100
            }

            test("failure with zero") {
                val result = validator.tryValidate(0)
                result.isFailure().mustBeTrue()
                result.messages[0].constraintId shouldBe "kova.number.negative"
            }

            test("failure with positive number") {
                val result = validator.tryValidate(1)
                result.isFailure().mustBeTrue()
                result.messages[0].constraintId shouldBe "kova.number.negative"
            }
        }

        context("negative with double") {
            val validator = Kova.double().negative()

            test("success with negative number") {
                val result = validator.tryValidate(-0.1)
                result.isSuccess().mustBeTrue()
                result.value shouldBe -0.1
            }

            test("failure with positive number") {
                val result = validator.tryValidate(0.1)
                result.isFailure().mustBeTrue()
                result.messages[0].constraintId shouldBe "kova.number.negative"
            }
        }

        context("notPositive") {
            val validator = Kova.int().notPositive()

            test("success with zero") {
                val result = validator.tryValidate(0)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 0
            }

            test("success with negative number") {
                val result = validator.tryValidate(-1)
                result.isSuccess().mustBeTrue()
                result.value shouldBe -1
            }

            test("success with large negative number") {
                val result = validator.tryValidate(-100)
                result.isSuccess().mustBeTrue()
                result.value shouldBe -100
            }

            test("failure with positive number") {
                val result = validator.tryValidate(1)
                result.isFailure().mustBeTrue()
                result.messages[0].constraintId shouldBe "kova.number.notPositive"
            }
        }

        context("notPositive with double") {
            val validator = Kova.double().notPositive()

            test("success with zero") {
                val result = validator.tryValidate(0.0)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 0.0
            }

            test("success with negative number") {
                val result = validator.tryValidate(-0.1)
                result.isSuccess().mustBeTrue()
                result.value shouldBe -0.1
            }

            test("failure with positive number") {
                val result = validator.tryValidate(0.1)
                result.isFailure().mustBeTrue()
                result.messages[0].constraintId shouldBe "kova.number.notPositive"
            }
        }

        context("gt (greater than)") {
            val validator = Kova.int().gt(5)

            test("success with value greater than threshold") {
                val result = validator.tryValidate(6)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 6
            }

            test("success with large value") {
                val result = validator.tryValidate(100)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 100
            }

            test("failure with equal value") {
                val result = validator.tryValidate(5)
                result.isFailure().mustBeTrue()
                result.messages[0].constraintId shouldBe "kova.comparable.gt"
            }

            test("failure with value less than threshold") {
                val result = validator.tryValidate(4)
                result.isFailure().mustBeTrue()
                result.messages[0].constraintId shouldBe "kova.comparable.gt"
            }
        }

        context("gt with double") {
            val validator = Kova.double().gt(5.5)

            test("success with value greater than threshold") {
                val result = validator.tryValidate(5.6)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 5.6
            }

            test("failure with equal value") {
                val result = validator.tryValidate(5.5)
                result.isFailure().mustBeTrue()
                result.messages[0].constraintId shouldBe "kova.comparable.gt"
            }

            test("failure with smaller value") {
                val result = validator.tryValidate(5.4)
                result.isFailure().mustBeTrue()
                result.messages[0].constraintId shouldBe "kova.comparable.gt"
            }
        }

        context("gte (greater than or equal)") {
            val validator = Kova.int().gte(5)

            test("success with value greater than threshold") {
                val result = validator.tryValidate(6)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 6
            }

            test("success with equal value") {
                val result = validator.tryValidate(5)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 5
            }

            test("failure with value less than threshold") {
                val result = validator.tryValidate(4)
                result.isFailure().mustBeTrue()
                result.messages[0].constraintId shouldBe "kova.comparable.gte"
            }
        }

        context("gte with double") {
            val validator = Kova.double().gte(5.5)

            test("success with greater value") {
                val result = validator.tryValidate(5.6)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 5.6
            }

            test("success with equal value") {
                val result = validator.tryValidate(5.5)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 5.5
            }

            test("failure with value less than threshold") {
                val result = validator.tryValidate(5.4)
                result.isFailure().mustBeTrue()
                result.messages[0].constraintId shouldBe "kova.comparable.gte"
            }
        }

        context("lt (less than)") {
            val validator = Kova.int().lt(5)

            test("success with value less than threshold") {
                val result = validator.tryValidate(4)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 4
            }

            test("success with large negative value") {
                val result = validator.tryValidate(-100)
                result.isSuccess().mustBeTrue()
                result.value shouldBe -100
            }

            test("failure with equal value") {
                val result = validator.tryValidate(5)
                result.isFailure().mustBeTrue()
                result.messages[0].constraintId shouldBe "kova.comparable.lt"
            }

            test("failure with value greater than threshold") {
                val result = validator.tryValidate(6)
                result.isFailure().mustBeTrue()
                result.messages[0].constraintId shouldBe "kova.comparable.lt"
            }
        }

        context("lt with double") {
            val validator = Kova.double().lt(5.5)

            test("success with value less than threshold") {
                val result = validator.tryValidate(5.4)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 5.4
            }

            test("failure with equal value") {
                val result = validator.tryValidate(5.5)
                result.isFailure().mustBeTrue()
                result.messages[0].constraintId shouldBe "kova.comparable.lt"
            }

            test("failure with greater value") {
                val result = validator.tryValidate(5.6)
                result.isFailure().mustBeTrue()
                result.messages[0].constraintId shouldBe "kova.comparable.lt"
            }
        }

        context("lte (less than or equal)") {
            val validator = Kova.int().lte(5)

            test("success with value less than threshold") {
                val result = validator.tryValidate(4)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 4
            }

            test("success with equal value") {
                val result = validator.tryValidate(5)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 5
            }

            test("failure with value greater than threshold") {
                val result = validator.tryValidate(6)
                result.isFailure().mustBeTrue()
                result.messages[0].constraintId shouldBe "kova.comparable.lte"
            }
        }

        context("lte with double") {
            val validator = Kova.double().lte(5.5)

            test("success with smaller value") {
                val result = validator.tryValidate(5.4)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 5.4
            }

            test("success with equal value") {
                val result = validator.tryValidate(5.5)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 5.5
            }

            test("failure with value greater than threshold") {
                val result = validator.tryValidate(5.6)
                result.isFailure().mustBeTrue()
                result.messages[0].constraintId shouldBe "kova.comparable.lte"
            }
        }

        context("positive with float") {
            val validator = Kova.float().positive()

            test("success with positive number") {
                val result = validator.tryValidate(1.5f)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 1.5f
            }

            test("failure with negative number") {
                val result = validator.tryValidate(-1.5f)
                result.isFailure().mustBeTrue()
                result.messages[0].constraintId shouldBe "kova.number.positive"
            }
        }

        context("negative with byte") {
            val validator = Kova.byte().negative()

            test("success with negative number") {
                val result = validator.tryValidate(-10)
                result.isSuccess().mustBeTrue()
                result.value shouldBe (-10).toByte()
            }

            test("failure with positive number") {
                val result = validator.tryValidate(10)
                result.isFailure().mustBeTrue()
                result.messages[0].constraintId shouldBe "kova.number.negative"
            }
        }

        context("gt with bigDecimal") {
            val validator = Kova.bigDecimal().gt(100.toBigDecimal())

            test("success with value greater than threshold") {
                val result = validator.tryValidate(150.toBigDecimal())
                result.isSuccess().mustBeTrue()
                result.value shouldBe 150.toBigDecimal()
            }

            test("failure with equal value") {
                val result = validator.tryValidate(100.toBigDecimal())
                result.isFailure().mustBeTrue()
                result.messages[0].constraintId shouldBe "kova.comparable.gt"
            }

            test("failure with smaller value") {
                val result = validator.tryValidate(50.toBigDecimal())
                result.isFailure().mustBeTrue()
                result.messages[0].constraintId shouldBe "kova.comparable.gt"
            }
        }

        context("lt with bigInteger") {
            val validator = Kova.bigInteger().lt(100.toBigInteger())

            test("success with value less than threshold") {
                val result = validator.tryValidate(50.toBigInteger())
                result.isSuccess().mustBeTrue()
                result.value shouldBe 50.toBigInteger()
            }

            test("failure with equal value") {
                val result = validator.tryValidate(100.toBigInteger())
                result.isFailure().mustBeTrue()
                result.messages[0].constraintId shouldBe "kova.comparable.lt"
            }

            test("failure with greater value") {
                val result = validator.tryValidate(150.toBigInteger())
                result.isFailure().mustBeTrue()
                result.messages[0].constraintId shouldBe "kova.comparable.lt"
            }
        }
    })
