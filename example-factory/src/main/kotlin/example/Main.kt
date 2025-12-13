package example

import org.komapper.extension.validator.ObjectSchema
import org.komapper.extension.validator.ValidationResult
import org.komapper.extension.validator.factory.generateFactory
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
}) {
    fun tryCreate(
        name: String,
        age: String,
    ): ValidationResult<User> {
        val factory =
            generateFactory {
                val name = check("name", name) { it.min(1).notBlank() } // argument validator
                val age = check("age", age) { it.toInt() } // argument validator
                create { User(name(), age()) }
            }
        return factory.tryCreate()
    }
}

object AgeSchema : ObjectSchema<Age>({}) {
    fun factory(age: String) =
        generateFactory {
            val age = check("value", age) { it.toInt() } // argument validator
            create { Age(age()) }
        }
}

object PersonSchema : ObjectSchema<Person>({}) {
    fun tryCreate(
        name: String,
        age: String,
    ): ValidationResult<Person> {
        val factory =
            PersonSchema.generateFactory {
                val name = check("name", name) { it.min(1).notBlank() } // argument validator
                val age = check("age", AgeSchema.factory(age)) // argument validator
                create { Person(name(), age()) }
            }
        return factory.tryCreate()
    }
}

fun main() {
    println("\n#Creation")
    println("##Success")
    val result = UserSchema.tryCreate("a", "10")
    if (result.isSuccess()) {
        println("Success: ${result.value}") // Success: User(name=a, age=10)
    } else {
        error("never happens")
    }

    println("##Failure")
    val result2 = UserSchema.tryCreate("a", "130")
    if (result2.isSuccess()) {
        error("never happens")
    } else {
        result2.messages.joinToString("\n").let { println(it) }
        // Message(constraintId=kova.comparable.max, text='must be less than or equal to 120', root=example.User, path=age, input=130)
    }

    println("\n#Creation(nested object)")
    println("##Success")
    val result3 = PersonSchema.tryCreate("a", "10")
    if (result3.isSuccess()) {
        println("Success: ${result.value}") // Person(name=a, age=Age(value=10))
    } else {
        error("never happens")
    }

    println("##Failure")
    val result4 = PersonSchema.tryCreate("   ", "abc")
    if (result4.isSuccess()) {
        error("never happens")
    } else {
        result4.messages.joinToString("\n").let { println(it) }
        // Message(constraintId=kova.charSequence.notBlank, text='must not be blank', root=, path=name, input=  )
        // Message(constraintId=kova.string.isInt, text='must be a valid integer', root=, path=age.value, input=abc)
    }
}
