package org.komapper.extension.validator

import java.time.Clock
import java.time.temporal.Temporal

/**
 * Creates a new temporal validator.
 *
 * @param T The temporal type being validated
 * @param name Debug name for the validator
 * @param validator Validator to wrap
 * @param clock Clock used for temporal comparisons (past, future, etc.)
 * @param temporalNow Strategy for obtaining the current temporal value
 */
fun <T> TemporalValidator(
    name: String = "empty",
    validator: IdentityValidator<T> = Validator.success(),
    clock: Clock = Clock.systemDefaultZone(),
    temporalNow: TemporalNow<T>,
): TemporalValidator<T> where T : Temporal, T : Comparable<T> =
    object : TemporalValidator<T> {
        override val clock: Clock = clock
        override val temporalNow: TemporalNow<T> = temporalNow

        override fun execute(
            input: T,
            context: ValidationContext,
        ): ValidationResult<T> = validator.execute(input, context)
    }

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
    val clock: Clock
    val temporalNow: TemporalNow<T>
}

fun <T> TemporalValidator<T>.constrain(
    id: String,
    check: ConstraintScope<T>.(ConstraintContext<T>) -> ConstraintResult,
): TemporalValidator<T> where T : Temporal, T : Comparable<T> =
    TemporalValidator(
        name = id,
        validator = chain(ConstraintValidator(Constraint(id, check))),
        clock = clock,
        temporalNow = temporalNow,
    )

/**
 * Validates that the temporal value is greater than or equal to [value] (inclusive).
 *
 * @param value Minimum allowed value
 * @param message Custom error message provider
 */
fun <T> TemporalValidator<T>.min(
    value: T,
    message: MessageProvider = Message.resource(),
): TemporalValidator<T> where T : Temporal, T : Comparable<T> = constrain("kova.temporal.min", Constraints.min(value, message))

/**
 * Validates that the temporal value is less than or equal to [value] (inclusive).
 *
 * @param value Maximum allowed value
 * @param message Custom error message provider
 */
fun <T> TemporalValidator<T>.max(
    value: T,
    message: MessageProvider = Message.resource(),
): TemporalValidator<T> where T : Temporal, T : Comparable<T> = constrain("kova.temporal.max", Constraints.max(value, message))

/**
 * Validates that the temporal value is strictly greater than [value] (exclusive).
 *
 * @param value The value that the input must be greater than
 * @param message Custom error message provider
 */
fun <T> TemporalValidator<T>.gt(
    value: T,
    message: MessageProvider = Message.resource(),
): TemporalValidator<T> where T : Temporal, T : Comparable<T> = constrain("kova.temporal.gt", Constraints.gt(value, message))

/**
 * Validates that the temporal value is greater than or equal to [value] (inclusive).
 *
 * Alias for [min].
 *
 * @param value The minimum value (inclusive)
 * @param message Custom error message provider
 */
fun <T> TemporalValidator<T>.gte(
    value: T,
    message: MessageProvider = Message.resource(),
): TemporalValidator<T> where T : Temporal, T : Comparable<T> = constrain("kova.temporal.gte", Constraints.gte(value, message))

/**
 * Validates that the temporal value is strictly less than [value] (exclusive).
 *
 * @param value The value that the input must be less than
 * @param message Custom error message provider
 */
fun <T> TemporalValidator<T>.lt(
    value: T,
    message: MessageProvider = Message.resource(),
): TemporalValidator<T> where T : Temporal, T : Comparable<T> = constrain("kova.temporal.lt", Constraints.lt(value, message))

/**
 * Validates that the temporal value is less than or equal to [value] (inclusive).
 *
 * Alias for [max].
 *
 * @param value The maximum value (inclusive)
 * @param message Custom error message provider
 */
fun <T> TemporalValidator<T>.lte(
    value: T,
    message: MessageProvider = Message.resource(),
): TemporalValidator<T> where T : Temporal, T : Comparable<T> = constrain("kova.temporal.lte", Constraints.lte(value, message))

/**
 * Validates that the temporal value is in the future (strictly greater than now).
 *
 * @param message Custom error message provider
 */
fun <T> TemporalValidator<T>.future(
    message: MessageProvider = Message.resource(),
): TemporalValidator<T> where T : Temporal, T : Comparable<T> =
    constrain("kova.temporal.future") {
        satisfies(it.input > temporalNow.now(clock), message(it.input))
    }

/**
 * Validates that the temporal value is in the future or present (greater than or equal to now).
 *
 * @param message Custom error message provider
 */
fun <T> TemporalValidator<T>.futureOrPresent(
    message: MessageProvider = Message.resource(),
): TemporalValidator<T> where T : Temporal, T : Comparable<T> =
    constrain("kova.temporal.futureOrPresent") {
        satisfies(it.input >= temporalNow.now(clock), message(it.input))
    }

/**
 * Validates that the temporal value is in the past (strictly less than now).
 *
 * @param message Custom error message provider
 */
fun <T> TemporalValidator<T>.past(
    message: MessageProvider = Message.resource(),
): TemporalValidator<T> where T : Temporal, T : Comparable<T> =
    constrain("kova.temporal.past") {
        satisfies(it.input < temporalNow.now(clock), message(it.input))
    }

/**
 * Validates that the temporal value is in the past or present (less than or equal to now).
 *
 * @param message Custom error message provider
 */
fun <T> TemporalValidator<T>.pastOrPresent(
    message: MessageProvider = Message.resource(),
): TemporalValidator<T> where T : Temporal, T : Comparable<T> =
    constrain("kova.temporal.pastOrPresent") {
        satisfies(it.input <= temporalNow.now(clock), message(it.input))
    }

/**
 * Combines this validator with another validator using logical AND.
 *
 * Alias for [and].
 *
 * @param other The validator to combine with
 */
operator fun <T> TemporalValidator<T>.plus(other: IdentityValidator<T>): TemporalValidator<T>
    where T : Temporal, T : Comparable<T> =
    and(other)

/**
 * Combines this validator with another validator using logical AND.
 *
 * Both validators must pass for the combined validator to pass.
 *
 * @param other The validator to combine with
 */
infix fun <T> TemporalValidator<T>.and(other: IdentityValidator<T>): TemporalValidator<T>
    where T : Temporal, T : Comparable<T> =
    TemporalValidator(
        name = "and",
        validator = (this as IdentityValidator<T>).and(other),
        clock = clock,
        temporalNow = temporalNow,
    )

/**
 * Combines this validator with another validator using logical OR.
 *
 * Either validator can pass for the combined validator to pass.
 *
 * @param other The validator to combine with
 */
infix fun <T> TemporalValidator<T>.or(other: IdentityValidator<T>): TemporalValidator<T>
    where T : Temporal, T : Comparable<T> =
    TemporalValidator(
        name = "or",
        validator = (this as IdentityValidator<T>).or(other),
        clock = clock,
        temporalNow = temporalNow,
    )

/**
 * Chains this validator with another validator.
 *
 * The second validator is applied after this validator passes.
 *
 * @param other The validator to chain after this one
 */
fun <T> TemporalValidator<T>.chain(other: IdentityValidator<T>): TemporalValidator<T>
    where T : Temporal, T : Comparable<T> =
    TemporalValidator(
        name = "chain",
        validator = (this as IdentityValidator<T>).chain(other),
        clock = clock,
        temporalNow = temporalNow,
    )
