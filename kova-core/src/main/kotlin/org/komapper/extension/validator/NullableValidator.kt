package org.komapper.extension.validator

import org.komapper.extension.validator.ValidationResult.Success

interface NullableValidator<T : Any, S : Any> :
    Validator<T?, S?>,
    Constrainable<T?, NullableValidator<T, S>> {
    fun isNull(message: (ConstraintContext<T?>) -> Message = Message.resource0()): NullableValidator<T, S>

    fun notNull(message: (ConstraintContext<T?>) -> Message = Message.resource0()): NullableValidator<T, S>

    operator fun plus(other: Validator<T, S>): NullableValidator<T, S>

    fun and(other: Validator<T, S>): NullableValidator<T, S>

    fun or(other: Validator<T, S>): NullableValidator<T, S>

    fun <U: Any> compose(other: Validator<U, T>): NullableValidator<U, S>

    fun <U: Any> then(other: Validator<S, U>): NullableValidator<T, U>

    fun toDefaultIfNull(value: S): Validator<T?, S>

    fun toNonNullable(): Validator<T?, S>
}

fun <T : Any, S : Any> Validator<T, S>.asNullable(): NullableValidator<T, S> {
    val self = this
    // convert Validator<T, S> to Validator<T?, S?>
    val wrapped =
        Validator<T?, S?> { context, input ->
            if (input == null) Success(null, context) else self.execute(context, input)
        }
    return NullableValidator(wrapped)
}

fun <T : Any, S : Any> NullableValidator(
    inner: Validator<T?, S?>,
    constraints: List<Constraint<T?>> = emptyList(),
): NullableValidator<T, S> = NullableValidatorImpl(inner, constraints)

private class NullableValidatorImpl<T : Any, S : Any>(
    private val inner: Validator<T?, S?>,
    private val constraints: List<Constraint<T?>>,
) : NullableValidator<T, S> {
    override fun execute(
        context: ValidationContext,
        input: T?,
    ): ValidationResult<S?> =
        if (constraints.isEmpty()) {
            inner.execute(context, input)
        } else {
            val validator = constraints.map { ConstraintValidator(it) as Validator<T?, T?> }.reduce { a, b -> a.and(b) }
            validator.then(inner).execute(context, input)
        }

    override fun constrain(
        id: String,
        check: ConstraintScope.(ConstraintContext<T?>) -> ConstraintResult,
    ): NullableValidator<T, S> = NullableValidatorImpl(inner, constraints + Constraint(id, check))

    override fun isNull(message: (ConstraintContext<T?>) -> Message): NullableValidator<T, S> =
        constrain("kova.nullable.isNull", {
            satisfies(it.input == null, message(it))
        })

    override fun notNull(message: (ConstraintContext<T?>) -> Message): NullableValidator<T, S> =
        constrain("kova.nullable.notNull", { ctx ->
            satisfies(ctx.input != null, message(ctx))
        })

    override operator fun plus(other: Validator<T, S>): NullableValidator<T, S> = (this + (other.asNullable())).let { NullableValidator(it) }

    override fun and(other: Validator<T, S>): NullableValidator<T, S> = this.and(other.asNullable()).let { NullableValidator(it) }

    override fun or(other: Validator<T, S>): NullableValidator<T, S> = this.or(other.asNullable()).let { NullableValidator(it) }

    override fun <U : Any> compose(other: Validator<U, T>): NullableValidator<U, S> = this.compose(other.asNullable()).let { NullableValidator(it) }

    override fun <U: Any> then(other: Validator<S, U>): NullableValidator<T, U> = this.then(other.asNullable()).let { NullableValidator(it) }

    override fun toDefaultIfNull(value: S): Validator<T?, S> = map { it ?: value }

    override fun toNonNullable(): Validator<T?, S> = notNull().map { it!! }
}
