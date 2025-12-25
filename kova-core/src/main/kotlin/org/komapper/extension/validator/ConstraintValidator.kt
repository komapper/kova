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
context(c: Validation, _: Accumulate)
inline fun <T> T.constrain(
    id: String,
    check: Constraint<T>,
) {
    val result =
        accumulating {
            mapEachMessage({
                log {
                    LogEntry.Violated(
                        constraintId = id,
                        root = it.root,
                        path = it.path.fullName,
                        input = this,
                        args = if (it is Message.Resource) it.args.asList() else emptyList(),
                    )
                }
                it.withDetails(this, id)
            }) { check(this) }
        }
    when (result) {
        is Accumulate.Ok ->
            log {
                LogEntry.Satisfied(
                    constraintId = id,
                    root = c.root,
                    path = c.path.fullName,
                    input = this,
                )
            }
        else -> {}
    }
}

context(_: Accumulate)
inline fun <R> mapEachMessage(
    noinline transform: (Message) -> Message,
    block: context(Accumulate) () -> R,
): R = block { accumulate(it.map(transform)) }
