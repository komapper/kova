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
        input: T,
        context: ValidationContext,
    ): ValidationResult<T> {
        val context = context.addLog(toString())
        return if (condition(input)) {
            validator.execute(input, context)
        } else {
            ValidationResult.Success(input, context)
        }
    }

    override fun toString(): String = "${ConditionalValidator::class.simpleName}"
}

fun <T> Validator<T, T>.onlyIf(condition: (T) -> Boolean): ConditionalValidator<T> = ConditionalValidator(this, condition)
