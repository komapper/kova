package org.komapper.extension.validator

class MessageException(
    val validationMessage: Message,
) : RuntimeException(validationMessage.toString())
