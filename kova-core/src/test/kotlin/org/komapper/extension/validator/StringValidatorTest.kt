package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec

class StringValidatorTest :
    FunSpec({

        context("or") {
            context(_: Validation)
            fun String.validate() = or { isInt() } orElse { literal("zero") } map { toUppercase() }

            test("success with int value") {
                val result = tryValidate { "1".validate() }
                result.shouldBeSuccess()
                result.value shouldBe "1"
            }

            test("success with literal value") {
                val result = tryValidate { "zero".validate() }
                result.shouldBeSuccess()
                result.value shouldBe "ZERO"
            }

            test("failure") {
                val result = tryValidate { "abc".validate() }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.or"
            }
        }

        context("constrain") {
            context(_: Validation)
            fun String.validate() = constrain("test") { satisfies(it == "OK") { text("Constraint failed") } }

            test("success") {
                val result = tryValidate { "OK".validate() }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { "NG".validate() }
                result.shouldBeFailure()
                result.messages.single().text shouldBe "Constraint failed"
            }
        }

        context("notBlank with message") {
            test("success") {
                val result = tryValidate { "ab".notBlank { text("Must not be blank") } }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "".notBlank { text("Must not be blank") } }
                result.shouldBeFailure()
                result.messages.single().text shouldBe "Must not be blank"
            }
        }

        context("isInt") {
            test("success") {
                val result = tryValidate { "123".isInt() }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "123a".isInt() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isInt"
            }
        }

        context("isLong") {
            test("success") {
                val result = tryValidate { "9223372036854775807".isLong() }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "123.45".isLong() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isLong"
            }
        }

        context("isShort") {
            test("success") {
                val result = tryValidate { "32767".isShort() }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "99999".isShort() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isShort"
            }
        }

        context("isByte") {
            test("success") {
                val result = tryValidate { "127".isByte() }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "256".isByte() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isByte"
            }
        }

        context("isDouble") {
            test("success") {
                val result = tryValidate { "123.45".isDouble() }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "abc".isDouble() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isDouble"
            }
        }

        context("isFloat") {
            test("success") {
                val result = tryValidate { "123.45".isFloat() }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "abc".isFloat() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isFloat"
            }
        }

        context("isBigDecimal") {
            test("success") {
                val result = tryValidate { "123.456789012345678901234567890".isBigDecimal() }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "abc".isBigDecimal() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isBigDecimal"
            }
        }

        context("isBigInteger") {
            test("success") {
                val result = tryValidate { "12345678901234567890".isBigInteger() }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "123.45".isBigInteger() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isBigInteger"
            }
        }

        context("isBoolean") {
            test("success with true") {
                val result = tryValidate { "true".isBoolean() }
                result.shouldBeSuccess()
            }
            test("success with false") {
                val result = tryValidate { "false".isBoolean() }
                result.shouldBeSuccess()
            }
            test("failure with case sensitive mismatch") {
                val result = tryValidate { "TRUE".isBoolean() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isBoolean"
            }
            test("failure") {
                val result = tryValidate { "yes".isBoolean() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isBoolean"
            }
        }

        context("toBoolean") {
            test("success with true") {
                val result = tryValidate { "true".toBoolean() }
                result.shouldBeSuccess()
                result.value shouldBe true
            }
            test("success with false") {
                val result = tryValidate { "false".toBoolean() }
                result.shouldBeSuccess()
                result.value shouldBe false
            }
            test("failure") {
                val result = tryValidate { "yes".toBoolean() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isBoolean"
            }
        }

        context("toLong") {
            test("success") {
                val result = tryValidate { "9223372036854775807".toLong() }
                result.shouldBeSuccess()
                result.value shouldBe 9223372036854775807L
            }
            test("failure") {
                val result = tryValidate { "abc".toLong() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isLong"
            }
        }

        context("toShort") {
            test("success") {
                val result = tryValidate { "32767".toShort() }
                result.shouldBeSuccess()
                result.value shouldBe 32767.toShort()
            }
            test("failure") {
                val result = tryValidate { "99999".toShort() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isShort"
            }
        }

        context("toByte") {
            test("success") {
                val result = tryValidate { "127".toByte() }
                result.shouldBeSuccess()
                result.value shouldBe 127.toByte()
            }
            test("failure") {
                val result = tryValidate { "256".toByte() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isByte"
            }
        }

        context("toDouble") {
            test("success") {
                val result = tryValidate { "123.45".toDouble() }
                result.shouldBeSuccess()
                result.value shouldBe 123.45
            }
            test("failure") {
                val result = tryValidate { "abc".toDouble() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isDouble"
            }
        }

        context("toFloat") {
            test("success") {
                val result = tryValidate { "123.45".toFloat() }
                result.shouldBeSuccess()
                result.value shouldBe 123.45f
            }
            test("failure") {
                val result = tryValidate { "abc".toFloat() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isFloat"
            }
        }

        context("toBigDecimal") {
            test("success") {
                val result = tryValidate { "123.456789012345678901234567890".toBigDecimal() }
                result.shouldBeSuccess()
                result.value shouldBe "123.456789012345678901234567890".toBigDecimal()
            }
            test("failure") {
                val result = tryValidate { "abc".toBigDecimal() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isBigDecimal"
            }
        }

        context("toBigInteger") {
            test("success") {
                val result = tryValidate { "12345678901234567890".toBigInteger() }
                result.shouldBeSuccess()
                result.value shouldBe "12345678901234567890".toBigInteger()
            }
            test("failure") {
                val result = tryValidate { "123.45".toBigInteger() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isBigInteger"
            }
        }

        context("uppercase") {
            test("success") {
                val result = tryValidate { "HELLO".uppercase() }
                result.shouldBeSuccess()
            }
            test("success with empty string") {
                val result = tryValidate { "".uppercase() }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "Hello".uppercase() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.uppercase"
            }
        }

        context("lowercase") {
            test("success") {
                val result = tryValidate { "hello".lowercase() }
                result.shouldBeSuccess()
            }
            test("success with empty string") {
                val result = tryValidate { "".lowercase() }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "Hello".lowercase() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.lowercase"
            }
        }

        context("toInt") {
            test("success") {
                val result = tryValidate { "123".toInt() }
                result.shouldBeSuccess()
                result.value shouldBe 123
            }
            test("failure") {
                val result = tryValidate { "123a".toInt() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isInt"
            }
        }

        context("map - string bools") {
            context(_: Validation)
            fun String.stringBools() =
                when (this) {
                    "true" -> true
                    "1" -> true
                    else -> false
                }.success()

            test("success with true") {
                val result = tryValidate { "true".stringBools() }
                result.shouldBeSuccess()
                result.value shouldBe true
            }
            test("success with value 1") {
                val result = tryValidate { "1".stringBools() }
                result.shouldBeSuccess()
                result.value shouldBe true
            }
            test("success with false") {
                val result = tryValidate { "false".stringBools() }
                result.shouldBeSuccess()
                result.value shouldBe false
            }
            test("success with value 0") {
                val result = tryValidate { "0".stringBools() }
                result.shouldBeSuccess()
                result.value shouldBe false
            }
        }

        context("trim") {
            test("success when trimming leading whitespace") {
                val result = tryValidate { "  hello".trim().success() }
                result.shouldBeSuccess()
                result.value shouldBe "hello"
            }

            test("success when trimming trailing whitespace") {
                val result = tryValidate { "hello  ".trim().success() }
                result.shouldBeSuccess()
                result.value shouldBe "hello"
            }

            test("success when trimming both sides") {
                val result = tryValidate { "  hello  ".trim().success() }
                result.shouldBeSuccess()
                result.value shouldBe "hello"
            }

            test("success with no whitespace to trim") {
                val result = tryValidate { "hello".trim().success() }
                result.shouldBeSuccess()
                result.value shouldBe "hello"
            }

            test("success with empty string") {
                val result = tryValidate { "".trim().success() }
                result.shouldBeSuccess()
                result.value shouldBe ""
            }

            test("success with only whitespace") {
                val result = tryValidate { "   ".trim().success() }
                result.shouldBeSuccess()
                result.value shouldBe ""
            }
        }

        context("toUpperCase") {
            test("success when converting lowercase to uppercase") {
                val result = tryValidate { "hello".toUppercase().success() }
                result.shouldBeSuccess()
                result.value shouldBe "HELLO"
            }

            test("success when converting mixed case to uppercase") {
                val result = tryValidate { "HeLLo".toUppercase().success() }
                result.shouldBeSuccess()
                result.value shouldBe "HELLO"
            }

            test("success with already uppercase") {
                val result = tryValidate { "HELLO".toUppercase().success() }
                result.shouldBeSuccess()
                result.value shouldBe "HELLO"
            }

            test("success with empty string") {
                val result = tryValidate { "".toUppercase().success() }
                result.shouldBeSuccess()
                result.value shouldBe ""
            }

            test("success with numbers and symbols") {
                val result = tryValidate { "hello123!@#".toUppercase().success() }
                result.shouldBeSuccess()
                result.value shouldBe "HELLO123!@#"
            }
        }

        context("toLowerCase") {
            test("success when converting uppercase to lowercase") {
                val result = tryValidate { "HELLO".toLowercase().success() }
                result.shouldBeSuccess()
                result.value shouldBe "hello"
            }

            test("success when converting mixed case to lowercase") {
                val result = tryValidate { "HeLLo".toLowercase().success() }
                result.shouldBeSuccess()
                result.value shouldBe "hello"
            }

            test("success with already lowercase") {
                val result = tryValidate { "hello".toLowercase().success() }
                result.shouldBeSuccess()
                result.value shouldBe "hello"
            }

            test("success with empty string") {
                val result = tryValidate { "".toLowercase().success() }
                result.shouldBeSuccess()
                result.value shouldBe ""
            }

            test("success with numbers and symbols") {
                val result = tryValidate { "HELLO123!@#".toLowercase().success() }
                result.shouldBeSuccess()
                result.value shouldBe "hello123!@#"
            }
        }

        context("isEnum with Type") {
            test("success with ACTIVE") {
                val result = tryValidate { "ACTIVE".isEnum<Status>() }
                result.shouldBeSuccess()
            }
            test("success with INACTIVE") {
                val result = tryValidate { "INACTIVE".isEnum<Status>() }
                result.shouldBeSuccess()
            }
            test("success with PENDING") {
                val result = tryValidate { "PENDING".isEnum<Status>() }
                result.shouldBeSuccess()
            }
            test("failure with invalid value") {
                val result = tryValidate { "INVALID".isEnum<Status>() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isEnum"
            }
            test("failure with lowercase") {
                val result = tryValidate { "active".isEnum<Status>() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isEnum"
            }
        }

        context("isEnum with KClass") {
            test("success with ACTIVE") {
                val result = tryValidate { "ACTIVE".isEnum(Status::class) }
                result.shouldBeSuccess()
            }
            test("success with INACTIVE") {
                val result = tryValidate { "INACTIVE".isEnum(Status::class) }
                result.shouldBeSuccess()
            }
            test("success with PENDING") {
                val result = tryValidate { "PENDING".isEnum(Status::class) }
                result.shouldBeSuccess()
            }
            test("failure with invalid value") {
                val result = tryValidate { "INVALID".isEnum(Status::class) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isEnum"
            }
            test("failure with lowercase") {
                val result = tryValidate { "active".isEnum(Status::class) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isEnum"
            }
        }

        context("toEnum") {
            test("success with ACTIVE") {
                val result = tryValidate { "ACTIVE".toEnum<Status>() }
                result.shouldBeSuccess()
                result.value shouldBe Status.ACTIVE
            }
            test("success with INACTIVE") {
                val result = tryValidate { "INACTIVE".toEnum<Status>() }
                result.shouldBeSuccess()
                result.value shouldBe Status.INACTIVE
            }
            test("success with PENDING") {
                val result = tryValidate { "PENDING".toEnum<Status>() }
                result.shouldBeSuccess()
                result.value shouldBe Status.PENDING
            }
            test("failure with invalid value") {
                val result = tryValidate { "INVALID".toEnum<Status>() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isEnum"
            }
            test("failure with lowercase") {
                val result = tryValidate { "active".toEnum<Status>() }
                result.shouldBeFailure()
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
