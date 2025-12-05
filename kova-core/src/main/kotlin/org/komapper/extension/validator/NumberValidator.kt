package org.komapper.extension.validator

fun <T> IdentityValidator<T>.min(
    value: T,
    message: MessageProvider1<T, T> = Message.resource1("kova.number.min"),
): IdentityValidator<T> where T : Number, T : Comparable<T> = constrain(message.id, Constraints.min(value, message))

fun <T> IdentityValidator<T>.max(
    value: T,
    message: MessageProvider1<T, T> = Message.resource1("kova.number.max"),
): IdentityValidator<T> where T : Number, T : Comparable<T> = constrain(message.id, Constraints.max(value, message))

fun <T> IdentityValidator<T>.gt(
    value: T,
    message: MessageProvider1<T, T> = Message.resource1("kova.number.gt"),
): IdentityValidator<T> where T : Number, T : Comparable<T> = constrain(message.id, Constraints.gt(value, message))

fun <T> IdentityValidator<T>.gte(
    value: T,
    message: MessageProvider1<T, T> = Message.resource1("kova.number.gte"),
): IdentityValidator<T> where T : Number, T : Comparable<T> = constrain(message.id, Constraints.gte(value, message))

fun <T> IdentityValidator<T>.lt(
    value: T,
    message: MessageProvider1<T, T> = Message.resource1("kova.number.lt"),
): IdentityValidator<T> where T : Number, T : Comparable<T> = constrain(message.id, Constraints.lt(value, message))

fun <T> IdentityValidator<T>.lte(
    value: T,
    message: MessageProvider1<T, T> = Message.resource1("kova.number.lte"),
): IdentityValidator<T> where T : Number, T : Comparable<T> = constrain(message.id, Constraints.lte(value, message))

fun <T> IdentityValidator<T>.positive(
    message: MessageProvider0<T> = Message.resource0("kova.number.positive"),
): IdentityValidator<T> where T : Number, T : Comparable<T> =
    constrain(message.id) {
        satisfies(it.input.toDouble() > 0.0, message(it))
    }

fun <T> IdentityValidator<T>.negative(
    message: MessageProvider0<T> = Message.resource0("kova.number.negative"),
): IdentityValidator<T> where T : Number, T : Comparable<T> =
    constrain(message.id) {
        satisfies(it.input.toDouble() < 0.0, message(it))
    }

fun <T> IdentityValidator<T>.notPositive(
    message: MessageProvider0<T> = Message.resource0("kova.number.notPositive"),
): IdentityValidator<T> where T : Number, T : Comparable<T> =
    constrain(message.id) {
        satisfies(it.input.toDouble() <= 0.0, message(it))
    }

fun <T> IdentityValidator<T>.notNegative(
    message: MessageProvider0<T> = Message.resource0("kova.number.notNegative"),
): IdentityValidator<T> where T : Number, T : Comparable<T> =
    constrain(message.id) {
        satisfies(it.input.toDouble() >= 0.0, message(it))
    }
