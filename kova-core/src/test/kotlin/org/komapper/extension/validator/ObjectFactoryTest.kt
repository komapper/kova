package org.komapper.extension.validator

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class ObjectFactoryTest :
    FunSpec({

        context("FunctionDesc") {
            test("success when KParameter is available") {
                val desc = FunctionDesc("User", mapOf(0 to "name", 1 to "age"))
                desc[0] shouldBe "name"
                desc[1] shouldBe "age"
            }

            test("success when KParameter is unavailable") {
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
                    private val nameV = User::name { it }
                    private val ageV = User::age { it }

                    fun bind(
                        name: String?,
                        age: Int?,
                    ) = factory {
                        val arg0 = nameV.withDefault("").bind(name)
                        val arg1 = ageV.withDefault(0).bind(age)
                        create(::User, arg0, arg1)
                    }
                }

            test("success with null values") {
                val userFactory = userSchema.bind(null, null)
                val result = userFactory.tryCreate()
                result.isSuccess().mustBeTrue()
                result.value shouldBe User("", 0)
            }

            test("success with non-null values") {
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
                    private val idV = User::id { it.min(1) }

                    fun bind(id: Int) =
                        factory {
                            val arg0 = idV.bind(id)
                            create(::User, arg0)
                        }
                }

            test("success when using tryCreate") {
                val factory = userSchema.bind(1)
                val result = factory.tryCreate()
                result.isSuccess().mustBeTrue()
                result.value shouldBe User(1)
            }

            test("success when using create") {
                val factory = userSchema.bind(1)
                val user = factory.create()
                user shouldBe User(1)
            }

            test("failure when using tryCreate") {
                val factory = userSchema.bind(-1)
                val result = factory.tryCreate()
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                val message = result.messages.first()
                message.root shouldContain "<init>"
                message.path.fullName shouldBe "id"
                message.constraintId shouldBe "kova.comparable.min"
            }

            test("failure when using create") {
                val factory = userSchema.bind(-1)
                val ex =
                    shouldThrow<ValidationException> {
                        factory.create()
                    }
                ex.messages.size shouldBe 1
                val message = ex.messages.first()
                message.root shouldContain "<init>"
                message.path.fullName shouldBe "id"
                message.constraintId shouldBe "kova.comparable.min"
            }
        }

        context("2 args") {

            data class User(
                val id: Int,
                val name: String,
            )

            val userSchema =
                object : ObjectSchema<User>() {
                    private val idV = User::id { it.min(1) }
                    private val nameV = User::name { it.min(1).max(10) }

                    fun bind(
                        id: Int,
                        name: String,
                    ) = factory {
                        val id = idV.bind(id)
                        val name = nameV.bind(name)
                        create(::User, id, name)
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
                result.messages.size shouldBe 2
            }

            test("failure when failFast is true") {
                val userFactory = userSchema.bind(0, "")
                val result = userFactory.tryCreate(ValidationConfig(failFast = true))
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
            }
        }

        context("2 args with generic validator") {
            data class User(
                val id: Int,
                val name: String,
            )

            val userSchema =
                object : ObjectSchema<User>() {
                    fun bind(
                        id: Int,
                        name: String,
                    ) = factory {
                        val id = Kova.generic<Int>().bind(id)
                        val name = Kova.generic<String>().bind(name)
                        create(::User, id, name)
                    }
                }

            test("success") {
                val factory = userSchema.bind(1, "abc")
                val result = factory.tryCreate()
                result.isSuccess().mustBeTrue()
                result.value shouldBe User(1, "abc")
            }
        }

        context("2 args with nested factory") {
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
                    private val valueV = Age::value { it.min(0) }

                    fun bind(age: Int) =
                        factory {
                            val arg0 = valueV.bind(age)
                            create(::Age, arg0)
                        }
                }

            val nameSchema =
                object : ObjectSchema<Name>() {
                    private val valueV = Name::value { it.notBlank() }

                    fun bind(name: String) =
                        factory {
                            val arg0 = valueV.bind(name)
                            create(::Name, arg0)
                        }
                }

            val personSchema =
                object : ObjectSchema<Person>() {
                    private val nameV = Person::name { nameSchema }
                    private val ageV = Person::age { ageSchema }

                    fun bind(
                        name: String,
                        age: Int,
                    ) = factory {
                        val arg0 = nameV.bind(name)
                        val arg1 = ageV.bind(age)
                        create(::Person, arg0, arg1)
                    }
                }

            test("success") {
                val factory = personSchema.bind("abc", 10)
                val result = factory.tryCreate()
                result.isSuccess().mustBeTrue()
                result.value shouldBe Person(Name("abc"), Age(10))
            }
        }

        context("dynamic validator") {
            data class Street(
                val id: Int,
                val name: String,
            )

            data class Address(
                val id: Int,
                val street: Street,
                val country: String = "US",
                val postalCode: String = "",
            )

            data class Employee(
                val id: Int,
                val name: String,
                val address: Address,
            )

            val streetSchema =
                object : ObjectSchema<Street>() {
                    val idV = Street::id { it.min(1) }
                    val nameV = Street::name { it.min(3).max(5) }

                    fun bind(
                        id: Int,
                        name: String,
                    ) = factory {
                        create(::Street, idV.bind(id), nameV.bind(name))
                    }
                }

            val addressSchema =
                object : ObjectSchema<Address>() {
                    val idV = Address::id { it.positive() }
                    val streetV = Address::street { streetSchema }
                    val countryV = Address::country { it.notBlank() }
                    val postalCodeV =
                        Address::postalCode.choose({ it.country }) { country, v ->
                            when (country) {
                                "US" -> v.length(8)
                                else -> v.length(5)
                            }
                        }

                    fun bind(
                        id: Int,
                        streetId: Int,
                        street: String,
                        country: String,
                        postalCode: String,
                    ) = factory {
                        create(
                            ::Address,
                            idV.bind(id),
                            streetV.bind(streetId, street),
                            countryV.bind(country),
                            postalCodeV(country).bind(postalCode),
                        )
                    }
                }

            val employeeSchema =
                object : ObjectSchema<Employee>() {
                    val idV = Employee::id { it.positive() }
                    val nameV = Employee::name { it.notBlank() }
                    val addressV = Employee::address { addressSchema }

                    fun bind(
                        id: Int,
                        name: String,
                        addressFactory: ObjectFactory<Address>,
                    ) = factory {
                        create(::Employee, idV.bind(id), nameV.bind(name), addressFactory)
                    }
                }

            test("success") {
                val addressFactory = addressSchema.bind(1, 1, "abc", "US", "12345678")
                val employeeFactory = employeeSchema.bind(1, "abc", addressFactory)
                val result = employeeFactory.tryCreate()
                result.isSuccess().mustBeTrue(result.toString())
                result.value.id shouldBe 1
            }
        }
    })
