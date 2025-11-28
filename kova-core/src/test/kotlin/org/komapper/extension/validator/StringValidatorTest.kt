package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class StringValidatorTest :
    FunSpec({

        context("plus") {
            val validator = Kova.string().max(2) + Kova.string().max(3)

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

        context("constrain") {
            val validator =
                Kova.string().constrain("test") {
                    satisfies(it.input == "OK", "Constraint failed")
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

        context("notBlank") {
            val notBlank = Kova.string().notBlank()

            test("success") {
                val result = notBlank.tryValidate("ab")
                result.isSuccess().mustBeTrue()
            }
            test("failure") {
                val result = notBlank.tryValidate("")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"\" must not be blank"
            }
        }

        context("notEmpty") {
            val notEmpty = Kova.string().notEmpty()

            test("success") {
                val result = notEmpty.tryValidate("ab")
                result.isSuccess().mustBeTrue()
            }

            test("failure") {
                val result = notEmpty.tryValidate("")
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

        context("matches") {
            val emailPattern = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$")
            val matches = Kova.string().matches(emailPattern)

            test("success") {
                val result = matches.tryValidate("user@example.com")
                result.isSuccess().mustBeTrue()
            }
            test("failure") {
                val result = matches.tryValidate("invalid-email")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe
                    "\"invalid-email\" must match pattern ^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$"
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

        context("uppercase") {
            val uppercase = Kova.string().uppercase()

            test("success") {
                val result = uppercase.tryValidate("HELLO")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "HELLO"
            }
            test("success - empty string") {
                val result = uppercase.tryValidate("")
                result.isSuccess().mustBeTrue()
                result.value shouldBe ""
            }
            test("failure") {
                val result = uppercase.tryValidate("Hello")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"Hello\" must be uppercase"
            }
        }

        context("lowercase") {
            val lowercase = Kova.string().lowercase()

            test("success") {
                val result = lowercase.tryValidate("hello")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "hello"
            }
            test("success - empty string") {
                val result = lowercase.tryValidate("")
                result.isSuccess().mustBeTrue()
                result.value shouldBe ""
            }
            test("failure") {
                val result = lowercase.tryValidate("Hello")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"Hello\" must be lowercase"
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
                    .notNull()
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

        context("trim") {
            val trim = Kova.string().trim()

            test("success - trimming leading whitespace") {
                val result = trim.tryValidate("  hello")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "hello"
            }

            test("success - trimming trailing whitespace") {
                val result = trim.tryValidate("hello  ")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "hello"
            }

            test("success - trimming both sides") {
                val result = trim.tryValidate("  hello  ")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "hello"
            }

            test("success - no whitespace to trim") {
                val result = trim.tryValidate("hello")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "hello"
            }

            test("success - empty string") {
                val result = trim.tryValidate("")
                result.isSuccess().mustBeTrue()
                result.value shouldBe ""
            }

            test("success - only whitespace") {
                val result = trim.tryValidate("   ")
                result.isSuccess().mustBeTrue()
                result.value shouldBe ""
            }
        }

        context("trim with constraints") {
            val trimMin3 = Kova.string().trim().min(3)

            test("success - trimmed value meets constraint") {
                val result = trimMin3.tryValidate("  hello  ")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "hello"
            }

            test("failure - trimmed value violates constraint") {
                val result = trimMin3.tryValidate("  hi  ")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"hi\" must be at least 3 characters"
            }

            test("failure - whitespace only becomes empty after trim") {
                val result = trimMin3.tryValidate("   ")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"\" must be at least 3 characters"
            }
        }

        context("toUpperCase") {
            val toUpperCase = Kova.string().toUpperCase()

            test("success - lowercase to uppercase") {
                val result = toUpperCase.tryValidate("hello")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "HELLO"
            }

            test("success - mixed case to uppercase") {
                val result = toUpperCase.tryValidate("HeLLo")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "HELLO"
            }

            test("success - already uppercase") {
                val result = toUpperCase.tryValidate("HELLO")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "HELLO"
            }

            test("success - empty string") {
                val result = toUpperCase.tryValidate("")
                result.isSuccess().mustBeTrue()
                result.value shouldBe ""
            }

            test("success - with numbers and symbols") {
                val result = toUpperCase.tryValidate("hello123!@#")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "HELLO123!@#"
            }
        }

        context("toUpperCase with constraints") {
            val toUpperCaseMin3 = Kova.string().toUpperCase().min(3)

            test("success - transformed value meets constraint") {
                val result = toUpperCaseMin3.tryValidate("hello")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "HELLO"
            }

            test("failure - transformed value violates constraint") {
                val result = toUpperCaseMin3.tryValidate("hi")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"HI\" must be at least 3 characters"
            }

            test("success - combining toUpperCase with startsWith") {
                val toUpperCaseStartsWithH = Kova.string().toUpperCase().startsWith("H")
                val result = toUpperCaseStartsWithH.tryValidate("hello")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "HELLO"
            }
        }

        context("toLowerCase") {
            val toLowerCase = Kova.string().toLowerCase()

            test("success - uppercase to lowercase") {
                val result = toLowerCase.tryValidate("HELLO")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "hello"
            }

            test("success - mixed case to lowercase") {
                val result = toLowerCase.tryValidate("HeLLo")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "hello"
            }

            test("success - already lowercase") {
                val result = toLowerCase.tryValidate("hello")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "hello"
            }

            test("success - empty string") {
                val result = toLowerCase.tryValidate("")
                result.isSuccess().mustBeTrue()
                result.value shouldBe ""
            }

            test("success - with numbers and symbols") {
                val result = toLowerCase.tryValidate("HELLO123!@#")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "hello123!@#"
            }
        }

        context("toLowerCase with constraints") {
            val toLowerCaseMin3 = Kova.string().toLowerCase().min(3)

            test("success - transformed value meets constraint") {
                val result = toLowerCaseMin3.tryValidate("HELLO")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "hello"
            }

            test("failure - transformed value violates constraint") {
                val result = toLowerCaseMin3.tryValidate("HI")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"hi\" must be at least 3 characters"
            }

            test("success - combining toLowerCase with startsWith") {
                val toLowerCaseStartsWithH = Kova.string().toLowerCase().startsWith("h")
                val result = toLowerCaseStartsWithH.tryValidate("HELLO")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "hello"
            }
        }
    })
