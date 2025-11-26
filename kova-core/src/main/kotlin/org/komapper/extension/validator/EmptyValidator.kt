package org.komapper.extension.validator

internal class EmptyValidator<T> internal constructor() : Validator<T, T> {
    override fun tryValidate(
        input: T,
        context: ValidationContext,
    ): ValidationResult<T> = ValidationResult.Success(input, context)
}
