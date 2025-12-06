package org.komapper.extension.validator

typealias ConstraintValidator<T> = IdentityValidator<T>

fun <T> ConstraintValidator(constraint: Constraint<T>): ConstraintValidator<T> =
    Validator { input, context ->
        val context = context.addLog(constraint.id)
        val constraintContext = context.createConstraintContext(input, constraint.id)
        when (val result = constraint.apply(constraintContext)) {
            is ConstraintResult.Satisfied -> ValidationResult.Success(input, context)
            is ConstraintResult.Violated -> ValidationResult.Failure(listOf(result.message))
        }
    }
