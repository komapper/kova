# Kova

A type-safe Kotlin validation library with composable validators and detailed error reporting.

> **⚠️ Note**: This project is currently under active development. The API may change until a stable 1.0.0 release.

## Setup

Add Kova to your Gradle project:

### Gradle Kotlin DSL (build.gradle.kts)

```kotlin
dependencies {
    // Core validation library
    implementation("org.komapper:kova-core:0.0.4")

    // Factory validation (optional)
    implementation("org.komapper:kova-factory:0.0.4")

    // Ktor integration (optional)
    implementation("org.komapper:kova-ktor:0.0.4")
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

Validate individual values by calling validator functions within a `tryValidate` block. Each validator is an extension function on `Validation` that takes the input as the first parameter.

```kotlin
import org.komapper.extension.validator.*

val result = tryValidate {
    val name = "Wireless Mouse"
    notBlank(name)
    minLength(name, 1)
    maxLength(name, 100)
    name
}

when {
    result.isSuccess() -> println("Valid: ${result.value}")
    else -> result.messages.forEach { println("Error: $it") }
}
```

Alternatively, use `validate` to get the value directly or throw a `ValidationException`:

```kotlin
try {
    val name = validate {
        val name = "Wireless Mouse"
        notBlank(name)
        minLength(name, 1)
        maxLength(name, 100)
        name
    }
    println("Valid: $name")
} catch (e: ValidationException) {
    e.messages.forEach { println("Error: $it") }
}
```

### Reusable Validation Functions

Define extension functions on `Validation` for reusable validation logic:

```kotlin
fun Validation.validatePassword(password: String) {
    minLength(password, 8)
    matches(password, Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$"))
}

val result = tryValidate { validatePassword("SecurePass123") }
```

### Object Validation

Validate data class properties using the `schema` function.

```kotlin
data class Product(val id: Int, val name: String, val price: Double)

fun Validation.validate(product: Product) = product.schema {
    product::id { minValue(it, 1) }
    product::name { notBlank(it); minLength(it, 1); maxLength(it, 100) }
    product::price { minValue(it, 0.0) }
}

val result = tryValidate { validate(Product(1, "Mouse", 29.99)) }
```

#### Nested object validation

Validate nested objects by calling their validation functions within the parent's schema. Error messages include the full path to the failed property:

```kotlin
data class Address(val street: String, val city: String, val zipCode: String)
data class Customer(val name: String, val email: String, val address: Address)

fun Validation.validate(address: Address) = address.schema {
    address::street { notBlank(it); minLength(it, 1) }
    address::city { notBlank(it); minLength(it, 1) }
    address::zipCode { matches(it, Regex("^\\d{5}(-\\d{4})?$")) }
}

fun Validation.validate(customer: Customer) = customer.schema {
    customer::name { notBlank(it); minLength(it, 1); maxLength(it, 100) }
    customer::email { notBlank(it); contains(it, "@") }
    customer::address { validate(it) }  // Nested validation
}

val result = tryValidate {
    validate(Customer(
        name = "John Doe",
        email = "invalid-email",
        address = Address(street = "", city = "Tokyo", zipCode = "123")
    ))
}

if (!result.isSuccess()) {
    result.messages.forEach { println(it) }
    // Message(constraintId=kova.charSequence.contains, text='must contain "@"', root=Customer, path=email, input=invalid-email, args=[@])
    // Message(constraintId=kova.charSequence.notBlank, text='must not be blank', root=Customer, path=address.street, input=, args=[])
    // Message(constraintId=kova.charSequence.minLength, text='must be at least 1 characters', root=Customer, path=address.street, input=, args=[1])
    // Message(constraintId=kova.charSequence.matches, text='must match pattern: ^\d{5}(-\d{4})?$', root=Customer, path=address.zipCode, input=123, args=[^\d{5}(-\d{4})?$])
}
```

Notice how the error messages show the full path (e.g., `address.street`, `address.zipCode`) to pinpoint exactly where validation failed in the nested structure.

#### Cross-property validation

Validates relationships between multiple properties using `constrain` within a `schema` block:

```kotlin
data class PriceRange(val minPrice: Double, val maxPrice: Double)

fun Validation.validate(range: PriceRange) = range.schema {
    range::minPrice { notNegative(it) }
    range::maxPrice { notNegative(it) }

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
    val name by bind(name) { notBlank(it); minLength(it, 1); it }
    val age by bind(age) { toInt(it) }
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
    customer::id { positive(it) }
    customer::name { notBlank(it); minLength(it, 1); maxLength(it, 50) }
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
minLength(input, 1)                 // Minimum length
maxLength(input, 100)               // Maximum length
length(input, 10)                   // Exact length

// Content validation
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

// Comparable validation
minValue(input, "a")                // Minimum value (>= "a")
maxValue(input, "z")                // Maximum value (<= "z")
gtValue(input, "a")                 // Greater than (> "a")
gtEqValue(input, "a")               // Greater than or equal (>= "a")
ltValue(input, "z")                 // Less than (< "z")
ltEqValue(input, "z")               // Less than or equal (<= "z")
eqValue(input, "value")             // Equal to (== "value")
notEqValue(input, "value")          // Not equal to (!= "value")

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
minValue(input, 0)                  // Minimum value (>= 0)
maxValue(input, 100)                // Maximum value (<= 100)
gtValue(input, 0)                   // Greater than (> 0)
gtEqValue(input, 0)                 // Greater than or equal (>= 0)
ltValue(input, 100)                 // Less than (< 100)
ltEqValue(input, 100)               // Less than or equal (<= 100)
eqValue(input, 42)                  // Equal to (== 42)
notEqValue(input, 0)                // Not equal to (!= 0)
positive(input)                     // Must be positive (> 0)
negative(input)                     // Must be negative (< 0)
notPositive(input)                  // Must not be positive (<= 0)
notNegative(input)                  // Must not be negative (>= 0)
```

### Temporal Types

Supported types: `LocalDate`, `LocalTime`, `LocalDateTime`, `Instant`, `OffsetDateTime`, `OffsetTime`, `ZonedDateTime`, `Year`, `YearMonth`, `MonthDay`

```kotlin
minValue(input, LocalDate.of(2024, 1, 1))     // Minimum date/time (>=)
maxValue(input, LocalDate.of(2024, 12, 31))   // Maximum date/time (<=)
gtValue(input, LocalDate.of(2024, 6, 1))      // Greater than (>)
gtEqValue(input, LocalDate.of(2024, 1, 1))    // Greater than or equal (>=)
ltValue(input, LocalDate.of(2025, 1, 1))      // Less than (<)
ltEqValue(input, LocalDate.of(2024, 12, 31))  // Less than or equal (<=)
eqValue(input, LocalDate.of(2024, 6, 15))     // Equal to (==)
notEqValue(input, LocalDate.of(2024, 1, 1))   // Not equal to (!=)
future(input)                             // Must be in the future
futureOrPresent(input)                    // Must be in the future or present
past(input)                               // Must be in the past
pastOrPresent(input)                      // Must be in the past or present
```

### Iterables

Supported types: Any `Iterable` (including `List`, `Set`, `Collection`)

```kotlin
notEmpty(input)                     // Must not be empty
contains(input, "foo")              // Must contain element (alias: has)
notContains(input, "bar")           // Must not contain element
onEach(input) { element ->          // Validate each element
    minValue(element, 1)
}
```

### Collections

Supported types: `List`, `Set`, `Collection`

```kotlin
minSize(input, 1)                   // Minimum size
maxSize(input, 10)                  // Maximum size
size(input, 5)                      // Exact size
```

### Maps

```kotlin
minSize(input, 1)                   // Minimum size
maxSize(input, 10)                  // Maximum size
size(input, 5)                      // Exact size
notEmpty(input)                     // Must not be empty
containsKey(input, "foo")           // Must contain key (alias: hasKey)
notContainsKey(input, "bar")        // Must not contain key
containsValue(input, 42)            // Must contain value (alias: hasValue)
notContainsValue(input, 0)          // Must not contain value
onEachKey(input) { key ->           // Validate each key
    minValue(key, 1)
}
onEachValue(input) { value ->       // Validate each value
    minValue(value, 0)
}
```

### Nullable

```kotlin
isNull(input)                       // Must be null
notNull(input)                      // Must not be null (enables smart casting, stops on failure)
isNullOr(input) { block }           // Accept null or validate non-null
```

### Boolean

```kotlin
isTrue(input)                       // Must be true
isFalse(input)                      // Must be false
```

### Comparable Types

Supports all `Comparable` types, such as `UInt`, `ULong`, `UByte`, and `UShort`.

```kotlin
minValue(input, 0u)                 // Minimum value (>= 0u)
maxValue(input, 100u)               // Maximum value (<= 100u)
gtValue(input, 0u)                  // Greater than (> 0u)
gtEqValue(input, 0u)                // Greater than or equal (>= 0u)
ltValue(input, 100u)                // Less than (< 100u)
ltEqValue(input, 100u)              // Less than or equal (<= 100u)
eqValue(input, 42u)                 // Equal to (== 42u)
notEqValue(input, 0u)               // Not equal to (!= 0u)

// Range validation
inRange(input, 1..10)               // Within range (supports both 1..10 and 1..<10 syntax)
inRange(input, 1..<10)              // Within range with open-ended syntax (exclusive end)
inClosedRange(input, 1.0..10.0)     // Within closed range (inclusive start and end)
inOpenEndRange(input, 1..<10)       // Within open-ended range (inclusive start, exclusive end)
```

### Any Type Validators

Works with any type:

```kotlin
eqValue(input, "completed")            // Must equal specific value
notEqValue(input, "rejected")          // Must not equal specific value
inIterable(input, listOf("a", "b", "c"))  // Must be one of allowed values
```

## Error Handling

### Basic Error Handling

```kotlin
val result = tryValidate {
    val username = "joe"
    minLength(username, 5)
}

if (!result.isSuccess()) {
    result.messages.forEach {
        println(it)
        // Message(constraintId=kova.charSequence.minLength, text='must be at least 5 characters', root=, path=, input=joe, args=[5])
    }
}
```

### Validation Configuration

You can customize validation behavior using `ValidationConfig`:

#### Fail-Fast Mode

Stop at the first error instead of collecting all errors:

```kotlin
val result = tryValidate(config = ValidationConfig(failFast = true)) {
    minLength(password, 8)
    maxLength(password, 20)
    matches(password, Regex(".*[A-Z].*"))
}  // Stops at first error
```

#### Custom Clock for Temporal Validation

Provide a custom clock for temporal validators (useful for testing):

```kotlin
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

val fixedClock = Clock.fixed(Instant.parse("2024-06-15T10:00:00Z"), ZoneId.of("UTC"))

val result = tryValidate(config = ValidationConfig(clock = fixedClock)) {
    val date = LocalDate.of(2024, 6, 20)
    future(date)  // Uses the fixed clock for comparison
}
```

#### Debug Logging

Enable logging to debug validation flow:

```kotlin
val result = tryValidate(config = ValidationConfig(
    logger = { logEntry -> println("[Validation] $logEntry") }
)) {
    minLength(username, 3)
    maxLength(username, 20)
}
```

#### Combined Configuration

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

### Custom Error Messages

All validators accept an optional `message` parameter for custom error messages. You can use `text()` for plain text messages or `resource()` for internationalized messages:

```kotlin
val result = tryValidate {
    // Custom text message
    minLength(username, 3, message = { text("Username must be at least 3 characters") })

    // Internationalized message with parameters
    maxLength(bio, 500, message = { "custom.bio.tooLong".resource(500) })
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
isNull(value)
notNull(value)

// Validate only if non-null
isNullOr(email) { contains(it, "@") }

// notNull enables smart casting - subsequent validators work on non-null type
fun Validation.validateName(name: String?): String {
    notNull(name)           // Validates and enables smart cast
    minLength(name, 1)      // Compiler knows name is non-null
    maxLength(name, 100)
    return name             // Return type is String (non-nullable)
}
```

### Conditional Validation with `or` and `orElse`

Try the first validation; if it fails, try the next. Useful for alternative validation rules.

```kotlin
// Accept either domestic or international phone format
fun Validation.validatePhone(phone: String) =
    or { matches(phone, Regex("^\\d{3}-\\d{4}$")) }      // Domestic format: 123-4567
        .orElse { matches(phone, Regex("^\\+\\d{1,3}-\\d+$")) }  // International format: +1-1234567

val result = tryValidate { validatePhone("123-abc-456") }
if (!result.isSuccess()) {
    result.messages.map { it.text }.forEach { println(it) }
    // at least one constraint must be satisfied: [[must match pattern: ^\d{3}-\d{4}$], [must match pattern: ^\+\d{1,3}-\d+$]]
}

// Chain multiple alternatives
or { matches(id, Regex("^[a-z]+$")) }    // Lowercase letters only
    .or { matches(id, Regex("^\\d+$")) }  // Digits only
    .orElse { matches(id, Regex("^[A-Z]+$")) }  // Uppercase letters only
```

### Wrapping Errors with `withMessage`

The `withMessage` function wraps validation logic and consolidates multiple errors into a single custom message. This is useful when you want to present a higher-level error message instead of detailed field-level errors:

```kotlin
data class Address(val street: String, val city: String, val zipCode: String)

fun Validation.validate(address: Address) = address.schema {
    address::zipCode {
        withMessage("Invalid ZIP code format") {
            matches(it, Regex("^\\d{5}(-\\d{4})?$"))
            minLength(it, 5)
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
        minLength(password, 8)
        matches(password, Regex(".*[A-Z].*"))
        matches(password, Regex(".*[0-9].*"))
    }
```

### Circular Reference Detection

Kova automatically detects and handles circular references in nested object validation to prevent infinite loops.

### Internationalization

Error messages use resource bundles from `kova.properties`. The `resource()` function creates internationalized messages with parameter substitution (using MessageFormat syntax where {0}, {1}, etc. are replaced with the provided arguments):

```kotlin
// Using resource keys from kova.properties
minLength(str, 5, message = { "custom.message.key".resource(5) })

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
