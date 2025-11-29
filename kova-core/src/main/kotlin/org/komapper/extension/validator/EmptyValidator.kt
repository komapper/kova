package org.komapper.extension.validator

interface EmptyValidator<T> : Validator<T, T>

fun <T> EmptyValidator(): EmptyValidator<T> = EmptyValidatorImpl()

private class EmptyValidatorImpl<T> : EmptyValidator<T> {
    override fun execute(
        context: ValidationContext,
        input: T,
    ): ValidationResult<T> = ValidationResult.Success(input, context)
}
