package org.komapper.extension.validator

fun <T> Validator<T, T>.literal(
    value: T,
    message: MessageProvider1<T, T> = Message.resource1("kova.literal.single"),
) = constrain(message.id) {
    satisfies(it.input == value, message(it, value))
}

fun <T> Validator<T, T>.literal(
    values: List<T>,
    message: MessageProvider1<T, List<T>> = Message.resource1("kova.literal.list"),
) = constrain(message.id) {
    satisfies(it.input in values, message(it, values))
}
