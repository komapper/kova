package org.komapper.extension.validator

import kotlin.reflect.KProperty

/**
 * Captures a validation block for use with property delegation.
 *
 * This function wraps a validation block into a [Capture] instance, which can then be used
 * with Kotlin's property delegation syntax (`by`). When the delegate is accessed, the validation
 * block executes within a named path segment (using the property name) and accumulates any
 * validation errors.
 *
 * ## Usage
 *
 * Use `capture` to validate and transform function arguments:
 *
 * ```kotlin
 * context(_: Validation)
 * fun buildUser(rawName: String, rawAge: String): User {
 *     val name by capture { rawName.ensureNotBlank() }
 *     val age by capture { rawAge.transformToInt().ensurePositive() }
 *     return User(name, age)
 * }
 * ```
 *
 * ## Nested Validation
 *
 * `capture` is particularly useful for composing nested builder functions:
 *
 * ```kotlin
 * context(_: Validation)
 * fun buildPerson(rawName: String, rawAge: String): Person {
 *     val name by capture { rawName.ensureNotBlank() }
 *     val age by capture { buildAge(rawAge) }  // nested builder call
 *     return Person(name, age)
 * }
 * ```
 *
 * ## Error Path Naming
 *
 * The property name automatically becomes the path segment in validation errors.
 * For example, if validation fails for `val age by capture { ... }`, the error path
 * will include "age" as the segment name.
 *
 * @context Validation The validation context that manages constraint checking, error accumulation,
 *                     and path tracking. This context is automatically available in functions
 *                     declared with `context(_: Validation)`.
 * @param S The type of the validated output value
 * @param block The validation block that produces the validated value. This block executes
 *              within the current [Validation] context and can use any validator functions.
 * @return A [Capture] instance that provides property delegation with automatic path naming
 */
context(v: Validation)
public fun <S> capture(block: context(Validation)() -> S): Capture<S> = Capture(v, block)

/**
 * A property delegate provider that enables accumulating validation with automatic path naming.
 *
 * `Capture` implements the property delegate provider pattern, allowing validation blocks
 * to be used with Kotlin's `by` delegation syntax. When a property is delegated to a `Capture`,
 * the validation block is executed with the property name automatically added to the error path.
 *
 * This class is not typically instantiated directly. Instead, use the [capture] function.
 *
 * ## How It Works
 *
 * 1. The [capture] function creates a `Capture` instance with the validation context and block
 * 2. When property delegation occurs, [provideDelegate] is called
 * 3. The validation block executes in accumulating mode within a named path segment
 * 4. The result is wrapped in an [Accumulate.Value] that either holds the value or accumulates errors
 *
 * ## Accumulating Mode
 *
 * Validation runs in accumulating mode, meaning all validation errors are collected
 * rather than stopping at the first error. This allows multiple validation errors to be
 * reported at once.
 *
 * @param S The type of the validated output value
 * @property validation The validation context used when executing the validation block
 * @property block The validation block that produces the validated value
 * @see capture
 * @see Accumulate.Value
 */
public class Capture<S>(
    private val validation: Validation,
    private val block: context(Validation)
    () -> S,
) {
    /**
     * Provides the property delegate by executing the validation block.
     *
     * This method is called by Kotlin's delegation mechanism when a property is declared
     * with `by capture { ... }`. It executes the captured validation block within a named
     * path segment derived from the property name.
     *
     * ## Path Naming
     *
     * The property name becomes the path segment for any validation errors. For example:
     * ```kotlin
     * val email by capture { rawEmail.ensureEmail() }
     * // If validation fails, error path will be: "email"
     * ```
     *
     * ## Accumulating Behavior
     *
     * The validation block runs in accumulating mode via [accumulating], which means:
     * - All validation constraints are evaluated even if some fail
     * - Errors are collected and stored in the returned [Accumulate.Value]
     * - The value can be accessed later, triggering accumulated errors if any exist
     *
     * @param thisRef The object containing the delegated property (unused)
     * @param property The property metadata providing the name for the validation path segment
     * @return An [Accumulate.Value] containing either the validated result or accumulated errors
     */
    public operator fun provideDelegate(
        thisRef: Any?,
        property: KProperty<*>,
    ): Accumulate.Value<S> =
        context(validation) {
            addPath(property.name, null) {
                when (val result = ior { block() }) {
                    is ValidationResult.Success<S> -> Accumulate.Ok(result.value)
                    is ValidationIor.FailureLike<S> -> accumulate(result.messages)
                }
            }
        }
}
