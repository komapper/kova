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
 */
typealias Constraint<T> = Validator<T, Unit>

/**
 * Result of applying a constraint to a value.
 *
 * Either [ValidationResult.Success] if the constraint passes, or [ValidationResult.Failure] if it fails.
 */
typealias ConstraintResult = ValidationResult<Unit>

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
fun ValidationContext.text(content: String): Message = Message.Text(root, path, content)
