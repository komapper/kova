package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import java.util.Locale

class CharSequenceValidatorTest :
    FunSpec({

        beforeSpec {
            Locale.setDefault(Locale.US)
        }

        context("with conversion") {
            fun Validation.validate(string: String) =
                string.trim().let { trimmed ->
                    ensureLength(trimmed, 3)
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

        context("ensureMinLength") {
            test("success") {
                val result = tryValidate { ensureMinLength("abc", 3) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { ensureMinLength("ab", 3) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.minLength"
            }
        }

        context("ensureMaxLength") {
            test("success") {
                val result = tryValidate { ensureMaxLength("a", 1) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { ensureMaxLength("ab", 1) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.maxLength"
            }
        }

        context("length") {
            test("success") {
                val result = tryValidate { ensureLength("a", 1) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { ensureLength("ab", 1) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.length"
            }
        }

        context("ensureNotBlank") {
            test("success") {
                val result = tryValidate { ensureNotBlank("ab") }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { ensureNotBlank("") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.notBlank"
            }
        }

        context("ensureBlank") {
            test("success with ensureEmpty string") {
                val result = tryValidate { ensureBlank("") }
                result.shouldBeSuccess()
            }
            test("success with whitespace only") {
                val result = tryValidate { ensureBlank("   ") }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { ensureBlank("ab") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.blank"
            }
        }

        context("ensureNotEmpty") {
            test("success") {
                val result = tryValidate { ensureNotEmpty("ab") }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { ensureNotEmpty("") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.notEmpty"
            }
        }

        context("empty") {
            test("success") {
                val result = tryValidate { ensureEmpty("") }
                result.shouldBeSuccess()
            }
            test("failure with content") {
                val result = tryValidate { ensureEmpty("ab") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.empty"
            }
            test("failure with whitespace only") {
                val result = tryValidate { ensureEmpty("   ") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.empty"
            }
        }

        context("ensureStartsWith") {
            test("success") {
                val result = tryValidate { ensureStartsWith("abcde", "ab") }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { ensureStartsWith("cde", "ab") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.startsWith"
            }
        }

        context("ensureNotStartsWith") {
            test("success") {
                val result = tryValidate { ensureNotStartsWith("cde", "ab") }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { ensureNotStartsWith("abcde", "ab") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.notStartsWith"
            }
        }

        context("ensureEndsWith") {
            test("success") {
                val result = tryValidate { ensureEndsWith("abcde", "de") }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { ensureEndsWith("ab", "de") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.endsWith"
            }
        }

        context("ensureNotEndsWith") {
            test("success") {
                val result = tryValidate { ensureNotEndsWith("ab", "de") }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { ensureNotEndsWith("abcde", "de") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.notEndsWith"
            }
        }

        context("ensureContains") {
            test("success") {
                val result = tryValidate { ensureContains("abcde", "cd") }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { ensureContains("fg", "cd") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.contains"
            }
        }

        context("ensureNotContains") {
            test("success") {
                val result = tryValidate { ensureNotContains("fg", "cd") }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { ensureNotContains("abcde", "cd") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.notContains"
            }
        }

        context("ensureMatches") {
            val emailPattern = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$")

            test("success") {
                val result = tryValidate { ensureMatches("user@example.com", emailPattern) }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { ensureMatches("invalid-email", emailPattern) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.matches"
            }
        }

        context("ensureNotMatches") {
            val digitPattern = Regex("^\\d+\$")

            test("success") {
                val result = tryValidate { ensureNotMatches("hello", digitPattern) }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { ensureNotMatches("12345", digitPattern) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.notMatches"
            }
        }

        context("nullableString") {
            fun Validation.max1(string: String?) {
                ensureNotNull(string)
                ensureMaxLength(string, 1)
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
                result.messages.single().constraintId shouldBe "kova.charSequence.maxLength"
            }
        }
    })
