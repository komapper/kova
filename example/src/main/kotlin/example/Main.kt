package example

import org.komapper.extension.validator.Kova
import org.komapper.extension.validator.ObjectFactory
import org.komapper.extension.validator.ObjectSchema
import org.komapper.extension.validator.ValidationResult
import org.komapper.extension.validator.messages
import org.komapper.extension.validator.then
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
    private val name = User::name { Kova.string().min(1).notBlank() }
    private val age = User::age { Kova.int().min(0).max(120) }

    fun build(
        name: String,
        age: Int,
    ): ObjectFactory<User> {
        val arg0 = arg(name, this.name)
        val arg1 = arg(age, this.age)
        val arguments = arguments(arg0, arg1)
        return arguments.build(::User)
    }
}

object AgeSchema : ObjectSchema<Age>() {
    private val value = Age::value { Kova.int().min(0).max(120) }

    fun build(age: String): ObjectFactory<Age> {
        val arg0 = arg(age, Kova.string().toInt().then(this.value))
        val arguments = arguments(arg0)
        return arguments.build(::Age)
    }
}

object PersonSchema : ObjectSchema<Person>() {
    private val name = Person::name { Kova.string().min(1).notBlank() }
    private val age = Person::age { AgeSchema }

    fun build(
        name: String,
        age: String,
    ): ObjectFactory<Person> {
        val arg0 = arg(name, this.name)
        val arg1 = arg(this.age.build(age), this.age)
        val arguments = arguments(arg0, arg1)
        return arguments.build(::Person)
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
    when (val result = UserSchema.build("a", 10).tryCreate()) {
        is ValidationResult.Success -> {
            // Success: User(name=a, age=10)
            println("Success: ${result.value}")
        }
        is ValidationResult.Failure -> error("never happens")
    }
    println("##Failure")
    when (val result = UserSchema.build("", -1).tryCreate()) {
        is ValidationResult.Success -> error("never happens")
        is ValidationResult.Failure -> {
            // Failure: ["" must be at least 1 characters, "" must not be blank, Number -1 must be greater than or equal to 0]
            println("Failure: ${result.messages.map { it.content }}")
        }
    }

    println("\n#Creation(nest)")
    println("##Success")
    when (val result = PersonSchema.build("a", "30").tryCreate()) {
        is ValidationResult.Success -> {
            // Person(name=a, age=Age(value=30))
            println("Success: ${result.value}")
        }
        is ValidationResult.Failure -> error("never happens")
    }
    println("##Failure")
    when (val result = PersonSchema.build("", "not number").tryCreate()) {
        is ValidationResult.Success -> error("never happens")
        is ValidationResult.Failure -> {
            // Failure: ["" must be at least 1 characters, "" must not be blank, "not number" must be an int]
            println("Failure: ${result.messages.map { it.content }}")
        }
    }
}
