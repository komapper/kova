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

        context("transformToBoolean") {
            test("success with true") {
                val result = tryValidate { transformToBoolean("true") }
                result.shouldBeSuccess()
                result.value shouldBe true
            }
            test("success with false") {
                val result = tryValidate { transformToBoolean("false") }
                result.shouldBeSuccess()
                result.value shouldBe false
            }
            test("failure") {
                val result = tryValidate { transformToBoolean("yes") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.boolean"
            }
        }

        context("transformToLong") {
            test("success") {
                val result = tryValidate { transformToLong("9223372036854775807") }
                result.shouldBeSuccess()
                result.value shouldBe 9223372036854775807L
            }
            test("failure") {
                val result = tryValidate { transformToLong("abc") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.long"
            }
        }

        context("transformToShort") {
            test("success") {
                val result = tryValidate { transformToShort("32767") }
                result.shouldBeSuccess()
                result.value shouldBe 32767.toShort()
            }
            test("failure") {
                val result = tryValidate { transformToShort("99999") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.short"
            }
        }

        context("transformToByte") {
            test("success") {
                val result = tryValidate { transformToByte("127") }
                result.shouldBeSuccess()
                result.value shouldBe 127.toByte()
            }
            test("failure") {
                val result = tryValidate { transformToByte("256") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.byte"
            }
        }

        context("transformToDouble") {
            test("success") {
                val result = tryValidate { transformToDouble("123.45") }
                result.shouldBeSuccess()
                result.value shouldBe 123.45
            }
            test("failure") {
                val result = tryValidate { transformToDouble("abc") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.double"
            }
        }

        context("transformToFloat") {
            test("success") {
                val result = tryValidate { transformToFloat("123.45") }
                result.shouldBeSuccess()
                result.value shouldBe 123.45f
            }
            test("failure") {
                val result = tryValidate { transformToFloat("abc") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.float"
            }
        }

        context("transformToBigDecimal") {
            test("success") {
                val result = tryValidate { transformToBigDecimal("123.456789012345678901234567890") }
                result.shouldBeSuccess()
                result.value shouldBe "123.456789012345678901234567890".toBigDecimal()
            }
            test("failure") {
                val result = tryValidate { transformToBigDecimal("abc") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.bigDecimal"
            }
        }

        context("transformToBigInteger") {
            test("success") {
                val result = tryValidate { transformToBigInteger("12345678901234567890") }
                result.shouldBeSuccess()
                result.value shouldBe "12345678901234567890".toBigInteger()
            }
            test("failure") {
                val result = tryValidate { transformToBigInteger("123.45") }
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

        context("transformToInt") {
            test("success") {
                val result =
                    tryValidate {
                        transformToInt("123")
                    }
                result.shouldBeSuccess()
                result.value shouldBe 123
            }
            test("failure") {
                val result = tryValidate { transformToInt("123a") }
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

        context("transformToEnum") {
            test("success with ACTIVE") {
                val result = tryValidate { transformToEnum<Status>("ACTIVE") }
                result.shouldBeSuccess()
                result.value shouldBe Status.ACTIVE
            }
            test("success with INACTIVE") {
                val result = tryValidate { transformToEnum<Status>("INACTIVE") }
                result.shouldBeSuccess()
                result.value shouldBe Status.INACTIVE
            }
            test("success with PENDING") {
                val result = tryValidate { transformToEnum<Status>("PENDING") }
                result.shouldBeSuccess()
                result.value shouldBe Status.PENDING
            }
            test("failure with invalid value") {
                val result = tryValidate { transformToEnum<Status>("INVALID") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.enum"
            }
            test("failure with ensureLowercase") {
                val result = tryValidate { transformToEnum<Status>("active") }
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
