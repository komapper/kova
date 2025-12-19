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
    val check: ConstraintContext<T>.() -> ConstraintResult,
) {
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
class ConstraintContext<T>(
    val input: T,
    val constraintId: String,
    validationContext: ValidationContext
): ValidationContext by validationContext {
    /**
     * Evaluates a condition and returns the appropriate constraint result.
     *
     * Returns [ConstraintResult.Satisfied] if the condition is true,
     * or [ConstraintResult.Violated] with the given message if false.
     *
     * Example with message factory function:
     * ```kotlin
     * satisfies(
     *     value > 0
     * ) { ctx ->
     *     Message.Resource(ctx.createMessageContext(listOf(value)))
     * }
     * ```
     *
     * Example with MessageProvider:
     * ```kotlin
     * val messageProvider = MessageProvider.resource()
     * satisfies(
     *     value > 0,
     *     messageProvider(value)
     * )
     * ```
     *
     * @param condition The condition to evaluate
     * @param message A function that accepts a ConstraintContext and returns a Message
     * @return The constraint result
     */
    fun satisfies(
        condition: Boolean,
        message: MessageProvider<T>,
    ): ConstraintResult =
        if (condition) {
            ConstraintResult.Satisfied
        } else {
            ConstraintResult.Violated(message())
        }

    /**
     * Evaluates a condition and returns the appropriate constraint result.
     *
     * Returns [ConstraintResult.Satisfied] if the condition is true,
     * or [ConstraintResult.Violated] with the given message if false.
     *
     * Example with text message:
     * ```kotlin
     * satisfies(
     *     value > 0,
     *     context.text("Value must be positive")
     * )
     * ```
     *
     * Example with resource message:
     * ```kotlin
     * satisfies(
     *     value > 0,
     *     context.resource(value)
     * )
     * ```
     *
     * @param condition The condition to evaluate
     * @param message The Message to use if the condition is false
     * @return The constraint result
     */
    fun satisfies(
        condition: Boolean,
        message: Message,
    ): ConstraintResult = satisfies(condition) { message }
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
 * Creates a text-based validation message.
 *
 * Use this method to create simple text messages for constraint violations.
 * The message will include the current validation context (root, path, constraint ID).
 *
 * Example usage in a constraint:
 * ```kotlin
 * constrain("positive") { context ->
 *     satisfies(
 *         context.input > 0,
 *         context.text("Value must be positive")
 *     )
 * }
 * ```
 *
 * @param content The text content of the error message
 * @return A [Message.Text] instance with the given content
 */
fun <T> ConstraintContext<T>.text(content: String): Message = Message.Text(this, content)

/**
 * Creates a resource-based validation message.
 *
 * Use this method to create internationalized messages that load text from `kova.properties`.
 * The message key is determined by the constraint ID in the validation context.
 * Arguments are provided as a vararg and automatically converted to indexed pairs for
 * MessageFormat substitution (i.e., the first argument becomes {0}, second becomes {1}, etc.).
 *
 * Example usage in a constraint:
 * ```kotlin
 * constrain("kova.number.min") { context ->
 *     val minValue = 0
 *     satisfies(
 *         context.input >= minValue,
 *         context.resource(minValue)
 *     )
 * }
 * ```
 *
 * The corresponding entry in `kova.properties` would be:
 * ```
 * kova.number.min=The value must be greater than or equal to {0}.
 * ```
 *
 * For multiple arguments:
 * ```kotlin
 * constrain("kova.number.range") { context ->
 *     val minValue = 0
 *     val maxValue = 100
 *     satisfies(
 *         context.input in minValue..maxValue,
 *         context.resource(minValue, maxValue)
 *     )
 * }
 * ```
 *
 * With corresponding resource:
 * ```
 * kova.number.range=The value must be between {0} and {1}.
 * ```
 *
 * @param args Arguments to be interpolated into the message template using MessageFormat
 * @return A [Message.Resource] instance configured with the provided arguments
 */
fun <T> ConstraintContext<T>.resource(vararg args: Any?): Message =
    Message.Resource(this, args = args)
