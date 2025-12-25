package org.komapper.extension.validator

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
@IgnorableReturnValue
context(c: Validation, _: Accumulate)
inline fun <T, R> T.constrain(
    id: String,
    check: context(Validation, Accumulate) (T) -> R,
) = accumulating {
    mapEachMessage({ it.logAndAddDetails(this, id) }) {
        val result = check(this)
        log {
            LogEntry.Satisfied(
                constraintId = id,
                root = c.root,
                path = c.path.fullName,
                input = this,
            )
        }
        result
    }
}

context(_: Validation)
fun Message.logAndAddDetails(
    input: Any?,
    id: String,
): Message {
    log {
        LogEntry.Violated(
            constraintId = id,
            root = root,
            path = path.fullName,
            input = input,
            args = if (this is Message.Resource) args.asList() else emptyList(),
        )
    }
    return withDetails(input, id)
}

context(_: Accumulate)
inline fun <R> mapEachMessage(
    noinline transform: (Message) -> Message,
    block: context(Accumulate) () -> R,
): R = block { accumulate(it.map(transform)) }
