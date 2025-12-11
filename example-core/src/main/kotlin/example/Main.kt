package example

import org.komapper.extension.validator.ObjectSchema
import org.komapper.extension.validator.ValidationResult
import org.komapper.extension.validator.max
import org.komapper.extension.validator.min
import org.komapper.extension.validator.notBlank
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

object UserSchema : ObjectSchema<User>() {
    private val nameV = User::name { it.min(1).notBlank() }
    private val ageV = User::age { it.min(0).max(120) }
}

object AgeSchema : ObjectSchema<Age>() {
    private val valueV = Age::value { it.min(0).max(120) }
}

object PersonSchema : ObjectSchema<Person>() {
    private val nameV = Person::name { it.min(1).notBlank() }
    private val ageV = Person::age { AgeSchema }
}

fun main() {
    println("\n#Validation")
    println("##Success")
    when (val result = UserSchema.tryValidate(User("a", 10))) {
        is ValidationResult.Success -> {
            // Success: User(name=a, age=10)
            println("Success: ${result.value}")
        }

        is ValidationResult.Failure -> error("never happens")
    }
    println("##Failure")
    when (val result = UserSchema.tryValidate(User("", -1))) {
        is ValidationResult.Success -> error("never happens")
        is ValidationResult.Failure -> {
            // Failure: ["" must be at least 1 characters, "" must not be blank, Number -1 must be greater than or equal to 0]
            println("Failure: ${result.messages.map { it.text }}")
        }
    }
}
