package org.komapper.extension.validator

import org.komapper.extension.validator.ObjectValidator.Rule
import kotlin.reflect.KProperty1

class ObjectValidator<T : Any> internal constructor(
    private val block: ObjectValidatorScope<T>.() -> Unit,
) : Validator<T, T> {
    override fun tryValidate(
        input: T,
        context: ValidationContext,
    ): ValidationResult<T> {
        val context = context.addRoot(input::class.toString())
        val data = extractData(block)
        val ruleResult = applyRules(input, context, data.ruleMap)
        val constraintResult = applyConstraints(input, context, data.constraints)
        if (context.failFast && ruleResult.isFailure()) {
            return ruleResult
        }
        if (context.failFast && constraintResult.isFailure()) {
            return constraintResult
        }
        return ruleResult + constraintResult
    }

    operator fun plus(other: ObjectValidator<T>): ObjectValidator<T> =
        ObjectValidator {
            block()
            other.block(this)
        }

    fun merge(other: ObjectValidatorScope<T>.() -> Unit): ObjectValidator<T> =
        ObjectValidator {
            block()
            other(this)
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
        return when (val result = validator.tryValidate(value, newContext)) {
            is ValidationResult.Success -> ValidationResult.Success(input, result.context)
            is ValidationResult.Failure -> ValidationResult.Failure(result.details)
        }
    }

    private fun applyConstraints(
        input: T,
        context: ValidationContext,
        constraints: List<Constraint<T>>,
    ): ValidationResult<T> {
        val validator = CoreValidator(constraints)
        return validator.tryValidate(input, context)
    }

    data class Rule(
        val transform: (Any?) -> Any?,
        val choose: (Any?) -> Validator<Any?, Any?>,
    )
}

@KovaMarker
class ObjectValidatorScope<T : Any>(
    private val ruleMap: MutableMap<String, Rule>,
    private val constraints: MutableList<Constraint<T>>,
) : KovaValidatorScope by KovaValidatorScope {
    fun <V> prop(
        key: KProperty1<T, V>,
        choose: (T) -> Validator<V, V>,
    ) {
        val transform = { receiver: T -> key.get(receiver) }
        return addRule(key.name, transform, choose)
    }

    fun <V> named(
        name: String,
        transform: (T) -> V,
        choose: (T) -> Validator<V, V>,
    ) = addRule(name, transform, choose)

    private fun <V> addRule(
        key: String,
        transform: (T) -> V,
        choose: (T) -> Validator<V, V>,
    ) {
        transform as (Any?) -> Any?
        choose as (Any?) -> Validator<Any?, Any?>
        ruleMap[key] = Rule(transform, choose)
    }

    fun constraint(constraint: Constraint<T>) = constraints.add(constraint)

    operator fun <V> KProperty1<T, V>.invoke(choose: (T) -> Validator<V, V>) {
        prop(this, choose)
    }
}

private data class ObjectValidatorScopeData<T : Any>(
    val ruleMap: Map<String, Rule>,
    val constraints: List<Constraint<T>>,
)

private fun <T : Any> extractData(block: ObjectValidatorScope<T>.() -> Unit): ObjectValidatorScopeData<T> {
    val ruleMap = mutableMapOf<String, Rule>()
    val constraints = mutableListOf<Constraint<T>>()
    val scope = ObjectValidatorScope(ruleMap, constraints)
    scope.apply(block)
    return ObjectValidatorScopeData(ruleMap, constraints)
}
