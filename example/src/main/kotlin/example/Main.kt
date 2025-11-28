package example

import org.komapper.extension.validator.Kova
import org.komapper.extension.validator.ObjectSchema
import org.komapper.extension.validator.isFailure
import org.komapper.extension.validator.isSuccess
import org.komapper.extension.validator.plus
import org.komapper.extension.validator.tryValidate

data class User(
    val name: String,
    val age: Int,
)

object UserSchema : ObjectSchema<User>() {
    val name = User::name { Kova.string().min(1).isNotBlank() }
    val age = User::age { Kova.int().min(0).max(120) }
}

val userFactory =
    Kova
        .args(
            UserSchema.name,
            UserSchema.age + Kova.int().min(20), // add new rule
        ).factory(::User)

fun main() {
    println("# Validation")
    val invalidUser = User("", -1)
    val validationResult = UserSchema.tryValidate(invalidUser)
    if (validationResult.isFailure()) {
        // "" must be at least 1 characters
        // "" must not be blank
        // Number -1 must be greater than or equal to 0
        validationResult.details.forEach { println(it.message.content) }
    }

    println("# Creation 1 - try to create a user with invalid age")
    val creationResult1 = userFactory.tryCreate("abc", 19)
    if (creationResult1.isSuccess()) {
        // Number 19 must be greater than or equal to 20
        println(creationResult1.value)
    } else {
        creationResult1.details.forEach { println(it.message.content) }
    }

    println("# Creation 2 - try to create a valid user")
    val creationResult2 = userFactory.tryCreate("abc", 20)
    if (creationResult2.isSuccess()) {
        // User(name=abc, age=20)
        println(creationResult2.value)
    } else {
        creationResult2.details.forEach { println(it.message.content) }
    }
}
