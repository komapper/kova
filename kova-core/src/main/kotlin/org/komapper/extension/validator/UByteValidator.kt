package org.komapper.extension.validator

/**
 * Type alias for unsigned byte validators.
 *
 * Provides a convenient type for validators that work with UByte values (0 to 255).
 */
typealias UByteValidator = IdentityValidator<UByte>

/**
 * Validates that the unsigned byte is greater than or equal to the specified minimum value.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.ubyte().min(10u)
 * validator.validate(20u)  // Success
 * validator.validate(5u)   // Failure
 * ```
 *
 * @param value Minimum value (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the minimum constraint
 */
fun UByteValidator.min(
    value: UByte,
    message: MessageProvider1<UByte, UByte> = Message.resource1("kova.ubyte.min"),
) = constrain(message.id) {
    satisfies(it.input >= value, message(it, value))
}

/**
 * Validates that the unsigned byte is less than or equal to the specified maximum value.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.ubyte().max(100u)
 * validator.validate(50u)   // Success
 * validator.validate(150u)  // Failure
 * ```
 *
 * @param value Maximum value (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the maximum constraint
 */
fun UByteValidator.max(
    value: UByte,
    message: MessageProvider1<UByte, UByte> = Message.resource1("kova.ubyte.max"),
) = constrain(message.id) {
    satisfies(it.input <= value, message(it, value))
}

/**
 * Validates that the unsigned byte is strictly greater than the specified value.
 *
 * @param value The value to compare against (exclusive)
 * @param message Custom error message provider
 * @return A new validator with the greater-than constraint
 */
fun UByteValidator.gt(
    value: UByte,
    message: MessageProvider1<UByte, UByte> = Message.resource1("kova.ubyte.gt"),
) = constrain(message.id) {
    satisfies(it.input > value, message(it, value))
}

/**
 * Validates that the unsigned byte is greater than or equal to the specified value.
 *
 * @param value The value to compare against (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the greater-than-or-equal constraint
 */
fun UByteValidator.gte(
    value: UByte,
    message: MessageProvider1<UByte, UByte> = Message.resource1("kova.ubyte.gte"),
) = constrain(message.id) {
    satisfies(it.input >= value, message(it, value))
}

/**
 * Validates that the unsigned byte is strictly less than the specified value.
 *
 * @param value The value to compare against (exclusive)
 * @param message Custom error message provider
 * @return A new validator with the less-than constraint
 */
fun UByteValidator.lt(
    value: UByte,
    message: MessageProvider1<UByte, UByte> = Message.resource1("kova.ubyte.lt"),
) = constrain(message.id) {
    satisfies(it.input < value, message(it, value))
}

/**
 * Validates that the unsigned byte is less than or equal to the specified value.
 *
 * @param value The value to compare against (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the less-than-or-equal constraint
 */
fun UByteValidator.lte(
    value: UByte,
    message: MessageProvider1<UByte, UByte> = Message.resource1("kova.ubyte.lte"),
) = constrain(message.id) {
    satisfies(it.input <= value, message(it, value))
}
