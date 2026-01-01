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

            context("gtEqValue (greater than or equal)") {
                test("success with value greater than threshold") {
                    val result = tryValidate { gtEqValue(6u, 5u) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { gtEqValue(5u, 5u) }
                    result.shouldBeSuccess()
                }

                test("failure with value less than threshold") {
                    val result = tryValidate { gtEqValue(4u, 5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.gtEqValue"
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

            context("ltEqValue (less than or equal)") {
                test("success with value less than threshold") {
                    val result = tryValidate { ltEqValue(4u, 5u) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { ltEqValue(5u, 5u) }
                    result.shouldBeSuccess()
                }

                test("failure with value greater than threshold") {
                    val result = tryValidate { ltEqValue(6u, 5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.ltEqValue"
                }
            }

            context("inRange") {
                test("success with value in range (closed range syntax)") {
                    val result = tryValidate { inRange(5u, 1u..10u) }
                    result.shouldBeSuccess()
                }

                test("success with value at start of range (closed range syntax)") {
                    val result = tryValidate { inRange(1u, 1u..10u) }
                    result.shouldBeSuccess()
                }

                test("success with value at end of range (closed range syntax)") {
                    val result = tryValidate { inRange(10u, 1u..10u) }
                    result.shouldBeSuccess()
                }

                test("failure with value below range (closed range syntax)") {
                    val result = tryValidate { inRange(0u, 1u..10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.inRange"
                }

                test("failure with value above range (closed range syntax)") {
                    val result = tryValidate { inRange(11u, 1u..10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.inRange"
                }

                test("success with value in range (open-ended range syntax)") {
                    val result = tryValidate { inRange(5u, 1u..<10u) }
                    result.shouldBeSuccess()
                }

                test("success with value at start of range (open-ended range syntax)") {
                    val result = tryValidate { inRange(1u, 1u..<10u) }
                    result.shouldBeSuccess()
                }

                test("success with value just before end (open-ended range syntax)") {
                    val result = tryValidate { inRange(9u, 1u..<10u) }
                    result.shouldBeSuccess()
                }

                test("failure with value at end of range (open-ended range syntax - exclusive)") {
                    val result = tryValidate { inRange(10u, 1u..<10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.inRange"
                }

                test("failure with value below range (open-ended range syntax)") {
                    val result = tryValidate { inRange(0u, 1u..<10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.inRange"
                }

                test("failure with value above range (open-ended range syntax)") {
                    val result = tryValidate { inRange(11u, 1u..<10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.inRange"
                }
            }

            context("inClosedRange") {
                test("success with value in range") {
                    val result = tryValidate { inClosedRange(5u, 1u..10u) }
                    result.shouldBeSuccess()
                }

                test("success with value at start of range") {
                    val result = tryValidate { inClosedRange(1u, 1u..10u) }
                    result.shouldBeSuccess()
                }

                test("success with value at end of range") {
                    val result = tryValidate { inClosedRange(10u, 1u..10u) }
                    result.shouldBeSuccess()
                }

                test("failure with value below range") {
                    val result = tryValidate { inClosedRange(0u, 1u..10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.inClosedRange"
                }

                test("failure with value above range") {
                    val result = tryValidate { inClosedRange(11u, 1u..10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.inClosedRange"
                }
            }

            context("inOpenEndRange") {
                test("success with value in range") {
                    val result = tryValidate { inOpenEndRange(5u, 1u..<10u) }
                    result.shouldBeSuccess()
                }

                test("success with value at start of range") {
                    val result = tryValidate { inOpenEndRange(1u, 1u..<10u) }
                    result.shouldBeSuccess()
                }

                test("success with value just before end of range") {
                    val result = tryValidate { inOpenEndRange(9u, 1u..<10u) }
                    result.shouldBeSuccess()
                }

                test("failure with value at end of range (exclusive)") {
                    val result = tryValidate { inOpenEndRange(10u, 1u..<10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.inOpenEndRange"
                }

                test("failure with value below range") {
                    val result = tryValidate { inOpenEndRange(0u, 1u..<10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.inOpenEndRange"
                }

                test("failure with value above range") {
                    val result = tryValidate { inOpenEndRange(11u, 1u..<10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.inOpenEndRange"
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
