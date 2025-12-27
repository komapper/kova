package org.komapper.extension.validator

fun Validation.logAndAddDetails(
    message: Message,
    input: Any?,
    id: String,
): Message {
    log {
        LogEntry.Violated(
            constraintId = id,
            root = message.root,
            path = message.path.fullName,
            input = input,
            args = if (message is Message.Resource) message.args.asList() else emptyList(),
        )
    }
    return message.withDetails(input, id)
}

inline fun <R> Validation.mapEachMessage(
    noinline transform: (Message) -> Message,
    block: Validation.() -> R,
): R = block(copy(acc = { accumulate(it.map(transform)) }))
