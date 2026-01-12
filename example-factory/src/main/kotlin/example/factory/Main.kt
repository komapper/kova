package example.factory

import org.komapper.extension.validator.Validation
import org.komapper.extension.validator.ValidationResult
import org.komapper.extension.validator.ensureInRange
import org.komapper.extension.validator.ensureLengthAtLeast
import org.komapper.extension.validator.ensureNotBlank
import org.komapper.extension.validator.factory.capture
import org.komapper.extension.validator.isSuccess
import org.komapper.extension.validator.schema
import org.komapper.extension.validator.transformToInt
import org.komapper.extension.validator.tryValidate

/**
 * Simple data class representing a user.
 */
data class User(
    val name: String,
    val age: Int,
)

/**
 * Value object for age.
 */
data class Age(
    val value: Int,
)

/**
 * Data class containing a nested Age object.
 */
data class Person(
    val name: String,
    val age: Age,
)

/**
 * Schema validation for User.
 * Validates age is between 0 and 120.
 */
context(_: Validation)
fun User.validate() =
    schema {
        ::age {
            it.ensureInRange(0..120)
        } // property validator
    }

/**
 * Factory function to build a User from raw string inputs.
 *
 * Demonstrates two validation layers:
 * 1. Argument validators - validate and transform inputs (name: not blank, min length 1; age: String → Int)
 * 2. Object validator - validates the constructed User (age range 0-120)
 */
context(_: Validation)
fun buildUser(
    name: String,
    age: String,
): User {
    val name by capture { name.ensureLengthAtLeast(1).ensureNotBlank() } // argument validator
    val age by capture { age.transformToInt() } // argument validator
    return User(name, age).also { it.validate() }
}

/**
 * Factory function to build an Age from a string.
 */
context(_: Validation)
fun buildAge(age: String): Age {
    val value by capture { age.transformToInt() } // argument validator
    return Age(value)
}

/**
 * Factory function to build a Person with nested object creation.
 * The age is created by calling buildAge(), creating nested error paths like "age.value".
 */
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
 * Demonstrates factory validation with type conversion, multi-layer validation, and nested objects.
 */
fun main() {
    // Example 1: Basic factory validation
    println("\n# Example 1: Creation")

    // Valid user - name "a" and age "10" (string converted to Int)
    tryValidate { buildUser("a", "10") }.printResult()

    // Invalid user - age "130" exceeds maximum allowed value
    tryValidate { buildUser("a", "130") }.printResult()

    // Example 2: Nested factory validation
    println("\n# Example 2: Creation(nested object)")

    // Valid person
    tryValidate { buildPerson("a", "10") }.printResult()

    // Invalid person - name is blank and age cannot be converted to Int
    tryValidate { buildPerson("   ", "abc") }.printResult()
}

/**
 * Prints validation results.
 * Error paths indicate which parameter or nested property failed (e.g., "name", "age.value", "").
 */
private fun ValidationResult<*>.printResult() {
    if (isSuccess()) {
        println("## Success")
    } else {
        println("## Failure")
        println(this.messages.joinToString("\n"))
    }
}
