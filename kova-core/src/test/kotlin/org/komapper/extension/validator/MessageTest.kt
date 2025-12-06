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
            val input = "abc"
            val vc = ValidationContext()
            val mc1 = MessageContext(ConstraintContext(input = input, "kova.string.min", validationContext = vc), args = listOf(input, 1))
            val resource1 = Message.Resource(mc1)
            val mc2 = MessageContext(ConstraintContext(input = input, "kova.string.max", validationContext = vc), args = listOf(input, 5))
            val resource2 = Message.Resource(mc2)
            val mc3 =
                MessageContext(
                    ConstraintContext(input = input, "kova.or", validationContext = vc),
                    args = listOf(listOf(resource1), resource2),
                )
            val resource3 = Message.Resource(mc3)
            resource3.content shouldBe
                "at least one constraint must be satisfied: [[\"abc\" must be at least 1 characters], \"abc\" must be at most 5 characters]"
        }
    })
