package org.komapper.extension.validator.factory

import org.komapper.extension.validator.Message
import org.komapper.extension.validator.ValidationConfig
import org.komapper.extension.validator.ValidationContext
import org.komapper.extension.validator.ValidationResult
import org.komapper.extension.validator.addRoot
import org.komapper.extension.validator.bindObject
import org.komapper.extension.validator.failure
import org.komapper.extension.validator.name
import org.komapper.extension.validator.success
import kotlin.reflect.KProperty

context(_: ValidationContext)
inline fun <R> factory(
    name: String = "factory",
    block: context(ValidationContext) FactoryScope<R>.() -> ValidationResult<R>,
): ValidationResult<R> = addRoot(name, null) { block(contextOf<ValidationContext>(), FactoryScope(contextOf<ValidationContext>())) }

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
    block: context(ValidationContext) (T) -> ValidationResult<S>,
): context(ValidationContext)
() -> ValidationResult<S> = { bindObject(input) { block(input) } }

fun <S> bind(block: context(ValidationContext) () -> ValidationResult<S>) = block

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
    private val context: ValidationContext,
) {
    private val messages = mutableListOf<Message>()

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
    context(ValidationContext)
    () -> ValidationResult<S>
    ).provideDelegate(
        thisRef: Any?,
        property: KProperty<*>,
    ): ValueRef<S> =
        with(context) {
            if (config.failFast && messages.isNotEmpty()) {
                FailureValueRef
            } else {
                when (val result = name(property.name) { this@provideDelegate() }) {
                    is ValidationResult.Success -> SuccessValueRef(result.value)
                    is ValidationResult.Failure -> {
                        messages.addAll(result.messages)
                        FailureValueRef
                    }
                }
            }
        }

    /**
     * Creates the final object after validating all bound fields.
     *
     * All values and factories bound via [bind] are validated. If all validations pass,
     * the provided block is executed to construct the object, which is then validated
     * by the factory's validator (if one was provided).
     *
     * Within the [CreationScope] block, invoke [ValueRef] instances (e.g., `name()`)
     * to access their validated values.
     *
     * @param block construction block that creates the object using validated values
     * @return validation result containing the constructed and validated object, or
     *         validation failure with aggregated error messages if any validation fails
     */
    fun create(block: CreationScope.() -> R): ValidationResult<R> =
        if (messages.isEmpty()) {
            block(CreationScope()).success()
        } else {
            messages.failure()
        }
}

/**
 * Scope for constructing objects using validated values.
 *
 * This scope is provided to the [FactoryScope.create] block and allows invoking [ValueRef]
 * instances as functions to retrieve their validated values. All values are guaranteed to
 * have passed validation when accessed within this scope.
 *
 * Example:
 * ```kotlin
 * create {
 *     User(name(), age())  // Invoke ValueRefs to get validated values
 * }
 * ```
 */
class CreationScope {
    /**
     * Retrieves the validated value from a [ValueRef].
     *
     * This operator allows invoking [ValueRef] instances as functions to access their validated values.
     * This operation is only valid within the [FactoryScope.create] block after all validations have passed.
     *
     * @return the validated value
     * @throws IllegalStateException if the value reference is null or validation failed
     */
    operator fun <T> ValueRef<T>.invoke(): T = value
}

/**
 * A reference to a validated value within a factory.
 *
 * ValueRef is returned by [FactoryScope.bind] and holds the result of a validation.
 * Validation occurs immediately when [FactoryScope.bind] is called (during property initialization),
 * not when the ValueRef is invoked. Within the [CreationScope] of [FactoryScope.create],
 * a ValueRef can be invoked as a function to retrieve its validated value.
 *
 * Example:
 * ```kotlin
 * val userFactory = Kova.factory<User> {
 *     val name: ValueRef<String> by bind(rawName) { it.min(1).max(50) }  // Validates here
 *     val age: ValueRef<Int> by bind(rawAge) { it.positive() }           // Validates here
 *     create {
 *         User(name(), age())  // Retrieves validated values
 *     }
 * }
 * ```
 *
 * @param R the type of value referenced
 */
sealed interface ValueRef<out R> {
    val value: R

    operator fun getValue(
        instance: Any?,
        property: KProperty<*>,
    ): ValueRef<R> = this
}

internal data class SuccessValueRef<out R>(
    override val value: R,
) : ValueRef<R>

internal data object FailureValueRef : ValueRef<Nothing> {
    override val value: Nothing
        get() = error("ValueRef is illegal.")
}
