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

        context("notNull") {
            val validator = Kova.notNull<Int>()

            test("success - non-null") {
                val result = validator.tryValidate(0)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 0
            }

            test("failure - null") {
                val result = validator.tryValidate(null)
                result.isFailure().mustBeTrue()
            }
        }

        context("isNull") {
            val validator = Kova.isNull<Int>()

            test("success - null") {
                val result = validator.tryValidate(null)
                result.isSuccess().mustBeTrue()
                result.value shouldBe null
            }

            test("failure - non-null") {
                val result = validator.tryValidate(0)
                result.isFailure().mustBeTrue()
            }
        }

        context("isNullOr") {
            val validator = Kova.isNullOr(0)

            test("success - null") {
                val result = validator.tryValidate(null)
                result.isSuccess().mustBeTrue()
                result.value shouldBe null
            }

            test("success - 0") {
                val result = validator.tryValidate(0)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 0
            }

            test("failure - 1") {
                val result = validator.tryValidate(1)
                result.isFailure().mustBeTrue()
            }
        }

        context("isNullOr - schema") {
            data class User(
                val name: String?,
                val age: Int?,
            )

            val userSchema =
                object : ObjectSchema<User>() {
                    val name = User::name { Kova.nullable<String>().isNullOr("") }
                    val age = User::age { Kova.int().isNullOr(0) }
                }

            val userFactory =
                object {
                    private val args =
                        Kova.args(
                            userSchema.name,
                            userSchema.age,
                        )
                    private val factory = args.createFactory(::User)

                    fun tryCreate(
                        name: String?,
                        age: Int?,
                    ) = factory.tryCreate(name, age)
                }

            test("success - null") {
                val result = userFactory.tryCreate(null, null)
                result.isSuccess().mustBeTrue()
                result.value shouldBe User(null, null)
            }

            test("success - non-null") {
                val result = userFactory.tryCreate("", 0)
                result.isSuccess().mustBeTrue()
                result.value shouldBe User("", 0)
            }

            // TODO
            test("failure") {
                val result = userFactory.tryCreate("abc", 10)
                result.isFailure().mustBeTrue(result.messages.toString())
                result.messages.size shouldBe 4
                result.messages[0].content shouldBe "Value abc must be null"
                result.messages[1].content shouldBe "Value abc must be "
                result.messages[2].content shouldBe "Value 10 must be null"
                result.messages[3].content shouldBe "Value 10 must be 0"
            }
        }

        context("generic") {

            data class Request(
                private val map: Map<String, String>,
            ) {
                operator fun get(key: String): String? = map[key]
            }

            val notNull = Kova.notNull<String>()
            val notNullAndMin3 = Kova.notNullThen(Kova.string().min(3)).toNonNullable()
            val requestKey = Kova.generic<Request>().name("Request[key]").map { it["key"] }
            val requestKeyIsNotNull = requestKey.then(notNull)
            val requestKeyIsNotNullAndMin3 = requestKey.then(notNullAndMin3)

            test("success - requestKeyIsNotNull") {
                val result = requestKeyIsNotNull.tryValidate(Request(mapOf("key" to "abc")))
                result.isSuccess().mustBeTrue()
                result.value shouldBe "abc"
            }

            test("failure - requestKeyIsNotNull") {
                val result = requestKeyIsNotNull.tryValidate(Request(mapOf()))
                result.isFailure().mustBeTrue()
                result.details.size shouldBe 1
                result.details[0].let {
                    it.path shouldBe "Request[key]"
                    it.message.content shouldBe "Value must not be null"
                }
            }

            test("success - requestKeyIsNotNullAndMin3") {
                val result = requestKeyIsNotNullAndMin3.tryValidate(Request(mapOf("key" to "abc")))
                result.isSuccess().mustBeTrue()
                result.value shouldBe "abc"
            }

            test("failure - requestKeyIsNotNullAndMin3") {
                val result = requestKeyIsNotNullAndMin3.tryValidate(Request(mapOf("key" to "ab")))
                result.isFailure().mustBeTrue()
                result.details.size shouldBe 1
                result.details[0].let {
                    it.path shouldBe "Request[key]"
                    it.message.content shouldBe "\"ab\" must be at least 3 characters"
                }
            }
        }
    })
