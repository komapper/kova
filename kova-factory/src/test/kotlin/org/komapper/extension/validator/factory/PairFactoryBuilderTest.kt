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

class PairFactoryBuilderTest :
    FunSpec({

        context("PairFactoryBuilder with primitive types") {
            val builder =
                PairFactoryBuilder(
                    firstValidator = Kova.string().notBlank().max(10),
                    secondValidator = Kova.int().positive(),
                )

            context("tryCreate") {
                test("success") {
                    val factory = builder.build("hello", 42)
                    val result = factory.tryCreate()
                    result.isSuccess().mustBeTrue()
                    result.value shouldBe Pair("hello", 42)
                }

                test("failure - first element invalid") {
                    val factory = builder.build("", 42)
                    val result = factory.tryCreate()
                    result.isFailure().mustBeTrue()
                    result.messages.size shouldBe 1
                    result.messages[0].constraintId shouldBe "kova.charSequence.notBlank"
                    result.messages[0].root shouldBe "kotlin.Pair"
                    result.messages[0].path.fullName shouldBe "first"
                }

                test("failure - second element invalid") {
                    val factory = builder.build("hello", -5)
                    val result = factory.tryCreate()
                    result.isFailure().mustBeTrue()
                    result.messages.size shouldBe 1
                    result.messages[0].constraintId shouldBe "kova.number.positive"
                    result.messages[0].root shouldBe "kotlin.Pair"
                    result.messages[0].path.fullName shouldBe "second"
                }

                test("failure - both elements invalid") {
                    val factory = builder.build("", -5)
                    val result = factory.tryCreate()
                    result.isFailure().mustBeTrue()
                    result.messages.size shouldBe 2
                    result.messages[0].constraintId shouldBe "kova.charSequence.notBlank"
                    result.messages[0].path.fullName shouldBe "first"
                    result.messages[1].constraintId shouldBe "kova.number.positive"
                    result.messages[1].path.fullName shouldBe "second"
                }

                test("failure - both elements invalid with failFast") {
                    val factory = builder.build("", -5)
                    val result = factory.tryCreate(config = ValidationConfig(failFast = true))
                    result.isFailure().mustBeTrue()
                    result.messages.size shouldBe 1
                    result.messages[0].constraintId shouldBe "kova.charSequence.notBlank"
                    result.messages[0].path.fullName shouldBe "first"
                }
            }

            context("create") {
                test("success") {
                    val factory = builder.build("hello", 42)
                    val pair = factory.create()
                    pair shouldBe Pair("hello", 42)
                }

                test("failure - first element invalid") {
                    val factory = builder.build("", 42)
                    val ex =
                        shouldThrow<ValidationException> {
                            factory.create()
                        }
                    ex.messages.single().constraintId shouldBe "kova.charSequence.notBlank"
                }

                test("failure - second element invalid") {
                    val factory = builder.build("hello", -5)
                    val ex =
                        shouldThrow<ValidationException> {
                            factory.create()
                        }
                    ex.messages.single().constraintId shouldBe "kova.number.positive"
                }

                test("failure - both elements invalid") {
                    val factory = builder.build("", -5)
                    val ex =
                        shouldThrow<ValidationException> {
                            factory.create()
                        }
                    ex.messages.size shouldBe 2
                }
            }
        }

        context("PairFactoryBuilder with different types") {
            val builder =
                PairFactoryBuilder(
                    firstValidator =
                        Kova
                            .string()
                            .notBlank()
                            .min(1)
                            .max(50),
                    secondValidator = Kova.int().min(0).max(120),
                )

            test("success") {
                val factory = builder.build("Alice", 30)
                val result = factory.tryCreate()
                result.isSuccess().mustBeTrue()
                result.value shouldBe Pair("Alice", 30)
            }

            test("failure - name too long") {
                val longName = "a".repeat(51)
                val factory = builder.build(longName, 30)
                val result = factory.tryCreate()
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.charSequence.max"
            }

            test("failure - age out of range") {
                val factory = builder.build("Alice", 150)
                val result = factory.tryCreate()
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.comparable.max"
            }
        }

        context("PairFactoryBuilder with identity validators") {
            val builder =
                PairFactoryBuilder(
                    firstValidator = Kova.string(),
                    secondValidator = Kova.int(),
                )

            test("success - no constraints") {
                val factory = builder.build("any string", 123)
                val result = factory.tryCreate()
                result.isSuccess().mustBeTrue()
                result.value shouldBe Pair("any string", 123)
            }
        }

        context("PairFactoryBuilder with type transformation") {
            val builder =
                PairFactoryBuilder(
                    firstValidator = Kova.string().toInt(),
                    secondValidator = Kova.string().toInt(),
                )

            test("success - both elements transformed") {
                val factory = builder.build("10", "20")
                val result = factory.tryCreate()
                result.isSuccess().mustBeTrue()
                result.value shouldBe Pair(10, 20)
            }

            test("failure - first element not a number") {
                val factory = builder.build("abc", "20")
                val result = factory.tryCreate()
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.string.isInt"
                result.messages
                    .single()
                    .path.fullName shouldBe "first"
            }

            test("failure - second element not a number") {
                val factory = builder.build("10", "xyz")
                val result = factory.tryCreate()
                result.isFailure().mustBeTrue()
                result.messages.single().constraintId shouldBe "kova.string.isInt"
                result.messages
                    .single()
                    .path.fullName shouldBe "second"
            }
        }
    })
