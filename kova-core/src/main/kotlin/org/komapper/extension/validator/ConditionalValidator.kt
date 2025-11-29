package org.komapper.extension.validator

interface ConditionalValidator<T> : Validator<T, T>

fun <T> ConditionalValidator(
    validator: Validator<T, T>,
    condition: (T) -> Boolean,
): ConditionalValidator<T> = ConditionalValidatorImpl(validator, condition)

private class ConditionalValidatorImpl<T>(
    private val validator: Validator<T, T>,
    private val condition: (T) -> Boolean,
) : ConditionalValidator<T> {
    override fun execute(
        context: ValidationContext,
        input: T,
    ): ValidationResult<T> =
        if (condition(input)) {
            validator.execute(context, input)
        } else {
            ValidationResult.Success(input, context)
        }
}

fun <T> Validator<T, T>.conditional(condition: (T) -> Boolean): ConditionalValidator<T> = ConditionalValidator(this, condition)

fun <T> Validator<T, T>.onlyWhen(condition: (T) -> Boolean): ConditionalValidator<T> = conditional(condition)
