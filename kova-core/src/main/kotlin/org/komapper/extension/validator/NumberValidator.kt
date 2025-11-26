package org.komapper.extension.validator

class NumberValidator<T> internal constructor(
    private val delegate: CoreValidator<T> = CoreValidator(),
) : Validator<T, T> by delegate
    where T : Number, T : Comparable<T> {
    operator fun plus(other: NumberValidator<T>): NumberValidator<T> = NumberValidator(delegate + other.delegate)

    fun constraint(constraint: Constraint<T>): NumberValidator<T> = NumberValidator(delegate + constraint)

    fun min(
        value: T,
        message: (ConstraintContext<T>, T) -> Message = Message.resource1("kova.number.min"),
    ): NumberValidator<T> = constraint(Constraints.min(value, message))

    fun max(
        value: T,
        message: (ConstraintContext<T>, T) -> Message = Message.resource1("kova.number.max"),
    ): NumberValidator<T> = constraint(Constraints.max(value, message))
}
