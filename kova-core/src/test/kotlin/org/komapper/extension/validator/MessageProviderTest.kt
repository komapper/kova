package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec

class MessageProviderTest :
    FunSpec({
        context("MessageProvider") {
            val input = "abc"

            test("text") {
                val message = with(Validation()) { text("input=$input") }
                message.text shouldBe "input=abc"
            }

            test("resource") {
                val message = with(Validation()) { "kova.nullable.notNull".resource }
                message.text shouldBe "must not be null"
            }
        }
    })
