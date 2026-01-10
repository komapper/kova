# Kova

A type-safe Kotlin validation library with composable validators and detailed error reporting.

> **⚠️ Note**: This project is currently under active development. The API may change until a stable 1.0.0 release.

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

## Features

- **Type-Safe**: Leverages Kotlin's type system for compile-time safety
- **Composable**: Build complex validation logic by composing reusable validation functions, chaining constraints, and using conditional operators (`or`, `orElse`)
- **Immutable**: All validators are immutable and thread-safe
- **Detailed Error Reporting**: Get precise error messages with path tracking for nested validations
- **Internationalization**: Built-in support for localized error messages
- **Fail-Fast Support**: Option to stop validation at the first error or collect all errors
- **Ktor Integration**: Automatic request validation with Ktor's RequestValidation plugin
- **Zero Dependencies**: No external runtime dependencies, only requires Kotlin standard library

## Table of Contents

- [Setup](#setup)
- [Features](#features)
- [Quick Start](#quick-start)
  - [Basic Validation](#basic-validation)
  - [Multiple Validators](#multiple-validators)
  - [Object Validation](#object-validation)
- [Factory Validation](#factory-validation)
- [Ktor Integration](#ktor-integration)
- [Available Validators](#available-validators)
  - [String & CharSequence](#string--charsequence)
  - [Numbers](#numbers)
  - [Temporal Types](#temporal-types)
  - [Iterables](#iterables)
  - [Collections](#collections)
  - [Maps](#maps)
  - [Nullable](#nullable)
  - [Boolean](#boolean)
  - [Comparable Types](#comparable-types)
  - [Any Type Validators](#any-type-validators)
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
  - [Custom Constraints](#custom-constraints)
  - [Nullable Validation](#nullable-validation)
  - [Conditional Validation with `or` and `orElse`](#conditional-validation-with-or-and-orelse)
  - [Wrapping Errors with `withMessage`](#wrapping-errors-with-withmessage)
  - [Circular Reference Detection](#circular-reference-detection)
  - [Internationalization](#internationalization)
- [Examples](#examples)
- [Building and Testing](#building-and-testing)
- [Requirements](#requirements)
- [License](#license)
- [Contributing](#contributing)

## Quick Start

### Basic Validation

Validate individual values by calling validator functions within a `tryValidate` block. Each validator is an extension function on the input type with a `Validation` context receiver.

```kotlin
import org.komapper.extension.validator.*

// Define validator function
context(_: Validation)
fun validateProductName(name: String): String {
    name.ensureNotBlank()
    name.ensureLengthInRange(1..100)
    return name
}

// in this case, the return type is ValidationResult.Success<String>
val result = tryValidate { validateProductName("Wireless Mouse") }
if (result.isSuccess()) {
    println("Valid: ${result.value}") // Valid: Wireless Mouse
} else {
    result.messages.forEach { println("Invalid: $it") }
}
```

Alternatively, use `validate` to get the value directly or throw a `ValidationException` on failure:

```kotlin
try {
    // in this case, the return type is String
    val value = validate { validateProductName("Wireless Mouse") }
    println("Valid: $value") // Valid: Wireless Mouse
} catch (e: ValidationException) {
    e.messages.forEach { println("Invalid: $it") }
}
```

### Multiple Validators

You can execute multiple validators together by calling them sequentially within a `tryValidate` block. The last expression determines the return value:

```kotlin
context(_: Validation)
fun validateProductName(name: String): String {
    name.ensureNotBlank()
    name.ensureLengthInRange(1..100)
    return name
}

context(_: Validation)
fun validatePrice(price: Double): Double {
    price.ensureInClosedRange(0.0..1000.0)
    return price
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
    ::name {
        it.ensureNotBlank()
        it.ensureLengthAtLeast(1)
        it.ensureLengthAtMost(100)
    }
    ::price { it.ensureAtLeast(0.0) }
}

val result = tryValidate { Product(1, "Mouse", 29.99).validate() }
```

#### Reusable validators

Extract common validation logic into reusable validator functions:

```kotlin
context(_: Validation)
fun validateName(name: String, maxLength: Int = 100): String {
    name.ensureNotBlank()
    name.ensureLengthInRange(1..maxLength)
    return name
}

context(_: Validation)
fun validatePrice(price: Double): Double {
    price.ensureAtLeast(0.0)
    price.ensureAtMost(1000000.0)
    return price
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
    ::street {
        it.ensureNotBlank()
        it.ensureLengthAtLeast(1)
    }
    ::city {
        it.ensureNotBlank()
        it.ensureLengthAtLeast(1)
    }
    ::zipCode { it.ensureMatches(Regex("^\\d{5}(-\\d{4})?$")) }
}

context(_: Validation)
fun Customer.validate() = schema {
    ::name {
        it.ensureNotBlank()
        it.ensureLengthAtLeast(1)
        it.ensureLengthAtMost(100)
    }
    ::email {
        it.ensureNotBlank()
        it.ensureContains("@")
    }
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
    val name by bind(name) {
        it.ensureNotBlank()
        it.ensureLengthAtLeast(1)
        it
    }
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
        ::name {
            it.ensureNotBlank()
            it.ensureLengthInRange(1..50)
        }
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

All validators are extension functions on the input type with a `Validation` context receiver.

### String & CharSequence

Supported types: `String`, `CharSequence`

```kotlin
// Length validation
input.ensureLength(10)                   // Exact length
input.ensureLengthAtLeast(1)             // Minimum length
input.ensureLengthAtMost(100)            // Maximum length
input.ensureLengthInRange(1..100)        // Length within range (supports both 1..100 and 1..<100)

// Content validation
input.ensureBlank()                      // Must be blank (empty or whitespace only)
input.ensureNotBlank()                   // Must not be blank
input.ensureEmpty()                      // Must be empty
input.ensureNotEmpty()                   // Must not be empty
input.ensureStartsWith("prefix")         // Must start with prefix
input.ensureNotStartsWith("prefix")      // Must not start with prefix
input.ensureEndsWith("suffix")           // Must end with suffix
input.ensureNotEndsWith("suffix")        // Must not end with suffix
input.ensureContains("substring")        // Must contain substring
input.ensureNotContains("substring")     // Must not contain substring
input.ensureMatches(Regex("\\d+"))       // Must match regex
input.ensureNotMatches(Regex("\\d+"))    // Must not match regex
input.ensureUppercase()                  // Must be uppercase
input.ensureLowercase()                  // Must be lowercase

// Comparable validation
input.ensureAtLeast("a")                 // At least "a" (>= "a")
input.ensureAtMost("z")                  // At most "z" (<= "z")
input.ensureGreaterThan("a")             // Greater than "a" (> "a")
input.ensureGreaterThanOrEqual("a")      // Greater than or equal to "a" (>= "a")
input.ensureLessThan("z")                // Less than "z" (< "z")
input.ensureLessThanOrEqual("z")         // Less than or equal to "z" (<= "z")
input.ensureEquals("value")              // Equal to "value" (==)
input.ensureNotEquals("value")           // Not equal to "value" (!=)

// String-specific validation
input.ensureInt()                        // Validates string is valid Int
input.ensureLong()                       // Validates string is valid Long
input.ensureShort()                      // Validates string is valid Short
input.ensureByte()                       // Validates string is valid Byte
input.ensureDouble()                     // Validates string is valid Double
input.ensureFloat()                      // Validates string is valid Float
input.ensureBigDecimal()                 // Validates string is valid BigDecimal
input.ensureBigInteger()                 // Validates string is valid BigInteger
input.ensureBoolean()                    // Validates string is valid Boolean
input.ensureEnum<Status>()               // Validates string is valid enum value

// Conversions
input.transformToInt()                   // Validate and convert to Int
input.transformToLong()                  // Validate and convert to Long
input.transformToShort()                 // Validate and convert to Short
input.transformToByte()                  // Validate and convert to Byte
input.transformToDouble()                // Validate and convert to Double
input.transformToFloat()                 // Validate and convert to Float
input.transformToBigDecimal()            // Validate and convert to BigDecimal
input.transformToBigInteger()            // Validate and convert to BigInteger
input.transformToBoolean()               // Validate and convert to Boolean
input.transformToEnum<Status>()          // Validate and convert to enum
```

### Numbers

Supported types: `Int`, `Long`, `Double`, `Float`, `Byte`, `Short`, `BigDecimal`, `BigInteger`

```kotlin
input.ensureAtLeast(0)                 // At least 0 (>= 0)
input.ensureAtMost(100)                // At most 100 (<= 100)
input.ensureGreaterThan(0)             // Greater than 0 (> 0)
input.ensureGreaterThanOrEqual(0)      // Greater than or equal to 0 (>= 0)
input.ensureLessThan(100)              // Less than 100 (< 100)
input.ensureLessThanOrEqual(100)       // Less than or equal to 100 (<= 100)
input.ensureEquals(42)                 // Equal to 42 (==)
input.ensureNotEquals(0)               // Not equal to 0 (!=)
input.ensurePositive()                 // Positive (> 0)
input.ensureNegative()                 // Negative (< 0)
input.ensureNotPositive()              // Not positive (<= 0)
input.ensureNotNegative()              // Not negative (>= 0)
```

### Temporal Types

Supported types: `LocalDate`, `LocalTime`, `LocalDateTime`, `Instant`, `OffsetDateTime`, `OffsetTime`, `ZonedDateTime`, `Year`, `YearMonth`, `MonthDay`

```kotlin
input.ensureAtLeast(LocalDate.of(2024, 1, 1))                 // At least 2024-01-01 (>=)
input.ensureAtMost(LocalDate.of(2024, 12, 31))                // At most 2024-12-31 (<=)
input.ensureGreaterThan(LocalDate.of(2024, 6, 1))             // Greater than 2024-06-01 (>)
input.ensureGreaterThanOrEqual(LocalDate.of(2024, 1, 1))      // Greater than or equal to 2024-01-01 (>=)
input.ensureLessThan(LocalDate.of(2025, 1, 1))                // Less than 2025-01-01 (<)
input.ensureLessThanOrEqual(LocalDate.of(2024, 12, 31))       // Less than or equal to 2024-12-31 (<=)
input.ensureEquals(LocalDate.of(2024, 6, 15))                 // Equal to 2024-06-15 (==)
input.ensureNotEquals(LocalDate.of(2024, 1, 1))               // Not equal to 2024-01-01 (!=)
input.ensureFuture()                                          // In the future
input.ensureFutureOrPresent()                                 // In the future or present
input.ensurePast()                                            // In the past
input.ensurePastOrPresent()                                   // In the past or present
```

### Iterables

Supported types: Any `Iterable` (including `List`, `Set`, `Collection`)

```kotlin
input.ensureNotEmpty()                     // Must not be empty
input.ensureContains("foo")                // Must contain element (alias: ensureHas)
input.ensureNotContains("bar")             // Must not contain element
input.ensureEach { element ->              // Validate each element
    element.ensureAtLeast(1)
}
```

### Collections

Supported types: `List`, `Set`, `Collection`

```kotlin
input.ensureSize(5)                        // Exact size
input.ensureSizeAtLeast(1)                 // Minimum size
input.ensureSizeAtMost(10)                 // Maximum size
input.ensureSizeInRange(1..10)             // Size within range (supports both 1..10 and 1..<10)
```

### Maps

```kotlin
input.ensureSize(5)                        // Exact size
input.ensureSizeAtLeast(1)                 // Minimum size
input.ensureSizeAtMost(10)                 // Maximum size
input.ensureSizeInRange(1..10)             // Size within range (supports both 1..10 and 1..<10)
input.ensureNotEmpty()                     // Must not be empty
input.ensureContainsKey("foo")             // Must contain key (alias: ensureHasKey)
input.ensureNotContainsKey("bar")          // Must not contain key
input.ensureContainsValue(42)              // Must contain value (alias: ensureHasValue)
input.ensureNotContainsValue(0)            // Must not contain value
input.ensureEachKey { key ->               // Validate each key
    key.ensureAtLeast(1)
}
input.ensureEachValue { value ->           // Validate each value
    value.ensureAtLeast(0)
}
```

### Nullable

```kotlin
input.ensureNull()                         // Must be null
input.ensureNotNull()                      // Must not be null (enables smart casting, stops on failure)
input.ensureNullOr { block }               // Accept null or validate non-null
```

### Boolean

```kotlin
input.ensureTrue()                         // Must be true
input.ensureFalse()                        // Must be false
```

### Comparable Types

Supports all `Comparable` types, such as `UInt`, `ULong`, `UByte`, and `UShort`.

```kotlin
input.ensureAtLeast(0u)                    // At least 0u (>= 0u)
input.ensureAtMost(100u)                   // At most 100u (<= 100u)
input.ensureGreaterThan(0u)                // Greater than 0u (> 0u)
input.ensureGreaterThanOrEqual(0u)         // Greater than or equal to 0u (>= 0u)
input.ensureLessThan(100u)                 // Less than 100u (< 100u)
input.ensureLessThanOrEqual(100u)          // Less than or equal to 100u (<= 100u)
input.ensureEquals(42u)                    // Equal to 42u (==)
input.ensureNotEquals(0u)                  // Not equal to 0u (!=)

// Range validation
input.ensureInRange(1..10)                 // In range 1..10 (supports both 1..10 and 1..<10)
input.ensureInClosedRange(1.0..10.0)       // In closed range 1.0..10.0 (inclusive)
input.ensureInOpenEndRange(1..<10)         // In open-end range 1..<10 (inclusive start, exclusive end)
```

### Any Type Validators

Works with any type:

```kotlin
input.ensureEquals("completed")                    // Equal to "completed"
input.ensureNotEquals("rejected")                  // Not equal to "rejected"
input.ensureInIterable(listOf("a", "b", "c"))      // One of the allowed values
```

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
    ::name {
        it.ensureNotBlank()
        it.ensureLengthAtLeast(3)
        it.ensureLengthAtMost(100)
    }
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
    name.ensureNotBlank()
    name.ensureLengthInRange(1..100)
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
    date.ensureFuture()  // Uses the fixed clock for comparison
}
```

### Debug Logging

Enable logging to debug validation flow:

```kotlin
val result = tryValidate(config = ValidationConfig(
    logger = { logEntry -> println("[Validation] $logEntry") }
)) {
    username.ensureLengthAtLeast(3)
    username.ensureLengthAtMost(20)
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

### Custom Constraints

Create custom validators using `constrain` and `satisfies`. The `constrain()` function automatically populates the constraint ID and input value in error messages:

```kotlin
context(_: Validation)
fun isUrlPath(input: String) {
    input.constrain("custom.urlPath") {
        satisfies(it.startsWith("/") && !it.contains("..")) {
            text("Must be a valid URL path")
        }
    }
}

val result = tryValidate { isUrlPath("/a/../b") }
if (!result.isSuccess()) {
    result.messages.forEach(::println)
    // Message(text='Must be a valid URL path', root=, path=, input=/a/../b)
}
```

The `satisfies()` method uses a `MessageProvider` lambda for lazy message construction—the message is only created when validation fails:

```kotlin
context(_: Validation)
fun String.alphanumeric(
    message: MessageProvider = { "kova.string.alphanumeric".resource }
) = constrain("kova.string.alphanumeric") {
    satisfies(it.all { c -> c.isLetterOrDigit() }, message)
}
```

### Nullable Validation

```kotlin
// Accept or reject null
value.ensureNull()
value.ensureNotNull()

// Validate only if non-null
email.ensureNullOr { it.ensureContains("@") }

// ensureNotNull enables smart casting - subsequent validators work on non-null type
context(_: Validation)
fun validateName(name: String?): String {
    name.ensureNotNull()           // Validates and enables smart cast
    name.ensureLengthAtLeast(1)    // Compiler knows name is non-null
    name.ensureLengthAtMost(100)
    return name                    // Return type is String (non-nullable)
}
```

### Conditional Validation with `or` and `orElse`

Try the first validation; if it fails, try the next. Useful for alternative validation rules.

```kotlin
// Accept either domestic or international phone format
context(_: Validation)
fun validatePhone(phone: String) =
    or { phone.ensureMatches(Regex("^\\d{3}-\\d{4}$")) }      // Domestic format: 123-4567
        .orElse { phone.ensureMatches(Regex("^\\+\\d{1,3}-\\d+$")) }  // International format: +1-1234567

val result = tryValidate { validatePhone("123-abc-456") }
if (!result.isSuccess()) {
    result.messages.map { it.text }.forEach { println(it) }
    // at least one constraint must be satisfied: [[must match pattern: ^\d{3}-\d{4}$], [must match pattern: ^\+\d{1,3}-\d+$]]
}

// Chain multiple alternatives
or { id.ensureMatches(Regex("^[a-z]+$")) }    // Lowercase letters only
    .or { id.ensureMatches(Regex("^\\d+$")) }  // Digits only
    .orElse { id.ensureMatches(Regex("^[A-Z]+$")) }  // Uppercase letters only
```

### Wrapping Errors with `withMessage`

The `withMessage` function wraps validation logic and consolidates multiple errors into a single custom message. This is useful when you want to present a higher-level error message instead of detailed field-level errors:

```kotlin
data class Address(val street: String, val city: String, val zipCode: String)

context(_: Validation)
fun Address.validate() = schema {
    ::zipCode {
        withMessage("Invalid ZIP code format") {
            it.ensureMatches(Regex("^\\d{5}(-\\d{4})?$"))
            it.ensureLengthAtLeast(5)
        }
    }
}

val result = tryValidate { Address("Eitai", "Tokyo", "123-456").validate() }
if (!result.isSuccess()) {
    result.messages.forEach { println(it) }
    // Message(text='Invalid ZIP code format', root=Address, path=zipCode, input=null)
}
```

You can also use a transform function to customize how multiple errors are consolidated:

```kotlin
context(_: Validation)
fun validatePassword(password: String) =
    withMessage({ messages ->
        text("Password validation failed: ${messages.size} errors found")
    }) {
        password.ensureLengthAtLeast(8)
        password.ensureMatches(Regex(".*[A-Z].*"))
        password.ensureMatches(Regex(".*[0-9].*"))
    }
```

### Circular Reference Detection

Kova automatically detects and handles circular references in nested object validation to prevent infinite loops.

### Internationalization

Error messages use resource bundles from `kova.properties`. The `resource()` function creates internationalized messages with parameter substitution (using MessageFormat syntax where {0}, {1}, etc. are replaced with the provided arguments):

```kotlin
// Using resource keys from kova.properties
str.ensureLengthAtLeast(5, message = { "custom.message.key".resource(5) })

// Multiple parameters
context(_: Validation)
fun Int.range(
    minValue: Int,
    maxValue: Int,
    message: MessageProvider = { "kova.number.range".resource(minValue, maxValue) }
) = constrain("kova.number.range") {
    satisfies(it in minValue..maxValue, message)
}
```

Corresponding entry in `kova.properties`:
```properties
kova.number.range=The value must be between {0} and {1}.
```

## Examples

The project includes several example modules demonstrating different use cases:

- **[example-core](example-core/)** - Basic validation examples including schema validation, cross-property validation, and nested object validation
- **[example-factory](example-factory/)** - Factory pattern examples showing how to validate and transform raw input into typed objects
- **[example-ktor](example-ktor/)** - Ktor integration examples with request validation and error handling
- **[example-exposed](example-exposed/)** - Database integration examples using Exposed ORM
- **[example-hibernate-validator](example-hibernate-validator/)** - Side-by-side comparison of Kova and Hibernate Validator validation approaches
- **[example-konform](example-konform/)** - Side-by-side comparison of Kova and Konform validation approaches

Each example module contains complete, runnable code that you can use as a reference for your own projects.

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

- Kotlin 2.1.0+ (for context parameters support)
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
