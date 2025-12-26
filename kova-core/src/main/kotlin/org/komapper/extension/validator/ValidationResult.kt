package org.komapper.extension.validator

import org.komapper.extension.validator.ValidationIor.Both
import org.komapper.extension.validator.ValidationIor.FailureLike
import org.komapper.extension.validator.ValidationResult.Failure
import org.komapper.extension.validator.ValidationResult.Success
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

sealed interface ValidationIor<out T> {
    sealed interface FailureLike<out T> : ValidationIor<T> {
        val messages: List<Message>

        fun withMessage(message: Message): FailureLike<T>
    }

    data class Both<out T>(
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
 * val result = validator.tryValidate(input)
 * when (result) {
 *     is ValidationResult.Success -> println("Value: ${result.value}")
 *     is ValidationResult.Failure -> println("Errors: ${result.messages}")
 * }
 * ```
 *
 * @param T The type of the validated value on success
 */
sealed interface ValidationResult<out T> : ValidationIor<T> {
    /**
     * Represents a successful validation with the validated value.
     *
     * @param value The validated value
     * @param context The validation context after validation completed
     */
    data class Success<out T>(
        val value: T,
    ) : ValidationResult<T>

    /**
     * Represents a failed validation with detailed error information.
     *
     * @property messages List of error messages describing what went wrong
     */
    data class Failure(
        override val messages: List<Message>,
    ) : FailureLike<Nothing>,
        ValidationResult<Nothing> {
        override fun withMessage(message: Message): Failure = Failure(listOf(message))
    }
}

context(_: Accumulate)
fun <T> ValidationIor<T>.bind(): T =
    when (this) {
        is Success -> value
        is Failure -> raise(messages)
        is Both -> {
            accumulate(messages)
            value
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
        returns(true) implies (this@isSuccess is Success)
        returns(false) implies (this@isSuccess is Failure)
    }
    return this is Success
}
