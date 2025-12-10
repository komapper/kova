package org.komapper.extension.validator.ktor.server

import io.ktor.server.plugins.requestvalidation.*
import org.komapper.extension.validator.Message
import org.komapper.extension.validator.ObjectSchema
import org.komapper.extension.validator.ktor.server.SchemaValidator.Companion.defaultErrorFormatter
import org.komapper.extension.validator.tryValidate
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.hasAnnotation

/**
 * A Ktor [Validator] that validates request bodies using Kova [ObjectSchema].
 *
 * This validator works with classes annotated with [@ValidatedWith][ValidatedWith],
 * which specifies the [ObjectSchema] to use for validation. The validator integrates
 * with Ktor's RequestValidation plugin to automatically validate incoming requests.
 *
 * Example:
 * ```kotlin
 * @ValidatedWith(CustomerSchema::class)
 * @Serializable
 * class Customer(val id: Int, val name: String)
 *
 * object CustomerSchema : ObjectSchema<Customer>() {
 *     val id = Customer::id { it.positive() }
 *     val name = Customer::name { it.min(1).max(100) }
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
 * @see ValidatedWith
 * @see ObjectSchema
 */
class SchemaValidator(
    private val errorFormatter: (List<Message>) -> String = ::defaultErrorFormatter
): Validator {
    override suspend fun validate(value: Any): ValidationResult {
        val schema = getSchemaFromAnnotation(value) ?: return ValidationResult.Valid
        val result = schema.tryValidate(value)
        return mapToKtorValidationResult(result)
    }

    override fun filter(value: Any): Boolean {
        return value::class.hasAnnotation<ValidatedWith>()
    }

    private fun getSchemaFromAnnotation(value: Any): ObjectSchema<Any>? {
        val annotation = value::class.findAnnotations<ValidatedWith>().firstOrNull()
            ?: return null

        val schemaClass = annotation.value
        val objectInstance = schemaClass.objectInstance
            ?: throw IllegalStateException("Schema class must be an object declaration: $schemaClass")

        if (objectInstance !is ObjectSchema<*>) {
            throw IllegalStateException("Schema class must be an ObjectSchema: $schemaClass")
        }

        @Suppress("UNCHECKED_CAST")
        return objectInstance as ObjectSchema<Any>
    }

    private fun mapToKtorValidationResult(
        result: org.komapper.extension.validator.ValidationResult<*>
    ): ValidationResult {
        return when (result) {
            is org.komapper.extension.validator.ValidationResult.Success ->
                ValidationResult.Valid
            is org.komapper.extension.validator.ValidationResult.Failure ->
                ValidationResult.Invalid(formatValidationErrors(result))
        }
    }

    private fun formatValidationErrors(
        failure: org.komapper.extension.validator.ValidationResult.Failure<*>
    ): String {
        return errorFormatter(failure.messages)
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