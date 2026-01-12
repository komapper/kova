package org.komapper.extension.validator

import org.komapper.extension.validator.ValidationIor.Both
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
 * @param R The type of the validated value
 * @param config Configuration options for validation (failFast, clock, logger)
 * @param validator The validation logic to execute within a Validation context
 * @return A [ValidationResult] containing either the validated value or failure messages
 */
public fun <R> tryValidate(
    config: ValidationConfig = ValidationConfig(),
    validator: context(Validation)() -> R,
): ValidationResult<R> =
    when (val result = context(Validation(config = config)) { ior { validator() } }) {
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
 * @param R The type of the validated value
 * @param config Configuration options for validation (failFast, clock, logger)
 * @param validator The validation logic to execute within a Validation context
 * @return The validated value of type [R]
 * @throws ValidationException if validation fails
 */
public fun <R> validate(
    config: ValidationConfig = ValidationConfig(),
    validator: context(Validation)() -> R,
): R =
    when (val result = tryValidate(config, validator)) {
        is Success -> result.value
        is Failure -> throw ValidationException(result.messages)
    }

/**
 * Exception thrown when validation fails using the [validate] function.
 *
 * This exception ensureContains detailed information about all validation failures,
 * including the constraint ID, validation path, and formatted error message for each failure.
 * Use [tryValidate] instead if you want to handle validation failures
 * programmatically without exceptions.
 *
 * Example:
 * ```kotlin
 * try {
 *     val result = validate { min("", 1); max("", 10) }
 *     println("Valid: $result")
 * } catch (e: ValidationException) {
 *     // Access validation error messages
 *     e.messages.forEach { message ->
 *         println("Path: ${message.path.fullName}")
 *         println("Message: ${message.text}")
 *         println("Constraint ID: ${message.constraintId}")
 *     }
 * }
 * ```
 *
 * @property messages List of [Message] objects describing each validation failure
 */
public class ValidationException(
    public val messages: List<Message>,
) : RuntimeException(messages.toString())
