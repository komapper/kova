package org.komapper.extension.validator

object Constraints {
    fun <T : Comparable<T>> min(
        value: T,
        message: MessageProvider,
    ): ConstraintScope<T>.(ConstraintContext<T>) -> ConstraintResult =
        {
            satisfies(it.input >= value, message(it.input, value))
        }

    fun <T : Comparable<T>> max(
        value: T,
        message: MessageProvider,
    ): ConstraintScope<T>.(ConstraintContext<T>) -> ConstraintResult =
        {
            satisfies(it.input <= value, message(it.input, value))
        }

    fun <T : Comparable<T>> gt(
        value: T,
        message: MessageProvider,
    ): ConstraintScope<T>.(ConstraintContext<T>) -> ConstraintResult =
        {
            satisfies(it.input > value, message(it.input, value))
        }

    fun <T : Comparable<T>> gte(
        value: T,
        message: MessageProvider,
    ): ConstraintScope<T>.(ConstraintContext<T>) -> ConstraintResult =
        {
            satisfies(it.input >= value, message(it.input, value))
        }

    fun <T : Comparable<T>> lt(
        value: T,
        message: MessageProvider,
    ): ConstraintScope<T>.(ConstraintContext<T>) -> ConstraintResult =
        {
            satisfies(it.input < value, message(it.input, value))
        }

    fun <T : Comparable<T>> lte(
        value: T,
        message: MessageProvider,
    ): ConstraintScope<T>.(ConstraintContext<T>) -> ConstraintResult =
        {
            satisfies(it.input <= value, message(it.input, value))
        }

    fun <T : Any> isNull(message: MessageProvider): ConstraintScope<T?>.(ConstraintContext<T?>) -> ConstraintResult =
        {
            satisfies(it.input == null, message(it.input))
        }

    fun <T : Any> notNull(message: MessageProvider): ConstraintScope<T?>.(ConstraintContext<T?>) -> ConstraintResult =
        {
            satisfies(it.input != null, message(it.input))
        }
}
