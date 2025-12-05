package org.komapper.extension.validator.pbt

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import org.komapper.extension.validator.Kova
import org.komapper.extension.validator.isSuccess
import org.komapper.extension.validator.max
import org.komapper.extension.validator.min
import org.komapper.extension.validator.onEachKey
import org.komapper.extension.validator.onEachValue
import org.komapper.extension.validator.plus
import org.komapper.extension.validator.tryValidate

class KovaMapTest :
    FunSpec({

        test("min - boundary cases") {
            checkAll(Arb.Companion.int(0..100)) { minSize ->
                val validator = Kova.map<String, String>().min(minSize)

                // Test exactly at boundary
                val atBoundary = (0 until minSize).associate { "key$it" to "value$it" }
                validator.tryValidate(atBoundary).isSuccess().shouldBeTrue()

                // Test one below boundary (if possible)
                if (minSize > 0) {
                    val belowBoundary = (0 until minSize - 1).associate { "key$it" to "value$it" }
                    validator.tryValidate(belowBoundary).isSuccess().shouldBeFalse()
                }

                // Test above boundary
                val aboveBoundary = (0 until minSize + 1).associate { "key$it" to "value$it" }
                validator.tryValidate(aboveBoundary).isSuccess().shouldBeTrue()
            }
        }

        test("min - empty map") {
            val validator = Kova.map<String, String>().min(1)
            validator.tryValidate(emptyMap()).isSuccess().shouldBeFalse()
        }

        test("min - zero size should always pass") {
            checkAll(
                Arb.Companion.map(
                    Arb.Companion.string(),
                    Arb.Companion.string(),
                    minSize = 0,
                    maxSize = 10,
                ),
            ) { map ->
                Kova
                    .map<String, String>()
                    .min(0)
                    .tryValidate(map)
                    .isSuccess()
                    .shouldBeTrue()
            }
        }

        test("onEachKey - all keys valid") {
            checkAll(
                Arb.Companion.map(
                    Arb.Companion.string(1..10),
                    Arb.Companion.string(),
                    minSize = 0,
                    maxSize = 20,
                ),
            ) { map ->
                val validator = Kova.map<String, String>().onEachKey(Kova.string().min(1))
                validator.tryValidate(map).isSuccess().shouldBeTrue()
            }
        }

        test("onEachKey - all keys invalid") {
            checkAll(
                Arb.Companion.map(
                    Arb.Companion.string(0..0),
                    Arb.Companion.string(),
                    minSize = 1,
                    maxSize = 20,
                ),
            ) { map ->
                // All keys are empty (length 0)
                val validator = Kova.map<String, String>().onEachKey(Kova.string().min(1))
                validator.tryValidate(map).isSuccess().shouldBeFalse()
            }
        }

        test("onEachKey - mixed valid and invalid keys") {
            val mixedMap = mapOf("valid" to "v1", "" to "v2", "also valid" to "v3", "" to "v4")
            val validator = Kova.map<String, String>().onEachKey(Kova.string().min(1))
            validator.tryValidate(mixedMap).isSuccess().shouldBeFalse()
        }

        test("onEachKey - empty map should pass") {
            val validator = Kova.map<String, String>().onEachKey(Kova.string().min(1))
            validator.tryValidate(emptyMap()).isSuccess().shouldBeTrue()
        }

        test("onEachValue - all values valid") {
            checkAll(
                Arb.Companion.map(
                    Arb.Companion.string(),
                    Arb.Companion.string(1..10),
                    minSize = 0,
                    maxSize = 20,
                ),
            ) { map ->
                val validator = Kova.map<String, String>().onEachValue(Kova.string().min(1))
                validator.tryValidate(map).isSuccess().shouldBeTrue()
            }
        }

        test("onEachValue - all values invalid") {
            checkAll(
                Arb.Companion.map(
                    Arb.Companion.string(),
                    Arb.Companion.string(0..0),
                    minSize = 1,
                    maxSize = 20,
                ),
            ) { map ->
                // All values are empty (length 0)
                val validator = Kova.map<String, String>().onEachValue(Kova.string().min(1))
                validator.tryValidate(map).isSuccess().shouldBeFalse()
            }
        }

        test("onEachValue - mixed valid and invalid values") {
            val mixedMap = mapOf("k1" to "valid", "k2" to "", "k3" to "also valid", "k4" to "")
            val validator = Kova.map<String, String>().onEachValue(Kova.string().min(1))
            validator.tryValidate(mixedMap).isSuccess().shouldBeFalse()
        }

        test("onEachValue - empty map should pass") {
            val validator = Kova.map<String, String>().onEachValue(Kova.string().min(1))
            validator.tryValidate(emptyMap()).isSuccess().shouldBeTrue()
        }

        test("onEachValue - with complex validator") {
            checkAll(
                Arb.Companion.map(
                    Arb.Companion.string(),
                    Arb.Companion.string(5..20),
                    minSize = 0,
                    maxSize = 10,
                ),
            ) { map ->
                // All values have length 5-20, so they all satisfy min(3).max(25)
                val validator =
                    Kova.map<String, String>().onEachValue(
                        Kova
                            .string()
                            .min(3)
                            .max(25),
                    )
                validator.tryValidate(map).isSuccess().shouldBeTrue()
            }
        }

        test("onEachKey and onEachValue together - both constraints satisfied") {
            val validMap = mapOf("hello" to "world", "foo" to "bar", "test" to "data")
            val validator =
                Kova
                    .map<String, String>()
                    .onEachKey(Kova.string().min(1))
                    .onEachValue(Kova.string().min(1))
            validator.tryValidate(validMap).isSuccess().shouldBeTrue()
        }

        test("onEachKey and onEachValue together - onEachKey fails") {
            val invalidKeyMap = mapOf("hello" to "world", "" to "bar", "test" to "data")
            val validator =
                Kova
                    .map<String, String>()
                    .onEachKey(Kova.string().min(1))
                    .onEachValue(Kova.string().min(1))
            validator.tryValidate(invalidKeyMap).isSuccess().shouldBeFalse()
        }

        test("onEachKey and onEachValue together - onEachValue fails") {
            val invalidValueMap = mapOf("hello" to "world", "foo" to "", "test" to "data")
            val validator =
                Kova
                    .map<String, String>()
                    .onEachKey(Kova.string().min(1))
                    .onEachValue(Kova.string().min(1))
            validator.tryValidate(invalidValueMap).isSuccess().shouldBeFalse()
        }

        test("onEachKey and onEachValue together - both fail") {
            val bothInvalidMap = mapOf("hello" to "world", "" to "", "test" to "data")
            val validator =
                Kova
                    .map<String, String>()
                    .onEachKey(Kova.string().min(1))
                    .onEachValue(Kova.string().min(1))
            validator.tryValidate(bothInvalidMap).isSuccess().shouldBeFalse()
        }

        test("min and onEachValue together - both constraints satisfied") {
            val validMap = mapOf("k1" to "hello", "k2" to "world", "k3" to "test")
            val validator =
                Kova
                    .map<String, String>()
                    .min(2)
                    .onEachValue(Kova.string().min(1))
            validator.tryValidate(validMap).isSuccess().shouldBeTrue()
        }

        test("min and onEachValue together - min fails") {
            val tooSmallMap = mapOf("k1" to "hello")
            val validator =
                Kova
                    .map<String, String>()
                    .min(2)
                    .onEachValue(Kova.string().min(1))
            validator.tryValidate(tooSmallMap).isSuccess().shouldBeFalse()
        }

        test("min and onEachValue together - onEachValue fails") {
            val invalidValuesMap = mapOf("k1" to "hello", "k2" to "", "k3" to "world")
            val validator =
                Kova
                    .map<String, String>()
                    .min(2)
                    .onEachValue(Kova.string().min(1))
            validator.tryValidate(invalidValuesMap).isSuccess().shouldBeFalse()
        }

        test("min and onEachValue together - both fail") {
            val singleInvalidValue = mapOf("k1" to "")
            val validator =
                Kova
                    .map<String, String>()
                    .min(2)
                    .onEachValue(Kova.string().min(1))
            validator.tryValidate(singleInvalidValue).isSuccess().shouldBeFalse()
        }

        test("validator composition with plus operator") {
            val validator1 = Kova.map<String, String>().min(2)
            val validator2 = Kova.map<String, String>().onEachValue(Kova.string().min(3))
            val combined = validator1 + validator2

            // Should pass both constraints
            val validMap = mapOf("k1" to "hello", "k2" to "world")
            combined.tryValidate(validMap).isSuccess().shouldBeTrue()

            // Should fail min constraint
            val tooSmallMap = mapOf("k1" to "hello")
            combined.tryValidate(tooSmallMap).isSuccess().shouldBeFalse()

            // Should fail onEachValue constraint
            val invalidValues = mapOf("k1" to "hello", "k2" to "ab")
            combined.tryValidate(invalidValues).isSuccess().shouldBeFalse()
        }

        test("onEachValue with nested map validators") {
            val innerValidator = Kova.map<String, Int>().min(1)
            val outerValidator = Kova.map<String, Map<String, Int>>().onEachValue(innerValidator)

            // All inner maps have at least 1 entry
            val validNestedMap =
                mapOf(
                    "map1" to mapOf("a" to 1, "b" to 2),
                    "map2" to mapOf("c" to 3),
                    "map3" to mapOf("d" to 4, "e" to 5, "f" to 6),
                )
            outerValidator.tryValidate(validNestedMap).isSuccess().shouldBeTrue()

            // One inner map is empty
            val invalidNestedMap =
                mapOf(
                    "map1" to mapOf("a" to 1, "b" to 2),
                    "map2" to emptyMap(),
                    "map3" to mapOf("d" to 4, "e" to 5),
                )
            outerValidator.tryValidate(invalidNestedMap).isSuccess().shouldBeFalse()
        }
    })
