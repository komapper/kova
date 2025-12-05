package example

import org.komapper.extension.validator.Kova
import org.komapper.extension.validator.ObjectSchema
import org.komapper.extension.validator.ValidationResult
import org.komapper.extension.validator.max
import org.komapper.extension.validator.messages
import org.komapper.extension.validator.min
import org.komapper.extension.validator.notBlank
import org.komapper.extension.validator.then
import org.komapper.extension.validator.toInt
import org.komapper.extension.validator.tryCreate
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
    private val nameV = User::name { Kova.string().min(1).notBlank() }
    private val ageV = User::age { Kova.int().min(0).max(120) }

    fun bind(
        name: String,
        age: Int,
    ) = factory {
        val name = nameV.bind(name)
        val age = ageV.bind(age)
        create(::User, name, age)
    }
}

object AgeSchema : ObjectSchema<Age>() {
    private val valueV = Age::value { Kova.int().min(0).max(120) }

    fun bind(age: String) =
        factory {
            val age =
                Kova
                    .string()
                    .toInt()
                    .then(valueV)
                    .bind(age)
            create(::Age, age)
        }
}

object PersonSchema : ObjectSchema<Person>() {
    private val nameV = Person::name { Kova.string().min(1).notBlank() }
    private val ageV = Person::age { AgeSchema }

    fun bind(
        name: String,
        age: String,
    ) = factory {
        val name = nameV.bind(name)
        val age = ageV.bind(age)
        create(::Person, name, age)
    }
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
            println("Failure: ${result.messages.map { it.content }}")
        }
    }

    println("\n#Creation")
    println("##Success")
    when (val result = UserSchema.bind("a", 10).tryCreate()) {
        is ValidationResult.Success -> {
            // Success: User(name=a, age=10)
            println("Success: ${result.value}")
        }

        is ValidationResult.Failure -> error("never happens")
    }
    println("##Failure")
    when (val result = UserSchema.bind("", -1).tryCreate()) {
        is ValidationResult.Success -> error("never happens")
        is ValidationResult.Failure -> {
            // Failure: ["" must be at least 1 characters, "" must not be blank, Number -1 must be greater than or equal to 0]
            println("Failure: ${result.messages.map { it.content }}")
        }
    }

    println("\n#Creation(nest)")
    println("##Success")
    when (val result = PersonSchema.bind("a", "30").tryCreate()) {
        is ValidationResult.Success -> {
            // Person(name=a, age=Age(value=30))
            println("Success: ${result.value}")
        }

        is ValidationResult.Failure -> error("never happens")
    }
    println("##Failure")
    when (val result = PersonSchema.bind("", "not number").tryCreate()) {
        is ValidationResult.Success -> error("never happens")
        is ValidationResult.Failure -> {
            // Failure: ["" must be at least 1 characters, "" must not be blank, "not number" must be an int]
            println("Failure: ${result.messages.map { it.content }}")
        }
    }
}
