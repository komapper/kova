# kova-factory

Factory-based validation for creating validated object instances.

## Overview

`kova-factory` provides a factory pattern for combining object construction and validation into a single operation. It's useful when you need to validate raw input (like strings) before converting them into typed objects.

## Key Features

- **Type-safe construction**: Validate and transform raw inputs before object creation
- **Composable factories**: Nest factories to build complex object hierarchies
- **Detailed error reporting**: Get validation errors with full path information
- **Property delegation**: Automatic path tracking using Kotlin property delegation

## Basic Usage

```kotlin
import org.komapper.extension.validator.*
import org.komapper.extension.validator.factory.*

data class User(val name: String, val age: Int)

fun Validation.buildUser(name: String, age: String) =
    factory {
        val name by bind(name) {
            notBlank(it)
            minLength(it, 1)
            it
        }
        val age by bind(age) { toInt(it) }
        User(name, age)
    }

// Usage - returns ValidationResult<User>
val result = tryValidate { buildUser("Alice", "25") }

// Or use validate() for direct creation (throws ValidationException on failure)
val user = validate { buildUser("Alice", "25") }
```

## Nested Factories

Factories can be composed to validate nested object structures:

```kotlin
data class Name(val value: String)
data class FullName(val first: Name, val last: Name)
data class User(val id: Int, val fullName: FullName)

fun Validation.buildName(value: String) =
    factory {
        val value by bind(value) {
            notBlank(it)
            it
        }
        Name(value)
    }

fun Validation.buildFullName(first: String, last: String) =
    factory {
        val first by bind { buildName(first) }
        val last by bind { buildName(last) }
        FullName(first, last)
    }

fun Validation.buildUser(id: String, firstName: String, lastName: String) =
    factory {
        val id by bind(id) { toInt(it) }
        val fullName by bind { buildFullName(firstName, lastName) }
        User(id, fullName)
    }

// Usage
val result = tryValidate { buildUser("1", "Alice", "Smith") }
// Validation errors include full path: "id", "fullName.first.value", "fullName.last.value"
```

## API

### Factory Creation

- `Validation.factory { ... }` - Create a factory context for building validated objects

### Field Binding

- `bind(value) { block }` - Bind and validate a field using property delegation
  - The block receives the input value as `it`
  - Must return the validated/transformed value
  - Property name is automatically used as the validation path

- `bind { block }` - Bind a nested factory call
  - Used for composing factory functions
  - The block should return the result of another factory call

### Execution

- `tryValidate { factory() }` - Returns `ValidationResult<T>`
- `validate { factory() }` - Returns `T` or throws `ValidationException`

## Error Reporting

Validation errors include detailed path information using property names:

```kotlin
val result = tryValidate { buildUser("1", "", "abc") }
if (result.isFailure()) {
    result.messages.forEach { msg ->
        println("${msg.path.fullName}: ${msg.text}")
        // Output: "fullName.first.value: must not be blank"
    }
}
```

## Advanced Usage

### With Validation Config

```kotlin
val result = tryValidate(config = ValidationConfig(failFast = true)) {
    buildUser("1", "", "")
}
// Only first error is reported due to failFast mode
```

### Type Transformations

The factory pattern naturally supports type transformations:

```kotlin
data class Age(val value: Int)

fun Validation.buildAge(ageString: String) =
    factory {
        val value by bind(ageString) {
            val age = toInt(it)  // String -> Int transformation
            min(age, 0)
            max(age, 120)
            age
        }
        Age(value)
    }

val result = tryValidate { buildAge("25") }
// Success: Age(25)
```

### Combining Multiple Validations

```kotlin
data class User(val username: String, val email: String, val age: Int)

fun Validation.buildUser(username: String, email: String, age: String) =
    factory {
        val username by bind(username) {
            notBlank(it)
            minLength(it, 3)
            maxLength(it, 20)
            matches(it, Regex("^[a-zA-Z0-9_]+$"))
            it
        }
        val email by bind(email) {
            notBlank(it)
            contains(it, "@")
            minLength(it, 5)
            it
        }
        val age by bind(age) {
            val ageInt = toInt(it)
            min(ageInt, 0)
            max(ageInt, 120)
            ageInt
        }
        User(username, email, age)
    }
```

## See Also

- [kova-core](../kova-core) - Core validation library
- [example-factory](../example-factory) - Complete usage examples
