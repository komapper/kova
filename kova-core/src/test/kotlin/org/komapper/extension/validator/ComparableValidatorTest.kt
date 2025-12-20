package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ComparableValidatorTest :
    FunSpec({

        context("plus") {
            val validator = (Kova.uInt().max(10u) + Kova.uInt().max(20u)).min(5u)

            test("success") {
                val result = validator.tryValidate(8u)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 8u
            }

            test("failure") {
                val result = validator.tryValidate(15u)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.comparable.max"
            }
        }

        context("or") {
            val validator = (Kova.uInt().max(10u) or Kova.uInt().max(20u)).min(5u)

            test("success : 10") {
                val result = validator.tryValidate(10u)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 10u
            }

            test("success : 20") {
                val result = validator.tryValidate(20u)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 20u
            }

            test("failure : 25") {
                val result = validator.tryValidate(25u)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.or"
            }
        }

        context("constrain") {
            val validator =
                Kova.uInt().constrain { satisfies(it == 10u) { text("Constraint failed") } }

            test("success") {
                val result = validator.tryValidate(10u)
                result.isSuccess().mustBeTrue()
            }

            test("failure") {
                val result = validator.tryValidate(20u)
                result.isFailure().mustBeTrue()
                result.messages.single().text shouldBe "Constraint failed"
            }
        }

        context("uInt") {
            context("min") {
                val validator = Kova.uInt().min(5u)

                test("success with value greater than threshold") {
                    val result = validator.tryValidate(6u)
                    result.isSuccess().mustBeTrue()
                    result.value shouldBe 6u
                }

                test("success with equal value") {
                    val result = validator.tryValidate(5u)
                    result.isSuccess().mustBeTrue()
                    result.value shouldBe 5u
                }

                test("failure with value less than threshold") {
                    val result = validator.tryValidate(4u)
                    result.isFailure().mustBeTrue()
                    result.messages[0].constraintId shouldBe "kova.comparable.min"
                }
            }

            context("max") {
                val validator = Kova.uInt().max(10u)

                test("success with value less than threshold") {
                    val result = validator.tryValidate(9u)
                    result.isSuccess().mustBeTrue()
                    result.value shouldBe 9u
                }

                test("success with equal value") {
                    val result = validator.tryValidate(10u)
                    result.isSuccess().mustBeTrue()
                    result.value shouldBe 10u
                }

                test("failure with value greater than threshold") {
                    val result = validator.tryValidate(11u)
                    result.isFailure().mustBeTrue()
                    result.messages[0].constraintId shouldBe "kova.comparable.max"
                }
            }

            context("gt (greater than)") {
                val validator = Kova.uInt().gt(5u)

                test("success with value greater than threshold") {
                    val result = validator.tryValidate(6u)
                    result.isSuccess().mustBeTrue()
                    result.value shouldBe 6u
                }

                test("success with large value") {
                    val result = validator.tryValidate(100u)
                    result.isSuccess().mustBeTrue()
                    result.value shouldBe 100u
                }

                test("failure with equal value") {
                    val result = validator.tryValidate(5u)
                    result.isFailure().mustBeTrue()
                    result.messages[0].constraintId shouldBe "kova.comparable.gt"
                }

                test("failure with value less than threshold") {
                    val result = validator.tryValidate(4u)
                    result.isFailure().mustBeTrue()
                    result.messages[0].constraintId shouldBe "kova.comparable.gt"
                }
            }

            context("gte (greater than or equal)") {
                val validator = Kova.uInt().gte(5u)

                test("success with value greater than threshold") {
                    val result = validator.tryValidate(6u)
                    result.isSuccess().mustBeTrue()
                    result.value shouldBe 6u
                }

                test("success with equal value") {
                    val result = validator.tryValidate(5u)
                    result.isSuccess().mustBeTrue()
                    result.value shouldBe 5u
                }

                test("failure with value less than threshold") {
                    val result = validator.tryValidate(4u)
                    result.isFailure().mustBeTrue()
                    result.messages[0].constraintId shouldBe "kova.comparable.gte"
                }
            }

            context("lt (less than)") {
                val validator = Kova.uInt().lt(5u)

                test("success with value less than threshold") {
                    val result = validator.tryValidate(4u)
                    result.isSuccess().mustBeTrue()
                    result.value shouldBe 4u
                }

                test("success with zero") {
                    val result = validator.tryValidate(0u)
                    result.isSuccess().mustBeTrue()
                    result.value shouldBe 0u
                }

                test("failure with equal value") {
                    val result = validator.tryValidate(5u)
                    result.isFailure().mustBeTrue()
                    result.messages[0].constraintId shouldBe "kova.comparable.lt"
                }

                test("failure with value greater than threshold") {
                    val result = validator.tryValidate(6u)
                    result.isFailure().mustBeTrue()
                    result.messages[0].constraintId shouldBe "kova.comparable.lt"
                }
            }

            context("lte (less than or equal)") {
                val validator = Kova.uInt().lte(5u)

                test("success with value less than threshold") {
                    val result = validator.tryValidate(4u)
                    result.isSuccess().mustBeTrue()
                    result.value shouldBe 4u
                }

                test("success with equal value") {
                    val result = validator.tryValidate(5u)
                    result.isSuccess().mustBeTrue()
                    result.value shouldBe 5u
                }

                test("failure with value greater than threshold") {
                    val result = validator.tryValidate(6u)
                    result.isFailure().mustBeTrue()
                    result.messages[0].constraintId shouldBe "kova.comparable.lte"
                }
            }

            context("eq (equal)") {
                val validator = Kova.uInt().eq(5u)

                test("success with equal value") {
                    val result = validator.tryValidate(5u)
                    result.isSuccess().mustBeTrue()
                    result.value shouldBe 5u
                }

                test("failure with value greater than threshold") {
                    val result = validator.tryValidate(6u)
                    result.isFailure().mustBeTrue()
                    result.messages[0].constraintId shouldBe "kova.comparable.eq"
                }

                test("failure with value less than threshold") {
                    val result = validator.tryValidate(4u)
                    result.isFailure().mustBeTrue()
                    result.messages[0].constraintId shouldBe "kova.comparable.eq"
                }
            }

            context("notEq (not equal)") {
                val validator = Kova.uInt().notEq(5u)

                test("success with value greater than threshold") {
                    val result = validator.tryValidate(6u)
                    result.isSuccess().mustBeTrue()
                    result.value shouldBe 6u
                }

                test("success with value less than threshold") {
                    val result = validator.tryValidate(4u)
                    result.isSuccess().mustBeTrue()
                    result.value shouldBe 4u
                }

                test("failure with equal value") {
                    val result = validator.tryValidate(5u)
                    result.isFailure().mustBeTrue()
                    result.messages[0].constraintId shouldBe "kova.comparable.notEq"
                }
            }

            context("chaining multiple validators") {
                val validator =
                    Kova
                        .uInt()
                        .min(5u)
                        .max(10u)
                        .gt(6u)
                        .lte(9u)

                test("success with value 7") {
                    val result = validator.tryValidate(7u)
                    result.isSuccess().mustBeTrue()
                    result.value shouldBe 7u
                }

                test("success with value 9") {
                    val result = validator.tryValidate(9u)
                    result.isSuccess().mustBeTrue()
                    result.value shouldBe 9u
                }

                test("failure with value 5") {
                    val result = validator.tryValidate(5u)
                    result.isFailure().mustBeTrue()
                    result.messages[0].constraintId shouldBe "kova.comparable.gt"
                }

                test("failure with value 10") {
                    val result = validator.tryValidate(10u)
                    result.isFailure().mustBeTrue()
                    result.messages[0].constraintId shouldBe "kova.comparable.lte"
                }
            }
        }

        context("uLong") {
            context("min") {
                val validator = Kova.uLong().min(5uL)

                test("success") {
                    val result = validator.tryValidate(6uL)
                    result.isSuccess().mustBeTrue()
                    result.value shouldBe 6uL
                }

                test("failure") {
                    val result = validator.tryValidate(4uL)
                    result.isFailure().mustBeTrue()
                    result.messages[0].constraintId shouldBe "kova.comparable.min"
                }
            }

            context("max") {
                val validator = Kova.uLong().max(10uL)

                test("success") {
                    val result = validator.tryValidate(9uL)
                    result.isSuccess().mustBeTrue()
                    result.value shouldBe 9uL
                }

                test("failure") {
                    val result = validator.tryValidate(11uL)
                    result.isFailure().mustBeTrue()
                    result.messages[0].constraintId shouldBe "kova.comparable.max"
                }
            }
        }

        context("uByte") {
            context("min") {
                val validator = Kova.uByte().min(5u)

                test("success") {
                    val result = validator.tryValidate(6u)
                    result.isSuccess().mustBeTrue()
                    result.value shouldBe 6.toUByte()
                }

                test("failure") {
                    val result = validator.tryValidate(4u)
                    result.isFailure().mustBeTrue()
                    result.messages[0].constraintId shouldBe "kova.comparable.min"
                }
            }

            context("max") {
                val validator = Kova.uByte().max(10u)

                test("success") {
                    val result = validator.tryValidate(9u)
                    result.isSuccess().mustBeTrue()
                    result.value shouldBe 9.toUByte()
                }

                test("failure") {
                    val result = validator.tryValidate(11u)
                    result.isFailure().mustBeTrue()
                    result.messages[0].constraintId shouldBe "kova.comparable.max"
                }
            }
        }

        context("uShort") {
            context("min") {
                val validator = Kova.uShort().min(5u)

                test("success") {
                    val result = validator.tryValidate(6u)
                    result.isSuccess().mustBeTrue()
                    result.value shouldBe 6.toUShort()
                }

                test("failure") {
                    val result = validator.tryValidate(4u)
                    result.isFailure().mustBeTrue()
                    result.messages[0].constraintId shouldBe "kova.comparable.min"
                }
            }

            context("max") {
                val validator = Kova.uShort().max(10u)

                test("success") {
                    val result = validator.tryValidate(9u)
                    result.isSuccess().mustBeTrue()
                    result.value shouldBe 9.toUShort()
                }

                test("failure") {
                    val result = validator.tryValidate(11u)
                    result.isFailure().mustBeTrue()
                    result.messages[0].constraintId shouldBe "kova.comparable.max"
                }
            }
        }
    })
