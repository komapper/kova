package org.komapper.extension.validator

class EnumValidator<E : Enum<E>> internal constructor(
    private val delegate: CoreValidator<E> = CoreValidator(),
) : Validator<E, E> by delegate {
    operator fun plus(other: EnumValidator<E>): EnumValidator<E> = EnumValidator(delegate + other.delegate)

    fun constraint(
        key: String,
        check: ConstraintScope.(ConstraintContext<E>) -> ConstraintResult,
    ): EnumValidator<E> =
        EnumValidator(
            delegate + Constraint(key, check),
        )

    fun contains(
        values: Set<E>,
        message: (ConstraintContext<E>, Set<E>) -> Message = Message.resource1(),
    ): EnumValidator<E> =
        constraint("kova.enum.contains") {
            satisfies(values.contains(it.input), message(it, values))
        }
}
