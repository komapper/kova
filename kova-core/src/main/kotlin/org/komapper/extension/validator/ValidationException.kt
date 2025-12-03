package org.komapper.extension.validator

class ValidationException(
    val details: List<FailureDetail>,
) : RuntimeException(details.toString()) {
    val messages: List<Message> get() = details.map { it.message }
}
