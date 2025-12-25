package org.komapper.extension.validator.factory

import org.komapper.extension.validator.Accumulate
import org.komapper.extension.validator.Validation
import org.komapper.extension.validator.ValidationConfig
import org.komapper.extension.validator.accumulating
import org.komapper.extension.validator.addRoot
import org.komapper.extension.validator.bindObject
import org.komapper.extension.validator.name
import kotlin.reflect.KProperty

context(_: Validation, _: Accumulate)
inline fun <R> factory(
    name: String = "factory",
    block: context(Validation) FactoryScope<R>.() -> R,
): R = addRoot(name, null) { block(FactoryScope(contextOf<Validation>(), contextOf<Accumulate>())) }

/**
 * Binds a value to a validator, returning a [ValueRef] that can be invoked in [create].
 *
 * The validator is built by applying the provided block to a base validator.
 * The property name is automatically used as the validation path.
 *
 * Example:
 * ```kotlin
 * val name by bind(rawName) { it.min(1).max(50) }
 * ```
 *
 * @param T the type of the input value
 * @param S the type of the validated output value
 * @param this the value to validate
 * @param block a function that receives a base validator and returns a configured validator
 * @return a property delegate provider that creates a [ValueRef] for accessing the validated value
 */
fun <T, S> bind(
    input: T,
    block: context(Validation, Accumulate) (T) -> S,
): context(Validation, Accumulate)
() -> S = { bindObject(input) { block(input) } }

fun <S> bind(block: context(Validation, Accumulate) () -> S) = block

/**
 * Scope for defining factory validation logic.
 *
 * Provides methods to bind individual fields to validators and create the final validated object.
 * Use [bind] to validate input values or compose other factories, then [create] to construct
 * the final object using the validated values. All bindings are validated before object construction.
 *
 * If failFast mode is enabled in [ValidationConfig], binding stops at the first validation failure.
 *
 * @param R the type of object being constructed
 */
class FactoryScope<R>(
    private val context: Validation,
    private val accumulate: Accumulate,
) {
    /**
     * Binds a factory to this factory, returning a [ValueRef] for the factory's result.
     *
     * This enables composing factories to build complex nested objects.
     * The property name is automatically used as the validation path.
     *
     * Example:
     * ```kotlin
     * val address by addressFactory
     * ```
     *
     * @param S the type produced by the factory
     * @param this the factory to bind
     * @return a [ValueRef] for accessing the factory's result
     */
    operator fun <S> (
    context(Validation, Accumulate)
    () -> S
    ).provideDelegate(
        thisRef: Any?,
        property: KProperty<*>,
    ): Accumulate.Value<S> =
        context(context, accumulate) {
            name(property.name) { accumulating { this@provideDelegate() } }
        }

    operator fun <R> Accumulate.Value<R>.getValue(
        instance: Any?,
        property: KProperty<*>,
    ): R = value
}
