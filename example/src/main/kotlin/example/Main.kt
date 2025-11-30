package example

import org.komapper.extension.validator.Kova
import org.komapper.extension.validator.ObjectFactory
import org.komapper.extension.validator.ObjectSchema
import org.komapper.extension.validator.messages
import org.komapper.extension.validator.then
import org.komapper.extension.validator.tryCreate

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
        val arg1 = Kova.arg(this.name, name)
        val arg2 = Kova.arg(this.age, age)
        val arguments = Kova.arguments(arg1, arg2)
        return arguments.createFactory(UserSchema, ::User)
    }
}

object AgeSchema : ObjectSchema<Age>() {
    private val value = Age::value { Kova.int().min(0).max(120) }

    fun build(age: String): ObjectFactory<Age> {
        val arg1 = Kova.arg(Kova.string().toInt().then(this.value), age)
        val arguments = Kova.arguments(arg1)
        return arguments.createFactory(AgeSchema, ::Age)
    }
}

object PersonSchema : ObjectSchema<Person>() {
    private val name = Person::name { Kova.string().min(1).notBlank() }
    private val age = Person::age { AgeSchema }

    fun build(
        name: String,
        age: String,
    ): ObjectFactory<Person> {
        val arg1 = Kova.arg(this.name, name)
        val arg2 = Kova.arg(this.age, this.age.build(age))
        val arguments = Kova.arguments(arg1, arg2)
        return arguments.createFactory(PersonSchema, ::Person)
    }
}

fun main() {
    val factory1 = UserSchema.build("aa", 20)
    val result1 = factory1.tryCreate()
    println(result1)

    val factory2 = UserSchema.build("", 20)
    val result2 = factory2.tryCreate()
    println(result2.messages.map { it.content })

    val personFactory = PersonSchema.build("bb", "30")
    val personResult = personFactory.tryCreate()
    println(personResult)
}
