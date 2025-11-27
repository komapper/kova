package org.komapper.extension.validator

open class NumberValidator<T> internal constructor(
    // TODO
    private val constraint: Constraint<T> = Constraint("kova.number") { ConstraintResult.Satisfied },
) : Validator<T, T>
    where T : Number, T : Comparable<T> {
    override fun execute(
        context: ValidationContext,
        input: T,
    ): ValidationResult<T> = CoreValidator(constraint).execute(context, input)

    fun constraint(
        key: String,
        check: ConstraintScope.(ConstraintContext<T>) -> ConstraintResult,
    ): NumberValidator<T> {
        val before = this
        return object : NumberValidator<T>(
            Constraint(key, check),
        ) {
            override fun execute(
                context: ValidationContext,
                input: T,
            ): ValidationResult<T> =
                chain(before, context, input) { context, input ->
                    super.execute(context, input)
                }
        }
    }

    fun min(
        value: T,
        message: (ConstraintContext<T>, T) -> Message = Message.resource1(),
    ): NumberValidator<T> = constraint("kova.number.min", Constraints.min(value, message))

    fun max(
        value: T,
        message: (ConstraintContext<T>, T) -> Message = Message.resource1(),
    ): NumberValidator<T> = constraint("kova.number.max", Constraints.max(value, message))
}
