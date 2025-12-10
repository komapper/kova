package org.komapper.extension.validator

import java.time.Clock
import java.time.temporal.Temporal

/**
 * Validator for temporal values (LocalDate, LocalTime, LocalDateTime) with comparison constraints.
 *
 * Supports validation for all temporal types that implement [Temporal] and [Comparable].
 *
 * Example:
 * ```kotlin
 * val dateValidator = Kova.temporal<LocalDate>().past().min(LocalDate.of(2020, 1, 1))
 * val timeValidator = Kova.temporal<LocalTime>().future()
 * val dateTimeValidator = Kova.temporal<LocalDateTime>().futureOrPresent()
 * ```
 *
 * @param T The temporal type being validated
 */
interface TemporalValidator<T> : IdentityValidator<T>
    where T : Temporal, T : Comparable<T> {
    /**
     * Adds a custom constraint to this temporal validator.
     *
     * This method allows you to define custom validation logic while maintaining
     * the temporal validator type for fluent chaining.
     *
     * Example:
     * ```kotlin
     * val validator = Kova.localDate().constrain("weekday") {
     *     satisfies(it.input.dayOfWeek !in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
     *               "Must be a weekday")
     * }
     * ```
     *
     * @param id Unique identifier for the constraint
     * @param check Constraint logic that produces a [ConstraintResult]
     * @return A new temporal validator with the constraint applied
     */
    fun TemporalValidator<T>.constrain(
        id: String,
        check: ConstraintScope<T>.(ConstraintContext<T>) -> ConstraintResult,
    ): TemporalValidator<T>

    /**
     * Validates that the temporal value is greater than or equal to [value] (inclusive).
     *
     * @param value Minimum allowed value
     * @param message Custom error message provider
     */
    fun min(
        value: T,
        message: MessageProvider = Message.resource(),
    ): TemporalValidator<T>

    /**
     * Validates that the temporal value is less than or equal to [value] (inclusive).
     *
     * @param value Maximum allowed value
     * @param message Custom error message provider
     */
    fun max(
        value: T,
        message: MessageProvider = Message.resource(),
    ): TemporalValidator<T>

    /**
     * Validates that the temporal value is strictly greater than [value] (exclusive).
     *
     * @param value The value that the input must be greater than
     * @param message Custom error message provider
     */
    fun gt(
        value: T,
        message: MessageProvider = Message.resource(),
    ): TemporalValidator<T>

    /**
     * Validates that the temporal value is greater than or equal to [value] (inclusive).
     *
     * Alias for [min].
     *
     * @param value The minimum value (inclusive)
     * @param message Custom error message provider
     */
    fun gte(
        value: T,
        message: MessageProvider = Message.resource(),
    ): TemporalValidator<T>

    /**
     * Validates that the temporal value is strictly less than [value] (exclusive).
     *
     * @param value The value that the input must be less than
     * @param message Custom error message provider
     */
    fun lt(
        value: T,
        message: MessageProvider = Message.resource(),
    ): TemporalValidator<T>

    /**
     * Validates that the temporal value is less than or equal to [value] (inclusive).
     *
     * Alias for [max].
     *
     * @param value The maximum value (inclusive)
     * @param message Custom error message provider
     */
    fun lte(
        value: T,
        message: MessageProvider = Message.resource(),
    ): TemporalValidator<T>

    /**
     * Validates that the temporal value is in the future (strictly greater than now).
     *
     * @param message Custom error message provider
     */
    fun future(message: MessageProvider = Message.resource()): TemporalValidator<T>

    /**
     * Validates that the temporal value is in the future or present (greater than or equal to now).
     *
     * @param message Custom error message provider
     */
    fun futureOrPresent(message: MessageProvider = Message.resource()): TemporalValidator<T>

    /**
     * Validates that the temporal value is in the past (strictly less than now).
     *
     * @param message Custom error message provider
     */
    fun past(message: MessageProvider = Message.resource()): TemporalValidator<T>

    /**
     * Validates that the temporal value is in the past or present (less than or equal to now).
     *
     * @param message Custom error message provider
     */
    fun pastOrPresent(message: MessageProvider = Message.resource()): TemporalValidator<T>

    /**
     * Combines this validator with another validator using logical AND.
     *
     * Alias for [and].
     *
     * @param other The validator to combine with
     */
    operator fun plus(other: IdentityValidator<T>): TemporalValidator<T> = and(other)

    /**
     * Combines this validator with another validator using logical AND.
     *
     * Both validators must pass for the combined validator to pass.
     *
     * @param other The validator to combine with
     */
    infix fun and(other: IdentityValidator<T>): TemporalValidator<T>

    /**
     * Combines this validator with another validator using logical OR.
     *
     * Either validator can pass for the combined validator to pass.
     *
     * @param other The validator to combine with
     */
    infix fun or(other: IdentityValidator<T>): TemporalValidator<T>
}

/**
 * Creates a new temporal validator.
 *
 * @param T The temporal type being validated
 * @param validator Validator to wrap
 * @param clock Clock used for temporal comparisons (past, future, etc.)
 * @param temporalNow Strategy for obtaining the current temporal value
 */
internal fun <T> TemporalValidator(
    validator: IdentityValidator<T> = Validator.success(),
    clock: Clock = Clock.systemDefaultZone(),
    temporalNow: TemporalNow<T>,
): TemporalValidator<T> where T : Temporal, T : Comparable<T> =
    object : TemporalValidator<T> {
        override fun execute(
            input: T,
            context: ValidationContext,
        ): ValidationResult<T> = validator.execute(input, context)

        private fun TemporalValidator(validator: IdentityValidator<T>): TemporalValidator<T> =
            TemporalValidator(
                validator = validator,
                clock = clock,
                temporalNow = temporalNow,
            )

        override fun TemporalValidator<T>.constrain(
            id: String,
            check: ConstraintScope<T>.(ConstraintContext<T>) -> ConstraintResult,
        ): TemporalValidator<T> = TemporalValidator(validator.constrain(id, check))

        override fun min(
            value: T,
            message: MessageProvider,
        ): TemporalValidator<T> = TemporalValidator(validator.min(value, message))

        override fun max(
            value: T,
            message: MessageProvider,
        ): TemporalValidator<T> = TemporalValidator(validator.max(value, message))

        override fun gt(
            value: T,
            message: MessageProvider,
        ): TemporalValidator<T> = TemporalValidator(validator.gt(value, message))

        override fun gte(
            value: T,
            message: MessageProvider,
        ): TemporalValidator<T> = TemporalValidator(validator.gte(value, message))

        override fun lt(
            value: T,
            message: MessageProvider,
        ): TemporalValidator<T> = TemporalValidator(validator.lt(value, message))

        override fun lte(
            value: T,
            message: MessageProvider,
        ): TemporalValidator<T> = TemporalValidator(validator.lte(value, message))

        override fun future(message: MessageProvider): TemporalValidator<T> =
            constrain("kova.temporal.future") {
                satisfies(it.input > temporalNow.now(clock), message())
            }

        override fun futureOrPresent(message: MessageProvider): TemporalValidator<T> =
            constrain("kova.temporal.futureOrPresent") {
                satisfies(it.input >= temporalNow.now(clock), message())
            }

        override fun past(message: MessageProvider): TemporalValidator<T> =
            constrain("kova.temporal.past") {
                satisfies(it.input < temporalNow.now(clock), message())
            }

        override fun pastOrPresent(message: MessageProvider): TemporalValidator<T> =
            constrain("kova.temporal.pastOrPresent") {
                satisfies(it.input <= temporalNow.now(clock), message())
            }

        override infix fun and(other: IdentityValidator<T>): TemporalValidator<T> = TemporalValidator(validator.and(other))

        override infix fun or(other: IdentityValidator<T>): TemporalValidator<T> = TemporalValidator(validator.or(other))
    }
