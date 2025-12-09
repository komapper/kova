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

/**
 * Converts a non-nullable validator to a nullable validator with a default value.
 *
 * When the input is null, the validator returns the provided default value.
 * This ensures the output is always non-null.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().min(3).asNullable("default")
 * validator.validate(null)    // Success: "default"
 * validator.validate("hello") // Success: "hello"
 * validator.validate("ab")    // Failure: too short
 * ```
 *
 * @param defaultValue The value to use when input is null
 * @return A new validator that accepts null input but produces non-nullable output
 */
fun <T : Any, S : Any> Validator<T, S>.asNullable(defaultValue: S): WithDefaultNullableValidator<T, S> = asNullable { defaultValue }

/**
 * Converts a non-nullable validator to a nullable validator with a lazily-evaluated default value.
 *
 * When the input is null, the provider function is called to generate the default value.
 * This is useful when the default value is expensive to compute or needs to be fresh each time.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().asNullable { UUID.randomUUID().toString() }
 * validator.validate(null)    // Success: newly generated UUID
 * validator.validate("hello") // Success: "hello"
 * ```
 *
 * @param withDefault Function that generates the default value when input is null
 * @return A new validator that accepts null input but produces non-nullable output
 */
fun <T : Any, S : Any> Validator<T, S>.asNullable(withDefault: () -> S): WithDefaultNullableValidator<T, S> {
    val validator =
        Validator<T?, S> { input, context ->
            if (input == null) Success(withDefault(), context) else execute(input, context)
        }
    return WithDefaultNullableValidator(validator, withDefault)
}
