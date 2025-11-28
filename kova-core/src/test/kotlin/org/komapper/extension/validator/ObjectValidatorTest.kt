package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import java.time.LocalDate

class ObjectValidatorTest :
    FunSpec({

        context("plus") {
            val a =
                Kova.validator {
                    User::class {
                        User::name { Kova.string().min(1).max(10) }
                    }
                }
            val b =
                Kova.validator {
                    User::class {
                        User::id { Kova.int().min(1) }
                    }
                }
            val validator = a + b

            test("success") {
                val user = User(1, "abc")
                val result = validator.tryValidate(user)
                result.isSuccess().mustBeTrue()
                result.value shouldBe user
            }

            test("success - replace existing rule") {
                val user = User(-1, "abc")
                val result =
                    validator
                        .merge {
                            User::id { Kova.int().min(-1) }
                        }.tryValidate(user)
                result.isSuccess().mustBeTrue()
                result.value shouldBe user
            }

            test("failure - 1 rule violated") {
                val user = User(2, "too-long-name")
                val result = validator.tryValidate(user)
                result.isFailure().mustBeTrue()
                result.details.size shouldBe 1
                result.details[0].let {
                    it.root shouldEndWith $$"$User"
                    it.path shouldBe "name"
                    it.message.content shouldBe "\"too-long-name\" must be at most 10 characters"
                }
            }

            test("failure - 2 rules violated") {
                val user = User(0, "too-long-name")
                val result = validator.tryValidate(user)
                result.isFailure().mustBeTrue()

                result.details.size shouldBe 2
                result.details[0].let {
                    it.root shouldEndWith $$"$User"
                    it.path shouldBe "name"
                    it.message.content shouldBe "\"too-long-name\" must be at most 10 characters"
                }
                result.details[1].let {
                    it.root shouldEndWith $$"$User"
                    it.path shouldBe "id"
                    it.message.content shouldBe "Number 0 must be greater than or equal to 1"
                }
            }
        }

        context("constrain") {
            val validator =
                Kova.validator {
                    Period::class {
                        constrain("test") {
                            if (it.input.startDate <= it.input.endDate) {
                                ConstraintResult.Satisfied
                            } else {
                                val message = Message.Text("startDate must be less than or equal to endDate")
                                ConstraintResult.Violated(message)
                            }
                        }
                    }
                }

            test("success") {
                val period = Period(LocalDate.of(2020, 1, 1), LocalDate.of(2021, 1, 1))
                val result = validator.tryValidate(period)
                result.isSuccess().mustBeTrue()
                result.value shouldBe period
            }

            test("failure") {
                val period = Period(LocalDate.of(2020, 1, 1), LocalDate.of(2019, 1, 1))
                val result = validator.tryValidate(period)
                result.isFailure().mustBeTrue()
                result.details.size shouldBe 1
                result.details[0].let {
                    it.root shouldEndWith $$"$Period"
                    it.path shouldBe ""
                    it.message.content shouldBe "startDate must be less than or equal to endDate"
                }
            }
        }

        context("prop - simple") {
            val validator =
                Kova.validator {
                    User::class {
                        User::id { Kova.int().min(1) }
                        User::name { Kova.string().min(1).max(10) }
                    }
                }

            test("success") {
                val user = User(1, "abc")
                val result = validator.tryValidate(user)
                result.isSuccess().mustBeTrue()
                result.value shouldBe user
            }

            test("failure - 1 constraint violated") {
                val user = User(2, "too-long-name")
                val result = validator.tryValidate(user)
                result.isFailure().mustBeTrue()
                result.details.size shouldBe 1
                result.details[0].let {
                    it.root shouldEndWith $$"$User"
                    it.path shouldBe "name"
                    it.message.content shouldBe "\"too-long-name\" must be at most 10 characters"
                }
            }

            test("failure - 2 constraints violated") {
                val user = User(0, "too-long-name")
                val result = validator.tryValidate(user)
                result.isFailure().mustBeTrue()

                result.details.size shouldBe 2
                result.details[0].let {
                    it.root shouldEndWith $$"$User"
                    it.path shouldBe "id"
                    it.message.content shouldBe "Number 0 must be greater than or equal to 1"
                }
                result.details[1].let {
                    it.root shouldEndWith $$"$User"
                    it.path shouldBe "name"
                    it.message.content shouldBe "\"too-long-name\" must be at most 10 characters"
                }
            }
        }

        context("validator - nullable") {
            val validator =
                Kova
                    .validator {
                        User::class {
                            User::id { Kova.int().min(1) }
                            User::name { Kova.string().min(1).max(10) }
                        }
                    }.asNullable()

            test("success - non null") {
                val user = User(1, "abc")
                val result = validator.tryValidate(user)
                result.isSuccess().mustBeTrue()
                result.value shouldBe user
            }

            test("success - null") {
                val result = validator.tryValidate(null)
                result.isSuccess().mustBeTrue()
                result.value shouldBe null
            }
        }

        context("prop - nest") {
            val streetValidator =
                Kova.validator {
                    Street::class {
                        Street::name { Kova.string().min(3).max(5) }
                    }
                }
            val addressValidator =
                Kova.validator {
                    Address::class {
                        Address::street { streetValidator }
                    }
                }
            val employeeValidator =
                Kova.validator {
                    Employee::class {
                        Employee::address { addressValidator }
                    }
                }

            test("success") {
                val employee = Employee(1, "abc", Address(1, Street(1, "def")))
                val result = employeeValidator.tryValidate(employee)
                result.isSuccess().mustBeTrue()
                result.value shouldBe employee
            }

            test("failure") {
                val employee = Employee(1, "abc", Address(1, Street(1, "too-long-name")))
                val result = employeeValidator.tryValidate(employee)
                result.isFailure().mustBeTrue()
                result.details.size shouldBe 1
                result.details[0].let {
                    it.root shouldEndWith $$"$Employee"
                    it.path shouldBe "address.street.name"
                    it.message.content shouldBe "\"too-long-name\" must be at most 5 characters"
                }
            }
        }

        context("prop - nest - dynamic") {
            val streetValidator =
                Kova.validator {
                    Street::class {
                        Street::name { Kova.string().min(3).max(5) }
                    }
                }

            val addressValidator =
                Kova.validator {
                    Address::class {
                        Address::street { streetValidator }
                        Address::postalCode { address ->
                            val base = Kova.string()
                            when (address.country) {
                                "US" -> base.length(8)
                                else -> base.length(5)
                            }
                        }
                    }
                }

            val employeeValidator =
                Kova.validator {
                    Employee::class {
                        Employee::address { addressValidator }
                    }
                }

            test("success - country is US") {
                val employee = Employee(1, "abc", Address(1, Street(1, "def"), country = "US", postalCode = "12345678"))
                val result = employeeValidator.tryValidate(employee)
                result.isSuccess().mustBeTrue()
                result.value shouldBe employee
            }

            test("success - country is not US") {
                val employee = Employee(1, "abc", Address(1, Street(1, "def"), country = "JP", postalCode = "12345"))
                val result = employeeValidator.tryValidate(employee)
                result.isSuccess().mustBeTrue()
                result.value shouldBe employee
            }

            test("failure - country is US") {
                val employee =
                    Employee(1, "abc", Address(1, Street(1, "def"), country = "US", postalCode = "123456789"))
                val result = employeeValidator.tryValidate(employee)
                result.isFailure().mustBeTrue()
                result.details.size shouldBe 1
                result.details[0].let {
                    it.root shouldEndWith $$"$Employee"
                    it.path shouldBe "address.postalCode"
                    it.message.content shouldBe "\"123456789\" must be exactly 8 characters"
                }
            }

            test("failure - country is not US") {
                val employee =
                    Employee(1, "abc", Address(1, Street(1, "def"), country = "JP", postalCode = "123456789"))
                val result = employeeValidator.tryValidate(employee)
                result.isFailure().mustBeTrue()
                result.details.size shouldBe 1
                result.details[0].let {
                    it.root shouldEndWith $$"$Employee"
                    it.path shouldBe "address.postalCode"
                    it.message.content shouldBe "\"123456789\" must be exactly 5 characters"
                }
            }
        }

        context("prop - nullable") {
            val streetValidator =
                Kova.validator {
                    Street::class {
                        Street::name { Kova.string().min(3).max(5) }
                    }
                }
            val addressValidator =
                Kova.validator {
                    Address::class {
                        Address::street { streetValidator }
                    }
                }
            val personValidator =
                Kova.validator {
                    Person::class {
                        Person::name { Kova.nullable() }
                        Person::address { addressValidator.asNullable() }
                    }
                }
            val personValidator2 =
                Kova.validator {
                    Person::class {
                        Person::name { Kova.nullable<String>().isNotNull() }
                        Person::address { addressValidator.asNullable().isNotNull() }
                    }
                }

            test("success") {
                val employee = Person(1, "abc", Address(1, Street(1, "def")))
                val result = personValidator.tryValidate(employee)
                result.isSuccess().mustBeTrue()
                result.value shouldBe employee
            }

            test("success - nullable") {
                val person = Person(1, null, null)
                val result = personValidator.tryValidate(person)
                result.isSuccess().mustBeTrue()
                result.value shouldBe person
            }

            test("failure - isNotNull") {
                val person = Person(1, null, null)
                val result = personValidator2.tryValidate(person)
                result.isFailure().mustBeTrue()
                result.details.size shouldBe 2
                result.details[0].let {
                    it.root shouldEndWith $$"$Person"
                    it.path shouldBe "name"
                    it.message.content shouldBe "Value must not be null"
                }
                result.details[1].let {
                    it.root shouldEndWith $$"$Person"
                    it.path shouldBe "address"
                    it.message.content shouldBe "Value must not be null"
                }
            }
        }

        context("named prop - simple") {
            val validator =
                Kova.validator {
                    User::class {
                        named("ID", { it.id }) { Kova.int().min(1) }
                        named("NAME", { it.name }) { Kova.string().min(1).max(10) }
                    }
                }

            test("success") {
                val user = User(1, "abc")
                val result = validator.tryValidate(user)
                result.isSuccess().mustBeTrue()
                result.value shouldBe user
            }

            test("failure") {
                val user = User(0, "")
                val result = validator.tryValidate(user)
                result.isFailure().mustBeTrue()
                result.details.size shouldBe 2
                with(result.details[0]) {
                    root shouldEndWith $$"$User"
                    path shouldBe "ID"
                    message.content shouldBe "Number 0 must be greater than or equal to 1"
                }
                with(result.details[1]) {
                    root shouldEndWith $$"$User"
                    path shouldBe "NAME"
                    message.content shouldBe "\"\" must be at least 1 characters"
                }
            }
        }

        context("named prop - nest") {
            val streetValidator =
                Kova.validator {
                    Street::class {
                        named("name", { it.name }) { Kova.string().min(3).max(5) }
                    }
                }
            val addressValidator =
                Kova.validator {
                    Address::class {
                        named("street", { it.street }) { streetValidator }
                    }
                }
            val employeeValidator =
                Kova.validator {
                    Employee::class {
                        named("address", { it.address }) { addressValidator }
                    }
                }

            test("success") {
                val employee = Employee(1, "abc", Address(1, Street(1, "def")))
                val result = employeeValidator.tryValidate(employee)
                result.isSuccess().mustBeTrue()
                result.value shouldBe employee
            }

            test("failure") {
                val employee = Employee(1, "abc", Address(1, Street(1, "too-long-name")))
                val result = employeeValidator.tryValidate(employee)
                result.isFailure().mustBeTrue()
                result.details.size shouldBe 1
                result.details[0].let {
                    it.root shouldEndWith $$"$Employee"
                    it.path shouldBe "address.street.name"
                    it.message.content shouldBe "\"too-long-name\" must be at most 5 characters"
                }
            }
        }

        context("obj - map with name") {
            val nullable = Kova.string().min(1).asNullable()
            val validator = Kova.obj<Request>().map("key") { it["key"] }.andThen(nullable)

            test("success") {
                val result = validator.tryValidate(Request(mapOf("key" to "abc")))
                result.isSuccess().mustBeTrue()
                result.value shouldBe "abc"
            }

            test("failure") {
                val result = validator.tryValidate(Request(mapOf("key" to "")))
                result.isFailure().mustBeTrue()
                result.details.size shouldBe 1
                result.details[0].let {
                    println(it)
                    it.root shouldEndWith $$"$Request"
                    it.path shouldBe "key"
                    it.message.content shouldBe "\"\" must be at least 1 characters"
                }
            }
        }

        context("obj - nullable") {
            val nullableString = Kova.nullable<String>().whenNotNull(Kova.string().min(1))

            val validator = Kova.obj<Request>().map("key") { it["key"] }.andThen(nullableString)

            test("success") {
                val result = validator.tryValidate(Request(mapOf("key" to "abc")))
                result.isSuccess().mustBeTrue()
                result.value shouldBe "abc"
            }

            test("failure") {
                val result = validator.tryValidate(Request(mapOf("key" to "")))
                result.isFailure().mustBeTrue()
                result.details.size shouldBe 1
                result.details[0].let {
                    it.root shouldEndWith $$"$Request"
                    it.path shouldBe "key"
                    it.message.content shouldBe "\"\" must be at least 1 characters"
                }
            }
        }
    }) {
    data class User(
        val id: Int,
        val name: String,
    )

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

    data class Period(
        val startDate: LocalDate,
        val endDate: LocalDate,
    )

    data class Department(
        val id: Int,
        val name: String,
    ) {
        init {
            require(id > 0) { "id must be greater than zero." }
        }
    }

    data class Person(
        val id: Int,
        val name: String?,
        val address: Address?,
    )

    data class Request(
        private val map: Map<String, String>,
    ) {
        operator fun get(key: String): String? = map[key]
    }
}
