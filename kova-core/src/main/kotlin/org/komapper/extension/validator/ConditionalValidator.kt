package org.komapper.extension.validator

class ConditionalValidator<T> internal constructor(
    private val validator: Validator<T, T>,
    private val condition: (T) -> Boolean,
) : Validator<T, T> {
    override fun tryValidate(
        input: T,
        context: ValidationContext,
    ): ValidationResult<T> =
        if (condition(input)) {
            validator.tryValidate(input, context)
        } else {
            ValidationResult.Success(input, context)
        }

    operator fun plus(other: ConditionalValidator<T>): ConditionalValidator<T> =
        ConditionalValidator(this.validator + other.validator, condition)
}

fun <T> Validator<T, T>.conditional(condition: (T) -> Boolean): ConditionalValidator<T> = ConditionalValidator(this, condition)

fun <T> Validator<T, T>.onlyWhen(condition: (T) -> Boolean): ConditionalValidator<T> = conditional(condition)
