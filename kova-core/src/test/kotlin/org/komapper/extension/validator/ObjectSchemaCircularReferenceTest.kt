package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec

class ObjectSchemaCircularReferenceTest :
    FunSpec({

        test("circular reference between schemas") {
            val city = City(emptyList())
            val user = User(city)
            validate { validate(city) }
            validate { validate(user) }
        }
    })

class City(
    val users: List<User>,
)

fun Validation.validate(city: City) = city.schema { city::users { users -> onEach(users) { validate(it) } } }

class User(
    val city: City,
)

fun Validation.validate(user: User): Unit = user.schema { user::city { validate(it) } }
