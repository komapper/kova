package org.komapper.extension.validator.factory

import org.komapper.extension.validator.Accumulate
import org.komapper.extension.validator.Validation
import org.komapper.extension.validator.accumulating
import org.komapper.extension.validator.addRoot
import org.komapper.extension.validator.bindObject
import org.komapper.extension.validator.name
import kotlin.reflect.KProperty

/**
 * Creates a factory context for building validated objects.
 *
 * The factory function establishes a validation root with the specified name and executes
 * the provided block within that context. Property delegates created with [bind] will
 * automatically use their property names as validation paths.
 *
 * Example:
 * ```kotlin
 * data class User(val name: String, val age: Int)
 *
 * fun Validation.buildUser(name: String, age: String) =
 *     factory {
 *         val name by bind(name) {
 *             ensureNotBlank(it)
 *             it
 *         }
 *         val age by bind(age) { transformToInt(it) }
 *         User(name, age)
 *     }
 * ```
 *
 * @param name The name of the factory root (defaults to "factory")
 * @param block The factory block that builds and returns the validated object
 * @return The result of executing the factory block
 */
context(_: Validation)
inline fun <R> factory(
    name: String = "factory",
    block: context(Validation) Factory.() -> R,
): R = addRoot(name, null) { block(Factory(contextOf<Validation>())) }

/**
 * Binds an input value to validation logic, creating a property delegate for factory contexts.
 *
 * The validation block receives the input value and performs validation operations on it
 * within the [Validation] context. The property name is automatically used as the validation path.
 *
 * Example:
 * ```kotlin
 * val name by bind(rawName) {
 *     ensureNotBlank(it)
 *     ensureLengthInRange(it, 1..50)
 *     it
 * }
 * val id by bind(rawId) { transformToInt(it) }
 * ```
 *
 * @param T The type of the input value
 * @param S The type of the validated output value
 * @param input The value to validate
 * @param block A function that receives the input value and returns the validated result
 * @return A property delegate provider that executes validation when accessed
 */
fun <T, S> bind(
    input: T,
    block: context(Validation)(T) -> S,
): context(Validation)
() -> S = { bindObject(input) { block(input) } }

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
 * @param S The type of the validated output value
 * @param block A function that returns the validated result
 * @return A property delegate provider that executes validation when accessed
 */
fun <S> bind(block: context(Validation)() -> S) = block

class Factory(
    private val validation: Validation,
) {
    /**
     * Binds a validation lambda using property delegation.
     *
     * This enables composing validation logic to build complex nested object validation.
     * The property name is automatically used as the validation path. The validation
     * is executed within an accumulating context that collects errors.
     *
     * @param S the type produced by the validation lambda
     * @return an [Accumulate.Value] for accessing the validation result
     */
    operator fun <S> (
    context(Validation)
    () -> S
    ).provideDelegate(
        thisRef: Any?,
        property: KProperty<*>,
    ): Accumulate.Value<S> =
        context(validation) {
            null.name(property.name) { accumulating { this@provideDelegate() } }
        }
}
