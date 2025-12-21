package org.komapper.extension.validator.factory

import org.komapper.extension.validator.IdentityValidator
import org.komapper.extension.validator.Input
import org.komapper.extension.validator.Kova
import org.komapper.extension.validator.ValidationConfig
import org.komapper.extension.validator.ValidationContext
import org.komapper.extension.validator.ValidationException
import org.komapper.extension.validator.ValidationResult
import org.komapper.extension.validator.Validator
import org.komapper.extension.validator.addPath
import org.komapper.extension.validator.addRoot
import org.komapper.extension.validator.isFailure
import org.komapper.extension.validator.isSuccess
import org.komapper.extension.validator.name
import org.komapper.extension.validator.tryValidate
import org.komapper.extension.validator.validate
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * A factory that produces validated instances of type [R].
 *
 * Factory is a specialized validator that takes no input (Unit) and produces
 * a validated result of type [R]. It combines construction and validation
 * into a single operation.
 *
 * @param R the type of object produced by this factory
 */
typealias Factory<R> = Validator<Unit, R>

/**
 * Creates an instance using this factory and returns the validation result.
 *
 * This is the safe variant that returns a [ValidationResult] rather than throwing exceptions.
 *
 * @param config the validation configuration to use
 * @return [ValidationResult.Success] with the created instance if validation passes,
 *         [ValidationResult.Failure] with validation errors otherwise
 */
fun <R : Any> Factory<R>.tryCreate(config: ValidationConfig = ValidationConfig()): ValidationResult<R> = tryValidate(Unit, config)

/**
 * Creates an instance using this factory and returns the result.
 *
 * @param config the validation configuration to use
 * @return the created instance if validation passes
 * @throws ValidationException if validation fails
 */
fun <R : Any> Factory<R>.create(config: ValidationConfig = ValidationConfig()): R = validate(Unit, config)

/**
 * Creates a factory from this validator.
 *
 * The factory uses a [FactoryScope] to collect and validate individual fields during construction,
 * then applies this validator to the final constructed object. This is useful for combining
 * ObjectSchemas with factory-based construction.
 *
 * Example:
 * ```kotlin
 * object UserSchema : ObjectSchema<User>({
 *     User::age { it.min(0).max(120) }
 * })
 *
 * fun createUser(name: String, age: String) = UserSchema.factory {
 *     val name by bind(name) { it.notBlank() }
 *     val age by bind(age) { it.toInt() }
 *     create { User(name(), age()) }
 * }
 * ```
 *
 * @param root the root name for the validation context
 * @param block the factory definition block that produces a validation result
 * @return a factory that validates fields during construction and applies this validator to the result
 */
fun <R : Any> IdentityValidator<R>.factory(
    root: String = "factory",
    block: FactoryScope<R>.() -> ValidationResult<R>,
): Factory<R> =
    Factory { _ ->
        addRoot(root, null) {
            val factoryScope = FactoryScope(this, this@factory)
            block(factoryScope)
        }
    }

/**
 * Creates a factory that produces validated instances of type [R].
 *
 * Factories combine construction and validation into a single operation.
 * Use [FactoryScope.bind] to validate individual fields or compose other factories,
 * then [FactoryScope.create] to construct the final object.
 *
 * Example:
 * ```kotlin
 * val userFactory = Kova.factory<User> {
 *     val name by bind(rawName) { it.min(1).max(50) }
 *     val age by bind(rawAge) { it.min(0).max(150) }
 *     create { User(name(), age()) }
 * }
 * val user = userFactory.create()
 * ```
 *
 * Factories can also be composed:
 * ```kotlin
 * val addressFactory = Kova.factory<Address> { /* ... */ }
 * val userFactory = Kova.factory<User> {
 *     val name by bind(rawName) { it.notBlank() }
 *     val address by bind(addressFactory)
 *     create { User(name(), address()) }
 * }
 * ```
 *
 * @param root the root name for the validation context
 * @param block the factory definition block that produces a validation result
 * @return a factory that validates and constructs instances of type [R]
 */
fun <R : Any> Kova.factory(
    root: String = "factory",
    block: FactoryScope<R>.() -> ValidationResult<R>,
): Factory<R> = Validator.success<R>().factory(root, block)

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
    private val validator: Validator<R, R>,
) {
    private val results = mutableListOf<ValidationResult<*>>()

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
     * @param value the value to validate
     * @param block a function that receives a base validator and returns a configured validator
     * @return a property delegate provider that creates a [ValueRef] for accessing the validated value
     */
    fun <T, S> bind(
        value: T,
        block: (Validator<T, T>) -> Validator<T, S>,
    ) = createPropertyDelegateProvider {
        context.addPath(it.name, value) {
            val validator = block(Validator.success())
            validator.execute(value)
        }
    }

    /**
     * Binds a factory to this factory, returning a [ValueRef] for the factory's result.
     *
     * This enables composing factories to build complex nested objects.
     * The property name is automatically used as the validation path.
     *
     * Example:
     * ```kotlin
     * val address by bind(addressFactory)
     * ```
     *
     * @param S the type produced by the factory
     * @param factory the factory to bind
     * @return a property delegate provider that creates a [ValueRef] for accessing the factory's result
     */
    fun <S> bind(factory: Factory<S>) =
        createPropertyDelegateProvider {
            with(context) {
                factory.name(it.name).execute(Unit)
            }
        }

    private fun <S> createPropertyDelegateProvider(execute: (KProperty<*>) -> ValidationResult<S>) =
        PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, ValueRef<S>>> { _, property ->
            val result =
                if (context.failFast && results.any { it.isFailure() }) {
                    null
                } else {
                    execute(property)
                }
            if (result != null) results.add(result)
            ReadOnlyProperty { _, _ -> ValueRef(result) }
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
        if (results.all { it.isSuccess() }) {
            val obj = block(CreationScope())
            // reset context
            with(ValidationContext(config = context.config)) { validator.execute(obj) }
        } else {
            val messages = results.filterIsInstance<ValidationResult.Failure<*>>().flatMap { it.messages }
            ValidationResult.Failure(Input.Unusable(Unit), messages)
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
    operator fun <T> ValueRef<T>.invoke(): T {
        if (validationResult == null || validationResult.isFailure()) error("ValueRef is illegal.")
        return validationResult.value
    }
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
class ValueRef<R> internal constructor(
    internal val validationResult: ValidationResult<R>?,
)
