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
                result.messages[0].content shouldBe "Number 5 must be less than or equal to 2"
                result.messages[1].content shouldBe "Number 5 must be less than or equal to 3"
                result.messages[2].content shouldBe "Number 5 must be negative"
            }
        }

        context("or") {
            val validator = (Kova.int().max(2) or Kova.int().max(3)).min(1)

            test("success : 2") {
                val result = validator.tryValidate(2)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 2
            }
            test("success : 3") {
                val result = validator.tryValidate(3)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 3
            }

            test("failure : 4") {
                val result = validator.tryValidate(4)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].id shouldBe "kova.or"
            }
        }

        context("constrain") {
            val validator =
                Kova.int().constrain("test") {
                    satisfies(it.input == 10, Message.Text("Constraint failed"))
                }

            test("success") {
                val result = validator.tryValidate(10)
                result.isSuccess().mustBeTrue()
            }

            test("failure") {
                val result = validator.tryValidate(20)
                result.isFailure().mustBeTrue()
                result.messages[0].content shouldBe "Constraint failed"
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
                result.messages[0].content shouldBe "Number 0 must be positive"
            }

            test("failure with negative number") {
                val result = validator.tryValidate(-1)
                result.isFailure().mustBeTrue()
                result.messages[0].content shouldBe "Number -1 must be positive"
            }
        }

        context("positive with double") {
            val validator = Kova.double().positive()

            test("success") {
                val result = validator.tryValidate(0.1)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 0.1
            }

            test("failure") {
                val result = validator.tryValidate(-0.1)
                result.isFailure().mustBeTrue()
                result.messages[0].content shouldBe "Number -0.1 must be positive"
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
                result.messages[0].content shouldBe "Number 0 must be negative"
            }

            test("failure with positive number") {
                val result = validator.tryValidate(1)
                result.isFailure().mustBeTrue()
                result.messages[0].content shouldBe "Number 1 must be negative"
            }
        }

        context("negative with double") {
            val validator = Kova.double().negative()

            test("success") {
                val result = validator.tryValidate(-0.1)
                result.isSuccess().mustBeTrue()
                result.value shouldBe -0.1
            }

            test("failure") {
                val result = validator.tryValidate(0.1)
                result.isFailure().mustBeTrue()
                result.messages[0].content shouldBe "Number 0.1 must be negative"
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
                result.messages[0].content shouldBe "Number 1 must not be positive"
            }
        }

        context("notPositive with double") {
            val validator = Kova.double().notPositive()

            test("success with zero") {
                val result = validator.tryValidate(0.0)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 0.0
            }

            test("success with negative") {
                val result = validator.tryValidate(-0.1)
                result.isSuccess().mustBeTrue()
                result.value shouldBe -0.1
            }

            test("failure with positive") {
                val result = validator.tryValidate(0.1)
                result.isFailure().mustBeTrue()
                result.messages[0].content shouldBe "Number 0.1 must not be positive"
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
                result.messages[0].content shouldBe "Number 5 must be greater than 5"
            }

            test("failure with value less than threshold") {
                val result = validator.tryValidate(4)
                result.isFailure().mustBeTrue()
                result.messages[0].content shouldBe "Number 4 must be greater than 5"
            }
        }

        context("gt with double") {
            val validator = Kova.double().gt(5.5)

            test("success") {
                val result = validator.tryValidate(5.6)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 5.6
            }

            test("failure with equal value") {
                val result = validator.tryValidate(5.5)
                result.isFailure().mustBeTrue()
                result.messages[0].content shouldBe "Number 5.5 must be greater than 5.5"
            }

            test("failure with smaller value") {
                val result = validator.tryValidate(5.4)
                result.isFailure().mustBeTrue()
                result.messages[0].content shouldBe "Number 5.4 must be greater than 5.5"
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
                result.messages[0].content shouldBe "Number 4 must be greater than or equal to 5"
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

            test("failure") {
                val result = validator.tryValidate(5.4)
                result.isFailure().mustBeTrue()
                result.messages[0].content shouldBe "Number 5.4 must be greater than or equal to 5.5"
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
                result.messages[0].content shouldBe "Number 5 must be less than 5"
            }

            test("failure with value greater than threshold") {
                val result = validator.tryValidate(6)
                result.isFailure().mustBeTrue()
                result.messages[0].content shouldBe "Number 6 must be less than 5"
            }
        }

        context("lt with double") {
            val validator = Kova.double().lt(5.5)

            test("success") {
                val result = validator.tryValidate(5.4)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 5.4
            }

            test("failure with equal value") {
                val result = validator.tryValidate(5.5)
                result.isFailure().mustBeTrue()
                result.messages[0].content shouldBe "Number 5.5 must be less than 5.5"
            }

            test("failure with greater value") {
                val result = validator.tryValidate(5.6)
                result.isFailure().mustBeTrue()
                result.messages[0].content shouldBe "Number 5.6 must be less than 5.5"
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
                result.messages[0].content shouldBe "Number 6 must be less than or equal to 5"
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

            test("failure") {
                val result = validator.tryValidate(5.6)
                result.isFailure().mustBeTrue()
                result.messages[0].content shouldBe "Number 5.6 must be less than or equal to 5.5"
            }
        }
    })
