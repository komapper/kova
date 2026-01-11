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
 * context(_: Validation)
 * fun Int.ensurePositive() = constrain("kova.number.ensurePositive") {
 *     satisfies(it > 0) { "kova.number.ensurePositive".resource }
 * }
 * ```
 *
 * @property validation The validation context that accumulates errors and manages validation state
 * @see satisfies
 */
public data class Constraint(
    val validation: Validation,
) {
    /**
     * Evaluates a condition and raises a validation error if it fails.
     *
     * This accepts a [MessageProvider]
     * lambda that is only evaluated when the condition is false, enabling lazy message construction.
     * This is beneficial when message creation involves resource lookups or formatting.
     *
     * Example:
     * ```kotlin
     * context(_: Validation)
     * fun Int.ensurePositive(
     *     message: MessageProvider = { "kova.number.ensurePositive".resource }
     * ) = constrain("kova.number.ensurePositive") {
     *     satisfies(it > 0, message)
     * }
     *
     * tryValidate { 5.ensurePositive() }  // Success (message provider not evaluated)
     * tryValidate { (-1).ensurePositive() } // Failure (message provider evaluated)
     * ```
     *
     * @param condition The condition to evaluate
     * @param message A MessageProvider lambda that produces the error message if the condition is false
     */
    public fun satisfies(
        condition: Boolean,
        message: MessageProvider,
    ) {
        contract { returns() implies condition }
        if (!condition) context(validation) { raise(message()) }
    }
}
