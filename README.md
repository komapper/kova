# Kova

A type-safe Kotlin validation library with composable validators and detailed error reporting.

> **⚠️ Note**: This project is currently under active development. The API may change until a stable 1.0.0 release.

## Setup

Add Kova to your Gradle project:

### Gradle Kotlin DSL (build.gradle.kts)

```kotlin
dependencies {
    // Core validation library
    implementation("org.komapper:kova-core:0.0.3")

    // Factory validation (optional)
    implementation("org.komapper:kova-factory:0.0.3")

    // Ktor integration (optional)
    implementation("org.komapper:kova-ktor:0.0.3")
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

## Quick Start

### Basic Validation

```kotlin
import org.komapper.extension.validator.*

val result = tryValidate {
    val name = "Wireless Mouse"
    notBlank(name)
    min(name, 1)
    max(name, 100)
    name
}

when {
    result.isSuccess() -> println("Valid: ${result.value}")
    else -> result.messages.forEach { println("Error: ${it.text}") }
}
```

### Reusable Validation Functions

Define extension functions on `Validation` for reusable validation logic:

```kotlin
fun Validation.validatePassword(password: String) {
    min(password, 8)
    matches(password, Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$"))
}

val result = tryValidate { validatePassword("SecurePass123") }
```

### Object Validation

```kotlin
data class Product(val id: Int, val name: String, val price: Double)

fun Validation.validateProduct(product: Product) = product.schema {
    product::id { min(it, 1) }
    product::name { notBlank(it); min(it, 1); max(it, 100) }
    product::price { min(it, 0.0) }
}

val result = tryValidate { validateProduct(Product(1, "Mouse", 29.99)) }
```

**Cross-property validation** validates relationships between multiple properties:

```kotlin
data class PriceRange(val minPrice: Double, val maxPrice: Double)

fun Validation.validatePriceRange(range: PriceRange) = range.schema {
    range::minPrice { notNegative(it) }
    range::maxPrice { notNegative(it) }
    // Validate relationship
    range.constrain("priceRange") {
        satisfies(it.minPrice <= it.maxPrice) {
            text("minPrice must be less than or equal to maxPrice")
        }
    }
}

val result = tryValidate { validatePriceRange(PriceRange(10.0, 100.0)) }
```

## Factory Validation

The `kova-factory` module provides a factory pattern for combining object construction and validation in a single operation. It's particularly useful when validating and transforming raw input (like form data or API requests) into typed objects.

```kotlin
import org.komapper.extension.validator.factory.*

data class User(val name: String, val age: Int)

fun Validation.buildUser(name: String, age: String) = factory {
    val name by bind(name) { notBlank(it); min(it, 1); it }
    val age by bind(age) { toInt(it) }
    User(name, age)
}

val result = tryValidate { buildUser("Alice", "25") }
```

**Key features:**
- Type-safe construction with automatic path tracking via property delegation
- Composable factories for building complex nested object hierarchies
- Validates and transforms inputs before object creation

For detailed documentation including nested factories, error reporting, and advanced usage patterns, see **[kova-factory/README.md](kova-factory/README.md)**.

## Ktor Integration

The `kova-ktor` module enables automatic request validation with Ktor's RequestValidation plugin:

```kotlin
@Serializable
data class Customer(val id: Int, val name: String) : Validated {
    override fun Validation.validate() = validate(this@Customer)
}

fun Validation.validateCustomer(customer: Customer) = customer.schema {
    customer::id { positive(it) }
    customer::name { notBlank(it); min(it, 1); max(it, 50) }
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

```kotlin
min(input, 1)                       // Minimum length
max(input, 100)                     // Maximum length
length(input, 10)                   // Exact length
blank(input)                        // Must be blank (empty or whitespace only)
notBlank(input)                     // Must not be blank
empty(input)                        // Must be empty
notEmpty(input)                     // Must not be empty
startsWith(input, "prefix")         // Must start with prefix
notStartsWith(input, "prefix")      // Must not start with prefix
endsWith(input, "suffix")           // Must end with suffix
notEndsWith(input, "suffix")        // Must not end with suffix
contains(input, "substring")        // Must contain substring
notContains(input, "substring")     // Must not contain substring
matches(input, Regex("\\d+"))       // Must match regex
notMatches(input, Regex("\\d+"))    // Must not match regex
uppercase(input)                    // Must be uppercase
lowercase(input)                    // Must be lowercase

// String-specific validation
isInt(input)                        // Validates string is valid Int
isLong(input)                       // Validates string is valid Long
isShort(input)                      // Validates string is valid Short
isByte(input)                       // Validates string is valid Byte
isDouble(input)                     // Validates string is valid Double
isFloat(input)                      // Validates string is valid Float
isBigDecimal(input)                 // Validates string is valid BigDecimal
isBigInteger(input)                 // Validates string is valid BigInteger
isBoolean(input)                    // Validates string is valid Boolean
isEnum<Status>(input)               // Validates string is valid enum value

// Conversions
toInt(input)                        // Validate and convert to Int
toLong(input)                       // Validate and convert to Long
toShort(input)                      // Validate and convert to Short
toByte(input)                       // Validate and convert to Byte
toDouble(input)                     // Validate and convert to Double
toFloat(input)                      // Validate and convert to Float
toBigDecimal(input)                 // Validate and convert to BigDecimal
toBigInteger(input)                 // Validate and convert to BigInteger
toBoolean(input)                    // Validate and convert to Boolean
toEnum<Status>(input)               // Validate and convert to enum
```

### Numbers

Supported types: `Int`, `Long`, `Double`, `Float`, `Byte`, `Short`, `BigDecimal`, `BigInteger`

```kotlin
min(input, 0)                       // Minimum value (>= 0)
max(input, 100)                     // Maximum value (<= 100)
gt(input, 0)                        // Greater than (> 0)
gte(input, 0)                       // Greater than or equal (>= 0)
lt(input, 100)                      // Less than (< 100)
lte(input, 100)                     // Less than or equal (<= 100)
eq(input, 42)                       // Equal to (== 42)
notEq(input, 0)                     // Not equal to (!= 0)
positive(input)                     // Must be positive (> 0)
negative(input)                     // Must be negative (< 0)
notPositive(input)                  // Must not be positive (<= 0)
notNegative(input)                  // Must not be negative (>= 0)
```

### Temporal Types

Supported types: `LocalDate`, `LocalTime`, `LocalDateTime`, `Instant`, `OffsetDateTime`, `OffsetTime`, `ZonedDateTime`, `Year`, `YearMonth`, `MonthDay`

```kotlin
min(input, LocalDate.of(2024, 1, 1))     // Minimum date/time (>=)
max(input, LocalDate.of(2024, 12, 31))   // Maximum date/time (<=)
gt(input, LocalDate.of(2024, 6, 1))      // Greater than (>)
gte(input, LocalDate.of(2024, 1, 1))     // Greater than or equal (>=)
lt(input, LocalDate.of(2025, 1, 1))      // Less than (<)
lte(input, LocalDate.of(2024, 12, 31))   // Less than or equal (<=)
eq(input, LocalDate.of(2024, 6, 15))     // Equal to (==)
notEq(input, LocalDate.of(2024, 1, 1))   // Not equal to (!=)
future(input)                             // Must be in the future
futureOrPresent(input)                    // Must be in the future or present
past(input)                               // Must be in the past
pastOrPresent(input)                      // Must be in the past or present
```

### Collections

Supported types: `List`, `Set`, `Collection`

```kotlin
min(input, 1)                       // Minimum size
max(input, 10)                      // Maximum size
size(input, 5)                      // Exact size
notEmpty(input)                     // Must not be empty
contains(input, "foo")              // Must contain element (alias: has)
notContains(input, "bar")           // Must not contain element
onEach(input) { element ->          // Validate each element
    min(element, 1)
}
```

### Maps

```kotlin
min(input, 1)                       // Minimum size
max(input, 10)                      // Maximum size
size(input, 5)                      // Exact size
notEmpty(input)                     // Must not be empty
containsKey(input, "foo")           // Must contain key (alias: hasKey)
notContainsKey(input, "bar")        // Must not contain key
containsValue(input, 42)            // Must contain value (alias: hasValue)
notContainsValue(input, 0)          // Must not contain value
onEachKey(input) { key ->           // Validate each key
    min(key, 1)
}
onEachValue(input) { value ->       // Validate each value
    min(value, 0)
}
```

### Nullable

```kotlin
isNull(input)                       // Must be null
notNull(input)                      // Must not be null
isNullOr(input) { block }           // Accept null or validate non-null
toNonNullable(input)                // Convert nullable to non-nullable (fails if null)
```

### Comparable Types

Supported types: `UInt`, `ULong`, `UByte`, `UShort`

```kotlin
min(input, 0u)                      // Minimum value (>= 0u)
max(input, 100u)                    // Maximum value (<= 100u)
gt(input, 0u)                       // Greater than (> 0u)
gte(input, 0u)                      // Greater than or equal (>= 0u)
lt(input, 100u)                     // Less than (< 100u)
lte(input, 100u)                    // Less than or equal (<= 100u)
eq(input, 42u)                      // Equal to (== 42u)
notEq(input, 0u)                    // Not equal to (!= 0u)
```

### Literal Values

```kotlin
literal(input, "completed")         // Must equal specific value
literal(input, "a", "b", "c")       // Must be one of allowed values
```

## Error Handling

### Basic Error Handling

```kotlin
val result = tryValidate {
    val password = "pass"
    min(password, 8)
}

if (!result.isSuccess()) {
    result.messages.forEach { message ->
        println("${message.path}: ${message.text}")
    }
}
```

### Fail-Fast Mode

```kotlin
val result = tryValidate(config = ValidationConfig(failFast = true)) {
    min(password, 8)
    max(password, 20)
}  // Stops at first error
```

### Custom Error Messages

```kotlin
val result = tryValidate {
    min(username, 3, message = { text("Username must be at least 3 characters") })
}
```

## Advanced Topics

### Custom Constraints

Create custom validators using `constrain` and `satisfies`:

```kotlin
fun Validation.isUrlPath(input: String) {
    input.constrain("custom.urlPath") {
        satisfies(it.startsWith("/") && !it.contains("..")) {
            text("Must be a valid URL path")
        }
    }
}
```

### Nullable Validation

```kotlin
// Accept or reject null
isNull(value)
notNull(value)

// Validate only if non-null
isNullOr(email) { contains(it, "@") }

// Convert nullable to non-nullable with validation
val name = toNonNullable(nullableName)
```

### Circular Reference Detection

Kova automatically detects and handles circular references in nested object validation to prevent infinite loops.

### Internationalization

Error messages use resource bundles from `kova.properties`. Custom messages can be provided using the `message` parameter:

```kotlin
min(str, 5, message = { "custom.message.key".resource(5) })
```

## Building and Testing

```bash
# Run all tests
./gradlew test

# Run tests for kova-core module
./gradlew kova-core:test

# Run tests for kova-ktor module
./gradlew kova-ktor:test

# Build the project
./gradlew build

# Format code
./gradlew spotlessApply
```

## Requirements

- Kotlin 1.9+
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
