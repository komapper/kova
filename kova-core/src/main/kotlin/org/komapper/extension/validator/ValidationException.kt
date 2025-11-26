package org.komapper.extension.validator

class ValidationException(
    val details: List<ValidationResult.FailureDetail>,
) : RuntimeException(details.toString()) {
    val messages: List<Message> get() = details.map { it.message }
}
