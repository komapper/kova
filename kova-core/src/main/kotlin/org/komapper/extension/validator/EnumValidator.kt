package org.komapper.extension.validator

class EnumValidator<E : Enum<E>> internal constructor(
    private val prev: Validator<E, E> = EmptyValidator(),
    constraint: Constraint<E> = Constraint.satisfied(),
) : Validator<E, E>,
    Constrainable<E, EnumValidator<E>> {
    private val next: ConstraintValidator<E> = ConstraintValidator(constraint)

    override fun execute(
        context: ValidationContext,
        input: E,
    ): ValidationResult<E> = prev.chain(next).execute(context, input)

    override fun constrain(
        key: String,
        check: ConstraintScope.(ConstraintContext<E>) -> ConstraintResult,
    ): EnumValidator<E> = EnumValidator(prev = this, constraint = Constraint(key, check))

    fun contains(
        values: Set<E>,
        message: (ConstraintContext<E>, Set<E>) -> Message = Message.resource1(),
    ): EnumValidator<E> =
        constrain("kova.enum.contains") {
            satisfies(values.contains(it.input), message(it, values))
        }
}
