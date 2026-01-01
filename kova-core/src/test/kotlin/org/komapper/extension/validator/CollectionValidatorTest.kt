package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import java.util.Locale

class CollectionValidatorTest :
    FunSpec({

        beforeSpec {
            Locale.setDefault(Locale.US)
        }

        context("size") {
            test("success") {
                val result = tryValidate { size(listOf("1", "2"), 2) }
                result.shouldBeSuccess()
            }

            test("failure with too few elements") {
                val result = tryValidate { size(listOf("1"), 2) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.collection.size"
            }

            test("failure with too many elements") {
                val result = tryValidate { size(listOf("1", "2", "3"), 2) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.collection.size"
            }
        }

        context("minSize and maxSize") {
            test("minSize success") {
                val result = tryValidate { minSize(listOf("1", "2", "3"), 2) }
                result.shouldBeSuccess()
            }

            test("minSize failure") {
                val result = tryValidate { minSize(listOf("1"), 2) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.collection.minSize"
            }

            test("maxSize success") {
                val result = tryValidate { maxSize(listOf("1", "2"), 3) }
                result.shouldBeSuccess()
            }

            test("maxSize failure") {
                val result = tryValidate { maxSize(listOf("1", "2", "3", "4"), 3) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.collection.maxSize"
            }

            test("multiple constraints") {
                val result =
                    tryValidate {
                        minSize(listOf("1"), 2)
                        minSize(listOf("1"), 3)
                    }
                result.shouldBeFailure()
                result.messages.size shouldBe 2
                result.messages[0].constraintId shouldBe "kova.collection.minSize"
                result.messages[1].constraintId shouldBe "kova.collection.minSize"
            }
        }
    })
