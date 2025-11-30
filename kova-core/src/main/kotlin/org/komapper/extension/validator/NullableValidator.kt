package org.komapper.extension.validator

import org.komapper.extension.validator.ValidationResult.Success

interface NullableValidator<T : Any, S : Any> :
    Validator<T?, S?>,
    Constrainable<T?, NullableValidator<T, S>> {
    fun isNull(message: (ConstraintContext<T?>) -> Message = Message.resource0()): NullableValidator<T, S>

    fun isNullOrElse(
        other: Validator<T, S>,
        message: ((ConstraintContext<T?>) -> Message)? = null,
    ): NullableValidator<T, S>

    fun notNull(message: (ConstraintContext<T?>) -> Message = Message.resource0()): NullableValidator<T, S>

    fun <U : Any> whenNotNull(other: Validator<S, U>): Validator<T?, U?> = andThen(other.asNullable())

    fun toNonNullable(message: ((ConstraintContext<T?>) -> Message)? = null): Validator<T?, S>
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
            validator.andThen(inner).execute(context, input)
        }

    override fun constrain(
        id: String,
        check: ConstraintScope.(ConstraintContext<T?>) -> ConstraintResult,
    ): NullableValidator<T, S> = NullableValidatorImpl(inner, constraints + Constraint(id, check))

    override fun isNull(message: (ConstraintContext<T?>) -> Message): NullableValidator<T, S> =
        constrain("kova.nullable.isNull", {
            satisfies(it.input == null, message(it))
        })

    override fun isNullOrElse(
        other: Validator<T, S>,
        message: ((ConstraintContext<T?>) -> Message)?,
    ): NullableValidator<T, S> {
        val isNull: Validator<T?, S?> = if (message == null) isNull() else isNull(message)
        return NullableValidatorImpl(isNull.or(other.asNullable()), emptyList())
    }

    override fun notNull(message: (ConstraintContext<T?>) -> Message): NullableValidator<T, S> =
        constrain("kova.nullable.notNull", { ctx ->
            satisfies(ctx.input != null, message(ctx))
        })

    override fun toNonNullable(message: ((ConstraintContext<T?>) -> Message)?): Validator<T?, S> {
        val notNull = if (message == null) notNull() else notNull(message)
        return notNull.map { it!! }
    }
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

// shortcut function for asNullable().isNull()
fun <T : Any, S : Any> Validator<T, S>.isNull(message: ((ConstraintContext<T?>) -> Message)? = null): NullableValidator<T, S> {
    val nullable = this.asNullable()
    return if (message == null) nullable.isNull() else nullable.isNull(message)
}

// shortcut function for asNullable().isNullOrElse()
fun <T : Any, S : Any> Validator<T, S>.isNullOrElse(
    other: Validator<T, S>,
    message: ((ConstraintContext<T?>) -> Message)? = null,
): NullableValidator<T, S> = this.asNullable().isNullOrElse(other, message = message)

// shortcut function for asNullable().toNonNullable()
fun <T : Any, S : Any> Validator<T, S>.notNull(message: ((ConstraintContext<T?>) -> Message)? = null): Validator<T?, S> {
    val nullable = this.asNullable()
    return if (message == null) nullable.toNonNullable() else nullable.toNonNullable(message)
}
