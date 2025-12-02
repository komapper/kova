package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.text.MessageFormat

class MessageTest :
    FunSpec({

        test("getPattern") {
            val pattern = getPattern("kova.number.min")
            val formatted = MessageFormat.format(pattern, 10, 0)
            formatted shouldBe "Number 10 must be greater than or equal to 0"
        }

        test("resolve arguments") {
            val resource =
                Message.Resource(
                    "kova.or",
                    listOf(Message.Resource("kova.string.min", 1)),
                    Message.Resource("kova.string.max", 5),
                )
            resource.content shouldBe
                "at least one constraint must be satisfied: [[\"1\" must be at least {1} characters], \"5\" must be at most {1} characters]"
        }
    })
