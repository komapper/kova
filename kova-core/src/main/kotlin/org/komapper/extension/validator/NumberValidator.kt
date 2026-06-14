package org.komapper.extension.validator

import java.math.BigDecimal
import java.math.BigInteger

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
    constrain("kova.number.positive") { satisfies(it.compareToZero() == 1, message) }

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
    constrain("kova.number.negative") { satisfies(it.compareToZero() == -1, message) }

/**
 * Validates that the number is positive or zero (greater than or equal to zero).
 *
 * Example:
 * ```kotlin
 * tryValidate { 0.ensurePositiveOrZero() }   // Success
 * tryValidate { 1.ensurePositiveOrZero() }   // Success
 * tryValidate { (-1).ensurePositiveOrZero() }  // Failure
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
public fun <T : Number> T.ensurePositiveOrZero(message: MessageProvider = { "kova.number.positiveOrZero".resource }): T =
    constrain("kova.number.positiveOrZero") {
        satisfies(it.compareToZero()?.let { sign -> sign >= 0 } == true, message)
    }

/**
 * Validates that the number is negative or zero (less than or equal to zero).
 *
 * Example:
 * ```kotlin
 * tryValidate { (-1).ensureNegativeOrZero() }  // Success
 * tryValidate { 0.ensureNegativeOrZero() }   // Success
 * tryValidate { 1.ensureNegativeOrZero() }   // Failure
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
public fun <T : Number> T.ensureNegativeOrZero(message: MessageProvider = { "kova.number.negativeOrZero".resource }): T =
    constrain("kova.number.negativeOrZero") {
        satisfies(it.compareToZero()?.let { sign -> sign <= 0 } == true, message)
    }

private fun Number.compareToZero(): Int? =
    when (this) {
        is BigDecimal -> compareTo(BigDecimal.ZERO).sign()
        is BigInteger -> compareTo(BigInteger.ZERO).sign()
        is Byte, is Short, is Int, is Long -> toLong().compareTo(0L).sign()
        is Float -> toDouble().compareFiniteToZero()
        is Double -> compareFiniteToZero()
        else -> toDouble().compareFiniteToZero()
    }

private fun Double.compareFiniteToZero(): Int? =
    when {
        isNaN() -> null
        this > 0.0 -> 1
        this < 0.0 -> -1
        else -> 0
    }

private fun Int.sign(): Int = compareTo(0)

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
        val bigDecimal = it.toBigDecimalOrNull()
        val matches =
            bigDecimal?.let { value ->
                val (integerDigits, fractionDigits) = countDigits(value)
                integerDigits <= integer && fractionDigits <= fraction
            }
        satisfies(matches == true, message)
    }

private fun Number.toBigDecimalOrNull(): BigDecimal? =
    when (this) {
        is BigDecimal -> this
        is BigInteger -> toBigDecimal()
        is Double -> takeIf { it.isFinite() }?.toBigDecimal()
        is Float -> takeIf { it.isFinite() }?.toBigDecimal()
        is Long -> toBigDecimal()
        is Int -> toBigDecimal()
        is Short -> toLong().toBigDecimal()
        is Byte -> toLong().toBigDecimal()
        else -> toDouble().takeIf { it.isFinite() }?.toBigDecimal()
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
