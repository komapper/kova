# Kova

[![Build](https://github.com/komapper/kova/actions/workflows/ci.yml/badge.svg)](https://github.com/komapper/kova/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.0+-purple.svg)](https://kotlinlang.org)

A type-safe Kotlin validation library with composable validators and detailed error reporting.

> **⚠️ Note**: This project is currently under active development. The API may change until a stable 1.0.0 release.

## Quick Start

Kova lets you write validation rules that are readable, composable, and type-safe.

**Without Kova** - Validation logic often ends up scattered, and collecting all errors requires extra boilerplate:

```kotlin
fun validateUser(name: String, age: Int): Pair<String, Int> {
    val errors = mutableListOf<String>()
    if (name.isBlank()) errors.add("Name cannot be blank")
    if (name.length > 100) errors.add("Name must be at most 100 characters")
    if (age < 0) errors.add("Age cannot be negative")
    if (age > 150) errors.add("Age must be at most 150")
    if (errors.isNotEmpty()) throw IllegalArgumentException(errors.joinToString())
    return name to age
}
```

**With Kova** - Chain validators fluently, and all errors are collected automatically:

```kotlin
import org.komapper.extension.validator.*

context(_: Validation)
fun validateUser(name: String, age: Int): Pair<String, Int> {
    val validName = name.ensureNotBlank().ensureLengthAtMost(100)
    val validAge = age.ensureNotNegative().ensureAtMost(150)
    return validName to validAge
}
```

Use `tryValidate` to run validation and get a result object:

```kotlin
// Valid input - returns Success
val result = tryValidate { validateUser("Alice", 25) }
if (result.isSuccess()) {
    println(result.value)  // (Alice, 25)
}

// Invalid input - returns Failure with ALL errors, not just the first one
val result = tryValidate { validateUser("", -5) }
if (result.isFailure()) {
    result.messages.forEach { println(it.text) }
    // Output:
    //   "must not be blank"
    //   "must not be negative"
}
```

`tryValidate` returns a `ValidationResult` which is either `Success` or `Failure`:
- `isSuccess()` - Returns `true` if validation passed. Enables smart cast to access `result.value`.
- `isFailure()` - Returns `true` if validation failed. Enables smart cast to access `result.messages`.

Alternatively, use `validate` to get the value directly or throw a `ValidationException` on failure:

```kotlin
val value = validate { validateUser("Alice", 25) }  // Returns (Alice, 25)
val value = validate { validateUser("", -5) }  // Throws ValidationException with all errors
```

> **Note**: The `context(_: Validation)` declaration is required for validator functions. This uses Kotlin's [context parameters](https://kotlinlang.org/docs/whatsnew2020.html#context-parameters) feature, which must be enabled in your build (see [Setup](#setup)).

### Complete Example

Here's a complete, runnable example you can copy and paste:

```kotlin
import org.komapper.extension.validator.*

data class User(val name: String, val email: String, val age: Int)

context(_: Validation)
fun User.validate() = schema {
    ::name { it.ensureNotBlank().ensureLengthInRange(1..50) }
    ::email { it.ensureNotBlank().ensureContains("@") }
    ::age { it.ensureInRange(0..150) }
}

fun main() {
    // Valid user
    val validResult = tryValidate { User("Alice", "alice@example.com", 25).validate() }
    if (validResult.isSuccess()) println("Valid") else error("never happens")
  
    // Invalid user - collects ALL errors
    val invalidResult = tryValidate { User("", "invalid", -5).validate() }
    if (invalidResult.isSuccess()) {
        error("never happens")
    } else {
        println("Invalid")
        invalidResult.messages.forEach { println(it) }
        // Message(constraintId=kova.charSequence.notBlank, text='must not be blank', root=User, path=name, input=, args=[])
        // Message(constraintId=kova.charSequence.lengthInRange, text='must have length within range 1..50', root=User, path=name, input=, args=[1..50])
        // Message(constraintId=kova.charSequence.contains, text='must contain "@"', root=User, path=email, input=invalid, args=[@])
        // Message(constraintId=kova.comparable.inRange, text='must be within range 0..150', root=User, path=age, input=-5, args=[0..150])
    }
}
```

## Why Kova?

There are several validation libraries for Kotlin. Here's why you might choose Kova:

| Feature                  | Kova                              | Hibernate Validator  | Konform                 |
|--------------------------|-----------------------------------|----------------------|-------------------------|
| **Approach**             | Function-based                    | Annotation-based     | DSL-based               |
| **Type safety**          | Compile-time (context parameters) | Runtime (reflection) | Compile-time            |
| **Value transformation** | Yes (`transformToInt()`, etc.)    | No                   | No                      |
| **Smart cast support**   | Yes (`ensureNotNull()`)           | No                   | No                      |
| **Dependencies**         | Zero                              | Jakarta EE           | Zero                    |
| **Error collection**     | All errors or fail-fast           | All errors or fail-fast | All errors or fail-fast |

### Function-Based Validation

Unlike annotation-based approaches, Kova validators are regular Kotlin functions. This means you can:

- **Compose freely**: Combine validators using standard function composition
- **Parameterize easily**: Pass arguments to customize validation behavior
- **Reuse across types**: Apply the same validator to different properties or classes
- **Test simply**: Unit test validators like any other function

```kotlin
// Reusable, parameterized validator
context(_: Validation)
fun validateLength(value: String, min: Int, max: Int): String {
    return value.ensureLengthAtLeast(min).ensureLengthAtMost(max)
}

// Use it anywhere
context(_: Validation)
fun User.validate() = schema {
    ::name { validateLength(it, 1, 100) }
    ::bio { validateLength(it, 0, 500) }
}
```

### Type-Safe Transformations

Kova can validate and transform values in a single operation—useful for handling raw input:

```kotlin
context(_: Validation)
fun buildProduct(rawPrice: String, rawQuantity: String) = factory {
    val price by bind(rawPrice) { it.transformToDouble().ensureNotNegative() }
    val quantity by bind(rawQuantity) { it.transformToInt().ensurePositive() }
    Product(price, quantity)
}
```

### Smart Cast Support

`ensureNotNull()` uses Kotlin contracts, enabling smart casts in subsequent code:

```kotlin
context(_: Validation)
fun processEmail(email: String?): String {
    email.ensureNotNull()  // Validates and enables smart cast
    return email.ensureContains("@").ensureLengthAtMost(254)  // email is now String, not String?
}
```

## Features

- **Type-Safe**: Leverages Kotlin's type system for compile-time safety
- **Composable**: Build complex validation logic by composing reusable validation functions, chaining constraints, and using conditional operators (`or`, `orElse`)
- **Immutable**: All validators are immutable and thread-safe
- **Detailed Error Reporting**: Get precise error messages with path tracking for nested validations
- **Internationalization**: Built-in support for localized error messages
- **Fail-Fast Support**: Option to stop validation at the first error or collect all errors
- **Ktor Integration**: Automatic request validation with Ktor's RequestValidation plugin
- **Zero Dependencies**: No external runtime dependencies, only requires Kotlin standard library

## Setup

Add Kova to your Gradle project:

### Gradle Kotlin DSL (build.gradle.kts)

```kotlin
dependencies {
    // Core validation library
    implementation("org.komapper:kova-core:0.1.0")

    // Factory validation (optional)
    implementation("org.komapper:kova-factory:0.1.0")

    // Ktor integration (optional)
    implementation("org.komapper:kova-ktor:0.1.0")
}
```

### Enable Context Parameters (Required)

Kova uses Kotlin's context parameters feature, which must be enabled in your project:

```kotlin
kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}
```

**Note**: Context parameters are an experimental Kotlin feature. This compiler flag is required to use Kova's validation functions.

## Table of Contents

- [Quick Start](#quick-start)
- [Why Kova?](#why-kova)
- [Features](#features)
- [Setup](#setup)
- [Basic Usage](#basic-usage)
  - [Multiple Validators](#multiple-validators)
  - [Object Validation](#object-validation)
- [Factory Validation](#factory-validation)
- [Ktor Integration](#ktor-integration)
- [Available Validators](#available-validators)
- [Error Handling](#error-handling)
  - [Basic Error Handling](#basic-error-handling)
  - [Message Properties](#message-properties)
  - [Custom Error Messages](#custom-error-messages)
- [Validation Configuration](#validation-configuration)
  - [Fail-Fast Mode](#fail-fast-mode)
  - [Custom Clock for Temporal Validation](#custom-clock-for-temporal-validation)
  - [Debug Logging](#debug-logging)
  - [Combined Configuration](#combined-configuration)
- [Advanced Topics](#advanced-topics)
- [Examples](#examples)
- [FAQ](#faq)
- [Building and Testing](#building-and-testing)
- [Requirements](#requirements)
- [License](#license)
- [Contributing](#contributing)

## Basic Usage

### Multiple Validators

You can execute multiple validators together by calling them sequentially within a `tryValidate` block. The last expression determines the return value:

```kotlin
context(_: Validation)
fun validateProductName(name: String): String {
    return name.ensureNotBlank().ensureLengthInRange(1..100)
}

context(_: Validation)
fun validatePrice(price: Double): Double {
    return price.ensureInClosedRange(0.0..1000.0)
}

val result = tryValidate {
    val name = validateProductName("Wireless Mouse")
    val price = validatePrice(29.99)
    name to price
}

if (result.isSuccess()) {
    println("Valid: ${result.value}") // Valid: (Wireless Mouse, 29.99)
} else {
    result.messages.forEach { println("Invalid: $it") }
}
```

### Object Validation

Validate class properties using the `schema` function.

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

#### Reusable validators

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

data class Product(val name: String, val price: Double)
data class Service(val title: String, val price: Double)

context(_: Validation)
fun Product.validate() = schema {
    ::name { validateName(it) }
    ::price { validatePrice(it) }
}

context(_: Validation)
fun Service.validate() = schema {
    ::title { validateName(it, 200) }  // Reused for different property
    ::price { validatePrice(it) }  // Reused across schemas
}
```

Reusable validators can be shared across multiple schemas, ensuring consistent validation rules throughout your application.

Validators can accept parameters to make them more flexible. By using default parameter values, you can provide sensible defaults while allowing customization when needed. In the example above, `validateName` uses a default `maxLength` of 100, but the `Service.title` validation overrides it to 200, demonstrating how the same validator can be adapted to different requirements.

#### Nested object validation

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
    ::address { it.validate() }  // Nested validation
}

val customer = Customer(
    name = "John Doe",
    email = "invalid-email",
    address = Address(street = "", city = "Tokyo", zipCode = "123")
)

val result = tryValidate { customer.validate() }
```

Notice how the error messages show the full path (e.g., `address.street`, `address.zipCode`) to pinpoint exactly where validation failed in the nested structure.

#### Cross-property validation

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

## Factory Validation

The `kova-factory` module provides a factory pattern for combining object construction and validation in a single operation. It's particularly useful when validating and transforming raw input (like form data or API requests) into typed objects.

```kotlin
import org.komapper.extension.validator.factory.*

data class User(val name: String, val age: Int)

context(_: Validation)
fun buildUser(name: String, age: String) = factory {
    val name by bind(name) { it.ensureNotBlank().ensureLengthAtLeast(1) }
    val age by bind(age) { it.transformToInt() }
    User(name, age)
}

val result = tryValidate { buildUser("Alice", "25") }
```

For detailed documentation, see **[kova-factory/README.md](kova-factory/README.md)**.

## Ktor Integration

The `kova-ktor` module enables automatic request validation with Ktor's RequestValidation plugin:

```kotlin
@Serializable
data class Customer(val id: Int, val name: String) : Validated {
    context(_: Validation)
    override fun validate() = schema {
        ::id { it.ensurePositive() }
        ::name { it.ensureNotBlank().ensureLengthInRange(1..50) }
    }
}

fun Application.module() {
    install(RequestValidation) { validate(SchemaValidator()) }
    install(StatusPages) {
        exception<RequestValidationException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, cause.reasons.joinToString("\n"))
        }
    }
    routing {
        post("/customers") {
            val customer = call.receive<Customer>()  // Validated automatically
            call.respond(HttpStatusCode.Created, customer)
        }
    }
}
```

For detailed documentation, see **[kova-ktor/README.md](kova-ktor/README.md)**.

## Available Validators

Kova provides validators for many types including String, Numbers, Temporal types, Collections, Maps, and more.

For the complete list, see **[docs/VALIDATORS.md](docs/VALIDATORS.md)**.

## Error Handling

### Basic Error Handling

```kotlin
val result = tryValidate {
    val username = "joe"
    username.ensureLengthAtLeast(5)
}

if (!result.isSuccess()) {
    result.messages.forEach {
        println(it)
        // Message(constraintId=kova.charSequence.lengthAtLeast, text='must be at least 5 characters', root=, path=, input=joe, args=[5])
    }
}
```

### Message Properties

Each validation error is represented by a `Message` object with the following properties that provide detailed error reporting information:

| Property       | Type            | Description                                                                                                                                                                                       |
|----------------|-----------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `text`         | `String`        | The formatted error message text, ready to display to users. For resource-based messages, parameters are already substituted using MessageFormat.                                                 |
| `constraintId` | `String`        | The unique identifier for the constraint that failed (e.g., `kova.charSequence.minLength`). Useful for programmatic error handling or custom error formatting.                                    |
| `root`         | `String`        | The root object identifier in the validation hierarchy. For schema validation, this is the simple class name (e.g., `Customer`). For simple validations, this is empty.                           |
| `path`         | `Path`          | The path to the validated value in the object graph (e.g., `address.zipCode` for nested properties, `items[0]` for collection elements). Use `path.fullName` to get the string representation.    |
| `input`        | `Any?`          | The actual input value that failed validation. Useful for debugging or creating custom error messages that include the problematic value.                                                         |
| `args`         | `List<Any?>`    | Arguments used for MessageFormat substitution. These correspond to the `{0}`, `{1}`, etc. placeholders in resource bundle messages. Can include nested Message objects for composite validations. |
| `descendants`  | `List<Message>` | Nested error messages from collection/map element validations or the `or` operator. For example, `onEach` validations include descendant messages for each failing element.                       |

**Message Types:**
- `Message.Text`: Simple text messages created with `text()`. Used for hardcoded error messages.
- `Message.Resource`: I18n messages loaded from `kova.properties` using `resource()`. The `constraintId` is used as the resource bundle key.

**Example of extracting message details:**

```kotlin
// Data class
data class Product(val id: Int, val name: String, val price: Double)

// Schema validation function
context(_: Validation)
fun Product.validate() = schema {
    ::id { it.ensureAtLeast(1) }
    ::name { it.ensureNotBlank().ensureLengthInRange(3..100) }
    ::price { it.ensureAtLeast(0.0) }
}

// Usage
val result = tryValidate {
    val product = Product(id = 0, name = "ab", price = 10.0)
    product.validate()
}

// Extract message details
if (!result.isSuccess()) {
    result.messages.forEach { message ->
        println("Constraint: ${message.constraintId}")      // kova.charSequence.lengthAtLeast
        println("Error text: ${message.text}")              // must be at least 3 characters
        println("Root object: ${message.root}")             // Product
        println("Path: ${message.path.fullName}")           // name
        println("Invalid value: ${message.input}")          // ab
        println("Arguments: ${message.args}")               // [3]
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

You can customize validation behavior using `ValidationConfig`:

### Fail-Fast Mode

Stop at the first error instead of collecting all errors:

```kotlin
context(_: Validation)
fun validateProductName(name: String) {
    name.ensureNotBlank().ensureLengthInRange(1..100)
}

// Stops at first error
val result = tryValidate(ValidationConfig(failFast = true)) {
    validateProductName("Wireless Mouse")
}
```

### Custom Clock for Temporal Validation

Provide a custom clock for temporal validators (useful for testing):

```kotlin
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

context(_: Validation)
fun validateDate(date: LocalDate) {
    date.ensureFuture()
}

val fixedClock = Clock.fixed(Instant.parse("2024-06-15T10:00:00Z"), ZoneId.of("UTC"))

val result = tryValidate(config = ValidationConfig(clock = fixedClock)) {
    val date = LocalDate.of(2024, 6, 20)
    validateDate(date)  // Uses the fixed clock for comparison
}
```

### Debug Logging

Enable logging to debug validation flow:

```kotlin
val result = tryValidate(config = ValidationConfig(
    logger = { logEntry -> println("[Validation] $logEntry") }
)) {
    username.ensureLengthInRange(3..20)
}
```

### Combined Configuration

All options can be combined:

```kotlin
val result = tryValidate(config = ValidationConfig(
    failFast = true,
    clock = Clock.systemUTC(),
    logger = { logEntry -> println(logEntry) }
)) {
    // validation logic
}
```

## Advanced Topics

For advanced usage including custom constraints, nullable validation, conditional validation (`or`/`orElse`), error wrapping, circular reference detection, and internationalization, see **[docs/ADVANCED.md](docs/ADVANCED.md)**.

To understand how Kova's error accumulation mechanism works internally, including fail-fast vs collect-all modes, path tracking, and the special behavior of validators like `ensureNotNull()` and `transformToInt()`, see **[docs/ERROR_ACCUMULATION.md](docs/ERROR_ACCUMULATION.md)**.

## Examples

The project includes several example modules demonstrating different use cases:

- **[example-core](example-core/)** - Basic validation examples including schema validation, cross-property validation, and nested object validation
- **[example-factory](example-factory/)** - Factory pattern examples showing how to validate and transform raw input into typed objects
- **[example-ktor](example-ktor/)** - Ktor integration examples with request validation and error handling
- **[example-exposed](example-exposed/)** - Database integration examples using Exposed ORM
- **[example-hibernate-validator](example-hibernate-validator/)** - Side-by-side comparison of Kova and Hibernate Validator validation approaches
- **[example-konform](example-konform/)** - Side-by-side comparison of Kova and Konform validation approaches

Each example module contains complete, runnable code that you can use as a reference for your own projects.

## FAQ

### Why does Kova use context parameters?

Context parameters allow validators to access the `Validation` context without explicitly passing it as an argument. This makes the API cleaner and enables fluent chaining like `name.ensureNotBlank().ensureLengthAtMost(100)`. While context parameters are still experimental in Kotlin, they are stable enough for production use and represent the future direction of Kotlin's context-aware programming.

### How is Kova different from Konform?

Both libraries are type-safe and have zero dependencies, but they take different approaches:

- **Kova** uses function-based validators with context parameters. Validators are regular Kotlin functions that can be composed, parameterized, and reused freely.
- **Konform** uses a DSL-based approach where you define validation rules declaratively.

Kova also supports value transformation (`transformToInt()`, etc.) and smart casting with `ensureNotNull()`, which Konform does not.

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

## Building and Testing

```bash
# Run all tests
./gradlew test

# Build the project
./gradlew build

# Format code
./gradlew spotlessApply
```

## Requirements

- Kotlin 2.3.0+ (for context parameters support)
- Java 17+
- Gradle 8.14+
- Context parameters compiler flag: `-Xcontext-parameters` (see [Setup](#setup))

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Here are some ways you can contribute:

- Report bugs and suggest features by opening issues
- Submit pull requests with bug fixes or new features
- Improve documentation
- Share your feedback and use cases

Before submitting a pull request:

1. Fork the repository and create a new branch
2. Make your changes and add tests if applicable
3. Run `./gradlew build` to ensure all tests pass and code is properly formatted
4. Submit a pull request with a clear description of your changes
