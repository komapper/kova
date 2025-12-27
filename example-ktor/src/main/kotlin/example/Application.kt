package example

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
import org.komapper.extension.validator.Validation
import org.komapper.extension.validator.ktor.server.SchemaValidator
import org.komapper.extension.validator.ktor.server.Validated
import org.komapper.extension.validator.positive
import org.komapper.extension.validator.text

@Serializable
data class Customer(val id: Int, val firstName: String, val lastName: String): Validated {
    override fun Validation.validate() = this@Customer.schema {
        ::id { positive(it) { text("A customer ID should be greater than 0") } }
    }
}

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    install(RequestValidation) {
        validate(SchemaValidator())
    }
    install(StatusPages) {
        exception<RequestValidationException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, cause.reasons.joinToString())
        }
    }
    install(ContentNegotiation) {
        json()
    }
    routing {
        post("/json") {
            val customer = call.receive<Customer>()
            call.respond(customer)
        }
    }
}