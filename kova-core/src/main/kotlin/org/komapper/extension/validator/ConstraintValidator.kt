package org.komapper.extension.validator

/**
 * Type alias for validators that validate constraints without transforming the type.
 *
 * A ConstraintValidator is an [IdentityValidator] that checks constraints on input values,
 * returning the same value if constraints are satisfied, or validation failures if violated.
 */
typealias ConstraintValidator<T> = IdentityValidator<T>

/**
 * Creates a ConstraintValidator from a [Constraint].
 *
 * This factory function converts a constraint into a validator that executes the constraint
 * and converts the [ConstraintResult] into a [ValidationResult]. It also logs the validation
 * result if logging is enabled in the [ValidationConfig].
 *
 * When a constraint is satisfied, the input value is returned unchanged wrapped in a Success.
 * When violated, the constraint's error message is returned in a Failure.
 *
 * Example:
 * ```kotlin
 * val constraint = Constraint("kova.string.min") { ctx ->
 *     satisfies(ctx.input.length >= 3, "Must be at least 3 characters")
 * }
 * val validator = ConstraintValidator(constraint)
 * ```
 *
 * @param constraint The constraint to apply
 * @return A validator that checks the constraint and returns the input unchanged if satisfied
 */
fun <T> ConstraintValidator(constraint: Constraint<T>): ConstraintValidator<T> =
    Validator { input, context ->
        val constraintContext = context.createConstraintContext(input, constraint.id)
        when (val result = constraint.apply(constraintContext)) {
            is ConstraintResult.Satisfied -> {
                context.log {
                    LogEntry.Satisfied(
                        constraintId = constraint.id,
                        root = context.root,
                        path = context.path.fullName,
                        input = input,
                    )
                }
                ValidationResult.Success(input, context)
            }

            is ConstraintResult.Violated -> {
                context.log {
                    LogEntry.Violated(
                        constraintId = constraint.id,
                        root = context.root,
                        path = context.path.fullName,
                        input = input,
                    )
                }
                ValidationResult.Failure(Input.Some(input), listOf(result.message))
            }
        }
    }
