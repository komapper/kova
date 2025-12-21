package org.komapper.extension.validator.factory

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.komapper.extension.validator.Kova
import org.komapper.extension.validator.ValidationConfig
import org.komapper.extension.validator.ValidationException
import org.komapper.extension.validator.isFailure
import org.komapper.extension.validator.isSuccess
import org.komapper.extension.validator.max
import org.komapper.extension.validator.min
import org.komapper.extension.validator.notBlank
import org.komapper.extension.validator.positive
import org.komapper.extension.validator.toInt

class TripleFactoryBuilderTest :
    FunSpec({

        context("TripleFactoryBuilder with primitive types") {
            val builder =
                TripleFactoryBuilder(
                    firstValidator = Kova.string().notBlank().max(10),
                    secondValidator = Kova.int().positive(),
                    thirdValidator = Kova.string().notBlank().max(20),
                )

            context("tryCreate") {
                test("success") {
                    val factory = builder.build("hello", 42, "world")
                    val result = factory.tryCreate()
                    result.isSuccess().mustBeTrue()
                    result.value shouldBe Triple("hello", 42, "world")
                }

                test("failure - first element invalid") {
                    val factory = builder.build("", 42, "world")
                    val result = factory.tryCreate()
                    result.isFailure().mustBeTrue()
                    result.messages.size shouldBe 1
                    result.messages[0].constraintId shouldBe "kova.charSequence.notBlank"
                    result.messages[0].root shouldBe "kotlin.Triple"
                    result.messages[0].path.fullName shouldBe "first"
                }

                test("failure - second element invalid") {
                    val factory = builder.build("hello", -5, "world")
                    val result = factory.tryCreate()
                    result.isFailure().mustBeTrue()
                    result.messages.size shouldBe 1
                    result.messages[0].constraintId shouldBe "kova.number.positive"
                    result.messages[0].root shouldBe "kotlin.Triple"
                    result.messages[0].path.fullName shouldBe "second"
                }

                test("failure - third element invalid") {
                    val factory = builder.build("hello", 42, "")
                    val result = factory.tryCreate()
                    result.isFailure().mustBeTrue()
                    result.messages.size shouldBe 1
                    result.messages[0].constraintId shouldBe "kova.charSequence.notBlank"
                    result.messages[0].root shouldBe "kotlin.Triple"
                    result.messages[0].path.fullName shouldBe "third"
                }

                test("failure - all elements invalid") {
                    val factory = builder.build("", -5, "")
                    val result = factory.tryCreate()
                    result.isFailure().mustBeTrue()
                    result.messages.size shouldBe 3
                    result.messages[0].constraintId shouldBe "kova.charSequence.notBlank"
                    result.messages[0].path.fullName shouldBe "first"
                    result.messages[1].constraintId shouldBe "kova.number.positive"
                    result.messages[1].path.fullName shouldBe "second"
                    result.messages[2].constraintId shouldBe "kova.charSequence.notBlank"
                    result.messages[2].path.fullName shouldBe "third"
                }

                test("failure - all elements invalid with failFast") {
                    val factory = builder.build("", -5, "")
                    val result = factory.tryCreate(config = ValidationConfig(failFast = true))
                    result.isFailure().mustBeTrue()
                    result.messages.size shouldBe 1
                    result.messages[0].constraintId shouldBe "kova.charSequence.notBlank"
                    result.messages[0].path.fullName shouldBe "first"
                }
            }

            context("create") {
                test("success") {
                    val factory = builder.build("hello", 42, "world")
                    val triple = factory.create()
                    triple shouldBe Triple("hello", 42, "world")
                }

                test("failure - first element invalid") {
                    val factory = builder.build("", 42, "world")
                    val ex =
                        shouldThrow<ValidationException> {
                            factory.create()
                        }
                    ex.messages.single().constraintId shouldBe "kova.charSequence.notBlank"
                }

                test("failure - second element invalid") {
                    val factory = builder.build("hello", -5, "world")
                    val ex =
                        shouldThrow<ValidationException> {
                            factory.create()
                        }
                    ex.messages.single().constraintId shouldBe "kova.number.positive"
                }

                test("failure - third element invalid") {
                    val factory = builder.build("hello", 42, "")
                    val ex =
                        shouldThrow<ValidationException> {
                            factory.create()
                        }
                    ex.messages.single().constraintId shouldBe "kova.charSequence.notBlank"
                }

                test("failure - all elements invalid") {
                    val factory = builder.build("", -5, "")
                    val ex =
                        shouldThrow<ValidationException> {
                            factory.create()
                        }
                    ex.messages.size shouldBe 3
                }
            }
        }

        context("TripleFactoryBuilder with different types") {
            val builder =
                TripleFactoryBuilder(
                    firstValidator =
                        Kova
                            .string()
                            .notBlank()
                            .min(1)
                            .max(50),
                    secondValidator = Kova.int().min(0).max(120),
                    thirdValidator = Kova.string().notBlank().max(100),
                )

            test("success") {
                val factory = builder.build("Alice", 30, "alice@example.com")
                val result = factory.tryCreate()
                result.isSuccess().mustBeTrue()
                result.value shouldBe Triple("Alice", 30, "alice@example.com")
            }

            test("failure - name too long") {
                val longName = "a".repeat(51)
                val factory = builder.build(longName, 30, "alice@example.com")
                val result = factory.tryCreate()
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.charSequence.max"
            }

            test("failure - age out of range") {
                val factory = builder.build("Alice", 150, "alice@example.com")
                val result = factory.tryCreate()
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.comparable.max"
            }

            test("failure - email too long") {
                val longEmail = "a".repeat(101)
                val factory = builder.build("Alice", 30, longEmail)
                val result = factory.tryCreate()
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.charSequence.max"
            }
        }

        context("TripleFactoryBuilder with identity validators") {
            val builder =
                TripleFactoryBuilder(
                    firstValidator = Kova.string(),
                    secondValidator = Kova.int(),
                    thirdValidator = Kova.string(),
                )

            test("success - no constraints") {
                val factory = builder.build("any string", 123, "another string")
                val result = factory.tryCreate()
                result.isSuccess().mustBeTrue()
                result.value shouldBe Triple("any string", 123, "another string")
            }
        }

        context("TripleFactoryBuilder with type transformation") {
            val builder =
                TripleFactoryBuilder(
                    firstValidator = Kova.string().toInt(),
                    secondValidator = Kova.string().toInt(),
                    thirdValidator = Kova.string().toInt(),
                )

            test("success - all elements transformed") {
                val factory = builder.build("10", "20", "30")
                val result = factory.tryCreate()
                result.isSuccess().mustBeTrue()
                result.value shouldBe Triple(10, 20, 30)
            }

            test("failure - first element not a number") {
                val factory = builder.build("abc", "20", "30")
                val result = factory.tryCreate()
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.string.isInt"
                result.messages
                    .single()
                    .path.fullName shouldBe "first"
            }

            test("failure - second element not a number") {
                val factory = builder.build("10", "xyz", "30")
                val result = factory.tryCreate()
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.string.isInt"
                result.messages
                    .single()
                    .path.fullName shouldBe "second"
            }

            test("failure - third element not a number") {
                val factory = builder.build("10", "20", "xyz")
                val result = factory.tryCreate()
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.string.isInt"
                result.messages
                    .single()
                    .path.fullName shouldBe "third"
            }
        }
    })
