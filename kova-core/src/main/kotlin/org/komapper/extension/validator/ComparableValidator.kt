package org.komapper.extension.validator

class ComparableValidator<T : Comparable<T>> internal constructor(
    private val delegate: CoreValidator<T> = CoreValidator(),
) : Validator<T, T> by delegate {
    operator fun plus(other: ComparableValidator<T>): ComparableValidator<T> = ComparableValidator(delegate + other.delegate)

    fun constraint(constraint: Constraint<T>): ComparableValidator<T> = ComparableValidator(delegate + constraint)

    fun min(
        value: T,
        message: (ConstraintContext<T>, T) -> Message = Message.resource1("kova.comparable.min"),
    ): ComparableValidator<T> = constraint(Constraints.min(value, message))

    fun max(
        value: T,
        message: (ConstraintContext<T>, T) -> Message = Message.resource1("kova.comparable.max"),
    ): ComparableValidator<T> = constraint(Constraints.max(value, message))
}
