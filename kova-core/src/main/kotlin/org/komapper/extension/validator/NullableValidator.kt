package org.komapper.extension.validator

import org.komapper.extension.validator.ValidationResult.Success

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
context(_: ValidationContext)
fun <T> T.isNull(message: MessageProvider = { "kova.nullable.isNull".resource }) =
    constrain("kova.nullable.isNull") { satisfies(it == null, message) }

context(_: ValidationContext)
inline fun <T> T.isNullOr(
    noinline message: MessageProvider = { "kova.nullable.isNull".resource },
    block: Constraint<T & Any>,
) = or { isNull(message) } orElse { block(this!!) }

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
context(_: ValidationContext)
fun <T> T.notNull(message: MessageProvider = { "kova.nullable.notNull".resource }) =
    constrain("kova.nullable.notNull") { toNonNullable(message).map {} }

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
context(_: ValidationContext)
fun <T> T.toNonNullable(message: MessageProvider = { "kova.nullable.notNull".resource }): ValidationResult<T & Any> =
    satisfies(this != null, message).map { this!! }

context(_: ValidationContext)
inline fun <T> T.notNullAnd(
    noinline message: MessageProvider = { "kova.nullable.notNull".resource },
    block: (T & Any) -> ValidationResult<Unit>,
) = notNull(message).and { this?.let(block).orSucceed() }

inline infix fun <T> T.withDefault(defaultValue: () -> T & Any): ValidationResult<T & Any> = Success(this ?: defaultValue())

infix fun <T> T.withDefault(defaultValue: T & Any): ValidationResult<T & Any> = withDefault { defaultValue }

inline fun <T, R> T.withDefault(
    defaultValue: () -> R,
    onNotNull: (T & Any) -> ValidationResult<R>,
): ValidationResult<R> = if (this == null) defaultValue().success() else onNotNull(this)

inline fun <T, R> T.withDefault(
    defaultValue: R,
    onNotNull: (T & Any) -> ValidationResult<R>,
): ValidationResult<R> = withDefault({ defaultValue }, onNotNull)
