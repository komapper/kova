package org.komapper.extension.validator.ktor.server

import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.requestvalidation.ValidationResult.*
import org.komapper.extension.validator.Message
import org.komapper.extension.validator.Validation
import org.komapper.extension.validator.ValidationResult.Failure
import org.komapper.extension.validator.ValidationResult.Success
import org.komapper.extension.validator.ktor.server.SchemaValidator.Companion.defaultErrorFormatter
import org.komapper.extension.validator.tryValidate

/**
 * A Ktor [Validator] that validates request bodies using Kova [Validated] interface.
 *
 * This validator integrates with Ktor's RequestValidation plugin to automatically
 * validate incoming request bodies. It works with any class that implements [Validated].
 *
 * Example:
 * ```kotlin
 * @Serializable
 * data class Customer(val id: Int, val name: String) : Validated {
 *     override fun Validation.validate() = this@Customer.schema {
 *         ::id { ensurePositive(it) }
 *         ::name {
 *             ensureNotBlank(it)
 *             ensureLengthInRange(it, 1..100)
 *         }
 *     }
 * }
 *
 * fun Application.module() {
 *     install(RequestValidation) {
 *         validate(SchemaValidator())
 *     }
 *     install(StatusPages) {
 *         exception<RequestValidationException> { call, cause ->
 *             call.respond(HttpStatusCode.BadRequest, cause.reasons.joinToString("\n"))
 *         }
 *     }
 *     routing {
 *         post("/customers") {
 *             val customer = call.receive<Customer>()
 *             // customer is validated automatically
 *         }
 *     }
 * }
 * ```
 *
 * @param errorFormatter Optional function to customize error message formatting.
 *   Receives a list of [Message] objects and returns a formatted error string.
 *   Defaults to [defaultErrorFormatter] which joins message texts with newlines.
 *
 * @see Validated
 */
public class SchemaValidator(
    private val errorFormatter: (List<Message>) -> String = ::defaultErrorFormatter
): Validator {
    override suspend fun validate(value: Any) = tryValidate {
        if (value is Validated) value.validate()
    }.toKtor()

    override fun filter(value: Any): Boolean = value is Validated

    private fun org.komapper.extension.validator.ValidationResult<*>.toKtor() = when (this) {
        is Success -> Valid
        is Failure -> Invalid(errorFormatter(messages))
    }

    companion object {
        /**
         * Default error formatter that formats error messages as plain text.
         *
         * Multiple errors are joined with newlines.
         */
        public fun defaultErrorFormatter(messages: List<Message>): String {
            return messages.joinToString("\n") { it.text }
        }
    }
}