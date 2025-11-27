package org.komapper.extension.validator

import java.util.ResourceBundle

class CoreValidator<T>(
    val constraint: Constraint<T>
) : Validator<T, T> {

    override fun execute(
        context: ValidationContext,
        input: T,
    ): ValidationResult<T> {
        val constraintContext = context.createConstraintContext(input).copy(key = constraint.key)
        return when (val result =  constraint.apply(constraintContext)) {
            is ConstraintResult.Satisfied -> return ValidationResult.Success(input, context)
            is ConstraintResult.Violated -> {
                val failureDetails = ValidationResult.FailureDetail.extract(context, result)
                ValidationResult.Failure(failureDetails)
            }
        }
    }

    // TODO
    companion object {
        private const val RESOURCE_BUNDLE_BASE_NAME = "kova"

        internal fun getPattern(key: String): String {
            val bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_BASE_NAME)
            return bundle.getString(key)
        }
    }
}
