package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec

class ObjectSchemaCircularReferenceTest :
    FunSpec({

        test("circular reference between schemas") {
            val city = City(emptyList())
            val user = User(city)
            CitySchema.validate(city)
            UserSchema.validate(user)
        }
    }) {
    class City(
        val users: List<User>,
    )

    class User(
        val city: City,
    )

    object CitySchema : ObjectSchema<City>({
        City::users { it.onEach(UserSchema) }
    })

    object UserSchema : ObjectSchema<User>({
        User::city { CitySchema }
    })
}
