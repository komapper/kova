package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec

class KovaTest :
    FunSpec({

        context("failFast") {
            fun Validation.validate(string: String) {
                min(string, 3)
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
                min(string, 3)
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

        context("boolean") {
            test("success with true value") {
                val result = tryValidate { true }
                result.shouldBeSuccess()
                result.value shouldBe true
            }

            test("success with false value") {
                val result = tryValidate { false }
                result.shouldBeSuccess()
                result.value shouldBe false
            }
        }

        context("generic") {

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
                    if (it != null) min(it, 3)
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
                    it.constraintId shouldBe "kova.charSequence.min"
                }
            }
        }
    })
