package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class CharSequenceValidatorTest :
    FunSpec({

        context("plus") {
            val validator = Kova.string().max(2) + Kova.string().max(3)

            test("success") {
                val result = validator.tryValidate("1")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "1"
            }

            test("failure") {
                val result = validator.tryValidate("1234")
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 2
                result.messages[0].constraintId shouldBe "kova.charSequence.max"
                result.messages[1].constraintId shouldBe "kova.charSequence.max"
            }
        }

        context("and") {
            val validator = Kova.string().max(2) and Kova.string().max(3)

            test("success") {
                val result = validator.tryValidate("1")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "1"
            }

            test("failure") {
                val result = validator.tryValidate("1234")
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 2
                result.messages[0].constraintId shouldBe "kova.charSequence.max"
                result.messages[1].constraintId shouldBe "kova.charSequence.max"
            }
        }

        context("chain") {
            val length = Kova.string().length(3)
            val validator =
                Kova
                    .string()
                    .trim()
                    .chain(length)
                    .toUppercase()

            test("success") {
                val result = validator.tryValidate(" abc ")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "ABC"
            }

            test("failure") {
                val result = validator.tryValidate(" a ")
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.charSequence.length"
            }
        }

        context("min") {
            val min = Kova.string().min(3)

            test("success") {
                val result = min.tryValidate("abc")
                result.isSuccess().mustBeTrue()
            }

            test("failure") {
                val result = min.tryValidate("ab")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.charSequence.min"
            }
        }

        context("max") {
            val max = Kova.string().max(1)

            test("success") {
                val result = max.tryValidate("a")
                result.isSuccess().mustBeTrue()
            }

            test("failure") {
                val result = max.tryValidate("ab")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.charSequence.max"
            }
        }

        context("length") {
            val length = Kova.string().length(1)

            test("success") {
                val result = length.tryValidate("a")
                result.isSuccess().mustBeTrue()
            }

            test("failure") {
                val result = length.tryValidate("ab")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.charSequence.length"
            }
        }

        context("notBlank") {
            val notBlank = Kova.string().notBlank()

            test("success") {
                val result = notBlank.tryValidate("ab")
                result.isSuccess().mustBeTrue()
            }
            test("failure") {
                val result = notBlank.tryValidate("")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.charSequence.notBlank"
            }
        }

        context("blank") {
            val blank = Kova.string().blank()

            test("success with empty string") {
                val result = blank.tryValidate("")
                result.isSuccess().mustBeTrue()
            }
            test("success with whitespace only") {
                val result = blank.tryValidate("   ")
                result.isSuccess().mustBeTrue()
            }
            test("failure") {
                val result = blank.tryValidate("ab")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.charSequence.blank"
            }
        }

        context("notEmpty") {
            val notEmpty = Kova.string().notEmpty()

            test("success") {
                val result = notEmpty.tryValidate("ab")
                result.isSuccess().mustBeTrue()
            }

            test("failure") {
                val result = notEmpty.tryValidate("")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.charSequence.notEmpty"
            }
        }

        context("empty") {
            val empty = Kova.string().empty()

            test("success") {
                val result = empty.tryValidate("")
                result.isSuccess().mustBeTrue()
            }
            test("failure with content") {
                val result = empty.tryValidate("ab")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.charSequence.empty"
            }
            test("failure with whitespace only") {
                val result = empty.tryValidate("   ")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.charSequence.empty"
            }
        }

        context("startsWith") {
            val startsWith = Kova.string().startsWith("ab")

            test("success") {
                val result = startsWith.tryValidate("abcde")
                result.isSuccess().mustBeTrue()
            }
            test("failure") {
                val result = startsWith.tryValidate("cde")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.charSequence.startsWith"
            }
        }

        context("notStartsWith") {
            val notStartsWith = Kova.string().notStartsWith("ab")

            test("success") {
                val result = notStartsWith.tryValidate("cde")
                result.isSuccess().mustBeTrue()
            }
            test("failure") {
                val result = notStartsWith.tryValidate("abcde")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.charSequence.notStartsWith"
            }
        }

        context("endsWith") {
            val endsWith = Kova.string().endsWith("de")

            test("success") {
                val result = endsWith.tryValidate("abcde")
                result.isSuccess().mustBeTrue()
            }
            test("failure") {
                val result = endsWith.tryValidate("ab")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.charSequence.endsWith"
            }
        }

        context("notEndsWith") {
            val notEndsWith = Kova.string().notEndsWith("de")

            test("success") {
                val result = notEndsWith.tryValidate("ab")
                result.isSuccess().mustBeTrue()
            }
            test("failure") {
                val result = notEndsWith.tryValidate("abcde")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.charSequence.notEndsWith"
            }
        }

        context("contains") {
            val contains = Kova.string().contains("cd")

            test("success") {
                val result = contains.tryValidate("abcde")
                result.isSuccess().mustBeTrue()
            }
            test("failure") {
                val result = contains.tryValidate("fg")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.charSequence.contains"
            }
        }

        context("notContains") {
            val notContains = Kova.string().notContains("cd")

            test("success") {
                val result = notContains.tryValidate("fg")
                result.isSuccess().mustBeTrue()
            }
            test("failure") {
                val result = notContains.tryValidate("abcde")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.charSequence.notContains"
            }
        }

        context("matches") {
            val emailPattern = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$")
            val matches = Kova.string().matches(emailPattern)

            test("success") {
                val result = matches.tryValidate("user@example.com")
                result.isSuccess().mustBeTrue()
            }
            test("failure") {
                val result = matches.tryValidate("invalid-email")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.charSequence.matches"
            }
        }

        context("notMatches") {
            val digitPattern = Regex("^\\d+\$")
            val notMatches = Kova.string().notMatches(digitPattern)

            test("success") {
                val result = notMatches.tryValidate("hello")
                result.isSuccess().mustBeTrue()
            }
            test("failure") {
                val result = notMatches.tryValidate("12345")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.charSequence.notMatches"
            }
        }

        context("nullableString") {
            val max1 =
                Kova
                    .nullable<String>()
                    .toNonNullable()
                    .then(Kova.string().max(1))

            test("success") {
                val result = max1.tryValidate("1")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "1"
            }
            test("failure with null value") {
                val result = max1.tryValidate(null)
                result.isFailure().mustBeTrue()
            }
            test("failure") {
                val result = max1.tryValidate("12")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.charSequence.max"
            }
        }

        context("trim with constraints") {
            val trimMin3 = Kova.string().trim().min(3)

            test("success when trimmed value meets constraint") {
                val result = trimMin3.tryValidate("  hello  ")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "hello"
            }

            test("failure when trimmed value violates constraint") {
                val result = trimMin3.tryValidate("  hi  ")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.charSequence.min"
            }

            test("failure when whitespace only becomes empty after trim") {
                val result = trimMin3.tryValidate("   ")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.charSequence.min"
            }
        }

        context("toUpperCase with constraints") {
            val toUpperCaseMin3 = Kova.string().toUppercase().min(3)

            test("success when transformed value meets constraint") {
                val result = toUpperCaseMin3.tryValidate("hello")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "HELLO"
            }

            test("failure when transformed value violates constraint") {
                val result = toUpperCaseMin3.tryValidate("hi")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.charSequence.min"
            }

            test("success when combining toUpperCase with startsWith") {
                val toUpperCaseStartsWithH = Kova.string().toUppercase().startsWith("H")
                val result = toUpperCaseStartsWithH.tryValidate("hello")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "HELLO"
            }
        }

        context("toLowerCase with constraints") {
            val toLowerCaseMin3 = Kova.string().toLowercase().min(3)

            test("success when transformed value meets constraint") {
                val result = toLowerCaseMin3.tryValidate("HELLO")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "hello"
            }

            test("failure when transformed value violates constraint") {
                val result = toLowerCaseMin3.tryValidate("HI")
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.charSequence.min"
            }

            test("success when combining toLowerCase with startsWith") {
                val toLowerCaseStartsWithH = Kova.string().toLowercase().startsWith("h")
                val result = toLowerCaseStartsWithH.tryValidate("HELLO")
                result.isSuccess().mustBeTrue()
                result.value shouldBe "hello"
            }
        }
    })
