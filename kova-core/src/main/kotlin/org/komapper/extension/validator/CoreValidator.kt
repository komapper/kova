package org.komapper.extension.validator

import java.util.ResourceBundle

class CoreValidator<T>(
    val constraints: List<Constraint<T>> = emptyList(),
) : Validator<T, T> {
    constructor(constraint: Constraint<T>) : this(listOf(constraint))

    override fun execute(
        context: ValidationContext,
        input: T,
    ): ValidationResult<T> {
        val constraintContext = context.createConstraintContext(input)

        val constraintResults =
            if (context.failFast) {
                constraints
                    .map { it.apply(constraintContext) }
                    .firstOrNull()
                    ?.let { listOf(it) }
                    ?: emptyList()
            } else {
                constraints.map { it.apply(constraintContext) }
            }

        val failureDetails =
            constraintResults.filterIsInstance<ConstraintResult.Violated>().flatMap {
                ValidationResult.FailureDetail.extract(context, it)
            }

        return if (failureDetails.isEmpty()) {
            // TODO error handling
            ValidationResult.Success(input, context)
        } else {
            ValidationResult.Failure(failureDetails)
        }
    }

    operator fun plus(other: CoreValidator<T>) = CoreValidator(constraints + other.constraints)

    operator fun plus(other: Constraint<T>) = CoreValidator(constraints + other)

    companion object {
        private const val RESOURCE_BUNDLE_BASE_NAME = "kova"

        internal fun getPattern(key: String): String {
            val bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_BASE_NAME)
            return bundle.getString(key)
        }
    }
}
