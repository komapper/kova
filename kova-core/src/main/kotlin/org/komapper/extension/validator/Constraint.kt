package org.komapper.extension.validator

/**
 * Represents a validation constraint that can be applied to a value.
 *
 * A constraint is a function that receives a value within a [Validation] context and
 * performs validation logic on it. Constraints are commonly used with collection validators
 * like [onEach], property validation within schema blocks, and custom validation logic.
 *
 * Example with collection validation:
 * ```kotlin
 * tryValidate {
 *     onEach(listOf(1, -2, 3)) { value ->
 *         positive(value)
 *     }
 * }
 * ```
 *
 * Example with schema validation:
 * ```kotlin
 * data class Period(val startDate: LocalDate, val endDate: LocalDate)
 *
 * fun Validation.validate(period: Period) {
 *     period.schema {
 *         period::startDate { pastOrPresent(it) }
 *         period::endDate { futureOrPresent(it) }
 *         period.constrain("period") {
 *             satisfies(it.startDate <= it.endDate) {
 *                 text("Start date must be before or equal to end date")
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * @param T The type of value this constraint validates
 */
typealias Constraint<T> = Validation.(T) -> Unit

/**
 * Creates a text-based validation message.
 *
 * Use this method to create simple text messages for constraint violations
 * without i18n support. The message will include the current validation context
 * (root, path, constraint ID).
 *
 * Example usage in a constraint:
 * ```kotlin
 * tryValidate {
 *     10.constrain("positive") {
 *         satisfies(it > 0) { text("Value must be positive") }
 *     }
 * }
 * ```
 *
 * Example with schema validation:
 * ```kotlin
 * data class Period(val startDate: LocalDate, val endDate: LocalDate)
 *
 * fun Validation.validate(period: Period) {
 *     period.schema {
 *         period::startDate { pastOrPresent(it) }
 *         period::endDate { futureOrPresent(it) }
 *         period.constrain("period") {
 *             satisfies(it.startDate <= it.endDate) {
 *                 text("Start date must be before or equal to end date")
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * @param content The text content of the error message
 * @return A [Message.Text] instance with the given content
 */
fun Validation.text(content: String): Message = Message.Text("", root, path, content, null)
