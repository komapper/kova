package org.komapper.extension.validator.factory

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import org.komapper.extension.validator.Validation
import org.komapper.extension.validator.ValidationConfig
import org.komapper.extension.validator.ValidationException
import org.komapper.extension.validator.ensureMax
import org.komapper.extension.validator.ensureMaxLength
import org.komapper.extension.validator.ensureMin
import org.komapper.extension.validator.ensureMinLength
import org.komapper.extension.validator.ensureNotBlank
import org.komapper.extension.validator.ensurePositive
import org.komapper.extension.validator.transformToInt
import org.komapper.extension.validator.tryValidate
import org.komapper.extension.validator.validate

class PairFactoryBuilderTest :
    FunSpec({

        context("PairFactoryBuilder with primitive types") {
            context(_: Validation)
            fun build(
                first: String,
                second: Int,
            ) = buildPair(
                bind(first) {
                    it.ensureNotBlank()
                    it.ensureMaxLength(10)
                    it
                },
                bind(second) {
                    it.ensurePositive()
                    it
                },
            )

            context("tryCreate") {
                test("success") {
                    val result = tryValidate { build("hello", 42) }
                    result.shouldBeSuccess()
                    result.value shouldBe Pair("hello", 42)
                }

                test("failure - first element invalid") {
                    val result = tryValidate { build("", 42) }
                    result.shouldBeFailure()
                    result.messages.size shouldBe 1
                    result.messages[0].constraintId shouldBe "kova.charSequence.notBlank"
                    result.messages[0].root shouldBe "kotlin.Pair"
                    result.messages[0].path.fullName shouldBe "first"
                }

                test("failure - second element invalid") {
                    val result = tryValidate { build("hello", -5) }
                    result.shouldBeFailure()
                    result.messages.size shouldBe 1
                    result.messages[0].constraintId shouldBe "kova.number.positive"
                    result.messages[0].root shouldBe "kotlin.Pair"
                    result.messages[0].path.fullName shouldBe "second"
                }

                test("failure - both elements invalid") {
                    val result = tryValidate { build("", -5) }
                    result.shouldBeFailure()
                    result.messages.size shouldBe 2
                    result.messages[0].constraintId shouldBe "kova.charSequence.notBlank"
                    result.messages[0].path.fullName shouldBe "first"
                    result.messages[1].constraintId shouldBe "kova.number.positive"
                    result.messages[1].path.fullName shouldBe "second"
                }

                test("failure - both elements invalid with failFast") {
                    val result = tryValidate(config = ValidationConfig(failFast = true)) { build("", -5) }
                    result.shouldBeFailure()
                    result.messages.size shouldBe 1
                    result.messages[0].constraintId shouldBe "kova.charSequence.notBlank"
                    result.messages[0].path.fullName shouldBe "first"
                }
            }

            context("create") {
                test("success") {
                    val pair = validate { build("hello", 42) }
                    pair shouldBe Pair("hello", 42)
                }

                test("failure - first element invalid") {
                    val ex = shouldThrow<ValidationException> { validate { build("", 42) } }
                    ex.messages.single().constraintId shouldBe "kova.charSequence.notBlank"
                }

                test("failure - second element invalid") {
                    val ex = shouldThrow<ValidationException> { validate { build("hello", -5) } }
                    ex.messages.single().constraintId shouldBe "kova.number.positive"
                }

                test("failure - both elements invalid") {
                    val ex = shouldThrow<ValidationException> { validate { build("", -5) } }
                    ex.messages.size shouldBe 2
                }
            }
        }

        context("PairFactoryBuilder with different types") {
            context(_: Validation)
            fun build(
                name: String,
                age: Int,
            ) = buildPair(
                bind(name) {
                    it.ensureNotBlank()
                    it.ensureMinLength(1)
                    it.ensureMaxLength(50)
                    it
                },
                bind(age) {
                    it.ensureMin(0)
                    it.ensureMax(120)
                    it
                },
            )

            test("success") {
                val result = tryValidate { build("Alice", 30) }
                result.shouldBeSuccess()
                result.value shouldBe Pair("Alice", 30)
            }

            test("failure - name too long") {
                val longName = "a".repeat(51)
                val result = tryValidate { build(longName, 30) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.maxLength"
            }

            test("failure - age out of range") {
                val result = tryValidate { build("Alice", 150) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.comparable.max"
            }
        }

        context("PairFactoryBuilder with identity validators") {
            context(_: Validation)
            fun build(
                first: String,
                second: Int,
            ) = buildPair(
                bind(first) { first },
                bind(second) { second },
            )

            test("success - no constraints") {
                val result = tryValidate { build("any string", 123) }
                result.shouldBeSuccess()
                result.value shouldBe Pair("any string", 123)
            }
        }

        context("PairFactoryBuilder with type transformation") {
            context(_: Validation)
            fun build(
                first: String,
                second: String,
            ) = buildPair(
                bind(first) { transformToInt(it) },
                bind(second) { transformToInt(it) },
            )

            test("success - both elements transformed") {
                val result = tryValidate { build("10", "20") }
                result.shouldBeSuccess()
                result.value shouldBe Pair(10, 20)
            }

            test("failure - first element not a number") {
                val result = tryValidate { build("abc", "20") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.int"
                result.messages
                    .single()
                    .path.fullName shouldBe "first"
            }

            test("failure - second element not a number") {
                val result = tryValidate { build("10", "xyz") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.int"
                result.messages
                    .single()
                    .path.fullName shouldBe "second"
            }
        }
    })
