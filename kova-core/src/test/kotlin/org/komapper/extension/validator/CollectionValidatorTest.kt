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
                val result = tryValidate { ensureSize(listOf("1", "2"), 2) }
                result.shouldBeSuccess()
            }

            test("failure with too few elements") {
                val result = tryValidate { ensureSize(listOf("1"), 2) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.collection.size"
            }

            test("failure with too many elements") {
                val result = tryValidate { ensureSize(listOf("1", "2", "3"), 2) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.collection.size"
            }
        }

        context("ensureMinSize and ensureMaxSize") {
            test("ensureMinSize success") {
                val result = tryValidate { ensureMinSize(listOf("1", "2", "3"), 2) }
                result.shouldBeSuccess()
            }

            test("ensureMinSize failure") {
                val result = tryValidate { ensureMinSize(listOf("1"), 2) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.collection.minSize"
            }

            test("ensureMaxSize success") {
                val result = tryValidate { ensureMaxSize(listOf("1", "2"), 3) }
                result.shouldBeSuccess()
            }

            test("ensureMaxSize failure") {
                val result = tryValidate { ensureMaxSize(listOf("1", "2", "3", "4"), 3) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.collection.maxSize"
            }

            test("multiple constraints") {
                val result =
                    tryValidate {
                        ensureMinSize(listOf("1"), 2)
                        ensureMinSize(listOf("1"), 3)
                    }
                result.shouldBeFailure()
                result.messages.size shouldBe 2
                result.messages[0].constraintId shouldBe "kova.collection.minSize"
                result.messages[1].constraintId shouldBe "kova.collection.minSize"
            }
        }
    })
