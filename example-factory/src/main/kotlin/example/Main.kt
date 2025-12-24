package example

import org.komapper.extension.validator.ValidationContext
import org.komapper.extension.validator.ValidationResult
import org.komapper.extension.validator.alsoThen
import org.komapper.extension.validator.and
import org.komapper.extension.validator.andMap
import org.komapper.extension.validator.checking
import org.komapper.extension.validator.factory.bind
import org.komapper.extension.validator.factory.factory
import org.komapper.extension.validator.isSuccess
import org.komapper.extension.validator.max
import org.komapper.extension.validator.min
import org.komapper.extension.validator.notBlank
import org.komapper.extension.validator.toInt
import org.komapper.extension.validator.invoke
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

context(_: ValidationContext)
fun User.validate() = checking {
    ::age { it.min(0) and { it.max(120) } } // property validator
}

object UserFactory {
    context(_: ValidationContext)
    operator fun invoke(
        name: String,
        age: String,
    ) = factory {
        val name by bind(name) { it.min(1) and { it.notBlank() } andMap { it } } // argument validator
        val age by bind(age) { it.toInt() } // argument validator
        create { User(name(), age()) }
    } alsoThen { it.validate() }
}

object AgeFactory {
    context(_: ValidationContext)
    operator fun invoke(age: String) = factory {
            val value by bind(age) { it.toInt() } // argument validator
            create { Age(value()) }
        }
}

object PersonFactory {
    context(_: ValidationContext)
    operator fun invoke(
        name: String,
        age: String,
    ) = factory {
        val name by bind(name) { it.min(1) and { it.notBlank() } andMap { it } } // argument validator
        val age by bind { AgeFactory(age) } // nested object validator
        create { Person(name(), age()) }
    }
}

fun main() {
    println("\n# Creation")

    tryValidate { UserFactory("a", "10") }.printResult()
    // ## Success
    // User(name=a, age=10)

    tryValidate { UserFactory("a", "130") }.printResult()
    // ## Failure
    // Message(constraintId=kova.comparable.max, text='must be less than or equal to 120', root=example.User, path=age, input=130, args=[120])

    println("\n# Creation(nested object)")

    tryValidate { PersonFactory("a", "10") }.printResult()
    // ## Success
    // Person(name=a, age=Age(value=10))

    tryValidate { PersonFactory("   ", "abc") }.printResult()
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
