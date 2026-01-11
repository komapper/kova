package org.komapper.extension.validator

import org.komapper.extension.validator.ValidationIor.Both
import org.komapper.extension.validator.ValidationIor.FailureLike
import org.komapper.extension.validator.ValidationResult.Failure
import org.komapper.extension.validator.ValidationResult.Success

/**
 * Executes validation logic and returns a [ValidationResult].
 *
 * This is the recommended way to perform validation when you want to handle
 * both success and failure cases programmatically. The validation logic is
 * executed within a [Validation] context that provides constraint validation functions.
 *
 * Example using when expression:
 * ```kotlin
 * val text = getUserInput()
 * when (val result = tryValidate { text.ensureLengthInRange(1..10) }) {
 *     is ValidationResult.Success -> println("Valid: ${result.value}")
 *     is ValidationResult.Failure -> println("Errors: ${result.messages}")
 * }
 * ```
 *
 * Example using isSuccess():
 * ```kotlin
 * val text = getUserInput()
 * val result = tryValidate { text.ensureLengthInRange(1..10) }
 * if (result.isSuccess()) {
 *     // result is automatically smart-cast to Success here
 *     println("Valid: ${result.value}")
 * } else {
 *     // result is automatically smart-cast to Failure here
 *     println("Errors: ${result.messages}")
 * }
 * ```
 *
 * @param config Configuration options for validation (failFast, clock, logger)
 * @param validator The validation logic to execute within a Validation context
 * @return A [ValidationResult] containing either the validated value or failure messages
 */
fun <R> tryValidate(
    config: ValidationConfig = ValidationConfig(),
    validator: context(Validation)() -> R,
): ValidationResult<R> =
    when (val result = context(Validation(config = config)) { or { validator() } }) {
        is ValidationResult -> result
        is Both -> Failure(result.messages)
    }

/**
 * Executes validation logic and returns the validated value, or throws an exception on failure.
 *
 * Use this when you want validation failures to throw exceptions rather than
 * handling them programmatically. The validation logic is executed within a
 * [Validation] context that provides constraint validation functions.
 *
 * Example:
 * ```kotlin
 * val text = getUserInput()
 * try {
 *     val validated = validate { text.ensureLengthInRange(1..10) }
 *     println("Valid: $validated")
 * } catch (e: ValidationException) {
 *     println("Errors: ${e.messages}")
 * }
 * ```
 *
 * @param config Configuration options for validation (failFast, clock, logger)
 * @param validator The validation logic to execute within a Validation context
 * @return The validated value of type [R]
 * @throws ValidationException if validation fails
 */
fun <R> validate(
    config: ValidationConfig = ValidationConfig(),
    validator: context(Validation)() -> R,
): R =
    when (val result = tryValidate(config, validator)) {
        is Success -> result.value
        is Failure -> throw ValidationException(result.messages)
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
 * @param block The validation logic to execute
 * @return A [ValidationIor] containing the result and/or accumulated error messages
 */
context(v: Validation)
inline fun <R> or(block: context(Validation)() -> R): ValidationIor<R> {
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
 * Executes a validation block and wraps any errors in a custom message.
 *
 * If the validation block fails, all accumulated error messages are transformed
 * into a single message using the provided transform function. If validation
 * succeeds, the result is returned directly.
 *
 * This is useful for providing context-specific error messages that wrap
 * detailed validation failures.
 *
 * Example:
 * ```kotlin
 * withMessage({ messages -> "Address validation failed".resource(messages) }) {
 *     // Validate address fields
 * }
 * ```
 *
 * @param transform Function to transform the list of error messages into a single message
 * @param block The validation logic to execute
 * @return The validated result
 */
context(_: Validation)
inline fun <R> withMessage(
    noinline transform: (List<Message>) -> Message = { "kova.withMessage".resource(it) },
    block: context(Validation)() -> R,
): R =
    when (val result = or(block)) {
        is Success -> result.value
        is FailureLike -> result.withMessage(transform(result.messages)).bind()
    }

/**
 * Executes a validation block and wraps any errors in a text message.
 *
 * This is a convenience overload of [withMessage] that creates a simple text
 * message instead of requiring a transform function.
 *
 * Example:
 * ```kotlin
 * withMessage("Address validation failed") {
 *     // Validate address fields
 * }
 * ```
 *
 * @param message The error message text to use if validation fails
 * @param block The validation logic to execute
 * @return The validated result
 */
context(_: Validation)
inline fun <R> withMessage(
    message: String,
    block: context(Validation)() -> R,
): R = withMessage({ text(message) }, block)
