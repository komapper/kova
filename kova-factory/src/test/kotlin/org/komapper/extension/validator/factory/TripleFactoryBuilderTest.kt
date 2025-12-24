package org.komapper.extension.validator.factory

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import org.komapper.extension.validator.ValidationConfig
import org.komapper.extension.validator.Validation
import org.komapper.extension.validator.ValidationException
import org.komapper.extension.validator.and
import org.komapper.extension.validator.max
import org.komapper.extension.validator.min
import org.komapper.extension.validator.notBlank
import org.komapper.extension.validator.positive
import org.komapper.extension.validator.success
import org.komapper.extension.validator.toInt
import org.komapper.extension.validator.tryValidate
import org.komapper.extension.validator.validate

class TripleFactoryBuilderTest :
    FunSpec({

        context("TripleFactoryBuilder with primitive types") {
            context(_: Validation)
            fun build(
                first: String,
                second: Int,
                third: String,
            ) = buildTriple(
                bind(first) { it.notBlank() and { it.max(10) } },
                bind(second) { it.positive() },
                bind(third) { it.notBlank() and { it.max(20) } },
            )

            context("tryCreate") {
                test("success") {
                    val result = tryValidate { build("hello", 42, "world") }
                    result.shouldBeSuccess()
                    result.value shouldBe Triple(Unit, Unit, Unit)
                }

                test("failure - first element invalid") {
                    val result = tryValidate { build("", 42, "world") }
                    result.shouldBeFailure()
                    result.messages.size shouldBe 1
                    result.messages[0].constraintId shouldBe "kova.charSequence.notBlank"
                    result.messages[0].root shouldBe "kotlin.Triple"
                    result.messages[0].path.fullName shouldBe "first"
                }

                test("failure - second element invalid") {
                    val result = tryValidate { build("hello", -5, "world") }
                    result.shouldBeFailure()
                    result.messages.size shouldBe 1
                    result.messages[0].constraintId shouldBe "kova.number.positive"
                    result.messages[0].root shouldBe "kotlin.Triple"
                    result.messages[0].path.fullName shouldBe "second"
                }

                test("failure - third element invalid") {
                    val result = tryValidate { build("hello", 42, "") }
                    result.shouldBeFailure()
                    result.messages.size shouldBe 1
                    result.messages[0].constraintId shouldBe "kova.charSequence.notBlank"
                    result.messages[0].root shouldBe "kotlin.Triple"
                    result.messages[0].path.fullName shouldBe "third"
                }

                test("failure - all elements invalid") {
                    val result = tryValidate { build("", -5, "") }
                    result.shouldBeFailure()
                    result.messages.size shouldBe 3
                    result.messages[0].constraintId shouldBe "kova.charSequence.notBlank"
                    result.messages[0].path.fullName shouldBe "first"
                    result.messages[1].constraintId shouldBe "kova.number.positive"
                    result.messages[1].path.fullName shouldBe "second"
                    result.messages[2].constraintId shouldBe "kova.charSequence.notBlank"
                    result.messages[2].path.fullName shouldBe "third"
                }

                test("failure - all elements invalid with failFast") {
                    val result = tryValidate(config = ValidationConfig(failFast = true)) { build("", -5, "") }
                    result.shouldBeFailure()
                    result.messages.size shouldBe 1
                    result.messages[0].constraintId shouldBe "kova.charSequence.notBlank"
                    result.messages[0].path.fullName shouldBe "first"
                }
            }

            context("create") {
                test("success") {
                    val triple = validate { build("hello", 42, "world") }
                    triple shouldBe Triple(Unit, Unit, Unit)
                }

                test("failure - first element invalid") {
                    val ex = shouldThrow<ValidationException> { validate { build("", 42, "world") } }
                    ex.messages.single().constraintId shouldBe "kova.charSequence.notBlank"
                }

                test("failure - second element invalid") {
                    val ex = shouldThrow<ValidationException> { validate { build("hello", -5, "world") } }
                    ex.messages.single().constraintId shouldBe "kova.number.positive"
                }

                test("failure - third element invalid") {
                    val ex = shouldThrow<ValidationException> { validate { build("hello", 42, "") } }
                    ex.messages.single().constraintId shouldBe "kova.charSequence.notBlank"
                }

                test("failure - all elements invalid") {
                    val ex = shouldThrow<ValidationException> { validate { build("", -5, "") } }
                    ex.messages.size shouldBe 3
                }
            }
        }

        context("TripleFactoryBuilder with different types") {
            context(_: Validation)
            fun build(
                name: String,
                age: Int,
                email: String,
            ) = buildTriple(
                bind(name) { it.notBlank() and { it.min(1) } and { it.max(50) } },
                bind(age) { it.min(0) and { it.max(120) } },
                bind(email) { it.notBlank() and { it.max(100) } },
            )

            test("success") {
                val result = tryValidate { build("Alice", 30, "alice@example.com") }
                result.shouldBeSuccess()
                result.value shouldBe Triple(Unit, Unit, Unit)
            }

            test("failure - name too long") {
                val longName = "a".repeat(51)
                val result = tryValidate { build(longName, 30, "alice@example.com") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.max"
            }

            test("failure - age out of range") {
                val result = tryValidate { build("Alice", 150, "alice@example.com") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.comparable.max"
            }

            test("failure - email too long") {
                val longEmail = "a".repeat(101)
                val result = tryValidate { build("Alice", 30, longEmail) }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.charSequence.max"
            }
        }

        context("TripleFactoryBuilder with identity validators") {
            context(_: Validation)
            fun build(
                first: String,
                second: Int,
                third: String,
            ) = buildTriple(
                bind(first) { Unit.success() },
                bind(second) { Unit.success() },
                bind(third) { Unit.success() },
            )

            test("success - no constraints") {
                val result = tryValidate { build("any string", 123, "another string") }
                result.shouldBeSuccess()
                result.value shouldBe Triple(Unit, Unit, Unit)
            }
        }

        context("TripleFactoryBuilder with type transformation") {
            context(_: Validation)
            fun build(
                first: String,
                second: String,
                third: String,
            ) = buildTriple(
                bind(first) { it.toInt() },
                bind(second) { it.toInt() },
                bind(third) { it.toInt() },
            )

            test("success - all elements transformed") {
                val result = tryValidate { build("10", "20", "30") }
                result.shouldBeSuccess()
                result.value shouldBe Triple(10, 20, 30)
            }

            test("failure - first element not a number") {
                val result = tryValidate { build("abc", "20", "30") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isInt"
                result.messages
                    .single()
                    .path.fullName shouldBe "first"
            }

            test("failure - second element not a number") {
                val result = tryValidate { build("10", "xyz", "30") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isInt"
                result.messages
                    .single()
                    .path.fullName shouldBe "second"
            }

            test("failure - third element not a number") {
                val result = tryValidate { build("10", "20", "xyz") }
                result.shouldBeFailure()
                result.messages.single().constraintId shouldBe "kova.string.isInt"
                result.messages
                    .single()
                    .path.fullName shouldBe "third"
            }
        }
    })
