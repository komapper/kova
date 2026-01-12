package org.komapper.extension.validator.factory

import org.komapper.extension.validator.Accumulate
import org.komapper.extension.validator.Validation
import org.komapper.extension.validator.accumulating
import org.komapper.extension.validator.named
import kotlin.reflect.KProperty

/**
 * Binds validation logic without an explicit input value, for nested factory calls.
 *
 * This overload is used when the validation block doesn't need an external input value,
 * typically for composing nested factory calls or performing validation operations
 * that produce their own values.
 *
 * Example:
 * ```kotlin
 * val fullName by bind { buildFullName(firstName, lastName) }
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param S The type of the validated output value
 * @param block A function that returns the validated result
 * @return A [Binder] that provides property delegation with automatic path naming
 */
context(v: Validation)
public fun <S> bind(block: context(Validation)() -> S): Binder<S> = Binder(v, block)

/**
 * A property delegate provider for factory-based validation.
 *
 * This class captures the validation context and validation block, then provides
 * a delegate that executes the validation with the property name as the path segment.
 * It enables the `by bind(...)` syntax in factory blocks.
 *
 * @param S The type of the validated output value
 * @param validation The validation context to use when executing the validation block
 * @param block The validation block that produces the validated value
 */
public class Binder<S>(
    private val validation: Validation,
    private val block: context(Validation)
    () -> S,
) {
    /**
     * Provides a property delegate that executes the validation block.
     *
     * The property name is automatically used as the validation path segment,
     * and the validation runs in accumulating mode to collect all errors.
     *
     * @param thisRef The object containing the delegated property (unused)
     * @param property The property metadata, used to extract the property name for the path
     * @return An [Accumulate.Value] that holds the validated result or accumulates errors
     */
    public operator fun provideDelegate(
        thisRef: Any?,
        property: KProperty<*>,
    ): Accumulate.Value<S> =
        context(validation) {
            null.named(property.name) { accumulating { block() } }
        }
}
