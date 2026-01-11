package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import java.util.Locale

class MessageProviderTest :
    FunSpec({

        beforeSpec {
            Locale.setDefault(Locale.US)
        }

        context("MessageProvider") {
            val input = "abc"

            test("text") {
                val message = with(Validation()) { text("input=$input") }
                message.text shouldBe "input=abc"
            }

            test("resource") {
                val message = context(Validation()) { "kova.nullable.notNull".resource }
                message.text shouldBe "must not be null"
            }
        }
    })
