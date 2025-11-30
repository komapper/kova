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
}

fun <T> NumberValidator(
    prev: Validator<T, T> = EmptyValidator(),
    constraint: Constraint<T> = Constraint.satisfied(),
): NumberValidator<T> where T : Number, T : Comparable<T> = NumberValidatorImpl(prev, constraint)

private class NumberValidatorImpl<T>(
    private val prev: Validator<T, T>,
    constraint: Constraint<T>,
) : NumberValidator<T>
    where T : Number, T : Comparable<T> {
    private val next: ConstraintValidator<T> = ConstraintValidator(constraint)

    override fun execute(
        context: ValidationContext,
        input: T,
    ): ValidationResult<T> = prev.chain(next).execute(context, input)

    override fun constrain(
        id: String,
        check: ConstraintScope.(ConstraintContext<T>) -> ConstraintResult,
    ): NumberValidator<T> = NumberValidatorImpl(prev = this, constraint = Constraint(id, check))

    override fun min(
        value: T,
        message: (ConstraintContext<T>, T) -> Message,
    ): NumberValidator<T> = constrain("kova.number.min", Constraints.min(value, message))

    override fun max(
        value: T,
        message: (ConstraintContext<T>, T) -> Message,
    ): NumberValidator<T> = constrain("kova.number.max", Constraints.max(value, message))
}
