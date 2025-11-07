package example

import org.komapper.extension.validator.Kova
import org.komapper.extension.validator.isFailure
import org.komapper.extension.validator.isSuccess

data class User(
    val name: String,
    val age: Int,
)

// App global constraint rules
object AppRules {
    val name = Kova.string().min(1).isNotBlank()
    val age = Kova.int().min(0).max(120)
}

val userValidator =
    Kova.validator {
        User::class {
            User::name { AppRules.name }
            User::age { AppRules.age }
        }
    }

val userFactory =
    Kova.factory {
        val name = AppRules.name
        val age = AppRules.age + Kova.int().min(20) // add new rule
        ::User { args(name, age) }
    }

fun main() {
    println("# Validation")
    val invalidUser = User("", -1)
    val validationResult = userValidator.tryValidate(invalidUser)
    if (validationResult.isFailure()) {
        // FailureDetail(context=ValidationContext(root=class example.User, path=name, failFast=false), messages=["" must be at least 1 characters], cause=null)
        // FailureDetail(context=ValidationContext(root=class example.User, path=name, failFast=false), messages=["" must not be blank], cause=null)
        // FailureDetail(context=ValidationContext(root=class example.User, path=age, failFast=false), messages=[Number -1 must be greater than or equal to 0], cause=null)
        validationResult.details.forEach { println(it) }
    }

    println("# Creation 1 - try to create a user with invalid age")
    val creationResult1 = userFactory.tryCreate("abc", 19)
    if (creationResult1.isSuccess()) {
        // FailureDetail(context=ValidationContext(root=fun `<init>`(kotlin.String, kotlin.Int): example.User, path=arg2, failFast=false), messages=[Number 19 must be greater than or equal to 20], cause=null)
        println(creationResult1.value)
    } else {
        creationResult1.details.forEach { println(it) }
    }

    println("# Creation 2 - try to create a valid user")
    val creationResult2 = userFactory.tryCreate("abc", 20)
    if (creationResult2.isSuccess()) {
        // User(name=abc, age=20)
        println(creationResult2.value)
    } else {
        creationResult2.details.forEach { println(it) }
    }
}
