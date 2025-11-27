package org.komapper.extension.validator

data class Constraint<T>(
    val key: String,
    val check: ConstraintScope.(ConstraintContext<T>) -> ConstraintResult,
) {
    fun apply(context: ConstraintContext<T>): ConstraintResult {
        val scope = ConstraintScope()
        return scope.check(context)
    }

    companion object {
        fun <T> satisfied(): Constraint<T> = Constraint("kova.always.satisfied") { ConstraintResult.Satisfied }
    }
}

data class ConstraintContext<T>(
    val input: T,
    val root: String = "",
    val path: String = "",
    val failFast: Boolean = false,
    val key: String = "",
)

sealed interface ConstraintResult {
    object Satisfied : ConstraintResult

    data class Violated(
        val message: Message,
    ) : ConstraintResult {
        constructor(content: String) : this(Message.Text(content = content))
    }
}

class ConstraintScope {
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
