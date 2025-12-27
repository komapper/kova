package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec

class CharSequenceValidatorTest :
    FunSpec({

        context("plus") {
            fun Validation.validate(string: String) {
                max(string, 2)
                max(string, 3)
            }

            test("success") {
                val result = tryValidate { validate("1") }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { validate("1234") }
                result.shouldBeFailure()
                result.messages.size shouldBe 2
                result.messages[0].constraintId shouldBe "kova.charSequence.max"
                result.messages[1].constraintId shouldBe "kova.charSequence.max"
            }
        }

        context("and") {
            fun Validation.validate(string: String) {
                max(string, 2)
                max(string, 3)
            }

            test("success") {
                val result = tryValidate { validate("1") }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { validate("1234") }
                result.shouldBeFailure()
                result.messages.size shouldBe 2
                result.messages[0].constraintId shouldBe "kova.charSequence.max"
                result.messages[1].constraintId shouldBe "kova.charSequence.max"
            }
        }

        context("then") {
            fun Validation.validate(string: String) =
                string.trim().let { trimmed ->
                    length(trimmed, 3)
                    trimmed.toUppercase()
                }

            test("success") {
                val result = tryValidate { validate(" abc ") }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { validate(" a ") }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.charSequence.length"
            }
        }

        context("min") {
            test("success") {
                val result = tryValidate { this.min("abc", 3) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { this.min("ab", 3) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.min"
            }
        }

        context("max") {
            test("success") {
                val result = tryValidate { this.max("a", 1) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { this.max("ab", 1) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.max"
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
                val result = tryValidate { this.notBlank("ab") }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { this.notBlank("") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.notBlank"
            }
        }

        context("blank") {
            test("success with empty string") {
                val result = tryValidate { this.blank("") }
                result.shouldBeSuccess()
            }
            test("success with whitespace only") {
                val result = tryValidate { this.blank("   ") }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { this.blank("ab") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.blank"
            }
        }

        context("notEmpty") {
            test("success") {
                val result = tryValidate { this.notEmpty("ab") }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { this.notEmpty("") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.notEmpty"
            }
        }

        context("empty") {
            test("success") {
                val result = tryValidate { this.empty("") }
                result.shouldBeSuccess()
            }
            test("failure with content") {
                val result = tryValidate { this.empty("ab") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.empty"
            }
            test("failure with whitespace only") {
                val result = tryValidate { this.empty("   ") }
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
            fun Validation.max1(string: String?) = max(toNonNullable(string), 1)

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
                result.messages.single().constraintId shouldBe "kova.charSequence.max"
            }
        }

        context("trim with constraints") {
            test("success when trimmed value meets constraint") {
                val result = tryValidate { this.min("  hello  ".trim(), 3) }
                result.shouldBeSuccess()
            }

            test("failure when trimmed value violates constraint") {
                val result = tryValidate { this.min("  hi  ".trim(), 3) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.min"
            }

            test("failure when whitespace only becomes empty after trim") {
                val result = tryValidate { this.min("   ".trim(), 3) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.min"
            }
        }

        context("toUpperCase with constraints") {
            test("success when transformed value meets constraint") {
                val result = tryValidate { this.min("hello".toUppercase(), 3) }
                result.shouldBeSuccess()
            }

            test("failure when transformed value violates constraint") {
                val result = tryValidate { this.min("hi".toUppercase(), 3) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.min"
            }

            test("success when combining toUpperCase with startsWith") {
                val result = tryValidate { startsWith("hello".toUppercase(), "H") }
                result.shouldBeSuccess()
            }
        }

        context("toLowerCase with constraints") {
            test("success when transformed value meets constraint") {
                val result = tryValidate { this.min("HELLO".toLowercase(), 3) }
                result.shouldBeSuccess()
            }

            test("failure when transformed value violates constraint") {
                val result = tryValidate { this.min("HI".toLowercase(), 3) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.min"
            }

            test("success when combining toLowerCase with startsWith") {
                val result = tryValidate { startsWith("HELLO".toLowercase(), "h") }
                result.shouldBeSuccess()
            }
        }
    })
