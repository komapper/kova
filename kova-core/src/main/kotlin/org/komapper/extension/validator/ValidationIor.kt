package org.komapper.extension.validator

import org.komapper.extension.validator.ValidationIor.Both
import org.komapper.extension.validator.ValidationIor.FailureLike
import org.komapper.extension.validator.ValidationResult.Failure
import org.komapper.extension.validator.ValidationResult.Success

/**
 * An inclusive-or (Ior) representation of validation results.
 *
 * This sealed interface is used internally to represent validation results that can have
 * both a value and error messages simultaneously ([Both]), or just errors ([FailureLike]).
 * [ValidationResult] extends this to provide the public Success/Failure API.
 *
 * @param T The type of the validated value
 */
public sealed interface ValidationIor<out T> {
    /**
     * Represents a validation result that contains error messages.
     *
     * This can be either a [Failure] (only errors, no value) or [Both] (value with errors).
     *
     * @param T The type of the validated value
     * @property messages The list of error messages from validation failures
     */
    public sealed interface FailureLike<out T> : ValidationIor<T> {
        public val messages: List<Message>

        /**
         * Creates a new [FailureLike] with the specified message replacing existing messages.
         *
         * @param message The message to use as the sole error message
         * @return A new [FailureLike] instance with only the specified message
         */
        public fun withMessage(message: Message): FailureLike<T>
    }

    /**
     * Represents a validation that produced a value but also accumulated error messages.
     *
     * This is used internally during validation to track partial successes. When converted
     * to [ValidationResult], this becomes a [Failure] containing the accumulated messages.
     *
     * @param T The type of the validated value
     * @property value The validated value that was produced despite errors
     * @property messages The list of error messages accumulated during validation
     */
    public data class Both<out T>(
        val value: T,
        override val messages: List<Message>,
    ) : FailureLike<T> {
        /**
         * Creates a new [Both] with the specified message replacing existing messages.
         *
         * @param message The message to use as the sole error message
         * @return A new [Both] instance with the same value but only the specified message
         */
        override fun withMessage(message: Message): Both<T> = Both(value, listOf(message))
    }
}

/**
 * Extracts the value from a [ValidationIor], accumulating or raising errors as needed.
 *
 * This function handles all cases of [ValidationIor]:
 * - [Success]: Returns the value directly
 * - [Failure]: Raises validation failure with accumulated messages
 * - [Both]: Accumulates partial errors and returns the partial value
 *
 * Example:
 * ```kotlin
 * val result: ValidationIor<String> = or { ensureNotBlank(name) }
 * val value: String = result.bind()  // Extracts or raises
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver ValidationIor<T> The validation result to extract from
 * @param T The type of the value
 * @return The validated value
 * @throws ValidationCancellationException if this is a [Failure]
 */
context(_: Validation)
public fun <T> ValidationIor<T>.bind(): T =
    when (this) {
        is Success -> value
        is Failure -> raise(messages)
        is Both -> {
            accumulate(messages)
            value
        }
    }

/**
 * Executes a validation block and returns a [ValidationIor] with accumulated errors.
 *
 * This function executes the validation logic within an accumulating context that collects
 * all validation errors (unless failFast is enabled). It returns either [Success] if no
 * errors occurred, [Failure] if validation failed with no result, or [Both] if validation
 * produced a result but also accumulated errors.
 *
 * This is typically used internally by [tryValidate] and other validation combinators.
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param R The type of the validation result
 * @param block The validation logic to execute
 * @return A [ValidationIor] containing the result and/or accumulated error messages
 */
context(v: Validation)
public inline fun <R> or(block: context(Validation)() -> R): ValidationIor<R> = ior(block)

@PublishedApi
context(v: Validation)
internal inline fun <R> ior(block: context(Validation)() -> R): ValidationIor<R> {
    val messages = mutableListOf<Message>()
    return recoverValidation({ Failure(messages) }) {
        val result =
            block(
                v.copy(acc = {
                    messages.addAll(it)
                    if (v.config.failFast) raise()
                    this
                }),
            )
        if (messages.isEmpty()) Success(result) else Both(result, messages)
    }
}

/**
 * Attempts alternative validation logic if this validation fails.
 *
 * This function implements a fallback strategy: if the current validation succeeds
 * (is not [FailureLike]), it returns immediately. Otherwise, it executes the fallback
 * block. If both fail, it wraps both error messages with a combined "or" message.
 *
 * Example:
 * ```kotlin
 * tryValidate {
 *     or { min(input, 10) } or { max(input, 5) }
 *     // Validates that input >= 10 OR input <= 5
 * }
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver ValidationIor<R> The current validation result
 * @param R The type of the validation result
 * @param block The fallback validation logic to try if this fails
 * @return [ValidationIor] representing the combined validation result
 */
context(v: Validation)
public inline infix fun <R> ValidationIor<R>.or(block: context(Validation)() -> R): ValidationIor<R> {
    if (this !is FailureLike) return this
    val other = ior(block)
    if (other !is FailureLike) return other
    return (this as? Both ?: other).withMessage("kova.or".resource(messages, other.messages))
}

/**
 * Attempts alternative validation logic and extracts the result, raising errors if both fail.
 *
 * This is a convenience function that combines [or] and [bind]. It tries the current
 * validation, falls back to the alternative if needed, and extracts the value or raises
 * accumulated errors.
 *
 * Example:
 * ```kotlin
 * tryValidate {
 *     val value = or { min(input, 10) } orElse { max(input, 5) }
 *     // Returns the value if either constraint passes, raises if both fail
 * }
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver ValidationIor<R> The current validation result
 * @param R The type of the validation result
 * @param block The fallback validation logic to try if this fails
 * @return The validated value
 * @throws ValidationCancellationException if both validations fail
 */
context(_: Validation)
public inline infix fun <R> ValidationIor<R>.orElse(block: context(Validation)() -> R): R = or(block).bind()
