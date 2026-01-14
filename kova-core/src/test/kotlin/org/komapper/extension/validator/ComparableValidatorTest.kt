package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import java.util.Locale

class ComparableValidatorTest :
    FunSpec({

        beforeSpec {
            Locale.setDefault(Locale.US)
        }

        context("uInt") {
            context("ensureAtLeast") {
                test("success with value greater than threshold") {
                    val result = tryValidate { 6u.ensureAtLeast(5u) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { 5u.ensureAtLeast(5u) }
                    result.shouldBeSuccess()
                }

                test("failure with value less than threshold") {
                    val result = tryValidate { 4u.ensureAtLeast(5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.atLeast"
                }
            }

            context("ensureAtMost") {
                test("success with value less than threshold") {
                    val result = tryValidate { 9u.ensureAtMost(10u) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { 10u.ensureAtMost(10u) }
                    result.shouldBeSuccess()
                }

                test("failure with value greater than threshold") {
                    val result = tryValidate { 11u.ensureAtMost(10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.atMost"
                }
            }

            context("ensureGreaterThan (greater than)") {
                test("success with value greater than threshold") {
                    val result = tryValidate { 6u.ensureGreaterThan(5u) }
                    result.shouldBeSuccess()
                }

                test("success with large value") {
                    val result = tryValidate { 100u.ensureGreaterThan(5u) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { 5u.ensureGreaterThan(5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.greaterThan"
                }

                test("failure with value less than threshold") {
                    val result = tryValidate { 4u.ensureGreaterThan(5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.greaterThan"
                }
            }

            context("ensureLessThan (less than)") {
                test("success with value less than threshold") {
                    val result = tryValidate { 4u.ensureLessThan(5u) }
                    result.shouldBeSuccess()
                }

                test("success with zero") {
                    val result = tryValidate { 0u.ensureLessThan(5u) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { 5u.ensureLessThan(5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.lessThan"
                }

                test("failure with value greater than threshold") {
                    val result = tryValidate { 6u.ensureLessThan(5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.lessThan"
                }
            }

            context("ensureInRange") {
                test("success with value in range (closed range syntax)") {
                    val result = tryValidate { 5u.ensureInRange(1u..10u) }
                    result.shouldBeSuccess()
                }

                test("success with value at start of range (closed range syntax)") {
                    val result = tryValidate { 1u.ensureInRange(1u..10u) }
                    result.shouldBeSuccess()
                }

                test("success with value at end of range (closed range syntax)") {
                    val result = tryValidate { 10u.ensureInRange(1u..10u) }
                    result.shouldBeSuccess()
                }

                test("failure with value below range (closed range syntax)") {
                    val result = tryValidate { 0u.ensureInRange(1u..10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.inRange"
                }

                test("failure with value above range (closed range syntax)") {
                    val result = tryValidate { 11u.ensureInRange(1u..10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.inRange"
                }

                test("success with value in range (open-ended range syntax)") {
                    val result = tryValidate { 5u.ensureInRange(1u..<10u) }
                    result.shouldBeSuccess()
                }

                test("success with value at start of range (open-ended range syntax)") {
                    val result = tryValidate { 1u.ensureInRange(1u..<10u) }
                    result.shouldBeSuccess()
                }

                test("success with value just before end (open-ended range syntax)") {
                    val result = tryValidate { 9u.ensureInRange(1u..<10u) }
                    result.shouldBeSuccess()
                }

                test("failure with value at end of range (open-ended range syntax - exclusive)") {
                    val result = tryValidate { 10u.ensureInRange(1u..<10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.inRange"
                }

                test("failure with value below range (open-ended range syntax)") {
                    val result = tryValidate { 0u.ensureInRange(1u..<10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.inRange"
                }

                test("failure with value above range (open-ended range syntax)") {
                    val result = tryValidate { 11u.ensureInRange(1u..<10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.inRange"
                }
            }

            context("ensureInClosedRange") {
                test("success with value in range") {
                    val result = tryValidate { 5u.ensureInClosedRange(1u..10u) }
                    result.shouldBeSuccess()
                }

                test("success with value at start of range") {
                    val result = tryValidate { 1u.ensureInClosedRange(1u..10u) }
                    result.shouldBeSuccess()
                }

                test("success with value at end of range") {
                    val result = tryValidate { 10u.ensureInClosedRange(1u..10u) }
                    result.shouldBeSuccess()
                }

                test("failure with value below range") {
                    val result = tryValidate { 0u.ensureInClosedRange(1u..10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.inClosedRange"
                }

                test("failure with value above range") {
                    val result = tryValidate { 11u.ensureInClosedRange(1u..10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.inClosedRange"
                }
            }

            context("ensureInOpenEndRange") {
                test("success with value in range") {
                    val result = tryValidate { 5u.ensureInOpenEndRange(1u..<10u) }
                    result.shouldBeSuccess()
                }

                test("success with value at start of range") {
                    val result = tryValidate { 1u.ensureInOpenEndRange(1u..<10u) }
                    result.shouldBeSuccess()
                }

                test("success with value just before end of range") {
                    val result = tryValidate { 9u.ensureInOpenEndRange(1u..<10u) }
                    result.shouldBeSuccess()
                }

                test("failure with value at end of range (exclusive)") {
                    val result = tryValidate { 10u.ensureInOpenEndRange(1u..<10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.inOpenEndRange"
                }

                test("failure with value below range") {
                    val result = tryValidate { 0u.ensureInOpenEndRange(1u..<10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.inOpenEndRange"
                }

                test("failure with value above range") {
                    val result = tryValidate { 11u.ensureInOpenEndRange(1u..<10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.inOpenEndRange"
                }
            }
        }

        context("uLong") {
            context("ensureAtLeast") {
                test("success") {
                    val result = tryValidate { 6uL.ensureAtLeast(5uL) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { 4uL.ensureAtLeast(5uL) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.atLeast"
                }
            }

            context("ensureAtMost") {
                test("success") {
                    val result = tryValidate { 9uL.ensureAtMost(10uL) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { 11uL.ensureAtMost(10uL) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.atMost"
                }
            }
        }

        context("uByte") {
            context("ensureAtLeast") {
                test("success") {
                    val result = tryValidate { 6u.ensureAtLeast(5u) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { 4u.ensureAtLeast(5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.atLeast"
                }
            }

            context("ensureAtMost") {
                test("success") {
                    val result = tryValidate { 9u.ensureAtMost(10u) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { 11u.ensureAtMost(10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.atMost"
                }
            }
        }

        context("uShort") {
            context("ensureAtLeast") {
                test("success") {
                    val result = tryValidate { 6u.ensureAtLeast(5u) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { 4u.ensureAtLeast(5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.atLeast"
                }
            }

            context("ensureAtMost") {
                test("success") {
                    val result = tryValidate { 9u.ensureAtMost(10u) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { 11u.ensureAtMost(10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.atMost"
                }
            }
        }
    })
