package org.komapper.extension.validator

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import kotlin.test.assertTrue

class ObjectFactoryTest :
    FunSpec({

        context("1 arg") {
            val factory =
                Kova.factory {
                    val id = Kova.int().min(1)
                    ::User1 { args(id) }
                }

            test("success - tryCreate") {
                val result = factory.tryCreate(1)
                assertTrue(result.isSuccess())
                result.value shouldBe User1(1)
            }

            test("success - create") {
                val user = factory.create(1)
                user shouldBe User1(1)
            }

            test("failure - tryCreate") {
                val result = factory.tryCreate(-1)
                assertTrue(result.isFailure())
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
            val factory =
                Kova.factory {
                    val id = Kova.int().min(1)
                    val name = Kova.string().min(1).max(10)
                    ::User2 { args(id, name) }
                }

            test("success") {
                val result = factory.tryCreate(1, "abc")
                assertTrue(result.isSuccess())
                result.value shouldBe User2(1, "abc")
            }

            test("failure") {
                val result = factory.tryCreate(0, "")
                assertTrue(result.isFailure())
                result.details.size shouldBe 2
            }

            test("failure - failFast is true") {
                val result = factory.tryCreate(0, "", failFast = true)
                assertTrue(result.isFailure())
                result.details.size shouldBe 1
            }
        }

        context("2 args - generic validator") {
            val factory =
                Kova.factory {
                    ::User2 { args(Kova.generic(), Kova.generic()) }
                }
            test("success") {
                val result = factory.tryCreate(1, "abc")
                assertTrue(result.isSuccess())
                result.value shouldBe User2(1, "abc")
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
