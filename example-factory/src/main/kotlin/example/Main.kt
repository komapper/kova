package example

import org.komapper.extension.validator.Validation
import org.komapper.extension.validator.ValidationResult
import org.komapper.extension.validator.factory.bind
import org.komapper.extension.validator.factory.factory
import org.komapper.extension.validator.isSuccess
import org.komapper.extension.validator.max
import org.komapper.extension.validator.min
import org.komapper.extension.validator.notBlank
import org.komapper.extension.validator.toInt
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

fun Validation.validate(user: User) =
    user.schema {
        user::age {
            min(it, 0)
            max(it, 120)
        } // property validator
    }

fun Validation.buildUser(
    name: String,
    age: String,
) = factory {
    val name by bind(name) {
        this.min(it, 1)
        this.notBlank(it)
        it
    } // argument validator
    val age by bind(age) { toInt(it) } // argument validator
    User(name, age)
}.also { validate(it) } // object validator

fun Validation.buildAge(age: String) =
    factory {
        val value by bind(age) { toInt(it) } // argument validator
        Age(value)
    }

fun Validation.buildPerson(
    name: String,
    age: String,
) = factory {
    val name by bind(name) {
        this.min(it, 1)
        this.notBlank(it)
        it
    } // argument validator
    val age by bind { buildAge(age) } // nested object validator
    Person(name, age)
}

fun main() {
    println("\n# Creation")

    tryValidate { buildUser("a", "10") }.printResult()
    // ## Success
    // User(name=a, age=10)

    tryValidate { buildUser("a", "130") }.printResult()
    // ## Failure
    // Message(constraintId=kova.comparable.max, text='must be less than or equal to 120', root=example.User, path=age, input=130, args=[120])

    println("\n# Creation(nested object)")

    tryValidate { buildPerson("a", "10") }.printResult()
    // ## Success
    // Person(name=a, age=Age(value=10))

    tryValidate { buildPerson("   ", "abc") }.printResult()
    // ## Failure
    // Message(constraintId=kova.charSequence.notBlank, text='must not be blank', root=factory, path=name, input=   , args=[])
    // Message(constraintId=kova.string.isInt, text='must be a valid integer', root=factory, path=age.value, input=abc, args=[])
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
