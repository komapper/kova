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
        block(ObjectSchemaScope(constraints))
        val klass = input::class
        val rootName = klass.qualifiedName ?: klass.simpleName ?: klass.toString()
        val context = context.addRoot(rootName, input)
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
        val value = rule.transform(input)
        val validator = rule.choose(input)
        val pathResult = context.addPathChecked(key, value)
        return when (pathResult) {
            is ValidationResult.Success -> {
                when (val result = validator.execute(pathResult.context, value)) {
                    is ValidationResult.Success -> ValidationResult.Success(input, result.context)
                    is ValidationResult.Failure -> ValidationResult.Failure.Simple(result.details)
                }
            }
            // If circular reference detected, terminate validation early with success
            is ValidationResult.Failure -> ValidationResult.Success(input, context)
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
        addRule(this) { _ -> validator }
        return validator
    }

    infix fun <V, VALIDATOR : Validator<V, V>> KProperty1<T, V>.choose(block: (T) -> VALIDATOR): (T) -> VALIDATOR {
        addRule(this, block)
        return block
    }

    private fun <T, V> addRule(
        key: KProperty1<T, V>,
        choose: (T) -> Validator<V, V>,
    ) {
        val transform = { receiver: T -> key.get(receiver) }
        transform as (Any?) -> Any?
        choose as (Any?) -> Validator<Any?, Any?>
        ruleMap[key.name] = Rule(transform, choose)
    }

    fun <IN, OUT> arg(
        value: IN,
        validator: Validator<IN, OUT>,
    ): Arg<OUT> = Arg.Value(value, validator)

    fun <IN, OUT> arg(
        factory: ObjectFactory<IN>,
        validator: Validator<IN, OUT>,
    ): Arg<OUT> = Arg.Factory(factory, validator)

    fun <T0> arguments(arg0: Arg<T0>) = Arguments(this, arg0)

    fun <T0, T1> arguments(
        arg0: Arg<T0>,
        arg1: Arg<T1>,
    ) = Arguments1(this, arg0, arg1)

    fun <T0, T1, T2> arguments(
        arg0: Arg<T0>,
        arg1: Arg<T1>,
        arg2: Arg<T2>,
    ) = Arguments2(this, arg0, arg1, arg2)

    fun <T0, T1, T2, T3> arguments(
        arg0: Arg<T0>,
        arg1: Arg<T1>,
        arg2: Arg<T2>,
        arg3: Arg<T3>,
    ) = Arguments3(this, arg0, arg1, arg2, arg3)

    fun <T0, T1, T2, T3, T4> arguments(
        arg0: Arg<T0>,
        arg1: Arg<T1>,
        arg2: Arg<T2>,
        arg3: Arg<T3>,
        arg4: Arg<T4>,
    ) = Arguments4(this, arg0, arg1, arg2, arg3, arg4)

    fun <T0, T1, T2, T3, T4, T5> arguments(
        arg0: Arg<T0>,
        arg1: Arg<T1>,
        arg2: Arg<T2>,
        arg3: Arg<T3>,
        arg4: Arg<T4>,
        arg5: Arg<T5>,
    ) = Arguments5(this, arg0, arg1, arg2, arg3, arg4, arg5)

    fun <T0, T1, T2, T3, T4, T5, T6> arguments(
        arg0: Arg<T0>,
        arg1: Arg<T1>,
        arg2: Arg<T2>,
        arg3: Arg<T3>,
        arg4: Arg<T4>,
        arg5: Arg<T5>,
        arg6: Arg<T6>,
    ) = Arguments6(this, arg0, arg1, arg2, arg3, arg4, arg5, arg6)

    fun <T0, T1, T2, T3, T4, T5, T6, T7> arguments(
        arg0: Arg<T0>,
        arg1: Arg<T1>,
        arg2: Arg<T2>,
        arg3: Arg<T3>,
        arg4: Arg<T4>,
        arg5: Arg<T5>,
        arg6: Arg<T6>,
        arg7: Arg<T7>,
    ) = Arguments7(this, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7)

    fun <T0, T1, T2, T3, T4, T5, T6, T7, T8> arguments(
        arg0: Arg<T0>,
        arg1: Arg<T1>,
        arg2: Arg<T2>,
        arg3: Arg<T3>,
        arg4: Arg<T4>,
        arg5: Arg<T5>,
        arg6: Arg<T6>,
        arg7: Arg<T7>,
        arg8: Arg<T8>,
    ) = Arguments8(this, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8)

    fun <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> arguments(
        arg0: Arg<T0>,
        arg1: Arg<T1>,
        arg2: Arg<T2>,
        arg3: Arg<T3>,
        arg4: Arg<T4>,
        arg5: Arg<T5>,
        arg6: Arg<T6>,
        arg7: Arg<T7>,
        arg8: Arg<T8>,
        arg9: Arg<T9>,
    ) = Arguments9(this, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9)
}

internal data class Rule(
    val transform: (Any?) -> Any?,
    val choose: (Any?) -> Validator<Any?, Any?>,
)

class ObjectSchemaScope<T : Any> internal constructor(
    private val constraints: MutableList<Constraint<T>>,
) : Constrainable<T, Unit> {
    override fun constrain(
        id: String,
        check: ConstraintScope.(ConstraintContext<T>) -> ConstraintResult,
    ) {
        constraints.add(Constraint(id, check))
    }
}
