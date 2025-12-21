package org.komapper.extension.validator

import org.komapper.extension.validator.ValidationResult.Both
import org.komapper.extension.validator.ValidationResult.Failed
import org.komapper.extension.validator.ValidationResult.Failure
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
sealed class ValidationResult<out T> {
    sealed class Failure<out T> : ValidationResult<T>() {
        abstract val messages: List<Message>
    }

    inline fun mapMessages(transform: (List<Message>) -> List<Message>): ValidationResult<T> =
        when (this) {
            is Success -> this
            is Failed -> copy(messages = transform(messages))
            is Both -> copy(messages = transform(messages))
        }

    inline fun <R> map(transform: (T) -> R): ValidationResult<R> =
        when (this) {
            is Success -> Success(transform(value))
            is Failed -> this
            is Both -> Both(transform(value), messages)
        }

    inline fun <R> then(transform: (T) -> ValidationResult<R>): ValidationResult<R> =
        when (this) {
            is Failed -> this
            is Success -> transform(value)
            is Both ->
                when (val result = transform(value)) {
                    is Success -> Both(result.value, messages)
                    is Failed -> Failed(messages + result.messages)
                    is Both -> Both(result.value, messages + result.messages)
                }
        }

    /**
     * Represents a successful validation with the validated value.
     *
     * @param value The validated value
     * @param context The validation context after validation completed
     */
    data class Success<out T>(
        val value: T,
    ) : ValidationResult<T>()

    /**
     * Represents a failed validation with detailed error information.
     *
     * @property messages List of error messages describing what went wrong
     */
    data class Failed(
        override val messages: List<Message>,
    ) : Failure<Nothing>()

    data class Both<out T>(
        val value: T,
        override val messages: List<Message>,
    ) : Failure<T>()
}

fun <T> ValidationContext.both(
    value: T,
    messages: List<Message>,
): Failure<T> = if (failFast) Failed(messages) else Both(value, messages)

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
