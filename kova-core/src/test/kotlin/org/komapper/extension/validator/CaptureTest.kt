package org.komapper.extension.validator

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec

class CaptureTest :
    FunSpec({

        context("factory with single argument") {
            data class User(
                val name: String,
            )

            context(_: Validation)
            fun buildUser(name: String): User {
                val name by capture { name.ensureNotBlank() }
                return User(name)
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
                    result.messages[0].root shouldBe ""
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

            data class Age(
                val value: Int,
            )

            data class User(
                val id: Int,
                val fullName: FullName,
                val age: Age,
            )

            context(_: Validation)
            fun buildName(value: String): Name {
                val value by capture { value.ensureNotBlank() }
                return Name(value)
            }

            context(_: Validation)
            fun buildFullName(
                first: String,
                last: String,
            ): FullName {
                val first by capture { buildName(first) }
                val last by capture { buildName(last) }
                return FullName(first, last)
            }

            context(_: Validation)
            fun buildAge(value: String): Age {
                val value by capture { value.transformToInt() }
                return Age(value)
            }

            context(_: Validation)
            fun buildUser(
                id: String,
                firstName: String,
                lastName: String,
                age: String,
            ): User {
                val id by capture { id.transformToInt() }
                val fullName by capture { buildFullName(firstName, lastName) }
                val age by capture { buildAge(age) }
                return User(id, fullName, age)
            }

            test("success") {
                val result = tryValidate { buildUser("1", "abc", "def", "20") }
                result.shouldBeSuccess()
                result.value shouldBe User(1, FullName(Name("abc"), Name("def")), Age(20))
            }
            test("failure") {
                val result = tryValidate { buildUser("1", "", "", "abc") }
                result.shouldBeFailure()
                result.messages.size shouldBe 3
                result.messages[0].constraintId shouldBe "kova.charSequence.notBlank"
                result.messages[0].path.fullName shouldBe "fullName.first.value"
                result.messages[1].constraintId shouldBe "kova.charSequence.notBlank"
                result.messages[1].path.fullName shouldBe "fullName.last.value"
                result.messages[2].constraintId shouldBe "kova.string.int"
                result.messages[2].path.fullName shouldBe "age.value"
            }
            test("failure - fail fast") {
                val result = tryValidate(config = ValidationConfig(failFast = true)) { buildUser("1", "", "", "") }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.charSequence.notBlank"
                result.messages[0].path.fullName shouldBe "fullName.first.value"
            }
        }
    })
