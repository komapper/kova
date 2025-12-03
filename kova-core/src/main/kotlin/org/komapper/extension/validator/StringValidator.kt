package org.komapper.extension.validator

import kotlin.reflect.KClass

/**
 * Validator for String values with string-specific validation constraints.
 *
 * Provides methods for validating string length, content, format, and patterns.
 * All validators are immutable and can be composed using operators.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string()
 *     .min(3)
 *     .max(20)
 *     .notBlank()
 *     .matches(Regex("[a-zA-Z0-9]+"))
 * ```
 */
interface StringValidator :
    Validator<String, String>,
    Constrainable<String, StringValidator>,
    Modifiable<String, StringValidator> {
    /**
     * Validates that the string has at least [length] characters.
     *
     * @param length Minimum number of characters required
     * @param message Custom error message provider
     */
    fun min(
        length: Int,
        message: MessageProvider1<String, Int> = Message.resource1("kova.string.min"),
    ): StringValidator

    /**
     * Validates that the string has at most [length] characters.
     *
     * @param length Maximum number of characters allowed
     * @param message Custom error message provider
     */
    fun max(
        length: Int,
        message: MessageProvider1<String, Int> = Message.resource1("kova.string.max"),
    ): StringValidator

    /**
     * Validates that the string is not blank (not empty and contains non-whitespace characters).
     *
     * @param message Custom error message provider
     */
    fun notBlank(message: MessageProvider0<String> = Message.resource0("kova.string.notBlank")): StringValidator

    /**
     * Validates that the string is not empty (length > 0).
     *
     * @param message Custom error message provider
     */
    fun notEmpty(message: MessageProvider0<String> = Message.resource0("kova.string.notEmpty")): StringValidator

    /**
     * Validates that the string has exactly [length] characters.
     *
     * @param length Exact number of characters required
     * @param message Custom error message provider
     */
    fun length(
        length: Int,
        message: MessageProvider1<String, Int> = Message.resource1("kova.string.length"),
    ): StringValidator

    /**
     * Validates that the string starts with the specified prefix.
     *
     * @param prefix The required prefix
     * @param message Custom error message provider
     */
    fun startsWith(
        prefix: CharSequence,
        message: MessageProvider1<String, CharSequence> = Message.resource1("kova.string.startsWith"),
    ): StringValidator

    /**
     * Validates that the string ends with the specified suffix.
     *
     * @param suffix The required suffix
     * @param message Custom error message provider
     */
    fun endsWith(
        suffix: CharSequence,
        message: MessageProvider1<String, CharSequence> = Message.resource1("kova.string.endsWith"),
    ): StringValidator

    /**
     * Validates that the string contains the specified substring.
     *
     * @param infix The required substring
     * @param message Custom error message provider
     */
    fun contains(
        infix: CharSequence,
        message: MessageProvider1<String, CharSequence> = Message.resource1("kova.string.contains"),
    ): StringValidator

    /**
     * Validates that the string matches the specified regular expression pattern.
     *
     * @param pattern The regex pattern to match
     * @param message Custom error message provider
     */
    fun matches(
        pattern: Regex,
        message: MessageProvider1<String, Regex> = Message.resource1("kova.string.matches"),
    ): StringValidator

    /**
     * Validates that the string is a valid email address format.
     *
     * @param message Custom error message provider
     */
    fun email(message: MessageProvider0<String> = Message.resource0("kova.string.email")): StringValidator

    fun isInt(message: MessageProvider0<String> = Message.resource0("kova.string.isInt")): StringValidator

    fun isLong(message: MessageProvider0<String> = Message.resource0("kova.string.isLong")): StringValidator

    fun isShort(message: MessageProvider0<String> = Message.resource0("kova.string.isShort")): StringValidator

    fun isByte(message: MessageProvider0<String> = Message.resource0("kova.string.isByte")): StringValidator

    fun isDouble(message: MessageProvider0<String> = Message.resource0("kova.string.isDouble")): StringValidator

    fun isFloat(message: MessageProvider0<String> = Message.resource0("kova.string.isFloat")): StringValidator

    fun isBigDecimal(message: MessageProvider0<String> = Message.resource0("kova.string.isBigDecimal")): StringValidator

    fun isBigInteger(message: MessageProvider0<String> = Message.resource0("kova.string.isBigInteger")): StringValidator

    fun isBoolean(message: MessageProvider0<String> = Message.resource0("kova.string.isBoolean")): StringValidator

    fun <E : Enum<E>> isEnum(
        klass: KClass<E>,
        message: MessageProvider1<String, List<String>> = Message.resource1("kova.string.isEnum"),
    ): StringValidator

    fun uppercase(message: MessageProvider0<String> = Message.resource0("kova.string.uppercase")): StringValidator

    fun lowercase(message: MessageProvider0<String> = Message.resource0("kova.string.lowercase")): StringValidator

    fun trim(): StringValidator

    fun toUpperCase(): StringValidator

    fun toLowerCase(): StringValidator

    fun toInt(): Validator<String, Int>

    fun toLong(): Validator<String, Long>

    fun toShort(): Validator<String, Short>

    fun toByte(): Validator<String, Byte>

    fun toDouble(): Validator<String, Double>

    fun toFloat(): Validator<String, Float>

    fun toBigDecimal(): Validator<String, java.math.BigDecimal>

    fun toBigInteger(): Validator<String, java.math.BigInteger>

    fun toBoolean(): Validator<String, Boolean>

    operator fun plus(other: Validator<String, String>): StringValidator

    infix fun and(other: Validator<String, String>): StringValidator

    infix fun or(other: Validator<String, String>): StringValidator

    fun chain(other: Validator<String, String>): StringValidator
}

inline fun <reified E : Enum<E>> StringValidator.isEnum(): StringValidator {
    val enumValues = enumValues<E>()
    val validNames = enumValues.map { it.name }
    return this.constrain("kova.string.isEnum") { ctx ->
        satisfies(validNames.contains(ctx.input), Message.Resource(ctx.constraintId, ctx.input, validNames))
    }
}

inline fun <reified E : Enum<E>> StringValidator.toEnum(): Validator<String, E> = isEnum<E>().map { enumValueOf<E>(it) }

fun StringValidator(
    name: String = "empty",
    prev: Validator<String, String> = EmptyValidator(),
    transform: (String) -> String = { it },
    constraint: Constraint<String> = Constraint.satisfied(),
): StringValidator = StringValidatorImpl(name, prev, transform, constraint)

private class StringValidatorImpl(
    private val name: String,
    private val prev: Validator<String, String>,
    private val transform: (String) -> String = { it },
    private val constraint: Constraint<String> = Constraint.satisfied(),
) : StringValidator {
    private val next: ConstraintValidator<String> = ConstraintValidator(constraint)

    override fun execute(
        input: String,
        context: ValidationContext,
    ): ValidationResult<String> {
        val context = context.addLog(toString())
        return prev.map(transform).chain(next).execute(input, context)
    }

    override fun constrain(
        id: String,
        check: ConstraintScope.(ConstraintContext<String>) -> ConstraintResult,
    ): StringValidator = StringValidatorImpl(name = id, prev = this, constraint = Constraint(id, check))

    override fun modify(
        name: String,
        transform: (String) -> String,
    ): StringValidator = StringValidatorImpl(name = name, prev = this, transform = transform)

    override fun min(
        length: Int,
        message: MessageProvider1<String, Int>,
    ): StringValidator =
        constrain(message.key) {
            satisfies(it.input.length >= length, message(it, length))
        }

    override fun max(
        length: Int,
        message: MessageProvider1<String, Int>,
    ): StringValidator =
        constrain(message.key) {
            satisfies(it.input.length <= length, message(it, length))
        }

    override fun notBlank(message: MessageProvider0<String>): StringValidator =
        constrain(message.key) {
            satisfies(it.input.isNotBlank(), message(it))
        }

    override fun notEmpty(message: MessageProvider0<String>): StringValidator =
        constrain(message.key) {
            satisfies(it.input.isNotEmpty(), message(it))
        }

    override fun length(
        length: Int,
        message: MessageProvider1<String, Int>,
    ): StringValidator =
        constrain(message.key) {
            satisfies(it.input.length == length, message(it, length))
        }

    override fun startsWith(
        prefix: CharSequence,
        message: MessageProvider1<String, CharSequence>,
    ): StringValidator =
        constrain(message.key) {
            satisfies(it.input.startsWith(prefix), message(it, prefix))
        }

    override fun endsWith(
        suffix: CharSequence,
        message: MessageProvider1<String, CharSequence>,
    ): StringValidator =
        constrain(message.key) {
            satisfies(it.input.endsWith(suffix), message(it, suffix))
        }

    override fun contains(
        infix: CharSequence,
        message: MessageProvider1<String, CharSequence>,
    ) = constrain(message.key) {
        satisfies(it.input.contains(infix), message(it, infix))
    }

    override fun matches(
        pattern: Regex,
        message: MessageProvider1<String, Regex>,
    ): StringValidator =
        constrain(message.key) {
            satisfies(pattern.matches(it.input), message(it, pattern))
        }

    override fun email(message: MessageProvider0<String>): StringValidator =
        constrain(message.key) {
            val emailPattern =
                Regex(
                    "^(?!\\.)(?!.*\\.\\.)([a-z0-9_'+\\-\\.]*)[a-z0-9_+-]@([a-z0-9][a-z0-9\\-]*\\.)+[a-z]{2,}\$",
                    RegexOption.IGNORE_CASE,
                )
            satisfies(emailPattern.matches(it.input), message(it))
        }

    override fun isInt(message: MessageProvider0<String>): StringValidator =
        constrain(message.key) {
            satisfies(it.input.toIntOrNull() != null, message(it))
        }

    override fun isLong(message: MessageProvider0<String>): StringValidator =
        constrain(message.key) {
            satisfies(it.input.toLongOrNull() != null, message(it))
        }

    override fun isShort(message: MessageProvider0<String>): StringValidator =
        constrain(message.key) {
            satisfies(it.input.toShortOrNull() != null, message(it))
        }

    override fun isByte(message: MessageProvider0<String>): StringValidator =
        constrain(message.key) {
            satisfies(it.input.toByteOrNull() != null, message(it))
        }

    override fun isDouble(message: MessageProvider0<String>): StringValidator =
        constrain(message.key) {
            satisfies(it.input.toDoubleOrNull() != null, message(it))
        }

    override fun isFloat(message: MessageProvider0<String>): StringValidator =
        constrain(message.key) {
            satisfies(it.input.toFloatOrNull() != null, message(it))
        }

    override fun isBigDecimal(message: MessageProvider0<String>): StringValidator =
        constrain(message.key) {
            satisfies(it.input.toBigDecimalOrNull() != null, message(it))
        }

    override fun isBigInteger(message: MessageProvider0<String>): StringValidator =
        constrain(message.key) {
            satisfies(it.input.toBigIntegerOrNull() != null, message(it))
        }

    override fun isBoolean(message: MessageProvider0<String>): StringValidator =
        constrain(message.key) {
            satisfies(it.input.toBooleanStrictOrNull() != null, message(it))
        }

    override fun <E : Enum<E>> isEnum(
        klass: KClass<E>,
        message: MessageProvider1<String, List<String>>,
    ): StringValidator {
        val enumValues = klass.java.enumConstants
        val validNames = enumValues.map { it.name }
        return this.constrain(message.key) { ctx ->
            satisfies(validNames.contains(ctx.input), message(ctx, validNames))
        }
    }

    override fun uppercase(message: MessageProvider0<String>): StringValidator =
        constrain(message.key) {
            satisfies(it.input == it.input.uppercase(), message(it))
        }

    override fun lowercase(message: MessageProvider0<String>): StringValidator =
        constrain(message.key) {
            satisfies(it.input == it.input.lowercase(), message(it))
        }

    override fun trim() = modify("trim") { it.trim() }

    override fun toUpperCase() = modify("toUpperCase") { it.uppercase() }

    override fun toLowerCase() = modify("toLowerCase") { it.lowercase() }

    override fun toInt(): Validator<String, Int> = isInt().map { it.toInt() }

    override fun toLong(): Validator<String, Long> = isLong().map { it.toLong() }

    override fun toShort(): Validator<String, Short> = isShort().map { it.toShort() }

    override fun toByte(): Validator<String, Byte> = isByte().map { it.toByte() }

    override fun toDouble(): Validator<String, Double> = isDouble().map { it.toDouble() }

    override fun toFloat(): Validator<String, Float> = isFloat().map { it.toFloat() }

    override fun toBigDecimal(): Validator<String, java.math.BigDecimal> = isBigDecimal().map { it.toBigDecimal() }

    override fun toBigInteger(): Validator<String, java.math.BigInteger> = isBigInteger().map { it.toBigInteger() }

    override fun toBoolean(): Validator<String, Boolean> = isBoolean().map { it.toBoolean() }

    override operator fun plus(other: Validator<String, String>): StringValidator = and(other)

    override fun and(other: Validator<String, String>): StringValidator {
        val combined = (this as Validator<String, String>).and(other)
        return StringValidatorImpl("and", prev = combined)
    }

    override fun or(other: Validator<String, String>): StringValidator {
        val combined = (this as Validator<String, String>).or(other)
        return StringValidatorImpl("or", prev = combined)
    }

    override fun chain(other: Validator<String, String>): StringValidator {
        val combined = (this as Validator<String, String>).chain(other)
        return StringValidatorImpl("chain", prev = combined)
    }

    override fun toString(): String = "${StringValidator::class.simpleName}(name=$name)"
}
