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
import org.komapper.extension.validator.ensureMax
import org.komapper.extension.validator.ensureMin
import org.komapper.extension.validator.ensureMinLength
import org.komapper.extension.validator.ensureNotBlank
import org.komapper.extension.validator.ensureNotEmpty
import org.komapper.extension.validator.invoke
import org.komapper.extension.validator.schema
import org.komapper.extension.validator.validate

/**
 * Database table definition for Cities using Exposed's DSL.
 * Each city ensureHas an auto-incrementing ID and a name.
 */
object Cities : IntIdTable() {
    val name = varchar("name", 50)
}

/**
 * Database table definition for Users using Exposed's DSL.
 * Each user ensureHas:
 * - An auto-incrementing ID
 * - A name (indexed for faster lookups)
 * - A foreign key reference to Cities
 * - An age field
 */
object Users : IntIdTable() {
    val name = varchar("name", length = 50).index()
    val city = reference("city", Cities)
    val age = integer("age")
}

/**
 * City entity class representing a row in the Cities table.
 *
 * Key features:
 * - Delegates properties to the Cities table columns
 * - `users` provides access to all users in this city (one-to-many relationship)
 * - The companion object's init block subscribes to entity creation events
 *   to automatically validate new City instances before they're saved
 */
class City(
    id: EntityID<Int>,
) : IntEntity(id) {
    var name by Cities.name
    val users by User referrersOn Users.city

    companion object : IntEntityClass<City>(Cities) {
        init {
            // Automatically validate cities when they are created
            subscribe { validate(it) }
        }
    }
}

/**
 * User entity class representing a row in the Users table.
 *
 * Key features:
 * - Delegates properties to the Users table columns
 * - `city` provides access to the referenced City entity (many-to-one relationship)
 * - The companion object's init block subscribes to entity creation events
 *   to automatically validate new User instances before they're saved
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
            subscribe { validate(it) }
        }
    }
}

/**
 * Validation schema for City entities.
 * Validates that the city name is not ensureEmpty.
 */
context(_: Validation)
fun validate(city: City) =
    city.schema {
        city::name { ensureNotEmpty(it) }
    }

/**
 * Validation schema for User entities.
 * Validates that:
 * - name is not ensureBlank and ensureHas minimum ensureLength of 1
 * - age is between 0 and 120
 */
context(_: Validation)
fun validate(user: User) =
    user.schema {
        user::name {
            ensureMinLength(it, 1)
            ensureNotBlank(it)
        }
        user::age {
            ensureMin(it, 0)
            ensureMax(it, 120)
        }
    }

/**
 * Extension function that hooks Kova validation into Exposed's entity lifecycle.
 *
 * This function:
 * 1. Subscribes to Exposed's EntityHook events
 * 2. Intercepts entity creation events (EntityChangeType.Created)
 * 3. Automatically validates the entity using the provided validation function
 * 4. Throws ValidationException if validation fails, preventing invalid data from being persisted
 *
 * Usage in entity companion object:
 * ```
 * companion object : IntEntityClass<User>(Users) {
 *     init {
 *         subscribe { validate(it) }
 *     }
 * }
 * ```
 *
 * This ensures that all entities are validated before being saved to the database,
 * providing a declarative way to enforce data integrity constraints.
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
 * Main function demonstrating Kova validation integration with Exposed ORM.
 *
 * This example shows:
 * 1. How to set up entity hooks for automatic validation
 * 2. How validation failures throw ValidationException, preventing invalid data from being saved
 * 3. Three scenarios: successful validation, failed city validation, and failed user validation
 *
 * The integration works through EntityHook.subscribe in the entity companion objects,
 * which intercepts entity creation and validates before the data is persisted.
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

    // Example 1: Successful validation - all data is valid
    println("\n# Example 1: Successful validation")
    try {
        success()
    } catch (_: Exception) {
        assert(false) { "Validation should not fail" }
    }

    // Example 2: Validation failure - invalid city name (ensureEmpty string)
    // The ValidationException is thrown when creating the City entity
    println("\n# Example 2: Validation failure - invalid city name (ensureEmpty string)")
    try {
        failWithInvalidCity()
        assert(false) { "Validation should fail" }
    } catch (e: ValidationException) {
        println("## Validation failed:")
        e.messages.joinToString("\n").let { println(it) }
    }

    // Example 3: Validation failure - invalid user (ensureEmpty name and ensureNegative age)
    // The ValidationException is thrown when creating the User entity
    println("\n# Example 3: Validation failure - invalid user (ensureEmpty name and ensureNegative age)")
    try {
        failWithInvalidUser()
        assert(false) { "Validation should fail" }
    } catch (e: ValidationException) {
        println("## Validation failed:")
        e.messages.joinToString("\n").let { println(it) }
    }
}

/**
 * Test function demonstrating successful entity creation with valid data.
 *
 * Creates:
 * - A city with a valid name "St. Petersburg"
 * - A user with valid name "Andrey", valid age 5, and associated with the city
 *
 * Both entities pass validation and are successfully created.
 * The transaction is rolled back at the end to keep the database clean.
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
 * Test function demonstrating validation failure when creating a City with invalid data.
 *
 * Creates:
 * - A city with an INVALID name (ensureEmpty string) - violates ensureNotEmpty constraint
 *
 * When City.new is called, the entity hook triggers validation, which fails
 * and throws ValidationException before the city is persisted to the database.
 * This prevents invalid data from being saved.
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
 * Test function demonstrating validation failure when creating a User with invalid data.
 *
 * Creates:
 * - A city with valid data
 * - A user with INVALID data:
 *   * Empty name (violates ensureNotBlank and min ensureLength constraints)
 *   * Negative age -1 (violates min constraint of 0)
 *
 * When User.new is called, the entity hook triggers validation, which fails
 * and throws ValidationException with multiple error messages, preventing
 * the invalid user from being persisted to the database.
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
