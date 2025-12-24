package org.komapper.extension.validator

/**
 * Validates that the input equals the specified value.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().literal("admin")
 * validator.validate("admin") // Success
 * validator.validate("user")  // Failure
 * ```
 *
 * @param value The expected value
 * @param message Custom error message provider
 * @return A new validator that accepts only the specified value
 */
context(_: ValidationContext)
fun <S> S.literal(
    value: S,
    message: MessageProvider = { "kova.literal.single".resource(value) },
) = constrain("kova.literal.single") { satisfies(it == value, message) }

/**
 * Validates that the input is one of the specified values.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().literal(listOf("admin", "user", "guest"))
 * validator.validate("admin") // Success
 * validator.validate("other") // Failure
 * ```
 *
 * @param values The list of acceptable values
 * @param message Custom error message provider
 * @return A new validator that accepts only values from the list
 */
context(_: ValidationContext)
fun <S> S.literal(
    values: List<S>,
    message: MessageProvider = { "kova.literal.list".resource(values) },
) = constrain("kova.literal.list") { satisfies(it in values, message) }

context(_: ValidationContext)
fun <S> S.literal(
    vararg values: S,
    message: MessageProvider = { "kova.literal.list".resource(values.asList()) },
) = literal(values.asList(), message)

context(_: ValidationContext)
inline fun onlyIf(
    condition: Boolean,
    block: () -> ValidationResult<Unit>,
): ValidationResult<Unit> =
    if (condition) {
        block()
    } else {
        Unit.success()
    }
