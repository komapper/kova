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

data class User(val name: String, val age: Int)

data class Age(val value: Int)

data class Person(val name: String, val age: Age)

data class PriceRange(val minPrice: Double, val maxPrice: Double)

/** Schema validation for User. */
context(_: Validation)
fun User.validate() =
    schema {
        ::name { it.ensureLengthAtLeast(1).ensureNotBlank() }
        ::age { it.ensureInRange(0..120) }
    }

/** Schema validation for Age. */
context(_: Validation)
fun Age.validate() =
    schema {
        ::value { it.ensureInRange(0..120) }
    }

/** Schema validation with nested object. Error path: "age.value". */
context(_: Validation)
fun Person.validate() =
    schema {
        ::name { it.ensureLengthAtLeast(1).ensureNotBlank() }
        ::age { it.validate() }
    }

/** Schema validation with cross-property constraint. */
context(_: Validation)
fun PriceRange.validate() =
    schema {
        ::minPrice { it.ensureNotNegative() }
        ::maxPrice { it.ensureNotNegative() }
        constrain("priceRange") {
            satisfies(it.minPrice <= it.maxPrice) {
                text("minPrice must be less than or equal to maxPrice")
            }
        }
    }

/** Build User from raw strings with validation. */
context(_: Validation)
fun buildUser(name: String, age: String): User {
    val name by capture { name.ensureLengthAtLeast(1).ensureNotBlank() }
    val age by capture { age.transformToInt() }
    return User(name, age).also { it.validate() }
}

/** Build Age from raw string. */
context(_: Validation)
fun buildAge(age: String): Age {
    val value by capture { age.transformToInt() }
    return Age(value)
}

/** Build Person with nested builder call. Error path: "age.value". */
context(_: Validation)
fun buildPerson(name: String, age: String): Person {
    val name by capture { name.ensureLengthAtLeast(1).ensureNotBlank() }
    val age by capture { buildAge(age) }
    return Person(name, age)
}

fun main() {
    println("\n# Example 1: Basic schema validation")
    tryValidate { User("a", 10).validate() }.printResult() // valid
    tryValidate { User("  ", -1).validate() }.printResult() // invalid: blank name, negative age

    println("\n# Example 2: Nested schema validation")
    tryValidate { Person("a", Age(10)).validate() }.printResult() // valid
    tryValidate { Person("  ", Age(-1)).validate() }.printResult() // invalid: path "age.value"

    println("\n# Example 3: Cross-property validation")
    tryValidate { PriceRange(10.0, 20.0).validate() }.printResult() // valid
    tryValidate { PriceRange(30.0, 20.0).validate() }.printResult() // invalid: min > max

    println("\n# Example 4: Object creation with capture")
    tryValidate { buildUser("a", "10") }.printResult() // valid
    tryValidate { buildUser("a", "130") }.printResult() // invalid: age > 120

    println("\n# Example 5: Nested object creation")
    tryValidate { buildPerson("a", "10") }.printResult() // valid
    tryValidate { buildPerson("   ", "abc") }.printResult() // invalid: blank name, non-integer age
}

private fun ValidationResult<*>.printResult() {
    if (isSuccess()) {
        println("## Success: $value")
    } else {
        println("## Failure")
        messages.forEach { println("  $it") }
    }
}
