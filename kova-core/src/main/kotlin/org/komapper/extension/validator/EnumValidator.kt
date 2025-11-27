package org.komapper.extension.validator

open class EnumValidator<E : Enum<E>> internal constructor(
    // TODO
    private val constraint: Constraint<E> = Constraint("kova.enum") { ConstraintResult.Satisfied },
) : Validator<E, E> {
    override fun execute(
        context: ValidationContext,
        input: E,
    ): ValidationResult<E> = CoreValidator(constraint).execute(context, input)

    fun constraint(
        key: String,
        check: ConstraintScope.(ConstraintContext<E>) -> ConstraintResult,
    ): EnumValidator<E> {
        val before = this
        return object : EnumValidator<E>(
            Constraint(key, check),
        ) {
            override fun execute(
                context: ValidationContext,
                input: E,
            ): ValidationResult<E> =
                chain(before, context, input) { context, input ->
                    super.execute(context, input)
                }
        }
    }

    fun contains(
        values: Set<E>,
        message: (ConstraintContext<E>, Set<E>) -> Message = Message.resource1(),
    ): EnumValidator<E> =
        constraint("kova.enum.contains") {
            satisfies(values.contains(it.input), message(it, values))
        }
}
