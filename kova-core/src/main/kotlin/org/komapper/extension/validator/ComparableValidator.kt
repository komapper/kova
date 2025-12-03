package org.komapper.extension.validator

interface ComparableValidator<T : Comparable<T>> :
    Validator<T, T>,
    Constrainable<T, ComparableValidator<T>> {
    fun min(
        value: T,
        message: MessageProvider1<T, T> = Message.resource1("kova.comparable.min"),
    ): ComparableValidator<T>

    fun max(
        value: T,
        message: MessageProvider1<T, T> = Message.resource1("kova.comparable.max"),
    ): ComparableValidator<T>

    operator fun plus(other: Validator<T, T>): ComparableValidator<T>

    infix fun and(other: Validator<T, T>): ComparableValidator<T>

    infix fun or(other: Validator<T, T>): ComparableValidator<T>

    fun chain(other: Validator<T, T>): ComparableValidator<T>
}

fun <T : Comparable<T>> ComparableValidator(
    name: String = "empty",
    prev: Validator<T, T> = EmptyValidator(),
    constraint: Constraint<T> = Constraint.satisfied(),
): ComparableValidator<T> = ComparableValidatorImpl(name, prev, constraint)

private class ComparableValidatorImpl<T : Comparable<T>> internal constructor(
    private val name: String,
    private val prev: Validator<T, T>,
    private val constraint: Constraint<T> = Constraint.satisfied(),
) : ComparableValidator<T> {
    private val next: ConstraintValidator<T> = ConstraintValidator(constraint)

    override fun execute(
        context: ValidationContext,
        input: T,
    ): ValidationResult<T> {
        val context = context.addLog(toString())
        return prev.chain(next).execute(context, input)
    }

    override fun constrain(
        id: String,
        check: ConstraintScope.(ConstraintContext<T>) -> ConstraintResult,
    ): ComparableValidator<T> = ComparableValidatorImpl(name = id, prev = this, constraint = Constraint(id, check))

    override fun min(
        value: T,
        message: MessageProvider1<T, T>,
    ): ComparableValidator<T> = constrain(message.key, Constraints.min(value, message))

    override fun max(
        value: T,
        message: MessageProvider1<T, T>,
    ): ComparableValidator<T> = constrain(message.key, Constraints.max(value, message))

    override operator fun plus(other: Validator<T, T>): ComparableValidator<T> = and(other)

    override fun and(other: Validator<T, T>): ComparableValidator<T> {
        val combined = (this as Validator<T, T>).and(other)
        return ComparableValidatorImpl("and", prev = combined)
    }

    override fun or(other: Validator<T, T>): ComparableValidator<T> {
        val combined = (this as Validator<T, T>).or(other)
        return ComparableValidatorImpl("or", prev = combined)
    }

    override fun chain(other: Validator<T, T>): ComparableValidator<T> {
        val combined = (this as Validator<T, T>).chain(other)
        return ComparableValidatorImpl("chain", prev = combined)
    }

    override fun toString(): String = "${ComparableValidator::class.simpleName}(name=$name)"
}
