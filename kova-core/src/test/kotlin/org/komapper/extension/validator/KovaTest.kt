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

        context("failFast - plus") {
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

            test("success - true") {
                val result = validator.tryValidate(true)
                result.isSuccess().mustBeTrue()
            }

            test("success - false") {
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
                    val nameV = User::name { Kova.nullable<String>().isNull().or(Kova.literal("")) }
                    val ageV = User::age { Kova.nullable<Int>().isNull().or(Kova.literal(0)) }

                    fun bind(
                        name: String?,
                        age: Int?,
                    ) = factory {
                        val name = nameV.bind(name)
                        val age = ageV.bind(age)
                        create(::User, name, age)
                    }
                }

            test("success - null") {
                val userFactory = userSchema.bind(null, null)
                val result = userFactory.tryCreate()
                result.isSuccess().mustBeTrue(result.toString())
                result.value shouldBe User(null, null)
            }

            test("success - non-null") {
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
                result.messages[0].text shouldBe
                    "at least one constraint must be satisfied: [[must be null], [must be ]]"
                result.messages[1].text shouldBe
                    "at least one constraint must be satisfied: [[must be null], [must be 0]]"
            }
        }

        context("generic") {

            data class Request(
                private val map: Map<String, String>,
            ) {
                operator fun get(key: String): String? = map[key]
            }

            val notNull = Kova.nullable<String>().notNull()
            val notNullAndMin3 = notNull.and(Kova.string().min(3)).toNonNullable()
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
                result.messages.size shouldBe 1
                result.messages[0].let {
                    it.path.fullName shouldBe "Request[key]"
                    it.text shouldBe "must not be null"
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
                result.messages.size shouldBe 1
                result.messages[0].let {
                    it.path.fullName shouldBe "Request[key]"
                    it.text shouldBe "must be at least 3 characters"
                }
            }
        }
    })
