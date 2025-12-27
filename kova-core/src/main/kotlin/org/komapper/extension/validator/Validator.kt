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
    validator: Validation.() -> R,
): ValidationResult<R> =
    when (val result = with(Validation(config = config)) { or { validator() } }) {
        is ValidationResult -> result
        is Both -> Failure(result.messages)
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
    validator: Validation.() -> R,
): R =
    when (val result = tryValidate(config, validator)) {
        is Success -> result.value
        is Failure -> throw ValidationException(result.messages)
    }

inline fun <R> Validation.or(block: Validation.() -> R): ValidationIor<R> {
    val messages = mutableListOf<Message>()
    return recoverValidation({ Failure(messages) }) {
        val result =
            block(
                copy(acc = {
                    messages.addAll(it)
                    if (config.failFast) raise()
                    this
                }),
            )
        if (messages.isEmpty()) Success(result) else Both(result, messages)
    }
}

inline fun <R> Validation.withMessage(
    noinline transform: (List<Message>) -> Message = { "kova.withMessage".resource(it) },
    block: Validation.() -> R,
): R =
    when (val result = or(block)) {
        is Success -> result.value
        is FailureLike -> result.withMessage(transform(result.messages)).bind()
    }

inline fun <R> Validation.withMessage(
    message: String,
    block: Validation.() -> R,
): R = withMessage({ text(message) }, block)
