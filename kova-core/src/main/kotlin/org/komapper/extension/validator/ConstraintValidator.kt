package org.komapper.extension.validator

import org.komapper.extension.validator.ValidationResult.FailureDetail

interface ConstraintValidator<T> : Validator<T, T>

fun <T> ConstraintValidator(constraint: Constraint<T>): ConstraintValidator<T> = ConstraintValidatorImpl(constraint)

private class ConstraintValidatorImpl<T>(
    val constraint: Constraint<T>,
) : ConstraintValidator<T> {
    override fun execute(
        context: ValidationContext,
        input: T,
    ): ValidationResult<T> {
        val constraintContext = context.createConstraintContext(input).copy(constraintId = constraint.id)
        return when (val result = constraint.apply(constraintContext)) {
            is ConstraintResult.Satisfied -> return ValidationResult.Success(input, context)
            is ConstraintResult.Violated -> {
                val failureDetails = collectFailureDetails(context, result.message)
                ValidationResult.Failure(failureDetails)
            }
        }
    }

    companion object {
        fun collectFailureDetails(
            context: ValidationContext,
            message: Message,
            cause: Throwable? = null,
        ): List<FailureDetail> =
            when (message) {
                is Message.Text -> listOf(FailureDetail(context, message, cause))
                is Message.Resource -> listOf(FailureDetail(context, message, cause))
                is Message.ValidationFailure -> message.details.flatMap { collectFailureDetails(it.context, it.message, it.cause) }
            }
    }
}
