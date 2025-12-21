package example

import org.komapper.extension.validator.Kova
import org.komapper.extension.validator.ObjectSchema
import org.komapper.extension.validator.ValidationResult
import org.komapper.extension.validator.factory.factory
import org.komapper.extension.validator.factory.tryCreate
import org.komapper.extension.validator.isSuccess
import org.komapper.extension.validator.max
import org.komapper.extension.validator.min
import org.komapper.extension.validator.notBlank
import org.komapper.extension.validator.toInt

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

object UserSchema : ObjectSchema<User>({
    User::age { it.min(0).max(120) } // property validator
})

object UserFactory {
    operator fun invoke(
        name: String,
        age: String,
    ) = UserSchema.factory {
        val name by bind(name) { it.min(1).notBlank() } // argument validator
        val age by bind(age) { it.toInt() } // argument validator
        create { User(name(), age()) }
    }
}

object AgeFactory {
    operator fun invoke(age: String) =
        Kova.factory {
            val value by bind(age) { it.toInt() } // argument validator
            create { Age(value()) }
        }
}

object PersonFactory {
    operator fun invoke(
        name: String,
        age: String,
    ) = Kova.factory {
        val name by bind(name) { it.min(1).notBlank() } // argument validator
        val age by bind(AgeFactory(age)) // argument validator
        create { Person(name(), age()) }
    }
}

fun main() {
    println("\n# Creation")

    UserFactory("a", "10").tryCreate().let { printResult(it) }
    // ## Success
    // User(name=a, age=10)

    UserFactory("a", "130").tryCreate().let { printResult(it) }
    // ## Failure
    // Message(constraintId=kova.comparable.max, text='must be less than or equal to 120', root=example.User, path=age, input=130, args=[120])

    println("\n# Creation(nested object)")

    PersonFactory("a", "10").tryCreate().let { printResult(it) }
    // ## Success
    // Person(name=a, age=Age(value=10))

    PersonFactory("   ", "abc").tryCreate().let { printResult(it) }
    // ## Failure
    // Message(constraintId=kova.charSequence.notBlank, text='must not be blank', root=factory, path=name, input=   , args=[])
    // Message(constraintId=kova.string.isInt, text='must be a valid integer', root=factory, path=age.value, input=abc, args=[])
}

private fun printResult(result: ValidationResult<*>) {
    if (result.isSuccess()) {
        println("## Success")
        println("${result.value}")
    } else {
        println("## Failure")
        println(result.messages.joinToString("\n"))
    }
}
