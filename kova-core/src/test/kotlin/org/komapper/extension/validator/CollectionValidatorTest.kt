package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import java.util.Locale

class CollectionValidatorTest :
    FunSpec({

        beforeSpec {
            Locale.setDefault(Locale.US)
        }

        context("ensureSize") {
            test("success") {
                val result = tryValidate { listOf("1", "2").ensureSize(2) }
                result.shouldBeSuccess()
            }

            test("failure with too few elements") {
                val result = tryValidate { listOf("1").ensureSize(2) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.collection.size"
            }

            test("failure with too many elements") {
                val result = tryValidate { listOf("1", "2", "3").ensureSize(2) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.collection.size"
            }
        }

        context("ensureMinSize and ensureMaxSize") {
            test("ensureMinSize success") {
                val result = tryValidate { listOf("1", "2", "3").ensureMinSize(2) }
                result.shouldBeSuccess()
            }

            test("ensureMinSize failure") {
                val result = tryValidate { listOf("1").ensureMinSize(2) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.collection.minSize"
            }

            test("ensureMaxSize success") {
                val result = tryValidate { listOf("1", "2").ensureMaxSize(3) }
                result.shouldBeSuccess()
            }

            test("ensureMaxSize failure") {
                val result = tryValidate { listOf("1", "2", "3", "4").ensureMaxSize(3) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.collection.maxSize"
            }

            test("multiple constraints") {
                val result =
                    tryValidate {
                        listOf("1").ensureMinSize(2)
                        listOf("1").ensureMinSize(3)
                    }
                result.shouldBeFailure()
                result.messages.size shouldBe 2
                result.messages[0].constraintId shouldBe "kova.collection.minSize"
                result.messages[1].constraintId shouldBe "kova.collection.minSize"
            }
        }

        context("ensureSizeInRange") {
            test("success with closed range") {
                val result = tryValidate { listOf("1", "2", "3").ensureSizeInRange(2..5) }
                result.shouldBeSuccess()
            }

            test("success at lower bound of closed range") {
                val result = tryValidate { listOf("1", "2").ensureSizeInRange(2..5) }
                result.shouldBeSuccess()
            }

            test("success at upper bound of closed range") {
                val result = tryValidate { listOf("1", "2", "3", "4", "5").ensureSizeInRange(2..5) }
                result.shouldBeSuccess()
            }

            test("success with open-ended range") {
                val result = tryValidate { listOf("1", "2", "3").ensureSizeInRange(2..<5) }
                result.shouldBeSuccess()
            }

            test("success at lower bound of open-ended range") {
                val result = tryValidate { listOf("1", "2").ensureSizeInRange(2..<5) }
                result.shouldBeSuccess()
            }

            test("failure below range") {
                val result = tryValidate { listOf("1").ensureSizeInRange(2..5) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.collection.sizeInRange"
            }

            test("failure above closed range") {
                val result = tryValidate { listOf("1", "2", "3", "4", "5", "6").ensureSizeInRange(2..5) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.collection.sizeInRange"
            }

            test("failure at upper bound of open-ended range") {
                val result = tryValidate { listOf("1", "2", "3", "4", "5").ensureSizeInRange(2..<5) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.collection.sizeInRange"
            }

            test("success with custom message") {
                val result =
                    tryValidate {
                        listOf("1", "2", "3").ensureSizeInRange(
                            2..5,
                        ) { text("Custom message") }
                    }
                result.shouldBeSuccess()
            }

            test("failure with custom message") {
                val result =
                    tryValidate {
                        listOf("1").ensureSizeInRange(
                            2..5,
                        ) { text("Custom message") }
                    }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].text shouldBe "Custom message"
            }
        }
    })
