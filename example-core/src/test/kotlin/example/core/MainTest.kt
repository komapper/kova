package example.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeInstanceOf
import org.komapper.extension.validator.Message
import org.komapper.extension.validator.ValidationResult
import org.komapper.extension.validator.ValidationResult.Failure
import org.komapper.extension.validator.ValidationResult.Success
import org.komapper.extension.validator.tryValidate
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import io.kotest.matchers.shouldBe as kotestShouldBe

@IgnorableReturnValue
@OptIn(ExperimentalContracts::class)
fun <T> ValidationResult<T>.shouldBeSuccess(): T {
    contract { returns() implies (this@shouldBeSuccess is Success<T>) }
    return shouldBeInstanceOf<Success<T>>().value
}

@IgnorableReturnValue
@OptIn(ExperimentalContracts::class)
fun ValidationResult<*>.shouldBeFailure(): List<Message> {
    contract { returns() implies (this@shouldBeFailure is Failure) }
    return shouldBeInstanceOf<Failure>().messages
}

/** Kotest's shouldBe, but annotated with @OnlyInputTypes. */
@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@IgnorableReturnValue
infix fun <@kotlin.internal.OnlyInputTypes T> T.shouldBe(expected: T?): T = kotestShouldBe(expected)

class MainTest :
    FunSpec({

        context("UserSchema validation") {
            test("success - valid user") {
                val user = User("Alice", 30)
                val result = tryValidate { validate(user) }

                result.shouldBeSuccess()
            }

            test("failure - invalid user with empty name and negative age") {
                val user = User("", -1)
                val result = tryValidate { validate(user) }

                result.shouldBeFailure()
                val ids = result.messages.map { it.constraintId }
                ids.size shouldBe 3
                ids[0] shouldBe "kova.charSequence.minLength"
                ids[1] shouldBe "kova.charSequence.notBlank"
                ids[2] shouldBe "kova.comparable.min"
            }

            test("failure - age exceeds maximum") {
                val user = User("Bob", 150)
                val result = tryValidate { validate(user) }

                result.shouldBeFailure()
                val ids = result.messages.map { it.constraintId }
                ids.size shouldBe 1
                ids[0] shouldBe "kova.comparable.max"
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
