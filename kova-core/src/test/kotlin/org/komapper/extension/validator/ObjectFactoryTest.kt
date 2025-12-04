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
                    private val nameV = User::name { Kova.nullable() }
                    private val ageV = User::age { Kova.int().asNullable() }

                    fun bind(
                        name: String?,
                        age: Int?,
                    ): ObjectFactory<User> {
                        val arg0 = nameV.withDefault("").bind(name)
                        val arg1 = ageV.withDefault(0).bind(age)
                        return create(::User, arg0, arg1)
                    }
                }

            test("success - null") {
                val userFactory = userSchema.bind(null, null)
                val result = userFactory.tryCreate()
                result.isSuccess().mustBeTrue()
                result.value shouldBe User("", 0)
            }

            test("success - non-null") {
                val userFactory = userSchema.bind("abc", 10)
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
                    private val idV = User::id { Kova.int().min(1) }

                    fun bind(id: Int): ObjectFactory<User> {
                        val arg0 = idV.bind(id)
                        return create(::User, arg0)
                    }
                }

            test("success - tryCreate") {
                val factory = userSchema.bind(1)
                val result = factory.tryCreate()
                result.isSuccess().mustBeTrue()
                result.value shouldBe User(1)
            }

            test("success - create") {
                val factory = userSchema.bind(1)
                val user = factory.create()
                user shouldBe User(1)
            }

            test("failure - tryCreate") {
                val factory = userSchema.bind(-1)
                val result = factory.tryCreate()
                result.isFailure().mustBeTrue()
                result.details.size shouldBe 1
                val detail = result.details.first()
                detail.root shouldContain "<init>"
                detail.path.fullName shouldBe "id"
                detail.message.content shouldBe "Number -1 must be greater than or equal to 1"
            }

            test("failure - create") {
                val factory = userSchema.bind(-1)
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
                    private val idV = User::id { Kova.int().min(1) }
                    private val nameV = User::name { Kova.string().min(1).max(10) }

                    fun bind(
                        id: Int,
                        name: String,
                    ): ObjectFactory<User> {
                        val id = idV.bind(id)
                        val name = nameV.bind(name)
                        return create(::User, id, name)
                    }
                }

            test("success") {
                val userFactory = userSchema.bind(1, "abc")
                val result = userFactory.tryCreate()
                result.isSuccess().mustBeTrue()
                result.value shouldBe User(1, "abc")
            }

            test("failure") {
                val userFactory = userSchema.bind(0, "")
                val result = userFactory.tryCreate()
                result.isFailure().mustBeTrue()
                result.details.size shouldBe 2
            }

            test("failure - failFast is true") {
                val userFactory = userSchema.bind(0, "")
                val result = userFactory.tryCreate(ValidationConfig(failFast = true))
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
                    fun bind(
                        id: Int,
                        name: String,
                    ): ObjectFactory<User> {
                        val id = Kova.generic<Int>().bind(id)
                        val name = Kova.generic<String>().bind(name)
                        return create(::User, id, name)
                    }
                }

            test("success") {
                val factory = userSchema.bind(1, "abc")
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
                    private val valueV = Age::value { Kova.int().min(0) }

                    fun bind(age: Int): ObjectFactory<Age> {
                        val arg0 = valueV.bind(age)
                        return create(::Age, arg0)
                    }
                }

            val nameSchema =
                object : ObjectSchema<Name>() {
                    private val valueV = Name::value { Kova.string().notBlank() }

                    fun bind(name: String): ObjectFactory<Name> {
                        val arg0 = valueV.bind(name)
                        return create(::Name, arg0)
                    }
                }

            val personSchema =
                object : ObjectSchema<Person>() {
                    private val nameV = Person::name { nameSchema }
                    private val ageV = Person::age { ageSchema }

                    fun bind(
                        name: String,
                        age: Int,
                    ): ObjectFactory<Person> {
                        val arg0 = nameV.bind(name)
                        val arg1 = ageV.bind(age)
                        return create(::Person, arg0, arg1)
                    }
                }

            test("success") {
                val factory = personSchema.bind("abc", 10)
                val result = factory.tryCreate()
                result.isSuccess().mustBeTrue()
                result.value shouldBe Person(Name("abc"), Age(10))
            }
        }
    })
