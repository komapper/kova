package org.komapper.extension.validator

class LiteralValidator<T : Any> internal constructor(
    private val prev: Validator<T, T> = EmptyValidator(),
    constraint: Constraint<T> = Constraint.satisfied(),
) : Validator<T, T>,
    Constrainable<T, LiteralValidator<T>> {
    private val next: ConstraintValidator<T> = ConstraintValidator(constraint)

    override fun execute(
        context: ValidationContext,
        input: T,
    ): ValidationResult<T> = prev.chain(next).execute(context, input)

    override fun constrain(
        key: String,
        check: ConstraintScope.(ConstraintContext<T>) -> ConstraintResult,
    ): LiteralValidator<T> = LiteralValidator(prev = this, constraint = Constraint(key, check))

    fun single(
        value: T,
        message: (ConstraintContext<T>, T) -> Message = Message.resource1(),
    ): LiteralValidator<T> =
        constrain("kova.literal.single") {
            satisfies(it.input == value, message(it, value))
        }

    fun list(
        values: List<T>,
        message: (ConstraintContext<T>, List<T>) -> Message = Message.resource1(),
    ): LiteralValidator<T> =
        constrain("kova.literal.list") {
            satisfies(it.input in values, message(it, values))
        }
}
