package org.komapper.extension.validator

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class ValidatorTest :
    FunSpec({

        context("validate") {
            val validator = Kova.int().min(1).max(10)

            test("success") {
                val result = validator.validate(5)
                result shouldBe 5
            }

            test("failure") {
                val ex =
                    shouldThrow<ValidationException> {
                        validator.validate(0)
                    }
                ex.messages.size shouldBe 1
                ex.messages[0].content shouldBe "Number 0 must be greater than or equal to 1"
            }
        }

        context("plus") {
            val a = Kova.int().max(2) as Validator<Int, Int>
            val b = Kova.int().max(3) as Validator<Int, Int>
            val c = a + b
            a.shouldBeInstanceOf<NumberValidator<Int>>()
            c.shouldBeInstanceOf<Validator<Int, Int>>()

            test("success") {
                val result = c.tryValidate(1)
                result.isSuccess().mustBeTrue()
            }
            test("failure") {
                val result = c.tryValidate(4)
                result.isFailure().mustBeTrue()
            }
        }

        context("or: 2") {
            val length2 = Kova.string().length(2)
            val length5 = Kova.string().length(5)
            val length2or5 = length2 or length5

            test("success - length(2)") {
                val result = length2or5.tryValidate("ab")
                result.isSuccess().mustBeTrue()
            }
            test("success - length(5)") {
                val result = length2or5.tryValidate("abcde")
                result.isSuccess().mustBeTrue()
            }
            test("failure - length(3)") {
                val result = length2or5.tryValidate("abc")
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].let {
                    it.id shouldBe "kova.or"
                    it.content shouldBe
                        "at least one constraint must be satisfied: [[\"abc\" must be exactly 2 characters], [\"abc\" must be exactly 5 characters]]"
                }
            }
        }

        context("or: 3") {
            val length2 = Kova.string().length(2)
            val length5 = Kova.string().length(5)
            val length7 = Kova.string().length(7)
            val length2or5or7 = length2 or length5 or length7

            test("failure - length(3)") {
                val result = length2or5or7.tryValidate("abc")
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].let {
                    it.id shouldBe "kova.or"
                    it.content shouldBe "at least one constraint must be satisfied: [[[\"abc\" must be exactly 2 characters], " +
                        "[\"abc\" must be exactly 5 characters]], [\"abc\" must be exactly 7 characters]]"
                }
            }
        }

        context("map") {
            val validator = Kova.int().min(1).map { it * 2 }
            test("success") {
                val result = validator.tryValidate(2)
                result.isSuccess().mustBeTrue()
                result.value shouldBe 4
            }
            test("failure") {
                val result = validator.tryValidate(-1)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Number -1 must be greater than or equal to 1"
            }
        }

        context("compose") {
            val validator = Kova.string().max(1).compose(Kova.int().min(3).map { it.toString() })
            test("success") {
                val result = validator.tryValidate(3)
                result.isSuccess().mustBeTrue()
                result.value shouldBe "3"
            }
            test("failure - first constraint violated") {
                val result = validator.tryValidate(2)
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "Number 2 must be greater than or equal to 3"
            }
            test("failure - second constraint violated") {
                val result = validator.tryValidate(10)
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"10\" must be at most 1 characters"
            }
        }

        context("andThen") {
            val validator =
                Kova
                    .int()
                    .min(3)
                    .map { it.toString() }
                    .then(Kova.string().max(1))
            test("success") {
                val result = validator.tryValidate(3)
                result.isSuccess().mustBeTrue()
                result.value shouldBe "3"
            }
            test("failure - first constraint violated") {
                val result = validator.tryValidate(2)
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "Number 2 must be greater than or equal to 3"
            }
            test("failure - second constraint violated") {
                val result = validator.tryValidate(10)
                result.isFailure().mustBeTrue()
                result.messages.single().content shouldBe "\"10\" must be at most 1 characters"
            }
        }

        context("logs") {
            val validator =
                Kova
                    .string()
                    .trim()
                    .min(3)
                    .max(5)

            test("success") {
                val result = validator.tryValidate(" abcde ", ValidationConfig(logging = true))
                result.isSuccess().mustBeTrue()
                result.value shouldBe "abcde"
                result.context.logs shouldBe
                    listOf(
                        "StringValidator(name=kova.string.max)",
                        "Validator.chain",
                        "Validator.map",
                        "StringValidator(name=kova.string.min)",
                        "Validator.chain",
                        "Validator.map",
                        "StringValidator(name=trim)",
                        "Validator.chain",
                        "Validator.map",
                        "StringValidator(name=empty)",
                        "Validator.chain",
                        "Validator.map",
                        "EmptyValidator",
                        "ConstraintValidator(name=kova.satisfied)",
                        "ConstraintValidator(name=kova.satisfied)",
                        "ConstraintValidator(name=kova.string.min)",
                        "ConstraintValidator(name=kova.string.max)",
                    )
            }
        }
    })
