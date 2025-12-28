package org.komapper.extension.validator

import kotlin.contracts.contract

/**
 * Validates that the input is null.
 *
 * This constraint fails if the input is non-null.
 *
 * Example:
 * ```kotlin
 * tryValidate { isNull(null) }    // Success
 * tryValidate { isNull("hello") } // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <T> Validation.isNull(
    input: T,
    message: MessageProvider = { "kova.nullable.isNull".resource },
) = input.constrain("kova.nullable.isNull") { satisfies(it == null, message) }

@IgnorableReturnValue
inline fun <T> Validation.isNullOr(
    input: T,
    noinline message: MessageProvider = { "kova.nullable.isNull".resource },
    block: Validation.(T & Any) -> Unit,
) = or<Unit> { isNull(input, message) } orElse { block(input!!) }

/**
 * Validates that the input is not null.
 *
 * This constraint fails if the input is null.
 *
 * Example:
 * ```kotlin
 * tryValidate { notNull("hello") } // Success
 * tryValidate { notNull(null) }    // Failure
 * ```
 *
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <T> Validation.notNull(
    input: T,
    message: MessageProvider = { "kova.nullable.notNull".resource },
) = input.constrain("kova.nullable.notNull") { toNonNullable(input, message) }

/**
 * Converts a nullable input to a non-nullable output.
 *
 * This validates that the input is not null and converts the output type from `T?` to `T & Any`.
 *
 * Example:
 * ```kotlin
 * fun Validation.validateString(s: String?): String {
 *     if (s != null) min(s, 3)
 *     return toNonNullable(s)
 * }
 *
 * tryValidate { validateString("hello") } // Success
 * tryValidate { validateString(null) }    // Failure
 * ```
 *
 * @return The non-null input value with type `T & Any`
 */
@IgnorableReturnValue
fun <T> Validation.toNonNullable(
    input: T,
    message: MessageProvider = { "kova.nullable.notNull".resource },
): T & Any {
    contract { returns() implies (input != null) }
    Constraint(this).satisfies(input != null, message)
    return input
}
