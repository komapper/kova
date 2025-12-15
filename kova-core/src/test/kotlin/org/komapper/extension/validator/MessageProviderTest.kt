package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

fun <T> ConstraintContext(input: T, constraintId: String = "") =
    ConstraintContext(input, constraintId, ValidationContext())

class MessageProviderTest :
    FunSpec({
        context("MessageProvider - no argument") {
            val input = "abc"

            test("text") {
                val provider = MessageProvider.text { "input=${input}" }
                val message = provider()(ConstraintContext(input = input))
                message.text shouldBe "input=abc"
            }

            test("resource") {
                val provider = MessageProvider.resource()
                val message = provider()(ConstraintContext(input = input, constraintId = "kova.nullable.notNull"))
                message.text shouldBe "must not be null"
            }
        }

        context("MessageProvider - 1 argument") {
            val input = "abc"

            test("text - index access") {
                val provider = MessageProvider.text { "input=${input}, a0=${this[0]}" }
                val message = provider("a0" to 10)(ConstraintContext(input = input))
                message.text shouldBe "input=abc, a0=10"
            }

            test("text - key access") {
                val provider = MessageProvider.text { "input=${input}, a0=${this["a0"]}" }
                val message = provider("a0" to 10)(ConstraintContext(input = input))
                message.text shouldBe "input=abc, a0=10"
            }

            test("text - key access - with validator") {
                val validator = Kova.list<Int>().contains(10, MessageProvider.text { "must contain ${this["element"]}" })
                val result = validator.tryValidate(listOf(1, 2, 3, 4, 5))
                result.isFailure().mustBeTrue()
                result.messages.single().text shouldBe "must contain 10"
            }

            test("resource") {
                val provider = MessageProvider.resource()
                val message = provider("infix" to input)(ConstraintContext(input = input, constraintId = "kova.charSequence.contains"))
                message.text shouldBe "must contain \"abc\""
            }
        }

        context("MessageProvider - 2 arguments") {
            test("text - index access") {
                val provider = MessageProvider.text { "input=${input}, a0=${this[0]}, a1=${this[1]}" }
                val message = provider("a0" to 10, "a1" to true)(ConstraintContext(input = "abc"))
                message.text shouldBe "input=abc, a0=10, a1=true"
            }

            test("text - key access") {
                val provider = MessageProvider.text { "input=${input}, a0=${this["a0"]}, a1=${this["a1"]}" }
                val message = provider("a0" to 10, "a1" to true)(ConstraintContext(input = "abc"))
                message.text shouldBe "input=abc, a0=10, a1=true"
            }

            test("text - key access - with validator") {
                val validator =
                    Kova.list<Int>().min(
                        10,
                        MessageProvider.text {
                            "Collection (actualSize ${this["actualSize"]}) must have at least ${this["minSize"]} elements"
                        },
                    )
                val result = validator.tryValidate(listOf(1, 2, 3, 4, 5))
                result.isFailure().mustBeTrue()
                result.messages.single().text shouldBe "Collection (actualSize 5) must have at least 10 elements"
            }

            test("resource") {
                val provider = MessageProvider.resource()
                val message =
                    provider(
                        "actualSize" to 5,
                        "minSize" to 10,
                    )(ConstraintContext(input = listOf(1, 2, 3, 4, 5), constraintId = "kova.collection.min"))
                message.text shouldBe "Collection (size 5) must have at least 10 elements"
            }
        }
    })
