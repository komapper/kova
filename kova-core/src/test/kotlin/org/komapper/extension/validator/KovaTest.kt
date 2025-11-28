package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class KovaTest :
    FunSpec({

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

        context("boolean") {
            val validator = Kova.boolean()

            test("success - true") {
                val result = validator.tryValidate(true)
                result.isSuccess().mustBeTrue()
            }

            test("success - false") {
                val result = validator.tryValidate(false)
                result.isSuccess().mustBeTrue()
            }
        }
    })
