package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MessageProviderTest :
    FunSpec({
        context("MessageProvider - no argument") {
            val input = "abc"

            test("text") {
                val provider = Message.text<String> { "input=${it.input}" }
                val message = provider(ConstraintContext(input = input))
                message.text shouldBe "input=abc"
            }

            test("resource") {
                val provider = Message.resource<String>()
                val message = provider(ConstraintContext(input = input, constraintId = "kova.nullable.notNull"))
                message.text shouldBe "Value must not be null"
            }
        }

        context("MessageProvider - 1 argument") {
            val input = "abc"

            test("text") {
                val provider = Message.text<String> { "input=${it.input}, a0=${it[0]}" }
                val message = provider(ConstraintContext(input = input), 10)
                message.text shouldBe "input=abc, a0=10"
            }

            test("resource") {
                val provider = Message.resource<String>()
                val message = provider(ConstraintContext(input = input, constraintId = "kova.string.email"), input)
                message.text shouldBe "\"abc\" must be a valid email address"
            }
        }

        context("MessageProvider - 2 arguments") {
            val input = "abc"

            test("text") {
                val provider = Message.text<String> { "input=${it.input}, a0=${it[0]}, a1=${it[1]}" }
                val message = provider(ConstraintContext(input = "abc"), 10, true)
                message.text shouldBe "input=abc, a0=10, a1=true"
            }

            test("resource") {
                val provider = Message.resource<String>()
                val message = provider(ConstraintContext(input = input, constraintId = "kova.string.length"), input, 1)
                message.text shouldBe "\"abc\" must be exactly 1 characters"
            }
        }
    })
