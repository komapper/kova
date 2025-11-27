package org.komapper.extension.validator

import org.komapper.extension.validator.ValidationResult.FailureDetail

class ConstraintValidator<T>(
    val constraint: Constraint<T>,
) : Validator<T, T> {
    override fun execute(
        context: ValidationContext,
        input: T,
    ): ValidationResult<T> {
        val constraintContext = context.createConstraintContext(input).copy(key = constraint.key)
        return when (val result = constraint.apply(constraintContext)) {
            is ConstraintResult.Satisfied -> return ValidationResult.Success(input, context)
            is ConstraintResult.Violated -> {
                val failureDetails = convertToFailureDetails(context, result.message)
                ValidationResult.Failure(failureDetails)
            }
        }
    }

    companion object {
        fun convertToFailureDetails(
            context: ValidationContext,
            message: Message,
            cause: Throwable? = null,
        ): List<FailureDetail> =
            when (message) {
                is Message.Text -> listOf(FailureDetail(context, message, cause))
                is Message.Resource -> listOf(FailureDetail(context, message, cause))
                is Message.ValidationFailure -> message.details.flatMap { convertToFailureDetails(it.context, it.message, it.cause) }
            }
    }
}
