package org.komapper.extension.validator

import org.komapper.extension.validator.ValidationIor.Both
import org.komapper.extension.validator.ValidationIor.FailureLike
import org.komapper.extension.validator.ValidationResult.Failure
import org.komapper.extension.validator.ValidationResult.Success
import kotlin.contracts.contract

/**
 * Result of a validation operation, either [Success] or [Failure].
 *
 *
 * Example:
 * ```kotlin
 * when (val result = tryValidate { min("hello", 1) }) {
 *     is ValidationResult.Success -> println("Value: ${result.value}")
 *     is ValidationResult.Failure -> println("Errors: ${result.messages}")
 * }
 * ```
 *
 * @param T The type of the validated value on success
 */
public sealed interface ValidationResult<out T> : ValidationIor<T> {
    /**
     * Represents a successful validation with the validated value.
     *
     * @param T The type of the validated value
     * @property value The validated value that passed all constraints
     */
    public data class Success<out T>(
        val value: T,
    ) : ValidationResult<T>

    /**
     * Represents a failed validation with detailed error information.
     *
     * @property messages List of error messages describing what went wrong
     */
    public data class Failure(
        override val messages: List<Message>,
    ) : FailureLike<Nothing>,
        ValidationResult<Nothing> {
        /**
         * Creates a new [Failure] with the specified message replacing existing messages.
         *
         * @param message The message to use as the sole error message
         * @return A new [Failure] instance with only the specified message
         */
        override fun withMessage(message: Message): Failure = Failure(listOf(message))
    }
}

/**
 * Type-safe check if this result is a success.
 *
 * Uses Kotlin contracts for smart casting. When this function returns true,
 * the result is automatically smart-cast to [Success]. When it returns false,
 * the result is automatically smart-cast to [Failure].
 *
 * Example:
 * ```kotlin
 * val result = tryValidate { min("hello", 1) }
 * if (result.isSuccess()) {
 *     // result is automatically smart-cast to Success here
 *     println("Value: ${result.value}")
 * } else {
 *     // result is automatically smart-cast to Failure here
 *     println("Errors: ${result.messages}")
 * }
 * ```
 *
 * @param T The type of the validated value
 * @receiver The validation result to check
 * @return `true` if this result is a [Success], `false` if it is a [Failure]
 */
public fun <T> ValidationResult<T>.isSuccess(): Boolean {
    contract {
        returns(true) implies (this@isSuccess is Success)
        returns(false) implies (this@isSuccess is Failure)
    }
    return this is Success
}

/**
 * Type-safe check if this result is a failure.
 *
 * Uses Kotlin contracts for smart casting. When this function returns true,
 * the result is automatically smart-cast to [Failure]. When it returns false,
 * the result is automatically smart-cast to [Success].
 *
 * Example:
 * ```kotlin
 * val result = tryValidate { min("hello", 1) }
 * if (result.isFailure()) {
 *     // result is automatically smart-cast to Failure here
 *     println("Errors: ${result.messages}")
 * } else {
 *     // result is automatically smart-cast to Success here
 *     println("Value: ${result.value}")
 * }
 * ```
 *
 * @param T The type of the validated value
 * @receiver The validation result to check
 * @return `true` if this result is a [Failure], `false` if it is a [Success]
 */
public fun <T> ValidationResult<T>.isFailure(): Boolean {
    contract {
        returns(true) implies (this@isFailure is Failure)
        returns(false) implies (this@isFailure is Success<T>)
    }
    return this is Failure
}
