package example.factory

import org.komapper.extension.validator.Validation
import org.komapper.extension.validator.ValidationResult
import org.komapper.extension.validator.ensureInRange
import org.komapper.extension.validator.ensureMinLength
import org.komapper.extension.validator.ensureNotBlank
import org.komapper.extension.validator.factory.bind
import org.komapper.extension.validator.factory.factory
import org.komapper.extension.validator.isSuccess
import org.komapper.extension.validator.schema
import org.komapper.extension.validator.transformToInt
import org.komapper.extension.validator.tryValidate

/**
 * Simple data class representing a user.
 * Used to demonstrate factory validation with type conversion.
 */
data class User(
    val name: String,
    val age: Int,
)

/**
 * Value object for age.
 * Used to demonstrate nested object creation in factory validation.
 */
data class Age(
    val value: Int,
)

/**
 * Data class containing a nested Age object.
 * Demonstrates nested factory validation.
 */
data class Person(
    val name: String,
    val age: Age,
)

/**
 * Schema validation for User objects.
 * Validates that the age property is between 0 and 120.
 */
context(_: Validation)
fun validate(user: User) =
    user.schema {
        user::age {
            it.ensureInRange(0..120)
        } // property validator
    }

/**
 * Factory function to build a User from raw string inputs.
 *
 * This demonstrates the factory validation pattern with three layers of validation:
 *
 * 1. **Argument validators** - Validate and transform each input parameter
 *    - name: validates it's not ensureBlank, ensureHas min ensureLength 1, and returns the validated string
 *    - age: converts the string to Int using transformToInt() validator
 *
 * 2. **Object validator** - Validates the constructed User object (.also { validate(it) })
 *    - Applies the validate(User) schema to check age range (0-120)
 *
 * The factory pattern is ideal for:
 * - Building objects from user input (forms, API requests)
 * - Type conversion with validation (String → Int)
 * - Combining multiple validation layers
 *
 * Property delegation (by bind) enables clean syntax while tracking validation errors
 * with proper paths for each parameter.
 */
context(_: Validation)
fun buildUser(
    name: String,
    age: String,
) = factory {
    val name by bind(name) {
        it.ensureMinLength(1)
        it.ensureNotBlank()
        it
    } // argument validator
    val age by bind(age) { transformToInt(it) } // argument validator
    User(name, age)
}.also { validate(it) } // object validator

/**
 * Factory function to build an Age object from a string.
 * Converts the string to Int and wraps it in an Age object.
 */
context(_: Validation)
fun buildAge(age: String) =
    factory {
        val value by bind(age) { transformToInt(it) } // argument validator
        Age(value)
    }

/**
 * Factory function to build a Person with nested object creation.
 *
 * Demonstrates nested factory validation:
 * - The name is validated as a string argument
 * - The age is created by calling buildAge(), which is itself a factory validator
 * - When using bind without an explicit input parameter (bind { ... }),
 *   it delegates to another factory/validator function
 *
 * This pattern allows you to compose complex object graphs while maintaining
 * proper validation error paths (e.g., "age.value" for nested properties).
 */
context(_: Validation)
fun buildPerson(
    name: String,
    age: String,
) = factory {
    val name by bind(name) {
        it.ensureMinLength(1)
        it.ensureNotBlank()
        it
    } // argument validator
    val age by bind { buildAge(age) } // nested object validator
    Person(name, age)
}

/**
 * Main function demonstrating Kova's factory validation pattern.
 *
 * This example shows:
 * 1. Basic factory validation with type conversion (String → Int)
 * 2. Multi-layer validation (argument validators + object validators)
 * 3. Nested factory validation for complex object graphs
 *
 * Factory validation is particularly useful for:
 * - Processing form data or API requests where inputs are strings
 * - Validating and transforming data during object construction
 * - Building domain objects from raw user input with proper error messages
 */
fun main() {
    // Example 1: Basic factory validation
    println("\n# Example 1: Creation")

    // Valid user - name "a" and age "10" (string converted to Int)
    // All validators pass: name is not ensureBlank, age converts successfully to 10 and is in range 0-120
    tryValidate { buildUser("a", "10") }.printResult()

    // Invalid user - age "130" exceeds the maximum allowed value
    // Argument validators pass (string converts to Int successfully)
    // Object validator fails (age 130 > max 120)
    tryValidate { buildUser("a", "130") }.printResult()

    // Example 2: Nested factory validation
    println("\n# Example 2: Creation(nested object)")

    // Valid person - both name and nested age are valid
    // buildAge is called to create the Age object, which is then validated
    tryValidate { buildPerson("a", "10") }.printResult()

    // Invalid person - multiple validation failures:
    // - name "   " is ensureBlank (fails ensureNotBlank validator)
    // - age "abc" cannot be converted to Int (fails parseInt validator)
    // Shows how multiple errors are collected across argument validators
    tryValidate { buildPerson("   ", "abc") }.printResult()
}

/**
 * Helper function to print validation results in a readable format.
 * Displays "Success" for valid data or "Failure" with detailed error messages.
 *
 * In factory validation, error paths indicate which parameter or nested property failed:
 * - "name" - the name argument failed validation
 * - "age" - the age argument failed validation
 * - "age.value" - the value property of the nested Age object failed
 * - "" (ensureEmpty path) - the object-level validation failed
 */
private fun ValidationResult<*>.printResult() {
    if (isSuccess()) {
        println("## Success")
    } else {
        println("## Failure")
        println(this.messages.joinToString("\n"))
    }
}
