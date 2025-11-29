package org.komapper.extension.validator

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
        block(ObjectSchemaScope(ruleMap, constraints))
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

    private fun <V> addRule(
        key: KProperty1<T, V>,
        choose: (T) -> Validator<V, V>,
    ) {
        val transform = { receiver: T -> key.get(receiver) }
        transform as (Any?) -> Any?
        choose as (Any?) -> Validator<Any?, Any?>
        ruleMap[key.name] = Rule(transform, choose)
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

    operator fun <V> KProperty1<T, V>.invoke(block: () -> Validator<V, V>): PropertyValidator<T, V> {
        val validator = block()
        ruleMap.addRule(this) { _ -> validator }
        return PropertyValidator(this, validator)
    }

    infix fun <V, VALIDATOR : Validator<V, V>> KProperty1<T, V>.choose(block: (T) -> VALIDATOR): (T) -> VALIDATOR {
        ruleMap.addRule(this, block)
        return block
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

private fun <T, V> PropertyValidator(property: KProperty1<T, V>, validator: Validator<V, V>): PropertyValidator<T, V> =
    object : PropertyValidator<T, V> {
        override val property: KProperty1<T, V> = property
        override val validator: Validator<V, V> = validator

        override fun execute(
            context: ValidationContext,
            input: V,
        ): ValidationResult<V> = validator.execute(context, input)
    }

internal data class Rule(
    val transform: (Any?) -> Any?,
    val choose: (Any?) -> Validator<Any?, Any?>,
)

interface PropertyValidator<T, V> : Validator<V, V> {
    val property: KProperty1<T, V>
    val validator: Validator<V, V>
}

class ObjectSchemaScope<T> internal constructor(
    private val ruleMap: MutableMap<String, Rule>,
    private val constraints: MutableList<Constraint<T>>,
) {
    fun constrain(
        key: String,
        check: ConstraintScope.(ConstraintContext<T>) -> ConstraintResult,
    ) {
        constraints.add(Constraint(key, check))
    }

    operator fun <V> KProperty1<T, V>.invoke(block: () -> Validator<V, V>): PropertyValidator<T, V> {
        val validator = block()
        ruleMap.addRule(this) { _ -> validator }
        return PropertyValidator(this, validator)
    }

    infix fun <V, VALIDATOR : Validator<V, V>> KProperty1<T, V>.choose(block: (T) -> VALIDATOR): (T) -> VALIDATOR {
        ruleMap.addRule(this, block)
        return block
    }
}
