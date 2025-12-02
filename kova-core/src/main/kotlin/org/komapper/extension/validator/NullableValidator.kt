package org.komapper.extension.validator

import org.komapper.extension.validator.ValidationResult.Success

interface NullableValidator<T : Any, S : Any> :
    Validator<T?, S?>,
    Constrainable<T?, NullableValidator<T, S>> {
    fun isNull(message: (ConstraintContext<T?>) -> Message = Message.resource0()): NullableValidator<T, S>

    fun notNull(message: (ConstraintContext<T?>) -> Message = Message.resource0()): NullableValidator<T, S>

    operator fun plus(other: Validator<T, S>): NullableValidator<T, S>

    infix fun and(other: Validator<T, S>): NullableValidator<T, S>

    infix fun or(other: Validator<T, S>): NullableValidator<T, S>

    fun <U : Any> compose(other: Validator<U, T>): NullableValidator<U, S>

    fun <U : Any> then(other: Validator<S, U>): NullableValidator<T, U>

    fun toNonNullable(): Validator<T?, S>

    fun withDefault(defaultValue: S): WithDefaultNullableValidator<T, S>
}

fun <T : Any, S : Any> Validator<T, S>.asNullable(): NullableValidator<T, S> {
    val self = this
    // convert Validator<T, S> to Validator<T?, S?>
    val wrapped =
        Validator<T?, S?> { context, input ->
            val context = context.addLog("Validator.asNullable")
            if (input == null) Success(null, context) else self.execute(context, input)
        }
    return NullableValidator("asNullable", wrapped)
}

fun <T : Any, S : Any> NullableValidator(
    name: String,
    after: Validator<T?, S?>,
    constraint: Constraint<T?> = Constraint.satisfied(),
): NullableValidator<T, S> = NullableValidatorImpl(name, after, constraint)

private class NullableValidatorImpl<T : Any, S : Any>(
    private val name: String,
    private val after: Validator<T?, S?>,
    constraint: Constraint<T?> = Constraint.satisfied(),
) : NullableValidator<T, S> {
    private val before: ConstraintValidator<T?> = ConstraintValidator(constraint)

    override fun execute(
        context: ValidationContext,
        input: T?,
    ): ValidationResult<S?> {
        val context = context.addLog(toString())
        return before.then(after).execute(context, input)
    }

    override fun constrain(
        id: String,
        check: ConstraintScope.(ConstraintContext<T?>) -> ConstraintResult,
    ): NullableValidator<T, S> = NullableValidatorImpl(id, after, Constraint(id, check))

    override fun isNull(message: (ConstraintContext<T?>) -> Message): NullableValidator<T, S> =
        constrain("kova.nullable.isNull", Constraints.isNull(message))

    override fun notNull(message: (ConstraintContext<T?>) -> Message): NullableValidator<T, S> =
        constrain("kova.nullable.notNull", Constraints.notNull(message))

    override operator fun plus(other: Validator<T, S>): NullableValidator<T, S> = and(other)

    override fun and(other: Validator<T, S>): NullableValidator<T, S> =
        this.and(other.asNullable()).let { NullableValidatorImpl("and", it) }

    override fun or(other: Validator<T, S>): NullableValidator<T, S> = this.or(other.asNullable()).let { NullableValidatorImpl("or", it) }

    override fun <U : Any> compose(other: Validator<U, T>): NullableValidator<U, S> =
        this.compose(other.asNullable()).let {
            NullableValidatorImpl("compose", it)
        }

    override fun <U : Any> then(other: Validator<S, U>): NullableValidator<T, U> =
        this.then(other.asNullable()).let {
            NullableValidatorImpl("then", it)
        }

    override fun toNonNullable(): Validator<T?, S> = notNull().map { it!! }

    override fun withDefault(defaultValue: S): WithDefaultNullableValidator<T, S> =
        WithDefaultNullableValidator(
            "withDefault",
            map {
                it ?: defaultValue
            },
        )

    override fun toString(): String = "${NullableValidator::class.simpleName}(name=$name)"
}
