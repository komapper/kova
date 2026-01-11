package org.komapper.extension.validator

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
    val messages: List<Message>,
) : RuntimeException(messages.toString())
