package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import java.time.LocalDate

class ObjectSchemaTest :
    FunSpec({

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

        data class Person(
            val id: Int,
            val firstName: String?,
            val lastName: String?,
            val address: Address?,
        )

        context("plus") {

            val a =
                object : ObjectSchema<User>({
                    User::name { Kova.string().min(1).max(10) }
                }) {}
            val b =
                object : ObjectSchema<User>({
                    User::id { Kova.int().min(1) }
                }) {}

            val userSchema = a + b

            test("success") {
                val user = User(1, "abc")
                val result = userSchema.tryValidate(user)
                result.isSuccess().mustBeTrue()
                result.value shouldBe user
            }

            test("failure - 1 rule violated") {
                val user = User(2, "too-long-name")
                val result = userSchema.tryValidate(user)
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
                val result = userSchema.tryValidate(user)
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

        context("replace") {

            val userSchema =
                object : ObjectSchema<User>({
                    User::id { Kova.int().min(1) }
                    User::name { Kova.string().min(1).max(10) }
                }) {}

            test("success") {
                val user = User(-1, "abc")
                val newSchema = userSchema.replace(User::id, Kova.int().min(-1))
                val result = newSchema.tryValidate(user)
                result.isSuccess().mustBeTrue()
                result.value shouldBe user
            }
        }

        context("constrain") {
            data class Period(
                val startDate: LocalDate,
                val endDate: LocalDate,
            )

            val periodSchema =
                object : ObjectSchema<Period>({
                    constrain("test") {
                        satisfies(
                            it.input.startDate <= it.input.endDate,
                            "startDate must be less than or equal to endDate",
                        )
                    }
                }) {}

            test("success") {
                val period = Period(LocalDate.of(2020, 1, 1), LocalDate.of(2021, 1, 1))
                val result = periodSchema.tryValidate(period)
                result.isSuccess().mustBeTrue()
                result.value shouldBe period
            }

            test("failure") {
                val period = Period(LocalDate.of(2020, 1, 1), LocalDate.of(2019, 1, 1))
                val result = periodSchema.tryValidate(period)
                result.isFailure().mustBeTrue()
                result.details.size shouldBe 1
                result.details[0].let {
                    it.root shouldEndWith $$"$Period"
                    it.path shouldBe ""
                    it.message.content shouldBe "startDate must be less than or equal to endDate"
                }
            }
        }

        context("nullable") {
            val validator =
                object : ObjectSchema<User>({
                    User::id { Kova.int().min(1) }
                    User::name { Kova.string().min(1).max(10) }
                }) {}.asNullable()

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

        context("prop - simple") {

            val userSchema =
                object : ObjectSchema<User>({
                    User::id { Kova.int().min(1) }
                    User::name { Kova.string().min(1).max(10) }
                }) {
                }

            test("success") {
                val user = User(1, "abc")
                val result = userSchema.tryValidate(user)
                result.isSuccess().mustBeTrue()
                result.value shouldBe user
            }

            test("failure - 1 constraint violated") {
                val user = User(2, "too-long-name")
                val result = userSchema.tryValidate(user)
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
                val result = userSchema.tryValidate(user)
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

        context("prop - nest") {

            val streetSchema =
                object : ObjectSchema<Street>({
                    Street::id { Kova.int().min(1) }
                    Street::name { Kova.string().min(3).max(5) }
                }) {}

            val addressSchema =
                object : ObjectSchema<Address>({
                    Address::street { streetSchema }
                }) {}

            val employeeSchema =
                object : ObjectSchema<Employee>({
                    Employee::address { addressSchema }
                }) {}

            test("success") {
                val employee = Employee(1, "abc", Address(1, Street(1, "def")))
                val result = employeeSchema.tryValidate(employee)
                result.isSuccess().mustBeTrue()
                result.value shouldBe employee
            }

            test("failure") {
                val employee = Employee(1, "abc", Address(1, Street(1, "too-long-name")))
                val result = employeeSchema.tryValidate(employee)
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

            val streetSchema =
                object : ObjectSchema<Street>({
                    Street::id { Kova.int().min(1) }
                    Street::name { Kova.string().min(3).max(5) }
                }) {}

            val addressSchema =
                object : ObjectSchema<Address>({
                    Address::street { streetSchema }
                    Address::postalCode choose { address ->
                        val base = Kova.string()
                        when (address.country) {
                            "US" -> base.length(8)
                            else -> base.length(5)
                        }
                    }
                }) {
                }

            val employeeSchema =
                object : ObjectSchema<Employee>({
                    Employee::address { addressSchema }
                }) {}

            test("success - country is US") {
                val employee = Employee(1, "abc", Address(1, Street(1, "def"), country = "US", postalCode = "12345678"))
                val result = employeeSchema.tryValidate(employee)
                result.isSuccess().mustBeTrue()
                result.value shouldBe employee
            }

            test("success - country is not US") {
                val employee = Employee(1, "abc", Address(1, Street(1, "def"), country = "JP", postalCode = "12345"))
                val result = employeeSchema.tryValidate(employee)
                result.isSuccess().mustBeTrue()
                result.value shouldBe employee
            }

            test("failure - country is US") {
                val employee =
                    Employee(1, "abc", Address(1, Street(1, "def"), country = "US", postalCode = "123456789"))
                val result = employeeSchema.tryValidate(employee)
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
                val result = employeeSchema.tryValidate(employee)
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
            val streetSchema =
                object : ObjectSchema<Street>({
                    Street::id { Kova.int().min(1) }
                    Street::name { Kova.string().min(3).max(5) }
                }) {}

            val addressSchema =
                object : ObjectSchema<Address>({
                    Address::street { streetSchema }
                }) {}

            val personSchema =
                object : ObjectSchema<Person>({
                    Person::firstName { Kova.string().asNullable() }
                    Person::lastName { Kova.nullable() }
                    Person::address { addressSchema.asNullable() }
                }) {}

            val personSchema2 =
                object : ObjectSchema<Person>({
                    Person::firstName { Kova.string().notNull() }
                    Person::lastName { Kova.nullable<String>().notNull() }
                    Person::address { addressSchema.asNullable() }
                }) {}

            test("success") {
                val person = Person(1, "abc", "def", Address(1, Street(1, "hij")))
                val result = personSchema.tryValidate(person)
                result.isSuccess().mustBeTrue()
                result.value shouldBe person
            }

            test("success - nullable") {
                val person = Person(1, null, null, null)
                val result = personSchema.tryValidate(person)
                result.isSuccess().mustBeTrue()
                result.value shouldBe person
            }

            test("failure - isNotNull") {
                val person = Person(1, null, null, null)
                val result = personSchema2.tryValidate(person)
                result.isFailure().mustBeTrue()
                result.details.size shouldBe 2
                result.details[0].let {
                    it.root shouldEndWith $$"$Person"
                    it.path shouldBe "firstName"
                    it.message.content shouldBe "Value must not be null"
                }
                result.details[1].let {
                    it.root shouldEndWith $$"$Person"
                    it.path shouldBe "lastName"
                    it.message.content shouldBe "Value must not be null"
                }
            }
        }

        context("recursive") {
            data class Node(
                val children: List<Node> = emptyList(),
            )

            val nodeSchema =
                object : ObjectSchema<Node>({
                    Node::children { Kova.list<Node>().max(3).onEach(caller) }
                }) {
                }

            test("success") {
                val node = Node(listOf(Node(), Node(), Node()))
                val result = nodeSchema.tryValidate(node)
                result.isSuccess().mustBeTrue()
            }

            test("failure - children size > 3") {
                val node = Node(listOf(Node(), Node(), Node(listOf(Node(), Node(), Node(), Node()))))
                val result = nodeSchema.tryValidate(node)
                result.isFailure().mustBeTrue()
                result.details.size shouldBe 1
                result.details[0].path shouldBe "children[2]<collection element>.children"
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Collection(size=4) must have at most 3 elements"
            }

            test("failure - grand children size > 3") {
                val node = Node(listOf(Node(), Node(), Node(listOf(Node(listOf(Node(), Node(), Node(), Node()))))))
                val result = nodeSchema.tryValidate(node)
                result.isFailure().mustBeTrue()
                result.details.size shouldBe 1
                result.details[0].path shouldBe "children[2]<collection element>.children[0]<collection element>.children"
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Collection(size=4) must have at most 3 elements"
            }
        }
    })
