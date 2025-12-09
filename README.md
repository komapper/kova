# Kova

A type-safe Kotlin validation library that provides composable validators through a fluent API.

> **⚠️ Note**: This project is currently under active development. The API may change until a stable 1.0.0 release.

## Setup

Add Kova to your Gradle project:

### Gradle Kotlin DSL (build.gradle.kts)

```kotlin
dependencies {
    implementation("org.komapper:kova-core:0.0.2")
}
```

## Features

- **Type-Safe**: Leverages Kotlin's type system for compile-time safety
- **Composable**: Combine validators using intuitive operators (`+`, `and`, `or`)
- **Immutable**: All validators are immutable and thread-safe
- **Detailed Error Reporting**: Get precise error messages with path tracking for nested validations
- **Circular Reference Detection**: Automatically detects and handles circular references in nested object validation
- **Internationalization**: Built-in support for localized error messages
- **Fail-Fast Support**: Option to stop validation at the first error or collect all errors
- **Nullable Support**: First-class support for nullable types
- **Object Construction**: Validate inputs and construct objects in a single step
- **Zero Dependencies**: No external runtime dependencies, only requires Kotlin standard library

## Quick Start

### Basic Validation

```kotlin
import org.komapper.extension.validator.Kova

// Create a validator for product name
val productNameValidator = Kova.string().min(1).max(100).notBlank()

// Validate a value
val result = productNameValidator.tryValidate("Wireless Mouse")

// Check the result
if (result.isSuccess()) {
    println("Valid: ${result.value}")
} else {
    result.messages.forEach { message ->
        println("Error: ${message.text}")
    }
}

// Or use validate() which throws ValidationException on failure
val productName = productNameValidator.validate("Wireless Mouse")
```

### Validator Composition

```kotlin
// Combine validators with + operator (or 'and')
val emailValidator = Kova.string().notBlank() + Kova.string().email()

// Use 'or' for alternative validations (succeeds if either passes)
// Accept either an email or a phone number format
val contactValidator = Kova.string().email() or Kova.string().matches(Regex("""^\+?[1-9]\d{1,14}$"""))

// Transform output with map
val priceValidator = Kova.string().toDouble()  // Validates and converts to Double

// Type-specific validators return the same type when composed
val usernameValidator: StringValidator = Kova.string().min(3).max(20).matches(Regex("^[a-zA-Z0-9_]+$"))
val stockValidator: NumberValidator<Int> = Kova.int().min(0).max(10000)
```

### Object Validation

```kotlin
data class Product(val id: Int, val name: String, val price: Double, val stock: Int)

// Define a schema for the Product class
object ProductSchema : ObjectSchema<Product>() {
    val id = Product::id { Kova.int().min(1) }
    val name = Product::name { Kova.string().min(1).max(100).notBlank() }
    val price = Product::price { Kova.double().min(0.0) }
    val stock = Product::stock { Kova.int().min(0) }
}

// Validate a product instance
val product = Product(1, "Wireless Mouse", 29.99, 150)
val result = ProductSchema.tryValidate(product)
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
import org.komapper.extension.validator.ObjectSchema

data class UserRegistration(val username: String, val email: String, val password: String)

object UserRegistrationSchema : ObjectSchema<UserRegistration>() {
    private val usernameV = UserRegistration::username {
        Kova.string().min(3).max(20).matches(Regex("^[a-zA-Z0-9_]+$"))
    }
    private val emailV = UserRegistration::email { Kova.string().email() }
    private val passwordV = UserRegistration::password { Kova.string().min(8).max(100) }

    // Create a factory method that builds an ObjectFactory
    fun bind(username: String, email: String, password: String) = factory {
        create(::UserRegistration, usernameV.bind(username), emailV.bind(email), passwordV.bind(password))
    }
}

// Use the factory to validate and construct
val factory = UserRegistrationSchema.bind("john_doe", "john@example.com", "SecurePass123")
val result = factory.tryCreate()  // Returns ValidationResult<UserRegistration>
// or
val registration = factory.create()     // Returns UserRegistration or throws ValidationException
```

**Nested Object Validation**:
```kotlin
data class Address(val street: String, val city: String, val zipCode: String)
data class Customer(val name: String, val email: String, val address: Address)

object AddressSchema : ObjectSchema<Address>() {
    private val streetV = Address::street { Kova.string().min(1).max(100) }
    private val cityV = Address::city { Kova.string().min(1).max(50) }
    private val zipCodeV = Address::zipCode { Kova.string().matches(Regex("^\\d{5}(-\\d{4})?$")) }

    fun bind(street: String, city: String, zipCode: String) = factory {
        create(::Address, streetV.bind(street), cityV.bind(city), zipCodeV.bind(zipCode))
    }
}

object CustomerSchema : ObjectSchema<Customer>() {
    private val nameV = Customer::name { Kova.string().min(1).max(100) }
    private val emailV = Customer::email { Kova.string().email() }
    private val addressV = Customer::address { AddressSchema }

    fun bind(name: String, email: String, street: String, city: String, zipCode: String) = factory {
        create(::Customer, nameV.bind(name), emailV.bind(email), addressV.bind(street, city, zipCode))
    }
}

val factory = CustomerSchema.bind("Bob Smith", "bob@example.com", "123 Main St", "Springfield", "12345")
val customer = factory.create()  // Validates and constructs nested objects
```

**Key Components**:
- **`ObjectSchema.factory(block)`**: Creates an object factory scope that provides access to `bind` and `create` methods for composing validators with object construction
- **`Validator.bind(value)`**: Extension method (available in factory scope) that creates an `ObjectFactory` from a validator and input value
- **`create(constructor, ...factories)`**: Method (available in factory scope) that creates an `ObjectFactory` which validates ObjectFactories and constructs objects (supports 1-10 arguments)
- **`ObjectFactory.tryCreate(config = ValidationConfig())`**: Validates and constructs, returning `ValidationResult<T>`
- **`ObjectFactory.create(config = ValidationConfig())`**: Validates and constructs, returning `T` or throwing `ValidationException`

**Note**: Properties must be defined as object properties because the `invoke` operator for property definitions is only available on the `ObjectSchema` class itself. This also allows properties to be referenced when binding values. The `bind` and `create` methods are only available within the `factory { }` scope, which provides access to `ObjectSchemaFactoryScope`.

### Nullable Validation

Kova provides first-class support for nullable types:

```kotlin
// Create a nullable validator using asNullable()
// By default, null values are considered valid
val nullableNameValidator = Kova.string().min(1).asNullable()
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
val nullOrMinValidator = Kova.nullable<Int>().isNull().or(Kova.int().min(5))
val result1 = nullOrMinValidator.tryValidate(null)           // Success(null)
val result2 = nullOrMinValidator.tryValidate(10)             // Success(10)
val result3 = nullOrMinValidator.tryValidate(3)              // Failure (min(5) violated)

// Accept null OR validate with a lambda-based validator (convenience method)
val isNullOrMinValidator = Kova.nullable<Int>().isNullOr { it.min(5) }
val result1 = isNullOrMinValidator.tryValidate(null)         // Success(null)
val result2 = isNullOrMinValidator.tryValidate(10)           // Success(10)
val result3 = isNullOrMinValidator.tryValidate(3)            // Failure (min(5) violated)

// Accept null OR a non-blank string
val nullOrNonBlankValidator = Kova.nullable<String>().isNull().or(Kova.string().notBlank())
val result1 = nullOrNonBlankValidator.tryValidate(null)      // Success(null)
val result2 = nullOrNonBlankValidator.tryValidate("value")   // Success("value")
val result3 = nullOrNonBlankValidator.tryValidate("")        // Failure

// Require non-null AND validate the value
val notNullAndMinValidator = Kova.nullable<String>().notNull().and(Kova.string().min(5))
val result1 = notNullAndMinValidator.tryValidate(null)       // Failure (null not allowed)
val result2 = notNullAndMinValidator.tryValidate("hello")    // Success("hello")
val result3 = notNullAndMinValidator.tryValidate("hi")       // Failure (min(5) violated)

// Require non-null AND validate with a lambda-based validator (convenience method)
val notNullAndMin = Kova.nullable<String>().notNullAnd { it.min(5) }
val result1 = notNullAndMin.tryValidate(null)                // Failure (null not allowed)
val result2 = notNullAndMin.tryValidate("hello")             // Success("hello")
val result3 = notNullAndMin.tryValidate("hi")                // Failure (min(5) violated)

// Convert with default value for null inputs using asNullable(defaultValue)
val withDefault = Kova.int().min(0).asNullable(10)
val result1 = withDefault.tryValidate(null)                  // Success(10)
val result2 = withDefault.tryValidate(5)                     // Success(5)
val result3 = withDefault.tryValidate(-1)                    // Failure (min(0) violated)

// Lazy-evaluated default value using lambda
val withLazyDefault = Kova.int().min(0).asNullable { computeDefault() }
val result = withLazyDefault.tryValidate(null)               // Success(computeDefault())

// Set default value for null inputs using nullable validator
val withDefaultAlt = Kova.nullable<String>().withDefault("default")
val result = withDefaultAlt.tryValidate(null)                // Success("default")

// Lazy-evaluated default with nullable validator
val withLazyDefaultAlt = Kova.nullable<String>().withDefault { "computed-default" }
val result2 = withLazyDefaultAlt.tryValidate(null)           // Success("computed-default")

// Create nullable validator with default value directly
val nullableWithDefault = Kova.nullable(10)
val nullableWithLazyDefault = Kova.nullable { 10 }
```

**Creating Nullable Validators**:
- `Kova.nullable<T>()` - Create an empty nullable validator (accepts null by default)
- `Kova.nullable(defaultValue)` - Create nullable validator with default value for null inputs
- `Kova.nullable { defaultValue }` - Create nullable validator with lazy-evaluated default for null inputs
- `Validator<T, S>.asNullable()` - Convert any validator to nullable (accepts null as-is)
- `Validator<T, S>.asNullable(defaultValue)` - Convert to nullable with default value for null inputs
- `Validator<T, S>.asNullable { defaultValue }` - Convert to nullable with lazy-evaluated default for null inputs

**Methods on NullableValidator**:
- `.isNull()` - Accept only null values
- `.notNull()` - Reject null values
- `.isNullOr { validator }` - Accept null OR validate non-null values using a lambda-based validator
- `.notNullAnd { validator }` - Require non-null AND validate using a lambda-based validator
- `.withDefault(value)` - Replace null values with a default
- `.withDefault { value }` - Replace null values with a lazy-evaluated default
- `.toNonNullable()` - Convert to non-nullable validator

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
    .gt(0)          // Greater than (> 0)
    .gte(0)         // Greater than or equal (>= 0)
    .lt(100)        // Less than (< 100)
    .lte(100)       // Less than or equal (<= 100)
    .positive()     // Must be positive (> 0)
    .negative()     // Must be negative (< 0)
    .notPositive()  // Must not be positive (<= 0)
    .notNegative()  // Must not be negative (>= 0)
```

### Boolean

```kotlin
Kova.boolean()     // Returns generic validator for boolean values
```

### Temporal Types

```kotlin
// LocalDate validation
Kova.localDate()           // Optional clock parameter (defaults to Clock.systemDefaultZone())
    .min(LocalDate.of(2024, 1, 1))     // Minimum date (>=)
    .max(LocalDate.of(2024, 12, 31))   // Maximum date (<=)
    .gt(LocalDate.of(2024, 6, 1))      // Greater than (>)
    .gte(LocalDate.of(2024, 1, 1))     // Greater than or equal (>=)
    .lt(LocalDate.of(2025, 1, 1))      // Less than (<)
    .lte(LocalDate.of(2024, 12, 31))   // Less than or equal (<=)
    .future()                           // Must be in the future
    .futureOrPresent()                  // Must be in the future or present
    .past()                             // Must be in the past
    .pastOrPresent()                    // Must be in the past or present

// LocalTime validation
Kova.localTime()           // Optional clock parameter (defaults to Clock.systemDefaultZone())
    .min(LocalTime.of(9, 0))           // Minimum time (>=)
    .max(LocalTime.of(17, 0))          // Maximum time (<=)
    .gt(LocalTime.of(8, 30))           // Greater than (>)
    .gte(LocalTime.of(9, 0))           // Greater than or equal (>=)
    .lt(LocalTime.of(18, 0))           // Less than (<)
    .lte(LocalTime.of(17, 30))         // Less than or equal (<=)
    .future()                           // Must be in the future
    .futureOrPresent()                  // Must be in the future or present
    .past()                             // Must be in the past
    .pastOrPresent()                    // Must be in the past or present

// LocalDateTime validation
Kova.localDateTime()       // Optional clock parameter (defaults to Clock.systemDefaultZone())
    .min(LocalDateTime.of(2024, 1, 1, 0, 0))     // Minimum datetime (>=)
    .max(LocalDateTime.of(2024, 12, 31, 23, 59)) // Maximum datetime (<=)
    .gt(startDateTime)                            // Greater than (>)
    .gte(startDateTime)                           // Greater than or equal (>=)
    .lt(endDateTime)                              // Less than (<)
    .lte(endDateTime)                             // Less than or equal (<=)
    .future()                                     // Must be in the future
    .futureOrPresent()                            // Must be in the future or present
    .past()                                       // Must be in the past
    .pastOrPresent()                              // Must be in the past or present

// All temporal validators support composition operators
val dateValidator: LocalDateValidator = Kova.localDate().past() + Kova.localDate().min(LocalDate.of(2020, 1, 1))
val timeValidator: LocalTimeValidator = Kova.localTime().gte(LocalTime.of(9, 0)) or Kova.localTime().lte(LocalTime.of(17, 0))
val dateTimeValidator: LocalDateTimeValidator = Kova.localDateTime().min(start).max(end)
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

#### Circular Reference Detection

Kova automatically detects circular references in nested object validation to prevent infinite loops:

```kotlin
data class Node(
    val value: Int,
    var next: Node?,
)

object NodeSchema : ObjectSchema<Node>() {
    val value = Node::value { Kova.int().min(0).max(100) }
    val next = Node::next { Kova.nullable<Node>().then(this@NodeSchema) }
}

// Create a circular reference: node1 -> node2 -> node1
val node1 = Node(10, null)
val node2 = Node(20, node1)
node1.next = node2

// Validation succeeds without infinite loop
val result = NodeSchema.tryValidate(node1)  // Success
```

The circular reference detection uses object identity (`===`) to track objects in the validation path. When a circular reference is detected, validation terminates gracefully for that branch while still validating all accessible paths and constraints.

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
// Single literal value - validate order status
val completedValidator = Kova.literal("completed")
val result1 = completedValidator.tryValidate("completed")   // Success("completed")
val result2 = completedValidator.tryValidate("pending")     // Failure

// Multiple allowed values (vararg) - validate order status
val orderStatusValidator = Kova.literal("pending", "processing", "shipped", "delivered")
val result = orderStatusValidator.tryValidate("shipped")    // Success("shipped")

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

// All unsigned integer validators support comparison methods:
    .min(0u)        // Minimum value (>= 0u)
    .max(100u)      // Maximum value (<= 100u)
    .gt(0u)         // Greater than (> 0u)
    .gte(0u)        // Greater than or equal (>= 0u)
    .lt(100u)       // Less than (< 100u)
    .lte(100u)      // Less than or equal (<= 100u)
```

### Generic Validator

```kotlin
Kova.generic<T>()  // No-op validator that always succeeds
                   // Useful as a base for custom validators or with nullable()
```

## Error Handling

### Collecting All Errors

```kotlin
val passwordValidator = Kova.string().min(8).max(20)
val result = passwordValidator.tryValidate("pass")

if (result.isFailure()) {
    // Get all error messages
    result.messages.forEach { message ->
        println(message.text)
    }
    // Output:
    // must be at least 8 characters
}
```

### Fail-Fast Mode

```kotlin
val passwordValidator = Kova.string().min(8).max(20)
val result = passwordValidator.tryValidate("pass", failFast = true)

if (result.isFailure()) {
    // Only the first error is reported
    result.messages.size shouldBe 1
}
```

### OR Validator Error Messages

When using the `or` operator, if both validators fail, the error message provides composite feedback showing both validation branches:

```kotlin
val validator = Kova.nullable<Int>().isNull().or(Kova.int().min(5).max(10))
val result = validator.tryValidate(3)

if (result.isFailure()) {
    // Composite OR error message showing both branches
    // "at least one constraint must be satisfied: [[must be null], [must be greater than or equal to 5]]"
    println(result.messages[0].text)
}
```

### Path Tracking for Nested Objects

```kotlin
val result = ProductSchema.tryValidate(Product(-1, "", -10.0, -5))

if (result.isFailure()) {
    result.messages.forEach { message ->
        println("Root: ${message.root}")      // e.g., "Product"
        println("Path: ${message.path}")      // e.g., "price"
        println("Message: ${message.text}")   // e.g., "must be greater than or equal to 0.0"
    }
}
```

### Error Message Structure

Validation failures provide detailed information through the `Message` interface:

```kotlin
val result = validator.tryValidate(input)
if (result.isFailure()) {
    result.messages.forEach { message ->
        println("Constraint ID: ${message.constraintId}")
        println("Path: ${message.path}")
        println("Root: ${message.root}")
        println("Message: ${message.text}")
        println("Context: ${message.context}")
    }
}
```

## Custom Constraints

You can add custom constraints to any validator using the `constrain` method:

```kotlin
val validator = Kova.string().constrain("custom.urlPath") { ctx ->
    satisfies(
        ctx.input.startsWith("/") && !ctx.input.contains(".."),
        "Must be a valid URL path starting with / and not contain .."
    )
}
```

The first parameter is the constraint ID, and the second is a lambda with `ConstraintScope` receiver that receives a `ConstraintContext<T>` and returns a `ConstraintResult`. Use the `satisfies()` helper within the lambda to simplify constraint creation. The `satisfies()` helper accepts either:
- A message factory function `(ConstraintContext<*>) -> Message` (returned by `MessageProvider.invoke()`)
- A plain string for simple error messages

### Creating Custom Extension Methods

You can create reusable validation logic by defining extension methods on validators:

```kotlin
// Define a custom extension method for StringValidator
fun StringValidator.isPhoneNumber(
    message: MessageProvider = Message.resource()
): StringValidator = constrain("custom.phoneNumber") { ctx ->
        val phonePattern = Regex("""^\+?[1-9]\d{1,14}$""")
        satisfies(
            phonePattern.matches(ctx.input),
            message()
        )
    }

// Use the custom extension method
val phoneValidator = Kova.string().isPhoneNumber()
val result = phoneValidator.tryValidate("+1234567890")  // Success

// Define extension with custom message provider
fun StringValidator.isStrongPassword(
    message: MessageProvider = Message.text { ctx ->
        "Password must be at least ${ctx[0]} characters with uppercase, lowercase, and digits"
    }
): StringValidator = constrain("custom.strongPassword") { ctx ->
        val input = ctx.input
        val minLength = 8
        satisfies(
            input.length >= minLength &&
            input.any { it.isUpperCase() } &&
            input.any { it.isLowerCase() } &&
            input.any { it.isDigit() },
            message(minLength)
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

Error messages are internationalized using resource bundles. All error messages are represented by `Message` objects with a `text` property containing the actual message string.

The default messages are in `kova.properties`:

```properties
kova.string.min=must be at least {0} characters
kova.string.max=must be at most {0} characters
kova.comparable.min=must be greater than or equal to {0}
kova.comparable.max=must be less than or equal to {0}
# ... more messages
```

You can customize messages per validation:

```kotlin
val validator = Kova.string().min(
    length = 5,
    message = Message.text { ctx -> "String '${ctx.input}' is too short (min: ${ctx[0]})" }
)
```

**Note**: When using `MessageProvider`, you only pass the constraint parameters (not the input value) to the `message()` function. The input value can be accessed via `ctx.input` in the message lambda if needed.

To access error messages:

```kotlin
val result = validator.tryValidate("ab")
if (result.isFailure()) {
    result.messages.forEach { message ->
        println(message.text)  // Prints: "String 'ab' is too short (min: 5)"
        println(message)        // Detailed output with all information
        // Prints: "Message(constraintId=kova.string.min, text=String 'ab' is too short (min: 5), root=, path=, input=ab)"
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
