package org.komapper.extension.validator

// TODO
object Constraints {
    fun <T> isNull(message: (ConstraintContext<T?>) -> Message): ConstraintScope.(ConstraintContext<T?>) -> ConstraintResult =
        {
            satisfies(it.input == null, message(it))
        }

    fun <T> isNotNull(message: (ConstraintContext<T?>) -> Message): ConstraintScope.(ConstraintContext<T?>) -> ConstraintResult =
        { ctx ->
            satisfies(ctx.input != null, message(ctx))
        }

    fun <T : Comparable<T>> min(
        value: T,
        message: (ConstraintContext<T>, T) -> Message,
    ): ConstraintScope.(ConstraintContext<T>) -> ConstraintResult =
        { ctx ->
            satisfies(ctx.input >= value, message(ctx, value))
        }

    fun <T : Comparable<T>> max(
        value: T,
        message: (ConstraintContext<T>, T) -> Message,
    ): ConstraintScope.(ConstraintContext<T>) -> ConstraintResult =
        { ctx ->
            satisfies(ctx.input <= value, message(ctx, value))
        }
}
