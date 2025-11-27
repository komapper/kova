package org.komapper.extension.validator

class ComparableValidator<T : Comparable<T>> internal constructor(
    private val prev: ComparableValidator<T>? = null,
    constraint: Constraint<T> = Constraint("kova.comparable") { ConstraintResult.Satisfied },
) : Validator<T, T> {
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
    ): ComparableValidator<T> = ComparableValidator(prev = this, constraint = Constraint(key, check))

    fun min(
        value: T,
        message: (ConstraintContext<T>, T) -> Message = Message.resource1(),
    ): ComparableValidator<T> = constraint("kova.comparable.min", Constraints.min(value, message))

    fun max(
        value: T,
        message: (ConstraintContext<T>, T) -> Message = Message.resource1(),
    ): ComparableValidator<T> = constraint("kova.comparable.max", Constraints.max(value, message))
}
