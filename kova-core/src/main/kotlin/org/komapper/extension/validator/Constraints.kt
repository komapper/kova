package org.komapper.extension.validator

// TODO
object Constraints {
    fun <T : Comparable<T>> min(
        value: T,
        message: (ConstraintContext<T>, T) -> Message,
    ): ConstraintScope.(ConstraintContext<T>) -> ConstraintResult =
        {
            satisfies(it.input >= value, message(it, value))
        }

    fun <T : Comparable<T>> max(
        value: T,
        message: (ConstraintContext<T>, T) -> Message,
    ): ConstraintScope.(ConstraintContext<T>) -> ConstraintResult =
        {
            satisfies(it.input <= value, message(it, value))
        }

    fun <T : Any> isNull(message: (ConstraintContext<T?>) -> Message): ConstraintScope.(ConstraintContext<T?>) -> ConstraintResult =
        {
            satisfies(it.input == null, message(it))
        }

    fun <T : Any> notNull(message: (ConstraintContext<T?>) -> Message): ConstraintScope.(ConstraintContext<T?>) -> ConstraintResult =
        {
            satisfies(it.input != null, message(it))
        }
}
