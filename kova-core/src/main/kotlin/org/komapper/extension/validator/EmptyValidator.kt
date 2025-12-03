package org.komapper.extension.validator

interface EmptyValidator<T> : Validator<T, T>

fun <T> EmptyValidator(): EmptyValidator<T> = EmptyValidatorImpl()

private class EmptyValidatorImpl<T> : EmptyValidator<T> {
    override fun execute(
        input: T,
        context: ValidationContext,
    ): ValidationResult<T> {
        val context = context.addLog(toString())
        return ValidationResult.Success(input, context)
    }

    override fun toString(): String = "${EmptyValidator::class.simpleName}"
}
