package org.komapper.extension.validator

interface LiteralValidator<T : Any> :
    Validator<T, T>,
    Constrainable<T, LiteralValidator<T>> {
    fun single(
        value: T,
        message: (ConstraintContext<T>, T) -> Message = Message.resource1(),
    ): LiteralValidator<T>

    fun list(
        values: List<T>,
        message: (ConstraintContext<T>, List<T>) -> Message = Message.resource1(),
    ): LiteralValidator<T>
}

fun <T : Any> LiteralValidator(
    prev: Validator<T, T> = EmptyValidator(),
    constraint: Constraint<T> = Constraint.satisfied(),
): LiteralValidator<T> = LiteralValidatorImpl(prev, constraint)

private class LiteralValidatorImpl<T : Any>(
    private val prev: Validator<T, T>,
    constraint: Constraint<T>,
) : LiteralValidator<T> {
    private val next: ConstraintValidator<T> = ConstraintValidator(constraint)

    override fun execute(
        context: ValidationContext,
        input: T,
    ): ValidationResult<T> = prev.chain(next).execute(context, input)

    override fun constrain(
        key: String,
        check: ConstraintScope.(ConstraintContext<T>) -> ConstraintResult,
    ): LiteralValidator<T> = LiteralValidatorImpl(prev = this, constraint = Constraint(key, check))

    override fun single(
        value: T,
        message: (ConstraintContext<T>, T) -> Message,
    ): LiteralValidator<T> =
        constrain("kova.literal.single") {
            satisfies(it.input == value, message(it, value))
        }

    override fun list(
        values: List<T>,
        message: (ConstraintContext<T>, List<T>) -> Message,
    ): LiteralValidator<T> =
        constrain("kova.literal.list") {
            satisfies(it.input in values, message(it, values))
        }
}
