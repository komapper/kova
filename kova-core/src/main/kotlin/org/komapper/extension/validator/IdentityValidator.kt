package org.komapper.extension.validator

import org.komapper.extension.validator.ValidationResult.Failure
import org.komapper.extension.validator.ValidationResult.Success

typealias IdentityValidator<T> = Validator<T, T>

fun <T> IdentityValidator<T>.literal(
    value: T,
    message: MessageProvider1<T, T> = Message.resource1("kova.literal.single"),
) = constrain(message.id) {
    satisfies(it.input == value, message(it, value))
}

fun <T> IdentityValidator<T>.literal(
    values: List<T>,
    message: MessageProvider1<T, List<T>> = Message.resource1("kova.literal.list"),
) = constrain(message.id) {
    satisfies(it.input in values, message(it, values))
}

fun <T> IdentityValidator<T>.constrain(
    id: String,
    check: ConstraintScope.(ConstraintContext<T>) -> ConstraintResult,
) = IdentityValidator<T> { input, context ->
    val constraint = Constraint(id, check)
    val next = ConstraintValidator(constraint)
    chain(next).execute(input, context)
}

fun <T> IdentityValidator<T>.onlyIf(condition: (T) -> Boolean) =
    IdentityValidator<T> { input, context ->
        if (condition(input)) {
            execute(input, context)
        } else {
            Success(input, context)
        }
    }

/**
 * Chains two validators where the second validator receives the output of the first if it succeeds.
 *
 * **Key characteristic**: Both input and output must be the same type (T).
 * This is designed for validators that don't transform the type.
 *
 * Unlike [then]:
 * - Requires IN == OUT (same type)
 * - If the first validator fails and failFast is disabled, both validators
 *   are executed and their failures are combined
 *
 * Example with same type:
 * ```kotlin
 * val normalizeValidator = Kova.string().map { it.trim() }
 * val validateValidator = Kova.string().min(1).max(10)
 * val validator = normalizeValidator.chain(validateValidator)
 * // Input: String, Output: String (same type)
 * ```
 *
 * @param next The validator to apply next
 * @return A new validator that chains both validators
 */
fun <T> IdentityValidator<T>.chain(next: IdentityValidator<T>): IdentityValidator<T> =
    IdentityValidator { input, context ->
        val context = context.addLog("Validator.chain")
        when (val result = this.execute(input, context)) {
            is Success -> {
                next.execute(result.value, result.context)
            }

            is Failure -> {
                if (context.failFast) {
                    result
                } else {
                    result + next.execute(input, context)
                }
            }
        }
    }

