# Kova

A type-safe Kotlin validation library that provides composable validators through a fluent API.

> **⚠️ Note**: This project is currently under active development. The API may change until a stable 1.0.0 release.

## Features

- **Type-Safe**: Leverages Kotlin's type system for compile-time safety
- **Composable**: Combine validators using intuitive operators (`+`, `and`, `or`)
- **Immutable**: All validators are immutable and thread-safe
- **Detailed Error Reporting**: Get precise error messages with path tracking for nested validations
- **Internationalization**: Built-in support for localized error messages
- **Fail-Fast Support**: Option to stop validation at the first error or collect all errors
- **Nullable Support**: First-class support for nullable types
- **Object Construction**: Validate inputs and construct objects in a single step
- **Reflection-Based**: Uses Kotlin reflection for property binding and object construction

## Quick Start

### Basic Validation

```kotlin
import org.komapper.extension.validator.Kova

// Create a validator
val nameValidator = Kova.string().min(1).max(50).notBlank()

// Validate a value
val result = nameValidator.tryValidate("John")

// Check the result
if (result.isSuccess()) {
    println("Valid: ${result.value}")
} else {
    println("Errors: ${result.messages}")
}

// Or use validate() which throws ValidationException on failure
val name = nameValidator.validate("John")
```

### Validator Composition

```kotlin
// Combine validators with + operator (or 'and')
val emailValidator = Kova.string().notBlank() + Kova.string().contains("@")

// Use 'or' for alternative validations
val validator = Kova.string().uppercase() or Kova.string().min(5)

// Transform output with map
val intValidator = Kova.string().toInt()  // Validates and converts to Int
```

### Object Validation

```kotlin
data class User(val id: Int, val name: String, val email: String)

// Define a schema for the User class
object UserSchema : ObjectSchema<User>({
    User::id { Kova.int().min(1) }
    User::name { Kova.string().min(1).max(50) }
    User::email { Kova.string().notBlank().contains("@") }
}) {}

// Validate a user instance
val user = User(1, "Alice", "alice@example.com")
val result = UserSchema.tryValidate(user)
```

**Note**: Properties are now defined within the constructor lambda scope, and the object body is typically empty (`{}`).

### Object-Level Constraints

You can add constraints that validate relationships between properties using the `constrain` method:

```kotlin
import java.time.LocalDate
import java.time.Clock

data class Period(val startDate: LocalDate, val endDate: LocalDate)

object PeriodSchema : ObjectSchema<Period>({
    Period::startDate { Kova.localDate(Clock.systemDefaultZone()) }
    Period::endDate { Kova.localDate(Clock.systemDefaultZone()) }

    constrain("dateRange") {
        satisfies(
            it.input.startDate <= it.input.endDate,
            "startDate must be less than or equal to endDate"
        )
    }
}) {}

val result = PeriodSchema.tryValidate(Period(
    LocalDate.of(2024, 1, 1),
    LocalDate.of(2023, 1, 1)
))
// Validation fails with message: "startDate must be less than or equal to endDate"
```

### Object Construction with Validation

```kotlin
data class Person(val name: String, val age: Int)

// Define a schema with properties that can be referenced by the factory
object PersonSchema : ObjectSchema<Person>() {
    val name = Person::name { Kova.string().min(1).max(50) }
    val age = Person::age { Kova.int().min(0).max(150) }
}

// Create a factory that validates inputs and constructs objects
val personFactory = Kova.args(PersonSchema.name, PersonSchema.age).createFactory(::Person)

// Construct a person with validated inputs
val person = personFactory.create("Alice", 30)  // Returns Person or throws ValidationException

// Or use tryCreate for non-throwing validation
val result = personFactory.tryCreate("Alice", 30)  // Returns ValidationResult<Person>
```

**Note**: When using `ObjectFactory`, properties must be defined as object properties (not within the constructor lambda) so they can be referenced by `Kova.args()`. The `Kova.args()` method supports 1 to 10 arguments through `Arguments1` to `Arguments10` classes.

### Nullable Validation

Kova provides first-class support for nullable types:

```kotlin
// Create a nullable validator
// By default, null values are considered valid
val nullableNameValidator = Kova.nullable(Kova.string().min(1))
val result1 = nullableNameValidator.tryValidate(null)        // Success(null)
val result2 = nullableNameValidator.tryValidate("hi")        // Success("hi")
val result3 = nullableNameValidator.tryValidate("")          // Failure (min(1) violated)

// Reject null values explicitly
val notNullValidator = Kova.nullable<String>().notNull()
val result = notNullValidator.tryValidate(null)              // Failure

// Accept only null values
val isNullValidator = Kova.nullable<String>().isNull()
val result = isNullValidator.tryValidate(null)               // Success(null)

// Accept null OR validate non-null values
val nullOrMinValidator = Kova.nullable<String>().isNullOr(Kova.string().min(5))
val result1 = nullOrMinValidator.tryValidate(null)           // Success(null)
val result2 = nullOrMinValidator.tryValidate("hello")        // Success("hello")
val result3 = nullOrMinValidator.tryValidate("hi")           // Failure (min(5) violated)

// Require non-null AND validate the value
val notNullAndMinValidator = Kova.nullable<String>().notNullAnd(Kova.string().min(5))
val result1 = notNullAndMinValidator.tryValidate(null)       // Failure (null not allowed)
val result2 = notNullAndMinValidator.tryValidate("hello")    // Success("hello")
val result3 = notNullAndMinValidator.tryValidate("hi")       // Failure (min(5) violated)

// Convert any validator to nullable using asNullable()
val validator = Kova.string().min(5).asNullable()
```

**Note**: `notNull()` and `notNullAnd()` return a `NotNullValidator`, which is a specialized validator that enforces non-null constraints while maintaining type safety.

## Available Validators

### String

```kotlin
Kova.string()
    .min(1)                    // Minimum length
    .max(100)                  // Maximum length
    .length(10)                // Exact length
    .notBlank()                // Must not be blank
    .notEmpty()                // Must not be empty
    .startsWith("prefix")      // Must start with prefix
    .endsWith("suffix")        // Must end with suffix
    .contains("substring")     // Must contain substring
    .matches(Regex("\\d+"))    // Must match regex pattern
    .email()                   // Must be a valid email address
    .isInt()                   // Must be a valid integer string
    .isLong()                  // Must be a valid long string
    .isShort()                 // Must be a valid short string
    .isByte()                  // Must be a valid byte string
    .isDouble()                // Must be a valid double string
    .isFloat()                 // Must be a valid float string
    .isBigDecimal()            // Must be a valid BigDecimal string
    .isBigInteger()            // Must be a valid BigInteger string
    .isBoolean()               // Must be a valid boolean string
    .isEnum<Status>()          // Must be a valid enum value (inline)
    .uppercase()               // Must be uppercase
    .lowercase()               // Must be lowercase
    .trim()                    // Transform: trim whitespace
    .toUpperCase()             // Transform: convert to uppercase
    .toLowerCase()             // Transform: convert to lowercase
    .toInt()                   // Transform: validate and convert to Int
    .toLong()                  // Transform: validate and convert to Long
    .toShort()                 // Transform: validate and convert to Short
    .toByte()                  // Transform: validate and convert to Byte
    .toDouble()                // Transform: validate and convert to Double
    .toFloat()                 // Transform: validate and convert to Float
    .toBigDecimal()            // Transform: validate and convert to BigDecimal
    .toBigInteger()            // Transform: validate and convert to BigInteger
    .toBoolean()               // Transform: validate and convert to Boolean
    .toEnum<Status>()          // Transform: validate and convert to Enum
```

### Numbers

```kotlin
Kova.int()         // Int
Kova.long()        // Long
Kova.double()      // Double
Kova.float()       // Float
Kova.byte()        // Byte
Kova.short()       // Short
Kova.bigDecimal()  // BigDecimal
Kova.bigInteger()  // BigInteger

// All numeric validators support:
    .min(0)        // Minimum value
    .max(100)      // Maximum value
```

### Boolean

```kotlin
Kova.boolean()     // Returns generic validator for boolean values
```

### LocalDate

```kotlin
import java.time.Clock

Kova.localDate(Clock.systemDefaultZone())
    .future()              // Must be in the future
    .futureOrPresent()     // Must be in the future or present
    .past()                // Must be in the past
    .pastOrPresent()       // Must be in the past or present
```

### Collections

```kotlin
Kova.list<String>()
    .min(1)     // Minimum size
    .max(10)    // Maximum size
    // Can also validate elements with map/andThen

Kova.set<Int>()
Kova.collection<String>()
```

### Maps

```kotlin
Kova.map<String, Int>()
    .min(1)     // Minimum size
    // Can validate keys and values with map/andThen
```

### Enums

Enum validation is now string-based via `StringValidator`:

```kotlin
enum class Status { ACTIVE, INACTIVE }

// Validate that a string is a valid enum value
Kova.string().isEnum<Status>()

// Validate and convert to enum
Kova.string().toEnum<Status>()

// Alternative: Use literal validator for enum values directly
Kova.literal(Status.ACTIVE, Status.INACTIVE)
```

### Comparable Types

```kotlin
Kova.uInt()
Kova.uLong()
Kova.uByte()
Kova.uShort()

// Support min/max comparisons
```

### Generic Validator

```kotlin
Kova.generic<T>()  // No-op validator that always succeeds
                   // Useful as a base for custom validators or with nullable()
```

## Error Handling

### Collecting All Errors

```kotlin
val validator = Kova.string().min(3).length(4)
val result = validator.tryValidate("ab")

if (result.isFailure()) {
    // Get all error messages
    result.messages.forEach { println(it) }
    // Output:
    // "ab" must be at least 3 characters
    // "ab" must be exactly 4 characters
}
```

### Fail-Fast Mode

```kotlin
val validator = Kova.string().min(3).length(4)
val result = validator.tryValidate("ab", failFast = true)

if (result.isFailure()) {
    // Only the first error is reported
    result.messages.size shouldBe 1
}
```

### Path Tracking for Nested Objects

```kotlin
val result = userValidator.tryValidate(User(-1, "", "invalid"))

result.details.forEach { detail ->
    println("Root: ${detail.root}")      // e.g., "User"
    println("Path: ${detail.path}")      // e.g., "name"
    println("Messages: ${detail.messages}")
}
```

## Custom Constraints

You can add custom constraints to any validator:

```kotlin
val validator = Kova.string().constraint { ctx ->
    Constraint.satisfies(
        ctx.input.contains("@") && ctx.input.contains("."),
        Message.Text("Must be a valid email format")
    )
}
```

## Internationalization

Error messages are internationalized using resource bundles. The default messages are in `kova.properties`:

```properties
kova.charSequence.min="{0}" must be at least {1} characters
kova.charSequence.max="{0}" must be at most {1} characters
kova.number.min=Number {0} must be greater than or equal to {1}
# ... more messages
```

You can customize messages per validation:

```kotlin
val validator = Kova.string().min(
    length = 5,
    message = { ctx, len -> Message.Text("String '${ctx.input}' is too short (min: $len)") }
)
```

## Building and Testing

```bash
# Run all tests
./gradlew test

# Run tests for kova-core module
./gradlew kova-core:test

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