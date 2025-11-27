package org.komapper.extension.validator

class ComparableValidator<T : Comparable<T>> internal constructor(
    private val delegate: CoreValidator<T> = CoreValidator(),
) : Validator<T, T> by delegate {
    operator fun plus(other: ComparableValidator<T>): ComparableValidator<T> = ComparableValidator(delegate + other.delegate)

    fun constraint(
        key: String,
        check: ConstraintScope.(ConstraintContext<T>) -> ConstraintResult,
    ): ComparableValidator<T> =
        ComparableValidator(
            delegate + Constraint(key, check),
        )

    fun min(
        value: T,
        message: (ConstraintContext<T>, T) -> Message = Message.resource1(),
    ): ComparableValidator<T> = constraint("kova.comparable.min", Constraints.min(value, message))

    fun max(
        value: T,
        message: (ConstraintContext<T>, T) -> Message = Message.resource1(),
    ): ComparableValidator<T> = constraint("kova.comparable.max", Constraints.max(value, message))
}
