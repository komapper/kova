package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MessageProviderTest :
    FunSpec({
        context("MessageProvider - no argument") {
            val input = "abc"

            test("text") {
                val provider = Message.text<String> { context, _ -> "input=${context.input}" }
                val message = provider(ConstraintContext(input = input))
                message.content shouldBe "input=abc"
            }

            test("resource") {
                val provider = Message.resource<String>()
                val message = provider(ConstraintContext(input = input, constraintId = "kova.nullable.notNull"))
                message.content shouldBe "Value must not be null"
            }
        }

        context("MessageProvider - 1 argument") {
            val input = "abc"

            test("text") {
                val provider = Message.text<String> { context, args -> "input=${context.input}, a0=${args[0]}" }
                val message = provider(ConstraintContext(input = input), 10)
                message.content shouldBe "input=abc, a0=10"
            }

            test("resource") {
                val provider = Message.resource<String>()
                val message = provider(ConstraintContext(input = input, constraintId = "kova.string.email"), input)
                message.content shouldBe "\"abc\" must be a valid email address"
            }
        }

        context("MessageProvider - 2 arguments") {
            val input = "abc"

            test("text") {
                val provider = Message.text<String> { context, args -> "input=${context.input}, a0=${args[0]}, a1=${args[1]}" }
                val message = provider(ConstraintContext(input = "abc"), 10, true)
                message.content shouldBe "input=abc, a0=10, a1=true"
            }

            test("resource") {
                val provider = Message.resource<String>()
                val message = provider(ConstraintContext(input = input, constraintId = "kova.string.length"), input, 1)
                message.content shouldBe "\"abc\" must be exactly 1 characters"
            }
        }
    })
