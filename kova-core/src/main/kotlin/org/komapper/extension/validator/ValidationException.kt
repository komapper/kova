package org.komapper.extension.validator

/**
 * Exception thrown when validation fails using the [Validator.validate] method.
 *
 * This exception contains detailed information about all validation failures.
 * Use [Validator.tryValidate] instead if you want to handle validation failures
 * programmatically without exceptions.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().min(1).max(10)
 *
 * try {
 *     val result = validator.validate("") // Throws ValidationException
 * } catch (e: ValidationException) {
 *     // Access validation messages
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
class ValidationException(
    val messages: List<Message>,
) : RuntimeException(messages.toString())
