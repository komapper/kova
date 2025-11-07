package org.komapper.extension.validator.pbt

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import org.komapper.extension.validator.Kova
import org.komapper.extension.validator.isSuccess

class KovaListTest :
    FunSpec({

        test("min - boundary cases") {
            checkAll(Arb.Companion.int(0..100)) { minSize ->
                val validator = Kova.list<String>().min(minSize)

                // Test exactly at boundary
                val atBoundary = List(minSize) { "element$it" }
                validator.tryValidate(atBoundary).isSuccess().shouldBeTrue()

                // Test one below boundary (if possible)
                if (minSize > 0) {
                    val belowBoundary = List(minSize - 1) { "element$it" }
                    validator.tryValidate(belowBoundary).isSuccess().shouldBeFalse()
                }

                // Test above boundary
                val aboveBoundary = List(minSize + 1) { "element$it" }
                validator.tryValidate(aboveBoundary).isSuccess().shouldBeTrue()
            }
        }

        test("min - empty list") {
            val validator = Kova.list<String>().min(1)
            validator.tryValidate(emptyList()).isSuccess().shouldBeFalse()
        }

        test("min - zero size should always pass") {
            checkAll(Arb.Companion.list(Arb.Companion.string(), 0..10)) { list ->
                Kova
                    .list<String>()
                    .min(0)
                    .tryValidate(list)
                    .isSuccess()
                    .shouldBeTrue()
            }
        }

        test("onEach - all elements valid") {
            checkAll(Arb.Companion.list(Arb.Companion.string(1..10), 0..20)) { list ->
                val validator = Kova.list<String>().onEach(Kova.string().min(1))
                validator.tryValidate(list).isSuccess().shouldBeTrue()
            }
        }

        test("onEach - all elements invalid") {
            checkAll(Arb.Companion.list(Arb.Companion.string(0..0), 1..20)) { list ->
                // All strings are empty (length 0)
                val validator = Kova.list<String>().onEach(Kova.string().min(1))
                validator.tryValidate(list).isSuccess().shouldBeFalse()
            }
        }

        test("onEach - mixed valid and invalid elements") {
            val mixedList = listOf("valid", "", "also valid", "")
            val validator = Kova.list<String>().onEach(Kova.string().min(1))
            validator.tryValidate(mixedList).isSuccess().shouldBeFalse()
        }

        test("onEach - empty list should pass") {
            val validator = Kova.list<String>().onEach(Kova.string().min(1))
            validator.tryValidate(emptyList()).isSuccess().shouldBeTrue()
        }

        test("onEach - with complex validator") {
            checkAll(Arb.Companion.list(Arb.Companion.string(5..20), 0..10)) { list ->
                // All strings have length 5-20, so they all satisfy min(3).max(25)
                val validator =
                    Kova.list<String>().onEach(
                        Kova
                            .string()
                            .min(3)
                            .max(25),
                    )
                validator.tryValidate(list).isSuccess().shouldBeTrue()
            }
        }

        test("min and onEach together - both constraints satisfied") {
            val validList = listOf("hello", "world", "test")
            val validator =
                Kova
                    .list<String>()
                    .min(2)
                    .onEach(Kova.string().min(1))
            validator.tryValidate(validList).isSuccess().shouldBeTrue()
        }

        test("min and onEach together - min fails") {
            val tooSmallList = listOf("hello")
            val validator =
                Kova
                    .list<String>()
                    .min(2)
                    .onEach(Kova.string().min(1))
            validator.tryValidate(tooSmallList).isSuccess().shouldBeFalse()
        }

        test("min and onEach together - onEach fails") {
            val invalidElementsList = listOf("hello", "", "world")
            val validator =
                Kova
                    .list<String>()
                    .min(2)
                    .onEach(Kova.string().min(1))
            validator.tryValidate(invalidElementsList).isSuccess().shouldBeFalse()
        }

        test("min and onEach together - both fail") {
            val singleInvalidElement = listOf("")
            val validator =
                Kova
                    .list<String>()
                    .min(2)
                    .onEach(Kova.string().min(1))
            validator.tryValidate(singleInvalidElement).isSuccess().shouldBeFalse()
        }

        test("validator composition with plus operator") {
            val validator1 = Kova.list<String>().min(2)
            val validator2 = Kova.list<String>().onEach(Kova.string().min(3))
            val combined = validator1 + validator2

            // Should pass both constraints
            val validList = listOf("hello", "world")
            combined.tryValidate(validList).isSuccess().shouldBeTrue()

            // Should fail min constraint
            val tooSmallList = listOf("hello")
            combined.tryValidate(tooSmallList).isSuccess().shouldBeFalse()

            // Should fail onEach constraint
            val invalidElements = listOf("hello", "ab")
            combined.tryValidate(invalidElements).isSuccess().shouldBeFalse()
        }

        test("onEach with nested list validators") {
            val innerValidator = Kova.list<Int>().min(1)
            val outerValidator = Kova.list<List<Int>>().onEach(innerValidator)

            // All inner lists have at least 1 element
            val validNestedList = listOf(listOf(1, 2), listOf(3), listOf(4, 5, 6))
            outerValidator.tryValidate(validNestedList).isSuccess().shouldBeTrue()

            // One inner list is empty
            val invalidNestedList = listOf(listOf(1, 2), emptyList(), listOf(4, 5))
            outerValidator.tryValidate(invalidNestedList).isSuccess().shouldBeFalse()
        }
    })
