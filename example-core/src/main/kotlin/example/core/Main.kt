package example.core

import org.komapper.extension.validator.Validation
import org.komapper.extension.validator.ValidationResult
import org.komapper.extension.validator.isSuccess
import org.komapper.extension.validator.max
import org.komapper.extension.validator.min
import org.komapper.extension.validator.notBlank
import org.komapper.extension.validator.notNegative
import org.komapper.extension.validator.tryValidate

data class User(
    val name: String,
    val age: Int,
)

data class Age(
    val value: Int,
)

data class Person(
    val name: String,
    val age: Age,
)

data class PriceRange(
    val minPrice: Double,
    val maxPrice: Double,
)

fun Validation.validate(user: User) =
    user.schema {
        user::name {
            min(it, 1)
            notBlank(it)
        }
        user::age {
            min(it, 0)
            max(it, 120)
        }
    }

fun Validation.validate(age: Age) =
    age.schema {
        age::value {
            min(it, 0)
            max(it, 120)
        }
    }

fun Validation.validate(person: Person) =
    person.schema {
        person::name {
            min(it, 1)
            notBlank(it)
        }
        person::age { validate(it) }
    }

fun Validation.validate(range: PriceRange) =
    range.schema {
        range::minPrice { notNegative(it) }
        range::maxPrice { notNegative(it) }
        // Validate relationship: minPrice must be less than or equal to maxPrice
        range.constrain("priceRange") {
            satisfies(it.minPrice <= it.maxPrice) {
                text("minPrice must be less than or equal to maxPrice")
            }
        }
    }

fun main() {
    println("\n# Validation")

    tryValidate { validate(User("a", 10)) }.printResult()
    // ## Success
    // User(name=a, age=10)

    tryValidate { validate(User("  ", -1)) }.printResult()
    // ## Failure
    // Message(constraintId=kova.charSequence.notBlank, text='must not be blank', root=example.User, path=name, input=  , args=[])
    // Message(constraintId=kova.comparable.min, text='must be greater than or equal to 0', root=example.User, path=age, input=-1, args=[0])

    println("\n# Validation(nested object schema)")

    tryValidate { validate(Person("a", Age(10))) }.printResult()
    // ## Success
    // Person(name=a, age=Age(value=10))

    tryValidate { validate(Person("  ", Age(-1))) }.printResult()
    // ## Failure
    // Message(constraintId=kova.charSequence.notBlank, text='must not be blank', root=example.Person, path=name, input=  , args=[])
    // Message(constraintId=kova.comparable.min, text='must be greater than or equal to 0', root=example.Person, path=age.value, input=-1, args=[0])

    tryValidate { validate(PriceRange(10.0, 20.0)) }.printResult()
    // ## Success

    tryValidate { validate(PriceRange(30.0, 20.0)) }.printResult()
    // ## Failure
    // Message(text='minPrice must be less than or equal to maxPrice', root=example.core.PriceRange, path=, input=PriceRange(minPrice=30.0, maxPrice=20.0))
}

private fun ValidationResult<*>.printResult() {
    if (isSuccess()) {
        println("## Success")
        println("${this.value}")
    } else {
        println("## Failure")
        println(this.messages.joinToString("\n"))
    }
}
