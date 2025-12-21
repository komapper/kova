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

            fun createFactory(name: String) =
                Kova.factory<User> {
                    val name by bind(name) { it.notBlank() }
                    create { User(name()) }
                }

            context("tryCreate") {
                test("success") {
                    val result = createFactory("abc").tryCreate()
                    result.isSuccess().mustBeTrue()
                    result.value shouldBe User("abc")
                }

                test("failure") {
                    val result = createFactory("").tryCreate()
                    result.isFailure().mustBeTrue()
                    result.messages.size shouldBe 1
                    result.messages[0].constraintId shouldBe "kova.charSequence.notBlank"
                    result.messages[0].root shouldBe "factory"
                    result.messages[0].path.fullName shouldBe "name"
                }
            }

            context("create") {

                test("success") {
                    val user = createFactory("abc").create()
                    user shouldBe User("abc")
                }

                test("failure") {
                    val ex =
                        shouldThrow<ValidationException> {
                            createFactory("").create()
                        }
                    ex.messages.single().constraintId shouldBe "kova.charSequence.notBlank"
                }
            }

            context("generateFactory and tryCreate") {
                test("success") {
                    val result = createFactory("abc").tryCreate()
                    result.isSuccess().mustBeTrue()
                    result.value shouldBe User("abc")
                }

                test("failure") {
                    val result = createFactory("").tryCreate()
                    result.isFailure().mustBeTrue()
                    result.messages.single().constraintId shouldBe "kova.charSequence.notBlank"
                }
            }

            context("generateFactory and create") {
                test("success") {
                    val user = createFactory("abc").create()
                    user shouldBe User("abc")
                }

                test("failure") {
                    val ex =
                        shouldThrow<ValidationException> {
                            createFactory("").create()
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

            fun createNameFactory(value: String) =
                Kova.factory {
                    val value by bind(value) { it.notBlank() }
                    create { Name(value()) }
                }

            fun createFullNameFactory(
                first: String,
                last: String,
            ) = Kova.factory {
                val first by bind(createNameFactory(first))
                val last by bind(createNameFactory(last))
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
                        val id by bind(id) { it.toInt() }
                        val fullName by bind(createFullNameFactory(firstName, lastName))
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
