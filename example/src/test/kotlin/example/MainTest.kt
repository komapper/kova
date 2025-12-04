package example

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.komapper.extension.validator.ValidationResult
import org.komapper.extension.validator.messages
import org.komapper.extension.validator.tryCreate
import org.komapper.extension.validator.tryValidate

class MainTest :
    FunSpec({

        context("UserSchema validation") {
            test("success - valid user") {
                val user = User("Alice", 30)
                val result = UserSchema.tryValidate(user)

                result.shouldBeInstanceOf<ValidationResult.Success<User>>()
                result.value shouldBe user
            }

            test("failure - invalid user with empty name and negative age") {
                val user = User("", -1)
                val result = UserSchema.tryValidate(user)

                result.shouldBeInstanceOf<ValidationResult.Failure>()
                val messages = result.messages.map { it.content }
                messages.size shouldBe 3
                messages.any { it.contains("at least 1 characters") } shouldBe true
                messages.any { it.contains("must not be blank") } shouldBe true
                messages.any { it.contains("greater than or equal to 0") } shouldBe true
            }

            test("failure - age exceeds maximum") {
                val user = User("Bob", 150)
                val result = UserSchema.tryValidate(user)

                result.shouldBeInstanceOf<ValidationResult.Failure>()
                val messages = result.messages.map { it.content }
                messages.any { it.contains("less than or equal to 120") } shouldBe true
            }
        }

        context("UserSchema creation with ObjectFactory") {
            test("success - valid inputs") {
                val result = UserSchema.bind("Alice", 30).tryCreate()

                result.shouldBeInstanceOf<ValidationResult.Success<User>>()
                result.value shouldBe User("Alice", 30)
            }

            test("failure - invalid inputs") {
                val result = UserSchema.bind("", -1).tryCreate()

                result.shouldBeInstanceOf<ValidationResult.Failure>()
                val messages = result.messages.map { it.content }
                messages.size shouldBe 3
                messages.any { it.contains("at least 1 characters") } shouldBe true
                messages.any { it.contains("must not be blank") } shouldBe true
                messages.any { it.contains("greater than or equal to 0") } shouldBe true
            }
        }

        context("AgeSchema validation and creation") {
            test("success - valid age string") {
                val result = AgeSchema.bind("30").tryCreate()

                result.shouldBeInstanceOf<ValidationResult.Success<Age>>()
                result.value shouldBe Age(30)
            }

            test("failure - non-numeric string") {
                val result = AgeSchema.bind("not a number").tryCreate()

                result.shouldBeInstanceOf<ValidationResult.Failure>()
                val messages = result.messages.map { it.content }
                messages.any { it.contains("must be an int") } shouldBe true
            }

            test("failure - age out of range") {
                val result = AgeSchema.bind("150").tryCreate()

                result.shouldBeInstanceOf<ValidationResult.Failure>()
                val messages = result.messages.map { it.content }
                messages.any { it.contains("less than or equal to 120") } shouldBe true
            }
        }

        context("PersonSchema nested validation") {
            test("success - valid person") {
                val result = PersonSchema.bind("Alice", "30").tryCreate()

                result.shouldBeInstanceOf<ValidationResult.Success<Person>>()
                result.value shouldBe Person("Alice", Age(30))
            }

            test("failure - invalid name and non-numeric age") {
                val result = PersonSchema.bind("", "not number").tryCreate()

                result.shouldBeInstanceOf<ValidationResult.Failure>()
                val messages = result.messages.map { it.content }
                messages.size shouldBe 3
                messages.any { it.contains("at least 1 characters") } shouldBe true
                messages.any { it.contains("must not be blank") } shouldBe true
                messages.any { it.contains("must be an int") } shouldBe true
            }

            test("failure - blank name") {
                val result = PersonSchema.bind("   ", "25").tryCreate()

                result.shouldBeInstanceOf<ValidationResult.Failure>()
                val messages = result.messages.map { it.content }
                messages.any { it.contains("must not be blank") } shouldBe true
            }
        }

        context("main function execution") {
            test("main runs without errors") {
                // This test verifies that the main function executes successfully
                // The main function uses error() for unexpected branches,
                // so if it completes without throwing, all validations work as expected
                main()
            }
        }
    })
