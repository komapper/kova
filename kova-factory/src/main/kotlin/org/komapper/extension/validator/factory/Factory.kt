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
fun <R : Any> Factory<R>.tryCreate(config: ValidationConfig = ValidationConfig()): ValidationResult<R> =
    execute(Unit, ValidationContext(config = config))

/**
 * Creates an instance using this factory and returns the result.
 *
 * @param config the validation configuration to use
 * @return the created instance if validation passes
 * @throws ValidationException if validation fails
 */
fun <R : Any> Factory<R>.create(config: ValidationConfig = ValidationConfig()): R =
    when (val result = execute(Unit, ValidationContext(config = config))) {
        is ValidationResult.Success -> result.value
        is ValidationResult.Failure -> throw ValidationException(result.messages)
    }

/**
 * Generates a factory from this validator.
 *
 * The factory uses a [FactoryScope] to collect and validate individual fields,
 * then applies this validator to the final constructed object.
 *
 * @param root the root name for the validation context
 * @param block the factory definition block that produces a validation result
 * @return a factory that validates according to the defined logic
 */
fun <R : Any> IdentityValidator<R>.generateFactory(
    root: String = "factory",
    block: FactoryScope<R>.() -> ValidationResult<R>,
): Factory<R> =
    Factory { _, context ->
        val rootContext = context.addRoot(root, null)
        val factories = mutableListOf<Factory<*>>()
        val factoryScope = FactoryScope<R>(rootContext, factories, this)
        block(factoryScope)
    }

/**
 * Creates a factory that produces validated instances of type [R].
 *
 * Factories combine construction and validation into a single operation.
 * Use [FactoryScope.check] to validate individual fields, then [FactoryScope.create]
 * to construct the final object.
 *
 * Example:
 * ```kotlin
 * val userFactory = Kova.factory<User> {
 *     val name = check("name", rawName) { it.min(1).max(50) }
 *     val age = check("age", rawAge) { it.min(0).max(150) }
 *     create { User(name(), age()) }
 * }
 * val user = userFactory.create()
 * ```
 *
 * @param root the root name for the validation context
 * @param block the factory definition block that produces a validation result
 * @return a factory that validates and constructs instances of type [R]
 */
fun <R : Any> Kova.factory(
    root: String = "factory",
    block: FactoryScope<R>.() -> ValidationResult<R>,
): Factory<R> = Validator.success<R>().generateFactory(root, block)

/**
 * Scope for defining factory validation logic.
 *
 * Provides methods to check individual fields and create the final validated object.
 * Fields must be checked using [check] before they can be accessed in the [create] block.
 *
 * @param R the type of object being constructed
 */
class FactoryScope<R>(
    private val context: ValidationContext,
    private val factories: MutableList<Factory<*>>,
    private val validator: Validator<R, R>,
) {
    /**
     * Registers a value to be validated when [create] is called.
     *
     * The validation is deferred until [create] is invoked. The resulting value can be
     * accessed in the [create] block by invoking the returned [ValueRef].
     *
     * @param name the field name for error reporting
     * @param value the value to validate
     * @param block validator definition that transforms the value
     * @return a reference to the value that will be validated and accessible in [create]
     */
    fun <T, S> check(
        name: String,
        value: T,
        block: (Validator<T, T>) -> Validator<T, S>,
    ): ValueRef<S> {
        val factory =
            Factory { _, context ->
                val validator = block(Validator.success())
                validator.execute(value, context.addPath(name, value))
            }
        factories.add(factory)
        return ValueRef(factory)
    }

    /**
     * Registers an existing factory to be executed when [create] is called.
     *
     * The factory's result can be accessed in the [create] block by invoking the returned [ValueRef].
     *
     * @param name the field name for error reporting
     * @param factory the factory to register
     * @return a reference to the value that will be produced by the factory
     */
    fun <S> check(
        name: String,
        factory: Factory<S>,
    ): ValueRef<S> {
        val namedFactory = factory.name(name)
        factories.add(namedFactory)
        return ValueRef(namedFactory)
    }

    /**
     * Creates the final object after validating all checked fields.
     *
     * All factories registered via [check] are executed. If all validations pass,
     * the provided block is executed to construct the object, which is then validated
     * by the factory's validator.
     *
     * @param block construction block that creates the object using validated values
     * @return validation result containing the constructed and validated object
     */
    fun create(block: CreationScope.() -> R): ValidationResult<R> {
        val resultMap = mutableMapOf<Factory<*>, ValidationResult<*>>()
        for (factory in factories) {
            val result = factory.execute(Unit, context)
            if (result.isFailure()) {
                if (context.failFast) {
                    return ValidationResult.Failure(Input.Unusable(Unit), result.messages)
                }
            }
            resultMap[factory] = result
        }
        val valid = resultMap.values.all { it.isSuccess() }
        return if (valid) {
            val scope = CreationScope(resultMap.mapValues { (_, value) -> value as ValidationResult.Success<*> })
            val obj = scope.block()
            val newContext = ValidationContext(config = context.config) // reset context
            validator.execute(obj, newContext)
        } else {
            val messages = resultMap.values.filterIsInstance<ValidationResult.Failure<*>>().flatMap { it.messages }
            ValidationResult.Failure(Input.Unusable(Unit), messages)
        }
    }
}

/**
 * Scope for constructing objects using validated values.
 *
 * Within this scope, [ValueRef] instances returned from [FactoryScope.check] can be invoked
 * to retrieve their validated values for object construction.
 */
class CreationScope(
    private val resultMap: Map<Factory<*>, ValidationResult.Success<*>>,
) {
    /**
     * Retrieves the validated value from a [ValueRef].
     *
     * This operator allows invoking [ValueRef] instances as functions to access their validated values.
     *
     * @return the validated value
     * @throws IllegalStateException if the value reference was not created via [FactoryScope.check]
     */
    operator fun <T> ValueRef<T>.invoke(): T {
        val result =
            resultMap[factory]
                ?: error("ValidationResult not found for factory '$factory'. This is an internal error.")
        @Suppress("UNCHECKED_CAST")
        return result.value as T
    }
}

/**
 * A reference to a value that will be validated when the factory is executed.
 *
 * ValueRef is returned by [FactoryScope.check] and represents a value that has been
 * registered for validation but not yet validated. The actual validation occurs when
 * [FactoryScope.create] is called. Within the [CreationScope], a ValueRef can be invoked
 * as a function to retrieve the validated value.
 *
 * Example:
 * ```kotlin
 * val userFactory = Kova.factory<User> {
 *     val name: ValueRef<String> = check("name", rawName) { it.min(1).max(50) }
 *     val age: ValueRef<Int> = check("age", rawAge) { it.positive() }
 *     create {
 *         User(name(), age()) // Invoke ValueRefs to get validated values
 *     }
 * }
 * ```
 *
 * @param R the type of value referenced
 */
class ValueRef<R> internal constructor(
    internal val factory: Factory<R>,
)
