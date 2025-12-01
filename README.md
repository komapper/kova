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
    result.messages.forEach { message ->
        println("Error: ${message.content}")
    }
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
object UserSchema : ObjectSchema<User>() {
    val id = User::id { Kova.int().min(1) }
    val name = User::name { Kova.string().min(1).max(50) }
    val email = User::email { Kova.string().notBlank().contains("@") }
}

// Validate a user instance
val user = User(1, "Alice", "alice@example.com")
val result = UserSchema.tryValidate(user)
```

**Note**: Properties must be defined as object properties (outside the constructor lambda), as the `invoke` operator for property definitions is only available on the `ObjectSchema` class itself, not within the lambda scope.

### Object-Level Constraints

You can add constraints that validate relationships between properties using the `constrain` method within the constructor lambda:

```kotlin
import java.time.LocalDate

data class Period(val startDate: LocalDate, val endDate: LocalDate)

object PeriodSchema : ObjectSchema<Period>({
    constrain("dateRange") {
        satisfies(
            it.input.startDate <= it.input.endDate,
            "startDate must be less than or equal to endDate"
        )
    }
}) {
    val startDate = Period::startDate { Kova.localDate() }
    val endDate = Period::endDate { Kova.localDate() }
}

val result = PeriodSchema.tryValidate(Period(
    LocalDate.of(2024, 1, 1),
    LocalDate.of(2023, 1, 1)
))
// Validation fails with message: "startDate must be less than or equal to endDate"
```

**Note**: Even when using object-level constraints, properties must still be defined as object properties (outside the constructor lambda). Only the `constrain()` calls are placed within the constructor lambda. The lambda provides access to `ObjectSchemaScope` which includes the `constrain()` method.

### Object Construction with Validation

The ObjectFactory pattern allows you to validate inputs and construct objects in a single operation. This is useful for creating validated domain objects from raw inputs.

```kotlin
import org.komapper.extension.validator.Kova
import org.komapper.extension.validator.ObjectFactory
import org.komapper.extension.validator.ObjectSchema

data class Person(val name: String, val age: Int)

object PersonSchema : ObjectSchema<Person>() {
    private val name = Person::name { Kova.string().min(1).max(50) }
    private val age = Person::age { Kova.int().min(0).max(150) }

    // Create a factory method that builds an ObjectFactory
    fun build(name: String, age: Int): ObjectFactory<Person> {
        val arg1 = arg(this.name, name)
        val arg2 = arg(this.age, age)
        return arguments(arg1, arg2).build(::Person)
    }
}

// Use the factory to validate and construct
val factory = PersonSchema.build("Alice", 30)
val result = factory.tryCreate()  // Returns ValidationResult<Person>
// or
val person = factory.create()     // Returns Person or throws ValidationException
```

**Nested Object Validation**:
```kotlin
data class Age(val value: Int)
data class Person(val name: String, val age: Age)

object AgeSchema : ObjectSchema<Age>() {
    private val value = Age::value { Kova.int().min(0).max(120) }

    fun build(age: String): ObjectFactory<Age> {
        val arg1 = arg(Kova.string().toInt().then(this.value), age)
        return arguments(arg1).build(::Age)
    }
}

object PersonSchema : ObjectSchema<Person>() {
    private val name = Person::name { Kova.string().min(1) }
    private val age = Person::age { AgeSchema }

    fun build(name: String, age: String): ObjectFactory<Person> {
        val arg1 = arg(this.name, name)
        val arg2 = arg(this.age, this.age.build(age))  // Nested factory
        return arguments(arg1, arg2).build(::Person)
    }
}

val factory = PersonSchema.build("Bob", "25")
val person = factory.create()  // Validates and constructs nested objects
```

**Key Components**:
- **`ObjectSchema.arg(validator, value)`**: Creates an `Arg` that wraps a validator and input value
- **`ObjectSchema.arg(validator, factory)`**: Creates an `Arg` that wraps a validator and nested ObjectFactory (for nested objects)
- **`ObjectSchema.arguments(...)`**: Creates an `Arguments1` through `Arguments10` object (supports 1-10 arguments)
- **`Arguments.build(constructor)`**: Creates an `ObjectFactory` that validates inputs and constructs objects
- **`ObjectFactory.tryCreate(failFast = false)`**: Validates and constructs, returning `ValidationResult<T>`
- **`ObjectFactory.create(failFast = false)`**: Validates and constructs, returning `T` or throwing `ValidationException`

**Note**: Properties must be defined as object properties because the `invoke` operator for property definitions is only available on the `ObjectSchema` class itself. This also allows properties to be referenced when creating `Arg` instances for ObjectFactory. The `arguments()` method passes the schema itself to the `Arguments` constructor, which is used for validating the constructed object via the `build()` method.

### Nullable Validation

Kova provides first-class support for nullable types:

```kotlin
// Create a nullable validator using asNullable()
// By default, null values are considered valid
val nullableNameValidator = Kova.string().min(1).asNullable()
val result1 = nullableNameValidator.tryValidate(null)        // Success(null)
val result2 = nullableNameValidator.tryValidate("hi")        // Success("hi")
val result3 = nullableNameValidator.tryValidate("")          // Failure (min(1) violated)

// Reject null values explicitly - use Kova.notNull() directly
val notNullValidator = Kova.notNull<String>()
val result = notNullValidator.tryValidate(null)              // Failure

// Accept only null values - use Kova.isNull() directly
val isNullValidator = Kova.isNull<String>()
val result = isNullValidator.tryValidate(null)               // Success(null)

// Accept null OR validate non-null values
val nullOrMinValidator = Kova.nullable<String>().isNullOr(Kova.string().min(5))
val result1 = nullOrMinValidator.tryValidate(null)           // Success(null)
val result2 = nullOrMinValidator.tryValidate("hello")        // Success("hello")
val result3 = nullOrMinValidator.tryValidate("hi")           // Failure (min(5) violated)

// Accept null OR a specific literal value
val nullOrDefaultValidator = Kova.isNullOr("default")
val result1 = nullOrDefaultValidator.tryValidate(null)       // Success(null)
val result2 = nullOrDefaultValidator.tryValidate("default")  // Success("default")
val result3 = nullOrDefaultValidator.tryValidate("other")    // Failure

// Require non-null AND validate the value - use Kova.notNullThen() directly
val notNullAndMinValidator = Kova.notNullThen(Kova.string().min(5)).toNonNullable()
val result1 = notNullAndMinValidator.tryValidate(null)       // Failure (null not allowed)
val result2 = notNullAndMinValidator.tryValidate("hello")    // Success("hello")
val result3 = notNullAndMinValidator.tryValidate("hi")       // Failure (min(5) violated)

// Chain nullable validators with notNullThen
val chainedValidator = Kova.nullable<String>().notNullThen(Kova.string().min(5))

// Set default value for null inputs
val withDefault = Kova.nullable<String>().orDefault("default")
val result = withDefault.tryValidate(null)                   // Success("default")

// Convert any validator to nullable using asNullable()
val validator = Kova.string().min(5).asNullable()
```

**API Summary**:
- `Kova.notNull<T>()` - Convenience method for rejecting null values
- `Kova.isNull<T>()` - Convenience method for accepting only null values
- `Kova.isNullOr(value)` - Accept null or a specific literal value
- `Kova.notNullThen(validator)` - Chain validation after null check
- `.notNullThen(validator)` - Extension on nullable validators
- `.orDefault(value)` - Replace null with default

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
    .min(0)         // Minimum value (>= 0)
    .max(100)       // Maximum value (<= 100)
    .positive()     // Must be positive (> 0)
    .negative()     // Must be negative (< 0)
    .notPositive()  // Must not be positive (<= 0)
    .notNegative()  // Must not be negative (>= 0)
```

### Boolean

```kotlin
Kova.boolean()     // Returns generic validator for boolean values
```

### LocalDate

```kotlin
Kova.localDate()           // Optional clock parameter (defaults to Clock.systemDefaultZone())
    .future()              // Must be in the future
    .futureOrPresent()     // Must be in the future or present
    .past()                // Must be in the past
    .pastOrPresent()       // Must be in the past or present
```

### Collections

```kotlin
Kova.list<String>()
    .min(1)                                  // Minimum size
    .max(10)                                 // Maximum size
    .length(5)                               // Exact size
    .notEmpty()                              // Must not be empty
    .onEach(Kova.string().min(1))           // Validate each element

Kova.set<Int>()
    .min(1)
    .max(10)
    .length(5)
    .notEmpty()
    .onEach(Kova.int().min(0))

Kova.collection<String>()
    .min(1)
    .max(10)
    .length(5)
    .notEmpty()
    .onEach(Kova.string().notBlank())
```

#### Recursive Validation with onEach

The `onEach` method can be used recursively to validate nested collections:

```kotlin
data class Node(
    val children: List<Node> = emptyList(),
)

object NodeSchema : ObjectSchema<Node>() {
    val children = Node::children { Kova.list<Node>().max(2).onEach(this@NodeSchema) }
}

val node = Node(
    listOf(Node(), Node(
        listOf(Node(), Node(), Node()))
    )
)

val result = NodeSchema.tryValidate(node)
```

### Maps

```kotlin
Kova.map<String, Int>()
    .min(1)                                  // Minimum size
    .max(10)                                 // Maximum size
    .length(5)                               // Exact size
    .notEmpty()                              // Must not be empty
    .onEach(Kova.mapEntry<String, Int>())   // Validate each entry
    .onEachKey(Kova.string().min(1))        // Validate each key
    .onEachValue(Kova.int().min(0))         // Validate each value
```

### Literal Values

Validate that a value matches a specific literal value or one of a set of allowed values:

```kotlin
// Single literal value
val activeValidator = Kova.literal("active")
val result1 = activeValidator.tryValidate("active")   // Success("active")
val result2 = activeValidator.tryValidate("inactive") // Failure

// Multiple allowed values (vararg)
val statusValidator = Kova.literal("active", "inactive", "pending")
val result = statusValidator.tryValidate("active")    // Success("active")

// Multiple allowed values (list)
val allowedValues = listOf(1, 2, 3, 5, 8, 13)
val fibValidator = Kova.literal(allowedValues)
val result = fibValidator.tryValidate(5)              // Success(5)

// Works with any type
enum class Status { ACTIVE, INACTIVE, PENDING }
val enumValidator = Kova.literal(Status.ACTIVE, Status.INACTIVE)
val result = enumValidator.tryValidate(Status.ACTIVE) // Success(ACTIVE)
```

### Enums

Enum validation can be done in two ways:

```kotlin
enum class Status { ACTIVE, INACTIVE }

// Option 1: String-based validation via StringValidator
// Validate that a string is a valid enum value
Kova.string().isEnum<Status>()

// Validate and convert to enum
Kova.string().toEnum<Status>()

// Option 2: Direct enum value validation via literal validator
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
    result.messages.forEach { message ->
        println(message.content)
    }
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
    detail.messages.forEach { message ->
        println("Message: ${message.content}")
    }
}
```

## Custom Constraints

You can add custom constraints to any validator using the `constrain` method:

```kotlin
val validator = Kova.string().constrain("custom.email") { ctx ->
    satisfies(
        ctx.input.contains("@") && ctx.input.contains("."),
        Message.Text("Must be a valid email format")
    )
}
```

The first parameter is the constraint ID, and the second is a lambda that receives a `ConstraintContext<T>` and returns a `ConstraintResult`. Use the `satisfies()` helper within the lambda to simplify constraint creation.

### Creating Custom Extension Methods

You can create reusable validation logic by defining extension methods on validators:

```kotlin
// Define a custom extension method for StringValidator
fun StringValidator.isPhoneNumber(
    message: (ConstraintContext<String>) -> Message = Message.resource0()
): StringValidator = constrain("custom.phoneNumber") { ctx ->
        val phonePattern = Regex("""^\+?[1-9]\d{1,14}$""")
        satisfies(
            phonePattern.matches(ctx.input),
            message(ctx)
        )
    }

// Use the custom extension method
val phoneValidator = Kova.string().isPhoneNumber()
val result = phoneValidator.tryValidate("+1234567890")  // Success

// Define extension with custom logic
fun StringValidator.isStrongPassword(
    message: (ConstraintContext<String>) -> Message = { ctx ->
        Message.Text("Password must be at least 8 characters with uppercase, lowercase, and digits")
    }
): StringValidator = constrain("custom.strongPassword") { ctx ->
        val input = ctx.input
        satisfies(
            input.length >= 8 &&
            input.any { it.isUpperCase() } &&
            input.any { it.isLowerCase() } &&
            input.any { it.isDigit() },
            message(ctx)
        )
    }

val passwordValidator = Kova.string().isStrongPassword()

// Extension methods can be chained with built-in validators
val userPasswordValidator = Kova.string()
    .min(8)
    .max(100)
    .isStrongPassword()
```

## Internationalization

Error messages are internationalized using resource bundles. All error messages are represented by `Message` objects with a `content` property containing the actual message string.

The default messages are in `kova.properties`:

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

To access error messages:

```kotlin
val result = validator.tryValidate("ab")
if (result.isFailure()) {
    result.messages.forEach { message ->
        println(message.content)  // Prints the actual error message string
    }
}
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