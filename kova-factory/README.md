# kova-factory

Factory-based validation for transforming and validating raw input into typed objects.

## Overview

`kova-factory` extends Kova with a factory pattern that combines validation and object construction. Use it when you need to validate and transform raw input (like form data or API requests) before creating typed objects.

See the [main README](../README.md) for core validation concepts.

## Key Features

- **Type-safe construction with validation** - Transform raw strings into typed objects in one step
- **Property delegation** - Automatic path tracking for precise error reporting
- **Composable factories** - Nest factories to build complex object hierarchies

## Basic Usage

```kotlin
import org.komapper.extension.validator.*
import org.komapper.extension.validator.factory.*

data class User(val name: String, val age: Int)

context(_: Validation)
fun buildUser(name: String, age: String) = factory {
    val name by bind(name) {
        it.ensureNotBlank().ensureLengthAtLeast(1)
    }
    val age by bind(age) { it.transformToInt() }
    User(name, age)
}

val result = tryValidate { buildUser("Alice", "25") }
```

## API

### `factory { ... }`

Creates a factory context for building validated objects:

```kotlin
context(_: Validation)
fun buildUser(...) = factory {
    val field by bind(...) { /* validation */ }
    User(field)
}
```

### `bind(value) { block }`

Binds and validates a field. Returns the validated/transformed value:

```kotlin
val name by bind(rawName) {
    it.ensureNotBlank().ensureLengthAtLeast(1)  // Return validated value
}
```

Property names are automatically used for error paths.

### `bind { block }`

Binds a nested factory for composing validations:

```kotlin
val address by bind { buildAddress(...) }
```

## Type Transformations

Convert and validate raw strings into typed values:

```kotlin
data class Age(val value: Int)

context(_: Validation)
fun buildAge(ageString: String) = factory {
    val value by bind(ageString) {
        val age = it.transformToInt()  // String -> Int
        age.ensureAtLeast(0).ensureAtMost(120)
    }
    Age(value)
}
```

## Nested Factories

Compose multiple factories to build complex object hierarchies:

```kotlin
data class Name(val value: String)
data class FullName(val first: Name, val last: Name)
data class User(val id: Int, val fullName: FullName)

context(_: Validation)
fun buildName(value: String) = factory {
    val value by bind(value) {
        it.ensureNotBlank()
        it
    }
    Name(value)
}

context(_: Validation)
fun buildFullName(first: String, last: String) = factory {
    val first by bind { buildName(first) }
    val last by bind { buildName(last) }
    FullName(first, last)
}

context(_: Validation)
fun buildUser(id: String, firstName: String, lastName: String) = factory {
    val id by bind(id) { it.transformToInt() }
    val fullName by bind { buildFullName(firstName, lastName) }
    User(id, fullName)
}

// Error paths include full nesting: "fullName.first.value"
```

## Complete Example

```kotlin
data class User(val username: String, val email: String, val age: Int)

context(_: Validation)
fun buildUser(username: String, email: String, age: String) = factory {
    val username by bind(username) {
        it.ensureNotBlank().ensureLengthInRange(3..20).ensureMatches(Regex("^[a-zA-Z0-9_]+$"))
    }
    val email by bind(email) {
        it.ensureNotBlank().ensureContains("@").ensureLengthAtLeast(5)
    }
    val age by bind(age) {
        val ageInt = it.transformToInt()
        ageInt.ensureAtLeast(0).ensureAtMost(120)
    }
    User(username, email, age)
}
```

## See Also

- [Main README](../README.md) - Core validation concepts and configuration
- [example-factory](../example-factory) - Complete usage examples
