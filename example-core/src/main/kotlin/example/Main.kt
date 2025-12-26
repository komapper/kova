package example

import org.komapper.extension.validator.Accumulate
import org.komapper.extension.validator.Validation
import org.komapper.extension.validator.ValidationResult
import org.komapper.extension.validator.checking
import org.komapper.extension.validator.invoke
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

context(_: Validation, _: Accumulate)
fun User.validate() {
    checking {
        ::name {
            it.min(1)
            it.notBlank()
        }
        ::age {
            it.min(0)
            it.max(120)
        }
    }
}

context(_: Validation, _: Accumulate)
fun Age.validate() {
    checking {
        ::value {
            it.min(0)
            it.max(120)
        }
    }
}

context(_: Validation, _: Accumulate)
fun Person.validate() {
    checking {
        ::name {
            it.min(1)
            it.notBlank()
        }
        ::age { it.validate() }
    }
}

fun main() {
    println("\n# Validation")

    tryValidate { User("a", 10).validate() }.printResult()
    // ## Success
    // User(name=a, age=10)

    tryValidate { User("  ", -1).validate() }.printResult()
    // ## Failure
    // Message(constraintId=kova.charSequence.notBlank, text='must not be blank', root=example.User, path=name, input=  , args=[])
    // Message(constraintId=kova.comparable.min, text='must be greater than or equal to 0', root=example.User, path=age, input=-1, args=[0])

    println("\n# Validation(nested object schema)")

    tryValidate { Person("a", Age(10)).validate() }.printResult()
    // ## Success
    // Person(name=a, age=Age(value=10))

    tryValidate { Person("  ", Age(-1)).validate() }.printResult()
    // ## Failure
    // Message(constraintId=kova.charSequence.notBlank, text='must not be blank', root=example.Person, path=name, input=  , args=[])
    // Message(constraintId=kova.comparable.min, text='must be greater than or equal to 0', root=example.Person, path=age.value, input=-1, args=[0])
}

private fun ValidationResult<*>.printResult() {
    if (isSuccess()) {
        println("## Success")
        println("${this.value}")
    } else {
        println("## Failure")
        println(this.messages.joinToString("\n"))
    }
}
