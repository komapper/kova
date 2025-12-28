# Kova Ktor Integration

Kova integration module for [Ktor](https://ktor.io/) framework, providing automatic request validation using Kova's type-safe validation.

## Features

- **Automatic validation**: Validates incoming request bodies automatically using Ktor's RequestValidation plugin
- **Type-safe validation**: Uses Kova's validation functions within a type-safe context
- **Interface-based**: Simple `Validated` interface to define validation rules
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

### 1. Define your data class with Validated interface

```kotlin
import kotlinx.serialization.Serializable
import org.komapper.extension.validator.*
import org.komapper.extension.validator.ktor.server.Validated

@Serializable
data class Customer(
    val id: Int,
    val firstName: String,
    val lastName: String
) : Validated {
    override fun Validation.validate() = this@Customer.schema {
        ::id { positive(it) }
        ::firstName {
            notBlank(it)
            min(it, 1)
            max(it, 50)
        }
        ::lastName {
            notBlank(it)
            min(it, 1)
            max(it, 50)
        }
    }
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

Implement the `Validated` interface and define validation logic in the `validate()` method:

```kotlin
@Serializable
data class User(
    val name: String,
    val email: String,
    val age: Int
) : Validated {
    override fun Validation.validate() = this@User.schema {
        ::name {
            notBlank(it)
            min(it, 1)
            max(it, 100)
        }
        ::email {
            notBlank(it)
            contains(it, "@")
            min(it, 5)
        }
        ::age {
            min(it, 0)
            max(it, 150)
        }
    }
}
```

### Custom Error Messages

You can provide custom validation error messages:

```kotlin
@Serializable
data class Customer(val id: Int, val name: String) : Validated {
    override fun Validation.validate() = this@Customer.schema {
        ::id {
            positive(
                it,
                message = { text("Customer ID must be greater than 0") }
            )
        }
        ::name {
            min(
                it,
                1,
                message = { text("Name is required") }
            )
        }
    }
}
```

### Nested Object Validation

Validate nested objects by calling validation functions recursively:

```kotlin
@Serializable
data class Address(val street: String, val city: String) : Validated {
    override fun Validation.validate() = this@Address.schema {
        ::street {
            notBlank(it)
            min(it, 1)
        }
        ::city {
            notBlank(it)
            min(it, 1)
        }
    }
}

@Serializable
data class User(val name: String, val address: Address) : Validated {
    override fun Validation.validate() = this@User.schema {
        ::name {
            notBlank(it)
            min(it, 1)
        }
        ::address { addr ->
            // Nested validation is handled automatically
            addr.schema {
                ::street { notBlank(it) }
                ::city { notBlank(it) }
            }
        }
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
            "errors" to cause.reasons
        ))
    }
}
```

## API Reference

### `Validated` Interface

Interface that marks a class as validatable. Classes implementing this interface must provide a `validate()` method that defines validation logic.

```kotlin
interface Validated {
    fun Validation.validate()
}
```

**Example:**
```kotlin
data class Customer(val id: Int) : Validated {
    override fun Validation.validate() = this@Customer.schema {
        ::id { positive(it) }
    }
}
```

### `SchemaValidator`

Ktor validator that validates request bodies using the `Validated` interface.

**Constructor:**
- `errorFormatter: (List<Message>) -> String` - Optional custom error formatter (defaults to joining messages with newlines)

**Methods:**
- `validate(value: Any): ValidationResult` - Validates the given value if it implements `Validated`
- `filter(value: Any): Boolean` - Returns true if the value implements `Validated`

**Example:**
```kotlin
validate(SchemaValidator())

// With custom formatter
validate(SchemaValidator { messages ->
    messages.joinToString("; ") { it.text }
})
```

## Complete Example

Here's a complete working example:

```kotlin
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.komapper.extension.validator.*
import org.komapper.extension.validator.ktor.server.*

@Serializable
data class Customer(val id: Int, val firstName: String, val lastName: String) : Validated {
    override fun Validation.validate() = this@Customer.schema {
        ::id { positive(it) }
        ::firstName {
            notBlank(it)
            min(it, 1)
            max(it, 50)
        }
        ::lastName {
            notBlank(it)
            min(it, 1)
            max(it, 50)
        }
    }
}

fun Application.module() {
    install(RequestValidation) {
        validate(SchemaValidator())
    }
    install(StatusPages) {
        exception<RequestValidationException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, cause.reasons.joinToString("\n"))
        }
    }
    install(ContentNegotiation) {
        json()
    }
    routing {
        post("/customers") {
            val customer = call.receive<Customer>()
            call.respond(HttpStatusCode.Created, customer)
        }
    }
}
```

See the [example-ktor](../example-ktor) module for more complete examples.

## License

Same as Kova Core - see the main project LICENSE file.
