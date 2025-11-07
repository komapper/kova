package org.komapper.extension.validator

fun interface Constraint<T> {
    fun apply(context: ConstraintContext<T>): ConstraintResult

    companion object {
        fun check(
            predicate: () -> Boolean,
            onViolated: () -> Message,
        ): ConstraintResult =
            if (predicate()) {
                ConstraintResult.Satisfied
            } else {
                ConstraintResult.Violated(onViolated())
            }
    }
}

data class ConstraintContext<T>(
    val input: T,
    val root: String = "",
    val path: String = "",
    val failFast: Boolean = false,
)

sealed interface ConstraintResult {
    object Satisfied : ConstraintResult

    data class Violated(
        val message: Message,
    ) : ConstraintResult {
        constructor(content: String) : this(Message.Text(content))
    }
}
