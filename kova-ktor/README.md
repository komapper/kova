# Kova Ktor Integration

Automatic request validation for [Ktor](https://ktor.io/) using Kova's type-safe validators.

## Overview

`kova-ktor` integrates with Ktor's RequestValidation plugin to automatically validate incoming request bodies. Simply implement the `Validated` interface on your data classes to enable validation.

See the [main README](../README.md) for core validation concepts and available validators.

## Features

- **Automatic validation** - Validates request bodies using Ktor's RequestValidation plugin
- **Interface-based** - Simple `Validated` interface integration
- **Customizable error formatting** - Flexible error message formatting

## Quick Start

### 1. Define data class with Validated interface

```kotlin
import kotlinx.serialization.Serializable
import org.komapper.extension.validator.*
import org.komapper.extension.validator.ktor.server.Validated

@Serializable
data class Customer(val id: Int, val name: String) : Validated {
    context(_: Validation)
    override fun validate() = schema {
        ::id { it.ensurePositive() }
        ::name { it.ensureNotBlank().ensureLengthInRange(1..50) }
    }
}
```

### 2. Install RequestValidation plugin

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
}
```

### 3. Use in routes

```kotlin
routing {
    post("/customers") {
        val customer = call.receive<Customer>()  // Validated automatically
        call.respond(HttpStatusCode.Created, customer)
    }
}
```

## API

### `Validated` Interface

Marker interface for classes that can be validated:

```kotlin
interface Validated {
    fun Validation.validate()
}
```

The `validate()` method is defined with a context receiver:

```kotlin
data class Customer(val id: Int) : Validated {
    context(_: Validation)
    override fun validate() = schema {
        ::id { it.ensurePositive() }
    }
}
```

### `SchemaValidator`

Integrates Kova validation with Ktor's RequestValidation plugin:

```kotlin
validate(SchemaValidator())
```

Accepts an optional error formatter (see Advanced Usage for examples).

## Advanced Usage

### Custom Error Formatting

```kotlin
install(RequestValidation) {
    validate(SchemaValidator { messages ->
        """{"errors": [${messages.joinToString(",") { "\"${it.text}\"" }}]}"""
    })
}
```

### Error Handling with StatusPages

```kotlin
install(StatusPages) {
    exception<RequestValidationException> { call, cause ->
        call.respond(HttpStatusCode.BadRequest, mapOf(
            "errors" to cause.reasons
        ))
    }
}
```

### Nested Object Validation

```kotlin
@Serializable
data class Address(val street: String, val city: String) : Validated {
    context(_: Validation)
    override fun validate() = schema {
        ::street { it.ensureNotBlank().ensureLengthAtLeast(1) }
        ::city { it.ensureNotBlank().ensureLengthAtLeast(1) }
    }
}

@Serializable
data class User(val name: String, val address: Address) : Validated {
    context(_: Validation)
    override fun validate() = schema {
        ::name { it.ensureNotBlank().ensureLengthAtLeast(1) }
        ::address { it.validate() }  // Reuse validation
    }
}
```

## Complete Example

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
data class Customer(val id: Int, val name: String) : Validated {
    context(_: Validation)
    override fun validate() = schema {
        ::id { it.ensurePositive() }
        ::name { it.ensureNotBlank().ensureLengthInRange(1..50) }
    }
}

fun Application.module() {
    install(ContentNegotiation) { json() }
    install(RequestValidation) { validate(SchemaValidator()) }
    install(StatusPages) {
        exception<RequestValidationException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, cause.reasons.joinToString("\n"))
        }
    }
    routing {
        post("/customers") {
            val customer = call.receive<Customer>()
            call.respond(HttpStatusCode.Created, customer)
        }
    }
}
```

## See Also

- [Main README](../README.md) - Core validation concepts and available validators
- [example-ktor](../example-ktor) - Complete usage examples
