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

object UserSchema : ObjectSchema<User>({
    User::name { it.min(1).notBlank() }
    User::age { it.min(0).max(120) }
})

object AgeSchema : ObjectSchema<Age>({
    Age::value { it.min(0).max(120) }
})

object PersonSchema : ObjectSchema<Person>({
    Person::name { it.min(1).notBlank() }
    Person::age { AgeSchema }
})

fun main() {
    println("\n#Validation")
    println("##Success")
    when (val result = UserSchema.tryValidate(User("a", 10))) {
        is ValidationResult.Success -> {
            println("Success: ${result.value}") // Success: User(name=a, age=10)
        }

        is ValidationResult.Failure -> error("never happens")
    }
    println("##Failure")
    when (val result = UserSchema.tryValidate(User("  ", -1))) {
        is ValidationResult.Success -> error("never happens")
        is ValidationResult.Failure -> {
            result.messages.joinToString("\n").let { println(it) }
            // Message(constraintId=kova.string.notBlank, text='must not be blank', root=example.User, path=name, input=)
            // Message(constraintId=kova.comparable.min, text='must be greater than or equal to 0', root=example.User, path=age, input=-1)
        }
    }

    println("\n#Validation(nested object schema)")
    println("##Success")
    when (val result = PersonSchema.tryValidate(Person("a", Age(10)))) {
        is ValidationResult.Success -> {
            println("Success: ${result.value}") // Person(name=a, age=Age(value=10))
        }

        is ValidationResult.Failure -> error("never happens")
    }
    println("##Failure")
    when (val result = PersonSchema.tryValidate(Person("  ", Age(-1)))) {
        is ValidationResult.Success -> error("never happens")
        is ValidationResult.Failure -> {
            result.messages.joinToString("\n").let { println(it) }
            // Message(constraintId=kova.string.notBlank, text='must not be blank', root=example.Person, path=name, input=)
            // Message(constraintId=kova.comparable.min, text='must be greater than or equal to 0', root=example.Person, path=age.value, input=-1)
        }
    }
}
