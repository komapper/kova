package org.komapper.extension.validator

// TODO
object Constraints {
    fun <T> isNull(message: (ConstraintContext<T?>) -> Message): (ConstraintContext<T?>) -> ConstraintResult =
        {
            Constraint.satisfies(it.input == null, message(it))
        }

    fun <T> isNotNull(message: (ConstraintContext<T?>) -> Message): (ConstraintContext<T?>) -> ConstraintResult =
        { ctx ->
            Constraint.satisfies(ctx.input != null, message(ctx))
        }

    fun <T : Comparable<T>> min(
        value: T,
        message: (ConstraintContext<T>, T) -> Message,
    ): (ConstraintContext<T>) -> ConstraintResult =
        { ctx ->
            Constraint.satisfies(ctx.input >= value, message(ctx, value))
        }

    fun <T : Comparable<T>> max(
        value: T,
        message: (ConstraintContext<T>, T) -> Message,
    ): (ConstraintContext<T>) -> ConstraintResult =
        { ctx ->
            Constraint.satisfies(ctx.input <= value, message(ctx, value))
        }
}
