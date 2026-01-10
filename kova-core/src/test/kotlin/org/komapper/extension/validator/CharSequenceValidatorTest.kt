package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import java.util.Locale

class CharSequenceValidatorTest :
    FunSpec({

        beforeSpec {
            Locale.setDefault(Locale.US)
        }

        context("with conversion") {
            context(_: Validation)
            fun validate(string: String) =
                string.trim().let { trimmed ->
                    trimmed.ensureLength(3)
                    trimmed.uppercase()
                }

            test("success") {
                val result = tryValidate { validate(" abc ") }
                result.shouldBeSuccess()
                result.value shouldBe "ABC"
            }

            test("failure") {
                val result = tryValidate { validate(" a ") }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.charSequence.length"
            }
        }

        context("ensureLengthAtLeast") {
            test("success") {
                val result = tryValidate { "abc".ensureLengthAtLeast(3) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { "ab".ensureLengthAtLeast(3) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.lengthAtLeast"
            }
        }

        context("ensureLengthAtMost") {
            test("success") {
                val result = tryValidate { "a".ensureLengthAtMost(1) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { "ab".ensureLengthAtMost(1) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.lengthAtMost"
            }
        }

        context("length") {
            test("success") {
                val result = tryValidate { "a".ensureLength(1) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { "ab".ensureLength(1) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.length"
            }
        }

        context("ensureLengthInRange") {
            test("success with closed range") {
                val result = tryValidate { "hello".ensureLengthInRange(1..10) }
                result.shouldBeSuccess()
            }

            test("success with range boundaries") {
                val result = tryValidate { "a".ensureLengthInRange(1..10) }
                result.shouldBeSuccess()
                val result2 = tryValidate { "1234567890".ensureLengthInRange(1..10) }
                result2.shouldBeSuccess()
            }

            test("success with open-ended range") {
                val result = tryValidate { "hello".ensureLengthInRange(1..<10) }
                result.shouldBeSuccess()
            }

            test("failure - too short") {
                val result = tryValidate { "".ensureLengthInRange(1..10) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.lengthInRange"
            }

            test("failure - too long") {
                val result = tryValidate { "this is too long".ensureLengthInRange(1..10) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.lengthInRange"
            }

            test("failure - open-ended range exclusive end") {
                val result = tryValidate { "12345".ensureLengthInRange(1..<5) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.lengthInRange"
            }
        }

        context("ensureNotBlank") {
            test("success") {
                val result = tryValidate { "ab".ensureNotBlank() }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "".ensureNotBlank() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.notBlank"
            }
        }

        context("ensureBlank") {
            test("success with ensureEmpty string") {
                val result = tryValidate { "".ensureBlank() }
                result.shouldBeSuccess()
            }
            test("success with whitespace only") {
                val result = tryValidate { "   ".ensureBlank() }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "ab".ensureBlank() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.blank"
            }
        }

        context("ensureNotEmpty") {
            test("success") {
                val result = tryValidate { "ab".ensureNotEmpty() }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { "".ensureNotEmpty() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.notEmpty"
            }
        }

        context("empty") {
            test("success") {
                val result = tryValidate { "".ensureEmpty() }
                result.shouldBeSuccess()
            }
            test("failure with content") {
                val result = tryValidate { "ab".ensureEmpty() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.empty"
            }
            test("failure with whitespace only") {
                val result = tryValidate { "   ".ensureEmpty() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.empty"
            }
        }

        context("ensureStartsWith") {
            test("success") {
                val result = tryValidate { "abcde".ensureStartsWith("ab") }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "cde".ensureStartsWith("ab") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.startsWith"
            }
        }

        context("ensureNotStartsWith") {
            test("success") {
                val result = tryValidate { "cde".ensureNotStartsWith("ab") }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "abcde".ensureNotStartsWith("ab") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.notStartsWith"
            }
        }

        context("ensureEndsWith") {
            test("success") {
                val result = tryValidate { "abcde".ensureEndsWith("de") }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "ab".ensureEndsWith("de") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.endsWith"
            }
        }

        context("ensureNotEndsWith") {
            test("success") {
                val result = tryValidate { "ab".ensureNotEndsWith("de") }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "abcde".ensureNotEndsWith("de") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.notEndsWith"
            }
        }

        context("ensureContains") {
            test("success") {
                val result = tryValidate { "abcde".ensureContains("cd") }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "fg".ensureContains("cd") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.contains"
            }
        }

        context("ensureNotContains") {
            test("success") {
                val result = tryValidate { "fg".ensureNotContains("cd") }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "abcde".ensureNotContains("cd") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.notContains"
            }
        }

        context("ensureMatches") {
            val emailPattern = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$")

            test("success") {
                val result = tryValidate { "user@example.com".ensureMatches(emailPattern) }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "invalid-email".ensureMatches(emailPattern) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.matches"
            }
        }

        context("ensureNotMatches") {
            val digitPattern = Regex("^\\d+\$")

            test("success") {
                val result = tryValidate { "hello".ensureNotMatches(digitPattern) }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "12345".ensureNotMatches(digitPattern) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.notMatches"
            }
        }

        context("nullableString") {
            context(_: Validation)
            fun max1(string: String?) {
                string.ensureNotNull()
                string.ensureLengthAtMost(1)
            }

            test("success") {
                val result = tryValidate { max1("1") }
                result.shouldBeSuccess()
            }
            test("failure with null value") {
                val result = tryValidate { max1(null) }
                result.shouldBeFailure()
            }
            test("failure") {
                val result = tryValidate { max1("12") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.lengthAtMost"
            }
        }
    })
