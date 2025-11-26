package org.komapper.extension.validator

object Constraints {
    fun <T> isNull(message: (ConstraintContext<T?>) -> Message = Message.resource0("kova.nullable.isNull")): Constraint<T?> =
        Constraint {
            Constraint.satisfies(it.input == null, message(it))
        }

    fun <T> isNotNull(message: (ConstraintContext<T?>) -> Message = Message.resource0("kova.nullable.isNotNull")): Constraint<T?> =
        Constraint { ctx ->
            Constraint.satisfies(ctx.input != null, message(ctx))
        }

    fun <T : Comparable<T>> min(
        value: T,
        message: (ConstraintContext<T>, T) -> Message,
    ): Constraint<T> =
        Constraint { ctx ->
            Constraint.satisfies(ctx.input >= value, message(ctx, value))
        }

    fun <T : Comparable<T>> max(
        value: T,
        message: (ConstraintContext<T>, T) -> Message,
    ): Constraint<T> =
        Constraint { ctx ->
            Constraint.satisfies(ctx.input <= value, message(ctx, value))
        }
}
