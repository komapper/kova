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
interface TemporalValidator<T> :
    Validator<T, T>,
    Constrainable<T, TemporalValidator<T>>
    where T : Temporal, T : Comparable<T> {
    /**
     * Validates that the temporal value is greater than or equal to [value] (inclusive).
     *
     * @param value Minimum allowed value
     * @param message Custom error message provider
     */
    fun min(
        value: T,
        message: MessageProvider1<T, T> = Message.resource1("kova.temporal.min"),
    ): TemporalValidator<T>

    /**
     * Validates that the temporal value is less than or equal to [value] (inclusive).
     *
     * @param value Maximum allowed value
     * @param message Custom error message provider
     */
    fun max(
        value: T,
        message: MessageProvider1<T, T> = Message.resource1("kova.temporal.max"),
    ): TemporalValidator<T>

    /**
     * Validates that the temporal value is strictly greater than [value] (exclusive).
     *
     * @param value The value that the input must be greater than
     * @param message Custom error message provider
     */
    fun gt(
        value: T,
        message: MessageProvider1<T, T> = Message.resource1("kova.temporal.gt"),
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
        message: MessageProvider1<T, T> = Message.resource1("kova.temporal.gte"),
    ): TemporalValidator<T>

    /**
     * Validates that the temporal value is strictly less than [value] (exclusive).
     *
     * @param value The value that the input must be less than
     * @param message Custom error message provider
     */
    fun lt(
        value: T,
        message: MessageProvider1<T, T> = Message.resource1("kova.temporal.lt"),
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
        message: MessageProvider1<T, T> = Message.resource1("kova.temporal.lte"),
    ): TemporalValidator<T>

    /**
     * Validates that the temporal value is in the future (strictly greater than now).
     *
     * @param message Custom error message provider
     */
    fun future(message: MessageProvider0<T> = Message.resource0("kova.temporal.future")): TemporalValidator<T>

    /**
     * Validates that the temporal value is in the future or present (greater than or equal to now).
     *
     * @param message Custom error message provider
     */
    fun futureOrPresent(message: MessageProvider0<T> = Message.resource0("kova.temporal.futureOrPresent")): TemporalValidator<T>

    /**
     * Validates that the temporal value is in the past (strictly less than now).
     *
     * @param message Custom error message provider
     */
    fun past(message: MessageProvider0<T> = Message.resource0("kova.temporal.past")): TemporalValidator<T>

    /**
     * Validates that the temporal value is in the past or present (less than or equal to now).
     *
     * @param message Custom error message provider
     */
    fun pastOrPresent(message: MessageProvider0<T> = Message.resource0("kova.temporal.pastOrPresent")): TemporalValidator<T>

    operator fun plus(other: Validator<T, T>): TemporalValidator<T>

    infix fun and(other: Validator<T, T>): TemporalValidator<T>

    infix fun or(other: Validator<T, T>): TemporalValidator<T>

    fun chain(other: Validator<T, T>): TemporalValidator<T>
}

/**
 * Creates a new temporal validator.
 *
 * @param T The temporal type being validated
 * @param name Debug name for the validator
 * @param prev Previous validator in the chain
 * @param constraint Constraint to apply
 * @param clock Clock used for temporal comparisons (past, future, etc.)
 * @param temporalNow Strategy for obtaining the current temporal value
 */
fun <T> TemporalValidator(
    name: String = "empty",
    prev: Validator<T, T> = EmptyValidator(),
    constraint: Constraint<T> = Constraint.satisfied(),
    clock: Clock = Clock.systemDefaultZone(),
    temporalNow: TemporalNow<T>,
): TemporalValidator<T> where T : Temporal, T : Comparable<T> = TemporalValidatorImpl(name, prev, constraint, clock, temporalNow)

private class TemporalValidatorImpl<T>(
    private val name: String,
    private val prev: Validator<T, T>,
    private val constraint: Constraint<T>,
    private val clock: Clock,
    private val temporalNow: TemporalNow<T>,
) : TemporalValidator<T>
    where T : Temporal, T : Comparable<T> {
    private val next: ConstraintValidator<T> = ConstraintValidator(constraint)

    override fun execute(
        input: T,
        context: ValidationContext,
    ): ValidationResult<T> {
        val context = context.addLog(toString())
        return prev.chain(next).execute(input, context)
    }

    override fun constrain(
        id: String,
        check: ConstraintScope.(ConstraintContext<T>) -> ConstraintResult,
    ): TemporalValidator<T> =
        TemporalValidatorImpl(name = id, prev = this, constraint = Constraint(id, check), clock = clock, temporalNow = temporalNow)

    override fun min(
        value: T,
        message: MessageProvider1<T, T>,
    ): TemporalValidator<T> = constrain(message.key, Constraints.min(value, message))

    override fun max(
        value: T,
        message: MessageProvider1<T, T>,
    ): TemporalValidator<T> = constrain(message.key, Constraints.max(value, message))

    override fun gt(
        value: T,
        message: MessageProvider1<T, T>,
    ): TemporalValidator<T> = constrain(message.key, Constraints.gt(value, message))

    override fun gte(
        value: T,
        message: MessageProvider1<T, T>,
    ): TemporalValidator<T> = constrain(message.key, Constraints.gte(value, message))

    override fun lt(
        value: T,
        message: MessageProvider1<T, T>,
    ): TemporalValidator<T> = constrain(message.key, Constraints.lt(value, message))

    override fun lte(
        value: T,
        message: MessageProvider1<T, T>,
    ): TemporalValidator<T> = constrain(message.key, Constraints.lte(value, message))

    override fun future(message: MessageProvider0<T>): TemporalValidator<T> =
        constrain(message.key) {
            satisfies(it.input > temporalNow.now(clock), message(it))
        }

    override fun futureOrPresent(message: MessageProvider0<T>): TemporalValidator<T> =
        constrain(message.key) {
            satisfies(it.input >= temporalNow.now(clock), message(it))
        }

    override fun past(message: MessageProvider0<T>): TemporalValidator<T> =
        constrain(message.key) {
            satisfies(it.input < temporalNow.now(clock), message(it))
        }

    override fun pastOrPresent(message: MessageProvider0<T>): TemporalValidator<T> =
        constrain(message.key) {
            satisfies(it.input <= temporalNow.now(clock), message(it))
        }

    override operator fun plus(other: Validator<T, T>): TemporalValidator<T> = and(other)

    override fun and(other: Validator<T, T>): TemporalValidator<T> {
        val combined = (this as Validator<T, T>).and(other)
        return TemporalValidatorImpl("and", prev = combined, constraint = Constraint.satisfied(), clock = clock, temporalNow = temporalNow)
    }

    override fun or(other: Validator<T, T>): TemporalValidator<T> {
        val combined = (this as Validator<T, T>).or(other)
        return TemporalValidatorImpl("or", prev = combined, constraint = Constraint.satisfied(), clock = clock, temporalNow = temporalNow)
    }

    override fun chain(other: Validator<T, T>): TemporalValidator<T> {
        val combined = (this as Validator<T, T>).chain(other)
        return TemporalValidatorImpl(
            "chain",
            prev = combined,
            constraint = Constraint.satisfied(),
            clock = clock,
            temporalNow = temporalNow,
        )
    }

    override fun toString(): String = "${TemporalValidator::class.simpleName}(name=$name)"
}
