package org.komapper.extension.validator

/**
 * Validates that the number is greater than or equal to the specified minimum value.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.int().min(0)
 * validator.validate(10)  // Success
 * validator.validate(-1)  // Failure
 * ```
 *
 * @param value Minimum value (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the minimum constraint
 */
@IgnorableReturnValue
context(_: Validation, _: Accumulate)
fun <S : Comparable<S>> min(
    input: S,
    value: S,
    message: MessageProvider = { "kova.comparable.min".resource(value) },
) = input.constrain("kova.comparable.min") { satisfies(it >= value, message) }

/**
 * Validates that the number is less than or equal to the specified maximum value.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.int().max(100)
 * validator.validate(50)   // Success
 * validator.validate(150)  // Failure
 * ```
 *
 * @param value Maximum value (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the maximum constraint
 */
@IgnorableReturnValue
context(_: Validation, _: Accumulate)
fun <S : Comparable<S>> max(
    input: S,
    value: S,
    message: MessageProvider = { "kova.comparable.max".resource(value) },
) = input.constrain("kova.comparable.max") { satisfies(it <= value, message) }

/**
 * Validates that the number is strictly greater than the specified value.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.int().gt(0)
 * validator.validate(1)   // Success
 * validator.validate(0)   // Failure
 * validator.validate(-1)  // Failure
 * ```
 *
 * @param value The value to compare against (exclusive)
 * @param message Custom error message provider
 * @return A new validator with the greater-than constraint
 */
@IgnorableReturnValue
context(_: Validation, _: Accumulate)
fun <S : Comparable<S>> gt(
    input: S,
    value: S,
    message: MessageProvider = { "kova.comparable.gt".resource(value) },
) = input.constrain("kova.comparable.gt") { satisfies(it > value, message) }

/**
 * Validates that the number is greater than or equal to the specified value.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.int().gte(0)
 * validator.validate(1)   // Success
 * validator.validate(0)   // Success
 * validator.validate(-1)  // Failure
 * ```
 *
 * @param value The value to compare against (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the greater-than-or-equal constraint
 */
@IgnorableReturnValue
context(_: Validation, _: Accumulate)
fun <S : Comparable<S>> gte(
    input: S,
    value: S,
    message: MessageProvider = { "kova.comparable.gte".resource(value) },
) = input.constrain("kova.comparable.gte") { satisfies(it >= value, message) }

/**
 * Validates that the number is strictly less than the specified value.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.int().lt(100)
 * validator.validate(50)   // Success
 * validator.validate(100)  // Failure
 * validator.validate(150)  // Failure
 * ```
 *
 * @param value The value to compare against (exclusive)
 * @param message Custom error message provider
 * @return A new validator with the less-than constraint
 */
@IgnorableReturnValue
context(_: Validation, _: Accumulate)
fun <S : Comparable<S>> lt(
    input: S,
    value: S,
    message: MessageProvider = { "kova.comparable.lt".resource(value) },
) = input.constrain("kova.comparable.lt") { satisfies(it < value, message) }

/**
 * Validates that the number is less than or equal to the specified value.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.int().lte(100)
 * validator.validate(50)   // Success
 * validator.validate(100)  // Success
 * validator.validate(150)  // Failure
 * ```
 *
 * @param value The value to compare against (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the less-than-or-equal constraint
 */
@IgnorableReturnValue
context(_: Validation, _: Accumulate)
fun <S : Comparable<S>> lte(
    input: S,
    value: S,
    message: MessageProvider = { "kova.comparable.lte".resource(value) },
) = input.constrain("kova.comparable.lte") { satisfies(it <= value, message) }

/**
 * Validates that the value is equal to the specified value.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.int().eq(42)
 * validator.validate(42)  // Success
 * validator.validate(41)  // Failure
 * validator.validate(43)  // Failure
 * ```
 *
 * @param value The value to compare against
 * @param message Custom error message provider
 * @return A new validator with the equality constraint
 */
@IgnorableReturnValue
context(_: Validation, _: Accumulate)
fun <S : Comparable<S>> eq(
    input: S,
    value: S,
    message: MessageProvider = { "kova.comparable.eq".resource(value) },
) = input.constrain("kova.comparable.eq") { satisfies(it == value, message) }

/**
 * Validates that the value is not equal to the specified value.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.int().notEq(0)
 * validator.validate(1)   // Success
 * validator.validate(-1)  // Success
 * validator.validate(0)   // Failure
 * ```
 *
 * @param value The value to compare against
 * @param message Custom error message provider
 * @return A new validator with the inequality constraint
 */
@IgnorableReturnValue
context(_: Validation, _: Accumulate)
fun <S : Comparable<S>> notEq(
    input: S,
    value: S,
    message: MessageProvider = { "kova.comparable.notEq".resource(value) },
) = input.constrain("kova.comparable.notEq") { satisfies(it != value, message) }
