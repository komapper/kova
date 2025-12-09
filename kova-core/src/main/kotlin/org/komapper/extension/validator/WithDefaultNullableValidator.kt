package org.komapper.extension.validator

import org.komapper.extension.validator.ValidationResult.Success

/**
 * Type interface for validators that accept nullable input but produce non-nullable output.
 *
 * This validator has a default value that is used when the input is null,
 * ensuring the output is always non-null.
 *
 * Example:
 * ```kotlin
 * val validator: WithDefaultNullableValidator<String, String> =
 *     Kova.string().min(1).asNullable("default")
 *
 * validator.validate(null)    // Success: "default" (non-null)
 * validator.validate("hello") // Success: "hello"
 * validator.validate("")      // Failure: too short
 * ```
 *
 * @param T The non-null input type
 * @param S The non-null output type (always non-null due to default)
 */
interface WithDefaultNullableValidator<T : Any, S : Any> : Validator<T?, S> {
    /**
     * Operator overload for [and]. Combines this validator with another validator.
     *
     * The other validator is automatically converted to nullable with the same default value.
     *
     * Example:
     * ```kotlin
     * val validator = Kova.string().asNullable("default") + Kova.string().min(3)
     * validator.validate(null)    // Success: "default"
     * validator.validate("hello") // Success: "hello"
     * validator.validate("ab")    // Failure: too short
     * ```
     *
     * @param other The validator to combine with
     * @return A new validator combining both
     */
    operator fun plus(other: Validator<T, S>): WithDefaultNullableValidator<T, S> = and(other)

    /**
     * Combines this validator with another validator using logical AND.
     *
     * The other validator is automatically converted to nullable with the same default value.
     * Both validators must pass for the combined validator to pass.
     *
     * Example:
     * ```kotlin
     * val validator = Kova.string().asNullable("default") and Kova.string().min(3)
     * validator.validate(null)    // Success: "default"
     * validator.validate("hello") // Success: "hello"
     * validator.validate("ab")    // Failure: too short
     * ```
     *
     * @param other The validator to combine with
     * @return A new validator combining both
     */
    infix fun and(other: Validator<T, S>): WithDefaultNullableValidator<T, S>

    /**
     * Combines this validator with another validator using logical OR.
     *
     * The other validator is automatically converted to nullable with the same default value.
     * Either validator can pass for the combined validator to pass.
     *
     * Example:
     * ```kotlin
     * val validator = Kova.string().asNullable("default").min(10) or Kova.string().max(3)
     * validator.validate(null)    // Success: "default"
     * validator.validate("hello") // Failure: length is 5 (fails both validators)
     * validator.validate("ab")    // Success: "ab" (passes second validator)
     * validator.validate("verylongstring") // Success: passes first validator
     * ```
     *
     * @param other The validator to combine with
     * @return A new validator combining both
     */
    infix fun or(other: Validator<T, S>): WithDefaultNullableValidator<T, S>
}

@PublishedApi
internal fun <T : Any, S : Any> WithDefaultNullableValidator(
    validator: Validator<T?, S>,
    provide: () -> S,
): WithDefaultNullableValidator<T, S> =
    object : WithDefaultNullableValidator<T, S> {
        override fun execute(
            input: T?,
            context: ValidationContext,
        ): ValidationResult<S> = validator.execute(input, context)

        override fun and(other: Validator<T, S>): WithDefaultNullableValidator<T, S> {
            val and = validator and other.asNullable(provide)
            return WithDefaultNullableValidator(and, provide)
        }

        override fun or(other: Validator<T, S>): WithDefaultNullableValidator<T, S> {
            val or = validator or other.asNullable(provide)
            return WithDefaultNullableValidator(or, provide)
        }
    }
