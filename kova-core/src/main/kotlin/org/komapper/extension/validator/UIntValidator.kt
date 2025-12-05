package org.komapper.extension.validator

/**
 * Type alias for unsigned integer validators.
 *
 * Provides a convenient type for validators that work with UInt values (0 to 4,294,967,295).
 */
typealias UIntValidator = IdentityValidator<UInt>

/**
 * Validates that the unsigned integer is greater than or equal to the specified minimum value.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.uint().min(1000u)
 * validator.validate(2000u)  // Success
 * validator.validate(500u)   // Failure
 * ```
 *
 * @param value Minimum value (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the minimum constraint
 */
fun UIntValidator.min(
    value: UInt,
    message: MessageProvider1<UInt, UInt> = Message.resource1("kova.uint.min"),
) = constrain(message.id) {
    satisfies(it.input >= value, message(it, value))
}

/**
 * Validates that the unsigned integer is less than or equal to the specified maximum value.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.uint().max(10000u)
 * validator.validate(5000u)   // Success
 * validator.validate(15000u)  // Failure
 * ```
 *
 * @param value Maximum value (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the maximum constraint
 */
fun UIntValidator.max(
    value: UInt,
    message: MessageProvider1<UInt, UInt> = Message.resource1("kova.uint.max"),
) = constrain(message.id) {
    satisfies(it.input <= value, message(it, value))
}

/**
 * Validates that the unsigned integer is strictly greater than the specified value.
 *
 * @param value The value to compare against (exclusive)
 * @param message Custom error message provider
 * @return A new validator with the greater-than constraint
 */
fun UIntValidator.gt(
    value: UInt,
    message: MessageProvider1<UInt, UInt> = Message.resource1("kova.uint.gt"),
) = constrain(message.id) {
    satisfies(it.input > value, message(it, value))
}

/**
 * Validates that the unsigned integer is greater than or equal to the specified value.
 *
 * @param value The value to compare against (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the greater-than-or-equal constraint
 */
fun UIntValidator.gte(
    value: UInt,
    message: MessageProvider1<UInt, UInt> = Message.resource1("kova.uint.gte"),
) = constrain(message.id) {
    satisfies(it.input >= value, message(it, value))
}

/**
 * Validates that the unsigned integer is strictly less than the specified value.
 *
 * @param value The value to compare against (exclusive)
 * @param message Custom error message provider
 * @return A new validator with the less-than constraint
 */
fun UIntValidator.lt(
    value: UInt,
    message: MessageProvider1<UInt, UInt> = Message.resource1("kova.uint.lt"),
) = constrain(message.id) {
    satisfies(it.input < value, message(it, value))
}

/**
 * Validates that the unsigned integer is less than or equal to the specified value.
 *
 * @param value The value to compare against (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the less-than-or-equal constraint
 */
fun UIntValidator.lte(
    value: UInt,
    message: MessageProvider1<UInt, UInt> = Message.resource1("kova.uint.lte"),
) = constrain(message.id) {
    satisfies(it.input <= value, message(it, value))
}
