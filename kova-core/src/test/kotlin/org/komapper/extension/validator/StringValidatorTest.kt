package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
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
                result.messages.single().input shouldBe "123a"
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
                result.messages.single().input shouldBe "123.45"
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
                result.messages.single().input shouldBe "99999"
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
                result.messages.single().input shouldBe "256"
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
                result.messages.single().input shouldBe "abc"
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
                result.messages.single().input shouldBe "abc"
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
                result.messages.single().input shouldBe "abc"
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
                result.messages.single().input shouldBe "123.45"
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
                result.messages.single().input shouldBe "yes"
            }
        }

        context("transformToBoolean") {
            test("success with true") {
                val result = tryValidate { "true".transformToBoolean() }
                result.shouldBeSuccess()
                result.value shouldBe true
            }
            test("success with false") {
                val result = tryValidate { "false".transformToBoolean() }
                result.shouldBeSuccess()
                result.value shouldBe false
            }
            test("failure") {
                val result = tryValidate { "yes".transformToBoolean() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.boolean"
                result.messages.single().input shouldBe "yes"
            }
            test("failure stops subsequent validation") {
                val result = tryValidate { "yes".transformToBoolean().ensureNull() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.boolean"
                result.messages.single().input shouldBe "yes"
            }
        }

        context("transformToLong") {
            test("success") {
                val result = tryValidate { "9223372036854775807".transformToLong() }
                result.shouldBeSuccess()
                result.value shouldBe 9223372036854775807L
            }
            test("failure") {
                val result = tryValidate { "abc".transformToLong() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.long"
                result.messages.single().input shouldBe "abc"
            }
            test("failure stops subsequent validation") {
                val result = tryValidate { "abc".transformToLong().ensureNull() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.long"
                result.messages.single().input shouldBe "abc"
            }
        }

        context("transformToShort") {
            test("success") {
                val result = tryValidate { "32767".transformToShort() }
                result.shouldBeSuccess()
                result.value shouldBe 32767.toShort()
            }
            test("failure") {
                val result = tryValidate { "99999".transformToShort() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.short"
                result.messages.single().input shouldBe "99999"
            }
            test("failure stops subsequent validation") {
                val result = tryValidate { "99999".transformToShort().ensureNull() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.short"
                result.messages.single().input shouldBe "99999"
            }
        }

        context("transformToByte") {
            test("success") {
                val result = tryValidate { "127".transformToByte() }
                result.shouldBeSuccess()
                result.value shouldBe 127.toByte()
            }
            test("failure") {
                val result = tryValidate { "256".transformToByte() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.byte"
                result.messages.single().input shouldBe "256"
            }
            test("failure stops subsequent validation") {
                val result = tryValidate { "256".transformToByte().ensureNull() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.byte"
                result.messages.single().input shouldBe "256"
            }
        }

        context("transformToDouble") {
            test("success") {
                val result = tryValidate { "123.45".transformToDouble() }
                result.shouldBeSuccess()
                result.value shouldBe 123.45
            }
            test("failure") {
                val result = tryValidate { "abc".transformToDouble() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.double"
                result.messages.single().input shouldBe "abc"
            }
            test("failure stops subsequent validation") {
                val result = tryValidate { "abc".transformToDouble().ensureNull() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.double"
                result.messages.single().input shouldBe "abc"
            }
        }

        context("transformToFloat") {
            test("success") {
                val result = tryValidate { "123.45".transformToFloat() }
                result.shouldBeSuccess()
                result.value shouldBe 123.45f
            }
            test("failure") {
                val result = tryValidate { "abc".transformToFloat() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.float"
                result.messages.single().input shouldBe "abc"
            }
            test("failure stops subsequent validation") {
                val result = tryValidate { "abc".transformToFloat().ensureNull() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.float"
                result.messages.single().input shouldBe "abc"
            }
        }

        context("transformToBigDecimal") {
            test("success") {
                val result = tryValidate { "123.456789012345678901234567890".transformToBigDecimal() }
                result.shouldBeSuccess()
                result.value shouldBe "123.456789012345678901234567890".toBigDecimal()
            }
            test("failure") {
                val result = tryValidate { "abc".transformToBigDecimal() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.bigDecimal"
                result.messages.single().input shouldBe "abc"
            }
            test("failure stops subsequent validation") {
                val result = tryValidate { "abc".transformToBigDecimal().ensureNull() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.bigDecimal"
                result.messages.single().input shouldBe "abc"
            }
        }

        context("transformToBigInteger") {
            test("success") {
                val result = tryValidate { "12345678901234567890".transformToBigInteger() }
                result.shouldBeSuccess()
                result.value shouldBe "12345678901234567890".toBigInteger()
            }
            test("failure") {
                val result = tryValidate { "123.45".transformToBigInteger() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.bigInteger"
                result.messages.single().input shouldBe "123.45"
            }
            test("failure stops subsequent validation") {
                val result = tryValidate { "123.45".transformToBigInteger().ensureNull() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.bigInteger"
                result.messages.single().input shouldBe "123.45"
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
                        "123".transformToInt()
                    }
                result.shouldBeSuccess()
                result.value shouldBe 123
            }
            test("failure") {
                val result = tryValidate { "123a".transformToInt() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.int"
                result.messages.single().input shouldBe "123a"
            }
            test("failure stops subsequent validation") {
                val result = tryValidate { "123a".transformToInt().ensureNull() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.int"
                result.messages.single().input shouldBe "123a"
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
            test("failure then other failure") {
                val result =
                    tryValidate {
                        "active".ensureEnum<Status>()
                        "".ensureNotEmpty()
                    }
                result.shouldBeFailure()
                result.messages.size shouldBe 2
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
            test("failure then other failure") {
                val result =
                    tryValidate {
                        "active".ensureEnum(Status::class)
                        "".ensureNotEmpty()
                    }
                result.shouldBeFailure()
                result.messages.size shouldBe 2
            }
        }

        context("transformToEnum") {
            test("success with ACTIVE") {
                val result = tryValidate { "ACTIVE".transformToEnum<Status>() }
                result.shouldBeSuccess()
                result.value shouldBe Status.ACTIVE
            }
            test("success with INACTIVE") {
                val result = tryValidate { "INACTIVE".transformToEnum<Status>() }
                result.shouldBeSuccess()
                result.value shouldBe Status.INACTIVE
            }
            test("success with PENDING") {
                val result = tryValidate { "PENDING".transformToEnum<Status>() }
                result.shouldBeSuccess()
                result.value shouldBe Status.PENDING
            }
            test("failure with invalid value") {
                val result = tryValidate { "INVALID".transformToEnum<Status>() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.enum"
                result.messages.single().input shouldBe "INVALID"
            }
            test("failure with ensureLowercase") {
                val result = tryValidate { "active".transformToEnum<Status>() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.enum"
                result.messages.single().input shouldBe "active"
            }
            test("failure stops subsequent validation") {
                val result = tryValidate { "active".transformToEnum<Status>().ensureNull() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.enum"
                result.messages.single().input shouldBe "active"
            }
        }

        context("transformToDate") {
            test("success with ISO format") {
                val result = tryValidate { "2025-01-17".transformToDate() }
                result.shouldBeSuccess()
                result.value shouldBe LocalDate.of(2025, 1, 17)
            }
            test("success with custom format") {
                val result =
                    tryValidate {
                        "17/01/2025".transformToDate(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    }
                result.shouldBeSuccess()
                result.value shouldBe LocalDate.of(2025, 1, 17)
            }
            test("failure") {
                val result = tryValidate { "invalid".transformToDate() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.localDate"
                result.messages.single().input shouldBe "invalid"
            }
            test("failure stops subsequent validation") {
                val result = tryValidate { "invalid".transformToDate().ensureNull() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.localDate"
                result.messages.single().input shouldBe "invalid"
            }
        }

        context("transformToDateTime") {
            test("success with ISO format") {
                val result = tryValidate { "2025-01-17T10:30:00".transformToDateTime() }
                result.shouldBeSuccess()
                result.value shouldBe LocalDateTime.of(2025, 1, 17, 10, 30, 0)
            }
            test("success with custom format") {
                val result =
                    tryValidate {
                        "17/01/2025 10:30".transformToDateTime(
                            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
                        )
                    }
                result.shouldBeSuccess()
                result.value shouldBe LocalDateTime.of(2025, 1, 17, 10, 30)
            }
            test("failure") {
                val result = tryValidate { "invalid".transformToDateTime() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.localDateTime"
                result.messages.single().input shouldBe "invalid"
            }
            test("failure stops subsequent validation") {
                val result = tryValidate { "invalid".transformToDateTime().ensureNull() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.localDateTime"
                result.messages.single().input shouldBe "invalid"
            }
        }

        context("transformToTime") {
            test("success with ISO format") {
                val result = tryValidate { "10:30:00".transformToTime() }
                result.shouldBeSuccess()
                result.value shouldBe LocalTime.of(10, 30, 0)
            }
            test("success with custom format") {
                val result =
                    tryValidate {
                        "10:30".transformToTime(DateTimeFormatter.ofPattern("HH:mm"))
                    }
                result.shouldBeSuccess()
                result.value shouldBe LocalTime.of(10, 30)
            }
            test("failure") {
                val result = tryValidate { "invalid".transformToTime() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.localTime"
                result.messages.single().input shouldBe "invalid"
            }
            test("failure stops subsequent validation") {
                val result = tryValidate { "invalid".transformToTime().ensureNull() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.localTime"
                result.messages.single().input shouldBe "invalid"
            }
        }
    }) {
    enum class Status {
        ACTIVE,
        INACTIVE,
        PENDING,
    }
}
