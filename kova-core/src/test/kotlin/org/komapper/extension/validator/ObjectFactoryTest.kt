package org.komapper.extension.validator

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith

class ObjectFactoryTest :
    FunSpec({

        context("1 arg") {
            val factory = Kova.args(Kova.int().min(1)).createFactory(::User1)

            test("success - tryCreate") {
                val result = factory.tryCreate(1)
                result.isSuccess().mustBeTrue()
                result.value shouldBe User1(1)
            }

            test("success - create") {
                val user = factory.create(1)
                user shouldBe User1(1)
            }

            test("failure - tryCreate") {
                val result = factory.tryCreate(-1)
                result.isFailure().mustBeTrue()
                result.details.size shouldBe 1
                val detail = result.details.first()
                detail.root shouldEndWith ".User1"
                detail.path shouldBe "arg1"
                detail.message.content shouldBe "Number -1 must be greater than or equal to 1"
            }

            test("failure - create") {
                val ex =
                    shouldThrow<ValidationException> {
                        factory.create(-1)
                    }
                ex.details.size shouldBe 1
                val detail = ex.details.first()
                detail.root shouldEndWith ".User1"
                detail.path shouldBe "arg1"
                detail.message.content shouldBe "Number -1 must be greater than or equal to 1"
            }
        }

        context("2 args") {

            val userFactory =
                Kova
                    .args(
                        Kova.int().min(1),
                        Kova.string().min(1).max(10),
                    ).createFactory(::User2)

            test("success") {
                val result = userFactory.tryCreate(1, "abc")
                result.isSuccess().mustBeTrue()
                result.value shouldBe User2(1, "abc")
            }

            test("failure") {
                val result = userFactory.tryCreate(0, "")
                result.isFailure().mustBeTrue()
                result.details.size shouldBe 2
            }

            test("failure - failFast is true") {
                val result = userFactory.tryCreate(0, "", failFast = true)
                result.isFailure().mustBeTrue()
                result.details.size shouldBe 1
            }
        }

        context("2 args - generic validator") {
            val factory = Kova.args(Kova.generic<Int>(), Kova.generic<String>()).createFactory(::User2)

            test("success") {
                val result = factory.tryCreate(1, "abc")
                result.isSuccess().mustBeTrue()
                result.value shouldBe User2(1, "abc")
            }
        }

        context("3 args") {
            val factory =
                Kova
                    .args(
                        Kova.int().min(1),
                        Kova.string().min(1).max(10),
                        Kova.int().min(0).max(120),
                    ).createFactory(::User3)

            test("success") {
                val result = factory.tryCreate(1, "abc", 25)
                result.isSuccess().mustBeTrue()
                result.value shouldBe User3(1, "abc", 25)
            }

            test("failure") {
                val result = factory.tryCreate(0, "", -1)
                result.isFailure().mustBeTrue()
                result.details.size shouldBe 3
            }
        }

        context("4 args") {
            val factory =
                Kova
                    .args(
                        Kova.int().min(1),
                        Kova.string().min(1).max(10),
                        Kova.int().min(0).max(120),
                        Kova.string().email(),
                    ).createFactory(::User4)

            test("success") {
                val result = factory.tryCreate(1, "abc", 25, "test@example.com")
                result.isSuccess().mustBeTrue()
                result.value shouldBe User4(1, "abc", 25, "test@example.com")
            }

            test("failure") {
                val result = factory.tryCreate(0, "", -1, "invalid-email")
                result.isFailure().mustBeTrue()
                result.details.size shouldBe 4
            }
        }

        context("5 args") {
            val factory =
                Kova
                    .args(
                        Kova.int().min(1),
                        Kova.string().min(1).max(10),
                        Kova.int().min(0).max(120),
                        Kova.string().email(),
                        Kova.string().min(10).max(15),
                    ).createFactory(::User5)

            test("success") {
                val result = factory.tryCreate(1, "abc", 25, "test@example.com", "1234567890")
                result.isSuccess().mustBeTrue()
                result.value shouldBe User5(1, "abc", 25, "test@example.com", "1234567890")
            }

            test("failure") {
                val result = factory.tryCreate(0, "", -1, "invalid-email", "123")
                result.isFailure().mustBeTrue()
                result.details.size shouldBe 5
            }
        }

        context("6 args") {
            val factory =
                Kova
                    .args(
                        Kova.int().min(1),
                        Kova.string().min(1).max(10),
                        Kova.int().min(0).max(120),
                        Kova.string().email(),
                        Kova.string().min(10).max(15),
                        Kova.string().min(1),
                    ).createFactory(::User6)

            test("success") {
                val result = factory.tryCreate(1, "abc", 25, "test@example.com", "1234567890", "123 Main St")
                result.isSuccess().mustBeTrue()
                result.value shouldBe User6(1, "abc", 25, "test@example.com", "1234567890", "123 Main St")
            }

            test("failure") {
                val result = factory.tryCreate(0, "", -1, "invalid-email", "123", "")
                result.isFailure().mustBeTrue()
                result.details.size shouldBe 6
            }
        }

        context("7 args") {
            val factory =
                Kova
                    .args(
                        Kova.int().min(1),
                        Kova.string().min(1).max(10),
                        Kova.int().min(0).max(120),
                        Kova.string().email(),
                        Kova.string().min(10).max(15),
                        Kova.string().min(1),
                        Kova.string().min(1),
                    ).createFactory(::User7)

            test("success") {
                val result = factory.tryCreate(1, "abc", 25, "test@example.com", "1234567890", "123 Main St", "Springfield")
                result.isSuccess().mustBeTrue()
                result.value shouldBe User7(1, "abc", 25, "test@example.com", "1234567890", "123 Main St", "Springfield")
            }

            test("failure") {
                val result = factory.tryCreate(0, "", -1, "invalid-email", "123", "", "")
                result.isFailure().mustBeTrue()
                result.details.size shouldBe 7
            }
        }

        context("8 args") {
            val factory =
                Kova
                    .args(
                        Kova.int().min(1),
                        Kova.string().min(1).max(10),
                        Kova.int().min(0).max(120),
                        Kova.string().email(),
                        Kova.string().min(10).max(15),
                        Kova.string().min(1),
                        Kova.string().min(1),
                        Kova.string().min(2).max(2),
                    ).createFactory(::User8)

            test("success") {
                val result = factory.tryCreate(1, "abc", 25, "test@example.com", "1234567890", "123 Main St", "Springfield", "IL")
                result.isSuccess().mustBeTrue()
                result.value shouldBe User8(1, "abc", 25, "test@example.com", "1234567890", "123 Main St", "Springfield", "IL")
            }

            test("failure") {
                val result = factory.tryCreate(0, "", -1, "invalid-email", "123", "", "", "INVALID")
                result.isFailure().mustBeTrue()
                result.details.size shouldBe 8
            }
        }

        context("9 args") {
            val factory =
                Kova
                    .args(
                        Kova.int().min(1),
                        Kova.string().min(1).max(10),
                        Kova.int().min(0).max(120),
                        Kova.string().email(),
                        Kova.string().min(10).max(15),
                        Kova.string().min(1),
                        Kova.string().min(1),
                        Kova.string().min(2).max(2),
                        Kova.string().min(5).max(10),
                    ).createFactory(::User9)

            test("success") {
                val result = factory.tryCreate(1, "abc", 25, "test@example.com", "1234567890", "123 Main St", "Springfield", "IL", "62701")
                result.isSuccess().mustBeTrue()
                result.value shouldBe User9(1, "abc", 25, "test@example.com", "1234567890", "123 Main St", "Springfield", "IL", "62701")
            }

            test("failure") {
                val result = factory.tryCreate(0, "", -1, "invalid-email", "123", "", "", "INVALID", "1")
                result.isFailure().mustBeTrue()
                result.details.size shouldBe 9
            }
        }

        context("10 args") {
            val factory =
                Kova
                    .args(
                        Kova.int().min(1),
                        Kova.string().min(1).max(10),
                        Kova.int().min(0).max(120),
                        Kova.string().email(),
                        Kova.string().min(10).max(15),
                        Kova.string().min(1),
                        Kova.string().min(1),
                        Kova.string().min(2).max(2),
                        Kova.string().min(5).max(10),
                        Kova.string().min(1),
                    ).createFactory(::User10)

            test("success") {
                val result =
                    factory.tryCreate(
                        1,
                        "abc",
                        25,
                        "test@example.com",
                        "1234567890",
                        "123 Main St",
                        "Springfield",
                        "IL",
                        "62701",
                        "USA",
                    )
                result.isSuccess().mustBeTrue()
                result.value shouldBe
                    User10(1, "abc", 25, "test@example.com", "1234567890", "123 Main St", "Springfield", "IL", "62701", "USA")
            }

            test("failure") {
                val result = factory.tryCreate(0, "", -1, "invalid-email", "123", "", "", "INVALID", "1", "")
                result.isFailure().mustBeTrue()
                result.details.size shouldBe 10
            }
        }
    }) {
    data class User1(
        val id: Int,
    )

    data class User2(
        val id: Int,
        val name: String,
    )

    data class User3(
        val id: Int,
        val name: String,
        val age: Int,
    )

    data class User4(
        val id: Int,
        val name: String,
        val age: Int,
        val email: String,
    )

    data class User5(
        val id: Int,
        val name: String,
        val age: Int,
        val email: String,
        val phone: String,
    )

    data class User6(
        val id: Int,
        val name: String,
        val age: Int,
        val email: String,
        val phone: String,
        val address: String,
    )

    data class User7(
        val id: Int,
        val name: String,
        val age: Int,
        val email: String,
        val phone: String,
        val address: String,
        val city: String,
    )

    data class User8(
        val id: Int,
        val name: String,
        val age: Int,
        val email: String,
        val phone: String,
        val address: String,
        val city: String,
        val state: String,
    )

    data class User9(
        val id: Int,
        val name: String,
        val age: Int,
        val email: String,
        val phone: String,
        val address: String,
        val city: String,
        val state: String,
        val zip: String,
    )

    data class User10(
        val id: Int,
        val name: String,
        val age: Int,
        val email: String,
        val phone: String,
        val address: String,
        val city: String,
        val state: String,
        val zip: String,
        val country: String,
    )
}
