package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class KovaTest :
    FunSpec({

        data class Request(
            private val map: Map<String, String>,
        ) : Map<String, String> by map

        context("obj - map and andThen") {
            val nullOrLength3 =
                Kova
                    .obj<Request>()
                    .map { it["a"] }
                    .andThen(Kova.nullable<String>().whenNotNull(Kova.string().length(3)))

            test("success - null") {
                val request = Request(emptyMap())
                val result = nullOrLength3.tryValidate(request)
                result.isSuccess().mustBeTrue()
            }

            test("success - non-null") {
                val request = Request(mapOf("a" to "abc"))
                val result = nullOrLength3.tryValidate(request)
                result.isSuccess().mustBeTrue()
            }

            test("failure") {
                val request = Request(mapOf("a" to "abcd"))
                val result = nullOrLength3.tryValidate(request)
                result.isFailure().mustBeTrue()
            }
        }

        context("failFast") {
            val validator = Kova.string().min(3).length(4)

            test("failFast = false") {
                val result = validator.tryValidate("ab")
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 2
            }

            test("failFast = true") {
                val result = validator.tryValidate("ab", failFast = true)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
            }
        }

        context("failFast - plus") {
            val validator = Kova.string().min(3).asNullable() + Kova.string().length(4).asNullable()

            test("failFast = false") {
                val result = validator.tryValidate("ab")
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 2
            }

            test("failFast = true") {
                val result = validator.tryValidate("ab", failFast = true)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
            }
        }
    })
