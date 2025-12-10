# Kova Ktor Integration

Kova integration module for [Ktor](https://ktor.io/) framework, providing automatic request validation using Kova's type-safe validation schemas.

## Features

- **Automatic validation**: Validates incoming request bodies automatically using Ktor's RequestValidation plugin
- **Type-safe schemas**: Uses Kova's `ObjectSchema` for compile-time safe validation rules
- **Annotation-based**: Simple `@ValidatedWith` annotation to specify validation schemas
- **Customizable error formatting**: Flexible error message formatting for validation failures
- **Seamless integration**: Works naturally with Ktor's existing plugins and routing

## Installation

Add the dependency to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("org.komapper.extension:kova-ktor:$kovaVersion")
}
```

## Quick Start

### 1. Define your data class and validation schema

```kotlin
import kotlinx.serialization.Serializable
import org.komapper.extension.validator.*
import org.komapper.extension.validator.ktor.server.ValidatedWith

@ValidatedWith(CustomerSchema::class)
@Serializable
data class Customer(
    val id: Int,
    val firstName: String,
    val lastName: String
)

object CustomerSchema : ObjectSchema<Customer>() {
    val id = Customer::id { it.positive() }
    val firstName = Customer::firstName { it.min(1).max(50) }
    val lastName = Customer::lastName { it.min(1).max(50) }
}
```

### 2. Install the validation plugin

```kotlin
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import org.komapper.extension.validator.ktor.server.SchemaValidator

fun Application.module() {
    install(RequestValidation) {
        validate(SchemaValidator())
    }

    install(StatusPages) {
        exception<RequestValidationException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, cause.reasons.joinToString("\n"))
        }
    }

    // ... other configurations
}
```

### 3. Use validated data in routes

```kotlin
routing {
    post("/customers") {
        val customer = call.receive<Customer>()
        // customer is automatically validated
        call.respond(HttpStatusCode.Created, customer)
    }
}
```

## Usage

### Basic Validation

The `@ValidatedWith` annotation links your data class to a Kova `ObjectSchema`:

```kotlin
@ValidatedWith(UserSchema::class)
@Serializable
data class User(
    val name: String,
    val email: String,
    val age: Int
)

object UserSchema : ObjectSchema<User>() {
    val name = User::name { it.min(1).max(100).notBlank() }
    val email = User::email { it.email() }
    val age = User::age { it.min(0).max(150) }
}
```

### Custom Error Messages

You can customize validation error messages in your schema:

```kotlin
object CustomerSchema : ObjectSchema<Customer>() {
    val id = Customer::id {
        it.positive(Message.text { "Customer ID must be greater than 0" })
    }
    val name = Customer::name {
        it.min(1, Message.text { "Name is required" })
    }
}
```

### Custom Error Formatting

The `SchemaValidator` accepts an optional error formatter:

```kotlin
install(RequestValidation) {
    validate(SchemaValidator { messages ->
        // Custom JSON formatting
        """{"errors": [${messages.joinToString(",") { "\"${it.text}\"" }}]}"""
    })
}
```

### Error Handling

Use Ktor's StatusPages plugin to handle validation errors:

```kotlin
install(StatusPages) {
    exception<RequestValidationException> { call, cause ->
        // Default: plain text errors
        call.respond(HttpStatusCode.BadRequest, cause.reasons.joinToString("\n"))

        // Or custom JSON response:
        call.respond(HttpStatusCode.BadRequest, mapOf(
            "errors" = cause.reasons
        ))
    }
}
```

## API Reference

### `@ValidatedWith`

Annotation that specifies the `ObjectSchema` to use for validation.

**Parameters:**
- `value: KClass<*>` - The ObjectSchema class (must be an object declaration)

**Example:**
```kotlin
@ValidatedWith(CustomerSchema::class)
data class Customer(...)
```

### `SchemaValidator`

Ktor validator that validates request bodies using Kova ObjectSchemas.

**Constructor:**
- `errorFormatter: (List<Message>) -> String` - Optional custom error formatter (defaults to joining messages with newlines)

**Methods:**
- `validate(value: Any): ValidationResult` - Validates the given value
- `filter(value: Any): Boolean` - Filters values to validate (checks for @ValidatedWith annotation)

**Example:**
```kotlin
validate(SchemaValidator())

// With custom formatter
validate(SchemaValidator { messages ->
    messages.joinToString("; ") { it.text }
})
```

## Complete Example

See the [example-ktor](../example-ktor) module for a complete working example.

## License

Same as Kova Core - see the main project LICENSE file.
