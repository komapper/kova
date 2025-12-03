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

        context("and") {
            val validator = Kova.string().max(2) and Kova.string().max(3)

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

        context("or") {
            val validator = (Kova.string().isInt() or Kova.literal("zero")).toUpperCase()

            test("success - int") {
                val result = validator.tryValidate("1")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "1"
            }

            test("success - literal") {
                val result = validator.tryValidate("zero")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "ZERO"
            }

            test("failure") {
                val result = validator.tryValidate("abc")
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.or"
            }
        }

        context("chain") {
            val length = Kova.string().length(3)
            val validator =
                Kova
                    .string()
                    .trim()
                    .chain(length)
                    .toUpperCase()

            test("success") {
                val result = validator.tryValidate(" abc ")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "ABC"
            }

            test("failure") {
                val result = validator.tryValidate(" a ")
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "\"a\" must be exactly 3 characters"
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

        context("notBlank with message") {
            val notBlank = Kova.string().notBlank(Message.text0 { "Must not be blank" })

            test("success") {
                val result = notBlank.tryValidate("ab")
                result.isSuccess().mustBeTrue()
            }
            test("failure") {
                val result = notBlank.tryValidate("")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "Must not be blank"
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

        context("email") {
            val email = Kova.string().email()

            test("success - simple email") {
                val result = email.tryValidate("user@example.com")
                result.isSuccess().mustBeTrue()
            }
            test("success - with dots") {
                val result = email.tryValidate("first.last@example.com")
                result.isSuccess().mustBeTrue()
            }
            test("success - with plus") {
                val result = email.tryValidate("user+tag@example.com")
                result.isSuccess().mustBeTrue()
            }
            test("success - with hyphen in domain") {
                val result = email.tryValidate("user@my-domain.com")
                result.isSuccess().mustBeTrue()
            }
            test("success - with subdomain") {
                val result = email.tryValidate("user@mail.example.com")
                result.isSuccess().mustBeTrue()
            }
            test("success - with numbers") {
                val result = email.tryValidate("user123@example.com")
                result.isSuccess().mustBeTrue()
            }
            test("success - with underscore") {
                val result = email.tryValidate("user_name@example.com")
                result.isSuccess().mustBeTrue()
            }
            test("success - case insensitive") {
                val result = email.tryValidate("User@Example.COM")
                result.isSuccess().mustBeTrue()
            }
            test("failure - starts with dot") {
                val result = email.tryValidate(".user@example.com")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\".user@example.com\" must be a valid email address"
            }
            test("failure - consecutive dots") {
                val result = email.tryValidate("user..name@example.com")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"user..name@example.com\" must be a valid email address"
            }
            test("failure - ends with dot before @") {
                val result = email.tryValidate("user.@example.com")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"user.@example.com\" must be a valid email address"
            }
            test("failure - no @") {
                val result = email.tryValidate("userexample.com")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"userexample.com\" must be a valid email address"
            }
            test("failure - no domain") {
                val result = email.tryValidate("user@")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"user@\" must be a valid email address"
            }
            test("failure - no local part") {
                val result = email.tryValidate("@example.com")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"@example.com\" must be a valid email address"
            }
            test("failure - no TLD") {
                val result = email.tryValidate("user@example")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"user@example\" must be a valid email address"
            }
            test("failure - spaces") {
                val result = email.tryValidate("user name@example.com")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"user name@example.com\" must be a valid email address"
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

        context("isLong") {
            val isLong = Kova.string().isLong()

            test("success") {
                val result = isLong.tryValidate("9223372036854775807")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "9223372036854775807"
            }
            test("failure") {
                val result = isLong.tryValidate("123.45")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"123.45\" must be a long"
            }
        }

        context("isShort") {
            val isShort = Kova.string().isShort()

            test("success") {
                val result = isShort.tryValidate("32767")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "32767"
            }
            test("failure") {
                val result = isShort.tryValidate("99999")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"99999\" must be a short"
            }
        }

        context("isByte") {
            val isByte = Kova.string().isByte()

            test("success") {
                val result = isByte.tryValidate("127")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "127"
            }
            test("failure") {
                val result = isByte.tryValidate("256")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"256\" must be a byte"
            }
        }

        context("isDouble") {
            val isDouble = Kova.string().isDouble()

            test("success") {
                val result = isDouble.tryValidate("123.45")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "123.45"
            }
            test("failure") {
                val result = isDouble.tryValidate("abc")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"abc\" must be a double"
            }
        }

        context("isFloat") {
            val isFloat = Kova.string().isFloat()

            test("success") {
                val result = isFloat.tryValidate("123.45")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "123.45"
            }
            test("failure") {
                val result = isFloat.tryValidate("abc")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"abc\" must be a float"
            }
        }

        context("isBigDecimal") {
            val isBigDecimal = Kova.string().isBigDecimal()

            test("success") {
                val result = isBigDecimal.tryValidate("123.456789012345678901234567890")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "123.456789012345678901234567890"
            }
            test("failure") {
                val result = isBigDecimal.tryValidate("abc")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"abc\" must be a big decimal"
            }
        }

        context("isBigInteger") {
            val isBigInteger = Kova.string().isBigInteger()

            test("success") {
                val result = isBigInteger.tryValidate("12345678901234567890")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "12345678901234567890"
            }
            test("failure") {
                val result = isBigInteger.tryValidate("123.45")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"123.45\" must be a big integer"
            }
        }

        context("isBoolean") {
            val isBoolean = Kova.string().isBoolean()

            test("success - true") {
                val result = isBoolean.tryValidate("true")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "true"
            }
            test("success - false") {
                val result = isBoolean.tryValidate("false")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "false"
            }
            test("false - case sensitive") {
                val result = isBoolean.tryValidate("TRUE")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"TRUE\" must be a boolean"
            }
            test("failure") {
                val result = isBoolean.tryValidate("yes")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"yes\" must be a boolean"
            }
        }

        context("toBoolean") {
            val toBoolean = Kova.string().toBoolean()

            test("success - true") {
                val result = toBoolean.tryValidate("true")
                result.isSuccess().mustBeTrue()
                result.value shouldBe true
            }
            test("success - false") {
                val result = toBoolean.tryValidate("false")
                result.isSuccess().mustBeTrue()
                result.value shouldBe false
            }
            test("failure") {
                val result = toBoolean.tryValidate("yes")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"yes\" must be a boolean"
            }
        }

        context("toLong") {
            val toLong = Kova.string().toLong()

            test("success") {
                val result = toLong.tryValidate("9223372036854775807")
                result.isSuccess().mustBeTrue()
                result.value shouldBe 9223372036854775807L
            }
            test("failure") {
                val result = toLong.tryValidate("abc")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"abc\" must be a long"
            }
        }

        context("toShort") {
            val toShort = Kova.string().toShort()

            test("success") {
                val result = toShort.tryValidate("32767")
                result.isSuccess().mustBeTrue()
                result.value shouldBe 32767.toShort()
            }
            test("failure") {
                val result = toShort.tryValidate("99999")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"99999\" must be a short"
            }
        }

        context("toByte") {
            val toByte = Kova.string().toByte()

            test("success") {
                val result = toByte.tryValidate("127")
                result.isSuccess().mustBeTrue()
                result.value shouldBe 127.toByte()
            }
            test("failure") {
                val result = toByte.tryValidate("256")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"256\" must be a byte"
            }
        }

        context("toDouble") {
            val toDouble = Kova.string().toDouble()

            test("success") {
                val result = toDouble.tryValidate("123.45")
                result.isSuccess().mustBeTrue()
                result.value shouldBe 123.45
            }
            test("failure") {
                val result = toDouble.tryValidate("abc")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"abc\" must be a double"
            }
        }

        context("toFloat") {
            val toFloat = Kova.string().toFloat()

            test("success") {
                val result = toFloat.tryValidate("123.45")
                result.isSuccess().mustBeTrue()
                result.value shouldBe 123.45f
            }
            test("failure") {
                val result = toFloat.tryValidate("abc")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"abc\" must be a float"
            }
        }

        context("toBigDecimal") {
            val toBigDecimal = Kova.string().toBigDecimal()

            test("success") {
                val result = toBigDecimal.tryValidate("123.456789012345678901234567890")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "123.456789012345678901234567890".toBigDecimal()
            }
            test("failure") {
                val result = toBigDecimal.tryValidate("abc")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"abc\" must be a big decimal"
            }
        }

        context("toBigInteger") {
            val toBigInteger = Kova.string().toBigInteger()

            test("success") {
                val result = toBigInteger.tryValidate("12345678901234567890")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "12345678901234567890".toBigInteger()
            }
            test("failure") {
                val result = toBigInteger.tryValidate("123.45")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"123.45\" must be a big integer"
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
                    .toNonNullable()
                    .then(Kova.string().max(1))

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

        context("isEnum with Type") {
            val isEnum = Kova.string().isEnum<Status>()

            test("success - ACTIVE") {
                val result = isEnum.tryValidate("ACTIVE")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "ACTIVE"
            }
            test("success - INACTIVE") {
                val result = isEnum.tryValidate("INACTIVE")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "INACTIVE"
            }
            test("success - PENDING") {
                val result = isEnum.tryValidate("PENDING")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "PENDING"
            }
            test("failure - invalid value") {
                val result = isEnum.tryValidate("INVALID")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"INVALID\" must be one of [ACTIVE, INACTIVE, PENDING]"
            }
            test("failure - lowercase") {
                val result = isEnum.tryValidate("active")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"active\" must be one of [ACTIVE, INACTIVE, PENDING]"
            }
        }

        context("isEnum with KClass") {
            val isEnum = Kova.string().isEnum(Status::class)

            test("success - ACTIVE") {
                val result = isEnum.tryValidate("ACTIVE")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "ACTIVE"
            }
            test("success - INACTIVE") {
                val result = isEnum.tryValidate("INACTIVE")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "INACTIVE"
            }
            test("success - PENDING") {
                val result = isEnum.tryValidate("PENDING")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "PENDING"
            }
            test("failure - invalid value") {
                val result = isEnum.tryValidate("INVALID")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"INVALID\" must be one of [ACTIVE, INACTIVE, PENDING]"
            }
            test("failure - lowercase") {
                val result = isEnum.tryValidate("active")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"active\" must be one of [ACTIVE, INACTIVE, PENDING]"
            }
        }

        context("toEnum") {
            val toEnum = Kova.string().toEnum<Status>()

            test("success - ACTIVE") {
                val result = toEnum.tryValidate("ACTIVE")
                result.isSuccess().mustBeTrue()
                result.value shouldBe Status.ACTIVE
            }
            test("success - INACTIVE") {
                val result = toEnum.tryValidate("INACTIVE")
                result.isSuccess().mustBeTrue()
                result.value shouldBe Status.INACTIVE
            }
            test("success - PENDING") {
                val result = toEnum.tryValidate("PENDING")
                result.isSuccess().mustBeTrue()
                result.value shouldBe Status.PENDING
            }
            test("failure - invalid value") {
                val result = toEnum.tryValidate("INVALID")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"INVALID\" must be one of [ACTIVE, INACTIVE, PENDING]"
            }
            test("failure - lowercase") {
                val result = toEnum.tryValidate("active")
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"active\" must be one of [ACTIVE, INACTIVE, PENDING]"
            }
        }
    }) {
    enum class Status {
        ACTIVE,
        INACTIVE,
        PENDING,
    }
}
