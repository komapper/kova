package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec

class CharSequenceValidatorTest :
    FunSpec({

        context("plus") {
            context(_: Validation, _: Accumulate)
            fun String.validate() {
                max(2)
                max(3)
            }

            test("success") {
                val result = tryValidate { "1".validate() }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { "1234".validate() }
                result.shouldBeFailure()
                result.messages.size shouldBe 2
                result.messages[0].constraintId shouldBe "kova.charSequence.max"
                result.messages[1].constraintId shouldBe "kova.charSequence.max"
            }
        }

        context("and") {
            context(_: Validation, _: Accumulate)
            fun String.validate() {
                max(2)
                max(3)
            }

            test("success") {
                val result = tryValidate { "1".validate() }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { "1234".validate() }
                result.shouldBeFailure()
                result.messages.size shouldBe 2
                result.messages[0].constraintId shouldBe "kova.charSequence.max"
                result.messages[1].constraintId shouldBe "kova.charSequence.max"
            }
        }

        context("then") {
            context(_: Validation, _: Accumulate)
            fun String.validate() =
                trim().let { trimmed ->
                    trimmed.length(3)
                    trimmed.toUppercase()
                }

            test("success") {
                val result = tryValidate { " abc ".validate() }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { " a ".validate() }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.charSequence.length"
            }
        }

        context("min") {
            test("success") {
                val result = tryValidate { "abc".min(3) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { "ab".min(3) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.min"
            }
        }

        context("max") {
            test("success") {
                val result = tryValidate { "a".max(1) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { "ab".max(1) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.max"
            }
        }

        context("length") {
            test("success") {
                val result = tryValidate { "a".length(1) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { "ab".length(1) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.length"
            }
        }

        context("notBlank") {
            test("success") {
                val result = tryValidate { "ab".notBlank() }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "".notBlank() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.notBlank"
            }
        }

        context("blank") {
            test("success with empty string") {
                val result = tryValidate { "".blank() }
                result.shouldBeSuccess()
            }
            test("success with whitespace only") {
                val result = tryValidate { "   ".blank() }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "ab".blank() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.blank"
            }
        }

        context("notEmpty") {
            test("success") {
                val result = tryValidate { "ab".notEmpty() }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { "".notEmpty() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.notEmpty"
            }
        }

        context("empty") {
            test("success") {
                val result = tryValidate { "".empty() }
                result.shouldBeSuccess()
            }
            test("failure with content") {
                val result = tryValidate { "ab".empty() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.empty"
            }
            test("failure with whitespace only") {
                val result = tryValidate { "   ".empty() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.empty"
            }
        }

        context("startsWith") {
            test("success") {
                val result = tryValidate { "abcde".startsWith("ab") }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "cde".startsWith("ab") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.startsWith"
            }
        }

        context("notStartsWith") {
            test("success") {
                val result = tryValidate { "cde".notStartsWith("ab") }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "abcde".notStartsWith("ab") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.notStartsWith"
            }
        }

        context("endsWith") {
            test("success") {
                val result = tryValidate { "abcde".endsWith("de") }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "ab".endsWith("de") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.endsWith"
            }
        }

        context("notEndsWith") {
            test("success") {
                val result = tryValidate { "ab".notEndsWith("de") }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "abcde".notEndsWith("de") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.notEndsWith"
            }
        }

        context("contains") {
            test("success") {
                val result = tryValidate { "abcde".contains("cd") }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "fg".contains("cd") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.contains"
            }
        }

        context("notContains") {
            test("success") {
                val result = tryValidate { "fg".notContains("cd") }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "abcde".notContains("cd") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.notContains"
            }
        }

        context("matches") {
            val emailPattern = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$")

            test("success") {
                val result = tryValidate { "user@example.com".matches(emailPattern) }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "invalid-email".matches(emailPattern) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.matches"
            }
        }

        context("notMatches") {
            val digitPattern = Regex("^\\d+\$")

            test("success") {
                val result = tryValidate { "hello".notMatches(digitPattern) }
                result.shouldBeSuccess()
            }
            test("failure") {
                val result = tryValidate { "12345".notMatches(digitPattern) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.notMatches"
            }
        }

        context("nullableString") {
            context(_: Validation, _: Accumulate)
            fun String?.max1() = toNonNullable().max(1)

            test("success") {
                val result = tryValidate { "1".max1() }
                result.shouldBeSuccess()
            }
            test("failure with null value") {
                val result = tryValidate { null.max1() }
                result.shouldBeFailure()
            }
            test("failure") {
                val result = tryValidate { "12".max1() }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.max"
            }
        }

        context("trim with constraints") {
            test("success when trimmed value meets constraint") {
                val result = tryValidate { "  hello  ".trim().min(3) }
                result.shouldBeSuccess()
            }

            test("failure when trimmed value violates constraint") {
                val result = tryValidate { "  hi  ".trim().min(3) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.min"
            }

            test("failure when whitespace only becomes empty after trim") {
                val result = tryValidate { "   ".trim().min(3) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.min"
            }
        }

        context("toUpperCase with constraints") {
            test("success when transformed value meets constraint") {
                val result = tryValidate { "hello".toUppercase().min(3) }
                result.shouldBeSuccess()
            }

            test("failure when transformed value violates constraint") {
                val result = tryValidate { "hi".toUppercase().min(3) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.min"
            }

            test("success when combining toUpperCase with startsWith") {
                val result = tryValidate { "hello".toUppercase().startsWith("H") }
                result.shouldBeSuccess()
            }
        }

        context("toLowerCase with constraints") {
            test("success when transformed value meets constraint") {
                val result = tryValidate { "HELLO".toLowercase().min(3) }
                result.shouldBeSuccess()
            }

            test("failure when transformed value violates constraint") {
                val result = tryValidate { "HI".toLowercase().min(3) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.min"
            }

            test("success when combining toLowerCase with startsWith") {
                val result = tryValidate { "HELLO".toLowercase().startsWith("h") }
                result.shouldBeSuccess()
            }
        }
    })
