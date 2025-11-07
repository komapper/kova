package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.assertTrue

class StringValidatorTest :
    FunSpec({

        context("plus") {
            val validator = Kova.string().max(2) + Kova.string().max(3)
            validator.shouldBeInstanceOf<CharSequenceValidator<String>>()

            test("success") {
                val result = validator.tryValidate("1")
                assertTrue(result.isSuccess())
                result.value shouldBe "1"
            }

            test("failure") {
                val result = validator.tryValidate("1234")
                assertTrue(result.isFailure())
                result.messages.size shouldBe 2
                result.messages[0] shouldBe "\"1234\" must be at most 2 characters"
                result.messages[1] shouldBe "\"1234\" must be at most 3 characters"
            }
        }

        context("constraint") {
            val validator =
                Kova.string().constraint {
                    if (it.input == "OK") {
                        ConstraintResult.Satisfied
                    } else {
                        ConstraintResult.Violated("Constraint failed")
                    }
                }

            test("success") {
                val result = validator.tryValidate("OK")
                assertTrue(result.isSuccess())
            }

            test("failure") {
                val result = validator.tryValidate("NG")
                assertTrue(result.isFailure())
                result.messages.single() shouldBe "Constraint failed"
            }
        }

        context("min") {
            val min = Kova.string().min(3)

            test("success") {
                val result = min.tryValidate("abc")
                assertTrue(result.isSuccess())
            }

            test("failure") {
                val result = min.tryValidate("ab")
                assertTrue(result.isFailure())
                result.messages.single() shouldBe "\"ab\" must be at least 3 characters"
            }
        }

        context("max") {
            val max = Kova.string().max(1)

            test("success") {
                val result = max.tryValidate("a")
                assertTrue(result.isSuccess())
            }

            test("failure") {
                val result = max.tryValidate("ab")
                assertTrue(result.isFailure())
                result.messages.single() shouldBe "\"ab\" must be at most 1 characters"
            }
        }

        context("length") {
            val length = Kova.string().length(1)

            test("success") {
                val result = length.tryValidate("a")
                assertTrue(result.isSuccess())
            }

            test("failure") {
                val result = length.tryValidate("ab")
                assertTrue(result.isFailure())
                result.messages.single() shouldBe "\"ab\" must be exactly 1 characters"
            }
        }

        context("isBlank") {
            val isBlank = Kova.string().isBlank()

            test("success") {
                val result = isBlank.tryValidate("   ")
                assertTrue(result.isSuccess())
            }

            test("failure") {
                val result = isBlank.tryValidate("ab")
                assertTrue(result.isFailure())
                result.messages.single() shouldBe "\"ab\" must be blank"
            }
        }

        context("isNotBlank") {
            val isNotBlank = Kova.string().isNotBlank()

            test("success") {
                val result = isNotBlank.tryValidate("ab")
                assertTrue(result.isSuccess())
            }
            test("failure") {
                val result = isNotBlank.tryValidate("")
                assertTrue(result.isFailure())
                result.messages.single() shouldBe "\"\" must not be blank"
            }
        }

        context("isEmpty") {
            val isEmpty = Kova.string().isEmpty()

            test("success") {
                val result = isEmpty.tryValidate("")
                assertTrue(result.isSuccess())
            }

            test("failure") {
                val result = isEmpty.tryValidate("ab")
                assertTrue(result.isFailure())
                result.messages.single() shouldBe "\"ab\" must be empty"
            }
        }

        context("isNotEmpty") {
            val isNotEmpty = Kova.string().isNotEmpty()

            test("success") {
                val result = isNotEmpty.tryValidate("ab")
                assertTrue(result.isSuccess())
            }

            test("failure") {
                val result = isNotEmpty.tryValidate("")
                assertTrue(result.isFailure())
                result.messages.single() shouldBe "\"\" must not be empty"
            }
        }

        context("startsWith") {
            val startsWith = Kova.string().startsWith("ab")

            test("success") {
                val result = startsWith.tryValidate("abcde")
                assertTrue(result.isSuccess())
            }
            test("failure") {
                val result = startsWith.tryValidate("cde")
                assertTrue(result.isFailure())
                result.messages.single() shouldBe "\"cde\" must start with \"ab\""
            }
        }

        context("endsWith") {
            val endsWith = Kova.string().endsWith("de")

            test("success") {
                val result = endsWith.tryValidate("abcde")
                assertTrue(result.isSuccess())
            }
            test("failure") {
                val result = endsWith.tryValidate("ab")
                assertTrue(result.isFailure())
                result.messages.single() shouldBe "\"ab\" must end with \"de\""
            }
        }

        context("contains") {
            val contains = Kova.string().contains("cd")

            test("success") {
                val result = contains.tryValidate("abcde")
                assertTrue(result.isSuccess())
            }
            test("failure") {
                val result = contains.tryValidate("fg")
                assertTrue(result.isFailure())
                result.messages.single() shouldBe "\"fg\" must contain \"cd\""
            }
        }

        context("isInt") {
            val isInt = Kova.string().isInt()

            test("success") {
                val result = isInt.tryValidate("123")
                assertTrue(result.isSuccess())
                result.value shouldBe "123"
            }
            test("failure") {
                val result = isInt.tryValidate("123a")
                assertTrue(result.isFailure())
                result.messages.single() shouldBe "\"123a\" must be an int"
            }
        }

        context("toInt") {
            val toInt = Kova.string().toInt()

            test("success") {
                val result = toInt.tryValidate("123")
                assertTrue(result.isSuccess())
                result.value shouldBe 123
            }
            test("failure") {
                val result = toInt.tryValidate("123a")
                assertTrue(result.isFailure())
                result.messages.single() shouldBe "\"123a\" must be an int"
            }
        }

        context("nullableString") {
            val max1 =
                Kova
                    .nullable<String>()
                    .isNotNull()
                    .whenNotNull(Kova.string().max(1))

            test("success") {
                val result = max1.tryValidate("1")
                assertTrue(result.isSuccess())
                result.value shouldBe "1"
            }
            test("failure - null") {
                val result = max1.tryValidate(null)
                assertTrue(result.isFailure())
            }
            test("failure") {
                val result = max1.tryValidate("12")
                assertTrue(result.isFailure())
                result.messages.single() shouldBe "\"12\" must be at most 1 characters"
            }
        }
    })
