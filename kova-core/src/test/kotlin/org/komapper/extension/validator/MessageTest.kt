package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeInstanceOf
import java.text.MessageFormat
import java.util.Locale

class MessageTest :
    FunSpec({

        beforeSpec {
            Locale.setDefault(Locale.US)
        }

        test("getPattern") {
            val pattern = getPattern("kova.comparable.atLeast")
            val formatted = MessageFormat.format(pattern, 0)
            formatted shouldBe "must be greater than or equal to 0"
        }

        test("resolve arguments") {
            context(Validation()) {
                val resource1 = "kova.charSequence.lengthAtLeast".resource(1)
                val resource2 = "kova.charSequence.lengthAtMost".resource(5)
                val resource3 = "kova.or".resource(listOf(resource1), resource2)

                resource3.text shouldBe
                    "at least one constraint must be satisfied: [[must be at least 1 characters], must be at most 5 characters]"
                resource3.descendants.size shouldBe 2
                resource3.descendants[0] shouldBe resource1
                resource3.descendants[1] shouldBe resource2
            }
        }

        test("toString: string") {
            val result = tryValidate { "abc".ensureLengthAtLeast(5) }
            result.shouldBeFailure()
            result.messages.size shouldBe 1
            result.messages[0].toString() shouldBe
                "Message(constraintId=kova.charSequence.lengthAtLeast, text='must be at least 5 characters', root=, path=, input=abc, args=[5])"
        }

        test("toString: object") {
            data class Person(
                val name: String,
            )

            context(_: Validation)
            fun validate(person: Person) = person.schema { person::name { it.ensureLengthAtLeast(5) } }

            val result = tryValidate { validate(Person("abc")) }
            result.shouldBeFailure()
            result.messages.size shouldBe 1
            result.messages[0].toString() shouldBe
                "Message(constraintId=kova.charSequence.lengthAtLeast, text='must be at least 5 characters', root=Person, path=name, input=abc, args=[5])"
        }

        context("withMessage - text") {
            context(_: Validation)
            fun validate(string: String) =
                withMessage({ messages -> text("Invalid: consolidates messages=(${messages.joinToString { it.text }})") }) {
                    string.ensureUppercase()
                    string.ensureLengthAtLeast(3)
                    Unit
                }

            test("success") {
                val result = tryValidate { validate("ABCDE") }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { validate("ab") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.shouldBeInstanceOf<Message.Text>()
                message.text shouldBe "Invalid: consolidates messages=(must be uppercase, must be at least 3 characters)"
                message.root shouldBe ""
                message.path.fullName shouldBe ""
            }
        }

        context("withMessage - resource") {
            context(_: Validation)
            fun validate(string: String) =
                withMessage {
                    string.ensureUppercase()
                    string.ensureLengthAtLeast(3)
                    Unit
                }

            test("success") {
                val result = tryValidate { validate("ABCDE") }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { validate("ab") }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.constraintId shouldBe "kova.withMessage"
                message.text shouldBe "invalid: [must be uppercase, must be at least 3 characters]"
                message.root shouldBe ""
                message.path.fullName shouldBe ""
            }
        }

        context("withMessage - text in schema") {
            data class User(
                val id: Int,
                val name: String,
            )

            context(_: Validation)
            fun validate(user: User) =
                user.schema {
                    user::id { }
                    user::name {
                        withMessage({ text("Must be uppercase and at least 3 characters long") }) {
                            it.ensureUppercase()
                            it.ensureLengthAtLeast(3)
                        }
                    }
                }

            test("success") {
                val result = tryValidate { validate(User(1, "ABCDE")) }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { validate(User(1, "ab")) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.shouldBeInstanceOf<Message.Text>()
                message.text shouldBe "Must be uppercase and at least 3 characters long"
                message.root shouldBe "User"
                message.path.fullName shouldBe "name"
            }
        }
    })
