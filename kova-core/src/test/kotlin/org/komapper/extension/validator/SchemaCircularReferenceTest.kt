package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import java.util.Locale

class SchemaCircularReferenceTest :
    FunSpec({

        beforeSpec {
            Locale.setDefault(Locale.US)
        }

        test("circular reference between schemas") {
            val users = mutableListOf<User>()
            val city = City(users)
            val user = User(city)
            users.add(user)
            validate { validate(city) }
            validate { validate(user) }
        }
    })

class City(
    val users: List<User>,
)

context(_: Validation)
fun validate(city: City) = city.schema { city::users { users -> users.ensureEach { validate(it) } } }

class User(
    val city: City,
)

context(_: Validation)
fun validate(user: User): Unit = user.schema { user::city { validate(it) } }
