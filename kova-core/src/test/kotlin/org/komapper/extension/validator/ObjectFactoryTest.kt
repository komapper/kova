package org.komapper.extension.validator

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class ObjectFactoryTest :
    FunSpec({

        context("FunctionDesc") {
            test("KParameter available") {
                val desc = FunctionDesc("User", mapOf(0 to "name", 1 to "age"))
                desc[0] shouldBe "name"
                desc[1] shouldBe "age"
            }

            test("KParameter unavailable") {
                val desc = FunctionDesc("", emptyMap())
                desc[0] shouldBe "param0"
                desc[1] shouldBe "param1"
            }

        }

        context("withDefault") {
            data class User(
                val name: String?,
                val age: Int?,
            )

            val userSchema =
                object : ObjectSchema<User>() {
                    private val name = User::name { Kova.nullable() }
                    private val age = User::age { Kova.int().asNullable() }

                    fun build(
                        name: String?,
                        age: Int?,
                    ): ObjectFactory<User> {
                        val arg1 = arg(name, this.name.withDefault(""))
                        val arg2 = arg(age, this.age.withDefault(0))
                        val arguments = arguments(arg1, arg2)
                        return arguments.build(::User)
                    }
                }

            test("success - null") {
                val userFactory = userSchema.build(null, null)
                val result = userFactory.tryCreate()
                result.isSuccess().mustBeTrue()
                result.value shouldBe User("", 0)
            }

            test("success - non-null") {
                val userFactory = userSchema.build("abc", 10)
                val result = userFactory.tryCreate()
                result.isSuccess().mustBeTrue()
                result.value shouldBe User("abc", 10)
            }
        }

        context("1 arg") {
            data class User(
                val id: Int,
            )

            val userSchema =
                object : ObjectSchema<User>() {
                    private val id = User::id { Kova.int().min(1) }

                    fun build(id: Int): ObjectFactory<User> {
                        val arg1 = arg(id, this.id)
                        val arguments = arguments(arg1)
                        return arguments.build(::User)
                    }
                }

            test("success - tryCreate") {
                val factory = userSchema.build(1)
                val result = factory.tryCreate()
                result.isSuccess().mustBeTrue()
                result.value shouldBe User(1)
            }

            test("success - create") {
                val factory = userSchema.build(1)
                val user = factory.create()
                user shouldBe User(1)
            }

            test("failure - tryCreate") {
                val factory = userSchema.build(-1)
                val result = factory.tryCreate()
                result.isFailure().mustBeTrue()
                result.details.size shouldBe 1
                val detail = result.details.first()
                detail.root shouldContain "<init>"
                detail.path.fullName shouldBe "id"
                detail.message.content shouldBe "Number -1 must be greater than or equal to 1"
            }

            test("failure - create") {
                val factory = userSchema.build(-1)
                val ex =
                    shouldThrow<ValidationException> {
                        factory.create()
                    }
                ex.details.size shouldBe 1
                val detail = ex.details.first()
                detail.root shouldContain "<init>"
                detail.path.fullName shouldBe "id"
                detail.message.content shouldBe "Number -1 must be greater than or equal to 1"
            }
        }

        context("2 args") {

            data class User(
                val id: Int,
                val name: String,
            )

            val userSchema =
                object : ObjectSchema<User>() {
                    private val id = User::id { Kova.int().min(1) }
                    private val name = User::name { Kova.string().min(1).max(10) }

                    fun build(
                        id: Int,
                        name: String,
                    ): ObjectFactory<User> {
                        val arg1 = arg(id, this.id)
                        val arg2 = arg(name, this.name)
                        val arguments = arguments(arg1, arg2)
                        return arguments.build(::User)
                    }
                }

            test("success") {
                val userFactory = userSchema.build(1, "abc")
                val result = userFactory.tryCreate()
                result.isSuccess().mustBeTrue()
                result.value shouldBe User(1, "abc")
            }

            test("failure") {
                val userFactory = userSchema.build(0, "")
                val result = userFactory.tryCreate()
                result.isFailure().mustBeTrue()
                result.details.size shouldBe 2
            }

            test("failure - failFast is true") {
                val userFactory = userSchema.build(0, "")
                val result = userFactory.tryCreate(failFast = true)
                result.isFailure().mustBeTrue()
                result.details.size shouldBe 1
            }
        }

        context("2 args - generic validator") {
            data class User(
                val id: Int,
                val name: String,
            )

            val userSchema =
                object : ObjectSchema<User>() {
                    fun build(
                        id: Int,
                        name: String,
                    ): ObjectFactory<User> {
                        val arg1 = arg(id, Kova.generic())
                        val arg2 = arg(name, Kova.generic())
                        val arguments = arguments(arg1, arg2)
                        return arguments.build(::User)
                    }
                }

            test("success") {
                val factory = userSchema.build(1, "abc")
                val result = factory.tryCreate()
                result.isSuccess().mustBeTrue()
                result.value shouldBe User(1, "abc")
            }
        }

        context("2 args - nested factory") {
            data class Age(
                val value: Int,
            )

            data class Name(
                val value: String,
            )

            data class Person(
                val name: Name,
                val age: Age,
            )

            val ageSchema =
                object : ObjectSchema<Age>() {
                    private val value = Age::value { Kova.int().min(0) }

                    fun build(age: Int): ObjectFactory<Age> {
                        val args = arguments(arg(age, this.value))
                        return args.build(::Age)
                    }
                }

            val nameSchema =
                object : ObjectSchema<Name>() {
                    private val value = Name::value { Kova.string().notBlank() }

                    fun build(name: String): ObjectFactory<Name> {
                        val args = arguments(arg(name, this.value))
                        return args.build(::Name)
                    }
                }

            val personSchema =
                object : ObjectSchema<Person>() {
                    private val name = Person::name { nameSchema }
                    private val age = Person::age { ageSchema }

                    fun build(
                        name: String,
                        age: Int,
                    ): ObjectFactory<Person> {
                        val arg1 = arg(this.name.build(name), this.name)
                        val arg2 = arg(this.age.build(age), this.age)
                        val arguments = arguments(arg1, arg2)
                        return arguments.build(::Person)
                    }
                }

            test("success") {
                val factory = personSchema.build("abc", 10)
                val result = factory.tryCreate()
                result.isSuccess().mustBeTrue()
                result.value shouldBe Person(Name("abc"), Age(10))
            }
        }
    })
