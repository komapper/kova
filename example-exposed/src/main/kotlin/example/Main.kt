package example

import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityChange
import org.jetbrains.exposed.v1.dao.EntityChangeType
import org.jetbrains.exposed.v1.dao.EntityClass
import org.jetbrains.exposed.v1.dao.EntityHook
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass
import org.jetbrains.exposed.v1.dao.toEntity
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.komapper.extension.validator.ObjectSchema
import org.komapper.extension.validator.ValidationException
import org.komapper.extension.validator.Validator
import org.komapper.extension.validator.max
import org.komapper.extension.validator.min
import org.komapper.extension.validator.notBlank
import org.komapper.extension.validator.notEmpty
import org.komapper.extension.validator.validate

object Cities : IntIdTable() {
    val name = varchar("name", 50)
}

object Users : IntIdTable() {
    val name = varchar("name", length = 50).index()
    val city = reference("city", Cities)
    val age = integer("age")
}

class City(
    id: EntityID<Int>,
) : IntEntity(id) {
    companion object : IntEntityClass<City>(Cities) {
        val hook = subscribe(Schema)
    }

    object Schema : ObjectSchema<City>({
        City::name { it.notEmpty() }
    })

    var name by Cities.name
    val users by User referrersOn Users.city
}

class User(
    id: EntityID<Int>,
) : IntEntity(id) {
    companion object : IntEntityClass<User>(Users) {
        val hook = subscribe(Schema)
    }

    object Schema : ObjectSchema<User>({
        User::name { it.min(1).notBlank() }
        User::age { it.min(0).max(120) }
    })

    var name by Users.name
    var city by City referencedOn Users.city
    var age by Users.age
}

fun <ID : Any, T : Entity<ID>> EntityClass<ID, T>.subscribe(validator: Validator<T, T>): (EntityChange) -> Unit {
    return EntityHook.subscribe { change ->
        if (change.changeType == EntityChangeType.Created &&
            change.entityClass == this
        ) {
            val entity = change.toEntity(this) ?: return@subscribe
            validator.validate(entity)
        }
    }
}

fun main() {
    Database.connect(
        url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;",
        driver = "org.h2.Driver",
    )

    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create(Cities, Users)
    }

    try {
        success()
    } catch (_: Exception) {
        assert(false) { "Validation should not fail" }
    }

    try {
        failWithInvalidCity()
        assert(false) { "Validation should fail" }
    } catch (e: ValidationException) {
        println("Validation failed:")
        e.messages.joinToString("\n").let { println(it) }
        // Message(constraintId=kova.charSequence.notEmpty, text='must not be empty', root=example.City, path=name, input=, args=[])
    }

    try {
        failWithInvalidUser()
        assert(false) { "Validation should fail" }
    } catch (e: ValidationException) {
        println("Validation failed:")
        e.messages.joinToString("\n").let { println(it) }
        // Message(constraintId=kova.charSequence.min, text='must be at least 1 characters', root=example.User, path=name, input=, args=[(length, 1)])
        // Message(constraintId=kova.charSequence.notBlank, text='must not be blank', root=example.User, path=name, input=, args=[])
        // Message(constraintId=kova.comparable.min, text='must be greater than or equal to 0', root=example.User, path=age, input=-1, args=[(value, 0)])
    }
}

private fun success() {
    transaction {
        addLogger(StdOutSqlLogger)

        val saintPetersburg =
            City.new {
                name = "St. Petersburg"
            }

        User.new {
            name = "Andrey"
            city = saintPetersburg
            age = 5
        }

        println("Cities: ${City.all().joinToString { it.name }}")
        println("Users in ${saintPetersburg.name}: ${saintPetersburg.users.joinToString { it.name }}")

        rollback()
    }
}

private fun failWithInvalidCity() {
    transaction {
        addLogger(StdOutSqlLogger)

        val saintPetersburg =
            City.new {
                name = "" // invalid name
            }

        User.new {
            name = "Andrey"
            city = saintPetersburg
            age = 5
        }

        println("Cities: ${City.all().joinToString { it.name }}")
        println("Users in ${saintPetersburg.name}: ${saintPetersburg.users.joinToString { it.name }}")

        rollback()
    }
}

private fun failWithInvalidUser() {
    transaction {
        addLogger(StdOutSqlLogger)

        val saintPetersburg =
            City.new {
                name = "St. Petersburg"
            }

        User.new {
            name = "" // invalid name
            city = saintPetersburg
            age = -1 // invalid age
        }

        println("Cities: ${City.all().joinToString { it.name }}")
        println("Users in ${saintPetersburg.name}: ${saintPetersburg.users.joinToString { it.name }}")

        rollback()
    }
}
