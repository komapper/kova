package org.komapper.extension.validator

import java.math.BigDecimal

/**
 * Validates that the number is ensurePositive (greater than zero).
 *
 * Example:
 * ```kotlin
 * tryValidate { 1.ensurePositive() }   // Success
 * tryValidate { 0.ensurePositive() }   // Failure
 * tryValidate { (-1).ensurePositive() }  // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param T The numeric type being validated
 * @receiver The number to validate
 * @param message Custom error message provider
 * @return The validated input value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun <T : Number> T.ensurePositive(message: MessageProvider = { "kova.number.positive".resource }): T =
    constrain("kova.number.positive") { satisfies(it.toDouble() > 0.0, message) }

/**
 * Validates that the number is ensureNegative (less than zero).
 *
 * Example:
 * ```kotlin
 * tryValidate { (-1).ensureNegative() }  // Success
 * tryValidate { 0.ensureNegative() }   // Failure
 * tryValidate { 1.ensureNegative() }   // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param T The numeric type being validated
 * @receiver The number to validate
 * @param message Custom error message provider
 * @return The validated input value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun <T : Number> T.ensureNegative(message: MessageProvider = { "kova.number.negative".resource }): T =
    constrain("kova.number.negative") { satisfies(it.toDouble() < 0.0, message) }

/**
 * Validates that the number is not ensurePositive (less than or equal to zero).
 *
 * Example:
 * ```kotlin
 * tryValidate { (-1).ensureNotPositive() }  // Success
 * tryValidate { 0.ensureNotPositive() }   // Success
 * tryValidate { 1.ensureNotPositive() }   // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param T The numeric type being validated
 * @receiver The number to validate
 * @param message Custom error message provider
 * @return The validated input value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun <T : Number> T.ensureNotPositive(message: MessageProvider = { "kova.number.notPositive".resource }): T =
    constrain("kova.number.notPositive") { satisfies(it.toDouble() <= 0.0, message) }

/**
 * Validates that the number is not ensureNegative (greater than or equal to zero).
 *
 * Example:
 * ```kotlin
 * tryValidate { 0.ensureNotNegative() }   // Success
 * tryValidate { 1.ensureNotNegative() }   // Success
 * tryValidate { (-1).ensureNotNegative() }  // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param T The numeric type being validated
 * @receiver The number to validate
 * @param message Custom error message provider
 * @return The validated input value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun <T : Number> T.ensureNotNegative(message: MessageProvider = { "kova.number.notNegative".resource }): T =
    constrain("kova.number.notNegative") { satisfies(it.toDouble() >= 0.0, message) }

/**
 * Validates that the number has at most the specified number of integer and fractional digits.
 *
 * This is equivalent to Hibernate Validator's `@Digits` annotation.
 *
 * Example:
 * ```kotlin
 * tryValidate { "123456.78".toBigDecimal().ensureDigits(integer = 6, fraction = 2) }  // Success
 * tryValidate { "1234567.89".toBigDecimal().ensureDigits(integer = 6, fraction = 2) } // Failure (7 integer digits)
 * tryValidate { "123456.789".toBigDecimal().ensureDigits(integer = 6, fraction = 2) } // Failure (3 fractional digits)
 * tryValidate { 12345.ensureDigits(integer = 6) }  // Success (Int, no fraction)
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param T The numeric type being validated
 * @receiver The number to validate
 * @param integer Maximum number of integer digits allowed
 * @param fraction Maximum number of fractional digits allowed (default: 0)
 * @param message Custom error message provider
 * @return The validated input value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public fun <T : Number> T.ensureDigits(
    integer: Int,
    fraction: Int = 0,
    message: MessageProvider = { "kova.number.digits".resource(integer, fraction) },
): T =
    constrain("kova.number.digits") {
        require(integer >= 0) { "integer must be non-negative" }
        require(fraction >= 0) { "fraction must be non-negative" }
        val bigDecimal =
            when (it) {
                is BigDecimal -> it
                is java.math.BigInteger -> it.toBigDecimal()
                is Double -> it.toBigDecimal()
                is Float -> it.toBigDecimal()
                is Long -> it.toBigDecimal()
                is Int -> it.toBigDecimal()
                is Short -> it.toLong().toBigDecimal()
                is Byte -> it.toLong().toBigDecimal()
                else -> it.toDouble().toBigDecimal()
            }
        val (integerDigits, fractionDigits) = countDigits(bigDecimal)
        satisfies(integerDigits <= integer && fractionDigits <= fraction, message)
    }

private fun countDigits(value: BigDecimal): Pair<Int, Int> {
    val stripped = value.abs().stripTrailingZeros()
    val scale = stripped.scale()
    val precision = stripped.precision()
    return when {
        scale < 0 -> Pair(precision - scale, 0)
        scale >= precision -> Pair(0, scale)
        else -> Pair(precision - scale, scale)
    }
}
