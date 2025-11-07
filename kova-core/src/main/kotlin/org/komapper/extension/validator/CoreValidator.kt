package org.komapper.extension.validator

import java.text.MessageFormat
import java.util.ResourceBundle

class CoreValidator<T, S>(
    val constraints: List<Constraint<T>> = emptyList(),
    val transform: (T) -> S,
) : Validator<T, S> {
    constructor(constraint: Constraint<T>, transform: (T) -> S) : this(listOf(constraint), transform)

    override fun tryValidate(
        input: T,
        context: ValidationContext,
    ): ValidationResult<S> {
        val constraintContext = context.createConstraintContext(input)

        val violatedConstraints =
            if (context.failFast) {
                constraints
                    .asSequence()
                    .map { it.apply(constraintContext) }
                    .filterIsInstance<ConstraintResult.Violated>()
                    .firstOrNull()
                    ?.let { sequenceOf(it) }
                    ?: emptySequence()
            } else {
                constraints
                    .asSequence()
                    .map { it.apply(constraintContext) }
                    .filterIsInstance<ConstraintResult.Violated>()
            }

        val failureDetails =
            violatedConstraints
                .flatMap { violated ->
                    when (val message = violated.message) {
                        is Message.Text -> {
                            sequenceOf(ValidationResult.FailureDetail(context, listOf(message.content)))
                        }
                        is Message.Resource -> {
                            val pattern = getPattern(message.key)
                            val formatted = MessageFormat.format(pattern, *message.args.toTypedArray())
                            sequenceOf(ValidationResult.FailureDetail(context, listOf(formatted)))
                        }
                        is Message.ValidationFailure -> {
                            message.details.asSequence()
                        }
                    }
                }.toList()

        return if (failureDetails.isEmpty()) {
            // TODO error handling
            val value = transform(input)
            ValidationResult.Success(value, context)
        } else {
            ValidationResult.Failure(failureDetails)
        }
    }

    operator fun plus(other: CoreValidator<T, S>) = CoreValidator(constraints + other.constraints, transform)

    operator fun plus(other: Constraint<T>) = CoreValidator(constraints + other, transform)

    companion object {
        private const val RESOURCE_BUNDLE_BASE_NAME = "kova"

        internal fun getPattern(key: String): String {
            val bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_BASE_NAME)
            return bundle.getString(key)
        }
    }
}
