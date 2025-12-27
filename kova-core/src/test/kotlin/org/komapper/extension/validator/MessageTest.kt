package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import java.text.MessageFormat

class MessageTest :
    FunSpec({

        test("getPattern") {
            val pattern = getPattern("kova.comparable.min")
            val formatted = MessageFormat.format(pattern, 0)
            formatted shouldBe "must be greater than or equal to 0"
        }

        test("resolve arguments") {
            with(Validation()) {
                val resource1 = "kova.charSequence.min".resource(1)
                val resource2 = "kova.charSequence.max".resource(5)
                val resource3 = "kova.or".resource(listOf(resource1), resource2)

                resource3.text shouldBe
                    "at least one constraint must be satisfied: [[must be at least 1 characters], must be at most 5 characters]"
            }
        }

        test("toString: string") {
            val result = tryValidate { this.min("abc", 5) }
            result.shouldBeFailure()
            result.messages.size shouldBe 1
            result.messages[0].toString() shouldBe
                "Message(constraintId=kova.charSequence.min, text='must be at least 5 characters', root=, path=, args=[5])"
        }

        test("toString: object") {
            data class Person(
                val name: String,
            )

            fun Validation.validate(person: Person) = person.schema { person::name { min(it, 5) } }

            val result = tryValidate { validate(Person("abc")) }
            result.shouldBeFailure()
            result.messages.size shouldBe 1
            result.messages[0].toString() shouldBe
                "Message(constraintId=kova.charSequence.min, text='must be at least 5 characters', root=Person, path=name, args=[5])"
        }
    })
