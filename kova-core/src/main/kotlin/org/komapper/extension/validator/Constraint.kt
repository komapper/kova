package org.komapper.extension.validator

/**
 * Represents a validation constraint that can be applied to a value.
 *
 * Constraints are used to define custom validation rules that go beyond simple type checks.
 * They are commonly used in ObjectSchema for object-level validation that involves multiple fields.
 *
 * Example of a custom constraint:
 * ```kotlin
 * data class Period(val startDate: LocalDate, val endDate: LocalDate)
 *
 * object PeriodSchema : ObjectSchema<Period>({
 *     constrain("dateRange") { context ->
 *         satisfies(
 *             context.input.startDate <= context.input.endDate,
 *             "Start date must be before or equal to end date"
 *         )
 *     }
 * }) {
 *     val startDate = Period::startDate { Kova.localDate() }
 *     val endDate = Period::endDate { Kova.localDate() }
 * }
 * ```
 *
 * @param T The type of value this constraint validates
 * @property id A unique identifier for this constraint
 * @property check The validation logic that returns a [ConstraintResult]
 */
data class Constraint<T>(
    val id: String,
    val check: ConstraintScope.(ConstraintContext<T>) -> ConstraintResult,
) {
    /**
     * Applies this constraint to the given context.
     *
     * @param context The constraint context containing the input value
     * @return The result of the constraint check
     */
    fun apply(context: ConstraintContext<T>): ConstraintResult {
        val scope = ConstraintScope()
        return scope.check(context)
    }

    companion object {
        /**
         * Creates a constraint that is always satisfied.
         *
         * Used internally as a default constraint.
         */
        fun <T> satisfied(): Constraint<T> = Constraint("kova.satisfied") { ConstraintResult.Satisfied }
    }
}

/**
 * Context provided to constraint validation logic.
 *
 * Contains the input value being validated and access to the validation context.
 *
 * Example usage in a custom constraint:
 * ```kotlin
 * constrain("positive") { context ->
 *     satisfies(
 *         context.input > 0,
 *         "Value ${context.input} must be positive"
 *     )
 * }
 * ```
 *
 * @param T The type of the input value
 * @property input The value being validated
 * @property constraintId Optional identifier for this specific constraint check
 * @property validationContext The broader validation context
 */
data class ConstraintContext<T>(
    val input: T,
    val constraintId: String = "",
    val validationContext: ValidationContext = ValidationContext(),
) {
    /** The root object's qualified class name */
    val root: String get() = validationContext.root

    /** The current validation path */
    val path: Path get() = validationContext.path

    /** Whether validation should stop at the first failure */
    val failFast: Boolean get() = validationContext.failFast
}

/**
 * Result of applying a constraint to a value.
 *
 * Either [Satisfied] if the constraint passes, or [Violated] if it fails.
 */
sealed interface ConstraintResult {
    /**
     * Indicates that the constraint was satisfied.
     */
    object Satisfied : ConstraintResult

    /**
     * Indicates that the constraint was violated.
     *
     * @property message The error message describing why the constraint failed
     */
    data class Violated(
        val message: Message,
    ) : ConstraintResult
}

/**
 * Scope available within constraint validation logic.
 *
 * Provides helper methods for evaluating conditions and producing constraint results.
 *
 * Example usage:
 * ```kotlin
 * constrain("range") { context ->
 *     satisfies(
 *         context.input in 1..100,
 *         "Value must be between 1 and 100"
 *     )
 * }
 * ```
 */
class ConstraintScope {
    /**
     * Evaluates a condition and returns the appropriate constraint result.
     *
     * Returns [ConstraintResult.Satisfied] if the condition is true,
     * or [ConstraintResult.Violated] with the given message if false.
     *
     * Example with Message object:
     * ```kotlin
     * satisfies(
     *     value > 0,
     *     Message.Resource("custom.positive", value)
     * )
     * ```
     *
     * @param condition The condition to evaluate
     * @param message The error message to use if the condition is false
     * @return The constraint result
     */
    fun satisfies(
        condition: Boolean,
        message: Message,
    ): ConstraintResult =
        if (condition) {
            ConstraintResult.Satisfied
        } else {
            ConstraintResult.Violated(message)
        }

    /**
     * Evaluates a condition and returns the appropriate constraint result.
     *
     * Returns [ConstraintResult.Satisfied] if the condition is true,
     * or [ConstraintResult.Violated] with the given text message if false.
     *
     * Example with simple string:
     * ```kotlin
     * satisfies(
     *     value > 0,
     *     "Value must be positive"
     * )
     * ```
     *
     * @param condition The condition to evaluate
     * @param message The error message text to use if the condition is false
     * @return The constraint result
     */
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
