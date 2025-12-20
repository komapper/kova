package example

import org.komapper.extension.validator.Kova
import org.komapper.extension.validator.ObjectSchema
import org.komapper.extension.validator.factory.Factory
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
    ): Factory<User> =
        UserSchema.factory {
            val name = check("name", name) { it.min(1).notBlank() } // argument validator
            val age = check("age", age) { it.toInt() } // argument validator
            create { User(name(), age()) }
        }
}

object AgeFactory {
    operator fun invoke(age: String) =
        Kova.factory {
            val age = check("value", age) { it.toInt() } // argument validator
            create { Age(age()) }
        }
}

object PersonFactory {
    operator fun invoke(
        name: String,
        age: String,
    ): Factory<Person> =
        Kova.factory {
            val name = check("name", name) { it.min(1).notBlank() } // argument validator
            val age = check("age", AgeFactory(age)) // argument validator
            create { Person(name(), age()) }
        }
}

fun main() {
    println("\n#Creation")
    println("##Success")
    val result = UserFactory("a", "10").tryCreate()
    if (result.isSuccess()) {
        println("Success: ${result.value}") // Success: User(name=a, age=10)
    } else {
        error("never happens")
    }

    println("##Failure")
    val result2 = UserFactory("a", "130").tryCreate()
    if (result2.isSuccess()) {
        error("never happens")
    } else {
        result2.messages.joinToString("\n").let { println(it) }
        // Message(constraintId=kova.comparable.max, text='must be less than or equal to 120', root=example.User, path=age, input=130, args=[(value, 120)])
    }

    println("\n#Creation(nested object)")
    println("##Success")
    val result3 = PersonFactory("a", "10").tryCreate()
    if (result3.isSuccess()) {
        println("Success: ${result.value}") // Person(name=a, age=Age(value=10))
    } else {
        error("never happens")
    }

    println("##Failure")
    val result4 = PersonFactory("   ", "abc").tryCreate()
    if (result4.isSuccess()) {
        error("never happens")
    } else {
        result4.messages.joinToString("\n").let { println(it) }
        // Message(constraintId=kova.charSequence.notBlank, text='must not be blank', root=factory, path=name, input=   , args=[])
        // Message(constraintId=kova.string.isInt, text='must be a valid integer', root=factory, path=age.value, input=abc, args=[])
    }
}
