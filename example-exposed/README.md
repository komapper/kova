# Kova + Exposed Integration Example

This example demonstrates how to integrate **Kova** validation with **JetBrains Exposed** ORM using entity hooks.

## Overview

The example shows how to automatically validate Exposed DAO entities when they are created, using Kova's `ObjectSchema` and Exposed's `EntityHook` mechanism.

## Key Components

### Entity Schemas

- **City.Schema**: Validates that city name is not empty
- **User.Schema**: Validates that user name is not blank (min 1 character) and age is between 0-120

### Hook Mechanism

The `subscribe()` extension function registers a Kova validator with Exposed's EntityHook:

```kotlin
fun <ID : Any, T : Entity<ID>> EntityClass<ID, T>.subscribe(validator: Validator<T, T>): (EntityChange) -> Unit
```

This hook is triggered on entity creation and validates the entity using the provided schema. If validation fails, a `ValidationException` is thrown with detailed error messages.

## Running the Example

```bash
./gradlew example-exposed:run
```

The main function demonstrates three scenarios:

1. **Success**: Valid city and user creation
2. **Invalid City**: Empty city name triggers validation error
3. **Invalid User**: Empty user name and negative age trigger multiple validation errors

## Output

The example prints validation errors with detailed information:

```
Message(constraintId=kova.charSequence.notEmpty, text='must not be empty', root=example.City, path=name, input=, args=[])

Message(constraintId=kova.charSequence.min, text='must be at least 1 characters', root=example.User, path=name, input=, args=[(length, 1)])
Message(constraintId=kova.charSequence.notBlank, text='must not be blank', root=example.User, path=name, input=, args=[])
Message(constraintId=kova.comparable.min, text='must be greater than or equal to 0', root=example.User, path=age, input=-1, args=[(value, 0)])
```

## Dependencies

- Kova Core
- Exposed DAO & JDBC
- H2 Database (in-memory)
