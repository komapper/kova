# Exposed ORM Integration Example

This example demonstrates how to integrate Kova validation with [JetBrains Exposed](https://github.com/JetBrains/Exposed) ORM using entity hooks for automatic validation.

## Overview

Shows how to automatically validate Exposed DAO entities when they are created, preventing invalid data from being persisted to the database. Uses Kova's schema validation with Exposed's `EntityHook` mechanism to intercept entity creation and validate before saving.

See the [main README](../README.md) for core validation concepts.

## Key Features

- **Automatic validation on entity creation** - Validates entities before they're persisted
- **EntityHook integration** - Uses Exposed's lifecycle hooks to trigger validation
- **Prevents invalid data** - Throws `ValidationException` if validation fails

## How It Works

### Entity Validation

Define validation schemas for your entities:

```kotlin
fun Validation.validate(city: City) = city.schema {
    city::name { notEmpty(it) }
}

fun Validation.validate(user: User) = user.schema {
    user::name { minLength(it, 1); notBlank(it) }
    user::age { min(it, 0); max(it, 120) }
}
```

See [Object Validation](../README.md#object-validation) in the main README for more about schema validation.

### Hook Registration

Register validation hooks in entity companion objects:

```kotlin
class City(id: EntityID<Int>) : IntEntity(id) {
    var name by Cities.name

    companion object : IntEntityClass<City>(Cities) {
        init {
            subscribe { validate(it) }
        }
    }
}
```

The `subscribe()` extension function integrates Kova with Exposed's EntityHook:

```kotlin
fun <ID : Any, T : Entity<ID>> EntityClass<ID, T>.subscribe(
    validate: Validation.(T) -> Unit
): (EntityChange) -> Unit
```

This hook:
1. Intercepts entity creation events
2. Validates the entity using the provided schema
3. Throws `ValidationException` if validation fails, preventing the entity from being saved

## Running the Example

```bash
./gradlew example-exposed:run
```

The example demonstrates three scenarios:

1. **Success**: Valid city and user creation
2. **Invalid City**: Empty city name triggers validation error
3. **Invalid User**: Empty name and negative age trigger multiple validation errors

## See Also

- [Main README](../README.md) - Core validation concepts and available validators
- [Exposed Documentation](https://github.com/JetBrains/Exposed) - JetBrains Exposed ORM
