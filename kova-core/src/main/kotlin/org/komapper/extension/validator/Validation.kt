package org.komapper.extension.validator

import org.komapper.extension.validator.ValidationIor.Both
import org.komapper.extension.validator.ValidationIor.FailureLike
import org.komapper.extension.validator.ValidationResult.Failure
import org.komapper.extension.validator.ValidationResult.Success
import java.time.Clock

/**
 * Context object that tracks the state of validation execution.
 *
 * Contains information about the validation root, current path, and configuration.
 * The context is immutable and threaded through the validation process, with validation
 * functions creating new contexts with updated state as needed.
 *
 * @property root The root object's qualified name (e.g., "com.example.User")
 * @property path The current validation path, tracking nested objects and circular references
 * @property config Validation configuration settings
 * @property acc Accumulator function for collecting validation errors
 */
public data class Validation(
    val root: String = "",
    val path: Path = Path(name = "", obj = null, parent = null),
    val config: ValidationConfig = ValidationConfig(),
    val acc: Accumulate = { error("Accumulate context not initialized") },
)

/**
 * Extracts the value from a [ValidationIor], accumulating or raising errors as needed.
 *
 * This function handles all cases of [ValidationIor]:
 * - [Success]: Returns the value directly
 * - [Failure]: Raises validation failure with accumulated messages
 * - [Both]: Accumulates partial errors and returns the partial value
 *
 * Example:
 * ```kotlin
 * val result: ValidationIor<String> = or { ensureNotBlank(name) }
 * val value: String = result.bind()  // Extracts or raises
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver ValidationIor<T> The validation result to extract from
 * @param T The type of the value
 * @return The validated value
 * @throws ValidationCancellationException if this is a [Failure]
 */
context(_: Validation)
public fun <T> ValidationIor<T>.bind(): T =
    when (this) {
        is Success -> value
        is Failure -> raise(messages)
        is Both -> {
            accumulate(messages)
            value
        }
    }

/**
 * Attempts alternative validation logic if this validation fails.
 *
 * This function implements a fallback strategy: if the current validation succeeds
 * (is not [FailureLike]), it returns immediately. Otherwise, it executes the fallback
 * block. If both fail, it wraps both error messages with a combined "or" message.
 *
 * Example:
 * ```kotlin
 * tryValidate {
 *     or { min(input, 10) } or { max(input, 5) }
 *     // Validates that input >= 10 OR input <= 5
 * }
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver ValidationIor<R> The current validation result
 * @param R The type of the validation result
 * @param block The fallback validation logic to try if this fails
 * @return [ValidationIor] representing the combined validation result
 */
context(v: Validation)
public inline infix fun <R> ValidationIor<R>.or(block: context(Validation)() -> R): ValidationIor<R> {
    if (this !is FailureLike) return this
    val other = eval(block)
    if (other !is FailureLike) return other
    return (this as? Both ?: other).withMessage("kova.or".resource(messages, other.messages))
}

@PublishedApi
context(v: Validation)
internal inline fun <R> eval(block: context(Validation)() -> R): ValidationIor<R> = or(block)

/**
 * Attempts alternative validation logic and extracts the result, raising errors if both fail.
 *
 * This is a convenience function that combines [or] and [bind]. It tries the current
 * validation, falls back to the alternative if needed, and extracts the value or raises
 * accumulated errors.
 *
 * Example:
 * ```kotlin
 * tryValidate {
 *     val value = or { min(input, 10) } orElse { max(input, 5) }
 *     // Returns the value if either constraint passes, raises if both fail
 * }
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver ValidationIor<R> The current validation result
 * @param R The type of the validation result
 * @param block The fallback validation logic to try if this fails
 * @return The validated value
 * @throws ValidationCancellationException if both validations fail
 */
context(_: Validation)
public inline infix fun <R> ValidationIor<R>.orElse(block: context(Validation)() -> R): R = or(block).bind()

/**
 * The clock used for temporal validation constraints (ensurePast, ensureFuture, etc.).
 *
 * Delegates to [ValidationConfig.clock].
 *
 * @param Validation (context parameter) The validation context containing the clock configuration
 * @return The configured [Clock] instance
 */
context(v: Validation)
public val clock: Clock get() = v.config.clock

/**
 * Configuration settings for validation execution.
 *
 * @property failFast If true, validation stops at the first failure instead of collecting all errors.
 *                    Default is false (collect all errors).
 * @property clock The clock used for temporal validation constraints (ensurePast, ensureFuture, ensurePastOrPresent, ensureFutureOrPresent).
 *                 Defaults to [Clock.systemDefaultZone]. Use a fixed clock for deterministic testing.
 * @property logger Optional callback function for receiving debug log messages during validation.
 *                  If null (default), no logging is performed. Each log message ensureContains information
 *                  about constraint satisfaction/violation, including constraint ID, root, path, and input value.
 *
 * Example:
 * ```kotlin
 * // Basic configuration with fail-fast
 * val config = ValidationConfig(
 *     failFast = true,
 *     logger = { entry -> println(entry) }
 * )
 * val result = tryValidate(config) { min("hello", 1); max("hello", 10) }
 *
 * // Configuration with fixed clock for testing temporal validators
 * val fixedClock = Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC)
 * val testConfig = ValidationConfig(clock = fixedClock)
 * val result2 = tryValidate(testConfig) {
 *     ensurePast(LocalDate.of(2024, 12, 31))
 * }
 * ```
 */
public data class ValidationConfig(
    val failFast: Boolean = false,
    val clock: Clock = Clock.systemDefaultZone(),
    val logger: ((LogEntry) -> Unit)? = null,
)

/**
 * Executes a validation block with a message transformation function applied to all errors.
 *
 * This function wraps the error accumulator so that each validation message accumulated
 * during the block's execution is transformed by the provided function. This is useful
 * for enriching messages with additional context or modifying their content.
 *
 * The transformation is applied to all validation errors that occur within the block,
 * including nested validation errors. This is used internally by the [constrain] function
 * to add constraint details (input value and constraint ID) to error messages.
 *
 * Example internal usage:
 * ```kotlin
 * mapEachMessage({ logAndAddDetails(it, input, "kova.string.min") }) {
 *     // All messages from this block will have details added
 *     satisfies(condition) { "kova.string.min".resource }
 * }
 * ```
 *
 * @param R the type of the validation result
 * @param transform The function to apply to each validation message
 * @param block The validation logic to execute
 * @return The result of executing the validation block
 */
@PublishedApi
context(v: Validation)
internal inline fun <R> mapEachMessage(
    noinline transform: (Message) -> Message,
    block: context(Validation)() -> R,
): R = block(v.copy(acc = { accumulate(it.map(transform)) }))

