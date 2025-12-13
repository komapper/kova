package org.komapper.extension.validator.factory

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.komapper.extension.validator.Kova
import org.komapper.extension.validator.ValidationConfig
import org.komapper.extension.validator.ValidationException
import org.komapper.extension.validator.ValidationResult
import org.komapper.extension.validator.isFailure
import org.komapper.extension.validator.isSuccess
import org.komapper.extension.validator.notBlank
import org.komapper.extension.validator.toInt

class FactoryTest :
    FunSpec({

        context("1 argument") {
            data class User(
                val name: String,
            )

            fun FactoryScope<User>.createUser(name: String): ValidationResult<User> {
                val name = check("name", name) { it.notBlank() }
                return create { User(name()) }
            }

            context("tryCreate") {
                test("success") {
                    val result = Kova.factory { createUser("abc") }.tryCreate()
                    result.isSuccess().mustBeTrue()
                    result.value shouldBe User("abc")
                }

                test("failure") {
                    val result = Kova.factory { createUser("") }.tryCreate()
                    result.isFailure().mustBeTrue()
                    result.messages.size shouldBe 1
                    result.messages[0].constraintId shouldBe "kova.charSequence.notBlank"
                    result.messages[0].root shouldBe "factory"
                    result.messages[0].path.fullName shouldBe "name"
                }
            }

            context("create") {

                test("success") {
                    val user = Kova.factory { createUser("abc") }.create()
                    user shouldBe User("abc")
                }

                test("failure") {
                    val ex =
                        shouldThrow<ValidationException> {
                            Kova
                                .factory {
                                    createUser("")
                                }.create()
                        }
                    ex.messages.single().constraintId shouldBe "kova.charSequence.notBlank"
                }
            }

            context("generateFactory and tryCreate") {
                test("success") {
                    val result = Kova.factory { createUser("abc") }.tryCreate()
                    result.isSuccess().mustBeTrue()
                    result.value shouldBe User("abc")
                }

                test("failure") {
                    val result = Kova.factory { createUser("") }.tryCreate()
                    result.isFailure().mustBeTrue()
                    result.messages.single().constraintId shouldBe "kova.charSequence.notBlank"
                }
            }

            context("generateFactory and create") {
                test("success") {
                    val user = Kova.factory { createUser("abc") }.create()
                    user shouldBe User("abc")
                }

                test("failure") {
                    val ex =
                        shouldThrow<ValidationException> {
                            Kova.factory { createUser("") }.create()
                        }
                    ex.messages.single().constraintId shouldBe "kova.charSequence.notBlank"
                }
            }
        }

        context("2 arguments - nest") {
            data class Name(
                val value: String,
            )

            data class FullName(
                val first: Name,
                val last: Name,
            )

            data class User(
                val id: Int,
                val fullName: FullName,
            )

            fun nameFactory(value: String) =
                Kova.factory {
                    val value = check("value", value) { it.notBlank() }
                    create { Name(value()) }
                }

            fun fullNameFactory(
                first: String,
                last: String,
            ) = Kova.factory {
                val first = check("first", nameFactory(first))
                val last = check("last", nameFactory(last))
                create { FullName(first(), last()) }
            }

            fun tryCreateUser(
                id: String,
                firstName: String,
                lastName: String,
                config: ValidationConfig,
            ): ValidationResult<User> =
                Kova
                    .factory {
                        val id = check("id", id) { it.toInt() }
                        val fullName = check("fullName", fullNameFactory(firstName, lastName))
                        create { User(id(), fullName()) }
                    }.tryCreate(config)

            test("success") {
                val result = tryCreateUser("1", "abc", "def", ValidationConfig())
                result.isSuccess().mustBeTrue()
                result.value shouldBe User(1, FullName(Name("abc"), Name("def")))
            }
            test("failure") {
                val result = tryCreateUser("1", "", "", ValidationConfig())
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 2
                result.messages[0].constraintId shouldBe "kova.charSequence.notBlank"
                result.messages[0].path.fullName shouldBe "fullName.first.value"
                result.messages[1].constraintId shouldBe "kova.charSequence.notBlank"
                result.messages[1].path.fullName shouldBe "fullName.last.value"
            }
            test("failure - fail fast") {
                val result = tryCreateUser("1", "", "", config = ValidationConfig(failFast = true))
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.charSequence.notBlank"
                result.messages[0].path.fullName shouldBe "fullName.first.value"
            }
        }
    })
