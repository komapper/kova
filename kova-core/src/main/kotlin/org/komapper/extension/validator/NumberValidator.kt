package org.komapper.extension.validator

/**
 * Validator for numeric values with comparison constraints.
 *
 * Supports validation for all numeric types: Int, Long, Double, Float, Byte, Short,
 * BigDecimal, and BigInteger.
 *
 * Example:
 * ```kotlin
 * val ageValidator = Kova.int().min(0).max(120)
 * val priceValidator = Kova.bigDecimal().gt(BigDecimal.ZERO).max(BigDecimal("999.99"))
 * ```
 *
 * @param T The numeric type being validated
 */
interface NumberValidator<T> :
    Validator<T, T>,
    Constrainable<T, NumberValidator<T>>
    where T : Number, T : Comparable<T> {
    /**
     * Validates that the number is greater than or equal to [value] (inclusive).
     *
     * @param value Minimum allowed value
     * @param message Custom error message provider
     */
    fun min(
        value: T,
        message: MessageProvider1<T, T> = Message.resource1("kova.number.min"),
    ): NumberValidator<T>

    /**
     * Validates that the number is less than or equal to [value] (inclusive).
     *
     * @param value Maximum allowed value
     * @param message Custom error message provider
     */
    fun max(
        value: T,
        message: MessageProvider1<T, T> = Message.resource1("kova.number.max"),
    ): NumberValidator<T>

    /**
     * Validates that the number is strictly greater than [value] (exclusive).
     *
     * @param value The value that the input must be greater than
     * @param message Custom error message provider
     */
    fun gt(
        value: T,
        message: MessageProvider1<T, T> = Message.resource1("kova.number.gt"),
    ): NumberValidator<T>

    /**
     * Validates that the number is greater than or equal to [value] (inclusive).
     *
     * Alias for [min].
     *
     * @param value The minimum value (inclusive)
     * @param message Custom error message provider
     */
    fun gte(
        value: T,
        message: MessageProvider1<T, T> = Message.resource1("kova.number.gte"),
    ): NumberValidator<T>

    /**
     * Validates that the number is strictly less than [value] (exclusive).
     *
     * @param value The value that the input must be less than
     * @param message Custom error message provider
     */
    fun lt(
        value: T,
        message: MessageProvider1<T, T> = Message.resource1("kova.number.lt"),
    ): NumberValidator<T>

    /**
     * Validates that the number is less than or equal to [value] (inclusive).
     *
     * Alias for [max].
     *
     * @param value The maximum value (inclusive)
     * @param message Custom error message provider
     */
    fun lte(
        value: T,
        message: MessageProvider1<T, T> = Message.resource1("kova.number.lte"),
    ): NumberValidator<T>

    fun positive(message: MessageProvider0<T> = Message.resource0("kova.number.positive")): NumberValidator<T>

    fun negative(message: MessageProvider0<T> = Message.resource0("kova.number.negative")): NumberValidator<T>

    fun notPositive(message: MessageProvider0<T> = Message.resource0("kova.number.notPositive")): NumberValidator<T>

    fun notNegative(message: MessageProvider0<T> = Message.resource0("kova.number.notNegative")): NumberValidator<T>

    operator fun plus(other: Validator<T, T>): NumberValidator<T>

    infix fun and(other: Validator<T, T>): NumberValidator<T>

    infix fun or(other: Validator<T, T>): NumberValidator<T>

    fun chain(other: Validator<T, T>): NumberValidator<T>
}

fun <T> NumberValidator(
    name: String = "empty",
    prev: Validator<T, T> = EmptyValidator(),
    constraint: Constraint<T> = Constraint.satisfied(),
): NumberValidator<T> where T : Number, T : Comparable<T> = NumberValidatorImpl(name, prev, constraint)

private class NumberValidatorImpl<T>(
    private val name: String,
    private val prev: Validator<T, T>,
    private val constraint: Constraint<T> = Constraint.satisfied(),
) : NumberValidator<T>
    where T : Number, T : Comparable<T> {
    private val next: ConstraintValidator<T> = ConstraintValidator(constraint)

    override fun execute(
        input: T,
        context: ValidationContext,
    ): ValidationResult<T> {
        val context = context.addLog(toString())
        return prev.chain(next).execute(input, context)
    }

    override fun constrain(
        id: String,
        check: ConstraintScope.(ConstraintContext<T>) -> ConstraintResult,
    ): NumberValidator<T> = NumberValidatorImpl(name = id, prev = this, constraint = Constraint(id, check))

    override fun min(
        value: T,
        message: MessageProvider1<T, T>,
    ): NumberValidator<T> = constrain(message.id, Constraints.min(value, message))

    override fun max(
        value: T,
        message: MessageProvider1<T, T>,
    ): NumberValidator<T> = constrain(message.id, Constraints.max(value, message))

    override fun gt(
        value: T,
        message: MessageProvider1<T, T>,
    ): NumberValidator<T> = constrain(message.id, Constraints.gt(value, message))

    override fun gte(
        value: T,
        message: MessageProvider1<T, T>,
    ): NumberValidator<T> = constrain(message.id, Constraints.gte(value, message))

    override fun lt(
        value: T,
        message: MessageProvider1<T, T>,
    ): NumberValidator<T> = constrain(message.id, Constraints.lt(value, message))

    override fun lte(
        value: T,
        message: MessageProvider1<T, T>,
    ): NumberValidator<T> = constrain(message.id, Constraints.lte(value, message))

    override fun positive(message: MessageProvider0<T>): NumberValidator<T> =
        constrain(message.id) {
            satisfies(it.input.toDouble() > 0.0, message(it))
        }

    override fun negative(message: MessageProvider0<T>): NumberValidator<T> =
        constrain(message.id) {
            satisfies(it.input.toDouble() < 0.0, message(it))
        }

    override fun notPositive(message: MessageProvider0<T>): NumberValidator<T> =
        constrain(message.id) {
            satisfies(it.input.toDouble() <= 0.0, message(it))
        }

    override fun notNegative(message: MessageProvider0<T>): NumberValidator<T> =
        constrain(message.id) {
            satisfies(it.input.toDouble() >= 0.0, message(it))
        }

    override operator fun plus(other: Validator<T, T>): NumberValidator<T> = and(other)

    override fun and(other: Validator<T, T>): NumberValidator<T> {
        val combined = (this as Validator<T, T>).and(other)
        return NumberValidatorImpl("and", prev = combined)
    }

    override fun or(other: Validator<T, T>): NumberValidator<T> {
        val combined = (this as Validator<T, T>).or(other)
        return NumberValidatorImpl("or", prev = combined)
    }

    override fun chain(other: Validator<T, T>): NumberValidator<T> {
        val combined = (this as Validator<T, T>).chain(other)
        return NumberValidatorImpl("chain", prev = combined)
    }

    override fun toString(): String = "${NumberValidator::class.simpleName}(name=$name)"
}
