package org.komapper.extension.validator

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class ObjectFactoryTest :
    FunSpec({

        context("orDefault - factory") {
            data class User(
                val name: String?,
                val age: Int?,
            )

            val userSchema =
                object : ObjectSchema<User>() {
                    private val name = User::name { Kova.generic() }
                    private val age = User::age { Kova.int().asNullable() }

                    fun build(
                        name: String?,
                        age: Int?,
                    ): ObjectFactory<User> {
                        val arg1 = Kova.arg(this.name.orDefault(""), name)
                        val arg2 = Kova.arg(this.age.orDefault(0), age)
                        val arguments = Kova.arguments(arg1, arg2)
                        return arguments.createFactory(this, ::User)
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
                        val arg1 = Kova.arg(this.id, id)
                        val arguments = Kova.arguments(arg1)
                        return arguments.createFactory(this, ::User)
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
                detail.root shouldContain $$"$User"
                detail.path shouldBe "arg1"
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
                detail.root shouldContain $$"$User"
                detail.path shouldBe "arg1"
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
                        val arg1 = Kova.arg(this.id, id)
                        val arg2 = Kova.arg(this.name, name)
                        val arguments = Kova.arguments(arg1, arg2)
                        return arguments.createFactory(this, ::User)
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

            val userFactoryBuilder =
                object {
                    fun build(
                        id: Int,
                        name: String,
                    ): ObjectFactory<User> {
                        val arg1 = Kova.arg(Kova.generic(), id)
                        val arg2 = Kova.arg(Kova.generic(), name)
                        val arguments = Kova.arguments(arg1, arg2)
                        return arguments.createFactory(Kova.generic(), ::User)
                    }
                }

            test("success") {
                val factory = userFactoryBuilder.build(1, "abc")
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
                        val args = Kova.arguments(Kova.arg(this.value, age))
                        return args.createFactory(this, ::Age)
                    }
                }

            val nameSchema =
                object : ObjectSchema<Name>() {
                    private val value = Name::value { Kova.string().notBlank() }

                    fun build(name: String): ObjectFactory<Name> {
                        val args = Kova.arguments(Arg.Value(this.value, name))
                        return args.createFactory(this, ::Name)
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
                        val arg1 = Kova.arg(this.name, this.name.build(name))
                        val arg2 = Kova.arg(this.age, this.age.build(age))
                        val arguments = Kova.arguments(arg1, arg2)
                        return arguments.createFactory(this, ::Person)
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
