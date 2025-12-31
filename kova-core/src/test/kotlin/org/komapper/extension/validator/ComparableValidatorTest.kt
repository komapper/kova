package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import java.util.Locale

class ComparableValidatorTest :
    FunSpec({

        beforeSpec {
            Locale.setDefault(Locale.US)
        }

        context("uInt") {
            context("minValue") {
                test("success with value greater than threshold") {
                    val result = tryValidate { minValue(6u, 5u) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { minValue(5u, 5u) }
                    result.shouldBeSuccess()
                }

                test("failure with value less than threshold") {
                    val result = tryValidate { minValue(4u, 5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.minValue"
                }
            }

            context("maxValue") {
                test("success with value less than threshold") {
                    val result = tryValidate { maxValue(9u, 10u) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { maxValue(10u, 10u) }
                    result.shouldBeSuccess()
                }

                test("failure with value greater than threshold") {
                    val result = tryValidate { maxValue(11u, 10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.maxValue"
                }
            }

            context("gtValue (greater than)") {
                test("success with value greater than threshold") {
                    val result = tryValidate { gtValue(6u, 5u) }
                    result.shouldBeSuccess()
                }

                test("success with large value") {
                    val result = tryValidate { gtValue(100u, 5u) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { gtValue(5u, 5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.gtValue"
                }

                test("failure with value less than threshold") {
                    val result = tryValidate { gtValue(4u, 5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.gtValue"
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

            context("ltValue (less than)") {
                test("success with value less than threshold") {
                    val result = tryValidate { ltValue(4u, 5u) }
                    result.shouldBeSuccess()
                }

                test("success with zero") {
                    val result = tryValidate { ltValue(0u, 5u) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { ltValue(5u, 5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.ltValue"
                }

                test("failure with value greater than threshold") {
                    val result = tryValidate { ltValue(6u, 5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.ltValue"
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
            context("minValue") {
                test("success") {
                    val result = tryValidate { minValue(6uL, 5uL) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { minValue(4uL, 5uL) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.minValue"
                }
            }

            context("maxValue") {
                test("success") {
                    val result = tryValidate { maxValue(9uL, 10uL) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { maxValue(11uL, 10uL) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.maxValue"
                }
            }
        }

        context("uByte") {
            context("minValue") {
                test("success") {
                    val result = tryValidate { minValue(6u, 5u) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { minValue(4u, 5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.minValue"
                }
            }

            context("maxValue") {
                test("success") {
                    val result = tryValidate { maxValue(9u, 10u) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { maxValue(11u, 10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.maxValue"
                }
            }
        }

        context("uShort") {
            context("minValue") {
                test("success") {
                    val result = tryValidate { minValue(6u, 5u) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { minValue(4u, 5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.minValue"
                }
            }

            context("maxValue") {
                test("success") {
                    val result = tryValidate { maxValue(9u, 10u) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { maxValue(11u, 10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.maxValue"
                }
            }
        }
    })
