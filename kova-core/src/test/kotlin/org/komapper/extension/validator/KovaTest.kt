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

        context("whenNullAs") {
            val validator = Kova.whenNullAs(0)

            test("success - null") {
                val result = validator.tryValidate(null)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 0
            }

            test("success - 0") {
                val result = validator.tryValidate(0)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 0
            }

            test("success - 1") {
                val result = validator.tryValidate(1)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 1
            }
        }

        context("whenNullAs - factory") {
            data class User(
                val name: String?,
                val age: Int?,
            )

            val userSchema =
                object : ObjectSchema<User>() {
                    val name = User::name { Kova.generic() }
                    val age = User::age { Kova.int().asNullable() }
                }

            val userFactory =
                object {
                    private val args =
                        Kova.args(
                            userSchema.name.whenNullAs(""),
                            userSchema.age.whenNullAs(0),
                        )
                    private val factory = args.createFactory(::User)

                    fun create(
                        name: String?,
                        age: Int?,
                    ) = factory.create(name, age)
                }

            test("success - null") {
                val user = userFactory.create(null, null)
                user shouldBe User("", 0)
            }

            test("success - non-null") {
                val user = userFactory.create("abc", 10)
                user shouldBe User("abc", 10)
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
    })
