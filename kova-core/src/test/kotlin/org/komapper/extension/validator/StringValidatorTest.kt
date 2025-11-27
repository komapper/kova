package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class StringValidatorTest :
    FunSpec({

        context("plus") {
            val validator = Kova.string().max(2) + Kova.string().max(3)
            validator.shouldBeInstanceOf<CharSequenceValidator<String>>()

            test("success") {
                val result = validator.tryValidate("1")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "1"
            }

            test("failure") {
                val result = validator.tryValidate("1234")
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 2
                result.messages[0].content shouldBe "\"1234\" must be at most 2 characters"
                result.messages[1].content shouldBe "\"1234\" must be at most 3 characters"
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
                result.isSuccess().mustBeTrue()
            }

            test("failure") {
                val result = validator.tryValidate("NG")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "Constraint failed"
            }
        }

        context("min") {
            val min = Kova.string().min(3)

            test("success") {
                val result = min.tryValidate("abc")
                result.isSuccess().mustBeTrue()
            }

            test("failure") {
                val result = min.tryValidate("ab")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"ab\" must be at least 3 characters"
            }
        }

        context("max") {
            val max = Kova.string().max(1)

            test("success") {
                val result = max.tryValidate("a")
                result.isSuccess().mustBeTrue()
            }

            test("failure") {
                val result = max.tryValidate("ab")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"ab\" must be at most 1 characters"
            }
        }

        context("length") {
            val length = Kova.string().length(1)

            test("success") {
                val result = length.tryValidate("a")
                result.isSuccess().mustBeTrue()
            }

            test("failure") {
                val result = length.tryValidate("ab")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"ab\" must be exactly 1 characters"
            }
        }

        context("isBlank") {
            val isBlank = Kova.string().isBlank()

            test("success") {
                val result = isBlank.tryValidate("   ")
                result.isSuccess().mustBeTrue()
            }

            test("failure") {
                val result = isBlank.tryValidate("ab")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"ab\" must be blank"
            }
        }

        context("isNotBlank") {
            val isNotBlank = Kova.string().isNotBlank()

            test("success") {
                val result = isNotBlank.tryValidate("ab")
                result.isSuccess().mustBeTrue()
            }
            test("failure") {
                val result = isNotBlank.tryValidate("")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"\" must not be blank"
            }
        }

        context("isEmpty") {
            val isEmpty = Kova.string().isEmpty()

            test("success") {
                val result = isEmpty.tryValidate("")
                result.isSuccess().mustBeTrue()
            }

            test("failure") {
                val result = isEmpty.tryValidate("ab")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"ab\" must be empty"
            }
        }

        context("isNotEmpty") {
            val isNotEmpty = Kova.string().isNotEmpty()

            test("success") {
                val result = isNotEmpty.tryValidate("ab")
                result.isSuccess().mustBeTrue()
            }

            test("failure") {
                val result = isNotEmpty.tryValidate("")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"\" must not be empty"
            }
        }

        context("startsWith") {
            val startsWith = Kova.string().startsWith("ab")

            test("success") {
                val result = startsWith.tryValidate("abcde")
                result.isSuccess().mustBeTrue()
            }
            test("failure") {
                val result = startsWith.tryValidate("cde")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"cde\" must start with \"ab\""
            }
        }

        context("endsWith") {
            val endsWith = Kova.string().endsWith("de")

            test("success") {
                val result = endsWith.tryValidate("abcde")
                result.isSuccess().mustBeTrue()
            }
            test("failure") {
                val result = endsWith.tryValidate("ab")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"ab\" must end with \"de\""
            }
        }

        context("contains") {
            val contains = Kova.string().contains("cd")

            test("success") {
                val result = contains.tryValidate("abcde")
                result.isSuccess().mustBeTrue()
            }
            test("failure") {
                val result = contains.tryValidate("fg")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"fg\" must contain \"cd\""
            }
        }

        context("isInt") {
            val isInt = Kova.string().isInt()

            test("success") {
                val result = isInt.tryValidate("123")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "123"
            }
            test("failure") {
                val result = isInt.tryValidate("123a")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"123a\" must be an int"
            }
        }

        context("toInt") {
            val toInt = Kova.string().toInt()

            test("success") {
                val result = toInt.tryValidate("123")
                result.isSuccess().mustBeTrue()
                result.value shouldBe 123
            }
            test("failure") {
                val result = toInt.tryValidate("123a")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"123a\" must be an int"
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
                result.isSuccess().mustBeTrue()
                result.value shouldBe "1"
            }
            test("failure - null") {
                val result = max1.tryValidate(null)
                result.isFailure().mustBeTrue()
            }
            test("failure") {
                val result = max1.tryValidate("12")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"12\" must be at most 1 characters"
            }
        }

        context("literal") {
            val tuna = Kova.string().literal("tuna")

            test("success") {
                val result = tuna.tryValidate("tuna")
                result.isSuccess().mustBeTrue()
            }
            test("failure") {
                val result = tuna.tryValidate("salmon")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"salmon\" must be \"tuna\""
            }
        }

        context("literals") {
            val tuna = Kova.string().literals(listOf("tuna", "sushi"))

            test("success") {
                val result = tuna.tryValidate("tuna")
                result.isSuccess().mustBeTrue()
            }
            test("failure") {
                val result = tuna.tryValidate("salmon")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"salmon\" must be one of [tuna, sushi]"
            }
        }

        context("map - string bools") {
            val stringBools =
                Kova.string().map {
                    when (it) {
                        "true" -> true
                        "1" -> true
                        "false" -> false
                        "0" -> false
                        else -> Kova.error(Message.Text("\"$it\" is not a boolean value"))
                    }
                }

            test("success - true") {
                val result = stringBools.tryValidate("true")
                result.isSuccess().mustBeTrue()
                result.value shouldBe true
            }
            test("success - 1") {
                val result = stringBools.tryValidate("1")
                result.isSuccess().mustBeTrue()
                result.value shouldBe true
            }
            test("success - false") {
                val result = stringBools.tryValidate("false")
                result.isSuccess().mustBeTrue()
                result.value shouldBe false
            }
            test("success - 0") {
                val result = stringBools.tryValidate("0")
                result.isSuccess().mustBeTrue()
                result.value shouldBe false
            }
            test("failure") {
                val result = stringBools.tryValidate("abc")
                result.isFailure().mustBeTrue()
                val message = result.details.single().message
                message.content shouldBe "\"abc\" is not a boolean value"
            }
        }
    })
