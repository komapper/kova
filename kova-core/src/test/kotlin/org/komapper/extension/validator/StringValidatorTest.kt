package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import java.util.Locale

class StringValidatorTest :
    FunSpec({

        beforeSpec {
            Locale.setDefault(Locale.US)
        }

        context("notBlank with message") {
            test("success") {
                val result = tryValidate { notBlank("ab") { text("Must not be blank") } }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { notBlank("") { text("Must not be blank") } }
                result.shouldBeFailure()
                result.messages.single().text shouldBe "Must not be blank"
            }
        }

        context("isInt") {
            test("success") {
                val result = tryValidate { isInt("123") }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { isInt("123a") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isInt"
            }
        }

        context("isLong") {
            test("success") {
                val result = tryValidate { isLong("9223372036854775807") }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { isLong("123.45") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isLong"
            }
        }

        context("isShort") {
            test("success") {
                val result = tryValidate { isShort("32767") }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { isShort("99999") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isShort"
            }
        }

        context("isByte") {
            test("success") {
                val result = tryValidate { isByte("127") }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { isByte("256") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isByte"
            }
        }

        context("isDouble") {
            test("success") {
                val result = tryValidate { isDouble("123.45") }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { isDouble("abc") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isDouble"
            }
        }

        context("isFloat") {
            test("success") {
                val result = tryValidate { isFloat("123.45") }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { isFloat("abc") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isFloat"
            }
        }

        context("isBigDecimal") {
            test("success") {
                val result = tryValidate { isBigDecimal("123.456789012345678901234567890") }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { isBigDecimal("abc") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isBigDecimal"
            }
        }

        context("isBigInteger") {
            test("success") {
                val result = tryValidate { isBigInteger("12345678901234567890") }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { isBigInteger("123.45") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isBigInteger"
            }
        }

        context("isBoolean") {
            test("success with true") {
                val result = tryValidate { isBoolean("true") }
                result.shouldBeSuccess()
            }
            test("success with false") {
                val result = tryValidate { isBoolean("false") }
                result.shouldBeSuccess()
            }
            test("failure with case sensitive mismatch") {
                val result = tryValidate { isBoolean("TRUE") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isBoolean"
            }
            test("failure") {
                val result = tryValidate { isBoolean("yes") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isBoolean"
            }
        }

        context("toBoolean") {
            test("success with true") {
                val result = tryValidate { toBoolean("true") }
                result.shouldBeSuccess()
                result.value shouldBe true
            }
            test("success with false") {
                val result = tryValidate { toBoolean("false") }
                result.shouldBeSuccess()
                result.value shouldBe false
            }
            test("failure") {
                val result = tryValidate { toBoolean("yes") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isBoolean"
            }
        }

        context("toLong") {
            test("success") {
                val result = tryValidate { toLong("9223372036854775807") }
                result.shouldBeSuccess()
                result.value shouldBe 9223372036854775807L
            }
            test("failure") {
                val result = tryValidate { toLong("abc") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isLong"
            }
        }

        context("toShort") {
            test("success") {
                val result = tryValidate { toShort("32767") }
                result.shouldBeSuccess()
                result.value shouldBe 32767.toShort()
            }
            test("failure") {
                val result = tryValidate { toShort("99999") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isShort"
            }
        }

        context("toByte") {
            test("success") {
                val result = tryValidate { toByte("127") }
                result.shouldBeSuccess()
                result.value shouldBe 127.toByte()
            }
            test("failure") {
                val result = tryValidate { toByte("256") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isByte"
            }
        }

        context("toDouble") {
            test("success") {
                val result = tryValidate { toDouble("123.45") }
                result.shouldBeSuccess()
                result.value shouldBe 123.45
            }
            test("failure") {
                val result = tryValidate { toDouble("abc") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isDouble"
            }
        }

        context("toFloat") {
            test("success") {
                val result = tryValidate { toFloat("123.45") }
                result.shouldBeSuccess()
                result.value shouldBe 123.45f
            }
            test("failure") {
                val result = tryValidate { toFloat("abc") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isFloat"
            }
        }

        context("toBigDecimal") {
            test("success") {
                val result = tryValidate { toBigDecimal("123.456789012345678901234567890") }
                result.shouldBeSuccess()
                result.value shouldBe "123.456789012345678901234567890".toBigDecimal()
            }
            test("failure") {
                val result = tryValidate { toBigDecimal("abc") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isBigDecimal"
            }
        }

        context("toBigInteger") {
            test("success") {
                val result = tryValidate { toBigInteger("12345678901234567890") }
                result.shouldBeSuccess()
                result.value shouldBe "12345678901234567890".toBigInteger()
            }
            test("failure") {
                val result = tryValidate { toBigInteger("123.45") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isBigInteger"
            }
        }

        context("uppercase") {
            test("success") {
                val result = tryValidate { uppercase("HELLO") }
                result.shouldBeSuccess()
            }
            test("success with empty string") {
                val result = tryValidate { uppercase("") }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { uppercase("Hello") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.uppercase"
            }
        }

        context("lowercase") {
            test("success") {
                val result = tryValidate { lowercase("hello") }
                result.shouldBeSuccess()
            }
            test("success with empty string") {
                val result = tryValidate { lowercase("") }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { lowercase("Hello") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.lowercase"
            }
        }

        context("toInt") {
            test("success") {
                val result = tryValidate { toInt("123") }
                result.shouldBeSuccess()
                result.value shouldBe 123
            }
            test("failure") {
                val result = tryValidate { toInt("123a") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isInt"
            }
        }

        context("map - string bools") {
            fun String.stringBools() =
                when (this) {
                    "true" -> true
                    "1" -> true
                    else -> false
                }

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

        context("isEnum with Type") {
            test("success with ACTIVE") {
                val result = tryValidate { isEnum<Status>("ACTIVE") }
                result.shouldBeSuccess()
            }
            test("success with INACTIVE") {
                val result = tryValidate { isEnum<Status>("INACTIVE") }
                result.shouldBeSuccess()
            }
            test("success with PENDING") {
                val result = tryValidate { isEnum<Status>("PENDING") }
                result.shouldBeSuccess()
            }
            test("failure with invalid value") {
                val result = tryValidate { isEnum<Status>("INVALID") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isEnum"
            }
            test("failure with lowercase") {
                val result = tryValidate { isEnum<Status>("active") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isEnum"
            }
        }

        context("isEnum with KClass") {
            test("success with ACTIVE") {
                val result = tryValidate { isEnum("ACTIVE", Status::class) }
                result.shouldBeSuccess()
            }
            test("success with INACTIVE") {
                val result = tryValidate { isEnum("INACTIVE", Status::class) }
                result.shouldBeSuccess()
            }
            test("success with PENDING") {
                val result = tryValidate { isEnum("PENDING", Status::class) }
                result.shouldBeSuccess()
            }
            test("failure with invalid value") {
                val result = tryValidate { isEnum("INVALID", Status::class) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isEnum"
            }
            test("failure with lowercase") {
                val result = tryValidate { isEnum("active", Status::class) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isEnum"
            }
        }

        context("toEnum") {
            test("success with ACTIVE") {
                val result = tryValidate { toEnum<Status>("ACTIVE") }
                result.shouldBeSuccess()
                result.value shouldBe Status.ACTIVE
            }
            test("success with INACTIVE") {
                val result = tryValidate { toEnum<Status>("INACTIVE") }
                result.shouldBeSuccess()
                result.value shouldBe Status.INACTIVE
            }
            test("success with PENDING") {
                val result = tryValidate { toEnum<Status>("PENDING") }
                result.shouldBeSuccess()
                result.value shouldBe Status.PENDING
            }
            test("failure with invalid value") {
                val result = tryValidate { toEnum<Status>("INVALID") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isEnum"
            }
            test("failure with lowercase") {
                val result = tryValidate { toEnum<Status>("active") }
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
