package org.komapper.extension.validator

typealias UIntValidator = Validator<UInt, UInt>

fun UIntValidator.min(
    value: UInt,
    message: MessageProvider1<UInt, UInt> = Message.resource1("kova.uint.min"),
) = constrain(message.id) {
    satisfies(it.input >= value, message(it, value))
}

fun UIntValidator.max(
    value: UInt,
    message: MessageProvider1<UInt, UInt> = Message.resource1("kova.uint.max"),
) = constrain(message.id) {
    satisfies(it.input <= value, message(it, value))
}

fun UIntValidator.gt(
    value: UInt,
    message: MessageProvider1<UInt, UInt> = Message.resource1("kova.uint.gt"),
) = constrain(message.id) {
    satisfies(it.input > value, message(it, value))
}

fun UIntValidator.gte(
    value: UInt,
    message: MessageProvider1<UInt, UInt> = Message.resource1("kova.uint.gte"),
) = constrain(message.id) {
    satisfies(it.input >= value, message(it, value))
}

fun UIntValidator.lt(
    value: UInt,
    message: MessageProvider1<UInt, UInt> = Message.resource1("kova.uint.lt"),
) = constrain(message.id) {
    satisfies(it.input < value, message(it, value))
}

fun UIntValidator.lte(
    value: UInt,
    message: MessageProvider1<UInt, UInt> = Message.resource1("kova.uint.lte"),
) = constrain(message.id) {
    satisfies(it.input <= value, message(it, value))
}
