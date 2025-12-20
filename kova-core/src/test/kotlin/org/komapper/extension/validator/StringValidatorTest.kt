package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class StringValidatorTest :
    FunSpec({

        context("or") {
            val validator = (Kova.string().isInt() or Kova.literal("zero")).toUppercase()

            test("success with int value") {
                val result = validator.tryValidate("1")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "1"
            }

            test("success with literal value") {
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

        context("constrain") {
            val validator =
                Kova.string().constrain {
                    satisfies(it == "OK", text("Constraint failed"))
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

        context("notBlank with message") {
            val notBlank = Kova.string().notBlank { text("Must not be blank") }

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

            test("success with true") {
                val result = isBoolean.tryValidate("true")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "true"
            }
            test("success with false") {
                val result = isBoolean.tryValidate("false")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "false"
            }
            test("failure with case sensitive mismatch") {
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

            test("success with true") {
                val result = toBoolean.tryValidate("true")
                result.isSuccess().mustBeTrue()
                result.value shouldBe true
            }
            test("success with false") {
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
            test("success with empty string") {
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
            test("success with empty string") {
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

        context("map - string bools") {
            val stringBools =
                Kova.string().map {
                    when (it) {
                        "true" -> true
                        "1" -> true
                        else -> false
                    }
                }

            test("success with true") {
                val result = stringBools.tryValidate("true")
                result.isSuccess().mustBeTrue()
                result.value shouldBe true
            }
            test("success with value 1") {
                val result = stringBools.tryValidate("1")
                result.isSuccess().mustBeTrue()
                result.value shouldBe true
            }
            test("success with false") {
                val result = stringBools.tryValidate("false")
                result.isSuccess().mustBeTrue()
                result.value shouldBe false
            }
            test("success with value 0") {
                val result = stringBools.tryValidate("0")
                result.isSuccess().mustBeTrue()
                result.value shouldBe false
            }
        }

        context("trim") {
            val trim = Kova.string().trim()

            test("success when trimming leading whitespace") {
                val result = trim.tryValidate("  hello")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "hello"
            }

            test("success when trimming trailing whitespace") {
                val result = trim.tryValidate("hello  ")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "hello"
            }

            test("success when trimming both sides") {
                val result = trim.tryValidate("  hello  ")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "hello"
            }

            test("success with no whitespace to trim") {
                val result = trim.tryValidate("hello")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "hello"
            }

            test("success with empty string") {
                val result = trim.tryValidate("")
                result.isSuccess().mustBeTrue()
                result.value shouldBe ""
            }

            test("success with only whitespace") {
                val result = trim.tryValidate("   ")
                result.isSuccess().mustBeTrue()
                result.value shouldBe ""
            }
        }

        context("toUpperCase") {
            val toUpperCase = Kova.string().toUppercase()

            test("success when converting lowercase to uppercase") {
                val result = toUpperCase.tryValidate("hello")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "HELLO"
            }

            test("success when converting mixed case to uppercase") {
                val result = toUpperCase.tryValidate("HeLLo")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "HELLO"
            }

            test("success with already uppercase") {
                val result = toUpperCase.tryValidate("HELLO")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "HELLO"
            }

            test("success with empty string") {
                val result = toUpperCase.tryValidate("")
                result.isSuccess().mustBeTrue()
                result.value shouldBe ""
            }

            test("success with numbers and symbols") {
                val result = toUpperCase.tryValidate("hello123!@#")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "HELLO123!@#"
            }
        }

        context("toLowerCase") {
            val toLowerCase = Kova.string().toLowercase()

            test("success when converting uppercase to lowercase") {
                val result = toLowerCase.tryValidate("HELLO")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "hello"
            }

            test("success when converting mixed case to lowercase") {
                val result = toLowerCase.tryValidate("HeLLo")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "hello"
            }

            test("success with already lowercase") {
                val result = toLowerCase.tryValidate("hello")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "hello"
            }

            test("success with empty string") {
                val result = toLowerCase.tryValidate("")
                result.isSuccess().mustBeTrue()
                result.value shouldBe ""
            }

            test("success with numbers and symbols") {
                val result = toLowerCase.tryValidate("HELLO123!@#")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "hello123!@#"
            }
        }

        context("isEnum with Type") {
            val isEnum = Kova.string().isEnum<Status>()

            test("success with ACTIVE") {
                val result = isEnum.tryValidate("ACTIVE")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "ACTIVE"
            }
            test("success with INACTIVE") {
                val result = isEnum.tryValidate("INACTIVE")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "INACTIVE"
            }
            test("success with PENDING") {
                val result = isEnum.tryValidate("PENDING")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "PENDING"
            }
            test("failure with invalid value") {
                val result = isEnum.tryValidate("INVALID")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.string.isEnum"
            }
            test("failure with lowercase") {
                val result = isEnum.tryValidate("active")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.string.isEnum"
            }
        }

        context("isEnum with KClass") {
            val isEnum = Kova.string().isEnum(Status::class)

            test("success with ACTIVE") {
                val result = isEnum.tryValidate("ACTIVE")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "ACTIVE"
            }
            test("success with INACTIVE") {
                val result = isEnum.tryValidate("INACTIVE")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "INACTIVE"
            }
            test("success with PENDING") {
                val result = isEnum.tryValidate("PENDING")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "PENDING"
            }
            test("failure with invalid value") {
                val result = isEnum.tryValidate("INVALID")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.string.isEnum"
            }
            test("failure with lowercase") {
                val result = isEnum.tryValidate("active")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.string.isEnum"
            }
        }

        context("toEnum") {
            val toEnum = Kova.string().toEnum<Status>()

            test("success with ACTIVE") {
                val result = toEnum.tryValidate("ACTIVE")
                result.isSuccess().mustBeTrue()
                result.value shouldBe Status.ACTIVE
            }
            test("success with INACTIVE") {
                val result = toEnum.tryValidate("INACTIVE")
                result.isSuccess().mustBeTrue()
                result.value shouldBe Status.INACTIVE
            }
            test("success with PENDING") {
                val result = toEnum.tryValidate("PENDING")
                result.isSuccess().mustBeTrue()
                result.value shouldBe Status.PENDING
            }
            test("failure with invalid value") {
                val result = toEnum.tryValidate("INVALID")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.string.isEnum"
            }
            test("failure with lowercase") {
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
