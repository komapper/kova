package org.komapper.extension.validator

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
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun Number.ensurePositive(message: MessageProvider = { "kova.number.positive".resource }) =
    this.constrain("kova.number.positive") { satisfies(it.toDouble() > 0.0, message) }

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
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun Number.ensureNegative(message: MessageProvider = { "kova.number.negative".resource }) =
    this.constrain("kova.number.negative") { satisfies(it.toDouble() < 0.0, message) }

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
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun Number.ensureNotPositive(message: MessageProvider = { "kova.number.notPositive".resource }) =
    this.constrain("kova.number.notPositive") { satisfies(it.toDouble() <= 0.0, message) }

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
 * @param message Custom error message provider
 */
@IgnorableReturnValue
context(_: Validation)
fun Number.ensureNotNegative(message: MessageProvider = { "kova.number.notNegative".resource }) =
    this.constrain("kova.number.notNegative") { satisfies(it.toDouble() >= 0.0, message) }
