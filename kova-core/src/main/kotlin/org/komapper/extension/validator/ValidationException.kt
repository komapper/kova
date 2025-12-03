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
 *     // Access failure details
 *     e.details.forEach { detail ->
 *         println("Path: ${detail.path.fullName}")
 *         println("Message: ${detail.message.content}")
 *     }
 *     // Or access messages directly
 *     e.messages.forEach { message ->
 *         println(message.content)
 *     }
 * }
 * ```
 *
 * @property details List of [FailureDetail] objects describing each validation failure
 */
class ValidationException(
    val details: List<FailureDetail>,
) : RuntimeException(details.toString()) {
    /**
     * List of error messages extracted from the failure details.
     *
     * This is a convenience property for accessing just the messages
     * without the full failure context.
     */
    val messages: List<Message> get() = details.map { it.message }
}
