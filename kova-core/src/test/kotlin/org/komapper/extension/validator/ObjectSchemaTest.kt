package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldNotBeNull
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

        context("and") {
            context(_: Validation, _: Accumulate)
            fun User.validate() =
                checking {
                    ::name {
                        it.min(1)
                        it.max(10)
                    }
                    ::id { it.min(1) }
                }

            test("success") {
                val user = User(1, "abc")
                val result = tryValidate { user.validate() }
                result.shouldBeSuccess()
            }

            test("failure with 1 rule violated") {
                val user = User(2, "too-long-name")
                val result = tryValidate { user.validate() }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].let {
                    it.root shouldBe "User"
                    it.path.fullName shouldBe "name"
                    it.constraintId shouldBe "kova.charSequence.max"
                }
            }

            test("failure with 2 rules violated") {
                val user = User(0, "too-long-name")
                val result = tryValidate { user.validate() }
                result.shouldBeFailure()

                result.messages.size shouldBe 2
                result.messages[0].let {
                    it.root shouldBe "User"
                    it.path.fullName shouldBe "name"
                    it.constraintId shouldBe "kova.charSequence.max"
                }
                result.messages[1].let {
                    it.root shouldBe "User"
                    it.path.fullName shouldBe "id"
                    it.constraintId shouldBe "kova.comparable.min"
                }
            }
        }

        context("or") {
            context(_: Validation, _: Accumulate)
            fun User.validate() =
                checking {
                    or {
                        ::name {
                            it.min(1)
                            it.max(10)
                        }
                    } orElse { ::id { it.min(1) } }
                }

            test("success when both schemas are satisfied") {
                val user = User(1, "abc")
                val result = tryValidate { user.validate() }
                result.shouldBeSuccess()
            }

            test("success when first schema is satisfied and second fails") {
                val user = User(0, "abc")
                val result = tryValidate { user.validate() }
                result.shouldBeSuccess()
            }

            test("success when first schema fails and second is satisfied") {
                val user = User(1, "too-long-name")
                val result = tryValidate { user.validate() }
                result.shouldBeSuccess()
            }

            test("failure when both schemas fail") {
                val user = User(0, "too-long-name")
                val result = tryValidate { user.validate() }
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

            context(_: Validation, _: Accumulate)
            fun Period.validate() =
                checking {
                    constrain("test") {
                        satisfies(it.startDate <= it.endDate) { text("startDate must be less than or equal to endDate") }
                    }
                }

            test("success") {
                val period = Period(LocalDate.of(2020, 1, 1), LocalDate.of(2021, 1, 1))
                val result = tryValidate { period.validate() }
                result.shouldBeSuccess()
            }

            test("failure") {
                val period = Period(LocalDate.of(2020, 1, 1), LocalDate.of(2019, 1, 1))
                val result = tryValidate { period.validate() }
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
            context(_: Validation, _: Accumulate)
            fun User?.validate() =
                this?.checking {
                    ::id { it.min(1) }
                    ::name {
                        it.min(1)
                        it.max(10)
                    }
                }

            test("success with non-null value") {
                val user = User(1, "abc")
                val result = tryValidate { user.validate() }
                result.shouldBeSuccess()
            }

            test("success with null value") {
                val result = tryValidate { null.validate() }
                result.shouldBeSuccess()
            }
        }

        context("prop - simple") {
            context(_: Validation, _: Accumulate)
            fun User.validate() =
                checking {
                    ::id { it.min(1) }
                    ::name {
                        it.min(1)
                        it.max(10)
                    }
                }

            test("success") {
                val user = User(1, "abc")
                val result = tryValidate { user.validate() }
                result.shouldBeSuccess()
            }

            test("failure with 1 constraint violated") {
                val user = User(2, "too-long-name")
                val result = tryValidate { user.validate() }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].let {
                    it.root shouldBe "User"
                    it.path.fullName shouldBe "name"
                    it.constraintId shouldBe "kova.charSequence.max"
                }
            }

            test("failure with 2 constraints violated") {
                val user = User(0, "too-long-name")
                val result = tryValidate { user.validate() }
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
                    it.constraintId shouldBe "kova.charSequence.max"
                }
            }
        }

        context("prop - nest") {
            context(_: Validation, _: Accumulate)
            fun Street.validate() =
                checking {
                    ::id { it.min(1) }
                    ::name {
                        it.min(3)
                        it.max(5)
                    }
                }

            context(_: Validation, _: Accumulate)
            fun Address.validate() = checking { ::street { it.validate() } }

            context(_: Validation, _: Accumulate)
            fun Employee.validate() = checking { ::address { it.validate() } }

            test("success") {
                val employee = Employee(1, "abc", Address(1, Street(1, "def")))
                val result = tryValidate { employee.validate() }
                result.shouldBeSuccess()
            }

            test("failure") {
                val employee = Employee(1, "abc", Address(1, Street(1, "too-long-name")))
                val result = tryValidate { employee.validate() }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].let {
                    it.root shouldBe "Employee"
                    it.path.fullName shouldBe "address.street.name"
                    it.constraintId shouldBe "kova.charSequence.max"
                }
            }
        }

        context("prop - nest - dynamic") {
            context(_: Validation, _: Accumulate)
            fun Street.validate() =
                checking {
                    ::id { it.min(1) }
                    ::name {
                        it.min(3)
                        it.max(5)
                    }
                }

            context(_: Validation, _: Accumulate)
            fun Address.validate() =
                checking {
                    ::street { it.validate() }
                    ::postalCode {
                        when (country) {
                            "US" -> it.length(8)
                            else -> it.length(5)
                        }
                    }
                }

            context(_: Validation, _: Accumulate)
            fun Employee.validate() = checking { ::address { it.validate() } }

            test("success when country is US") {
                val employee = Employee(1, "abc", Address(1, Street(1, "def"), country = "US", postalCode = "12345678"))
                val result = tryValidate { employee.validate() }
                result.shouldBeSuccess()
            }

            test("success when country is not US") {
                val employee = Employee(1, "abc", Address(1, Street(1, "def"), country = "JP", postalCode = "12345"))
                val result = tryValidate { employee.validate() }
                result.shouldBeSuccess()
            }

            test("failure when country is US") {
                val employee =
                    Employee(1, "abc", Address(1, Street(1, "def"), country = "US", postalCode = "123456789"))
                val result = tryValidate { employee.validate() }
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
                val result = tryValidate { employee.validate() }
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
            context(_: Validation, _: Accumulate)
            fun Street.validate() =
                checking {
                    ::id { it.min(1) }
                    ::name {
                        it.min(3)
                        it.max(5)
                    }
                }

            context(_: Validation, _: Accumulate)
            fun Address.validate() = checking { ::street { it.validate() } }

            context(_: Validation, _: Accumulate)
            fun Person.validate() =
                checking {
                    ::firstName { }
                    ::lastName { }
                    ::address { it?.validate() }
                }

            context(_: Validation, _: Accumulate)
            fun Person.validate2() =
                checking {
                    ::firstName { it.notNull() }
                    ::lastName { it.notNull() }
                    ::address { it?.validate() }
                }

            test("success") {
                val person = Person(1, "abc", "def", Address(1, Street(1, "hij")))
                val result = tryValidate { person.validate() }
                result.shouldBeSuccess()
            }

            test("success with nullable values") {
                val person = Person(1, null, null, null)
                val result = tryValidate { person.validate() }
                result.shouldBeSuccess()
            }

            test("failure when not null constraint violated") {
                val person = Person(1, null, null, null)
                val result = tryValidate { person.validate2() }
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

            context(_: Validation, _: Accumulate)
            fun Node.validate() {
                checking {
                    ::children {
                        it.max(3)
                        it.onEach<Node> { child -> child.validate() }
                    }
                }
            }

            test("success") {
                val node = Node(listOf(Node(), Node(), Node()))
                val result = tryValidate { node.validate() }
                result.shouldBeSuccess()
            }

            test("failure when children size exceeds 3") {
                val node = Node(listOf(Node(), Node(), Node(listOf(Node(), Node(), Node(), Node()))))
                val result = tryValidate { node.validate() }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].text shouldBe
                    "Some elements do not satisfy the constraint: [Collection (size 4) must have at most 3 elements]"
            }

            test("failure when grandchildren size exceeds 3") {
                val node = Node(listOf(Node(), Node(), Node(listOf(Node(listOf(Node(), Node(), Node(), Node()))))))
                val result = tryValidate { node.validate() }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].text shouldBe
                    "Some elements do not satisfy the constraint: [Some elements do not satisfy the constraint: [Collection (size 4) must have at most 3 elements]]"
            }
        }

        context("circular reference detection") {
            data class NodeWithValue(
                val value: Int,
                var next: NodeWithValue?,
            )

            context(_: Validation, _: Accumulate)
            fun NodeWithValue.validate() {
                checking {
                    ::value {
                        it.min(0)
                        it.max(100)
                    }
                    ::next { it?.validate() }
                }
            }

            test("success when circular reference detected") {
                val node1 = NodeWithValue(10, null)
                val node2 = NodeWithValue(20, node1)
                node1.next = node2 // Create circular reference: node1 -> node2 -> node1

                val result = tryValidate { node1.validate() }
                result.shouldBeSuccess()
            }

            test("success with non-circular nested objects") {
                val node4 = NodeWithValue(40, null)
                val node3 = NodeWithValue(30, node4)
                val node2 = NodeWithValue(20, node3)
                val node1 = NodeWithValue(10, node2)

                val result = tryValidate { node1.validate() }
                result.shouldBeSuccess()
            }

            test("failure when constraint violated in nested object") {
                val node3 = NodeWithValue(150, null) // Invalid: > 100
                val node2 = NodeWithValue(20, node3)
                val node1 = NodeWithValue(10, node2)

                val result = tryValidate { node1.validate() }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].path.fullName shouldBe "next.next.value"
                result.messages[0].constraintId shouldBe "kova.comparable.max"
            }

            test("failure when constraint violated in root object") {
                val node2 = NodeWithValue(20, null)
                val node1 = NodeWithValue(-5, node2) // Invalid: < 0

                val result = tryValidate { node1.validate() }
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

                val result = tryValidate { node1.validate() }
                result.shouldBeFailure()
                result.messages.size shouldBeEqual 1
                result.messages[0].path.fullName shouldBeEqual "value"
                result.messages[0].constraintId.shouldNotBeNull() shouldBeEqual "kova.comparable.max"
            }
        }

        context("temporal property") {
            data class User(
                val name: String,
                val birthday: LocalDate,
            )

            context(_: Validation, _: Accumulate)
            fun User.validate() {
                checking {
                    ::name { it.notBlank() }
                    ::birthday { }
                }
            }

            test("success") {
                val user = User("abc", LocalDate.of(2021, 1, 1))
                val result = tryValidate { user.validate() }
                result.shouldBeSuccess()
            }
        }
    })
