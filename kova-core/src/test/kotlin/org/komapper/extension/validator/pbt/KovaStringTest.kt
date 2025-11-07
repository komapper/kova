package org.komapper.extension.validator.pbt

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.property.Arb
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.constant
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import org.komapper.extension.validator.Kova
import org.komapper.extension.validator.isSuccess

class KovaStringTest :
    FunSpec({

        test("min - boundary cases") {
            checkAll(Arb.int(0..100)) { minLength ->
                val validator = Kova.string().min(minLength)

                // Test exactly at boundary
                val atBoundary = "a".repeat(minLength)
                validator.tryValidate(atBoundary).isSuccess().shouldBeTrue()

                // Test one below boundary (if possible)
                if (minLength > 0) {
                    val belowBoundary = "a".repeat(minLength - 1)
                    validator.tryValidate(belowBoundary).isSuccess().shouldBeFalse()
                }

                // Test above boundary
                val aboveBoundary = "a".repeat(minLength + 1)
                validator.tryValidate(aboveBoundary).isSuccess().shouldBeTrue()
            }
        }

        test("max - boundary cases") {
            checkAll(Arb.int(0..100)) { maxLength ->
                val validator = Kova.string().max(maxLength)

                // Test exactly at boundary
                val atBoundary = "a".repeat(maxLength)
                validator.tryValidate(atBoundary).isSuccess().shouldBeTrue()

                // Test one above boundary
                val aboveBoundary = "a".repeat(maxLength + 1)
                validator.tryValidate(aboveBoundary).isSuccess().shouldBeFalse()

                // Test below boundary
                if (maxLength > 0) {
                    val belowBoundary = "a".repeat(maxLength - 1)
                    validator.tryValidate(belowBoundary).isSuccess().shouldBeTrue()
                }
            }
        }

        test("min and max together") {
            checkAll(Arb.int(0..50), Arb.int(0..50)) { min, max ->
                if (min <= max) {
                    val validator = Kova.string().min(min).max(max)

                    // Test valid range - middle
                    val validLength = (min + max) / 2
                    validator.tryValidate("a".repeat(validLength)).isSuccess().shouldBeTrue()

                    // Test valid range - at min boundary
                    validator.tryValidate("a".repeat(min)).isSuccess().shouldBeTrue()

                    // Test valid range - at max boundary
                    validator.tryValidate("a".repeat(max)).isSuccess().shouldBeTrue()

                    // Test below range
                    if (min > 0) {
                        validator.tryValidate("a".repeat(min - 1)).isSuccess().shouldBeFalse()
                    }

                    // Test above range
                    validator.tryValidate("a".repeat(max + 1)).isSuccess().shouldBeFalse()
                }
            }
        }

        test("blank - should pass for blank strings") {
            val blankStrings =
                Arb.choice(
                    Arb.constant(""),
                    Arb.int(1..10).map { " ".repeat(it) },
                    Arb.int(1..10).map { "\t".repeat(it) },
                    Arb.int(1..10).map { "\n".repeat(it) },
                    Arb.int(1..10).map { " \t\n".repeat(it) },
                )

            checkAll(blankStrings) { input ->
                Kova
                    .string()
                    .isBlank()
                    .tryValidate(input)
                    .isSuccess()
                    .shouldBeTrue()
            }
        }

        test("blank - should fail for non-blank strings") {
            checkAll(Arb.string(1..100).filter { it.isNotBlank() }) { input ->
                Kova
                    .string()
                    .isBlank()
                    .tryValidate(input)
                    .isSuccess()
                    .shouldBeFalse()
            }
        }

        test("isNotBlank - should pass for non-blank strings") {
            checkAll(Arb.string(1..100).filter { it.isNotBlank() }) { input ->
                Kova
                    .string()
                    .isNotBlank()
                    .tryValidate(input)
                    .isSuccess()
                    .shouldBeTrue()
            }
        }

        test("isNotBlank - should fail for blank strings") {
            val blankStrings =
                Arb.choice(
                    Arb.constant(""),
                    Arb.int(1..10).map { " ".repeat(it) },
                    Arb.int(1..10).map { "\t".repeat(it) },
                    Arb.int(1..10).map { "\n".repeat(it) },
                    Arb.int(1..10).map { " \t\n".repeat(it) },
                )

            checkAll(blankStrings) { input ->
                Kova
                    .string()
                    .isNotBlank()
                    .tryValidate(input)
                    .isSuccess()
                    .shouldBeFalse()
            }
        }

        test("length - should pass when string has exact length") {
            checkAll(Arb.int(0..100)) { targetLength ->
                val input = "a".repeat(targetLength)
                Kova
                    .string()
                    .length(targetLength)
                    .tryValidate(input)
                    .isSuccess()
                    .shouldBeTrue()
            }
        }

        test("length - should fail when string has different length") {
            checkAll(Arb.int(0..100), Arb.int(0..100)) { actualLength, targetLength ->
                if (actualLength != targetLength) {
                    val input = "a".repeat(actualLength)
                    Kova
                        .string()
                        .length(targetLength)
                        .tryValidate(input)
                        .isSuccess()
                        .shouldBeFalse()
                }
            }
        }

        test("startsWith - should pass when string starts with prefix") {
            checkAll(Arb.string(0..50), Arb.string(0..50)) { prefix, suffix ->
                val input = prefix + suffix
                Kova
                    .string()
                    .startsWith(prefix)
                    .tryValidate(input)
                    .isSuccess()
                    .shouldBeTrue()
            }
        }

        test("startsWith - should fail when string doesn't start with prefix") {
            checkAll(Arb.string(1..10), Arb.string(1..10).filter { it.isNotEmpty() }) { prefix, input ->
                if (!input.startsWith(prefix)) {
                    Kova
                        .string()
                        .startsWith(prefix)
                        .tryValidate(input)
                        .isSuccess()
                        .shouldBeFalse()
                }
            }
        }

        test("endsWith - should pass when string ends with suffix") {
            checkAll(Arb.string(0..50), Arb.string(0..50)) { prefix, suffix ->
                val input = prefix + suffix
                Kova
                    .string()
                    .endsWith(suffix)
                    .tryValidate(input)
                    .isSuccess()
                    .shouldBeTrue()
            }
        }

        test("endsWith - should fail when string doesn't end with suffix") {
            checkAll(Arb.string(1..10), Arb.string(1..10).filter { it.isNotEmpty() }) { suffix, input ->
                if (!input.endsWith(suffix)) {
                    Kova
                        .string()
                        .endsWith(suffix)
                        .tryValidate(input)
                        .isSuccess()
                        .shouldBeFalse()
                }
            }
        }
    })
