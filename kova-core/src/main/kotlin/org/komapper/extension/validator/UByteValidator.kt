package org.komapper.extension.validator

typealias UByteValidator = IdentityValidator<UByte>

fun UByteValidator.min(
    value: UByte,
    message: MessageProvider1<UByte, UByte> = Message.resource1("kova.ubyte.min"),
) = constrain(message.id) {
    satisfies(it.input >= value, message(it, value))
}

fun UByteValidator.max(
    value: UByte,
    message: MessageProvider1<UByte, UByte> = Message.resource1("kova.ubyte.max"),
) = constrain(message.id) {
    satisfies(it.input <= value, message(it, value))
}

fun UByteValidator.gt(
    value: UByte,
    message: MessageProvider1<UByte, UByte> = Message.resource1("kova.ubyte.gt"),
) = constrain(message.id) {
    satisfies(it.input > value, message(it, value))
}

fun UByteValidator.gte(
    value: UByte,
    message: MessageProvider1<UByte, UByte> = Message.resource1("kova.ubyte.gte"),
) = constrain(message.id) {
    satisfies(it.input >= value, message(it, value))
}

fun UByteValidator.lt(
    value: UByte,
    message: MessageProvider1<UByte, UByte> = Message.resource1("kova.ubyte.lt"),
) = constrain(message.id) {
    satisfies(it.input < value, message(it, value))
}

fun UByteValidator.lte(
    value: UByte,
    message: MessageProvider1<UByte, UByte> = Message.resource1("kova.ubyte.lte"),
) = constrain(message.id) {
    satisfies(it.input <= value, message(it, value))
}
