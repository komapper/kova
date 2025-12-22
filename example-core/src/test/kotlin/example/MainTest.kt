package example

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.komapper.extension.validator.ValidationResult
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
                val messages = result.messages.map { it.text }
                messages.size shouldBe 3
                messages.any { it.contains("at least 1 characters") } shouldBe true
                messages.any { it.contains("must not be blank") } shouldBe true
                messages.any { it.contains("greater than or equal to 0") } shouldBe true
            }

            test("failure - age exceeds maximum") {
                val user = User("Bob", 150)
                val result = UserSchema.tryValidate(user)

                result.shouldBeInstanceOf<ValidationResult.Failure>()
                val messages = result.messages.map { it.text }
                messages.any { it.contains("less than or equal to 120") } shouldBe true
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
