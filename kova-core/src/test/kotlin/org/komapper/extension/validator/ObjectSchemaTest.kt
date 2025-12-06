package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
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
                object : ObjectSchema<User>() {
                    val name = User::name { Kova.string().min(1).max(10) }
                }
            val b =
                object : ObjectSchema<User>() {
                    val id = User::id { Kova.int().min(1) }
                }

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
                result.messages.size shouldBe 1
                result.messages[0].let {
                    it.root shouldBe "User"
                    it.path?.fullName shouldBe "name"
                    it.content shouldBe "\"too-long-name\" must be at most 10 characters"
                }
            }

            test("failure - 2 rules violated") {
                val user = User(0, "too-long-name")
                val result = userSchema.tryValidate(user)
                result.isFailure().mustBeTrue()

                result.messages.size shouldBe 2
                result.messages[0].let {
                    it.root shouldBe "User"
                    it.path?.fullName shouldBe "name"
                    it.content shouldBe "\"too-long-name\" must be at most 10 characters"
                }
                result.messages[1].let {
                    it.root shouldBe "User"
                    it.path?.fullName shouldBe "id"
                    it.content shouldBe "Number 0 must be greater than or equal to 1"
                }
            }
        }

        context("replace") {

            val userSchema =
                object : ObjectSchema<User>() {
                    val id = User::id { Kova.int().min(1) }
                    val name = User::name { Kova.string().min(1).max(10) }
                }

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
                result.messages.size shouldBe 1
                result.messages[0].let {
                    it.root shouldBe "Period"
                    it.path?.fullName shouldBe ""
                    it.content shouldBe "startDate must be less than or equal to endDate"
                }
            }
        }

        context("nullable") {
            val validator =
                object : ObjectSchema<User>() {
                    val id = User::id { Kova.int().min(1) }
                    val name = User::name { Kova.string().min(1).max(10) }
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

        context("prop - simple") {

            val userSchema =
                object : ObjectSchema<User>() {
                    val id = User::id { Kova.int().min(1) }
                    val name = User::name { Kova.string().min(1).max(10) }
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
                result.messages.size shouldBe 1
                result.messages[0].let {
                    it.root shouldBe "User"
                    it.path?.fullName shouldBe "name"
                    it.content shouldBe "\"too-long-name\" must be at most 10 characters"
                }
            }

            test("failure - 2 constraints violated") {
                val user = User(0, "too-long-name")
                val result = userSchema.tryValidate(user)
                result.isFailure().mustBeTrue()

                result.messages.size shouldBe 2
                result.messages[0].let {
                    it.root shouldBe "User"
                    it.path?.fullName shouldBe "id"
                    it.content shouldBe "Number 0 must be greater than or equal to 1"
                }
                result.messages[1].let {
                    it.root shouldBe "User"
                    it.path?.fullName shouldBe "name"
                    it.content shouldBe "\"too-long-name\" must be at most 10 characters"
                }
            }
        }

        context("prop - nest") {

            val streetSchema =
                object : ObjectSchema<Street>() {
                    val id = Street::id { Kova.int().min(1) }
                    val name = Street::name { Kova.string().min(3).max(5) }
                }

            val addressSchema =
                object : ObjectSchema<Address>() {
                    val street = Address::street { streetSchema }
                }

            val employeeSchema =
                object : ObjectSchema<Employee>() {
                    val address = Employee::address { addressSchema }
                }

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
                result.messages.size shouldBe 1
                result.messages[0].let {
                    it.root shouldBe "Employee"
                    it.path?.fullName shouldBe "address.street.name"
                    it.content shouldBe "\"too-long-name\" must be at most 5 characters"
                }
            }
        }

        context("prop - nest - dynamic") {

            val streetSchema =
                object : ObjectSchema<Street>() {
                    val id = Street::id { Kova.int().min(1) }
                    val name = Street::name { Kova.string().min(3).max(5) }
                }

            val addressSchema =
                object : ObjectSchema<Address>() {
                    val street = Address::street { streetSchema }
                    val postalCode =
                        Address::postalCode choose { address ->
                            val base = Kova.string()
                            when (address.country) {
                                "US" -> base.length(8)
                                else -> base.length(5)
                            }
                        }
                }

            val employeeSchema =
                object : ObjectSchema<Employee>() {
                    val address = Employee::address { addressSchema }
                }

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
                result.messages.size shouldBe 1
                result.messages[0].let {
                    it.root shouldBe "Employee"
                    it.path?.fullName shouldBe "address.postalCode"
                    it.content shouldBe "\"123456789\" must be exactly 8 characters"
                }
            }

            test("failure - country is not US") {
                val employee =
                    Employee(1, "abc", Address(1, Street(1, "def"), country = "JP", postalCode = "123456789"))
                val result = employeeSchema.tryValidate(employee)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].let {
                    it.root shouldBe "Employee"
                    it.path?.fullName shouldBe "address.postalCode"
                    it.content shouldBe "\"123456789\" must be exactly 5 characters"
                }
            }
        }

        context("prop - nullable") {
            val streetSchema =
                object : ObjectSchema<Street>() {
                    val id = Street::id { Kova.int().min(1) }
                    val name = Street::name { Kova.string().min(3).max(5) }
                }

            val addressSchema =
                object : ObjectSchema<Address>() {
                    val street = Address::street { streetSchema }
                }

            val personSchema =
                object : ObjectSchema<Person>() {
                    val firstName = Person::firstName { Kova.string().asNullable() }
                    val lastName = Person::lastName { Kova.nullable() }
                    val address = Person::address { addressSchema.asNullable() }
                }

            val personSchema2 =
                object : ObjectSchema<Person>() {
                    val firstName = Person::firstName { Kova.nullable<String>().notNull() }
                    val lastName = Person::lastName { Kova.nullable<String>().notNull() }
                    val address = Person::address { addressSchema.asNullable() }
                }

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
                result.messages.size shouldBe 2
                result.messages[0].let {
                    it.root shouldBe "Person"
                    it.path?.fullName shouldBe "firstName"
                    it.content shouldBe "Value must not be null"
                }
                result.messages[1].let {
                    it.root shouldBe "Person"
                    it.path?.fullName shouldBe "lastName"
                    it.content shouldBe "Value must not be null"
                }
            }
        }

        context("recursive") {
            data class Node(
                val children: List<Node> = emptyList(),
            )

            val nodeSchema =
                object : ObjectSchema<Node>() {
                    val children = Node::children { Kova.list<Node>().max(3).onEach(this) }
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
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe
                    "Some elements in the collection do not satisfy the constraint: [Collection(size=4) must have at most 3 elements]"
            }

            test("failure - grand children size > 3") {
                val node = Node(listOf(Node(), Node(), Node(listOf(Node(listOf(Node(), Node(), Node(), Node()))))))
                val result = nodeSchema.tryValidate(node)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe
                    "Some elements in the collection do not satisfy the constraint: [Some elements in the collection do not satisfy the constraint: [Collection(size=4) must have at most 3 elements]]"
            }
        }

        context("circular reference detection") {
            data class NodeWithValue(
                val value: Int,
                var next: NodeWithValue?,
            )

            val nodeSchema =
                object : ObjectSchema<NodeWithValue>() {
                    val value = NodeWithValue::value { Kova.int().min(0).max(100) }
                    val next = NodeWithValue::next { Kova.nullable<NodeWithValue>().then(this) }
                }

            test("circular reference detected - validation succeeds without error") {
                val node1 = NodeWithValue(10, null)
                val node2 = NodeWithValue(20, node1)
                node1.next = node2 // Create circular reference: node1 -> node2 -> node1

                val result = nodeSchema.tryValidate(node1)
                result.isSuccess().mustBeTrue()
            }

            test("non-circular nested objects - all valid") {
                val node4 = NodeWithValue(40, null)
                val node3 = NodeWithValue(30, node4)
                val node2 = NodeWithValue(20, node3)
                val node1 = NodeWithValue(10, node2)

                val result = nodeSchema.tryValidate(node1)
                result.isSuccess().mustBeTrue()
            }

            test("constraint violation in nested object") {
                val node3 = NodeWithValue(150, null) // Invalid: > 100
                val node2 = NodeWithValue(20, node3)
                val node1 = NodeWithValue(10, node2)

                val result = nodeSchema.tryValidate(node1)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].path?.fullName shouldBe "next.next.value"
                result.messages[0].content shouldBe "Number 150 must be less than or equal to 100"
            }

            test("constraint violation in root object") {
                val node2 = NodeWithValue(20, null)
                val node1 = NodeWithValue(-5, node2) // Invalid: < 0

                val result = nodeSchema.tryValidate(node1)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].path?.fullName shouldBe "value"
                result.messages[0].content shouldBe "Number -5 must be greater than or equal to 0"
            }

            test("circular reference with constraint violation - stops before revisiting") {
                val node1 = NodeWithValue(200, null) // Invalid: > 100
                val node2 = NodeWithValue(20, node1)
                node1.next = node2 // Create circular reference

                val result = nodeSchema.tryValidate(node1)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].path?.fullName shouldBe "value"
                result.messages[0].content shouldBe "Number 200 must be less than or equal to 100"
            }
        }
    })
