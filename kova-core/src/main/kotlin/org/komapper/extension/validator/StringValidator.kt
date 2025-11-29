package org.komapper.extension.validator

import kotlin.reflect.KClass

interface StringValidator :
    Validator<String, String>,
    Constrainable<String, StringValidator>,
    Modifiable<String, StringValidator> {
    fun min(
        length: Int,
        message: (ConstraintContext<String>, Int) -> Message = Message.resource1(),
    ): StringValidator

    fun max(
        length: Int,
        message: (ConstraintContext<String>, Int) -> Message = Message.resource1(),
    ): StringValidator

    fun notBlank(message: (ConstraintContext<String>) -> Message = Message.resource0()): StringValidator

    fun notEmpty(message: (ConstraintContext<String>) -> Message = Message.resource0()): StringValidator

    fun length(
        length: Int,
        message: (ConstraintContext<String>, Int) -> Message = Message.resource1(),
    ): StringValidator

    fun startsWith(
        prefix: CharSequence,
        message: (ConstraintContext<String>, CharSequence) -> Message = Message.resource1(),
    ): StringValidator

    fun endsWith(
        suffix: CharSequence,
        message: (ConstraintContext<String>, CharSequence) -> Message = Message.resource1(),
    ): StringValidator

    fun contains(
        infix: CharSequence,
        message: (ConstraintContext<String>, CharSequence) -> Message = Message.resource1(),
    ): StringValidator

    fun matches(
        pattern: Regex,
        message: (ConstraintContext<String>, Regex) -> Message = Message.resource1(),
    ): StringValidator

    fun email(message: (ConstraintContext<String>) -> Message = Message.resource0()): StringValidator

    fun isInt(message: (ConstraintContext<String>) -> Message = Message.resource0()): StringValidator

    fun isLong(message: (ConstraintContext<String>) -> Message = Message.resource0()): StringValidator

    fun isShort(message: (ConstraintContext<String>) -> Message = Message.resource0()): StringValidator

    fun isByte(message: (ConstraintContext<String>) -> Message = Message.resource0()): StringValidator

    fun isDouble(message: (ConstraintContext<String>) -> Message = Message.resource0()): StringValidator

    fun isFloat(message: (ConstraintContext<String>) -> Message = Message.resource0()): StringValidator

    fun isBigDecimal(message: (ConstraintContext<String>) -> Message = Message.resource0()): StringValidator

    fun isBigInteger(message: (ConstraintContext<String>) -> Message = Message.resource0()): StringValidator

    fun isBoolean(message: (ConstraintContext<String>) -> Message = Message.resource0()): StringValidator

    fun <E : Enum<E>> isEnum(
        klass: KClass<E>,
        message: (ConstraintContext<String>, List<String>) -> Message = Message.resource1(),
    ): StringValidator

    fun uppercase(message: (ConstraintContext<String>) -> Message = Message.resource0()): StringValidator

    fun lowercase(message: (ConstraintContext<String>) -> Message = Message.resource0()): StringValidator

    fun trim(): StringValidator

    fun toUpperCase(): StringValidator

    fun toLowerCase(): StringValidator
}

fun StringValidator(
    prev: Validator<String, String> = EmptyValidator(),
    transform: (String) -> String = { it },
    constraint: Constraint<String> = Constraint.satisfied(),
): StringValidator = StringValidatorImpl(prev, transform, constraint)

private class StringValidatorImpl(
    private val prev: Validator<String, String>,
    private val transform: (String) -> String,
    constraint: Constraint<String>,
) : StringValidator {
    private val next: ConstraintValidator<String> = ConstraintValidator(constraint)

    override fun execute(
        context: ValidationContext,
        input: String,
    ): ValidationResult<String> = prev.map(transform).chain(next).execute(context, input)

    override fun constrain(
        key: String,
        check: ConstraintScope.(ConstraintContext<String>) -> ConstraintResult,
    ): StringValidator = StringValidatorImpl(prev = this, transform = { it }, constraint = Constraint(key, check))

    override fun modify(transform: (String) -> String): StringValidator =
        StringValidatorImpl(prev = this, transform = transform, Constraint.satisfied())

    override fun min(
        length: Int,
        message: (ConstraintContext<String>, Int) -> Message,
    ): StringValidator =
        constrain("kova.string.min") {
            satisfies(it.input.length >= length, message(it, length))
        }

    override fun max(
        length: Int,
        message: (ConstraintContext<String>, Int) -> Message,
    ): StringValidator =
        constrain("kova.string.max") {
            satisfies(it.input.length <= length, message(it, length))
        }

    override fun notBlank(message: (ConstraintContext<String>) -> Message): StringValidator =
        constrain("kova.string.notBlank") {
            satisfies(it.input.isNotBlank(), message(it))
        }

    override fun notEmpty(message: (ConstraintContext<String>) -> Message): StringValidator =
        constrain("kova.string.notEmpty") {
            satisfies(it.input.isNotEmpty(), message(it))
        }

    override fun length(
        length: Int,
        message: (ConstraintContext<String>, Int) -> Message,
    ): StringValidator =
        constrain("kova.string.length") {
            satisfies(it.input.length == length, message(it, length))
        }

    override fun startsWith(
        prefix: CharSequence,
        message: (ConstraintContext<String>, CharSequence) -> Message,
    ): StringValidator =
        constrain("kova.string.startsWith") {
            satisfies(it.input.startsWith(prefix), message(it, prefix))
        }

    override fun endsWith(
        suffix: CharSequence,
        message: (ConstraintContext<String>, CharSequence) -> Message,
    ): StringValidator =
        constrain("kova.string.endsWith") {
            satisfies(it.input.endsWith(suffix), message(it, suffix))
        }

    override fun contains(
        infix: CharSequence,
        message: (ConstraintContext<String>, CharSequence) -> Message,
    ) = constrain("kova.string.contains") {
        satisfies(it.input.contains(infix), message(it, infix))
    }

    override fun matches(
        pattern: Regex,
        message: (ConstraintContext<String>, Regex) -> Message,
    ): StringValidator =
        constrain("kova.string.matches") {
            satisfies(pattern.matches(it.input), message(it, pattern))
        }

    override fun email(message: (ConstraintContext<String>) -> Message): StringValidator =
        constrain("kova.string.email") {
            val emailPattern =
                Regex(
                    "^(?!\\.)(?!.*\\.\\.)([a-z0-9_'+\\-\\.]*)[a-z0-9_+-]@([a-z0-9][a-z0-9\\-]*\\.)+[a-z]{2,}\$",
                    RegexOption.IGNORE_CASE,
                )
            satisfies(emailPattern.matches(it.input), message(it))
        }

    override fun isInt(message: (ConstraintContext<String>) -> Message): StringValidator =
        constrain("kova.string.isInt") {
            satisfies(it.input.toIntOrNull() != null, message(it))
        }

    override fun isLong(message: (ConstraintContext<String>) -> Message): StringValidator =
        constrain("kova.string.isLong") {
            satisfies(it.input.toLongOrNull() != null, message(it))
        }

    override fun isShort(message: (ConstraintContext<String>) -> Message): StringValidator =
        constrain("kova.string.isShort") {
            satisfies(it.input.toShortOrNull() != null, message(it))
        }

    override fun isByte(message: (ConstraintContext<String>) -> Message): StringValidator =
        constrain("kova.string.isByte") {
            satisfies(it.input.toByteOrNull() != null, message(it))
        }

    override fun isDouble(message: (ConstraintContext<String>) -> Message): StringValidator =
        constrain("kova.string.isDouble") {
            satisfies(it.input.toDoubleOrNull() != null, message(it))
        }

    override fun isFloat(message: (ConstraintContext<String>) -> Message): StringValidator =
        constrain("kova.string.isFloat") {
            satisfies(it.input.toFloatOrNull() != null, message(it))
        }

    override fun isBigDecimal(message: (ConstraintContext<String>) -> Message): StringValidator =
        constrain("kova.string.isBigDecimal") {
            satisfies(it.input.toBigDecimalOrNull() != null, message(it))
        }

    override fun isBigInteger(message: (ConstraintContext<String>) -> Message): StringValidator =
        constrain("kova.string.isBigInteger") {
            satisfies(it.input.toBigIntegerOrNull() != null, message(it))
        }

    override fun isBoolean(message: (ConstraintContext<String>) -> Message): StringValidator =
        constrain("kova.string.isBoolean") {
            satisfies(it.input.toBooleanStrictOrNull() != null, message(it))
        }

    override fun <E : Enum<E>> isEnum(
        klass: KClass<E>,
        message: (ConstraintContext<String>, List<String>) -> Message,
    ): StringValidator {
        val enumValues = klass.java.enumConstants
        val validNames = enumValues.map { it.name }
        return this.constrain("kova.string.isEnum") { ctx ->
            satisfies(validNames.contains(ctx.input), message(ctx, validNames))
        }
    }

    override fun uppercase(message: (ConstraintContext<String>) -> Message): StringValidator =
        constrain("kova.string.uppercase") {
            satisfies(it.input == it.input.uppercase(), message(it))
        }

    override fun lowercase(message: (ConstraintContext<String>) -> Message): StringValidator =
        constrain("kova.string.lowercase") {
            satisfies(it.input == it.input.lowercase(), message(it))
        }

    override fun trim() = modify { it.trim() }

    override fun toUpperCase() = modify { it.uppercase() }

    override fun toLowerCase() = modify { it.lowercase() }
}

fun StringValidator.toInt(): Validator<String, Int> = isInt().map { it.toInt() }

fun StringValidator.toLong(): Validator<String, Long> = isLong().map { it.toLong() }

fun StringValidator.toShort(): Validator<String, Short> = isShort().map { it.toShort() }

fun StringValidator.toByte(): Validator<String, Byte> = isByte().map { it.toByte() }

fun StringValidator.toDouble(): Validator<String, Double> = isDouble().map { it.toDouble() }

fun StringValidator.toFloat(): Validator<String, Float> = isFloat().map { it.toFloat() }

fun StringValidator.toBigDecimal(): Validator<String, java.math.BigDecimal> = isBigDecimal().map { it.toBigDecimal() }

fun StringValidator.toBigInteger(): Validator<String, java.math.BigInteger> = isBigInteger().map { it.toBigInteger() }

fun StringValidator.toBoolean(): Validator<String, Boolean> = isBoolean().map { it.toBoolean() }

inline fun <reified E : Enum<E>> StringValidator.isEnum(): StringValidator {
    val enumValues = enumValues<E>()
    val validNames = enumValues.map { it.name }
    return this.constrain("kova.string.isEnum") { ctx ->
        satisfies(validNames.contains(ctx.input), Message.Resource(ctx.key, ctx.input, validNames))
    }
}

inline fun <reified E : Enum<E>> StringValidator.toEnum(): Validator<String, E> = isEnum<E>().map { enumValueOf<E>(it) }
