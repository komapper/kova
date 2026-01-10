package example.ktor

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.komapper.extension.validator.Validation
import org.komapper.extension.validator.ktor.server.SchemaValidator
import org.komapper.extension.validator.ktor.server.Validated
import org.komapper.extension.validator.ensurePositive
import org.komapper.extension.validator.schema
import org.komapper.extension.validator.*

/**
 * Customer data class demonstrating Kova integration with Ktor.
 *
 * Key features:
 * - Implements the Validated interface from kova-ktor module
 * - @Serializable for JSON serialization/deserialization
 * - The validate() method defines validation rules that will be automatically
 *   triggered by Ktor's RequestValidation plugin when this object is received
 *
 * When a POST request is received with Customer data, Ktor will:
 * 1. Deserialize the JSON to a Customer object
 * 2. Automatically call validate() before passing it to the route handler
 * 3. Throw RequestValidationException if validation fails
 */
@Serializable
data class Customer(val id: Int, val firstName: String, val lastName: String) : Validated {
    context(_: Validation)
    override fun validate() = validate(this@Customer)
}

/**
 * Validation schema for Customer.
 *
 * Validates that:
 * - id is ensurePositive (greater than 0) with a custom error message
 *
 * Note: Additional validations could be added for firstName and lastName,
 * but this example focuses on demonstrating custom error messages with the
 * ensurePositive() constraint.
 */
context(_: Validation)
fun validate(customer: Customer) = customer.schema {
    customer::id { ensurePositive(it) { text("A customer ID should be greater than 0") } }
}

/**
 * Application entry point.
 * Uses Ktor's EngineMain to start the server with configuration from application.conf.
 */
fun main(args: Array<String>): Unit = EngineMain.main(args)

/**
 * Main application module that configures Ktor plugins and routing.
 *
 * This demonstrates how to integrate Kova validation into a Ktor application:
 *
 * 1. RequestValidation plugin - Automatically validates incoming requests
 *    - SchemaValidator() connects Kova's validation to Ktor's validation system
 *    - Any class implementing Validated will be validated automatically
 *
 * 2. StatusPages plugin - Handles validation errors gracefully
 *    - Catches RequestValidationException thrown when validation fails
 *    - Returns HTTP 400 (Bad Request) with validation error messages
 *
 * 3. ContentNegotiation plugin - Enables JSON serialization/deserialization
 *    - Allows automatic conversion between JSON and Kotlin objects
 *
 * 4. Routing - Defines API endpoints
 *    - POST /json - Receives Customer JSON, validates it, and echoes it back
 */
fun Application.module() {
    // Install RequestValidation plugin with Kova's SchemaValidator
    // This enables automatic validation of objects implementing Validated interface
    install(RequestValidation) {
        validate(SchemaValidator())
    }

    // Install StatusPages plugin to handle validation exceptions
    // When validation fails, returns 400 Bad Request with error details
    install(StatusPages) {
        exception<RequestValidationException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, cause.reasons.joinToString())
        }
    }

    // Install ContentNegotiation for JSON serialization
    install(ContentNegotiation) {
        json()
    }

    // Define routes
    routing {
        // POST endpoint that receives and validates Customer objects
        // The validation happens automatically before the handler is called
        // If validation fails, StatusPages catches the exception and returns 400
        post("/json") {
            val customer = call.receive<Customer>()
            call.respond(customer)
        }
    }
}