# kova-factory

Factory-based validation for creating validated object instances.

## Overview

`kova-factory` provides a factory pattern for combining object construction and validation into a single operation. It's useful when you need to validate raw input (like strings) before converting them into typed objects.

## Key Features

- **Type-safe construction**: Validate and transform raw inputs before object creation
- **Composable factories**: Nest factories to build complex object hierarchies
- **Detailed error reporting**: Get validation errors with full path information
- **ObjectSchema integration**: Combine factory validation with schema-based property validation

## Basic Usage

```kotlin
data class User(val name: String, val age: Int)

fun createUser(rawName: String, rawAge: String): ValidationResult<User> {
    val userFactory = Kova.factory<User> {
        val name = check("name", rawName) { it.min(1).notBlank() }
        val age = check("age", rawAge) { it.toInt().min(0).max(120) }
        create { User(name(), age()) }
    }
    return userFactory.tryCreate()
}

// Usage
val result = createUser("Alice", "25")

// Or use create() for direct creation (throws ValidationException on failure)
fun createUserUnsafe(rawName: String, rawAge: String): User {
    return Kova.factory<User> {
        val name = check("name", rawName) { it.min(1).notBlank() }
        val age = check("age", rawAge) { it.toInt().min(0).max(120) }
        create { User(name(), age()) }
    }.create()
}
```

## Nested Factories

Factories can be composed to validate nested object structures:

```kotlin
data class Name(val value: String)
data class Person(val firstName: Name, val lastName: Name)

fun nameFactory(rawValue: String) = Kova.factory<Name> {
    val value = check("value", rawValue) { it.notBlank() }
    create { Name(value()) }
}

fun createPerson(rawFirstName: String, rawLastName: String): ValidationResult<Person> {
    val personFactory = Kova.factory<Person> {
        val firstName = check("firstName", nameFactory(rawFirstName))
        val lastName = check("lastName", nameFactory(rawLastName))
        create { Person(firstName(), lastName()) }
    }
    return personFactory.tryCreate()
}

// Usage
val result = createPerson("Alice", "Smith")
// Validation errors include full path: "firstName.value", "lastName.value"
```

## Integration with ObjectSchema

Combine factory-based input validation with schema-based property validation:

```kotlin
object UserSchema : ObjectSchema<User>({
    User::age { it.min(0).max(120) } // property validation
}) {
    fun tryCreate(name: String, age: String): ValidationResult<User> {
        return generateFactory {
            val name = check("name", name) { it.notBlank() }  // input validation
            val age = check("age", age) { it.toInt() }        // input validation
            create { User(name(), age()) }
        }.tryCreate()
    }
}

// Both input and property validations are enforced
val result = UserSchema.tryCreate("Alice", "130")
// Fails: age exceeds maximum of 120
```

## API

### Factory Creation

- `Kova.factory<T> { ... }` - Create a factory for type T
- `ObjectSchema.generateFactory { ... }` - Create a factory with schema validation

### Validation

- `FactoryScope.check(name, value) { validator }` - Validate a field
- `FactoryScope.check(name, factory)` - Register a nested factory
- `FactoryScope.create { constructor }` - Build the final object

### Execution

- `Factory.tryCreate()` - Returns `ValidationResult<T>`
- `Factory.create()` - Returns `T` or throws `ValidationException`

## Error Reporting

Validation errors include detailed path information:

```kotlin
val result = createPerson("", "")
if (result.isFailure()) {
    result.messages.forEach { msg ->
        println("${msg.path.fullName}: ${msg.text}")
        // Output: "firstName.value: must not be blank"
        //         "lastName.value: must not be blank"
    }
}
```

## See Also

- [kova-core](../kova-core) - Core validation library
- [example-factory](../example-factory) - Complete usage examples
