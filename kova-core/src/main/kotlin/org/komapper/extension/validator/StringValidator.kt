package org.komapper.extension.validator

import kotlin.reflect.KClass

typealias StringValidator = IdentityValidator<String>

fun StringValidator.min(
    length: Int,
    message: MessageProvider1<String, Int> = Message.resource1("kova.string.min"),
) = constrain(message.id) {
    satisfies(it.input.length >= length, message(it, length))
}

fun StringValidator.max(
    length: Int,
    message: MessageProvider1<String, Int> = Message.resource1("kova.string.max"),
) = constrain(message.id) {
    satisfies(it.input.length <= length, message(it, length))
}

fun StringValidator.notBlank(message: MessageProvider0<String> = Message.resource0("kova.string.notBlank")) =
    constrain(message.id) {
        satisfies(it.input.isNotBlank(), message(it))
    }

fun StringValidator.notEmpty(message: MessageProvider0<String> = Message.resource0("kova.string.notEmpty")) =
    constrain(message.id) {
        satisfies(it.input.isNotEmpty(), message(it))
    }

fun StringValidator.length(
    length: Int,
    message: MessageProvider1<String, Int> = Message.resource1("kova.string.length"),
) = constrain(message.id) {
    satisfies(it.input.length == length, message(it, length))
}

fun StringValidator.startsWith(
    prefix: CharSequence,
    message: MessageProvider1<String, CharSequence> = Message.resource1("kova.string.startsWith"),
) = constrain(message.id) {
    satisfies(it.input.startsWith(prefix), message(it, prefix))
}

fun StringValidator.endsWith(
    suffix: CharSequence,
    message: MessageProvider1<String, CharSequence> = Message.resource1("kova.string.endsWith"),
) = constrain(message.id) {
    satisfies(it.input.endsWith(suffix), message(it, suffix))
}

fun StringValidator.contains(
    infix: CharSequence,
    message: MessageProvider1<String, CharSequence> = Message.resource1("kova.string.contains"),
) = constrain(message.id) {
    satisfies(it.input.contains(infix), message(it, infix))
}

fun StringValidator.matches(
    pattern: Regex,
    message: MessageProvider1<String, Regex> = Message.resource1("kova.string.matches"),
) = constrain(message.id) {
    satisfies(pattern.matches(it.input), message(it, pattern))
}

fun StringValidator.email(message: MessageProvider0<String> = Message.resource0("kova.string.email")) =
    constrain(message.id) {
        val emailPattern =
            Regex(
                "^(?!\\.)(?!.*\\.\\.)([a-z0-9_'+\\-\\.]*)[a-z0-9_+-]@([a-z0-9][a-z0-9\\-]*\\.)+[a-z]{2,}\$",
                RegexOption.IGNORE_CASE,
            )
        satisfies(emailPattern.matches(it.input), message(it))
    }

fun StringValidator.isInt(message: MessageProvider0<String> = Message.resource0("kova.string.isInt")) =
    constrain(message.id) {
        satisfies(it.input.toIntOrNull() != null, message(it))
    }

fun StringValidator.isLong(message: MessageProvider0<String> = Message.resource0("kova.string.isLong")) =
    constrain(message.id) {
        satisfies(it.input.toLongOrNull() != null, message(it))
    }

fun StringValidator.isShort(message: MessageProvider0<String> = Message.resource0("kova.string.isShort")) =
    constrain(message.id) {
        satisfies(it.input.toShortOrNull() != null, message(it))
    }

fun StringValidator.isByte(message: MessageProvider0<String> = Message.resource0("kova.string.isByte")) =
    constrain(message.id) {
        satisfies(it.input.toByteOrNull() != null, message(it))
    }

fun StringValidator.isDouble(message: MessageProvider0<String> = Message.resource0("kova.string.isDouble")) =
    constrain(message.id) {
        satisfies(it.input.toDoubleOrNull() != null, message(it))
    }

fun StringValidator.isFloat(message: MessageProvider0<String> = Message.resource0("kova.string.isFloat")) =
    constrain(message.id) {
        satisfies(it.input.toFloatOrNull() != null, message(it))
    }

fun StringValidator.isBigDecimal(message: MessageProvider0<String> = Message.resource0("kova.string.isBigDecimal")) =
    constrain(message.id) {
        satisfies(it.input.toBigDecimalOrNull() != null, message(it))
    }

fun StringValidator.isBigInteger(message: MessageProvider0<String> = Message.resource0("kova.string.isBigInteger")) =
    constrain(message.id) {
        satisfies(it.input.toBigIntegerOrNull() != null, message(it))
    }

fun StringValidator.isBoolean(message: MessageProvider0<String> = Message.resource0("kova.string.isBoolean")) =
    constrain(message.id) {
        satisfies(it.input.toBooleanStrictOrNull() != null, message(it))
    }

fun <E : Enum<E>> StringValidator.isEnum(
    klass: KClass<E>,
    message: MessageProvider1<String, List<String>> = Message.resource1("kova.string.isEnum"),
): StringValidator {
    val enumValues = klass.java.enumConstants
    val validNames = enumValues.map { it.name }
    return this.constrain(message.id) { ctx ->
        satisfies(validNames.contains(ctx.input), message(ctx, validNames))
    }
}

inline fun <reified E : Enum<E>> StringValidator.isEnum(): StringValidator {
    val enumValues = enumValues<E>()
    val validNames = enumValues.map { it.name }
    return this.constrain("kova.string.isEnum") { ctx ->
        satisfies(validNames.contains(ctx.input), Message.Resource(ctx.constraintId, ctx.input, validNames))
    }
}

inline fun <reified E : Enum<E>> StringValidator.toEnum(): Validator<String, E> = isEnum<E>().map { enumValueOf<E>(it) }

fun StringValidator.uppercase(message: MessageProvider0<String> = Message.resource0("kova.string.uppercase")) =
    constrain(message.id) {
        satisfies(it.input == it.input.uppercase(), message(it))
    }

fun StringValidator.lowercase(message: MessageProvider0<String> = Message.resource0("kova.string.lowercase")) =
    constrain(message.id) {
        satisfies(it.input == it.input.lowercase(), message(it))
    }

fun StringValidator.trim() = map { it.trim() }

fun StringValidator.toUpperCase() = map { it.uppercase() }

fun StringValidator.toLowerCase() = map { it.lowercase() }

fun StringValidator.toInt(): Validator<String, Int> = isInt().map { it.toInt() }

fun StringValidator.toLong(): Validator<String, Long> = isLong().map { it.toLong() }

fun StringValidator.toShort(): Validator<String, Short> = isShort().map { it.toShort() }

fun StringValidator.toByte(): Validator<String, Byte> = isByte().map { it.toByte() }

fun StringValidator.toDouble(): Validator<String, Double> = isDouble().map { it.toDouble() }

fun StringValidator.toFloat(): Validator<String, Float> = isFloat().map { it.toFloat() }

fun StringValidator.toBigDecimal(): Validator<String, java.math.BigDecimal> = isBigDecimal().map { it.toBigDecimal() }

fun StringValidator.toBigInteger(): Validator<String, java.math.BigInteger> = isBigInteger().map { it.toBigInteger() }

fun StringValidator.toBoolean(): Validator<String, Boolean> = isBoolean().map { it.toBoolean() }
