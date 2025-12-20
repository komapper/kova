package example

import org.komapper.extension.validator.ObjectSchema
import org.komapper.extension.validator.ValidationResult
import org.komapper.extension.validator.isSuccess
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
    println("\n# Validation")

    UserSchema.tryValidate(User("a", 10)).let { printResult(it) }
    // ## Success
    // User(name=a, age=10)

    UserSchema.tryValidate(User("  ", -1)).let { printResult(it) }
    // ## Failure
    // Message(constraintId=kova.charSequence.notBlank, text='must not be blank', root=example.User, path=name, input=  , args=[])
    // Message(constraintId=kova.comparable.min, text='must be greater than or equal to 0', root=example.User, path=age, input=-1, args=[0])

    println("\n# Validation(nested object schema)")

    PersonSchema.tryValidate(Person("a", Age(10))).let { printResult(it) }
    // ## Success
    // Person(name=a, age=Age(value=10))

    PersonSchema.tryValidate(Person("  ", Age(-1))).let { printResult(it) }
    // ## Failure
    // Message(constraintId=kova.charSequence.notBlank, text='must not be blank', root=example.Person, path=name, input=  , args=[])
    // Message(constraintId=kova.comparable.min, text='must be greater than or equal to 0', root=example.Person, path=age.value, input=-1, args=[0])
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
