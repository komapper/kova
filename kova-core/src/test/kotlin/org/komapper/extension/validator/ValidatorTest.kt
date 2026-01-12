package org.komapper.extension.validator

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import java.util.Locale

class ValidatorTest :
    FunSpec({

        beforeSpec {
            Locale.setDefault(Locale.US)
        }

        context("tryValidate and validate") {
            context(_: Validation)
            fun validate(i: Int) {
                i.ensureAtLeast(1)
                i.ensureAtMost(10)
            }

            test("tryValidate - success") {
                val result = tryValidate { validate(5) }
                result.shouldBeSuccess()
            }

            test("tryValidate - failure") {
                val result = tryValidate { validate(0) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.comparable.atLeast"
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
                ex.messages[0].constraintId shouldBe "kova.comparable.atLeast"
            }
        }

        context("map") {
            context(_: Validation)
            fun validate(i: Int): Int {
                i.ensureAtLeast(1)
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
            context(_: Validation)
            fun validate(i: Int): String {
                i.ensureAtLeast(3)
                return i.toString().also { it.ensureLengthAtMost(1) }
            }

            test("success") {
                val result = tryValidate { validate(3) }
                result.shouldBeSuccess()
                result.value shouldBe "3"
            }
            test("failure when first constraint violated") {
                val result = tryValidate { validate(2) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.comparable.atLeast"
            }
            test("failure when second constraint violated") {
                val result = tryValidate { validate(10) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.lengthAtMost"
            }
        }

        context("logs") {
            context(_: Validation)
            fun validate(string: String) =
                string.trim().let {
                    it.ensureLengthAtLeast(3)
                    it.ensureLengthAtMost(5)
                }

            test("success") {
                buildList {
                    val result = tryValidate(ValidationConfig(logger = { add(it) })) { validate(" abcde ") }
                    result.shouldBeSuccess()
                } shouldBe
                    listOf(
                        LogEntry.Satisfied(
                            constraintId = "kova.charSequence.lengthAtLeast",
                            root = "",
                            path = "",
                            input = "abcde",
                        ),
                        LogEntry.Satisfied(
                            constraintId = "kova.charSequence.lengthAtMost",
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
                            constraintId = "kova.charSequence.lengthAtLeast",
                            root = "",
                            path = "",
                            input = "ab",
                            args = listOf(3),
                        ),
                        LogEntry.Satisfied(
                            constraintId = "kova.charSequence.lengthAtMost",
                            root = "",
                            path = "",
                            input = "ab",
                        ),
                    )
            }
        }

        context("mapping operation after failure") {
            context(_: Validation)
            fun validate(string: String) =
                string.trim().also { it.ensureLengthAtLeast(3) }.uppercase().also {
                    it.ensureLengthAtMost(3)
                }

            test("failure") {
                val logs = mutableListOf<LogEntry>()
                val result = tryValidate(ValidationConfig(logger = { logs.add(it) })) { validate("  ab  ") }
                result.shouldBeFailure()
                logs shouldBe
                    listOf(
                        LogEntry.Violated(
                            constraintId = "kova.charSequence.lengthAtLeast",
                            root = "",
                            path = "",
                            input = "ab",
                            args = listOf(3),
                        ),
                        LogEntry.Satisfied(
                            constraintId = "kova.charSequence.lengthAtMost",
                            root = "",
                            path = "",
                            input = "AB",
                        ),
                    )
            }
        }

        context("failFast") {
            context(_: Validation)
            fun validate(string: String) {
                string.ensureLengthAtLeast(3)
                string.ensureLength(4)
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
            context(_: Validation)
            fun validate(string: String?) {
                if (string == null) return
                string.ensureLengthAtLeast(3)
                string.ensureLength(4)
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

            context(_: Validation)
            fun requestKey(
                request: Request,
                block: context(Validation)(String?) -> Unit,
            ) = request.named("Request[key]") { r ->
                r["key"].also { block(it) }
            }

            context(_: Validation)
            fun requestKeyIsNotNull(request: Request) = requestKey(request) { it.ensureNotNull() }

            context(_: Validation)
            fun requestKeyIsNotNullAndMin3(request: Request) =
                requestKey(request) {
                    it.ensureNotNull()
                    if (it != null) it.ensureLengthAtLeast(3)
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
                    it.constraintId shouldBe "kova.charSequence.lengthAtLeast"
                }
            }
        }

        context("constrain - with text message") {
            @IgnorableReturnValue
            context(_: Validation)
            fun validate(string: String) =
                string.constrain("test") {
                    satisfies(it == "OK") { text("Constraint failed") }
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
                result.messages[0].toString() shouldBe "Message(constraintId=test, text='Constraint failed', root=, path=, input=NG)"
            }
        }

        context("constrain - with resource message") {
            @IgnorableReturnValue
            context(_: Validation)
            fun validate(string: String) =
                string.constrain("test") {
                    satisfies(it.isNotBlank()) { "kova.charSequence.notBlank".resource }
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
                result.messages[0].toString() shouldBe
                    "Message(constraintId=test, text='must not be blank', root=, path=, input= , args=[])"
            }
        }
    })
