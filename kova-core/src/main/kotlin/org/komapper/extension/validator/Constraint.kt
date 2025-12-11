package org.komapper.extension.validator

import java.time.Clock

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
    val check: ConstraintScope<T>.(ConstraintContext<T>) -> ConstraintResult,
) {
    /**
     * Applies this constraint to the given context.
     *
     * @param context The constraint context containing the input value
     * @return The result of the constraint check
     */
    fun apply(context: ConstraintContext<T>): ConstraintResult {
        val scope = ConstraintScope(context)
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

    /**
     * The clock used for temporal validation constraints.
     *
     * This clock is used by temporal extension functions (past, future, pastOrPresent, futureOrPresent)
     * to determine the current time for comparison. Defaults to the system clock, but can be configured
     * via [ValidationConfig] for testing purposes.
     *
     * Example usage in temporal constraints:
     * ```kotlin
     * constrain("kova.temporal.future") { ctx ->
     *     satisfies(ctx.input > LocalDate.now(ctx.clock), "Date must be in the future")
     * }
     * ```
     */
    val clock: Clock get() = validationContext.clock
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
class ConstraintScope<T>(
    private val context: ConstraintContext<T>,
) {
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
     * val messageProvider = Message.resource()
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
        message: (ConstraintContext<*>) -> Message,
    ): ConstraintResult =
        if (condition) {
            ConstraintResult.Satisfied
        } else {
            ConstraintResult.Violated(message(context))
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
    ): ConstraintResult =
        if (condition) {
            ConstraintResult.Satisfied
        } else {
            ConstraintResult.Violated(message)
        }
}

fun <T> ConstraintContext<T>.createMessageContext(args: List<Any?>): MessageContext<T> = MessageContext(args, this)

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
fun <T> ConstraintContext<T>.text(content: String): Message {
    val messageContext = createMessageContext(emptyList())
    return Message.Text(messageContext, content)
}

/**
 * Creates a resource-based validation message.
 *
 * Use this method to create internationalized messages that load text from `kova.properties`.
 * The message key is determined by the constraint ID in the validation context.
 * Arguments can be provided for message interpolation.
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
 * @param args Arguments to be interpolated into the message template
 * @return A [Message.Resource] instance configured with the provided arguments
 */
fun <T> ConstraintContext<T>.resource(vararg args: Any?): Message {
    val messageContext = createMessageContext(args.toList())
    return Message.Resource(messageContext)
}
