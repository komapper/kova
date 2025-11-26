package org.komapper.extension.validator

class EnumValidator<E : Enum<E>> internal constructor(
    private val delegate: CoreValidator<E> = CoreValidator(),
) : Validator<E, E> by delegate {
    operator fun plus(other: EnumValidator<E>): EnumValidator<E> = EnumValidator(delegate + other.delegate)

    fun constraint(constraint: Constraint<E>): EnumValidator<E> = EnumValidator(delegate + constraint)

    fun contains(
        values: Set<E>,
        message: (ConstraintContext<E>, Set<E>) -> Message = Message.resource1("kova.enum.contains"),
    ): EnumValidator<E> =
        constraint {
            Constraint.satisfies(values.contains(it.input), message(it, values))
        }
}
