package org.komapper.extension.validator

class NumberValidator<T> internal constructor(
    private val delegate: CoreValidator<T> = CoreValidator(),
) : Validator<T, T> by delegate
    where T : Number, T : Comparable<T> {
    operator fun plus(other: NumberValidator<T>): NumberValidator<T> = NumberValidator(delegate + other.delegate)

    fun constraint(
        key: String,
        check: (ConstraintContext<T>) -> ConstraintResult,
    ): NumberValidator<T> =
        NumberValidator(
            delegate + Constraint(key, check),
        )

    fun min(
        value: T,
        message: (ConstraintContext<T>, T) -> Message = Message.resource1(),
    ): NumberValidator<T> = constraint("kova.number.min", Constraints.min(value, message))

    fun max(
        value: T,
        message: (ConstraintContext<T>, T) -> Message = Message.resource1(),
    ): NumberValidator<T> = constraint("kova.number.max", Constraints.max(value, message))
}
