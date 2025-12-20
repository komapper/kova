package org.komapper.extension.validator

/**
 * Type alias for validators that coalesce null inputs to a default value.
 *
 * This validator accepts nullable input (`T?`) but produces non-nullable output (`S`).
 * When the input is null, it returns a default value instead, similar to Kotlin's Elvis operator (`?:`).
 *
 * Named after Kotlin's Elvis operator which does the same thing: `value ?: default`
 *
 * Example:
 * ```kotlin
 * // Create a validator that defaults null to 0
 * val validator: ElvisValidator<Int, Int> = Kova.nullable(0)
 * validator.validate(null) // Returns 0
 * validator.validate(5)    // Returns 5
 *
 * // With validation constraints
 * val validator = Kova.int().min(0).asNullable(0)
 * validator.validate(null) // Success: 0
 * validator.validate(5)    // Success: 5
 * validator.validate(-1)   // Failure: violates min constraint
 * ```
 *
 * @param T The non-null input type
 * @param S The non-null output type (always non-null due to Elvis operator behavior)
 * @see NullableValidator for validators that allow null in both input and output
 * @see Validator.asNullable for converting non-nullable validators to Elvis validators
 */
typealias ElvisValidator<T, S> = Validator<T?, S>

/**
 * Operator overload for [and]. Combines this Elvis validator with a non-nullable validator.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.nullable(0) + Kova.int().min(3)
 * validator.validate(null) // Success: 0
 * validator.validate(5)    // Success: 5
 * validator.validate(2)    // Failure: violates min(3)
 * ```
 *
 * @param other The non-nullable validator to combine with
 * @return A new Elvis validator combining both
 */
infix fun <T : Any, S : Any> ElvisValidator<T, S>.plus(other: Validator<T, S>): ElvisValidator<T, S> = and(other)

/**
 * Combines this Elvis validator with a non-nullable validator using logical AND.
 *
 * The non-nullable validator is only applied to non-null values. When the input is null,
 * the default value is returned without applying the other validator.
 *
 * Example:
 * ```kotlin
 * val min3 = Kova.int().min(3)
 * val validator = Kova.nullable(0).and(min3)
 *
 * validator.validate(null) // Success: 0 (default, min3 not applied)
 * validator.validate(5)    // Success: 5 (both pass)
 * validator.validate(2)    // Failure: violates min(3)
 * ```
 *
 * @param other The non-nullable validator to combine with
 * @return A new Elvis validator that applies both validations to non-null values
 */
fun <T : Any, S : Any> ElvisValidator<T, S>.and(other: Validator<T, S>): ElvisValidator<T, S> = and(withDefault(other))

/**
 * Combines this Elvis validator with a non-nullable validator using logical OR.
 *
 * The non-nullable validator is only applied to non-null values. When the input is null,
 * the default value is returned without applying the other validator.
 *
 * Example:
 * ```kotlin
 * val min3 = Kova.int().min(3)
 * val validator = Kova.int().max(5).asNullable(0).or(min3)
 *
 * validator.validate(null) // Success: 0 (default value)
 * validator.validate(4)    // Success: 4 (passes max(5))
 * validator.validate(6)    // Success: 6 (fails max(5) but passes min(3))
 * validator.validate(2)    // Success: 2 (passes max(5))
 * ```
 *
 * @param other The non-nullable validator to combine with
 * @return A new Elvis validator that tries both validations on non-null values
 */
fun <T : Any, S : Any> ElvisValidator<T, S>.or(other: Validator<T, S>): ElvisValidator<T, S> = or(withDefault(other))

/**
 * Wraps a non-nullable validator with this Elvis validator's default behavior.
 *
 * This helper function adapts a non-nullable validator so that:
 * - If input is null: returns the default value from this Elvis validator
 * - If input is non-null: applies the other validator
 *
 * This is used internally by [and] and [or] to properly handle null values in composed validators.
 */
private fun <T : Any, S : Any> ElvisValidator<T, S>.withDefault(other: Validator<T, S>): ElvisValidator<T, S> =
    ElvisValidator { input ->
        if (input == null) execute(input) else other.execute(input)
    }
