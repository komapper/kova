package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import java.util.Locale

class StringValidatorTest :
    FunSpec({

        beforeSpec {
            Locale.setDefault(Locale.US)
        }

        context("ensureNotBlank with message") {
            test("success") {
                val result = tryValidate { "ab".ensureNotBlank { text("Must not be ensureBlank") } }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "".ensureNotBlank { text("Must not be ensureBlank") } }
                result.shouldBeFailure()
                result.messages.single().text shouldBe "Must not be ensureBlank"
            }
        }

        context("ensureInt") {
            test("success") {
                val result = tryValidate { "123".ensureInt() }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "123a".ensureInt() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.int"
            }
        }

        context("ensureLong") {
            test("success") {
                val result = tryValidate { "9223372036854775807".ensureLong() }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "123.45".ensureLong() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.long"
            }
        }

        context("ensureShort") {
            test("success") {
                val result = tryValidate { "32767".ensureShort() }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "99999".ensureShort() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.short"
            }
        }

        context("ensureByte") {
            test("success") {
                val result = tryValidate { "127".ensureByte() }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "256".ensureByte() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.byte"
            }
        }

        context("ensureDouble") {
            test("success") {
                val result = tryValidate { "123.45".ensureDouble() }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "abc".ensureDouble() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.double"
            }
        }

        context("ensureFloat") {
            test("success") {
                val result = tryValidate { "123.45".ensureFloat() }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "abc".ensureFloat() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.float"
            }
        }

        context("ensureBigDecimal") {
            test("success") {
                val result = tryValidate { "123.456789012345678901234567890".ensureBigDecimal() }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "abc".ensureBigDecimal() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.bigDecimal"
            }
        }

        context("ensureBigInteger") {
            test("success") {
                val result = tryValidate { "12345678901234567890".ensureBigInteger() }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "123.45".ensureBigInteger() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.bigInteger"
            }
        }

        context("ensureBoolean") {
            test("success with true") {
                val result = tryValidate { "true".ensureBoolean() }
                result.shouldBeSuccess()
            }
            test("success with false") {
                val result = tryValidate { "false".ensureBoolean() }
                result.shouldBeSuccess()
            }
            test("failure with case sensitive mismatch") {
                val result = tryValidate { "TRUE".ensureBoolean() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.boolean"
            }
            test("failure") {
                val result = tryValidate { "yes".ensureBoolean() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.boolean"
            }
        }

        context("parseBoolean") {
            test("success with true") {
                val result = tryValidate { parseBoolean("true") }
                result.shouldBeSuccess()
                result.value shouldBe true
            }
            test("success with false") {
                val result = tryValidate { parseBoolean("false") }
                result.shouldBeSuccess()
                result.value shouldBe false
            }
            test("failure") {
                val result = tryValidate { parseBoolean("yes") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.boolean"
            }
        }

        context("parseLong") {
            test("success") {
                val result = tryValidate { parseLong("9223372036854775807") }
                result.shouldBeSuccess()
                result.value shouldBe 9223372036854775807L
            }
            test("failure") {
                val result = tryValidate { parseLong("abc") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.long"
            }
        }

        context("parseShort") {
            test("success") {
                val result = tryValidate { parseShort("32767") }
                result.shouldBeSuccess()
                result.value shouldBe 32767.toShort()
            }
            test("failure") {
                val result = tryValidate { parseShort("99999") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.short"
            }
        }

        context("parseByte") {
            test("success") {
                val result = tryValidate { parseByte("127") }
                result.shouldBeSuccess()
                result.value shouldBe 127.toByte()
            }
            test("failure") {
                val result = tryValidate { parseByte("256") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.byte"
            }
        }

        context("parseDouble") {
            test("success") {
                val result = tryValidate { parseDouble("123.45") }
                result.shouldBeSuccess()
                result.value shouldBe 123.45
            }
            test("failure") {
                val result = tryValidate { parseDouble("abc") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.double"
            }
        }

        context("parseFloat") {
            test("success") {
                val result = tryValidate { parseFloat("123.45") }
                result.shouldBeSuccess()
                result.value shouldBe 123.45f
            }
            test("failure") {
                val result = tryValidate { parseFloat("abc") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.float"
            }
        }

        context("parseBigDecimal") {
            test("success") {
                val result = tryValidate { parseBigDecimal("123.456789012345678901234567890") }
                result.shouldBeSuccess()
                result.value shouldBe "123.456789012345678901234567890".toBigDecimal()
            }
            test("failure") {
                val result = tryValidate { parseBigDecimal("abc") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.bigDecimal"
            }
        }

        context("parseBigInteger") {
            test("success") {
                val result = tryValidate { parseBigInteger("12345678901234567890") }
                result.shouldBeSuccess()
                result.value shouldBe "12345678901234567890".toBigInteger()
            }
            test("failure") {
                val result = tryValidate { parseBigInteger("123.45") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.bigInteger"
            }
        }

        context("ensureUppercase") {
            test("success") {
                val result = tryValidate { "HELLO".ensureUppercase() }
                result.shouldBeSuccess()
            }
            test("success with ensureEmpty string") {
                val result = tryValidate { "".ensureUppercase() }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "Hello".ensureUppercase() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.uppercase"
            }
        }

        context("ensureLowercase") {
            test("success") {
                val result = tryValidate { "hello".ensureLowercase() }
                result.shouldBeSuccess()
            }
            test("success with ensureEmpty string") {
                val result = tryValidate { "".ensureLowercase() }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "Hello".ensureLowercase() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.lowercase"
            }
        }

        context("parseInt") {
            test("success") {
                val result = tryValidate { parseInt("123") }
                result.shouldBeSuccess()
                result.value shouldBe 123
            }
            test("failure") {
                val result = tryValidate { parseInt("123a") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.int"
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

        context("ensureEnum with Type") {
            test("success with ACTIVE") {
                val result = tryValidate { "ACTIVE".ensureEnum<Status>() }
                result.shouldBeSuccess()
            }
            test("success with INACTIVE") {
                val result = tryValidate { "INACTIVE".ensureEnum<Status>() }
                result.shouldBeSuccess()
            }
            test("success with PENDING") {
                val result = tryValidate { "PENDING".ensureEnum<Status>() }
                result.shouldBeSuccess()
            }
            test("failure with invalid value") {
                val result = tryValidate { "INVALID".ensureEnum<Status>() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.enum"
            }
            test("failure with ensureLowercase") {
                val result = tryValidate { "active".ensureEnum<Status>() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.enum"
            }
        }

        context("ensureEnum with KClass") {
            test("success with ACTIVE") {
                val result = tryValidate { "ACTIVE".ensureEnum(Status::class) }
                result.shouldBeSuccess()
            }
            test("success with INACTIVE") {
                val result = tryValidate { "INACTIVE".ensureEnum(Status::class) }
                result.shouldBeSuccess()
            }
            test("success with PENDING") {
                val result = tryValidate { "PENDING".ensureEnum(Status::class) }
                result.shouldBeSuccess()
            }
            test("failure with invalid value") {
                val result = tryValidate { "INVALID".ensureEnum(Status::class) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.enum"
            }
            test("failure with ensureLowercase") {
                val result = tryValidate { "active".ensureEnum(Status::class) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.enum"
            }
        }

        context("parseEnum") {
            test("success with ACTIVE") {
                val result = tryValidate { parseEnum<Status>("ACTIVE") }
                result.shouldBeSuccess()
                result.value shouldBe Status.ACTIVE
            }
            test("success with INACTIVE") {
                val result = tryValidate { parseEnum<Status>("INACTIVE") }
                result.shouldBeSuccess()
                result.value shouldBe Status.INACTIVE
            }
            test("success with PENDING") {
                val result = tryValidate { parseEnum<Status>("PENDING") }
                result.shouldBeSuccess()
                result.value shouldBe Status.PENDING
            }
            test("failure with invalid value") {
                val result = tryValidate { parseEnum<Status>("INVALID") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.enum"
            }
            test("failure with ensureLowercase") {
                val result = tryValidate { parseEnum<Status>("active") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.enum"
            }
        }
    }) {
    enum class Status {
        ACTIVE,
        INACTIVE,
        PENDING,
    }
}
