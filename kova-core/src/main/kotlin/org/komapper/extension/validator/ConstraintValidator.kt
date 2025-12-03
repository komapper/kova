package org.komapper.extension.validator

interface ConstraintValidator<T> : Validator<T, T>

fun <T> ConstraintValidator(constraint: Constraint<T>): ConstraintValidator<T> = ConstraintValidatorImpl(constraint)

private class ConstraintValidatorImpl<T>(
    val constraint: Constraint<T>,
) : ConstraintValidator<T> {
    override fun execute(
        input: T,
        context: ValidationContext,
    ): ValidationResult<T> {
        val context = context.addLog(toString())
        val constraintContext = context.createConstraintContext(input).copy(constraintId = constraint.id)
        return when (val result = constraint.apply(constraintContext)) {
            is ConstraintResult.Satisfied -> ValidationResult.Success(input, context)
            is ConstraintResult.Violated -> {
                val failureDetails =
                    when (result.message) {
                        is Message.Text, is Message.Resource -> listOf(SimpleFailureDetail(context, result.message))
                        is Message.ValidationFailure -> result.message.details
                    }
                ValidationResult.Failure(failureDetails)
            }
        }
    }

    override fun toString(): String = "${ConstraintValidator::class.simpleName}(name=${constraint.id})"
}
