package org.komapper.extension.validator

class NumberValidator<T> internal constructor(
    private val prev: Validator<T, T> = EmptyValidator(),
    constraint: Constraint<T> = Constraint.satisfied(),
) : Validator<T, T>,
    Constrainable<T, NumberValidator<T>>
    where T : Number, T : Comparable<T> {
    private val next: ConstraintValidator<T> = ConstraintValidator(constraint)

    override fun execute(
        context: ValidationContext,
        input: T,
    ): ValidationResult<T> = prev.chain(next).execute(context, input)

    override fun constrain(
        key: String,
        check: ConstraintScope.(ConstraintContext<T>) -> ConstraintResult,
    ): NumberValidator<T> = NumberValidator(prev = this, constraint = Constraint(key, check))

    fun min(
        value: T,
        message: (ConstraintContext<T>, T) -> Message = Message.resource1(),
    ): NumberValidator<T> = constrain("kova.number.min", Constraints.min(value, message))

    fun max(
        value: T,
        message: (ConstraintContext<T>, T) -> Message = Message.resource1(),
    ): NumberValidator<T> = constrain("kova.number.max", Constraints.max(value, message))
}
