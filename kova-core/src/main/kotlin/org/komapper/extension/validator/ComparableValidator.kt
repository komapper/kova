package org.komapper.extension.validator

open class ComparableValidator<T : Comparable<T>> internal constructor(
    // TODO
    private val constraint: Constraint<T> = Constraint("kova.comparable") { ConstraintResult.Satisfied },
) : Validator<T, T> {
    override fun execute(
        context: ValidationContext,
        input: T,
    ): ValidationResult<T> {
        // TODO
        return ConstraintValidator(constraint).execute(context, input)
    }

    private class Chain<T : Comparable<T>>(
        val before: ComparableValidator<T>,
        val transform: (T) -> T = { it },
        constraint: Constraint<T> = Constraint("kova.comparable") { ConstraintResult.Satisfied },
    ) : ComparableValidator<T>(constraint) {
        override fun execute(
            context: ValidationContext,
            input: T,
        ): ValidationResult<T> =
            chain(before, context, input) { context, input ->
                super.execute(context, transform(input))
            }
    }

    fun constraint(
        key: String,
        check: ConstraintScope.(ConstraintContext<T>) -> ConstraintResult,
    ): ComparableValidator<T> = Chain(before = this, constraint = Constraint(key, check))

    fun min(
        value: T,
        message: (ConstraintContext<T>, T) -> Message = Message.resource1(),
    ): ComparableValidator<T> = constraint("kova.comparable.min", Constraints.min(value, message))

    fun max(
        value: T,
        message: (ConstraintContext<T>, T) -> Message = Message.resource1(),
    ): ComparableValidator<T> = constraint("kova.comparable.max", Constraints.max(value, message))
}
