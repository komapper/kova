package org.komapper.extension.validator

fun interface Constraint<T> {
    fun apply(context: ConstraintContext<T>): ConstraintResult

    companion object {
        fun satisfies(
            condition: Boolean,
            message: Message,
        ): ConstraintResult =
            if (condition) {
                ConstraintResult.Satisfied
            } else {
                ConstraintResult.Violated(message)
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
        constructor(content: String) : this(Message.Text(content = content))
    }
}
