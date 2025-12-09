package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class StringValidatorTest :
    FunSpec({

        context("mapping operation after failure") {
            val validator =
                Kova
                    .string()
                    .trim()
                    .min(3)
                    .toUppercase()
                    .max(3)

            test("failure") {
                val logs = mutableListOf<LogEntry>()
                val result = validator.tryValidate("  ab  ", config = ValidationConfig(logger = { logs.add(it) }))
                result.isFailure().mustBeTrue()
                logs shouldBe
                    listOf(
                        LogEntry.Violated(constraintId = "kova.string.min", root = "", path = "", input = "ab"),
                        LogEntry.Satisfied(constraintId = "kova.string.max", root = "", path = "", input = "AB"),
                    )
            }
        }

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
                result.messages[0].constraintId shouldBe "kova.string.max"
                result.messages[1].constraintId shouldBe "kova.string.max"
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
                result.messages[0].constraintId shouldBe "kova.string.max"
                result.messages[1].constraintId shouldBe "kova.string.max"
            }
        }

        context("or") {
            val validator = (Kova.string().isInt() or Kova.literal("zero")).toUppercase()

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
                    .toUppercase()

            test("success") {
                val result = validator.tryValidate(" abc ")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "ABC"
            }

            test("failure") {
                val result = validator.tryValidate(" a ")
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.string.length"
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
                result.messages.single().text shouldBe "Constraint failed"
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
                result.messages.single().constraintId shouldBe "kova.string.min"
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
                result.messages.single().constraintId shouldBe "kova.string.max"
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
                result.messages.single().constraintId shouldBe "kova.string.length"
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
                result.messages.single().constraintId shouldBe "kova.string.notBlank"
            }
        }

        context("blank") {
            val blank = Kova.string().blank()

            test("success - empty string") {
                val result = blank.tryValidate("")
                result.isSuccess().mustBeTrue()
            }
            test("success - whitespace only") {
                val result = blank.tryValidate("   ")
                result.isSuccess().mustBeTrue()
            }
            test("failure") {
                val result = blank.tryValidate("ab")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.string.blank"
            }
        }

        context("notBlank with message") {
            val notBlank = Kova.string().notBlank(Message.text { "Must not be blank" })

            test("success") {
                val result = notBlank.tryValidate("ab")
                result.isSuccess().mustBeTrue()
            }
            test("failure") {
                val result = notBlank.tryValidate("")
                result.isFailure().mustBeTrue()
                result.messages.single().text shouldBe "Must not be blank"
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
                result.messages.single().constraintId shouldBe "kova.string.notEmpty"
            }
        }

        context("empty") {
            val empty = Kova.string().empty()

            test("success") {
                val result = empty.tryValidate("")
                result.isSuccess().mustBeTrue()
            }
            test("failure - with content") {
                val result = empty.tryValidate("ab")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.string.empty"
            }
            test("failure - whitespace only") {
                val result = empty.tryValidate("   ")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.string.empty"
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
                result.messages.single().constraintId shouldBe "kova.string.startsWith"
            }
        }

        context("notStartsWith") {
            val notStartsWith = Kova.string().notStartsWith("ab")

            test("success") {
                val result = notStartsWith.tryValidate("cde")
                result.isSuccess().mustBeTrue()
            }
            test("failure") {
                val result = notStartsWith.tryValidate("abcde")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.string.notStartsWith"
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
                result.messages.single().constraintId shouldBe "kova.string.endsWith"
            }
        }

        context("notEndsWith") {
            val notEndsWith = Kova.string().notEndsWith("de")

            test("success") {
                val result = notEndsWith.tryValidate("ab")
                result.isSuccess().mustBeTrue()
            }
            test("failure") {
                val result = notEndsWith.tryValidate("abcde")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.string.notEndsWith"
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
                result.messages.single().constraintId shouldBe "kova.string.contains"
            }
        }

        context("notContains") {
            val notContains = Kova.string().notContains("cd")

            test("success") {
                val result = notContains.tryValidate("fg")
                result.isSuccess().mustBeTrue()
            }
            test("failure") {
                val result = notContains.tryValidate("abcde")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.string.notContains"
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
                result.messages.single().constraintId shouldBe "kova.string.matches"
            }
        }

        context("notMatches") {
            val digitPattern = Regex("^\\d+\$")
            val notMatches = Kova.string().notMatches(digitPattern)

            test("success") {
                val result = notMatches.tryValidate("hello")
                result.isSuccess().mustBeTrue()
            }
            test("failure") {
                val result = notMatches.tryValidate("12345")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.string.notMatches"
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
                result.messages.single().constraintId shouldBe "kova.string.email"
            }
            test("failure - consecutive dots") {
                val result = email.tryValidate("user..name@example.com")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.string.email"
            }
            test("failure - ends with dot before @") {
                val result = email.tryValidate("user.@example.com")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.string.email"
            }
            test("failure - no @") {
                val result = email.tryValidate("userexample.com")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.string.email"
            }
            test("failure - no domain") {
                val result = email.tryValidate("user@")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.string.email"
            }
            test("failure - no local part") {
                val result = email.tryValidate("@example.com")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.string.email"
            }
            test("failure - no TLD") {
                val result = email.tryValidate("user@example")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.string.email"
            }
            test("failure - spaces") {
                val result = email.tryValidate("user name@example.com")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.string.email"
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
                result.messages.single().constraintId shouldBe "kova.string.isInt"
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
                result.messages.single().constraintId shouldBe "kova.string.isLong"
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
                result.messages.single().constraintId shouldBe "kova.string.isShort"
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
                result.messages.single().constraintId shouldBe "kova.string.isByte"
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
                result.messages.single().constraintId shouldBe "kova.string.isDouble"
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
                result.messages.single().constraintId shouldBe "kova.string.isFloat"
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
                result.messages.single().constraintId shouldBe "kova.string.isBigDecimal"
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
                result.messages.single().constraintId shouldBe "kova.string.isBigInteger"
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
                result.messages.single().constraintId shouldBe "kova.string.isBoolean"
            }
            test("failure") {
                val result = isBoolean.tryValidate("yes")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.string.isBoolean"
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
                result.messages.single().constraintId shouldBe "kova.string.isBoolean"
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
                result.messages.single().constraintId shouldBe "kova.string.isLong"
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
                result.messages.single().constraintId shouldBe "kova.string.isShort"
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
                result.messages.single().constraintId shouldBe "kova.string.isByte"
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
                result.messages.single().constraintId shouldBe "kova.string.isDouble"
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
                result.messages.single().constraintId shouldBe "kova.string.isFloat"
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
                result.messages.single().constraintId shouldBe "kova.string.isBigDecimal"
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
                result.messages.single().constraintId shouldBe "kova.string.isBigInteger"
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
                result.messages.single().constraintId shouldBe "kova.string.uppercase"
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
                result.messages.single().constraintId shouldBe "kova.string.lowercase"
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
                result.messages.single().constraintId shouldBe "kova.string.isInt"
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
                result.messages.single().constraintId shouldBe "kova.string.max"
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
                        else -> Kova.fail("is not a boolean value")
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
                val message = result.messages.single()
                message.text shouldBe "is not a boolean value"
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
                result.messages.single().constraintId shouldBe "kova.string.min"
            }

            test("failure - whitespace only becomes empty after trim") {
                val result = trimMin3.tryValidate("   ")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.string.min"
            }
        }

        context("toUpperCase") {
            val toUpperCase = Kova.string().toUppercase()

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
            val toUpperCaseMin3 = Kova.string().toUppercase().min(3)

            test("success - transformed value meets constraint") {
                val result = toUpperCaseMin3.tryValidate("hello")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "HELLO"
            }

            test("failure - transformed value violates constraint") {
                val result = toUpperCaseMin3.tryValidate("hi")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.string.min"
            }

            test("success - combining toUpperCase with startsWith") {
                val toUpperCaseStartsWithH = Kova.string().toUppercase().startsWith("H")
                val result = toUpperCaseStartsWithH.tryValidate("hello")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "HELLO"
            }
        }

        context("toLowerCase") {
            val toLowerCase = Kova.string().toLowercase()

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
            val toLowerCaseMin3 = Kova.string().toLowercase().min(3)

            test("success - transformed value meets constraint") {
                val result = toLowerCaseMin3.tryValidate("HELLO")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "hello"
            }

            test("failure - transformed value violates constraint") {
                val result = toLowerCaseMin3.tryValidate("HI")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.string.min"
            }

            test("success - combining toLowerCase with startsWith") {
                val toLowerCaseStartsWithH = Kova.string().toLowercase().startsWith("h")
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
                result.messages.single().constraintId shouldBe "kova.string.isEnum"
            }
            test("failure - lowercase") {
                val result = isEnum.tryValidate("active")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.string.isEnum"
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
                result.messages.single().constraintId shouldBe "kova.string.isEnum"
            }
            test("failure - lowercase") {
                val result = isEnum.tryValidate("active")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.string.isEnum"
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
                result.messages.single().constraintId shouldBe "kova.string.isEnum"
            }
            test("failure - lowercase") {
                val result = toEnum.tryValidate("active")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.string.isEnum"
            }
        }
    }) {
    enum class Status {
        ACTIVE,
        INACTIVE,
        PENDING,
    }
}
