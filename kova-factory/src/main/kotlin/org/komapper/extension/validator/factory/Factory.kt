package org.komapper.extension.validator.factory

import org.komapper.extension.validator.Validation
import org.komapper.extension.validator.addRoot
import org.komapper.extension.validator.bindObject

inline fun <R> Validation.factory(
    name: String = "factory",
    block: Validation.() -> R,
): R = addRoot(name, null) { block() }

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
    block: Validation.(T) -> S,
): Validation.() -> S = { bindObject(input) { block(input) } }

fun <S> bind(block: Validation.() -> S) = block
