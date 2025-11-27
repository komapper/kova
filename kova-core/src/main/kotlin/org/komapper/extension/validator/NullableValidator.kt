package org.komapper.extension.validator

import org.komapper.extension.validator.ValidationResult.Success

class NullableValidator<T : Any, S : Any> internal constructor(
    private val inner: Validator<T?, S?>,
    private val constraints: List<Constraint<T?>> = emptyList(),
) : Validator<T?, S?> {
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

    private fun constraint(
        key: String,
        constraint: ConstraintScope.(ConstraintContext<T?>) -> ConstraintResult,
    ): NullableValidator<T, S> = NullableValidator(inner, constraints + Constraint(key, constraint))

    fun isNull(message: (ConstraintContext<T?>) -> Message = Message.resource0()): NullableValidator<T, S> =
        constraint("kova.nullable.isNull", {
            satisfies(it.input == null, message(it))
        })

    fun isNullOr(
        vararg validators: Validator<T, S>,
        message: ((ConstraintContext<T?>) -> Message)? = null,
    ): NullableValidator<T, S> {
        val isNull: Validator<T?, S?> = if (message == null) isNull() else isNull(message)
        val validator = validators.fold(isNull) { acc, validator -> acc.or(validator.asNullable()) }
        return NullableValidator(validator)
    }

    fun isNotNull(message: (ConstraintContext<T?>) -> Message = Message.resource0()): NotNullValidator<T, S> {
        val validator =
            constraint("kova.nullable.isNotNull", { ctx ->
                satisfies(ctx.input != null, message(ctx))
            })
        return NotNullValidator(validator)
    }

    fun isNotNullAnd(
        vararg validators: Validator<T, S>,
        message: ((ConstraintContext<T?>) -> Message)? = null,
    ): NotNullValidator<T, S> {
        val isNotNull: Validator<T?, S?> = if (message == null) isNotNull() else isNotNull(message)
        val validator = validators.fold(isNotNull) { acc, validator -> acc.and(validator.asNullable()) }
        return NotNullValidator(validator)
    }

    fun asNonNullable(): Validator<T?, S> =
        // add the isNotNull constraint to avoid NulPointerException
        isNotNull().asNonNullable()

    fun <N> asNonNullableThen(next: Validator<S, N>): Validator<T?, N> =
        // add the isNotNull constraint to avoid NulPointerException
        isNotNull().asNonNullable().andThen(next)
}

class NotNullValidator<T : Any, S : Any> internal constructor(
    private val inner: Validator<T?, S?>,
) : Validator<T?, S?> {
    override fun execute(
        context: ValidationContext,
        input: T?,
    ): ValidationResult<S?> = inner.execute(context, input)

    fun asNonNullable(): Validator<T?, S> = map { it!! }

    fun <N> asNonNullableThen(next: Validator<S, N>): Validator<T?, N> = asNonNullable().andThen(next)
}

fun <T : Any, S : Any> Validator<T, S>.asNullable(): NullableValidator<T, S> {
    val self = this
    // convert Validator<T, S> to Validator<T?, S?>
    val wrapped =
        Validator<T?, S?> { context, input ->
            if (input == null) {
                Success(null, context)
            } else {
                self.execute(context, input)
            }
        }
    return NullableValidator(wrapped)
}

// shortcut function for asNullable().isNull()
fun <T : Any, S : Any> Validator<T, S>.isNull(message: ((ConstraintContext<T?>) -> Message)? = null): NullableValidator<T, S> {
    val nullable = this.asNullable()
    return if (message == null) nullable.isNull() else nullable.isNull(message)
}

// shortcut function for asNullable().isNullOr()
fun <T : Any, S : Any> Validator<T, S>.isNullOr(
    vararg validators: Validator<T, S>,
    message: ((ConstraintContext<T?>) -> Message)? = null,
): NullableValidator<T, S> = this.asNullable().isNullOr(*validators, message = message)

// shortcut function for asNullable().isNotNull()
fun <T : Any, S : Any> Validator<T, S>.isNotNull(message: ((ConstraintContext<T?>) -> Message)? = null): NotNullValidator<T, S> {
    val nullable = this.asNullable()
    return if (message == null) nullable.isNotNull() else nullable.isNotNull(message)
}

// shortcut function for asNullable().isNotNullAnd()
fun <T : Any, S : Any> Validator<T, S>.isNotNullAnd(
    vararg validators: Validator<T, S>,
    message: ((ConstraintContext<T?>) -> Message)? = null,
): NotNullValidator<T, S> = this.asNullable().isNotNullAnd(*validators, message = message)

fun <T : Any, S : Any, X : Any> Validator<T?, S?>.whenNotNull(next: Validator<S, X>): Validator<T?, X?> = this.andThen(next.asNullable())
