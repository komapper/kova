package org.komapper.extension.validator

data class Constraint<T>(
    val id: String,
    val check: ConstraintScope.(ConstraintContext<T>) -> ConstraintResult,
) {
    fun apply(context: ConstraintContext<T>): ConstraintResult {
        val scope = ConstraintScope()
        return scope.check(context)
    }

    companion object {
        fun <T> satisfied(): Constraint<T> = Constraint("kova.satisfied") { ConstraintResult.Satisfied }
    }
}

data class ConstraintContext<T>(
    val input: T,
    val root: String = "",
    val path: Path = Path(name = "", obj = null, parent = null),
    val failFast: Boolean = false,
    val constraintId: String = "",
)

sealed interface ConstraintResult {
    object Satisfied : ConstraintResult

    data class Violated(
        val message: Message,
    ) : ConstraintResult
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

    fun satisfies(
        condition: Boolean,
        message: String,
    ): ConstraintResult =
        if (condition) {
            ConstraintResult.Satisfied
        } else {
            ConstraintResult.Violated(Message.Text(content = message))
        }
}
