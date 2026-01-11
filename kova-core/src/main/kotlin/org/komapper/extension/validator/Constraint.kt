package org.komapper.extension.validator

import org.komapper.extension.validator.Constraint.satisfies
import kotlin.contracts.contract

/**
 * Represents a validation constraint context that provides methods to evaluate conditions
 * and raise validation errors.
 *
 * This object is typically accessed through the `constrain()` extension function, which
 * creates a constraint context for a given input value and constraint ID. Within this context,
 * you can use the [satisfies] method to define validation rules.
 *
 * Example:
 * ```kotlin
 * context(_: Validation)
 * fun Int.ensurePositive() = constrain("kova.number.ensurePositive") {
 *     satisfies(it > 0) { "kova.number.ensurePositive".resource }
 * }
 * ```
 *
 * @see satisfies
 * @see constrain
 */
public object Constraint {
    /**
     * Evaluates a condition and raises a validation error if it fails.
     *
     * This accepts a [MessageProvider]
     * lambda that is only evaluated when the condition is false, enabling lazy message construction.
     * This is beneficial when message creation involves resource lookups or formatting.
     *
     * This function uses a Kotlin contract to enable smart casting: when it returns normally,
     * the compiler knows the condition was true.
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
     * @param Validation (context parameter) The validation context for constraint checking and error accumulation
     * @param condition The condition to evaluate; if false, the message provider is invoked and an error is raised
     * @param message A [MessageProvider] lambda that produces the error message if the condition is false
     * @return Unit. This function returns normally only when the condition is true; otherwise, it raises an error
     */
    context(_: Validation)
    public fun satisfies(
        condition: Boolean,
        message: MessageProvider,
    ) {
        contract { returns() implies condition }
        if (!condition) raise(message())
    }
}
