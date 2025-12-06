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

            val cc1 = vc.createConstraintContext(input, "kova.string.min")
            val mc1 = cc1.createMessageContext(listOf(input, 1))
            val resource1 = Message.Resource(mc1)

            val cc2 = vc.createConstraintContext(input, "kova.string.max")
            val mc2 = cc2.createMessageContext(listOf(input, 5))
            val resource2 = Message.Resource(mc2)

            val cc3 = vc.createConstraintContext(input, "kova.or")
            val mc3 =
                cc3.createMessageContext(listOf(listOf(resource1), resource2))
            val resource3 = Message.Resource(mc3)

            resource3.content shouldBe
                "at least one constraint must be satisfied: [[\"abc\" must be at least 1 characters], \"abc\" must be at most 5 characters]"
        }
    })
