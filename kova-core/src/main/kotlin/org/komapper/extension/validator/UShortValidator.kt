package org.komapper.extension.validator

/**
 * Type alias for unsigned short validators.
 *
 * Provides a convenient type for validators that work with UShort values (0 to 65,535).
 */
typealias UShortValidator = IdentityValidator<UShort>

/**
 * Validates that the unsigned short is greater than or equal to the specified minimum value.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.ushort().min(100u)
 * validator.validate(200u)  // Success
 * validator.validate(50u)   // Failure
 * ```
 *
 * @param value Minimum value (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the minimum constraint
 */
fun UShortValidator.min(
    value: UShort,
    message: MessageProvider1<UShort, UShort> = Message.resource1("kova.ushort.min"),
) = constrain(message.id) {
    satisfies(it.input >= value, message(it, value))
}

/**
 * Validates that the unsigned short is less than or equal to the specified maximum value.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.ushort().max(1000u)
 * validator.validate(500u)   // Success
 * validator.validate(1500u)  // Failure
 * ```
 *
 * @param value Maximum value (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the maximum constraint
 */
fun UShortValidator.max(
    value: UShort,
    message: MessageProvider1<UShort, UShort> = Message.resource1("kova.ushort.max"),
) = constrain(message.id) {
    satisfies(it.input <= value, message(it, value))
}

/**
 * Validates that the unsigned short is strictly greater than the specified value.
 *
 * @param value The value to compare against (exclusive)
 * @param message Custom error message provider
 * @return A new validator with the greater-than constraint
 */
fun UShortValidator.gt(
    value: UShort,
    message: MessageProvider1<UShort, UShort> = Message.resource1("kova.ushort.gt"),
) = constrain(message.id) {
    satisfies(it.input > value, message(it, value))
}

/**
 * Validates that the unsigned short is greater than or equal to the specified value.
 *
 * @param value The value to compare against (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the greater-than-or-equal constraint
 */
fun UShortValidator.gte(
    value: UShort,
    message: MessageProvider1<UShort, UShort> = Message.resource1("kova.ushort.gte"),
) = constrain(message.id) {
    satisfies(it.input >= value, message(it, value))
}

/**
 * Validates that the unsigned short is strictly less than the specified value.
 *
 * @param value The value to compare against (exclusive)
 * @param message Custom error message provider
 * @return A new validator with the less-than constraint
 */
fun UShortValidator.lt(
    value: UShort,
    message: MessageProvider1<UShort, UShort> = Message.resource1("kova.ushort.lt"),
) = constrain(message.id) {
    satisfies(it.input < value, message(it, value))
}

/**
 * Validates that the unsigned short is less than or equal to the specified value.
 *
 * @param value The value to compare against (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the less-than-or-equal constraint
 */
fun UShortValidator.lte(
    value: UShort,
    message: MessageProvider1<UShort, UShort> = Message.resource1("kova.ushort.lte"),
) = constrain(message.id) {
    satisfies(it.input <= value, message(it, value))
}
