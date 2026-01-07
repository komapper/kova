package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import java.util.Locale

class ComparableValidatorTest :
    FunSpec({

        beforeSpec {
            Locale.setDefault(Locale.US)
        }

        context("uInt") {
            context("ensureMin") {
                test("success with value greater than threshold") {
                    val result = tryValidate { ensureMin(6u, 5u) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { ensureMin(5u, 5u) }
                    result.shouldBeSuccess()
                }

                test("failure with value less than threshold") {
                    val result = tryValidate { ensureMin(4u, 5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.min"
                }
            }

            context("ensureMax") {
                test("success with value less than threshold") {
                    val result = tryValidate { ensureMax(9u, 10u) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { ensureMax(10u, 10u) }
                    result.shouldBeSuccess()
                }

                test("failure with value greater than threshold") {
                    val result = tryValidate { ensureMax(11u, 10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.max"
                }
            }

            context("ensureGreaterThan (greater than)") {
                test("success with value greater than threshold") {
                    val result = tryValidate { ensureGreaterThan(6u, 5u) }
                    result.shouldBeSuccess()
                }

                test("success with large value") {
                    val result = tryValidate { ensureGreaterThan(100u, 5u) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { ensureGreaterThan(5u, 5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.greaterThan"
                }

                test("failure with value less than threshold") {
                    val result = tryValidate { ensureGreaterThan(4u, 5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.greaterThan"
                }
            }

            context("ensureGreaterThanOrEqual (greater than or equal)") {
                test("success with value greater than threshold") {
                    val result = tryValidate { ensureGreaterThanOrEqual(6u, 5u) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { ensureGreaterThanOrEqual(5u, 5u) }
                    result.shouldBeSuccess()
                }

                test("failure with value less than threshold") {
                    val result = tryValidate { ensureGreaterThanOrEqual(4u, 5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.greaterThanOrEqual"
                }
            }

            context("ensureLessThan (less than)") {
                test("success with value less than threshold") {
                    val result = tryValidate { ensureLessThan(4u, 5u) }
                    result.shouldBeSuccess()
                }

                test("success with zero") {
                    val result = tryValidate { ensureLessThan(0u, 5u) }
                    result.shouldBeSuccess()
                }

                test("failure with equal value") {
                    val result = tryValidate { ensureLessThan(5u, 5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.lessThan"
                }

                test("failure with value greater than threshold") {
                    val result = tryValidate { ensureLessThan(6u, 5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.lessThan"
                }
            }

            context("ensureLessThanOrEqual (less than or equal)") {
                test("success with value less than threshold") {
                    val result = tryValidate { ensureLessThanOrEqual(4u, 5u) }
                    result.shouldBeSuccess()
                }

                test("success with equal value") {
                    val result = tryValidate { ensureLessThanOrEqual(5u, 5u) }
                    result.shouldBeSuccess()
                }

                test("failure with value greater than threshold") {
                    val result = tryValidate { ensureLessThanOrEqual(6u, 5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.lessThanOrEqual"
                }
            }

            context("ensureInRange") {
                test("success with value in range (closed range syntax)") {
                    val result = tryValidate { ensureInRange(5u, 1u..10u) }
                    result.shouldBeSuccess()
                }

                test("success with value at start of range (closed range syntax)") {
                    val result = tryValidate { ensureInRange(1u, 1u..10u) }
                    result.shouldBeSuccess()
                }

                test("success with value at end of range (closed range syntax)") {
                    val result = tryValidate { ensureInRange(10u, 1u..10u) }
                    result.shouldBeSuccess()
                }

                test("failure with value below range (closed range syntax)") {
                    val result = tryValidate { ensureInRange(0u, 1u..10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.inRange"
                }

                test("failure with value above range (closed range syntax)") {
                    val result = tryValidate { ensureInRange(11u, 1u..10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.inRange"
                }

                test("success with value in range (open-ended range syntax)") {
                    val result = tryValidate { ensureInRange(5u, 1u..<10u) }
                    result.shouldBeSuccess()
                }

                test("success with value at start of range (open-ended range syntax)") {
                    val result = tryValidate { ensureInRange(1u, 1u..<10u) }
                    result.shouldBeSuccess()
                }

                test("success with value just before end (open-ended range syntax)") {
                    val result = tryValidate { ensureInRange(9u, 1u..<10u) }
                    result.shouldBeSuccess()
                }

                test("failure with value at end of range (open-ended range syntax - exclusive)") {
                    val result = tryValidate { ensureInRange(10u, 1u..<10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.inRange"
                }

                test("failure with value below range (open-ended range syntax)") {
                    val result = tryValidate { ensureInRange(0u, 1u..<10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.inRange"
                }

                test("failure with value above range (open-ended range syntax)") {
                    val result = tryValidate { ensureInRange(11u, 1u..<10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.inRange"
                }
            }

            context("ensureInClosedRange") {
                test("success with value in range") {
                    val result = tryValidate { ensureInClosedRange(5u, 1u..10u) }
                    result.shouldBeSuccess()
                }

                test("success with value at start of range") {
                    val result = tryValidate { ensureInClosedRange(1u, 1u..10u) }
                    result.shouldBeSuccess()
                }

                test("success with value at end of range") {
                    val result = tryValidate { ensureInClosedRange(10u, 1u..10u) }
                    result.shouldBeSuccess()
                }

                test("failure with value below range") {
                    val result = tryValidate { ensureInClosedRange(0u, 1u..10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.inClosedRange"
                }

                test("failure with value above range") {
                    val result = tryValidate { ensureInClosedRange(11u, 1u..10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.inClosedRange"
                }
            }

            context("ensureInOpenEndRange") {
                test("success with value in range") {
                    val result = tryValidate { ensureInOpenEndRange(5u, 1u..<10u) }
                    result.shouldBeSuccess()
                }

                test("success with value at start of range") {
                    val result = tryValidate { ensureInOpenEndRange(1u, 1u..<10u) }
                    result.shouldBeSuccess()
                }

                test("success with value just before end of range") {
                    val result = tryValidate { ensureInOpenEndRange(9u, 1u..<10u) }
                    result.shouldBeSuccess()
                }

                test("failure with value at end of range (exclusive)") {
                    val result = tryValidate { ensureInOpenEndRange(10u, 1u..<10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.inOpenEndRange"
                }

                test("failure with value below range") {
                    val result = tryValidate { ensureInOpenEndRange(0u, 1u..<10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.inOpenEndRange"
                }

                test("failure with value above range") {
                    val result = tryValidate { ensureInOpenEndRange(11u, 1u..<10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.inOpenEndRange"
                }
            }
        }

        context("uLong") {
            context("ensureMin") {
                test("success") {
                    val result = tryValidate { ensureMin(6uL, 5uL) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { ensureMin(4uL, 5uL) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.min"
                }
            }

            context("ensureMax") {
                test("success") {
                    val result = tryValidate { ensureMax(9uL, 10uL) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { ensureMax(11uL, 10uL) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.max"
                }
            }
        }

        context("uByte") {
            context("ensureMin") {
                test("success") {
                    val result = tryValidate { ensureMin(6u, 5u) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { ensureMin(4u, 5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.min"
                }
            }

            context("ensureMax") {
                test("success") {
                    val result = tryValidate { ensureMax(9u, 10u) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { ensureMax(11u, 10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.max"
                }
            }
        }

        context("uShort") {
            context("ensureMin") {
                test("success") {
                    val result = tryValidate { ensureMin(6u, 5u) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { ensureMin(4u, 5u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.min"
                }
            }

            context("ensureMax") {
                test("success") {
                    val result = tryValidate { ensureMax(9u, 10u) }
                    result.shouldBeSuccess()
                }

                test("failure") {
                    val result = tryValidate { ensureMax(11u, 10u) }
                    result.shouldBeFailure()
                    result.messages[0].constraintId shouldBe "kova.comparable.max"
                }
            }
        }
    })
