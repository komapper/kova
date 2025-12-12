package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.text.MessageFormat

class MessageTest :
    FunSpec({

        test("getPattern") {
            val pattern = getPattern("kova.comparable.min")
            val formatted = MessageFormat.format(pattern, 0)
            formatted shouldBe "must be greater than or equal to 0"
        }

        test("resolve arguments") {
            val input = "abc"
            val vc = ValidationContext()

            val cc1 = vc.createConstraintContext(input, "kova.charSequence.min")
            val mc1 = cc1.createMessageContext(listOf("length" to 1))
            val resource1 = Message.Resource(mc1)

            val cc2 = vc.createConstraintContext(input, "kova.charSequence.max")
            val mc2 = cc2.createMessageContext(listOf("length" to 5))
            val resource2 = Message.Resource(mc2)

            val cc3 = vc.createConstraintContext(input, "kova.or")
            val mc3 =
                cc3.createMessageContext(listOf("first" to listOf(resource1), "second" to resource2))
            val resource3 = Message.Resource(mc3)

            resource3.text shouldBe
                "at least one constraint must be satisfied: [[must be at least 1 characters], must be at most 5 characters]"
        }

        test("toString: string") {
            val min = Kova.string().min(5)

            val result = min.tryValidate("abc")
            result.isFailure().mustBeTrue()
            result.messages.size shouldBe 1
            result.messages[0].toString() shouldBe
                "Message(constraintId=kova.charSequence.min, text='must be at least 5 characters', root=, path=, input=abc)"
        }

        test("toString: object") {
            data class Person(
                val name: String,
            )

            val personSchema =
                object : ObjectSchema<Person>({
                    Person::name { it.min(5) }
                }) {}

            val result = personSchema.tryValidate(Person("abc"))
            result.isFailure().mustBeTrue()
            result.messages.size shouldBe 1
            result.messages[0].toString() shouldBe
                "Message(constraintId=kova.charSequence.min, text='must be at least 5 characters', root=Person, path=name, input=abc)"
        }
    })
