package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec

class ObjectSchemaCircularReferenceTest :
    FunSpec({

        test("circular reference between schemas") {
            val city = City(emptyList())
            val user = User(city)
            validate { city.validate() }
            validate { user.validate() }
        }
    }) {
    class City(
        val users: List<User>,
    ) {
        context(_: Validation, _: Accumulate)
        fun validate() = schema { ::users { users -> onEach(users) { it.validate() } } }
    }

    class User(
        val city: City,
    ) {
        context(_: Validation, _: Accumulate)
        fun validate(): Unit = schema { ::city { it.validate() } }
    }
}
