package org.komapper.extension.validator.ktor.server

import org.komapper.extension.validator.Validation

/**
 * Marker interface for classes that can be validated using Kova validation.
 *
 * Classes implementing this interface must provide a [validate] method that defines
 * validation logic within a [Validation] context. When used with [SchemaValidator]
 * in Ktor's RequestValidation plugin, incoming request bodies are automatically validated.
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
 * ```
 *
 * @see SchemaValidator
 */
interface Validated {
    /**
     * Defines validation logic for this object within a [Validation] context.
     *
     * Typically implemented using the [schema][Validation.schema] function with property
     * references. Property names are automatically used as validation paths in error messages.
     */
    context(_: Validation)
    fun validate()
}
