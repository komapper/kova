package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

fun <T> ConstraintContext(input: T, constraintId: String = "") =
    ConstraintContext(input, constraintId, ValidationContext())

class MessageProviderTest :
    FunSpec({
        context("MessageProvider") {
            val input = "abc"

            test("text") {
                val message = ConstraintContext(input = input).text("input=${input}")
                message.text shouldBe "input=abc"
            }

            test("resource") {
                val message = Message.Resource(ConstraintContext(input = input, constraintId = "kova.nullable.notNull"))
                message.text shouldBe "must not be null"
            }
        }
    })
