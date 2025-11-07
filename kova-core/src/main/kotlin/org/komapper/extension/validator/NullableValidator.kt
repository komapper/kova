package org.komapper.extension.validator

class NullableValidator<T> internal constructor(
    private val delegate: Validator<T, T>,
    private val nullableConstraints: CoreValidator<T?, T?> = CoreValidator(transform = { it }),
) : Validator<T?, T?> {
    override fun tryValidate(
        input: T?,
        context: ValidationContext,
    ): ValidationResult<T?> =
        when (val result = nullableConstraints.tryValidate(input, context)) {
            is ValidationResult.Success -> {
                if (input == null) {
                    result
                } else {
                    delegate.tryValidate(input, context)
                }
            }

            is ValidationResult.Failure -> {
                if (context.failFast || input == null) {
                    result
                } else {
                    result + delegate.tryValidate(input, context)
                }
            }
        }

    operator fun plus(other: NullableValidator<T>): NullableValidator<T> =
        NullableValidator(delegate + other.delegate, nullableConstraints + other.nullableConstraints)

    operator fun plus(constraint: Constraint<T?>): NullableValidator<T> = NullableValidator(delegate, nullableConstraints + constraint)

    fun constraint(constraint: Constraint<T?>): NullableValidator<T> = NullableValidator(delegate, nullableConstraints + constraint)

    fun isNull(message: (ConstraintContext<T?>) -> Message = Message.resource0("kova.nullable.isNull")): NullableValidator<T> =
        constraint(Constraints.isNull(message))

    fun isNullOr(
        vararg validators: Validator<T, T>,
        message: ((ConstraintContext<T?>) -> Message)? = null,
    ): Validator<T?, T?> {
        var result: Validator<T?, T?> = if (message == null) isNull() else isNull(message)
        for (validator in validators) {
            result = result.or(validator.asNullable())
        }
        return result
    }

    fun isNotNull(message: (ConstraintContext<T?>) -> Message = Message.resource0("kova.nullable.isNotNull")): NullableValidator<T> =
        constraint(Constraints.isNotNull(message))

    fun isNotNullAnd(
        vararg validators: Validator<T, T>,
        message: ((ConstraintContext<T?>) -> Message)? = null,
    ): Validator<T?, T?> {
        var result: Validator<T?, T?> = if (message == null) isNotNull() else isNotNull(message)
        for (validator in validators) {
            result = result and validator.asNullable()
        }
        return result
    }

    fun whenNotNull(next: Validator<T, T>): Validator<T?, T?> = andThen(next.asNullable())

    fun toNonNullable(): Validator<T?, T & Any> = isNotNull().map { it!! }

    fun <S> toNonNullableThen(next: Validator<T & Any, S>): Validator<T?, S> = toNonNullable().andThen(next)
}

fun <T> Validator<T, T>.asNullable(): NullableValidator<T> = NullableValidator(this)
