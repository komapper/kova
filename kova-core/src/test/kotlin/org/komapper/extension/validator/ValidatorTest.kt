package org.komapper.extension.validator

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeInstanceOf

class ValidatorTest :
    FunSpec({

        context("validate") {
            fun Validation.validate(i: Int) {
                min(i, 1)
                max(i, 10)
            }

            test("success") {
                validate { validate(5) }
            }

            test("failure") {
                val ex =
                    shouldThrow<ValidationException> {
                        validate { validate(0) }
                    }
                ex.messages.size shouldBe 1
                ex.messages[0].constraintId shouldBe "kova.comparable.min"
            }
        }

        context("plus") {
            fun Validation.validate(i: Int) {
                max(i, 2)
                max(i, 3)
            }

            test("success") {
                val result = tryValidate { validate(1) }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { validate(4) }
                result.shouldBeFailure()
            }
        }

        context("and") {
            fun Validation.validate(i: Int) {
                min(i, 2)
                max(i, 3)
            }

            test("success") {
                val result = tryValidate { validate(2) }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { validate(4) }
                result.shouldBeFailure()
            }
        }

        context("or: 2") {
            fun Validation.length2or5(string: String) = or { length(string, 2) } orElse { length(string, 5) }

            test("success with length 2") {
                val result = tryValidate { length2or5("ab") }
                result.shouldBeSuccess()
            }
            test("success with length 5") {
                val result = tryValidate { length2or5("abcde") }
                result.shouldBeSuccess()
            }
            test("failure with length 3") {
                val result = tryValidate { length2or5("abc") }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].let {
                    it.constraintId shouldBe "kova.or"
                    it.args.size shouldBe 2
                    it.args[0]
                        .shouldBeInstanceOf<List<Message>>()
                        .single()
                        .constraintId shouldBe "kova.charSequence.length"
                    it.args[1]
                        .shouldBeInstanceOf<List<Message>>()
                        .single()
                        .constraintId shouldBe "kova.charSequence.length"
                }
            }
        }

        context("or: 3") {
            fun Validation.length2or5or7(string: String) =
                or { length(string, 2) } or { length(string, 5) } orElse {
                    length(string, 7)
                }

            test("failure with length 3") {
                val result = tryValidate { length2or5or7("abc") }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].let {
                    it.constraintId shouldBe "kova.or"
                    it.args.size shouldBe 2
                    it.args[0]
                        .shouldBeInstanceOf<List<Message>>()
                        .single()
                        .constraintId shouldBe "kova.or"
                    it.args[1]
                        .shouldBeInstanceOf<List<Message>>()
                        .single()
                        .constraintId shouldBe "kova.charSequence.length"
                    println(it)
                }
            }
        }

        context("map") {
            fun Validation.validate(i: Int): Int {
                min(i, 1)
                return i * 2
            }
            test("success") {
                val result = tryValidate { validate(2) }
                result.shouldBeSuccess()
                result.value shouldBe 4
            }
            test("failure") {
                val result = tryValidate { validate(-1) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].text shouldBe "must be greater than or equal to 1"
            }
        }

        context("then") {
            fun Validation.validate(i: Int): String {
                min(i, 3)
                return i.toString().also { max(it, 1) }
            }

            test("success") {
                val result = tryValidate { validate(3) }
                result.shouldBeSuccess()
                result.value shouldBe "3"
            }
            test("failure when first constraint violated") {
                val result = tryValidate { validate(2) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.comparable.min"
            }
            test("failure when second constraint violated") {
                val result = tryValidate { validate(10) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.max"
            }
        }

        context("then - lambda") {
            fun Validation.validate(i: Int): String {
                min(i, 3)
                return i.toString().also { max(it, 1) }
            }

            test("success") {
                val result = tryValidate { validate(3) }
                result.shouldBeSuccess()
                result.value shouldBe "3"
            }
            test("failure when first constraint violated") {
                val result = tryValidate { validate(2) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.comparable.min"
            }
            test("failure when second constraint violated") {
                val result = tryValidate { validate(10) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.max"
            }
        }

        context("logs") {
            fun Validation.validate(string: String) =
                string.trim().let {
                    min(it, 3)
                    max(it, 5)
                }

            test("success") {
                buildList {
                    val result = tryValidate(ValidationConfig(logger = { add(it) })) { validate(" abcde ") }
                    result.shouldBeSuccess()
                } shouldBe
                    listOf(
                        LogEntry.Satisfied(constraintId = "kova.charSequence.min", root = "", path = "", input = "abcde"),
                        LogEntry.Satisfied(constraintId = "kova.charSequence.max", root = "", path = "", input = "abcde"),
                    )
            }

            test("failure") {
                val logs = mutableListOf<LogEntry>()
                val result = tryValidate(ValidationConfig(logger = { logs.add(it) })) { validate(" ab ") }
                result.shouldBeFailure()

                logs shouldBe
                    listOf(
                        LogEntry.Violated(
                            constraintId = "kova.charSequence.min",
                            root = "",
                            path = "",
                            input = "ab",
                            args = listOf(3),
                        ),
                        LogEntry.Satisfied(constraintId = "kova.charSequence.max", root = "", path = "", input = "ab"),
                    )
            }
        }

        context("message - provider - text") {
            fun Validation.validate(string: String) =
                withMessage({ messages -> text("Invalid: consolidates messages=(${messages.joinToString { it.text }})") }) {
                    uppercase(string)
                    min(string, 3)
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

        context("message - provider - resource") {
            fun Validation.validate(string: String) =
                withMessage {
                    uppercase(string)
                    min(string, 3)
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

        context("message - text") {
            fun Validation.validate(string: String) =
                withMessage("Invalid") {
                    uppercase(string)
                    min(string, 3)
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

            fun Validation.validate(user: User) =
                user.schema {
                    user::id { }
                    user::name {
                        withMessage({ text("Must be uppercase and at least 3 characters long") }) {
                            uppercase(it)
                            min(it, 3)
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

        context("mapping operation after failure") {
            fun Validation.validate(string: String) =
                string.trim().also { min(it, 3) }.toUppercase().also {
                    max(it, 3)
                }

            test("failure") {
                val logs = mutableListOf<LogEntry>()
                val result = tryValidate(ValidationConfig(logger = { logs.add(it) })) { validate("  ab  ") }
                result.shouldBeFailure()
                logs shouldBe
                    listOf(
                        LogEntry.Violated(
                            constraintId = "kova.charSequence.min",
                            root = "",
                            path = "",
                            input = "ab",
                            args = listOf(3),
                        ),
                        LogEntry.Satisfied(constraintId = "kova.charSequence.max", root = "", path = "", input = "AB"),
                    )
            }
        }
    })
