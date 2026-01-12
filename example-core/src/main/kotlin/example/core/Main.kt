package example.core

import org.komapper.extension.validator.Validation
import org.komapper.extension.validator.ValidationResult
import org.komapper.extension.validator.capture
import org.komapper.extension.validator.ensureInRange
import org.komapper.extension.validator.ensureLengthAtLeast
import org.komapper.extension.validator.ensureNotBlank
import org.komapper.extension.validator.ensureNotNegative
import org.komapper.extension.validator.isSuccess
import org.komapper.extension.validator.schema
import org.komapper.extension.validator.text
import org.komapper.extension.validator.transformToInt
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
 * Validates name (minimum length 1, not blank) and age (0-120).
 */
context(_: Validation)
fun User.validate() =
    schema {
        ::name { it.ensureLengthAtLeast(1).ensureNotBlank() }
        ::age { it.ensureInRange(0..120) }
    }

/**
 * Schema validation for Age.
 * Validates value is between 0 and 120.
 */
context(_: Validation)
fun Age.validate() =
    schema {
        ::value { it.ensureInRange(0..120) }
    }

/**
 * Schema validation for a Person with a nested object.
 * The age property reuses Age.validate(), creating nested paths like "age.value".
 */
context(_: Validation)
fun Person.validate() =
    schema {
        ::name { it.ensureLengthAtLeast(1).ensureNotBlank() }
        ::age { it.validate() }
    }

/**
 * Schema validation with cross-property constraint.
 * Validates individual properties, then checks minPrice <= maxPrice.
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

context(_: Validation)
fun buildUser(
    name: String,
    age: String,
): User {
    val name by capture { name.ensureLengthAtLeast(1).ensureNotBlank() } // argument validator
    val age by capture { age.transformToInt() } // argument validator
    return User(name, age).also { it.validate() }
}

context(_: Validation)
fun buildAge(age: String): Age {
    val value by capture { age.transformToInt() } // argument validator
    return Age(value)
}

context(_: Validation)
fun buildPerson(
    name: String,
    age: String,
): Person {
    val name by capture { name.ensureLengthAtLeast(1).ensureNotBlank() } // argument validator
    val age by capture { buildAge(age) } // nested object validator
    return Person(name, age)
}


/**
 * Demonstrates basic schema validation, nested validation, and cross-property validation.
 * Uses tryValidate() which returns ValidationResult (Success or Failure).
 */
fun main() {
    println("\n# Example 1: Basic schema validation")

    // Valid user - name is not blank and age is within range
    tryValidate { User("a", 10).validate() }.printResult()

    // Invalid user - name is blank and age is negative
    tryValidate { User("  ", -1).validate() }.printResult()

    println("\n# Example 2: Nested schema validation")

    // Valid person
    tryValidate { Person("a", Age(10)).validate() }.printResult()

    // Invalid person - error path "age.value" shows a nested property location
    tryValidate { Person("  ", Age(-1)).validate() }.printResult()

    println("\n# Example 3: Cross-property validation")

    // Valid price range - minPrice <= maxPrice
    tryValidate { PriceRange(10.0, 20.0).validate() }.printResult()

    // Invalid price range - minPrice > maxPrice violates the relationship constraint
    tryValidate { PriceRange(30.0, 20.0).validate() }.printResult()

    println("\n# Example 4: Creation")

    // Valid user - name "a" and age "10" (string converted to Int)
    tryValidate { buildUser("a", "10") }.printResult()

    // Invalid user - age "130" exceeds maximum allowed value
    tryValidate { buildUser("a", "130") }.printResult()

    println("\n# Example 5: Creation(nested object)")

    // Valid person
    tryValidate { buildPerson("a", "10") }.printResult()

    // Invalid person - name is blank and age cannot be converted to Int
    tryValidate { buildPerson("   ", "abc") }.printResult()
}

/**
 * Prints validation results.
 * Error messages include constraintId, text, path, input, and args.
 */
private fun ValidationResult<*>.printResult() {
    if (isSuccess()) {
        println("## Success")
        println(this.value)
    } else {
        println("## Failure")
        println(this.messages.joinToString("\n"))
    }
}
