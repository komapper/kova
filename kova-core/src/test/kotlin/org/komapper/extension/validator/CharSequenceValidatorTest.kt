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
                    length(trimmed, 3)
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

        context("minLength") {
            test("success") {
                val result = tryValidate { minLength("abc", 3) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { minLength("ab", 3) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.minLength"
            }
        }

        context("maxLength") {
            test("success") {
                val result = tryValidate { maxLength("a", 1) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { maxLength("ab", 1) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.maxLength"
            }
        }

        context("length") {
            test("success") {
                val result = tryValidate { length("a", 1) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { length("ab", 1) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.length"
            }
        }

        context("notBlank") {
            test("success") {
                val result = tryValidate { notBlank("ab") }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { notBlank("") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.notBlank"
            }
        }

        context("blank") {
            test("success with empty string") {
                val result = tryValidate { blank("") }
                result.shouldBeSuccess()
            }
            test("success with whitespace only") {
                val result = tryValidate { blank("   ") }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { blank("ab") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.blank"
            }
        }

        context("notEmpty") {
            test("success") {
                val result = tryValidate { notEmpty("ab") }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { notEmpty("") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.notEmpty"
            }
        }

        context("empty") {
            test("success") {
                val result = tryValidate { empty("") }
                result.shouldBeSuccess()
            }
            test("failure with content") {
                val result = tryValidate { empty("ab") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.empty"
            }
            test("failure with whitespace only") {
                val result = tryValidate { empty("   ") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.empty"
            }
        }

        context("startsWith") {
            test("success") {
                val result = tryValidate { startsWith("abcde", "ab") }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { startsWith("cde", "ab") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.startsWith"
            }
        }

        context("notStartsWith") {
            test("success") {
                val result = tryValidate { notStartsWith("cde", "ab") }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { notStartsWith("abcde", "ab") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.notStartsWith"
            }
        }

        context("endsWith") {
            test("success") {
                val result = tryValidate { endsWith("abcde", "de") }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { endsWith("ab", "de") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.endsWith"
            }
        }

        context("notEndsWith") {
            test("success") {
                val result = tryValidate { notEndsWith("ab", "de") }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { notEndsWith("abcde", "de") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.notEndsWith"
            }
        }

        context("contains") {
            test("success") {
                val result = tryValidate { contains("abcde", "cd") }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { contains("fg", "cd") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.contains"
            }
        }

        context("notContains") {
            test("success") {
                val result = tryValidate { notContains("fg", "cd") }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { notContains("abcde", "cd") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.notContains"
            }
        }

        context("matches") {
            val emailPattern = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$")

            test("success") {
                val result = tryValidate { matches("user@example.com", emailPattern) }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { matches("invalid-email", emailPattern) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.matches"
            }
        }

        context("notMatches") {
            val digitPattern = Regex("^\\d+\$")

            test("success") {
                val result = tryValidate { notMatches("hello", digitPattern) }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { notMatches("12345", digitPattern) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.notMatches"
            }
        }

        context("nullableString") {
            fun Validation.max1(string: String?) {
                notNull(string)
                maxLength(string, 1)
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
