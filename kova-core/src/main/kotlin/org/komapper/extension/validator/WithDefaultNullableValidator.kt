package org.komapper.extension.validator

import org.komapper.extension.validator.ValidationResult.Success

interface WithDefaultNullableValidator<T : Any, S : Any> :
    Validator<T?, S>,
    Constrainable<T?, WithDefaultNullableValidator<T, S>> {
    fun isNull(message: (ConstraintContext<T?>) -> Message = Message.resource0()): WithDefaultNullableValidator<T, S>

    fun notNull(message: (ConstraintContext<T?>) -> Message = Message.resource0()): WithDefaultNullableValidator<T, S>

    operator fun plus(other: Validator<T?, S>): WithDefaultNullableValidator<T, S>

    infix fun and(other: Validator<T?, S>): WithDefaultNullableValidator<T, S>

    infix fun or(other: Validator<T?, S>): WithDefaultNullableValidator<T, S>

    fun <U : Any> compose(other: Validator<U, T>): WithDefaultNullableValidator<U, S>

    fun <U : Any> then(other: Validator<S, U>): WithDefaultNullableValidator<T, U>

    fun toNonNullable(): Validator<T?, S>
}

fun <T : Any, S : Any> Validator<T, S>.asNullable(defaultValue: S): WithDefaultNullableValidator<T, S> {
    val self = this
    // convert Validator<T, S> to Validator<T?, S?>
    val wrapped =
        Validator<T?, S> { context, input ->
            val context = context.addLog("Validator.asNullable(defaultValue=$defaultValue)")
            if (input == null) Success(defaultValue, context) else self.execute(context, input)
        }
    return WithDefaultNullableValidator("asNullable", wrapped)
}

fun <T : Any, S : Any> WithDefaultNullableValidator(
    name: String,
    after: Validator<T?, S>,
    constraint: Constraint<T?> = Constraint.satisfied(),
): WithDefaultNullableValidator<T, S> = WithDefaultNullableValidatorImpl(name, after, constraint)

private class WithDefaultNullableValidatorImpl<T : Any, S : Any>(
    private val name: String,
    private val after: Validator<T?, S>,
    constraint: Constraint<T?> = Constraint.satisfied(),
) : WithDefaultNullableValidator<T, S> {
    private val before: ConstraintValidator<T?> = ConstraintValidator(constraint)

    override fun execute(
        context: ValidationContext,
        input: T?,
    ): ValidationResult<S> {
        val context = context.addLog(toString())
        return before.then(after).execute(context, input)
    }

    override fun constrain(
        id: String,
        check: ConstraintScope.(ConstraintContext<T?>) -> ConstraintResult,
    ): WithDefaultNullableValidator<T, S> = WithDefaultNullableValidatorImpl(id, after, Constraint(id, check))

    override fun isNull(message: (ConstraintContext<T?>) -> Message): WithDefaultNullableValidator<T, S> =
        constrain("kova.nullable.isNull", {
            satisfies(it.input == null, message(it))
        })

    override fun notNull(message: (ConstraintContext<T?>) -> Message): WithDefaultNullableValidator<T, S> =
        constrain("kova.nullable.notNull", {
            satisfies(it.input != null, message(it))
        })

    override operator fun plus(other: Validator<T?, S>): WithDefaultNullableValidator<T, S> = and(other)

    override fun and(other: Validator<T?, S>): WithDefaultNullableValidator<T, S> =
        (this as Validator<T?, S>).and(other).let { WithDefaultNullableValidatorImpl("and", it) }

    override fun or(other: Validator<T?, S>): WithDefaultNullableValidator<T, S> =
        (this as Validator<T?, S>).or(other).let { WithDefaultNullableValidatorImpl("or", it) }

    override fun <U : Any> compose(other: Validator<U, T>): WithDefaultNullableValidator<U, S> =
        (this as Validator<T?, S>).compose(other.asNullable()).let {
            WithDefaultNullableValidatorImpl("compose", it)
        }

    override fun <U : Any> then(other: Validator<S, U>): WithDefaultNullableValidator<T, U> =
        (this as Validator<T?, S>).then(other).let {
            WithDefaultNullableValidatorImpl("then", it)
        }

    override fun toNonNullable(): Validator<T?, S> = map { it }

    override fun toString(): String = "${WithDefaultNullableValidator::class.simpleName}(name=$name)"
}
