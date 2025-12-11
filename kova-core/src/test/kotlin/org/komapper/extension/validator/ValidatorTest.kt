package org.komapper.extension.validator

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class ValidatorTest :
    FunSpec({

        context("validate") {
            val validator = Kova.int().min(1).max(10)

            test("success") {
                val result = validator.validate(5)
                result shouldBe 5
            }

            test("failure") {
                val ex =
                    shouldThrow<ValidationException> {
                        validator.validate(0)
                    }
                ex.messages.size shouldBe 1
                ex.messages[0].constraintId shouldBe "kova.comparable.min"
            }
        }

        context("plus") {
            val a = Kova.int().max(2)
            val b = Kova.int().max(3)
            val c = a + b

            test("success") {
                val result = c.tryValidate(1)
                result.isSuccess().mustBeTrue()
            }
            test("failure") {
                val result = c.tryValidate(4)
                result.isFailure().mustBeTrue()
            }
        }

        context("and") {
            val and = Kova.int().min(2).and(Kova.int().max(3))

            test("success") {
                val result = and.tryValidate(2)
                result.isSuccess().mustBeTrue()
            }
            test("failure") {
                val result = and.tryValidate(4)
                result.isFailure().mustBeTrue()
            }
        }

        context("and with lambda") {
            val and = Kova.int().min(2).and { it.max(3) }

            test("success") {
                val result = and.tryValidate(2)
                result.isSuccess().mustBeTrue()
            }
            test("failure") {
                val result = and.tryValidate(4)
                result.isFailure().mustBeTrue()
            }
        }

        context("or: 2") {
            val length2 = Kova.string().length(2)
            val length5 = Kova.string().length(5)
            val length2or5 = length2 or length5

            test("success with length 2") {
                val result = length2or5.tryValidate("ab")
                result.isSuccess().mustBeTrue()
            }
            test("success with length 5") {
                val result = length2or5.tryValidate("abcde")
                result.isSuccess().mustBeTrue()
            }
            test("failure with length 3") {
                val result = length2or5.tryValidate("abc")
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].let {
                    it.constraintId shouldBe "kova.or"
                    it.shouldBeInstanceOf<Message.Or>()
                    it.first.messages[0].constraintId shouldBe "kova.string.length"
                    it.second.messages[0].constraintId shouldBe "kova.string.length"
                }
            }
        }

        context("or: 3") {
            val length2 = Kova.string().length(2)
            val length5 = Kova.string().length(5)
            val length7 = Kova.string().length(7)
            val length2or5or7 = length2 or length5 or length7

            test("failure with length 3") {
                val result = length2or5or7.tryValidate("abc")
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].let {
                    it.constraintId shouldBe "kova.or"
                    it.shouldBeInstanceOf<Message.Or>()
                    it.first.messages[0].constraintId shouldBe "kova.or"
                    it.second.messages[0].constraintId shouldBe "kova.string.length"
                    println(it)
                }
            }
        }

        context("or: 3 - lambda") {
            val length2or5or7 =
                Kova
                    .string()
                    .length(2)
                    .or { it.length(5) }
                    .or { it.length(7) }

            test("failure with length 3") {
                val result = length2or5or7.tryValidate("abc")
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].let {
                    it.constraintId shouldBe "kova.or"
                    it.shouldBeInstanceOf<Message.Or>()
                    it.first.messages[0].constraintId shouldBe "kova.or"
                    it.second.messages[0].constraintId shouldBe "kova.string.length"
                    println(it)
                }
            }
        }

        context("map") {
            val validator = Kova.int().min(1).map { it * 2 }
            test("success") {
                val result = validator.tryValidate(2)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 4
            }
            test("failure") {
                val result = validator.tryValidate(-1)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].text shouldBe "must be greater than or equal to 1"
            }
        }

        context("compose") {
            val validator = Kova.string().max(1).compose(Kova.int().min(3).map { it.toString() })
            test("success") {
                val result = validator.tryValidate(3)
                result.isSuccess().mustBeTrue()
                result.value shouldBe "3"
            }
            test("failure when first constraint violated") {
                val result = validator.tryValidate(2)
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.comparable.min"
            }
            test("failure when second constraint violated") {
                val result = validator.tryValidate(10)
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.string.max"
            }
        }

        context("compose - lambda") {
            val validator = Kova.string().max(1).compose { Kova.int().min(3).map { it.toString() } }
            test("success") {
                val result = validator.tryValidate(3)
                result.isSuccess().mustBeTrue()
                result.value shouldBe "3"
            }
            test("failure when first constraint violated") {
                val result = validator.tryValidate(2)
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.comparable.min"
            }
            test("failure when second constraint violated") {
                val result = validator.tryValidate(10)
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.string.max"
            }
        }

        context("then") {
            val validator =
                Kova
                    .int()
                    .min(3)
                    .map { it.toString() }
                    .then(Kova.string().max(1))
            test("success") {
                val result = validator.tryValidate(3)
                result.isSuccess().mustBeTrue()
                result.value shouldBe "3"
            }
            test("failure when first constraint violated") {
                val result = validator.tryValidate(2)
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.comparable.min"
            }
            test("failure when second constraint violated") {
                val result = validator.tryValidate(10)
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.string.max"
            }
        }

        context("then - lambda") {
            val validator =
                Kova
                    .int()
                    .min(3)
                    .map { it.toString() }
                    .then { it.max(1) }
            test("success") {
                val result = validator.tryValidate(3)
                result.isSuccess().mustBeTrue()
                result.value shouldBe "3"
            }
            test("failure when first constraint violated") {
                val result = validator.tryValidate(2)
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.comparable.min"
            }
            test("failure when second constraint violated") {
                val result = validator.tryValidate(10)
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.string.max"
            }
        }

        context("logs") {
            val validator =
                Kova.string().trim().then(Kova.string().min(3).max(5))

            test("success") {
                val logs = mutableListOf<LogEntry>()
                val result = validator.tryValidate(" abcde ", ValidationConfig(logger = { logs.add(it) }))
                result.isSuccess().mustBeTrue()
                result.value shouldBe "abcde"

                logs shouldBe
                    listOf(
                        LogEntry.Satisfied(constraintId = "kova.string.min", root = "", path = "", input = "abcde"),
                        LogEntry.Satisfied(constraintId = "kova.string.max", root = "", path = "", input = "abcde"),
                    )
            }

            test("failure") {
                val logs = mutableListOf<LogEntry>()
                val result = validator.tryValidate(" ab ", ValidationConfig(logger = { logs.add(it) }))
                result.isFailure().mustBeTrue()

                logs shouldBe
                    listOf(
                        LogEntry.Violated(constraintId = "kova.string.min", root = "", path = "", input = "ab"),
                        LogEntry.Satisfied(constraintId = "kova.string.max", root = "", path = "", input = "ab"),
                    )
            }
        }

        context("message - provider - text") {
            val validator =
                Kova.string().uppercase().min(3).withMessage { messages ->
                    Message.text { "Invalid: consolidates messages=(${messages.joinToString { it.text }})" }
                }

            test("success") {
                val result = validator.tryValidate("ABCDE")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "ABCDE"
            }
            test("failure") {
                val result = validator.tryValidate("ab")
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.constraintId shouldBe "kova.withMessage"
                message.text shouldBe "Invalid: consolidates messages=(must be uppercase, must be at least 3 characters)"
                message.root shouldBe ""
                message.path.fullName shouldBe ""
            }
        }

        context("message - provider - resource") {
            val validator =
                Kova
                    .string()
                    .uppercase()
                    .min(3)
                    .withMessage { Message.resource() }

            test("success") {
                val result = validator.tryValidate("ABCDE")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "ABCDE"
            }
            test("failure") {
                val result = validator.tryValidate("ab")
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.constraintId shouldBe "kova.withMessage"
                message.text shouldBe "invalid: [must be uppercase, must be at least 3 characters]"
                message.root shouldBe ""
                message.path.fullName shouldBe ""
            }
        }

        context("message - text") {
            val validator =
                Kova
                    .string()
                    .uppercase()
                    .min(3)
                    .withMessage("Invalid")

            test("success") {
                val result = validator.tryValidate("ABCDE")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "ABCDE"
            }
            test("failure") {
                val result = validator.tryValidate("ab")
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.constraintId shouldBe "kova.withMessage"
                message.text shouldBe "Invalid"
                message.root shouldBe ""
                message.path.fullName shouldBe ""
            }
        }

        context("message - schema") {
            data class User(
                val id: Int,
                val name: String,
            )

            val userSchema =
                object : ObjectSchema<User>({
                    User::id { it }
                    User::name {
                        it.uppercase().min(3).withMessage {
                            Message.text { "Must be uppercase and at least 3 characters long" }
                        }
                    }
                }) {}

            test("success") {
                val result = userSchema.tryValidate(User(1, "ABCDE"))
                result.isSuccess().mustBeTrue()
                result.value shouldBe User(1, "ABCDE")
            }
            test("failure") {
                val result = userSchema.tryValidate(User(1, "ab"))
                result.isFailure().mustBeTrue()
                val message = result.messages.single()
                message.constraintId shouldBe "kova.withMessage"
                message.text shouldBe "Must be uppercase and at least 3 characters long"
                message.root shouldBe "User"
                message.path.fullName shouldBe "name"
            }
        }
    })
