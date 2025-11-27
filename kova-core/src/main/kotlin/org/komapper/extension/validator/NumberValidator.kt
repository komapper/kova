package org.komapper.extension.validator

class NumberValidator<T> internal constructor(
    private val prev: NumberValidator<T>? = null,
    constraint: Constraint<T> = Constraint("kova.number") { ConstraintResult.Satisfied },
) : Validator<T, T>
    where T : Number, T : Comparable<T> {
    private val next: ConstraintValidator<T> = ConstraintValidator(constraint)

    override fun execute(
        context: ValidationContext,
        input: T,
    ): ValidationResult<T> =
        if (prev == null) {
            next.execute(context, input)
        } else {
            prev.chain(next = next).execute(context, input)
        }

    fun constraint(
        key: String,
        check: ConstraintScope.(ConstraintContext<T>) -> ConstraintResult,
    ): NumberValidator<T> = NumberValidator(prev = this, constraint = Constraint(key, check))

    fun min(
        value: T,
        message: (ConstraintContext<T>, T) -> Message = Message.resource1(),
    ): NumberValidator<T> = constraint("kova.number.min", Constraints.min(value, message))

    fun max(
        value: T,
        message: (ConstraintContext<T>, T) -> Message = Message.resource1(),
    ): NumberValidator<T> = constraint("kova.number.max", Constraints.max(value, message))
}
