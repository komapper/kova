package org.komapper.extension.validator

interface NumberValidator<T> :
    Validator<T, T>,
    Constrainable<T, NumberValidator<T>>
    where T : Number, T : Comparable<T> {
    fun min(
        value: T,
        message: (ConstraintContext<T>, T) -> Message = Message.resource1(),
    ): NumberValidator<T>

    fun max(
        value: T,
        message: (ConstraintContext<T>, T) -> Message = Message.resource1(),
    ): NumberValidator<T>

    fun positive(message: (ConstraintContext<T>) -> Message = Message.resource0()): NumberValidator<T>

    fun negative(message: (ConstraintContext<T>) -> Message = Message.resource0()): NumberValidator<T>

    fun notPositive(message: (ConstraintContext<T>) -> Message = Message.resource0()): NumberValidator<T>

    fun notNegative(message: (ConstraintContext<T>) -> Message = Message.resource0()): NumberValidator<T>

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
        context: ValidationContext,
        input: T,
    ): ValidationResult<T> {
        val context = context.addLog(toString())
        return prev.chain(next).execute(context, input)
    }

    override fun constrain(
        id: String,
        check: ConstraintScope.(ConstraintContext<T>) -> ConstraintResult,
    ): NumberValidator<T> = NumberValidatorImpl(name = id, prev = this, constraint = Constraint(id, check))

    override fun min(
        value: T,
        message: (ConstraintContext<T>, T) -> Message,
    ): NumberValidator<T> = constrain("kova.number.min", Constraints.min(value, message))

    override fun max(
        value: T,
        message: (ConstraintContext<T>, T) -> Message,
    ): NumberValidator<T> = constrain("kova.number.max", Constraints.max(value, message))

    override fun positive(message: (ConstraintContext<T>) -> Message): NumberValidator<T> =
        constrain("kova.number.positive") {
            satisfies(it.input.toDouble() > 0.0, message(it))
        }

    override fun negative(message: (ConstraintContext<T>) -> Message): NumberValidator<T> =
        constrain("kova.number.negative") {
            satisfies(it.input.toDouble() < 0.0, message(it))
        }

    override fun notPositive(message: (ConstraintContext<T>) -> Message): NumberValidator<T> =
        constrain("kova.number.notPositive") {
            satisfies(it.input.toDouble() <= 0.0, message(it))
        }

    override fun notNegative(message: (ConstraintContext<T>) -> Message): NumberValidator<T> =
        constrain("kova.number.notNegative") {
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
