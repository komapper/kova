package org.komapper.extension.validator

import org.komapper.extension.validator.ValidationResult.Failure
import org.komapper.extension.validator.ValidationResult.Success

/**
 * Adds a custom constraint to this validator.
 *
 * This is a fundamental building block for creating custom validation rules.
 * The constraint is chained to the existing validator, executing after it succeeds.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().constrain("alphanumeric") {
 *     satisfies(it.all { c -> c.isLetterOrDigit() }, "Must be alphanumeric")
 * }
 * ```
 *
 * @param id Unique identifier for the constraint
 * @param check Constraint logic that produces a [ValidationResult]
 * @return A new validator with the constraint applied
 */
context(c: ValidationContext)
inline fun <T> T.constrain(
    id: String,
    check: Constraint<T>,
): ValidationResult<Unit> {
    val result = withMessageDetails(this, id) { check(this@constrain) }
    when (result) {
        is Success ->
            log {
                LogEntry.Satisfied(
                    constraintId = id,
                    root = c.root,
                    path = c.path.fullName,
                    input = this,
                )
            }

        is Failure -> for (message in result.messages) {
            log {
                LogEntry.Violated(
                    constraintId = id,
                    root = message.root,
                    path = message.path.fullName,
                    input = this,
                    args = if (message is Message.Resource) message.args.asList() else emptyList(),
                )
            }
        }
    }
    return result.accumulateMessages()
}

context(c: ValidationContext)
inline fun <R> withMessageDetails(
    input: Any?,
    constraintId: String,
    block: context(ValidationContext) () -> ValidationResult<R>,
): ValidationResult<R> = mapEachMessage({ it.withDetails(input, constraintId) }, block)

context(c: ValidationContext)
inline fun <R> mapEachMessage(
    noinline transform: (Message) -> Message,
    block: context(ValidationContext) () -> ValidationResult<R>,
): ValidationResult<R> =
    when (val result = block(c.copy(accumulate = { messages -> c.accumulate(messages.map(transform)) }))) {
        is Success -> result
        is Failure -> Failure(result.messages.map(transform))
    }
