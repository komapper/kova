package org.komapper.extension.validator

/**
 * Interface for validators that support custom constraints.
 *
 * Allows adding custom validation logic to validators beyond their built-in methods.
 * This is commonly used to add business logic or complex validation rules.
 *
 * Example adding a custom constraint to a NumberValidator:
 * ```kotlin
 * val evenNumberValidator = Kova.int()
 *     .constrain("even") { context ->
 *         satisfies(
 *             context.input % 2 == 0,
 *             "Number must be even"
 *         )
 *     }
 * ```
 *
 * Example in ObjectSchema for cross-field validation:
 * ```kotlin
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
 * @param T The type being validated
 * @param R The return type (typically the validator itself for chaining)
 */
interface Constrainable<T, R> {
    /**
     * Adds a custom constraint to this validator.
     *
     * The constraint check function receives a [ConstraintScope] as receiver,
     * providing the `satisfies()` helper method for evaluating conditions.
     *
     * @param id A unique identifier for this constraint
     * @param check The validation logic that returns a [ConstraintResult]
     * @return A new validator with the constraint added (for method chaining)
     */
    fun constrain(
        id: String,
        check: ConstraintScope.(ConstraintContext<T>) -> ConstraintResult,
    ): R
}
