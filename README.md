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
val nameValidator = Kova.string().min(1).max(50).isNotBlank()

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
val emailValidator = Kova.string().isNotBlank() + Kova.string().contains("@")

// Use 'or' for alternative validations
val validator = Kova.string().isBlank() or Kova.string().min(5)

// Transform output with map
val intValidator = Kova.string().isInt().map { it.toString().toInt() }
```

### Object Validation

```kotlin
data class User(val id: Int, val name: String, val email: String)

// Define a validator for the User class
val userValidator = Kova.validator {
    User::class {
        User::id { Kova.int().min(1) }
        User::name { Kova.string().min(1).max(50) }
        User::email { Kova.string().isNotBlank().contains("@") }
    }
}

// Validate a user instance
val user = User(1, "Alice", "alice@example.com")
val result = userValidator.tryValidate(user)
```

### Object Construction with Validation

```kotlin
data class Person(val name: String, val age: Int)

// Create a factory that validates inputs and constructs objects
val personFactory = Kova.factory {
    val nameValidator = Kova.string().min(1).max(50)
    val ageValidator = Kova.int().min(0).max(150)
    ::Person { args(nameValidator, ageValidator) }
}

// Construct a person with validated inputs
val person = personFactory.create("Alice", 30)  // Returns Person or throws ValidationException

// Or use tryCreate for non-throwing validation
val result = personFactory.tryCreate("Alice", 30)  // Returns ValidationResult<Person>
```

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
val notNullValidator = Kova.nullable<String>().isNotNull()
val result = notNullValidator.tryValidate(null)              // Failure

// Accept null OR validate non-null values
val nullOrMinValidator = Kova.nullable<String>().isNullOr(Kova.string().min(5))
val result1 = nullOrMinValidator.tryValidate(null)           // Success(null)
val result2 = nullOrMinValidator.tryValidate("hello")        // Success("hello")
val result3 = nullOrMinValidator.tryValidate("hi")           // Failure (min(5) violated)

// Require non-null AND validate the value
val notNullAndMinValidator = Kova.nullable<String>().isNotNullAnd(Kova.string().min(5))
val result1 = notNullAndMinValidator.tryValidate(null)       // Failure (null not allowed)
val result2 = notNullAndMinValidator.tryValidate("hello")    // Success("hello")
val result3 = notNullAndMinValidator.tryValidate("hi")       // Failure (min(5) violated)
```

## Available Validators

### CharSequence/String

```kotlin
Kova.string()
    .min(1)                    // Minimum length
    .max(100)                  // Maximum length
    .length(10)                // Exact length
    .isBlank()                 // Must be blank
    .isNotBlank()              // Must not be blank
    .isEmpty()                 // Must be empty
    .isNotEmpty()              // Must not be empty
    .startsWith("prefix")      // Must start with prefix
    .endsWith("suffix")        // Must end with suffix
    .contains("substring")     // Must contain substring
    .isInt()                   // Must be a valid integer
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

```kotlin
enum class Status { ACTIVE, INACTIVE }

Kova.enum<Status>()
    .contains(Status.ACTIVE, Status.INACTIVE)
```

### Comparable Types

```kotlin
Kova.uInt()
Kova.uLong()
Kova.uByte()
Kova.uShort()

// Support min/max comparisons
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
val result = validator.tryValidate("ab", context = ValidationContext(failFast = true))

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