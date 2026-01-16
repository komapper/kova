# Kova

[![Build](https://github.com/komapper/kova/actions/workflows/ci.yml/badge.svg)](https://github.com/komapper/kova/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.0+-purple.svg)](https://kotlinlang.org)

A type-safe Kotlin validation library with composable validators and detailed error reporting.

> **⚠️ Note**: This project is currently under active development. The API may change until a stable 1.0.0 release.

## Table of Contents

- [Quick Start](#quick-start)
- [Setup](#setup)
- [Why Kova?](#why-kova)
- [Features](#features)
- [Running Validation](#running-validation)
- [Property Validation](#property-validation)
- [Argument Validation](#argument-validation)
- [Available Validators](#available-validators)
- [Error Handling](#error-handling)
- [Validation Configuration](#validation-configuration)
- [Advanced Topics](#advanced-topics)
- [Examples](#examples)
- [FAQ](#faq)
- [License](#license)
- [Powered by](#powered-by)

## Quick Start

Kova provides two validation approaches: **property validation** with `schema` and **argument validation** with `capture`.

### Property Validation with `schema`

Validate properties of an existing object:

```kotlin
import org.komapper.extension.validator.*

data class User(val name: String, val age: Int)

context(_: Validation)
fun User.validate() = schema {
    ::name { it.ensureNotBlank().ensureLengthAtMost(100) }
    ::age { it.ensureInRange(0..150) }
}

val result = tryValidate { User("Alice", 25).validate() }
```

### Argument Validation with `capture`

Validate and transform function arguments:

```kotlin
context(_: Validation)
fun buildUser(rawName: String, rawAge: String): User {
    val name by capture { rawName.ensureNotBlank() }
    val age by capture { rawAge.transformToInt().ensurePositive() }
    return User(name, age)
}

val result = tryValidate { buildUser("Alice", "25") }
```

### Error Handling

Both approaches collect all errors and provide detailed path information:

```kotlin
val result = tryValidate { buildUser("", "invalid") }
if (result.isFailure()) { 
    result.messages.forEach { println("${it.path}: ${it.text}") }
    // name: must not be blank
    // age: must be a valid integer
}
```

> **Note**: Validators require `context(_: Validation)`. This uses Kotlin's [context parameters](https://kotlinlang.org/docs/whatsnew2020.html#context-parameters), which must be enabled (see [Setup](#setup)).

## Setup

Add Kova to your Gradle project:

```kotlin
dependencies {
    // Core validation library
    implementation("org.komapper:kova-core:0.1.0")

    // Ktor integration (optional)
    implementation("org.komapper:kova-ktor:0.1.0")
}
```

Enable Kotlin's context parameters feature:

```kotlin
kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}
```

### Requirements

- Kotlin 2.3.0+
- Java 17+
- Gradle 8.14+

## Why Kova?

Most validation libraries only support **property validation** — validating an object after it's constructed. Kova goes further by also supporting **argument validation** — validating and transforming function arguments to construct typed objects from raw input.

This dual approach makes Kova ideal for scenarios like form processing, API request handling, and data import where you need to validate raw strings and convert them into domain objects.

| Feature                       | Kova                    | Hibernate Validator              | Konform                 |
|-------------------------------|-------------------------|----------------------------------|-------------------------|
| **Approach**                  | Function-based          | Annotation-based                 | DSL-based               |
| **Kotlin-native**             | Yes                     | No (`@field:` required)          | Yes                     |
| **Property validation**       | Yes (`schema`)          | Yes                              | Yes                     |
| **Argument validation**       | Yes (`capture`)         | No                               | No                      |
| **Custom validators**         | Simple (functions)      | Complex (annotation + class)     | Simple (lambdas)        |
| **Cross-property validation** | Simple (`constrain`)    | Complex (class-level constraint) | Simple                  |
| **Metadata API**              | No                      | Yes                              | No                      |
| **Validation groups**         | Via function parameters | Built-in                         | Via separate validators |
| **Multiplatform**             | JVM                     | JVM                              | JVM, JS, Native, Wasm   |
| **Dependencies**              | Zero                    | Many                             | Zero                    |
| **Standard compliance**       | —                       | JSR-380 (Jakarta Validation)     | —                       |
| **Framework integration**     | Ktor                    | Spring, Jakarta EE, CDI          | —                       |

### When to Choose Each

**Choose Kova when you:**
- Need **property validation** for existing objects (`schema`)
- Need **argument validation** to build typed objects from raw input (`capture`)
- Prefer composing validators as regular Kotlin functions
- Want a lightweight, zero-dependency solution for Kotlin

**Choose Hibernate Validator when you:**
- Need JSR-380 (Jakarta Validation) standard compliance
- Use Spring Boot, Jakarta EE, or other integrated frameworks
- Want to introspect constraints via Metadata API (form generation, documentation)
- Need validation groups for context-dependent rules

**Choose Konform when you:**
- Need Kotlin Multiplatform support (JS, Native, Wasm)
- Prefer DSL-style validation definitions

For detailed code comparisons with Hibernate Validator and Konform, see **[docs/COMPARISON.md](docs/COMPARISON.md)**.

## Features

- **Type-Safe**: Leverages Kotlin's type system for compile-time safety
- **Composable**: Build complex validation logic by composing reusable validation functions, chaining constraints, and using conditional operators (`or`, `orElse`)
- **Immutable**: All validators are immutable and thread-safe
- **Detailed Error Reporting**: Get precise error messages with path tracking for nested validations
- **Internationalization**: Built-in support for localized error messages
- **Fail-Fast Support**: Option to stop validation at the first error or collect all errors
- **Zero Dependencies**: No external runtime dependencies, only requires Kotlin standard library

## Running Validation

Kova provides two functions to execute validation: `tryValidate` and `validate`.

### tryValidate

Returns a `ValidationResult<T>` that can be either `Success` or `Failure`. This is the recommended approach when you want to handle validation results programmatically:

```kotlin
val result = tryValidate { user.validate() }

// Pattern matching with when expression
when (result) {
    is ValidationResult.Success -> println("Valid: ${result.value}")
    is ValidationResult.Failure -> println("Errors: ${result.messages}")
}

// Or use isSuccess() / isFailure() with smart casting
if (result.isSuccess()) {
    // result is smart-cast to Success
    println("Valid: ${result.value}")
} else {
    // result is smart-cast to Failure
    println("Errors: ${result.messages}")
}
```

### validate

Returns the validated value directly, or throws `ValidationException` on failure. Use this when you prefer exception-based error handling:

```kotlin
try {
    val user = validate { buildUser("Alice", "25") }
    println("Created: $user")
} catch (e: ValidationException) {
    e.messages.forEach { println("${it.path}: ${it.text}") }
}
```

### ValidationResult

A sealed interface with two subtypes:

| Type         | Properties                | Description                        |
|--------------|---------------------------|------------------------------------|
| `Success<T>` | `value: T`                | Contains the validated value       |
| `Failure`    | `messages: List<Message>` | Contains validation error messages |

Helper methods with Kotlin contracts for smart casting:

- `isSuccess()` - Returns `true` if `Success`, enables smart cast
- `isFailure()` - Returns `true` if `Failure`, enables smart cast

### ValidationException

A `RuntimeException` thrown by `validate` when validation fails:

| Property   | Type            | Description                       |
|------------|-----------------|-----------------------------------|
| `messages` | `List<Message>` | List of validation error messages |

## Property Validation

Validate object properties using the `schema` function.

```kotlin
data class Product(val id: Int, val name: String, val price: Double)

context(_: Validation)
fun Product.validate() = schema {
    ::id { it.ensureAtLeast(1) }
    ::name { it.ensureNotBlank().ensureLengthInRange(1..100) }
    ::price { it.ensureAtLeast(0.0) }
}

val result = tryValidate { Product(1, "Mouse", 29.99).validate() }
```

### Reusable Validators

Extract common validation logic into reusable validator functions:

```kotlin
context(_: Validation)
fun validateName(name: String, maxLength: Int = 100): String {
    return name.ensureNotBlank().ensureLengthInRange(1..maxLength)
}

context(_: Validation)
fun validatePrice(price: Double): Double {
    return price.ensureAtLeast(0.0).ensureAtMost(1000000.0)
}

data class Product(val id: Int, val name: String, val price: Double)
data class Service(val id: Int, val title: String, val price: Double)

context(_: Validation)
fun Product.validate() = schema {
    ::id { it.ensurePositive() }
    ::name { validateName(it) }
    ::price { validatePrice(it) }
}

context(_: Validation)
fun Service.validate() = schema {
    ::id { it.ensurePositive() }
    ::title { validateName(it, 200) }
    ::price { validatePrice(it) }
}
```

Reusable validators can be shared across multiple schemas, ensuring consistent validation rules throughout your application.

Validators can accept parameters to make them more flexible. By using default parameter values, you can provide sensible defaults while allowing customization when needed. In the example above, `validateName` uses a default `maxLength` of 100, but the `Service.title` validation overrides it to 200, demonstrating how the same validator can be adapted to different requirements.

### Nested Property Validation

Validate nested objects by calling their validation functions within the parent's schema. Error messages include the full path to the failed property:

```kotlin
data class Address(val street: String, val city: String, val zipCode: String)
data class Customer(val name: String, val email: String, val address: Address)

context(_: Validation)
fun Address.validate() = schema {
    ::street { it.ensureNotBlank().ensureLengthAtLeast(1) }
    ::city { it.ensureNotBlank().ensureLengthAtLeast(1) }
    ::zipCode { it.ensureMatches(Regex("^\\d{5}(-\\d{4})?$")) }
}

context(_: Validation)
fun Customer.validate() = schema {
    ::name { it.ensureNotBlank().ensureLengthInRange(1..100) }
    ::email { it.ensureNotBlank().ensureContains("@") }
    ::address { it.validate() }  // Nested property validation
}

val customer = Customer(
    name = "John Doe",
    email = "invalid-email",
    address = Address(street = "", city = "Tokyo", zipCode = "123")
)

val result = tryValidate { customer.validate() }
```

Notice how the error messages show the full path (e.g., `address.street`, `address.zipCode`) to pinpoint exactly where validation failed in the nested structure.

### Cross-Property Validation

Validates relationships between multiple properties using `constrain` within a `schema` block:

```kotlin
data class PriceRange(val minPrice: Double, val maxPrice: Double)

context(_: Validation)
fun PriceRange.validate() = schema {
    ::minPrice { it.ensureNotNegative() }
    ::maxPrice { it.ensureNotNegative() }

    // Validate relationship
    constrain("priceRange") {
        satisfies(it.minPrice <= it.maxPrice) {
            text("minPrice must be less than or equal to maxPrice")
        }
    }
}

val result = tryValidate { PriceRange(10.0, 100.0).validate() }
```

## Argument Validation

Use the `capture` function to validate and transform function arguments. This is useful for handling form data, API requests, or any scenario where you need to convert and validate external input.

### Basic Usage

The `capture` function uses property delegation to validate values with automatic error path naming:

```kotlin
data class Product(val id: Int, val name: String, val price: Double)

context(_: Validation)
fun buildProduct(rawId: String, rawName: String, rawPrice: String): Product {
    val id by capture { rawId.transformToInt().ensurePositive() }
    val name by capture { rawName.ensureNotBlank().ensureLengthInRange(1..100) }
    val price by capture { rawPrice.transformToDouble().ensurePositive() }
    return Product(id, name, price)
}

// Valid input
val result = tryValidate { buildProduct("1", "Mouse", "29.99") }
// result.value = Product(id=1, name="Mouse", price=29.99)

// Invalid input - collects all errors
val result = tryValidate { buildProduct("", "", "invalid") }
// Errors: id, name, price
```

### Nested Object Creation

Compose builder functions to create complex object hierarchies. Error paths automatically include the full nesting structure:

```kotlin
data class Name(val value: String)
data class FullName(val first: Name, val last: Name)
data class Person(val id: Int, val fullName: FullName)

context(_: Validation)
fun buildName(value: String): Name {
    val value by capture { value.ensureNotBlank() }
    return Name(value)
}

context(_: Validation)
fun buildFullName(first: String, last: String): FullName {
    val first by capture { buildName(first) }  // Nested builder call
    val last by capture { buildName(last) }
    return FullName(first, last)
}

context(_: Validation)
fun buildPerson(id: String, firstName: String, lastName: String): Person {
    val id by capture { id.transformToInt() }
    val fullName by capture { buildFullName(firstName, lastName) }
    return Person(id, fullName)
}

// Nested error paths
val result = tryValidate { buildPerson("1", "", "") }
// Errors:
//   fullName.first.value -> "must not be blank"
//   fullName.last.value -> "must not be blank"
```

### Combining with Property Validation

You can combine argument validation (`capture`) with property validation (`schema`):

```kotlin
data class User(val name: String, val age: Int)

context(_: Validation)
fun User.validate() = schema {
    ::age { it.ensureInRange(0..120) }
}

context(_: Validation)
fun buildUser(rawName: String, rawAge: String): User {
    val name by capture { rawName.ensureNotBlank() }
    val age by capture { rawAge.transformToInt() }
    return User(name, age).also { it.validate() }  // Property validation
}

// Invalid: age "130" passes transformToInt() but fails ensureInRange(0..120)
val result = tryValidate { buildUser("Alice", "130") }
// Error: age -> "must be within range 0..120"
```

## Available Validators

Kova provides validators for many types including String, Numbers, Temporal types, Collections, Maps, and more.

For the complete list, see **[docs/VALIDATORS.md](docs/VALIDATORS.md)**.

## Error Handling

Each validation error is represented by a `Message` object:

| Property       | Type     | Description                                                       |
|----------------|----------|-------------------------------------------------------------------|
| `text`         | `String` | Formatted error message text                                      |
| `constraintId` | `String` | Unique constraint identifier (e.g., `kova.charSequence.notBlank`) |
| `path`         | `Path`   | Path to the value (e.g., `address.zipCode`)                       |
| `input`        | `Any?`   | The actual value that failed validation                           |
| `root`         | `String` | Root object class name (for property validation)                  |

```kotlin
if (!result.isSuccess()) {
    result.messages.forEach { message ->
        println("${message.path}: ${message.text}")           // name: must not be blank
        println("Constraint: ${message.constraintId}")        // kova.charSequence.notBlank
    }
}
```

### Custom Error Messages

All validators accept an optional `message` parameter for custom error messages. You can use `text()` for plain text messages or `resource()` for internationalized messages:

```kotlin
val result = tryValidate {
    // Custom text message
    username.ensureLengthAtLeast(3, message = { text("Username must be at least 3 characters") })

    // Internationalized message with parameters
    bio.ensureLengthAtMost(500, message = { "custom.bio.tooLong".resource(500) })
}
```

## Validation Configuration

Customize validation behavior using `ValidationConfig`:

```kotlin
val result = tryValidate(ValidationConfig(
    failFast = true,    // Stop at first error (default: false)
    clock = fixedClock, // Custom clock for temporal validators
    logger = { println(it) }  // Debug logging
)) {
    user.validate()
}
```

For detailed configuration options, see **[docs/CONFIGURATION.md](docs/CONFIGURATION.md)**.

## Advanced Topics

- **[docs/ADVANCED.md](docs/ADVANCED.md)** - Custom constraints, nullable validation, conditional validation (`or`/`orElse`), error wrapping, circular reference detection, and internationalization
- **[docs/ERROR_ACCUMULATION.md](docs/ERROR_ACCUMULATION.md)** - How error accumulation works internally
- **[kova-ktor/README.md](kova-ktor/README.md)** - Ktor integration with RequestValidation plugin

## Examples

The project includes several example modules demonstrating different use cases:

- **[example-core](example-core/)** - Basic validation examples including property validation with `schema`, cross-property validation, nested property validation, and argument validation with `capture`
- **[example-ktor](example-ktor/)** - Ktor integration examples with request validation and error handling
- **[example-exposed](example-exposed/)** - Database integration examples using Exposed ORM
- **[example-hibernate-validator](example-hibernate-validator/)** - Side-by-side comparison of Kova and Hibernate Validator validation approaches
- **[example-konform](example-konform/)** - Side-by-side comparison of Kova and Konform validation approaches

Each example module contains complete, runnable code that you can use as a reference for your own projects.

## FAQ

### Why does Kova use context parameters?

Context parameters allow validators to access the `Validation` context without explicitly passing it as an argument. This makes the API cleaner and enables fluent chaining like `name.ensureNotBlank().ensureLengthAtMost(100)`. While context parameters are still experimental in Kotlin, they are stable enough for production use and represent the future direction of Kotlin's context-aware programming.

### How do I display error messages in my language?

Kova uses Java's `ResourceBundle` for internationalization. Create a `kova.properties` file for your locale (e.g., `kova_ja.properties` for Japanese) in your resources directory:

```properties
kova.charSequence.notBlank=空白にできません
kova.number.positive=正の数である必要があります
```

The appropriate locale is selected automatically based on `Locale.getDefault()`.

### Should I use fail-fast mode or collect all errors?

- **Collect all errors** (default): Best for form validation where you want to show all problems at once
- **Fail-fast mode**: Best for performance-critical paths or when the first error makes subsequent validation meaningless

```kotlin
// Collect all errors (default)
tryValidate { /* ... */ }

// Stop at first error
tryValidate(ValidationConfig(failFast = true)) { /* ... */ }
```

### Can I use Kova without the context parameters compiler flag?

No. The `-Xcontext-parameters` flag is required because Kova's API is built around context parameters. This is a deliberate design choice that enables the clean, fluent API.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Powered by
[![JetBrains logo.](https://resources.jetbrains.com/storage/products/company/brand/logos/jetbrains.svg)](https://jb.gg/OpenSource)