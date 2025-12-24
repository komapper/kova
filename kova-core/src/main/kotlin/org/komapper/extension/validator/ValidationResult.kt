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

        fun withMessages(messages: List<Message>): FailureLike<T>
    }

    data class Both<out T>(
        val value: T,
        override val messages: List<Message>,
    ) : FailureLike<T> {
        override fun withMessages(messages: List<Message>): Both<T> = Both(value, messages)
    }
}

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
        override fun withMessages(messages: List<Message>): Failure = Failure(messages)
    }
}

fun <T> T.success(): Success<T> = Success(this)

fun Message.failure(): Failure = listOf(this).failure()

fun List<Message>.failure(): Failure = Failure(this)

fun ValidationResult<Unit>?.orSucceed(): ValidationResult<Unit> = this ?: Unit.success()

inline infix fun <T> ValidationResult<T>.getOrElse(defaultValue: (Failure) -> T): T =
    when (this) {
        is Success -> value
        is Failure -> defaultValue(this)
    }

context(c: Validation)
fun <T> ValidationIor<T>.bind(): ValidationResult<T> =
    when (this) {
        is ValidationResult -> this
        is Both if failFast -> messages.failure()
        is Both -> {
            c.accumulate(messages)
            value.success()
        }
    }

context(_: Validation)
inline infix fun <T, R> ValidationIor<T>.map(transform: (T) -> R): ValidationResult<R> = then { transform(it).success() }

context(_: Validation)
inline infix fun <T, R> ValidationIor<T>.then(transform: (T) -> ValidationIor<R>): ValidationResult<R> =
    when (val res = bind()) {
        is Success -> transform(res.value).bind()
        is Failure -> res
    }

context(_: Validation)
inline infix fun <T> ValidationIor<T>.alsoThen(transform: (T) -> ValidationIor<Unit>): ValidationResult<T> =
    then { transform(it).map { _ -> it } }

inline fun <T> ValidationIor<T>.mapMessages(transform: (List<Message>) -> List<Message>): ValidationIor<T> =
    when (this) {
        is Success -> this
        is FailureLike -> withMessages(transform(messages))
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
        returns(true) implies (this@isFailure is Failure)
        returns(false) implies (this@isFailure is Success)
    }
    return this is Failure
}
