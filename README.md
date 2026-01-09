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

Validate individual values by calling validator functions within a `tryValidate` block. Each validator is an extension function on `Validation` that takes the input as the first parameter.

```kotlin
import org.komapper.extension.validator.*

// Define validator function
fun Validation.validateProductName(name: String): String {
    ensureNotBlank(name)
    ensureLengthInRange(name, 1..100)
    return name
}

// in this case, the return type is ValidationResult.Success<String>
val result = tryValidate { validateProductName("Wireless Mouse") }
if (result.isSuccess()) { 
    println("Valid: ${result.name}") // Valid: Wireless Mouse 
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
fun Validation.validateProductName(name: String): String {
    ensureNotBlank(name)
    ensureLengthInRange(name, 1..100)
    return name
}

fun Validation.validatePrice(price: Double): Double {
    ensureInClosedRange(price, 0.0..1000.0)
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

fun Validation.validate(product: Product) = product.schema {
    product::id { ensureMin(it, 1) }
    product::name { ensureNotBlank(it); ensureLengthInRange(it, 1..100) }
    product::price { ensureMin(it, 0.0) }
}

val result = tryValidate { validate(Product(1, "Mouse", 29.99)) }
```

#### Reusable validators

Extract common validation logic into reusable validator functions:

```kotlin
fun Validation.validateName(name: String, maxLength: Int = 100): String {
    ensureNotBlank(name); ensureLengthInRange(name, 1..maxLength)
    return name
}

fun Validation.validatePrice(price: Double): Double {
    ensureMin(price, 0.0); ensureMax(price, 1000000.0)
    return price
}

data class Product(val name: String, val price: Double)
data class Service(val title: String, val price: Double)

fun Validation.validate(product: Product) = product.schema {
    product::name { validateName(it) }
    product::price { validatePrice(it) }
}

fun Validation.validate(service: Service) = service.schema {
    service::title { validateName(it, 200) }  // Reused for different property
    service::price { validatePrice(it) }  // Reused across schemas
}
```

Reusable validators can be shared across multiple schemas, ensuring consistent validation rules throughout your application.

Validators can accept parameters to make them more flexible. By using default parameter values, you can provide sensible defaults while allowing customization when needed. In the example above, `validateName` uses a default `maxLength` of 100, but the `Service.title` validation overrides it to 200, demonstrating how the same validator can be adapted to different requirements.

#### Nested object validation

Validate nested objects by calling their validation functions within the parent's schema. Error messages include the full path to the failed property:

```kotlin
data class Address(val street: String, val city: String, val zipCode: String)
data class Customer(val name: String, val email: String, val address: Address)

fun Validation.validate(address: Address) = address.schema {
    address::street { ensureNotBlank(it); ensureMinLength(it, 1) }
    address::city { ensureNotBlank(it); ensureMinLength(it, 1) }
    address::zipCode { ensureMatches(it, Regex("^\\d{5}(-\\d{4})?$")) }
}

fun Validation.validate(customer: Customer) = customer.schema {
    customer::name { ensureNotBlank(it); ensureLengthInRange(it, 1..100) }
    customer::email { ensureNotBlank(it); ensureContains(it, "@") }
    customer::address { validate(it) }  // Nested validation
}

val customer = Customer(
    name = "John Doe",
    email = "invalid-email",
    address = Address(street = "", city = "Tokyo", zipCode = "123")
)

val result = tryValidate { validate(customer) }
```

Notice how the error messages show the full path (e.g., `address.street`, `address.zipCode`) to pinpoint exactly where validation failed in the nested structure.

#### Cross-property validation

Validates relationships between multiple properties using `constrain` within a `schema` block:

```kotlin
data class PriceRange(val minPrice: Double, val maxPrice: Double)

fun Validation.validate(range: PriceRange) = range.schema {
    range::minPrice { ensureNotNegative(it) }
    range::maxPrice { ensureNotNegative(it) }

    // Validate relationship
    range.constrain("priceRange") {
        satisfies(it.minPrice <= it.maxPrice) {
            text("minPrice must be less than or equal to maxPrice")
        }
    }
}

val result = tryValidate { validate(PriceRange(10.0, 100.0)) }
```

## Factory Validation

The `kova-factory` module provides a factory pattern for combining object construction and validation in a single operation. It's particularly useful when validating and transforming raw input (like form data or API requests) into typed objects.

```kotlin
import org.komapper.extension.validator.factory.*

data class User(val name: String, val age: Int)

fun Validation.buildUser(name: String, age: String) = factory {
    val name by bind(name) { ensureNotBlank(it); ensureMinLength(it, 1); it }
    val age by bind(age) { parseInt(it) }
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
    override fun Validation.validate() = validate(this@Customer)
}

fun Validation.validate(customer: Customer) = customer.schema {
    customer::id { ensurePositive(it) }
    customer::name { ensureNotBlank(it); ensureLengthInRange(it, 1..50) }
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

All validators are extension functions on `Validation` that take the input as the first parameter.

### String & CharSequence

Supported types: `String`, `CharSequence`

```kotlin
// Length validation
ensureMinLength(input, 1)                 // Minimum length
ensureMaxLength(input, 100)               // Maximum length
ensureLength(input, 10)                   // Exact length
ensureLengthInRange(input, 1..100)        // Length within range (supports both 1..100 and 1..<100)

// Content validation
ensureBlank(input)                        // Must be blank (empty or whitespace only)
ensureNotBlank(input)                     // Must not be blank
ensureEmpty(input)                        // Must be empty
ensureNotEmpty(input)                     // Must not be empty
ensureStartsWith(input, "prefix")         // Must start with prefix
ensureNotStartsWith(input, "prefix")      // Must not start with prefix
ensureEndsWith(input, "suffix")           // Must end with suffix
ensureNotEndsWith(input, "suffix")        // Must not end with suffix
ensureContains(input, "substring")        // Must contain substring
ensureNotContains(input, "substring")     // Must not contain substring
ensureMatches(input, Regex("\\d+"))       // Must match regex
ensureNotMatches(input, Regex("\\d+"))    // Must not match regex
ensureUppercase(input)                    // Must be uppercase
ensureLowercase(input)                    // Must be lowercase

// Comparable validation
ensureMin(input, "a")                      // Minimum value (>= "a")
ensureMax(input, "z")                      // Maximum value (<= "z")
ensureGreaterThan(input, "a")              // Greater than (> "a")
ensureGreaterThanOrEqual(input, "a")       // Greater than or equal (>= "a")
ensureLessThan(input, "z")                 // Less than (< "z")
ensureLessThanOrEqual(input, "z")          // Less than or equal (<= "z")
ensureEquals(input, "value")               // Equal to (== "value")
ensureNotEquals(input, "value")            // Not equal to (!= "value")

// String-specific validation
ensureInt(input)                           // Validates string is valid Int
ensureLong(input)                          // Validates string is valid Long
ensureShort(input)                         // Validates string is valid Short
ensureByte(input)                          // Validates string is valid Byte
ensureDouble(input)                        // Validates string is valid Double
ensureFloat(input)                         // Validates string is valid Float
ensureBigDecimal(input)                    // Validates string is valid BigDecimal
ensureBigInteger(input)                    // Validates string is valid BigInteger
ensureBoolean(input)                       // Validates string is valid Boolean
ensureEnum<Status>(input)                  // Validates string is valid enum value

// Conversions
parseInt(input)                            // Validate and convert to Int
parseLong(input)                           // Validate and convert to Long
parseShort(input)                          // Validate and convert to Short
parseByte(input)                           // Validate and convert to Byte
parseDouble(input)                         // Validate and convert to Double
parseFloat(input)                          // Validate and convert to Float
parseBigDecimal(input)                     // Validate and convert to BigDecimal
parseBigInteger(input)                     // Validate and convert to BigInteger
parseBoolean(input)                        // Validate and convert to Boolean
parseEnum<Status>(input)                   // Validate and convert to enum
```

### Numbers

Supported types: `Int`, `Long`, `Double`, `Float`, `Byte`, `Short`, `BigDecimal`, `BigInteger`

```kotlin
ensureMin(input, 0)                    // Minimum value (>= 0)
ensureMax(input, 100)                  // Maximum value (<= 100)
ensureGreaterThan(input, 0)            // Greater than (> 0)
ensureGreaterThanOrEqual(input, 0)     // Greater than or equal (>= 0)
ensureLessThan(input, 100)             // Less than (< 100)
ensureLessThanOrEqual(input, 100)      // Less than or equal (<= 100)
ensureEquals(input, 42)                // Equal to (== 42)
ensureNotEquals(input, 0)              // Not equal to (!= 0)
ensurePositive(input)                  // Must be positive (> 0)
ensureNegative(input)                  // Must be negative (< 0)
ensureNotPositive(input)               // Must not be positive (<= 0)
ensureNotNegative(input)               // Must not be negative (>= 0)
```

### Temporal Types

Supported types: `LocalDate`, `LocalTime`, `LocalDateTime`, `Instant`, `OffsetDateTime`, `OffsetTime`, `ZonedDateTime`, `Year`, `YearMonth`, `MonthDay`

```kotlin
ensureMin(input, LocalDate.of(2024, 1, 1))                 // Minimum date/time (>=)
ensureMax(input, LocalDate.of(2024, 12, 31))               // Maximum date/time (<=)
ensureGreaterThan(input, LocalDate.of(2024, 6, 1))         // Greater than (>)
ensureGreaterThanOrEqual(input, LocalDate.of(2024, 1, 1))  // Greater than or equal (>=)
ensureLessThan(input, LocalDate.of(2025, 1, 1))            // Less than (<)
ensureLessThanOrEqual(input, LocalDate.of(2024, 12, 31))   // Less than or equal (<=)
ensureEquals(input, LocalDate.of(2024, 6, 15))             // Equal to (==)
ensureNotEquals(input, LocalDate.of(2024, 1, 1))           // Not equal to (!=)
ensureFuture(input)                                        // Must be in the future
ensureFutureOrPresent(input)                               // Must be in the future or present
ensurePast(input)                                          // Must be in the past
ensurePastOrPresent(input)                                 // Must be in the past or present
```

### Iterables

Supported types: Any `Iterable` (including `List`, `Set`, `Collection`)

```kotlin
ensureNotEmpty(input)                      // Must not be empty
ensureContains(input, "foo")               // Must contain element (alias: ensureHas)
ensureNotContains(input, "bar")            // Must not contain element
ensureEach(input) { element ->             // Validate each element
    ensureMin(element, 1)
}
```

### Collections

Supported types: `List`, `Set`, `Collection`

```kotlin
ensureMinSize(input, 1)                    // Minimum size
ensureMaxSize(input, 10)                   // Maximum size
ensureSize(input, 5)                       // Exact size
ensureSizeInRange(input, 1..10)            // Size within range (supports both 1..10 and 1..<10)
```

### Maps

```kotlin
ensureMinSize(input, 1)                    // Minimum size
ensureMaxSize(input, 10)                   // Maximum size
ensureSize(input, 5)                       // Exact size
ensureSizeInRange(input, 1..10)            // Size within range (supports both 1..10 and 1..<10)
ensureNotEmpty(input)                      // Must not be empty
ensureContainsKey(input, "foo")            // Must contain key (alias: ensureHasKey)
ensureNotContainsKey(input, "bar")         // Must not contain key
ensureContainsValue(input, 42)             // Must contain value (alias: ensureHasValue)
ensureNotContainsValue(input, 0)           // Must not contain value
ensureEachKey(input) { key ->              // Validate each key
    ensureMin(key, 1)
}
ensureEachValue(input) { value ->          // Validate each value
    ensureMin(value, 0)
}
```

### Nullable

```kotlin
ensureNull(input)                          // Must be null
ensureNotNull(input)                       // Must not be null (enables smart casting, stops on failure)
ensureNullOr(input) { block }              // Accept null or validate non-null
```

### Boolean

```kotlin
ensureTrue(input)                          // Must be true
ensureFalse(input)                         // Must be false
```

### Comparable Types

Supports all `Comparable` types, such as `UInt`, `ULong`, `UByte`, and `UShort`.

```kotlin
ensureMin(input, 0u)                       // Minimum value (>= 0u)
ensureMax(input, 100u)                     // Maximum value (<= 100u)
ensureGreaterThan(input, 0u)               // Greater than (> 0u)
ensureGreaterThanOrEqual(input, 0u)        // Greater than or equal (>= 0u)
ensureLessThan(input, 100u)                // Less than (< 100u)
ensureLessThanOrEqual(input, 100u)         // Less than or equal (<= 100u)
ensureEquals(input, 42u)                   // Equal to (== 42u)
ensureNotEquals(input, 0u)                 // Not equal to (!= 0u)

// Range validation
ensureInRange(input, 1..10)                // Within range (supports both 1..10 and 1..<10 syntax)
ensureInClosedRange(input, 1.0..10.0)      // Within closed range (inclusive start and end)
ensureInOpenEndRange(input, 1..<10)        // Within open-ended range (inclusive start, exclusive end)
```

### Any Type Validators

Works with any type:

```kotlin
ensureEquals(input, "completed")                   // Must equal specific value
ensureNotEquals(input, "rejected")                 // Must not equal specific value
ensureInIterable(input, listOf("a", "b", "c"))     // Must be one of allowed values
```

## Error Handling

### Basic Error Handling

```kotlin
val result = tryValidate {
    val username = "joe"
    ensureMinLength(username, 5)
}

if (!result.isSuccess()) {
    result.messages.forEach {
        println(it)
        // Message(constraintId=kova.charSequence.minLength, text='must be at least 5 characters', root=, path=, input=joe, args=[5])
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
fun Validation.validate(product: Product) = product.schema {
    product::id { ensureMin(it, 1) }
    product::name {
        ensureNotBlank(it)
        ensureMinLength(it, 3)
        ensureMaxLength(it, 100)
    }
    product::price { ensureMin(it, 0.0) }
}

// Usage
val result = tryValidate {
    val product = Product(id = 0, name = "ab", price = 10.0)
    validate(product)
}

// Extract message details
if (!result.isSuccess()) {
    result.messages.forEach { message ->
        println("Constraint: ${message.constraintId}")      // kova.charSequence.minLength
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
    ensureMinLength(username, 3, message = { text("Username must be at least 3 characters") })

    // Internationalized message with parameters
    ensureMaxLength(bio, 500, message = { "custom.bio.tooLong".resource(500) })
}
```

## Validation Configuration

You can customize validation behavior using `ValidationConfig`:

### Fail-Fast Mode

Stop at the first error instead of collecting all errors:

```kotlin
fun Validation.validateProductName(name: String) {
    ensureNotBlank(name)
    ensureLengthInRange(name, 1..100)
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

fun Validation.validateDate(date: LocalDate) {
    ensureFuture(date)
}

val fixedClock = Clock.fixed(Instant.parse("2024-06-15T10:00:00Z"), ZoneId.of("UTC"))

val result = tryValidate(config = ValidationConfig(clock = fixedClock)) {
    val date = LocalDate.of(2024, 6, 20)
    ensureFuture(date)  // Uses the fixed clock for comparison
}
```

### Debug Logging

Enable logging to debug validation flow:

```kotlin
val result = tryValidate(config = ValidationConfig(
    logger = { logEntry -> println("[Validation] $logEntry") }
)) {
    ensureMinLength(username, 3)
    ensureMaxLength(username, 20)
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
fun Validation.isUrlPath(input: String) {
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
fun Validation.alphanumeric(
    input: String,
    message: MessageProvider = { "kova.string.alphanumeric".resource }
) = input.constrain("kova.string.alphanumeric") {
    satisfies(it.all { c -> c.isLetterOrDigit() }, message)
}
```

### Nullable Validation

```kotlin
// Accept or reject null
ensureNull(value)
ensureNotNull(value)

// Validate only if non-null
ensureNullOr(email) { ensureContains(it, "@") }

// ensureNotNull enables smart casting - subsequent validators work on non-null type
fun Validation.validateName(name: String?): String {
    ensureNotNull(name)           // Validates and enables smart cast
    ensureMinLength(name, 1)      // Compiler knows name is non-null
    ensureMaxLength(name, 100)
    return name             // Return type is String (non-nullable)
}
```

### Conditional Validation with `or` and `orElse`

Try the first validation; if it fails, try the next. Useful for alternative validation rules.

```kotlin
// Accept either domestic or international phone format
fun Validation.validatePhone(phone: String) =
    or { ensureMatches(phone, Regex("^\\d{3}-\\d{4}$")) }      // Domestic format: 123-4567
        .orElse { ensureMatches(phone, Regex("^\\+\\d{1,3}-\\d+$")) }  // International format: +1-1234567

val result = tryValidate { validatePhone("123-abc-456") }
if (!result.isSuccess()) {
    result.messages.map { it.text }.forEach { println(it) }
    // at least one constraint must be satisfied: [[must match pattern: ^\d{3}-\d{4}$], [must match pattern: ^\+\d{1,3}-\d+$]]
}

// Chain multiple alternatives
or { ensureMatches(id, Regex("^[a-z]+$")) }    // Lowercase letters only
    .or { ensureMatches(id, Regex("^\\d+$")) }  // Digits only
    .orElse { ensureMatches(id, Regex("^[A-Z]+$")) }  // Uppercase letters only
```

### Wrapping Errors with `withMessage`

The `withMessage` function wraps validation logic and consolidates multiple errors into a single custom message. This is useful when you want to present a higher-level error message instead of detailed field-level errors:

```kotlin
data class Address(val street: String, val city: String, val zipCode: String)

fun Validation.validate(address: Address) = address.schema {
    address::zipCode {
        withMessage("Invalid ZIP code format") {
            ensureMatches(it, Regex("^\\d{5}(-\\d{4})?$"))
            ensureMinLength(it, 5)
        }
    }
}

val result = tryValidate { validate(Address("Eitai", "Tokyo", "123-456")) }
if (!result.isSuccess()) {
    result.messages.forEach { println(it) }
    // Message(text='Invalid ZIP code format', root=Address, path=zipCode, input=null)
}
```

You can also use a transform function to customize how multiple errors are consolidated:

```kotlin
fun Validation.validatePassword(password: String) =
    withMessage({ messages ->
        text("Password validation failed: ${messages.size} errors found")
    }) {
        ensureMinLength(password, 8)
        ensureMatches(password, Regex(".*[A-Z].*"))
        ensureMatches(password, Regex(".*[0-9].*"))
    }
```

### Circular Reference Detection

Kova automatically detects and handles circular references in nested object validation to prevent infinite loops.

### Internationalization

Error messages use resource bundles from `kova.properties`. The `resource()` function creates internationalized messages with parameter substitution (using MessageFormat syntax where {0}, {1}, etc. are replaced with the provided arguments):

```kotlin
// Using resource keys from kova.properties
ensureMinLength(str, 5, message = { "custom.message.key".resource(5) })

// Multiple parameters
fun Validation.range(
    input: Int,
    minValue: Int,
    maxValue: Int,
    message: MessageProvider = { "kova.number.range".resource(minValue, maxValue) }
) = input.constrain("kova.number.range") {
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

- Kotlin 2.3.0+
- Java 17+
- Gradle 8.14+

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
