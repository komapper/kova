package org.komapper.extension.validator

import org.komapper.extension.validator.ValidationIor.FailureLike
import org.komapper.extension.validator.ValidationResult.Failure
import org.komapper.extension.validator.ValidationResult.Success
import kotlin.contracts.contract

/**
 * An inclusive-or (Ior) representation of validation results.
 *
 * This sealed interface is used internally to represent validation results that can have
 * both a value and error messages simultaneously ([Both]), or just errors ([FailureLike]).
 * [ValidationResult] extends this to provide the public Success/Failure API.
 */
public sealed interface ValidationIor<out T> {
    /**
     * Represents a validation result that ensureContains error messages.
     *
     * This can be either a [Failure] (only errors, no value) or [Both] (value with errors).
     */
    public sealed interface FailureLike<out T> : ValidationIor<T> {
        val messages: List<Message>

        fun withMessage(message: Message): FailureLike<T>
    }

    /**
     * Represents a validation that produced a value but also accumulated error messages.
     *
     * This is used internally during validation to track partial successes. When converted
     * to [ValidationResult], this becomes a [Failure] containing the accumulated messages.
     */
    public data class Both<out T>(
        val value: T,
        override val messages: List<Message>,
    ) : FailureLike<T> {
        override fun withMessage(message: Message): Both<T> = Both(value, listOf(message))
    }
}

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
 */
public fun <T> ValidationResult<T>.isFailure(): Boolean {
    contract {
        returns(true) implies (this@isFailure is Failure)
        returns(false) implies (this@isFailure is Success<T>)
    }
    return this is Failure
}
