# Kova

[![Build](https://github.com/komapper/kova/actions/workflows/ci.yml/badge.svg)](https://github.com/komapper/kova/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.0+-purple.svg)](https://kotlinlang.org)

A type-safe Kotlin validation library with composable validators and detailed error reporting.

> **⚠️ Note**: This project is currently under active development. The API may change until a stable 1.0.0 release.

## Quick Start

Kova provides two validation approaches: **property validation** with `schema` and **argument validation** with `capture`.

### Property Validation with `schema`

Validate properties of an existing object:

```kotlin
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
    result.messages.forEach { println("${it.path.fullName}: ${it.text}") }
    // name: must not be blank
    // age: must be a valid integer
}
```

> **Note**: Validators require `context(_: Validation)`. This uses Kotlin's [context parameters](https://kotlinlang.org/docs/whatsnew2020.html#context-parameters), which must be enabled (see [Setup](#setup)).

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

### Kotlin-Native Design

Kova is designed specifically for Kotlin. Hibernate Validator, being a Java library, requires annotation use-site targets:

```kotlin
// Hibernate Validator - @field: prefix required in Kotlin
class User(
    @field:NotBlank
    @field:Size(max = 100)
    val name: String,
)

// Kova - natural Kotlin syntax
context(_: Validation)
fun User.validate() = schema {
    ::name { it.ensureNotBlank().ensureLengthAtMost(100) }
}
```

### Simple Custom Validators

In Hibernate Validator, creating a custom constraint requires an annotation class plus a validator class. In Kova, it's just a function:

```kotlin
// Kova - custom validator is just a function
context(_: Validation)
fun String.ensureNoTabCharacters() = constrain("noTabs") {
    satisfies(!it.contains("\t")) { text("must not contain tabs") }
}
```

### Simple Cross-Property Validation

Comparing multiple properties is straightforward in Kova:

```kotlin
context(_: Validation)
fun Car.validate() = schema {
    ::seatCount { it.ensurePositive() }
    ::passengers { it.ensureNotNull() }

    // Cross-property validation - just access both properties
    constrain("passengerCount") {
        satisfies(it.passengers.size <= it.seatCount) {
            text("Passengers cannot exceed seat count")
        }
    }
}
```

In Hibernate Validator, this requires a class-level constraint annotation with a custom `ConstraintValidator` implementation.

### Argument Validation with `capture`

Kova can validate and transform function arguments using `capture`:

```kotlin
data class User(val name: String, val age: Int)

context(_: Validation)
fun buildUser(rawName: String, rawAge: String): User {
    val name by capture { rawName.ensureNotBlank() }
    val age by capture { rawAge.transformToInt().ensurePositive() }
    return User(name, age)
}

// Collects ALL errors across both fields
val result = tryValidate { buildUser("", "invalid") }
// Errors: name -> "must not be blank", age -> "must be a valid integer"
```

### Function-Based Validation

Validators are regular Kotlin functions, enabling natural composition:

```kotlin
// Reusable, parameterized validator
context(_: Validation)
fun validatePrice(value: Double, max: Double = 10000.0): Double {
    return value.ensurePositive().ensureAtMost(max)
}

// Compose validators freely
context(_: Validation)
fun Product.validate() = schema {
    ::price { validatePrice(it) }
    ::discountedPrice { validatePrice(it, max = price) }  // Cross-field reference
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
  - [Property Validation](#property-validation)
- [Argument Validation](#argument-validation)
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

### Property Validation

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

#### Nested property validation

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

## Argument Validation

Use the `capture` function to validate and transform function arguments. This is useful for handling form data, API requests, or any scenario where you need to convert and validate external input.

### Basic Usage

The `capture` function uses property delegation to validate values with automatic error path naming:

```kotlin
data class User(val name: String, val age: Int)

context(_: Validation)
fun buildUser(rawName: String, rawAge: String): User {
    val name by capture { rawName.ensureNotBlank().ensureLengthAtLeast(1) }
    val age by capture { rawAge.transformToInt().ensureInRange(0..120) }
    return User(name, age)
}

// Valid input
val result = tryValidate { buildUser("Alice", "25") }
// result.value = User(name="Alice", age=25)

// Invalid input - collects all errors
val result = tryValidate { buildUser("", "invalid") }
// Errors: name -> "must not be blank", age -> "must be a valid integer"
```

### Error Path Naming

The property name automatically becomes the path segment in validation errors. This provides clear, precise error messages:

```kotlin
val result = tryValidate { buildUser("", "-5") }
if (!result.isSuccess()) {
    result.messages.forEach { println("${it.path.fullName}: ${it.text}") }
    // Output:
    //   name: must not be blank
    //   age: must be within range 0..120
}
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
| `root`         | `String`        | The root object identifier in the validation hierarchy. For property validation, this is the simple class name (e.g., `Customer`). For argument validation, this is empty.                         |
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

// Property validation function
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

- **[example-core](example-core/)** - Basic validation examples including property validation with `schema`, cross-property validation, nested property validation, and argument validation with `capture`
- **[example-ktor](example-ktor/)** - Ktor integration examples with request validation and error handling
- **[example-exposed](example-exposed/)** - Database integration examples using Exposed ORM
- **[example-hibernate-validator](example-hibernate-validator/)** - Side-by-side comparison of Kova and Hibernate Validator validation approaches
- **[example-konform](example-konform/)** - Side-by-side comparison of Kova and Konform validation approaches

Each example module contains complete, runnable code that you can use as a reference for your own projects.

## FAQ

### Why does Kova use context parameters?

Context parameters allow validators to access the `Validation` context without explicitly passing it as an argument. This makes the API cleaner and enables fluent chaining like `name.ensureNotBlank().ensureLengthAtMost(100)`. While context parameters are still experimental in Kotlin, they are stable enough for production use and represent the future direction of Kotlin's context-aware programming.

### How is Kova different from Konform?

Both are Kotlin-native libraries with zero dependencies. The main differences:

- **Kova** uses function-based validators. Validators are regular Kotlin functions that can be composed, parameterized, and reused freely. Kova supports both property validation (`schema`) and argument validation (`capture`).
- **Konform** uses a DSL-based approach where you define validation rules declaratively. Konform supports Kotlin Multiplatform (JS, Native, Wasm).

See [Why Kova?](#why-kova) for a detailed comparison.

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
