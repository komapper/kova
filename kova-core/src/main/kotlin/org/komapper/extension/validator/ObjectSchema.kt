package org.komapper.extension.validator

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

open class ObjectSchema<T : Any> private constructor(
    private val ruleMap: MutableMap<String, Rule>,
    private val block: ObjectSchemaScope<T>.() -> Unit = {},
) : Validator<T, T> {
    constructor(block: ObjectSchemaScope<T>.() -> Unit = {}) : this(mutableMapOf(), block)

    override fun execute(
        context: ValidationContext,
        input: T,
    ): ValidationResult<T> {
        val constraints: MutableList<Constraint<T>> = mutableListOf()
        block(ObjectSchemaScope(this, ruleMap, constraints))
        val context = context.addRoot(input::class.toString())
        val ruleResult = applyRules(input, context, ruleMap)
        val constraintResult = applyConstraints(input, context, constraints)
        if (context.failFast && ruleResult.isFailure()) {
            return ruleResult
        }
        if (context.failFast && constraintResult.isFailure()) {
            return constraintResult
        }
        return ruleResult + constraintResult
    }

    private fun applyRules(
        input: T,
        context: ValidationContext,
        ruleMap: Map<String, Rule>,
    ): ValidationResult<T> {
        val results = mutableListOf<ValidationResult<T>>()
        for ((key, rule) in ruleMap) {
            val result = applyRule(input, context, key, rule)
            if (result.isFailure() && context.failFast) {
                return result
            }
            results.add(result)
        }
        return results.fold(ValidationResult.Success(input, context), ValidationResult<T>::plus)
    }

    private fun applyRule(
        input: T,
        context: ValidationContext,
        key: String,
        rule: Rule,
    ): ValidationResult<T> {
        // TODO exception handling
        val newContext = context.addPath(key)
        val value = rule.transform(input)
        val validator = rule.choose(input)
        return when (val result = validator.execute(newContext, value)) {
            is ValidationResult.Success -> ValidationResult.Success(input, result.context)
            is ValidationResult.Failure -> ValidationResult.Failure(result.details)
        }
    }

    private fun applyConstraints(
        input: T,
        context: ValidationContext,
        constraints: List<Constraint<T>>,
    ): ValidationResult<T> {
        val validator =
            constraints
                .map { ConstraintValidator(it) }
                .fold(EmptyValidator<T>() as Validator<T, T>) { acc, v -> acc + v }
        return validator.execute(context, input)
    }

    fun <V> replace(
        key: KProperty1<T, V>,
        validator: Validator<V, V>,
    ): ObjectSchema<T> {
        val rule =
            Rule(
                transform = { receiver: T -> key.get(receiver) } as (Any?) -> Any?,
                choose = { _: T -> validator } as (Any?) -> Validator<Any?, Any?>,
            )
        val newRuleMap = ruleMap.toMutableMap()
        newRuleMap.replace(key.name, rule)
        return ObjectSchema(newRuleMap)
    }

    operator fun <V, VALIDATOR : Validator<V, V>> KProperty1<T, V>.invoke(block: () -> VALIDATOR): VALIDATOR {
        val validator = block()
        ruleMap.addRule(this) { _ -> validator }
        return validator
    }

    infix fun <V, VALIDATOR : Validator<V, V>> KProperty1<T, V>.choose(block: (T) -> VALIDATOR): (T) -> VALIDATOR {
        ruleMap.addRule(this, block)
        return block
    }

    fun <V> named(block: Validator<T, T>.(KProperty<*>) -> Validator<T, V>): ReadOnlyProperty<ObjectSchema<T>, Validator<T, V>> =
        ReadOnlyProperty<ObjectSchema<T>, Validator<T, V>> { thisRef, property ->
            thisRef.path(property.name).block(property)
        }
}

private fun <T, V> MutableMap<String, Rule>.addRule(
    key: KProperty1<T, V>,
    choose: (T) -> Validator<V, V>,
) {
    val transform = { receiver: T -> key.get(receiver) }
    transform as (Any?) -> Any?
    choose as (Any?) -> Validator<Any?, Any?>
    this[key.name] = Rule(transform, choose)
}

internal data class Rule(
    val transform: (Any?) -> Any?,
    val choose: (Any?) -> Validator<Any?, Any?>,
)

class ObjectSchemaScope<T : Any> internal constructor(
    val caller: ObjectSchema<T>,
    private val ruleMap: MutableMap<String, Rule>,
    private val constraints: MutableList<Constraint<T>>,
) : Constrainable<T, Unit> {
    override fun constrain(
        id: String,
        check: ConstraintScope.(ConstraintContext<T>) -> ConstraintResult,
    ) {
        constraints.add(Constraint(id, check))
    }

    operator fun <V> KProperty1<T, V>.invoke(block: () -> Validator<V, V>): Validator<V, V> {
        val validator = block()
        ruleMap.addRule(this) { _ -> validator }
        return validator
    }

    infix fun <V, VALIDATOR : Validator<V, V>> KProperty1<T, V>.choose(block: (T) -> VALIDATOR): (T) -> VALIDATOR {
        ruleMap.addRule(this, block)
        return block
    }
}
