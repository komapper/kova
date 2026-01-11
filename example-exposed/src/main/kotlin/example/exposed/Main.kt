package example.exposed

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
import org.komapper.extension.validator.Validation
import org.komapper.extension.validator.ValidationException
import org.komapper.extension.validator.ensureInRange
import org.komapper.extension.validator.ensureLengthAtLeast
import org.komapper.extension.validator.ensureNotBlank
import org.komapper.extension.validator.ensureNotEmpty
import org.komapper.extension.validator.schema
import org.komapper.extension.validator.validate

/**
 * Database table definition for Cities.
 * Each city has an auto-incrementing ID and a name.
 */
object Cities : IntIdTable() {
    val name = varchar("name", 50)
}

/**
 * Database table definition for Users.
 * Each user has an auto-incrementing ID, indexed name, city reference, and age.
 */
object Users : IntIdTable() {
    val name = varchar("name", length = 50).index()
    val city = reference("city", Cities)
    val age = integer("age")
}

/**
 * City entity class.
 * The companion object subscribes to entity creation events to automatically validate cities.
 */
class City(
    id: EntityID<Int>,
) : IntEntity(id) {
    var name by Cities.name
    val users by User referrersOn Users.city

    companion object : IntEntityClass<City>(Cities) {
        init {
            // Automatically validate cities when they are created
            subscribe { it.validate() }
        }
    }
}

/**
 * User entity class.
 * The companion object subscribes to entity creation events to automatically validate users.
 */
class User(
    id: EntityID<Int>,
) : IntEntity(id) {
    var name by Users.name
    var city by City referencedOn Users.city
    var age by Users.age

    companion object : IntEntityClass<User>(Users) {
        init {
            // Automatically validate users when they are created
            subscribe { it.validate() }
        }
    }
}

/**
 * Validation schema for City.
 * Validates name is not empty.
 */
context(_: Validation)
fun City.validate() =
    schema {
        ::name { it.ensureNotEmpty() }
    }

/**
 * Validation schema for User.
 * Validates name (minimum length 1, not blank) and age (0-120).
 */
context(_: Validation)
fun User.validate() =
    schema {
        ::name { it.ensureLengthAtLeast(1).ensureNotBlank() }
        ::age { it.ensureInRange(0..120) }
    }

/**
 * Hooks Kova validation into Exposed's entity lifecycle.
 * Validates entities on creation and throws ValidationException if validation fails.
 */
@IgnorableReturnValue
fun <ID : Any, T : Entity<ID>> EntityClass<ID, T>.subscribe(validate: context(Validation)(T) -> Unit): (EntityChange) -> Unit =
    EntityHook.subscribe { change ->
        if (change.changeType == EntityChangeType.Created &&
            change.entityClass == this
        ) {
            val entity = change.toEntity(this) ?: return@subscribe
            validate { validate(entity) }
        }
    }

/**
 * Demonstrates Kova validation integration with Exposed ORM.
 * Shows successful validation and two failure scenarios (invalid city and invalid user).
 */
fun main() {
    // Connect to an in-memory H2 database for testing
    Database.connect(
        url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;",
        driver = "org.h2.Driver",
    )

    // Create database tables
    println("\n# Create tables")
    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create(Cities, Users)
    }

    // Example 1: Successful validation
    println("\n# Example 1: Successful validation")
    try {
        success()
    } catch (_: Exception) {
        assert(false) { "Validation should not fail" }
    }

    // Example 2: Validation failure - invalid city (empty name)
    println("\n# Example 2: Validation failure - invalid city (empty name)")
    try {
        failWithInvalidCity()
        assert(false) { "Validation should fail" }
    } catch (e: ValidationException) {
        println("## Validation failed:")
        e.messages.joinToString("\n").let { println(it) }
    }

    // Example 3: Validation failure - invalid user (blank name, negative age)
    println("\n# Example 3: Validation failure - invalid user (blank name, negative age)")
    try {
        failWithInvalidUser()
        assert(false) { "Validation should fail" }
    } catch (e: ValidationException) {
        println("## Validation failed:")
        e.messages.joinToString("\n").let { println(it) }
    }
}

/**
 * Demonstrates successful entity creation with valid data.
 */
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

/**
 * Demonstrates validation failure with invalid city (empty name).
 * ValidationException is thrown before persistence.
 */
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

/**
 * Demonstrates validation failure with invalid user (blank name, negative age).
 * ValidationException is thrown with multiple error messages before persistence.
 */
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
