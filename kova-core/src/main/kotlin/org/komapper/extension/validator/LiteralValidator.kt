package org.komapper.extension.validator

/**
 * Validates that the input equals the specified value.
 *
 * Example:
 * ```kotlin
 * tryValidate { literal("admin", "admin") } // Success
 * tryValidate { literal("user", "admin") }  // Failure
 * ```
 *
 * @param value The expected value
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <S> Validation.literal(
    input: S,
    value: S,
    message: MessageProvider = { "kova.literal.single".resource(value) },
) = input.constrain("kova.literal.single") { satisfies(it == value, message) }

/**
 * Validates that the input is one of the specified values.
 *
 * Example:
 * ```kotlin
 * val allowed = listOf("admin", "user", "guest")
 * tryValidate { literal("admin", allowed) } // Success
 * tryValidate { literal("other", allowed) } // Failure
 * ```
 *
 * @param values The list of acceptable values
 * @param message Custom error message provider
 */
@IgnorableReturnValue
fun <S> Validation.literal(
    input: S,
    values: List<S>,
    message: MessageProvider = { "kova.literal.list".resource(values) },
) = input.constrain("kova.literal.list") { satisfies(it in values, message) }

@IgnorableReturnValue
fun <S> Validation.literal(
    input: S,
    vararg values: S,
    message: MessageProvider = { "kova.literal.list".resource(values.asList()) },
) = literal(input, values.asList(), message)
