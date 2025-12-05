package org.komapper.extension.validator

/**
 * Type alias for unsigned long validators.
 *
 * Provides a convenient type for validators that work with ULong values (0 to 18,446,744,073,709,551,615).
 */
typealias ULongValidator = IdentityValidator<ULong>

/**
 * Validates that the unsigned long is greater than or equal to the specified minimum value.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.ulong().min(1000uL)
 * validator.validate(2000uL)  // Success
 * validator.validate(500uL)   // Failure
 * ```
 *
 * @param value Minimum value (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the minimum constraint
 */
fun ULongValidator.min(
    value: ULong,
    message: MessageProvider1<ULong, ULong> = Message.resource1("kova.ulong.min"),
) = constrain(message.id) {
    satisfies(it.input >= value, message(it, value))
}

/**
 * Validates that the unsigned long is less than or equal to the specified maximum value.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.ulong().max(10000uL)
 * validator.validate(5000uL)   // Success
 * validator.validate(15000uL)  // Failure
 * ```
 *
 * @param value Maximum value (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the maximum constraint
 */
fun ULongValidator.max(
    value: ULong,
    message: MessageProvider1<ULong, ULong> = Message.resource1("kova.ulong.max"),
) = constrain(message.id) {
    satisfies(it.input <= value, message(it, value))
}

/**
 * Validates that the unsigned long is strictly greater than the specified value.
 *
 * @param value The value to compare against (exclusive)
 * @param message Custom error message provider
 * @return A new validator with the greater-than constraint
 */
fun ULongValidator.gt(
    value: ULong,
    message: MessageProvider1<ULong, ULong> = Message.resource1("kova.ulong.gt"),
) = constrain(message.id) {
    satisfies(it.input > value, message(it, value))
}

/**
 * Validates that the unsigned long is greater than or equal to the specified value.
 *
 * @param value The value to compare against (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the greater-than-or-equal constraint
 */
fun ULongValidator.gte(
    value: ULong,
    message: MessageProvider1<ULong, ULong> = Message.resource1("kova.ulong.gte"),
) = constrain(message.id) {
    satisfies(it.input >= value, message(it, value))
}

/**
 * Validates that the unsigned long is strictly less than the specified value.
 *
 * @param value The value to compare against (exclusive)
 * @param message Custom error message provider
 * @return A new validator with the less-than constraint
 */
fun ULongValidator.lt(
    value: ULong,
    message: MessageProvider1<ULong, ULong> = Message.resource1("kova.ulong.lt"),
) = constrain(message.id) {
    satisfies(it.input < value, message(it, value))
}

/**
 * Validates that the unsigned long is less than or equal to the specified value.
 *
 * @param value The value to compare against (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the less-than-or-equal constraint
 */
fun ULongValidator.lte(
    value: ULong,
    message: MessageProvider1<ULong, ULong> = Message.resource1("kova.ulong.lte"),
) = constrain(message.id) {
    satisfies(it.input <= value, message(it, value))
}
