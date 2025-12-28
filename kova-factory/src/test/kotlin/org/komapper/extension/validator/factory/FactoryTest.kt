package org.komapper.extension.validator.factory

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import org.komapper.extension.validator.Validation
import org.komapper.extension.validator.ValidationConfig
import org.komapper.extension.validator.ValidationException
import org.komapper.extension.validator.notBlank
import org.komapper.extension.validator.toInt
import org.komapper.extension.validator.tryValidate
import org.komapper.extension.validator.validate

class FactoryTest :
    FunSpec({

        context("factory with single argument") {
            data class User(
                val name: String,
            )

            fun Validation.buildUser(name: String) =
                factory {
                    val name by bind(name) {
                        notBlank(it)
                        it
                    }
                    User(name)
                }

            context("using tryValidate") {
                test("success") {
                    val result = tryValidate { buildUser("abc") }
                    result.shouldBeSuccess()
                    result.value shouldBe User("abc")
                }

                test("failure") {
                    val result = tryValidate { buildUser("") }
                    result.shouldBeFailure()
                    result.messages.size shouldBe 1
                    result.messages[0].constraintId shouldBe "kova.charSequence.notBlank"
                    result.messages[0].root shouldBe "factory"
                    result.messages[0].path.fullName shouldBe "name"
                }
            }

            context("using validate") {

                test("success") {
                    val user = validate { buildUser("abc") }
                    user shouldBe User("abc")
                }

                test("failure") {
                    val ex = shouldThrow<ValidationException> { validate { buildUser("") } }
                    ex.messages.single().constraintId shouldBe "kova.charSequence.notBlank"
                }
            }
        }

        context("factory with nested objects") {
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

            fun Validation.buildName(value: String) =
                factory {
                    val value by bind(value) {
                        notBlank(it)
                        it
                    }
                    Name(value)
                }

            fun Validation.buildFullName(
                first: String,
                last: String,
            ) = factory {
                val first by bind { buildName(first) }
                val last by bind { buildName(last) }
                FullName(first, last)
            }

            fun Validation.buildUser(
                id: String,
                firstName: String,
                lastName: String,
            ) = factory {
                val id by bind(id) { toInt(it) }
                val fullName by bind { buildFullName(firstName, lastName) }
                User(id, fullName)
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
