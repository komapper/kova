package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MessageProviderTest :
    FunSpec({
        context("MessageProvider") {
            val input = "abc"

            test("text") {
                val message = ValidationContext().text("input=$input")
                message.text shouldBe "input=abc"
            }

            test("resource") {
                val message = with(ValidationContext()) { "kova.nullable.notNull".resource }
                message.text shouldBe "must not be null"
            }
        }
    })
