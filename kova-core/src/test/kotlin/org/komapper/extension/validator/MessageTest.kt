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
            val resource1 = Message.Resource(cc1, 1)

            val cc2 = vc.createConstraintContext(input, "kova.charSequence.max")
            val resource2 = Message.Resource(cc2, 5)

            val cc3 = vc.createConstraintContext(input, "kova.or")
            val resource3 = Message.Resource(cc3, listOf(resource1), resource2)

            resource3.text shouldBe
                "at least one constraint must be satisfied: [[must be at least 1 characters], must be at most 5 characters]"
        }

        test("toString: string") {
            val min = Kova.string().min(5)

            val result = min.tryValidate("abc")
            result.isFailure().mustBeTrue()
            result.messages.size shouldBe 1
            result.messages[0].toString() shouldBe
                "Message(constraintId=kova.charSequence.min, text='must be at least 5 characters', root=, path=, input=abc, args=[5])"
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
                "Message(constraintId=kova.charSequence.min, text='must be at least 5 characters', root=Person, path=name, input=abc, args=[5])"
        }
    })
