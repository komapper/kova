package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import java.time.LocalDate

class SchemaTest :
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

        context("and") {
            fun Validation.validate(user: User) =
                user.schema {
                    user::name {
                        minLength(it, 1)
                        maxLength(it, 10)
                    }
                    user::id { min(it, 1) }
                }

            test("success") {
                val user = User(1, "abc")
                val result = tryValidate { validate(user) }
                result.shouldBeSuccess()
            }

            test("failure with 1 rule violated") {
                val user = User(2, "too-long-name")
                val result = tryValidate { validate(user) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].let {
                    it.root shouldBe "User"
                    it.path.fullName shouldBe "name"
                    it.constraintId shouldBe "kova.charSequence.maxLength"
                }
            }

            test("failure with 2 rules violated") {
                val user = User(0, "too-long-name")
                val result = tryValidate { validate(user) }
                result.shouldBeFailure()

                result.messages.size shouldBe 2
                result.messages[0].let {
                    it.root shouldBe "User"
                    it.path.fullName shouldBe "name"
                    it.constraintId shouldBe "kova.charSequence.maxLength"
                }
                result.messages[1].let {
                    it.root shouldBe "User"
                    it.path.fullName shouldBe "id"
                    it.constraintId shouldBe "kova.comparable.min"
                }
            }
        }

        context("or") {
            fun Validation.validate(user: User) =
                user.schema {
                    or<Unit> {
                        user::name {
                            minLength(it, 1)
                            maxLength(it, 10)
                        }
                    } orElse { user::id { min(it, 1) } }
                }

            test("success when both schemas are satisfied") {
                val user = User(1, "abc")
                val result = tryValidate { validate(user) }
                result.shouldBeSuccess()
            }

            test("success when first schema is satisfied and second fails") {
                val user = User(0, "abc")
                val result = tryValidate { validate(user) }
                result.shouldBeSuccess()
            }

            test("success when first schema fails and second is satisfied") {
                val user = User(1, "too-long-name")
                val result = tryValidate { validate(user) }
                result.shouldBeSuccess()
            }

            test("failure when both schemas fail") {
                val user = User(0, "too-long-name")
                val result = tryValidate { validate(user) }
                result.shouldBeFailure()

                result.messages.size shouldBe 1
                result.messages[0].let {
                    it.constraintId shouldBe "kova.or"
                    it.text shouldBe
                        "at least one constraint must be satisfied: [[must be at most 10 characters], [must be greater than or equal to 1]]"
                }
            }
        }

        context("constrain") {
            data class Period(
                val startDate: LocalDate,
                val endDate: LocalDate,
            )

            fun Validation.validate(period: Period) =
                period.schema {
                    period.constrain("test") {
                        satisfies(it.startDate <= it.endDate) { text("startDate must be less than or equal to endDate") }
                    }
                }

            test("success") {
                val period = Period(LocalDate.of(2020, 1, 1), LocalDate.of(2021, 1, 1))
                val result = tryValidate { validate(period) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val period = Period(LocalDate.of(2020, 1, 1), LocalDate.of(2019, 1, 1))
                val result = tryValidate { validate(period) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].let {
                    it.root shouldBe "Period"
                    it.path.fullName shouldBe ""
                    it.text shouldBe "startDate must be less than or equal to endDate"
                }
            }
        }

        context("nullable") {
            fun Validation.validate(user: User?) =
                user?.schema {
                    user::id { min(it, 1) }
                    user::name {
                        minLength(it, 1)
                        maxLength(it, 10)
                    }
                }

            test("success with non-null value") {
                val user = User(1, "abc")
                val result = tryValidate { validate(user) }
                result.shouldBeSuccess()
            }

            test("success with null value") {
                val result = tryValidate { validate(null) }
                result.shouldBeSuccess()
            }
        }

        context("prop - simple") {
            fun Validation.validate(user: User) =
                user.schema {
                    user::id { min(it, 1) }
                    user::name {
                        minLength(it, 1)
                        maxLength(it, 10)
                    }
                }

            test("success") {
                val user = User(1, "abc")
                val result = tryValidate { validate(user) }
                result.shouldBeSuccess()
            }

            test("failure with 1 constraint violated") {
                val user = User(2, "too-long-name")
                val result = tryValidate { validate(user) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].let {
                    it.root shouldBe "User"
                    it.path.fullName shouldBe "name"
                    it.constraintId shouldBe "kova.charSequence.maxLength"
                }
            }

            test("failure with 2 constraints violated") {
                val user = User(0, "too-long-name")
                val result = tryValidate { validate(user) }
                result.shouldBeFailure()

                result.messages.size shouldBe 2
                result.messages[0].let {
                    it.root shouldBe "User"
                    it.path.fullName shouldBe "id"
                    it.constraintId shouldBe "kova.comparable.min"
                }
                result.messages[1].let {
                    it.root shouldBe "User"
                    it.path.fullName shouldBe "name"
                    it.constraintId shouldBe "kova.charSequence.maxLength"
                }
            }
        }

        context("prop - nest") {
            fun Validation.validate(street: Street) =
                street.schema {
                    street::id { min(it, 1) }
                    street::name {
                        minLength(it, 3)
                        maxLength(it, 5)
                    }
                }

            fun Validation.validate(address: Address) = address.schema { address::street { validate(it) } }

            fun Validation.validate(employee: Employee) = employee.schema { employee::address { validate(it) } }

            test("success") {
                val employee = Employee(1, "abc", Address(1, Street(1, "def")))
                val result = tryValidate { validate(employee) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val employee = Employee(1, "abc", Address(1, Street(1, "too-long-name")))
                val result = tryValidate { validate(employee) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].let {
                    it.root shouldBe "Employee"
                    it.path.fullName shouldBe "address.street.name"
                    it.constraintId shouldBe "kova.charSequence.maxLength"
                }
            }
        }

        context("prop - nest - dynamic") {
            fun Validation.validate(street: Street) =
                street.schema {
                    street::id { min(it, 1) }
                    street::name {
                        minLength(it, 3)
                        maxLength(it, 5)
                    }
                }

            fun Validation.validate(address: Address) =
                address.schema {
                    address::street { validate(it) }
                    address::postalCode {
                        when (address.country) {
                            "US" -> length(it, 8)
                            else -> length(it, 5)
                        }
                    }
                }

            fun Validation.validate(employee: Employee) = employee.schema { employee::address { validate(it) } }

            test("success when country is US") {
                val employee = Employee(1, "abc", Address(1, Street(1, "def"), country = "US", postalCode = "12345678"))
                val result = tryValidate { validate(employee) }
                result.shouldBeSuccess()
            }

            test("success when country is not US") {
                val employee = Employee(1, "abc", Address(1, Street(1, "def"), country = "JP", postalCode = "12345"))
                val result = tryValidate { validate(employee) }
                result.shouldBeSuccess()
            }

            test("failure when country is US") {
                val employee =
                    Employee(1, "abc", Address(1, Street(1, "def"), country = "US", postalCode = "123456789"))
                val result = tryValidate { validate(employee) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].let {
                    it.root shouldBe "Employee"
                    it.path.fullName shouldBe "address.postalCode"
                    it.constraintId shouldBe "kova.charSequence.length"
                }
            }

            test("failure when country is not US") {
                val employee =
                    Employee(1, "abc", Address(1, Street(1, "def"), country = "JP", postalCode = "123456789"))
                val result = tryValidate { validate(employee) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].let {
                    it.root shouldBe "Employee"
                    it.path.fullName shouldBe "address.postalCode"
                    it.constraintId shouldBe "kova.charSequence.length"
                }
            }
        }

        context("prop - nullable") {
            fun Validation.validate(street: Street) =
                street.schema {
                    street::id { min(it, 1) }
                    street::name {
                        minLength(it, 3)
                        maxLength(it, 5)
                    }
                }

            fun Validation.validate(address: Address) = address.schema { address::street { validate(it) } }

            fun Validation.validate(person: Person) =
                person.schema {
                    person::firstName { }
                    person::lastName { }
                    person::address { if (it != null) validate(it) }
                }

            fun Validation.validate2(person: Person) =
                person.schema {
                    person::firstName { notNull(it) }
                    person::lastName { notNull(it) }
                    person::address { if (it != null) validate(it) }
                }

            test("success") {
                val person = Person(1, "abc", "def", Address(1, Street(1, "hij")))
                val result = tryValidate { validate(person) }
                result.shouldBeSuccess()
            }

            test("success with nullable values") {
                val person = Person(1, null, null, null)
                val result = tryValidate { validate(person) }
                result.shouldBeSuccess()
            }

            test("failure when not null constraint violated") {
                val person = Person(1, null, null, null)
                val result = tryValidate { validate2(person) }
                result.shouldBeFailure()
                result.messages.size shouldBe 2
                result.messages[0].let {
                    it.root shouldBe "Person"
                    it.path.fullName shouldBe "firstName"
                    it.constraintId shouldBe "kova.nullable.notNull"
                }
                result.messages[1].let {
                    it.root shouldBe "Person"
                    it.path.fullName shouldBe "lastName"
                    it.constraintId shouldBe "kova.nullable.notNull"
                }
            }
        }

        context("recursive") {
            data class Node(
                val children: List<Node> = emptyList(),
            )

            fun Validation.validate(node: Node) {
                node.schema {
                    node::children {
                        max(it, 3)
                        onEach(it) { child -> validate(child) }
                    }
                }
            }

            test("success") {
                val node = Node(listOf(Node(), Node(), Node()))
                val result = tryValidate { validate(node) }
                result.shouldBeSuccess()
            }

            test("failure when children size exceeds 3") {
                val node = Node(listOf(Node(), Node(), Node(listOf(Node(), Node(), Node(), Node()))))
                val result = tryValidate { validate(node) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].text shouldBe
                    "Some elements do not satisfy the constraint: [Collection (size 4) must have at most 3 elements]"
            }

            test("failure when grandchildren size exceeds 3") {
                val node = Node(listOf(Node(), Node(), Node(listOf(Node(listOf(Node(), Node(), Node(), Node()))))))
                val result = tryValidate { validate(node) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].text shouldBe
                    "Some elements do not satisfy the constraint: [Some elements do not satisfy the constraint: [Collection (size 4) must have at most 3 elements]]"
            }
        }

        context("recursive - circular reference detection") {
            data class NodeWithValue(
                val value: Int,
                var next: NodeWithValue?,
            )

            fun Validation.validate(node: NodeWithValue) {
                node.schema {
                    node::value {
                        min(it, 0)
                        max(it, 100)
                    }
                    node::next { if (it != null) validate(it) }
                }
            }

            test("success when circular reference detected") {
                val node1 = NodeWithValue(10, null)
                val node2 = NodeWithValue(20, node1)
                node1.next = node2 // Create circular reference: node1 -> node2 -> node1

                val result = tryValidate { validate(node1) }
                result.shouldBeSuccess()
            }

            test("success with non-circular nested objects") {
                val node4 = NodeWithValue(40, null)
                val node3 = NodeWithValue(30, node4)
                val node2 = NodeWithValue(20, node3)
                val node1 = NodeWithValue(10, node2)

                val result = tryValidate { validate(node1) }
                result.shouldBeSuccess()
            }

            test("failure when constraint violated in nested object") {
                val node3 = NodeWithValue(150, null) // Invalid: > 100
                val node2 = NodeWithValue(20, node3)
                val node1 = NodeWithValue(10, node2)

                val result = tryValidate { validate(node1) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].path.fullName shouldBe "next.next.value"
                result.messages[0].constraintId shouldBe "kova.comparable.max"
            }

            test("failure when constraint violated in root object") {
                val node2 = NodeWithValue(20, null)
                val node1 = NodeWithValue(-5, node2) // Invalid: < 0

                val result = tryValidate { validate(node1) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].path.fullName shouldBe "value"
                result.messages[0].constraintId shouldBe "kova.comparable.min"
            }

            // To avoid StackOverflowError, use 'shouldBeEqual' instead of 'shouldBe'
            test("failure when circular reference has constraint violation") {
                val node1 = NodeWithValue(200, null) // Invalid: > 100
                val node2 = NodeWithValue(20, node1)
                node1.next = node2 // Create circular reference

                val result = tryValidate { validate(node1) }
                result.shouldBeFailure()
                result.messages.size shouldBeEqual 1
                result.messages[0].path.fullName shouldBeEqual "value"
                result.messages[0].constraintId.shouldNotBeNull() shouldBeEqual "kova.comparable.max"
            }
        }
    })
