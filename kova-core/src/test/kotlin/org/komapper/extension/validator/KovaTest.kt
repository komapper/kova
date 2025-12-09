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
                val result = validator.tryValidate("ab", ValidationConfig(failFast = true))
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
            }
        }

        context("failFast with plus operator") {
            val validator = Kova.string().min(3).asNullable() + Kova.string().length(4).asNullable()

            test("failFast = false") {
                val result = validator.tryValidate("ab")
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 2
            }

            test("failFast = true") {
                val result = validator.tryValidate("ab", ValidationConfig(failFast = true))
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
            }
        }

        context("boolean") {
            val validator = Kova.boolean()

            test("success with true value") {
                val result = validator.tryValidate(true)
                result.isSuccess().mustBeTrue()
            }

            test("success with false value") {
                val result = validator.tryValidate(false)
                result.isSuccess().mustBeTrue()
            }
        }

        context("nullable") {
            data class User(
                val name: String?,
                val age: Int?,
            )

            val userSchema =
                object : ObjectSchema<User>() {
                    val nameV = User::name { Kova.nullable<String>().isNullOr({ it.literal("") }) }
                    val ageV = User::age { Kova.nullable<Int>().isNullOr({ it.literal(0) }) }

                    fun bind(
                        name: String?,
                        age: Int?,
                    ) = factory {
                        val name = nameV.bind(name)
                        val age = ageV.bind(age)
                        create(::User, name, age)
                    }
                }

            test("success with null value") {
                val userFactory = userSchema.bind(null, null)
                val result = userFactory.tryCreate()
                result.isSuccess().mustBeTrue(result.toString())
                result.value shouldBe User(null, null)
            }

            test("success with non-null value") {
                val userFactory = userSchema.bind("", 0)
                val result = userFactory.tryCreate()
                result.isSuccess().mustBeTrue()
                result.value shouldBe User("", 0)
            }

            test("failure") {
                val userFactory = userSchema.bind("abc", 10)
                val result = userFactory.tryCreate()
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 2
                result.messages[0].constraintId shouldBe "kova.or"
                result.messages[1].constraintId shouldBe "kova.or"
            }
        }

        context("generic") {

            data class Request(
                private val map: Map<String, String>,
            ) {
                operator fun get(key: String): String? = map[key]
            }

            val nullable = Kova.nullable<String>()
            val notNull = nullable.notNull()
            val notNullAndMin3 = nullable.notNullAnd({ it.min(3) }).toNonNullable()
            val requestKey = Kova.generic<Request>().name("Request[key]").map { it["key"] }
            val requestKeyIsNotNull = requestKey.then(notNull)
            val requestKeyIsNotNullAndMin3 = requestKey.then(notNullAndMin3)

            test("success when requestKey is not null") {
                val result = requestKeyIsNotNull.tryValidate(Request(mapOf("key" to "abc")))
                result.isSuccess().mustBeTrue()
                result.value shouldBe "abc"
            }

            test("failure when requestKey is null") {
                val result = requestKeyIsNotNull.tryValidate(Request(mapOf()))
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].let {
                    it.path.fullName shouldBe "Request[key]"
                    it.constraintId shouldBe "kova.nullable.notNull"
                }
            }

            test("success when requestKey is not null and min 3") {
                val result = requestKeyIsNotNullAndMin3.tryValidate(Request(mapOf("key" to "abc")))
                result.isSuccess().mustBeTrue()
                result.value shouldBe "abc"
            }

            test("failure when requestKey constraint violated") {
                val result = requestKeyIsNotNullAndMin3.tryValidate(Request(mapOf("key" to "ab")))
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].let {
                    it.path.fullName shouldBe "Request[key]"
                    it.constraintId shouldBe "kova.string.min"
                }
            }
        }
    })
