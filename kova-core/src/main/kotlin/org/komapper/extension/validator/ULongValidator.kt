package org.komapper.extension.validator

typealias ULongValidator = Validator<ULong, ULong>

fun ULongValidator.min(
    value: ULong,
    message: MessageProvider1<ULong, ULong> = Message.resource1("kova.ulong.min"),
) = constrain(message.id) {
    satisfies(it.input >= value, message(it, value))
}

fun ULongValidator.max(
    value: ULong,
    message: MessageProvider1<ULong, ULong> = Message.resource1("kova.ulong.max"),
) = constrain(message.id) {
    satisfies(it.input <= value, message(it, value))
}

fun ULongValidator.gt(
    value: ULong,
    message: MessageProvider1<ULong, ULong> = Message.resource1("kova.ulong.gt"),
) = constrain(message.id) {
    satisfies(it.input > value, message(it, value))
}

fun ULongValidator.gte(
    value: ULong,
    message: MessageProvider1<ULong, ULong> = Message.resource1("kova.ulong.gte"),
) = constrain(message.id) {
    satisfies(it.input >= value, message(it, value))
}

fun ULongValidator.lt(
    value: ULong,
    message: MessageProvider1<ULong, ULong> = Message.resource1("kova.ulong.lt"),
) = constrain(message.id) {
    satisfies(it.input < value, message(it, value))
}

fun ULongValidator.lte(
    value: ULong,
    message: MessageProvider1<ULong, ULong> = Message.resource1("kova.ulong.lte"),
) = constrain(message.id) {
    satisfies(it.input <= value, message(it, value))
}
