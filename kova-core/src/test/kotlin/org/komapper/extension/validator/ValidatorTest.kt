package org.komapper.extension.validator

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeInstanceOf

class ValidatorTest :
    FunSpec({

        context("tryValidate and validate") {
            fun Validation.validate(i: Int) {
                min(i, 1)
                max(i, 10)
            }

            test("tryValidate - success") {
                val result = tryValidate { validate(5) }
                result.shouldBeSuccess()
            }

            test("tryValidate - failure") {
                val result = tryValidate { validate(0) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.comparable.min"
            }

            test("validate - success") {
                validate { validate(5) }
            }

            test("validate - failure") {
                val ex =
                    shouldThrow<ValidationException> {
                        validate { validate(0) }
                    }
                ex.messages.size shouldBe 1
                ex.messages[0].constraintId shouldBe "kova.comparable.min"
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
                return i.toString().also { maxLength(it, 1) }
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
                result.messages.single().constraintId shouldBe "kova.charSequence.maxLength"
            }
        }

        context("logs") {
            fun Validation.validate(string: String) =
                string.trim().let {
                    minLength(it, 3)
                    maxLength(it, 5)
                }

            test("success") {
                buildList {
                    val result = tryValidate(ValidationConfig(logger = { add(it) })) { validate(" abcde ") }
                    result.shouldBeSuccess()
                } shouldBe
                    listOf(
                        LogEntry.Satisfied(
                            constraintId = "kova.charSequence.minLength",
                            root = "",
                            path = "",
                            input = "abcde",
                        ),
                        LogEntry.Satisfied(
                            constraintId = "kova.charSequence.maxLength",
                            root = "",
                            path = "",
                            input = "abcde",
                        ),
                    )
            }

            test("failure") {
                val logs = mutableListOf<LogEntry>()
                val result = tryValidate(ValidationConfig(logger = { logs.add(it) })) { validate(" ab ") }
                result.shouldBeFailure()

                logs shouldBe
                    listOf(
                        LogEntry.Violated(
                            constraintId = "kova.charSequence.minLength",
                            root = "",
                            path = "",
                            input = "ab",
                            args = listOf(3),
                        ),
                        LogEntry.Satisfied(
                            constraintId = "kova.charSequence.maxLength",
                            root = "",
                            path = "",
                            input = "ab",
                        ),
                    )
            }
        }

        context("mapping operation after failure") {
            fun Validation.validate(string: String) =
                string.trim().also { minLength(it, 3) }.uppercase().also {
                    maxLength(it, 3)
                }

            test("failure") {
                val logs = mutableListOf<LogEntry>()
                val result = tryValidate(ValidationConfig(logger = { logs.add(it) })) { validate("  ab  ") }
                result.shouldBeFailure()
                logs shouldBe
                    listOf(
                        LogEntry.Violated(
                            constraintId = "kova.charSequence.minLength",
                            root = "",
                            path = "",
                            input = "ab",
                            args = listOf(3),
                        ),
                        LogEntry.Satisfied(
                            constraintId = "kova.charSequence.maxLength",
                            root = "",
                            path = "",
                            input = "AB",
                        ),
                    )
            }
        }

        context("failFast") {
            fun Validation.validate(string: String) {
                minLength(string, 3)
                length(string, 4)
            }

            test("failFast = false") {
                val result = tryValidate { validate("ab") }
                result.shouldBeFailure()
                result.messages.size shouldBe 2
            }

            test("failFast = true") {
                val result = tryValidate(ValidationConfig(failFast = true)) { validate("ab") }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
            }
        }

        context("failFast with plus operator") {
            fun Validation.validate(string: String?) {
                if (string == null) return
                minLength(string, 3)
                length(string, 4)
            }

            test("failFast = false") {
                val result = tryValidate { validate("ab") }
                result.shouldBeFailure()
                result.messages.size shouldBe 2
            }

            test("failFast = true") {
                val result = tryValidate(ValidationConfig(failFast = true)) { validate("ab") }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
            }
        }

        context("name") {

            data class Request(
                private val map: Map<String, String>,
            ) {
                operator fun get(key: String): String? = map[key]
            }

            fun Validation.requestKey(
                request: Request,
                block: Validation.(String?) -> Unit,
            ) = request.name("Request[key]") {
                request["key"].also { block(it) }
            }

            fun Validation.requestKeyIsNotNull(request: Request) = requestKey(request) { notNull(it) }

            fun Validation.requestKeyIsNotNullAndMin3(request: Request) =
                requestKey(request) {
                    notNull(it)
                    if (it != null) minLength(it, 3)
                }

            test("success when requestKey is not null") {
                val result = tryValidate { requestKeyIsNotNull(Request(mapOf("key" to "abc"))) }
                result.shouldBeSuccess()
                result.value shouldBe "abc"
            }

            test("failure when requestKey is null") {
                val result = tryValidate { requestKeyIsNotNull(Request(mapOf())) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].let {
                    it.path.fullName shouldBe "Request[key]"
                    it.constraintId shouldBe "kova.nullable.notNull"
                }
            }

            test("success when requestKey is not null and min 3") {
                val result = tryValidate { requestKeyIsNotNullAndMin3(Request(mapOf("key" to "abc"))) }
                result.shouldBeSuccess()
                result.value shouldBe "abc"
            }

            test("failure when requestKey constraint violated") {
                val result = tryValidate { requestKeyIsNotNullAndMin3(Request(mapOf("key" to "ab"))) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].let {
                    it.path.fullName shouldBe "Request[key]"
                    it.constraintId shouldBe "kova.charSequence.minLength"
                }
            }
        }

        context("constrain - with text message") {
            @IgnorableReturnValue
            fun Validation.validate(string: String) =
                string.constrain("test") {
                    satisfies(it == "OK", text("Constraint failed"))
                }
            test("success") {
                val result = tryValidate { validate("OK") }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { validate("NG") }
                result.shouldBeFailure()
                result.messages[0].text shouldBe "Constraint failed"
                result.messages[0].constraintId shouldBe "test"
            }
        }

        context("constrain - with resource message") {
            @IgnorableReturnValue
            fun Validation.validate(string: String) =
                string.constrain("test") {
                    satisfies(it.isNotBlank(), "kova.charSequence.notBlank".resource)
                }

            test("success") {
                val result = tryValidate { validate("OK") }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { validate(" ") }
                result.shouldBeFailure()
                result.messages[0].text shouldBe "must not be blank"
                result.messages[0].constraintId shouldBe "test"
            }
        }

        context("withMessage - text") {
            fun Validation.validate(string: String) =
                withMessage({ messages -> text("Invalid: consolidates messages=(${messages.joinToString { it.text }})") }) {
                    uppercase(string)
                    minLength(string, 3)
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
            fun Validation.validate(string: String) =
                withMessage {
                    uppercase(string)
                    minLength(string, 3)
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

            fun Validation.validate(user: User) =
                user.schema {
                    user::id { }
                    user::name {
                        withMessage({ text("Must be uppercase and at least 3 characters long") }) {
                            uppercase(it)
                            minLength(it, 3)
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
