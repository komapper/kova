package org.komapper.extension.validator

import io.kotest.assertions.fail
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import java.util.Locale

class AccumulateTest :
    FunSpec({
        beforeSpec {
            Locale.setDefault(Locale.US)
        }

        context("recoverValidation") {
            test("returns block result when no exception is thrown") {
                val result =
                    recoverValidation(
                        recover = { "recovered" },
                        block = { "success" },
                    )
                result shouldBe "success"
            }

            test("calls recover when ValidationCancellationException with matching error is thrown") {
                val result =
                    recoverValidation(
                        recover = { "recovered" },
                        block = {
                            raise() // Throws ValidationCancellationException with this error context
                        },
                    )
                result shouldBe "recovered"
            }

            test("re-throws ValidationCancellationException when error context doesn't match") {
                val outerError = Accumulate.Error()
                val exception =
                    shouldThrow<ValidationCancellationException> {
                        recoverValidation(
                            recover = { "recovered" },
                            block = {
                                // Throw exception with different error context
                                throw ValidationCancellationException(outerError)
                            },
                        )
                    }
                // Verify it's the same error context that was thrown
                exception.error shouldBeSameInstanceAs outerError
            }

            test("propagates non-ValidationCancellationException") {
                val exception =
                    shouldThrow<IllegalStateException> {
                        recoverValidation(
                            recover = { "recovered" },
                            block = {
                                throw IllegalStateException("test error")
                            },
                        )
                    }
                exception.message shouldBe "test error"
            }

            test("nested recoverValidation calls handle errors independently") {
                val result =
                    recoverValidation(
                        recover = { "outer recovered" },
                        block = {
                            val innerResult =
                                recoverValidation(
                                    recover = { "inner recovered" },
                                    block = {
                                        raise() // Inner error context
                                    },
                                )
                            innerResult shouldBe "inner recovered"
                            "outer success"
                        },
                    )
                result shouldBe "outer success"
            }

            test("inner exception propagates to outer when error contexts don't match") {
                // Create a separate error that doesn't match any context
                val separateError = Accumulate.Error()

                val exception =
                    shouldThrow<ValidationCancellationException> {
                        recoverValidation(
                            recover = { "outer recovered" },
                            block = {
                                recoverValidation(
                                    recover = { "inner recovered" },
                                    block = {
                                        // Throw with the separate error, not the inner context
                                        throw ValidationCancellationException(separateError)
                                    },
                                )
                            },
                        )
                    }
                // The exception should propagate all the way out
                exception.error shouldBeSameInstanceAs separateError
            }
        }

        context("accumulating") {

            test("returns Ok when validation succeeds") {
                val messages = mutableListOf<Message>()
                val error = Accumulate.Error()
                val validation =
                    Validation(acc = {
                        messages.addAll(it)
                        error
                    })
                with(validation) {
                    val accumulated =
                        accumulating {
                            "success"
                        }
                    accumulated.shouldBeInstanceOf<Accumulate.Ok<String>>()
                    accumulated.value shouldBe "success"
                }
            }

            test("returns Error when validation fails") {
                val messages = mutableListOf<Message>()
                val error = Accumulate.Error()
                val validation =
                    Validation(acc = {
                        messages.addAll(it)
                        error
                    })
                with(validation) {
                    val accumulated =
                        accumulating {
                            raise(text("error"))
                            @Suppress("KotlinUnreachableCode")
                            fail("never happened")
                        }
                    accumulated shouldBe error
                    messages.size shouldBe 1
                    messages[0].text shouldBe "error"
                }
            }

            test("accumulates multiple errors") {
                val messages = mutableListOf<Message>()
                val error = Accumulate.Error()
                val validation =
                    Validation(acc = {
                        messages.addAll(it)
                        error
                    })
                with(validation) {
                    val result =
                        recoverValidation({ "recovered" }) {
                            val accumulated1 =
                                accumulating {
                                    raise(text("error1"))
                                }
                            val accumulated2 =
                                accumulating {
                                    raise(text("error2"))
                                }
                            accumulated1 shouldBe error
                            accumulated2 shouldBe error
                            "completed"
                        }
                    result shouldBe "completed"
                    messages.size shouldBe 2
                    messages[0].text shouldBe "error1"
                    messages[1].text shouldBe "error2"
                }
            }
        }

        context("getValue") {
            test("supports property delegation with getValue") {
                val value by Accumulate.Ok(10)
                value shouldBe 10
            }
        }
    })
