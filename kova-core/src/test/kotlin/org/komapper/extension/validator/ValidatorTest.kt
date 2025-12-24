package org.komapper.extension.validator

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeInstanceOf

class ValidatorTest :
    FunSpec({

        context("validate") {
            context(_: Validation)
            fun Int.validate() = min(1) and { max(10) }

            test("success") {
                validate { 5.validate() }
            }

            test("failure") {
                val ex =
                    shouldThrow<ValidationException> {
                        validate { 0.validate() }
                    }
                ex.messages.size shouldBe 1
                ex.messages[0].constraintId shouldBe "kova.comparable.min"
            }
        }

        context("plus") {
            context(_: Validation)
            fun Int.validate() = max(2) + { max(3) }

            test("success") {
                val result = tryValidate { 1.validate() }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { 4.validate() }
                result.shouldBeFailure()
            }
        }

        context("and") {
            context(_: Validation)
            fun Int.validate() = min(2) and { max(3) }

            test("success") {
                val result = tryValidate { 2.validate() }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { 4.validate() }
                result.shouldBeFailure()
            }
        }

        context("or: 2") {
            context(_: Validation)
            fun String.length2or5() = or { length(2) } orElse { length(5) }

            test("success with length 2") {
                val result = tryValidate { "ab".length2or5() }
                result.shouldBeSuccess()
            }
            test("success with length 5") {
                val result = tryValidate { "abcde".length2or5() }
                result.shouldBeSuccess()
            }
            test("failure with length 3") {
                val result = tryValidate { "abc".length2or5() }
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
            context(_: Validation)
            fun String.length2or5or7() = or { length(2) } or { length(5) } orElse { length(7) }

            test("failure with length 3") {
                val result = tryValidate { "abc".length2or5or7() }
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
            context(_: Validation)
            fun Int.validate() = min(1) andMap { this * 2 }
            test("success") {
                val result = tryValidate { 2.validate() }
                result.shouldBeSuccess()
                result.value shouldBe 4
            }
            test("failure") {
                val result = tryValidate { (-1).validate() }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].text shouldBe "must be greater than or equal to 1"
            }
        }

        context("then") {
            context(_: Validation)
            fun Int.validate() = min(3) andMap { toString() } alsoThen { it.max(1) }

            test("success") {
                val result = tryValidate { 3.validate() }
                result.shouldBeSuccess()
                result.value shouldBe "3"
            }
            test("failure when first constraint violated") {
                val result = tryValidate { 2.validate() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.comparable.min"
            }
            test("failure when second constraint violated") {
                val result = tryValidate { 10.validate() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.max"
            }
        }

        context("then - lambda") {
            context(_: Validation)
            fun Int.validate() = min(3) andMap { toString() } alsoThen { it.max(1) }

            test("success") {
                val result = tryValidate { 3.validate() }
                result.shouldBeSuccess()
                result.value shouldBe "3"
            }
            test("failure when first constraint violated") {
                val result = tryValidate { 2.validate() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.comparable.min"
            }
            test("failure when second constraint violated") {
                val result = tryValidate { 10.validate() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.max"
            }
        }

        context("logs") {
            context(_: Validation)
            fun String.validate() = trim().let { it.min(3) and { it.max(5) } }

            test("success") {
                buildList {
                    val result = tryValidate(ValidationConfig(logger = { add(it) })) { " abcde ".validate() }
                    result.shouldBeSuccess()
                } shouldBe
                    listOf(
                        LogEntry.Satisfied(constraintId = "kova.charSequence.min", root = "", path = "", input = "abcde"),
                        LogEntry.Satisfied(constraintId = "kova.charSequence.max", root = "", path = "", input = "abcde"),
                    )
            }

            test("failure") {
                val logs = mutableListOf<LogEntry>()
                val result = tryValidate(ValidationConfig(logger = { logs.add(it) })) { " ab ".validate() }
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
            context(_: Validation)
            fun String.validate() =
                withMessage({ messages -> text("Invalid: consolidates messages=(${messages.joinToString { it.text }})") }) {
                    uppercase() and { min(3) }
                }

            test("success") {
                val result = tryValidate { "ABCDE".validate() }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "ab".validate() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.shouldBeInstanceOf<Message.Text>()
                message.text shouldBe "Invalid: consolidates messages=(must be uppercase, must be at least 3 characters)"
                message.root shouldBe ""
                message.path.fullName shouldBe ""
            }
        }

        context("message - provider - resource") {
            context(_: Validation)
            fun String.validate() = withMessage { uppercase() and { min(3) } }

            test("success") {
                val result = tryValidate { "ABCDE".validate() }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "ab".validate() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.constraintId shouldBe "kova.withMessage"
                message.text shouldBe "invalid: [must be uppercase, must be at least 3 characters]"
                message.root shouldBe ""
                message.path.fullName shouldBe ""
            }
        }

        context("message - text") {
            context(_: Validation)
            fun String.validate() = withMessage("Invalid") { uppercase() and { min(3) } }

            test("success") {
                val result = tryValidate { "ABCDE".validate() }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "ab".validate() }
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

            context(_: Validation)
            fun User.validate() =
                checking {
                    ::id { Unit.success() } then {
                        ::name {
                            withMessage({ text("Must be uppercase and at least 3 characters long") }) {
                                it.uppercase() and { it.min(3) }
                            }
                        }
                    }
                }

            test("success") {
                val result = tryValidate { User(1, "ABCDE").validate() }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { User(1, "ab").validate() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.shouldBeInstanceOf<Message.Text>()
                message.text shouldBe "Must be uppercase and at least 3 characters long"
                message.root shouldBe "User"
                message.path.fullName shouldBe "name"
            }
        }

        context("mapping operation after failure") {
            context(_: Validation)
            fun String.validate() =
                trim().let {
                    it.min(3) andMap { it.toUppercase() } alsoThen { it.max(3) }
                }

            test("failure") {
                val logs = mutableListOf<LogEntry>()
                val result = tryValidate(ValidationConfig(logger = { logs.add(it) })) { "  ab  ".validate() }
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
