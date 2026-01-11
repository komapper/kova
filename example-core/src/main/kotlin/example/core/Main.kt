package example.core

import org.komapper.extension.validator.Validation
import org.komapper.extension.validator.ValidationResult
import org.komapper.extension.validator.ensureInRange
import org.komapper.extension.validator.ensureLengthAtLeast
import org.komapper.extension.validator.ensureNotBlank
import org.komapper.extension.validator.ensureNotNegative
import org.komapper.extension.validator.isSuccess
import org.komapper.extension.validator.schema
import org.komapper.extension.validator.text
import org.komapper.extension.validator.tryValidate

/**
 * Simple data class for demonstrating basic schema validation.
 * Shows validation of primitive properties (String and Int).
 */
data class User(
    val name: String,
    val age: Int,
)

/**
 * Value object for age, used to demonstrate nested schema validation.
 */
data class Age(
    val value: Int,
)

/**
 * Data class containing a nested object (Age).
 * Demonstrates how Kova handles nested schema validation.
 */
data class Person(
    val name: String,
    val age: Age,
)

/**
 * Data class for demonstrating cross-property validation.
 * Shows how to validate relationships between multiple properties.
 */
data class PriceRange(
    val minPrice: Double,
    val maxPrice: Double,
)

/**
 * Schema validation for User.
 * Validates that:
 * - name is not ensureBlank and ensureHas minimum ensureLength of 1
 * - age is between 0 and 120
 */
context(_: Validation)
fun User.validate() =
    schema {
        ::name { it.ensureLengthAtLeast(1).ensureNotBlank() }
        ::age { it.ensureInRange(0..120) }
    }

/**
 * Schema validation for Age value object.
 * Validates that the value is between 0 and 120.
 */
context(_: Validation)
fun Age.validate() =
    schema {
        ::value { it.ensureInRange(0..120) }
    }

/**
 * Schema validation for Person with nested object.
 * Demonstrates how to reuse validators - the age property uses validate(Age).
 * This creates a nested validation path (e.g., "age.value").
 */
context(_: Validation)
fun Person.validate() =
    schema {
        ::name { it.ensureLengthAtLeast(1).ensureNotBlank() }
        ::age { it.validate() }
    }

/**
 * Schema validation with cross-property constraint.
 * Validates individual properties first, then checks the relationship
 * between minPrice and maxPrice using a custom constraint.
 */
context(_: Validation)
fun PriceRange.validate() =
    schema {
        ::minPrice { it.ensureNotNegative() }
        ::maxPrice { it.ensureNotNegative() }
        // Validate relationship: minPrice must be less than or equal to maxPrice
        constrain("priceRange") {
            satisfies(it.minPrice <= it.maxPrice) {
                text("minPrice must be less than or equal to maxPrice")
            }
        }
    }

/**
 * Main function demonstrating various Kova validation scenarios.
 *
 * This example demonstrates:
 * 1. Basic schema validation - validating primitive properties
 * 2. Nested schema validation - validating objects containing other objects
 * 3. Cross-property validation - validating relationships between properties
 *
 * All examples use tryValidate() which returns ValidationResult (Success or Failure).
 * For each scenario, both successful and failing cases are shown.
 */
fun main() {
    println("\n# Example 1: Basic schema validation")

    // Valid user - name is not ensureBlank and age is within range
    tryValidate { User("a", 10).validate() }.printResult()

    // Invalid user - name is ensureBlank and age is ensureNegative
    // Shows how multiple validation errors are collected
    tryValidate { User("  ", -1).validate() }.printResult()

    println("\n# Example 2: Nested schema validation")

    // Valid person - both name and nested age object are valid
    tryValidate { Person("a", Age(10)).validate() }.printResult()

    // Invalid person - demonstrates nested validation path
    // Notice how the path "age.value" shows the nested property location
    tryValidate { Person("  ", Age(-1)).validate() }.printResult()

    println("\n# Example 3: Cross-property validation")

    // Valid price range - minPrice <= maxPrice
    tryValidate { PriceRange(10.0, 20.0).validate() }.printResult()

    // Invalid price range - minPrice > maxPrice violates the relationship constraint
    tryValidate { PriceRange(30.0, 20.0).validate() }.printResult()
}

/**
 * Helper function to print validation results in a readable format.
 * Displays "Success" for valid data or "Failure" with detailed error messages.
 *
 * Error messages include:
 * - constraintId: the validation rule that failed
 * - text: human-readable error message
 * - path: the property path where the error occurred
 * - input: the actual value that failed validation
 * - args: arguments passed to the constraint (e.g., min/max values)
 */
private fun ValidationResult<*>.printResult() {
    if (isSuccess()) {
        println("## Success")
    } else {
        println("## Failure")
        println(this.messages.joinToString("\n"))
    }
}
