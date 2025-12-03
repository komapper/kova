package org.komapper.extension.validator

interface NumberValidator<T> :
    Validator<T, T>,
    Constrainable<T, NumberValidator<T>>
    where T : Number, T : Comparable<T> {
    fun min(
        value: T,
        message: MessageProvider1<T, T> = Message.resource1("kova.number.min"),
    ): NumberValidator<T>

    fun max(
        value: T,
        message: MessageProvider1<T, T> = Message.resource1("kova.number.max"),
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
    ): NumberValidator<T> = constrain(message.key, Constraints.min(value, message))

    override fun max(
        value: T,
        message: MessageProvider1<T, T>,
    ): NumberValidator<T> = constrain(message.key, Constraints.max(value, message))

    override fun positive(message: MessageProvider0<T>): NumberValidator<T> =
        constrain(message.key) {
            satisfies(it.input.toDouble() > 0.0, message(it))
        }

    override fun negative(message: MessageProvider0<T>): NumberValidator<T> =
        constrain(message.key) {
            satisfies(it.input.toDouble() < 0.0, message(it))
        }

    override fun notPositive(message: MessageProvider0<T>): NumberValidator<T> =
        constrain(message.key) {
            satisfies(it.input.toDouble() <= 0.0, message(it))
        }

    override fun notNegative(message: MessageProvider0<T>): NumberValidator<T> =
        constrain(message.key) {
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
