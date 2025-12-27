package org.komapper.extension.validator.ktor.server

import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.requestvalidation.ValidationResult.Invalid
import io.ktor.server.plugins.requestvalidation.ValidationResult.Valid
import org.komapper.extension.validator.Message
import org.komapper.extension.validator.ValidationResult.Failure
import org.komapper.extension.validator.ValidationResult.Success
import org.komapper.extension.validator.ktor.server.SchemaValidator.Companion.defaultErrorFormatter
import org.komapper.extension.validator.tryValidate

/**
 * A Ktor [Validator] that validates request bodies using Kova [Validated].
 *
 * This validator works with classes that implement [Validated]. The validator integrates
 * with Ktor's RequestValidation plugin to automatically validate incoming requests.
 *
 * Example:
 * ```kotlin
 * @Serializable
 * class Customer(val id: Int, val name: String): Validated {
 *    override fun validate() = checking {
 *      ::id { it.positive() }
 *      ::name { it.min(1) and { it.max(100) } }
 *    }
 * }
 *
 * fun Application.module() {
 *     install(RequestValidation) {
 *         validate(SchemaValidator())
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
class SchemaValidator(
    private val errorFormatter: (List<Message>) -> String = ::defaultErrorFormatter
): Validator {
    override suspend fun validate(value: Any) = tryValidate {
        if (value is Validated) with(value) { validate() }
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
        fun defaultErrorFormatter(messages: List<Message>): String {
            return messages.joinToString("\n") { it.text }
        }
    }
}