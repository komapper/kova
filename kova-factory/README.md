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

object UserFactory {
    operator fun invoke(
        name: String,
        age: String,
    ) = Kova.factory<User> {
        val name by bind(name) { it.min(1).notBlank() }
        val age by bind(age) { it.toInt().then { it.min(0).max(120) } }
        create { User(name(), age()) }
    }
}

// Usage - returns ValidationResult<User>
val result = UserFactory("Alice", "25").tryCreate()

// Or use create() for direct creation (throws ValidationException on failure)
val user = UserFactory("Alice", "25").create()
```

## Nested Factories

Factories can be composed to validate nested object structures:

```kotlin
data class Age(val value: Int)
data class Person(val name: String, val age: Age)

object AgeFactory {
    operator fun invoke(age: String) =
        Kova.factory<Age> {
            val value by bind(age) { it.toInt() }
            create { Age(value()) }
        }
}

object PersonFactory {
    operator fun invoke(
        name: String,
        age: String,
    ) = Kova.factory<Person> {
        val name by bind(name) { it.min(1).notBlank() }
        val age by bind(AgeFactory(age))
        create { Person(name(), age()) }
    }
}

// Usage
val result = PersonFactory("Alice", "30").tryCreate()
// Validation errors include full path: "name", "age.value"
```

## Integration with ObjectSchema

Combine factory-based input validation with schema-based property validation:

```kotlin
object UserSchema : ObjectSchema<User>({
    User::age { it.min(0).max(120) } // property validation
})

object UserFactory {
    operator fun invoke(
        name: String,
        age: String,
    ) = UserSchema.factory {
        val name by bind(name) { it.min(1).notBlank() } // input validation
        val age by bind(age) { it.toInt() }             // input validation
        create { User(name(), age()) }
    }
}

// Both input and property validations are enforced
val result = UserFactory("Alice", "130").tryCreate()
// Fails: age exceeds maximum of 120
```

## Pair and Triple Builders

For simple Pair/Triple construction with validation, use the specialized builders:

```kotlin
// Pair validation
val pairBuilder = PairFactoryBuilder(
    firstValidator = Kova.string().notBlank().max(10),
    secondValidator = Kova.int().positive()
)
val pairFactory = pairBuilder.build("hello", 42)
val result = pairFactory.tryCreate() // Success: Pair("hello", 42)

// Triple validation
val tripleBuilder = TripleFactoryBuilder(
    firstValidator = Kova.string().notBlank().max(50),
    secondValidator = Kova.int().min(0).max(120),
    thirdValidator = Kova.string().notBlank().max(100)
)
val tripleFactory = tripleBuilder.build("Alice", 30, "alice@example.com")
val result = tripleFactory.tryCreate() // Success: Triple("Alice", 30, "alice@example.com")

// Type transformation is supported
val transformBuilder = PairFactoryBuilder(
    firstValidator = Kova.string().toInt(),
    secondValidator = Kova.string().toInt()
)
val factory = transformBuilder.build("10", "20")
val result = factory.tryCreate() // Success: Pair(10, 20)
```

## API

### Factory Creation

- `Kova.factory<T> { ... }` - Create a factory for type T
- `IdentityValidator<T>.factory { ... }` - Create a factory with validator (e.g., ObjectSchema)
- `PairFactoryBuilder(firstValidator, secondValidator)` - Builder for validated Pair construction
- `TripleFactoryBuilder(firstValidator, secondValidator, thirdValidator)` - Builder for validated Triple construction

### Field Binding

- `FactoryScope.bind(value) { validator }` - Bind and validate a field using property delegation
- `FactoryScope.bind(factory)` - Bind a nested factory
- `FactoryScope.create { constructor }` - Build the final object

### Execution

- `Factory.tryCreate()` - Returns `ValidationResult<T>`
- `Factory.create()` - Returns `T` or throws `ValidationException`

## Error Reporting

Validation errors include detailed path information:

```kotlin
val result = PersonFactory("   ", "abc").tryCreate()
if (result.isFailure()) {
    result.messages.forEach { msg ->
        println("${msg.path.fullName}: ${msg.text}")
        // Output: "name: must not be blank"
        //         "age.value: must be a valid integer"
    }
}
```

## See Also

- [kova-core](../kova-core) - Core validation library
- [example-factory](../example-factory) - Complete usage examples
