package org.komapper.extension.validator.factory

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import org.komapper.extension.validator.Accumulate
import org.komapper.extension.validator.Validation
import org.komapper.extension.validator.ValidationConfig
import org.komapper.extension.validator.ValidationException
import org.komapper.extension.validator.andMap
import org.komapper.extension.validator.notBlank
import org.komapper.extension.validator.toInt
import org.komapper.extension.validator.tryValidate
import org.komapper.extension.validator.validate

class FactoryTest :
    FunSpec({

        context("1 argument") {
            data class User(
                val name: String,
            )

            context(_: Validation, _: Accumulate)
            fun build(name: String) =
                factory {
                    val name by bind(name) { it.notBlank() andMap { it } }
                    create { User(name()) }
                }

            context("tryCreate") {
                test("success") {
                    val result = tryValidate { build("abc") }
                    result.shouldBeSuccess()
                    result.value shouldBe User("abc")
                }

                test("failure") {
                    val result = tryValidate { build("") }
                    result.shouldBeFailure()
                    result.messages.size shouldBe 1
                    result.messages[0].constraintId shouldBe "kova.charSequence.notBlank"
                    result.messages[0].root shouldBe "factory"
                    result.messages[0].path.fullName shouldBe "name"
                }
            }

            context("create") {

                test("success") {
                    val user = validate { build("abc") }
                    user shouldBe User("abc")
                }

                test("failure") {
                    val ex = shouldThrow<ValidationException> { validate { build("") } }
                    ex.messages.single().constraintId shouldBe "kova.charSequence.notBlank"
                }
            }

            context("generateFactory and tryCreate") {
                test("success") {
                    val result = tryValidate { build("abc") }
                    result.shouldBeSuccess()
                    result.value shouldBe User("abc")
                }

                test("failure") {
                    val result = tryValidate { build("") }
                    result.shouldBeFailure()
                    result.messages.single().constraintId shouldBe "kova.charSequence.notBlank"
                }
            }

            context("generateFactory and create") {
                test("success") {
                    val user = validate { build("abc") }
                    user shouldBe User("abc")
                }

                test("failure") {
                    val ex = shouldThrow<ValidationException> { validate { build("") } }
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

            context(_: Validation, _: Accumulate)
            fun buildName(value: String) =
                factory {
                    val value by bind(value) { it.notBlank() andMap { it } }
                    create { Name(value()) }
                }

            context(_: Validation, _: Accumulate)
            fun buildFullName(
                first: String,
                last: String,
            ) = factory {
                val first by bind { buildName(first) }
                val last by bind { buildName(last) }
                create { FullName(first(), last()) }
            }

            context(_: Validation, _: Accumulate)
            fun buildUser(
                id: String,
                firstName: String,
                lastName: String,
            ) = factory {
                val id by bind(id) { it.toInt() }
                val fullName by bind { buildFullName(firstName, lastName) }
                create { User(id(), fullName()) }
            }

            test("success") {
                val result = tryValidate { buildUser("1", "abc", "def") }
                result.shouldBeSuccess()
                result.value shouldBe User(1, FullName(Name("abc"), Name("def")))
            }
            test("failure") {
                val result = tryValidate { buildUser("1", "", "") }
                result.shouldBeFailure()
                result.messages.size shouldBe 2
                result.messages[0].constraintId shouldBe "kova.charSequence.notBlank"
                result.messages[0].path.fullName shouldBe "fullName.first.value"
                result.messages[1].constraintId shouldBe "kova.charSequence.notBlank"
                result.messages[1].path.fullName shouldBe "fullName.last.value"
            }
            test("failure - fail fast") {
                val result = tryValidate(config = ValidationConfig(failFast = true)) { buildUser("1", "", "") }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.charSequence.notBlank"
                result.messages[0].path.fullName shouldBe "fullName.first.value"
            }
        }
    })
