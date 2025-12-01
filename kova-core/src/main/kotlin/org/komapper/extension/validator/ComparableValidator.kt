package org.komapper.extension.validator

interface ComparableValidator<T : Comparable<T>> :
    Validator<T, T>,
    Constrainable<T, ComparableValidator<T>> {
    fun min(
        value: T,
        message: (ConstraintContext<T>, T) -> Message = Message.resource1(),
    ): ComparableValidator<T>

    fun max(
        value: T,
        message: (ConstraintContext<T>, T) -> Message = Message.resource1(),
    ): ComparableValidator<T>

    operator fun plus(other: Validator<T, T>): ComparableValidator<T>

    infix fun and(other: Validator<T, T>): ComparableValidator<T>

    infix fun or(other: Validator<T, T>): ComparableValidator<T>

    fun chain(other: Validator<T, T>): ComparableValidator<T>
}

fun <T : Comparable<T>> ComparableValidator(
    prev: Validator<T, T> = EmptyValidator(),
    constraint: Constraint<T> = Constraint.satisfied(),
): ComparableValidator<T> = ComparableValidatorImpl(prev, constraint)

private class ComparableValidatorImpl<T : Comparable<T>> internal constructor(
    private val prev: Validator<T, T>,
    constraint: Constraint<T> = Constraint.satisfied(),
) : ComparableValidator<T> {
    private val next: ConstraintValidator<T> = ConstraintValidator(constraint)

    override fun execute(
        context: ValidationContext,
        input: T,
    ): ValidationResult<T> = prev.chain(next).execute(context, input)

    override fun constrain(
        id: String,
        check: ConstraintScope.(ConstraintContext<T>) -> ConstraintResult,
    ): ComparableValidator<T> = ComparableValidatorImpl(prev = this, constraint = Constraint(id, check))

    override fun min(
        value: T,
        message: (ConstraintContext<T>, T) -> Message,
    ): ComparableValidator<T> = constrain("kova.comparable.min", Constraints.min(value, message))

    override fun max(
        value: T,
        message: (ConstraintContext<T>, T) -> Message,
    ): ComparableValidator<T> = constrain("kova.comparable.max", Constraints.max(value, message))

    override operator fun plus(other: Validator<T, T>): ComparableValidator<T> = and(other)

    override fun and(other: Validator<T, T>): ComparableValidator<T> {
        val combined = (this as Validator<T, T>).and(other)
        return ComparableValidatorImpl(prev = combined)
    }

    override fun or(other: Validator<T, T>): ComparableValidator<T> {
        val combined = (this as Validator<T, T>).or(other)
        return ComparableValidatorImpl(prev = combined)
    }

    override fun chain(other: Validator<T, T>): ComparableValidator<T> {
        val combined = (this as Validator<T, T>).chain(other)
        return ComparableValidatorImpl(prev = combined)
    }
}
