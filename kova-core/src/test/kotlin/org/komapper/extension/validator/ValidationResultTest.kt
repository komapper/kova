package org.komapper.extension.validator

import io.kotest.assertions.fail
import io.kotest.core.spec.style.FunSpec
import java.util.Locale

class ValidationResultTest :
    FunSpec({

        beforeSpec {
            Locale.setDefault(Locale.US)
        }

        context("success") {
            val result: ValidationResult<String> = ValidationResult.Success("hello")

            test("isSuccess") {
                if (result.isSuccess()) {
                    result.value shouldBe "hello"
                } else {
                    fail("result is not success")
                }
            }

            test("isFailure") {
                if (result.isFailure()) {
                    fail("result is not failure")
                } else {
                    result.value shouldBe "hello"
                }
            }
        }

        context("failure") {
            val result: ValidationResult<String> =
                ValidationResult.Failure(
                    listOf(
                        Message.Text(
                            "test",
                            "",
                            Path("", "", null),
                            "failure",
                            null,
                        ),
                    ),
                )

            test("isSuccess") {
                if (result.isSuccess()) {
                    fail("result is not success")
                } else {
                    result.messages.size shouldBe 1
                    result.messages[0].text shouldBe "failure"
                }
            }

            test("isFailure") {
                if (result.isFailure()) {
                    result.messages.size shouldBe 1
                    result.messages[0].text shouldBe "failure"
                } else {
                    fail("result is not failure")
                }
            }
        }
    })
