package org.komapper.extension.validator

internal class EmptyValidator<T> internal constructor() : Validator<T, T> {
    override fun execute(
        context: ValidationContext,
        input: T,
    ): ValidationResult<T> = ValidationResult.Success(input, context)
}
