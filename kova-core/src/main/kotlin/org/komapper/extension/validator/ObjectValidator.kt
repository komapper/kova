package org.komapper.extension.validator

interface ObjectValidator<T : Any> : Validator<T, T>

fun <T : Any> ObjectValidator(): ObjectValidator<T> = ObjValidatorImpl()

private class ObjValidatorImpl<T : Any> : ObjectValidator<T> {
    override fun execute(
        context: ValidationContext,
        input: T,
    ): ValidationResult<T> = ValidationResult.Success(input, context)
}
