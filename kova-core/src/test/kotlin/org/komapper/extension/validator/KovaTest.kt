package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec

class KovaTest :
    FunSpec({

        context("failFast") {
            context(_: Validation)
            fun String.validate() = min(3) and { length(4) }

            test("failFast = false") {
                val result = tryValidate { "ab".validate() }
                result.shouldBeFailure()
                result.messages.size shouldBe 2
            }

            test("failFast = true") {
                val result = tryValidate(ValidationConfig(failFast = true)) { "ab".validate() }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
            }
        }

        context("failFast with plus operator") {
            context(_: Validation)
            fun String?.validate() = this?.min(3)?.and { length(4) }.orSucceed()

            test("failFast = false") {
                val result = tryValidate { "ab".validate() }
                result.shouldBeFailure()
                result.messages.size shouldBe 2
            }

            test("failFast = true") {
                val result = tryValidate(ValidationConfig(failFast = true)) { "ab".validate() }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
            }
        }

        context("boolean") {
            test("success with true value") {
                val result = tryValidate { true.success() }
                result.shouldBeSuccess()
            }

            test("success with false value") {
                val result = tryValidate { false.success() }
                result.shouldBeSuccess()
            }
        }

        context("generic") {

            data class Request(
                private val map: Map<String, String>,
            ) {
                operator fun get(key: String): String? = map[key]
            }

            context(_: Validation)
            fun Request.requestKey(block: Constraint<String?>) = name("Request[key]") { this["key"].success().alsoThen { block(it) } }

            context(_: Validation)
            fun Request.requestKeyIsNotNull() = requestKey { it.notNull() }

            context(_: Validation)
            fun Request.requestKeyIsNotNullAndMin3() = requestKey { it.notNullAnd { it.min(3) } }

            test("success when requestKey is not null") {
                val result = tryValidate { Request(mapOf("key" to "abc")).requestKeyIsNotNull() }
                result.shouldBeSuccess()
                result.value shouldBe "abc"
            }

            test("failure when requestKey is null") {
                val result = tryValidate { Request(mapOf()).requestKeyIsNotNull() }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].let {
                    it.path.fullName shouldBe "Request[key]"
                    it.constraintId shouldBe "kova.nullable.notNull"
                }
            }

            test("success when requestKey is not null and min 3") {
                val result = tryValidate { Request(mapOf("key" to "abc")).requestKeyIsNotNullAndMin3() }
                result.shouldBeSuccess()
                result.value shouldBe "abc"
            }

            test("failure when requestKey constraint violated") {
                val result = tryValidate { Request(mapOf("key" to "ab")).requestKeyIsNotNullAndMin3() }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].let {
                    it.path.fullName shouldBe "Request[key]"
                    it.constraintId shouldBe "kova.charSequence.min"
                }
            }
        }
    })
