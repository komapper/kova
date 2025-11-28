package example

import org.komapper.extension.validator.Kova
import org.komapper.extension.validator.ObjectSchema
import org.komapper.extension.validator.ValidationResult
import org.komapper.extension.validator.isFailure
import org.komapper.extension.validator.isSuccess
import org.komapper.extension.validator.plus
import org.komapper.extension.validator.tryValidate

data class User(
    val name: String,
    val age: Int,
)

object UserSchema : ObjectSchema<User>() {
    val name = User::name { Kova.string().min(1).notBlank() }
    val age = User::age { Kova.int().min(0).max(120) }

    private val args =
        Kova.args(
            name,
            age + Kova.int().min(20), // add a new constraint
        )
    private val factory = args.createFactory(::User)

    fun tryCreate(
        name: String,
        age: Int,
    ): ValidationResult<User> = factory.tryCreate(name, age)

    fun create(
        name: String,
        age: Int,
    ): User = factory.create(name, age)
}

fun main() {
    println("\n# Validation")
    val invalidUser = User("", -1)
    val validationResult = UserSchema.tryValidate(invalidUser)
    if (validationResult.isFailure()) {
        // "" must be at least 1 characters
        // "" must not be blank
        // Number -1 must be greater than or equal to 0
        validationResult.details.forEach { println(it.message.content) }
    }

    println("\n# Creation 1 - try to create a user with invalid age")
    val creationResult1 = UserSchema.tryCreate("abc", 19)
    if (creationResult1.isSuccess()) {
        // Number 19 must be greater than or equal to 20
        println(creationResult1.value)
    } else {
        creationResult1.details.forEach { println(it.message.content) }
    }

    println("\n# Creation 2 - try to create a valid user")
    val creationResult2 = UserSchema.tryCreate("abc", 20)
    if (creationResult2.isSuccess()) {
        // User(name=abc, age=20)
        println(creationResult2.value)
    } else {
        creationResult2.details.forEach { println(it.message.content) }
    }

    println("\n# Creation 3 - create a valid user")
    val validUser = UserSchema.create("def", 30)
    // User(name=abc, age=20)
    println(validUser)
}
