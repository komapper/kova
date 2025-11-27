package org.komapper.extension.validator

class EnumValidator<E : Enum<E>> internal constructor(
    private val prev: EnumValidator<E>? = null,
    constraint: Constraint<E> = Constraint("kova.enum") { ConstraintResult.Satisfied },
) : Validator<E, E> {
    private val next: ConstraintValidator<E> = ConstraintValidator(constraint)

    override fun execute(
        context: ValidationContext,
        input: E,
    ): ValidationResult<E> =
        if (prev == null) {
            next.execute(context, input)
        } else {
            prev.chain(next = next).execute(context, input)
        }

    fun constraint(
        key: String,
        check: ConstraintScope.(ConstraintContext<E>) -> ConstraintResult,
    ): EnumValidator<E> = EnumValidator(prev = this, constraint = Constraint(key, check))

    fun contains(
        values: Set<E>,
        message: (ConstraintContext<E>, Set<E>) -> Message = Message.resource1(),
    ): EnumValidator<E> =
        constraint("kova.enum.contains") {
            satisfies(values.contains(it.input), message(it, values))
        }
}
