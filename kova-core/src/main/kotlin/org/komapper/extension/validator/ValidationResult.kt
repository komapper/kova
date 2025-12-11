package org.komapper.extension.validator

import org.komapper.extension.validator.ValidationResult.Failure
import org.komapper.extension.validator.ValidationResult.Success
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Result of a validation operation, either [Success] or [Failure].
 *
 * Use [isSuccess] and [isFailure] extension functions with Kotlin contracts
 * for type-safe result handling.
 *
 * Example:
 * ```kotlin
 * val result = validator.tryValidate(input)
 * when (result) {
 *     is ValidationResult.Success -> println("Value: ${result.value}")
 *     is ValidationResult.Failure -> println("Errors: ${result.messages}")
 * }
 * ```
 *
 * @param T The type of the validated value on success
 */
sealed interface ValidationResult<out T> {
    /**
     * Represents a successful validation with the validated value.
     *
     * @param value The validated value
     * @param context The validation context after validation completed
     */
    data class Success<T>(
        val value: T,
        val context: ValidationContext,
    ) : ValidationResult<T>

    /**
     * Represents a failed validation with detailed error information.
     *
     * @property messages List of error messages describing what went wrong
     */
    data class Failure<T>(
        val value: Input<T>,
        val messages: List<Message>,
    ) : ValidationResult<T>
}

/**
 * Combines two validation results.
 *
 * - If this is [Success], returns [other]
 * - If this is [Failure] and [other] is [Success], returns this failure
 * - If both are [Failure], combines their messages
 *
 * This is used internally by the [and] operator to accumulate failures.
 */
operator fun <T> ValidationResult<T>.plus(other: ValidationResult<T>): ValidationResult<T> =
    when (this) {
        is Success -> other
        is Failure ->
            when (other) {
                is Success -> Failure(value = Input.Available(other.value), messages = this.messages)
                is Failure -> Failure(other.value, messages = this.messages + other.messages)
            }
    }

/**
 * Type-safe check if this result is a success.
 *
 * Uses Kotlin contracts for smart casting, so after checking `isSuccess()`,
 * the result is automatically cast to [ValidationResult.Success].
 *
 * Example:
 * ```kotlin
 * val result = validator.tryValidate(input)
 * if (result.isSuccess()) {
 *     // result is automatically cast to Success here
 *     println("Value: ${result.value}")
 * }
 * ```
 */
@OptIn(ExperimentalContracts::class)
fun <T> ValidationResult<T>.isSuccess(): Boolean {
    contract {
        returns(true) implies (this@isSuccess is ValidationResult.Success)
        returns(false) implies (this@isSuccess is ValidationResult.Failure)
    }
    return this is ValidationResult.Success
}

/**
 * Type-safe check if this result is a failure.
 *
 * Uses Kotlin contracts for smart casting, so after checking `isFailure()`,
 * the result is automatically cast to [ValidationResult.Failure].
 *
 * Example:
 * ```kotlin
 * val result = validator.tryValidate(input)
 * if (result.isFailure()) {
 *     // result is automatically cast to Failure here
 *     println("Errors: ${result.messages}")
 * }
 * ```
 */
@OptIn(ExperimentalContracts::class)
fun <T> ValidationResult<T>.isFailure(): Boolean {
    contract {
        returns(true) implies (this@isFailure is ValidationResult.Failure)
        returns(false) implies (this@isFailure is ValidationResult.Success)
    }
    return this is ValidationResult.Failure
}

/**
 * Represents the input value state in a validation failure.
 *
 * This sealed interface captures the state of the input value when validation fails:
 * - [Available]: The input value is present and can be accessed
 * - [Unusable]: The input value could not be used (e.g., type conversion failure)
 *
 * @param T The expected type of the input value
 */
sealed interface Input<out T> {
    /**
     * Represents an available input value in a validation failure.
     *
     * This is used when validation fails but the input value is still accessible.
     * For example, when a string fails a length constraint, the string value is still available.
     *
     * @param value The input value that failed validation
     */
    data class Available<T>(
        val value: T,
    ) : Input<T>

    /**
     * Represents an unusable input value in a validation failure.
     *
     * This is used when the input value could not be used for validation, typically
     * when a type conversion or transformation fails. For example, when attempting
     * to parse "abc" as an integer, the original string value is unusable as an integer.
     *
     * @param value The original value before the failed conversion attempt
     */
    data class Unusable(
        val value: Any?,
    ) : Input<Nothing>
}
