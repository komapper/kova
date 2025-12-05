package org.komapper.extension.validator

typealias UShortValidator = IdentityValidator<UShort>

fun UShortValidator.min(
    value: UShort,
    message: MessageProvider1<UShort, UShort> = Message.resource1("kova.ushort.min"),
) = constrain(message.id) {
    satisfies(it.input >= value, message(it, value))
}

fun UShortValidator.max(
    value: UShort,
    message: MessageProvider1<UShort, UShort> = Message.resource1("kova.ushort.max"),
) = constrain(message.id) {
    satisfies(it.input <= value, message(it, value))
}

fun UShortValidator.gt(
    value: UShort,
    message: MessageProvider1<UShort, UShort> = Message.resource1("kova.ushort.gt"),
) = constrain(message.id) {
    satisfies(it.input > value, message(it, value))
}

fun UShortValidator.gte(
    value: UShort,
    message: MessageProvider1<UShort, UShort> = Message.resource1("kova.ushort.gte"),
) = constrain(message.id) {
    satisfies(it.input >= value, message(it, value))
}

fun UShortValidator.lt(
    value: UShort,
    message: MessageProvider1<UShort, UShort> = Message.resource1("kova.ushort.lt"),
) = constrain(message.id) {
    satisfies(it.input < value, message(it, value))
}

fun UShortValidator.lte(
    value: UShort,
    message: MessageProvider1<UShort, UShort> = Message.resource1("kova.ushort.lte"),
) = constrain(message.id) {
    satisfies(it.input <= value, message(it, value))
}
