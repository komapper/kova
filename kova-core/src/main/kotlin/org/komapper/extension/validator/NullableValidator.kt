package org.komapper.extension.validator

import kotlin.contracts.contract

/**
 * Validates that the input is null.
 *
 * This constraint fails if the input is non-null.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().asNullable().isNull()
 * validator.validate(null)    // Success: null
 * validator.validate("hello") // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator that only accepts null
 */
@IgnorableReturnValue
context(_: Validation, _: Accumulate)
fun <T> T.isNull(message: MessageProvider = { "kova.nullable.isNull".resource }) =
    constrain("kova.nullable.isNull") { satisfies(it == null, message) }

@IgnorableReturnValue
context(_: Validation, _: Accumulate)
inline fun <T> T.isNullOr(
    noinline message: MessageProvider = { "kova.nullable.isNull".resource },
    block: Constraint<T & Any>,
) = or<Unit> { isNull(message) } orElse { block(this!!) }

/**
 * Validates that the input is not null.
 *
 * This constraint fails if the input is null.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().asNullable().notNull()
 * validator.validate("hello") // Success: "hello"
 * validator.validate(null)    // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator that rejects null
 */
@IgnorableReturnValue
context(_: Validation, _: Accumulate)
fun <T> T.notNull(message: MessageProvider = { "kova.nullable.notNull".resource }) =
    constrain("kova.nullable.notNull") { toNonNullable(message) }

/**
 * Converts a nullable validator to a validator with non-nullable output.
 *
 * This adds a `notNull()` constraint and converts the output type from `S?` to `S`.
 *
 * Example:
 * ```kotlin
 * val nullableValidator = Kova.string().min(3).asNullable()
 * val nonNullableValidator: Validator<String?, String> = nullableValidator.toNonNullable()
 *
 * nonNullableValidator.validate("hello") // Success: "hello" (non-null type)
 * nonNullableValidator.validate(null)    // Failure
 * ```
 *
 * @return A validator that rejects null and produces non-nullable output
 */
@IgnorableReturnValue
context(_: Validation, _: Accumulate)
fun <T> T.toNonNullable(message: MessageProvider = { "kova.nullable.notNull".resource }): T & Any {
    contract { returns() implies (this@toNonNullable != null) }
    satisfies(this != null, message)
    return this
}
