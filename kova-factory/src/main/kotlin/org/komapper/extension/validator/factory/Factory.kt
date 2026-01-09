package org.komapper.extension.validator.factory

import org.komapper.extension.validator.Validation
import org.komapper.extension.validator.addRoot
import org.komapper.extension.validator.bindObject

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
 *         val age by bind(age) { parseInt(it) }
 *         User(name, age)
 *     }
 * ```
 *
 * @param name The name of the factory root (defaults to "factory")
 * @param block The factory block that builds and returns the validated object
 * @return The result of executing the factory block
 */
inline fun <R> Validation.factory(
    name: String = "factory",
    block: Validation.() -> R,
): R = addRoot(name, null) { block() }

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
 * val id by bind(rawId) { parseInt(it) }
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
    block: Validation.(T) -> S,
): Validation.() -> S = { bindObject(input) { block(input) } }

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
fun <S> bind(block: Validation.() -> S) = block
