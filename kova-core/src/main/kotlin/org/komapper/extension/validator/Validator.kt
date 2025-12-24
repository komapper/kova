package org.komapper.extension.validator

import org.komapper.extension.validator.ValidationIor.Both
import org.komapper.extension.validator.ValidationIor.FailureLike
import org.komapper.extension.validator.ValidationResult.Failure
import org.komapper.extension.validator.ValidationResult.Success

/**
 * Validates the input and returns a [ValidationResult].
 *
 * This is the recommended way to perform validation when you want to handle
 * both success and failure cases programmatically.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().min(1).max(10)
 * when (val result = validator.tryValidate("hello")) {
 *     is ValidationResult.Success -> println("Valid: ${result.value}")
 *     is ValidationResult.Failure -> println("Errors: ${result.details}")
 * }
 * ```
 *
 * @param config Configuration options for validation (failFast, logging)
 * @return A [ValidationResult] containing either the validated value or failure details
 */
fun <R> tryValidate(
    config: ValidationConfig = ValidationConfig(),
    validator: context(Validation) () -> ValidationIor<R>,
): ValidationResult<R> =
    when (val result = with(Validation(config = config)) { or(validator) }) {
        is ValidationResult -> result
        is Both -> result.messages.failure()
    }

/**
 * Validates the input and returns the validated value, or throws an exception on failure.
 *
 * Use this when you want validation failures to throw exceptions rather than
 * handling them programmatically.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().min(1).max(10)
 * try {
 *     val validated = validator.validate("hello")
 *     println("Valid: $validated")
 * } catch (e: ValidationException) {
 *     println("Errors: ${e.details}")
 * }
 * ```
 *
 * @param config Configuration options for validation (failFast, logging)
 * @return The validated value of type [R]
 * @throws ValidationException if validation fails
 */
fun <R> validate(
    config: ValidationConfig = ValidationConfig(),
    validator: context(Validation) () -> ValidationIor<R>,
): R =
    when (val result = tryValidate(config, validator)) {
        is Success -> result.value
        is Failure -> throw ValidationException(result.messages)
    }

context(_: Validation)
inline operator fun <R> ValidationIor<Unit>.plus(other: () -> ValidationIor<R>): ValidationResult<R> = this and other

context(_: Validation)
inline infix fun <R> ValidationIor<Unit>.and(other: () -> ValidationIor<R>): ValidationResult<R> = accumulateMessages() then { other() }

context(_: Validation)
inline infix fun <R> ValidationIor<Unit>.andMap(other: () -> R): ValidationResult<R> = accumulateMessages() map { other() }

context(c: Validation)
inline fun <R> or(block: context(Validation) () -> ValidationIor<R>): ValidationIor<R> {
    val newMessages = mutableListOf<Message>()
    return when (val result = block(c.copy(accumulate = { newMessages.addAll(it) }))) {
        is Success if newMessages.isEmpty() -> result
        is Success -> Both(result.value, newMessages)
        is FailureLike -> result.mapMessages { newMessages + it }
    }
}

context(_: Validation)
inline infix fun <R> ValidationIor<R>.or(block: context(Validation) () -> ValidationIor<R>): ValidationIor<R> {
    if (this !is FailureLike) return this
    val other =
        org.komapper.extension.validator
            .or(block)
    if (other !is FailureLike) return other
    val orMessage = "kova.or".resource(messages, other.messages)
    return (this as? Both ?: other).mapMessages { listOf(orMessage) }
}

context(_: Validation)
inline infix fun <R> ValidationIor<R>.orElse(block: context(Validation) () -> ValidationIor<R>): ValidationResult<R> =
    or(block).bind()

context(_: Validation)
inline fun <T, R> T.name(
    name: String,
    block: context(Validation) () -> R,
): R = addPath(name, this, block)

context(_: Validation)
inline fun <R> withMessage(
    noinline transform: (List<Message>) -> Message = { "kova.withMessage".resource(it) },
    block: context(Validation) () -> ValidationIor<R>,
): ValidationResult<R> = or(block).mapMessages { listOf(transform(it)) }.bind()

context(_: Validation)
inline fun <R> withMessage(
    message: String,
    block: context(Validation) () -> ValidationResult<R>,
): ValidationResult<R> = withMessage({ text(message) }, block)
