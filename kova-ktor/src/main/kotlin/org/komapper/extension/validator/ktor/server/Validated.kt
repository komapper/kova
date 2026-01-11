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
 *     context(_: Validation)
 *     override fun validate() = schema {
 *         ::id { it.ensurePositive() }
 *         ::name { it.ensureNotBlank().ensureLengthInRange(1..100) }
 *     }
 * }
 * ```
 *
 * @see SchemaValidator
 */
public interface Validated {
    /**
     * Defines validation logic for this object within a [Validation] context.
     *
     * Typically implemented using the [schema][Validation.schema] function with property
     * references. Property names are automatically used as validation paths in error messages.
     *
     * @param Validation (context parameter) The validation context for constraint checking and error accumulation
     */
    context(_: Validation)
    public fun validate()
}
