package org.komapper.extension.validator

typealias NumberValidator = Validator<Number, Number>

fun <T> Validator<T, T>.min(
    value: T,
    message: MessageProvider1<T, T> = Message.resource1("kova.number.min"),
): Validator<T, T> where T : Number, T : Comparable<T> = constrain(message.id, Constraints.min(value, message))

fun <T> Validator<T, T>.max(
    value: T,
    message: MessageProvider1<T, T> = Message.resource1("kova.number.max"),
): Validator<T, T> where T : Number, T : Comparable<T> = constrain(message.id, Constraints.max(value, message))

fun <T> Validator<T, T>.gt(
    value: T,
    message: MessageProvider1<T, T> = Message.resource1("kova.number.gt"),
): Validator<T, T> where T : Number, T : Comparable<T> = constrain(message.id, Constraints.gt(value, message))

fun <T> Validator<T, T>.gte(
    value: T,
    message: MessageProvider1<T, T> = Message.resource1("kova.number.gte"),
): Validator<T, T> where T : Number, T : Comparable<T> = constrain(message.id, Constraints.gte(value, message))

fun <T> Validator<T, T>.lt(
    value: T,
    message: MessageProvider1<T, T> = Message.resource1("kova.number.lt"),
): Validator<T, T> where T : Number, T : Comparable<T> = constrain(message.id, Constraints.lt(value, message))

fun <T> Validator<T, T>.lte(
    value: T,
    message: MessageProvider1<T, T> = Message.resource1("kova.number.lte"),
): Validator<T, T> where T : Number, T : Comparable<T> = constrain(message.id, Constraints.lte(value, message))

fun <T> Validator<T, T>.positive(
    message: MessageProvider0<T> = Message.resource0("kova.number.positive"),
): Validator<T, T> where T : Number, T : Comparable<T> =
    constrain(message.id) {
        satisfies(it.input.toDouble() > 0.0, message(it))
    }

fun <T> Validator<T, T>.negative(
    message: MessageProvider0<T> = Message.resource0("kova.number.negative"),
): Validator<T, T> where T : Number, T : Comparable<T> =
    constrain(message.id) {
        satisfies(it.input.toDouble() < 0.0, message(it))
    }

fun <T> Validator<T, T>.notPositive(
    message: MessageProvider0<T> = Message.resource0("kova.number.notPositive"),
): Validator<T, T> where T : Number, T : Comparable<T> =
    constrain(message.id) {
        satisfies(it.input.toDouble() <= 0.0, message(it))
    }

fun <T> Validator<T, T>.notNegative(
    message: MessageProvider0<T> = Message.resource0("kova.number.notNegative"),
): Validator<T, T> where T : Number, T : Comparable<T> =
    constrain(message.id) {
        satisfies(it.input.toDouble() >= 0.0, message(it))
    }
