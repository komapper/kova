package org.komapper.extension.validator

import kotlin.contracts.contract

/**
 * Represents a validation constraint context that provides methods to evaluate conditions
 * and raise validation errors.
 *
 * This class is typically accessed through the `constrain()` extension function, which
 * creates a constraint context for a given input value and constraint ID. Within this context,
 * you can use the `satisfies()` method to define validation rules.
 *
 * Example:
 * ```kotlin
 * fun Validation.positive(input: Int) = input.constrain("kova.number.positive") {
 *     satisfies(it > 0) { "kova.number.positive".resource }
 * }
 * ```
 *
 * @property validation The validation context that accumulates errors and manages validation state
 * @see satisfies
 */
data class Constraint(
    val validation: Validation,
) {
    /**
     * Evaluates a condition and raises a validation error if it fails.
     *
     * This is the preferred form for most validation constraints. It accepts a [MessageProvider]
     * lambda that is only evaluated when the condition is false, enabling lazy message construction.
     * This is beneficial when message creation involves resource lookups or formatting.
     *
     * Example:
     * ```kotlin
     * fun Validation.positive(
     *     input: Int,
     *     message: MessageProvider = { "kova.number.positive".resource }
     * ) = input.constrain("kova.number.positive") {
     *     satisfies(it > 0, message)
     * }
     *
     * tryValidate { positive(5) }  // Success (message provider not evaluated)
     * tryValidate { positive(-1) } // Failure (message provider evaluated)
     * ```
     *
     * @param condition The condition to evaluate
     * @param message A MessageProvider lambda that produces the error message if the condition is false
     * @see satisfies Overload that accepts a Message directly
     */
    fun satisfies(
        condition: Boolean,
        message: MessageProvider,
    ) {
        contract { returns() implies condition }
        satisfies(condition, message())
    }

    /**
     * Lower-level variant that accepts a [Message] instance directly instead of a [MessageProvider].
     *
     * Example:
     * ```kotlin
     * val errorMessage = "kova.string.pattern".resource(pattern)
     * satisfies(input.matches(regex), errorMessage)
     * ```
     *
     * @param condition The condition to evaluate
     * @param message The error message to raise if the condition is false
     * @see satisfies Overload that accepts a MessageProvider for lazy evaluation
     */
    fun satisfies(
        condition: Boolean,
        message: Message,
    ) {
        contract { returns() implies condition }
        if (!condition) validation.raise(message)
    }
}
