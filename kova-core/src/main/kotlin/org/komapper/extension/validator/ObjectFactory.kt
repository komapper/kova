package org.komapper.extension.validator

import kotlin.reflect.KFunction

private fun <T> ValidationContext.shouldReturnEarly(validationResult: ValidationResult<T>): Boolean =
    failFast && validationResult.isFailure()

private fun <T> tryConstruct(
    context: ValidationContext,
    validator: Validator<T, T>,
    block: () -> T,
): ValidationResult<T> {
    val instance = block()
    return validator.execute(instance, context)
}

private fun <T : Any> createFailure(
    arg: ValidationResult<*>,
    vararg args: ValidationResult<*>,
): ValidationResult<T> {
    val result =
        (listOf(arg) + args)
            .filterIsInstance<ValidationResult.Failure>()
            .map { it as ValidationResult<Any?> }
            .reduce { a, b -> a + b }
    return when (result) {
        is ValidationResult.Success -> error("This should never happen.")
        is ValidationResult.Failure -> result
    }
}

private fun <T> unwrapValidationResult(result: ValidationResult<T>): T =
    when (result) {
        is ValidationResult.Success -> result.value
        is ValidationResult.Failure -> throw ValidationException(result.details)
    }

/**
 * Factory for validating inputs and constructing objects.
 *
 * ObjectFactory combines validation with object construction, allowing you to
 * validate multiple inputs and then construct an object only if all validations succeed.
 * This is commonly used in ObjectSchema to create validated instances.
 *
 * Example:
 * ```kotlin
 * data class Person(val name: String, val age: Int)
 *
 * object PersonSchema : ObjectSchema<Person>() {
 *     private val name = Person::name { Kova.string().min(1).max(50) }
 *     private val age = Person::age { Kova.int().min(0).max(120) }
 *
 *     fun build(nameInput: String, ageInput: Int) =
 *         arguments(
 *             arg(nameInput, name),
 *             arg(ageInput, age)
 *         ).build(::Person)
 * }
 *
 * // Usage
 * val result = PersonSchema.build("Alice", 30).tryCreate()
 * when (result) {
 *     is ValidationResult.Success -> println("Created: ${result.value}")
 *     is ValidationResult.Failure -> println("Errors: ${result.details}")
 * }
 * ```
 *
 * @param T The type of object this factory creates
 */
fun interface ObjectFactory<T> {
    /**
     * Executes validation and object construction.
     *
     * @param context The validation context
     * @return A validation result containing either the constructed object or failure details
     */
    fun execute(context: ValidationContext): ValidationResult<T>
}

/**
 * Validates inputs and attempts to create an object, returning a [ValidationResult].
 *
 * This is the recommended way to use ObjectFactory when you want to handle
 * both success and failure cases programmatically.
 *
 * Example:
 * ```kotlin
 * val factory = PersonSchema.build("Alice", 30)
 * val result = factory.tryCreate()
 * when (result) {
 *     is ValidationResult.Success -> println("Created: ${result.value}")
 *     is ValidationResult.Failure -> result.details.forEach { println(it.message.content) }
 * }
 * ```
 *
 * @param config Configuration options for validation (failFast, logging)
 * @return A validation result containing either the created object or failure details
 */
fun <T> ObjectFactory<T>.tryCreate(config: ValidationConfig = ValidationConfig()): ValidationResult<T> =
    execute(ValidationContext(config = config))

/**
 * Validates inputs and creates an object, or throws an exception on failure.
 *
 * Use this when you want validation failures to throw exceptions rather than
 * handling them programmatically.
 *
 * Example:
 * ```kotlin
 * try {
 *     val person = PersonSchema.build("Alice", 30).create()
 *     println("Created: $person")
 * } catch (e: ValidationException) {
 *     println("Validation failed: ${e.messages}")
 * }
 * ```
 *
 * @param config Configuration options for validation (failFast, logging)
 * @return The created object of type [T]
 * @throws ValidationException if validation fails
 */
fun <T> ObjectFactory<T>.create(config: ValidationConfig = ValidationConfig()): T {
    val result = execute(ValidationContext(config = config))
    return unwrapValidationResult(result)
}

internal data class FunctionDesc(
    val name: String,
    private val parameters: Map<Int, String?>,
) {
    operator fun get(index: Int): String {
        if (index < 0 || parameters.size <= index) return "param$index"
        return parameters[index] ?: "param$index"
    }
}

private val isKotlinReflectAvailable: Boolean =
    try {
        Class.forName("kotlin.reflect.full.KClasses")
        true
    } catch (ignored: ClassNotFoundException) {
        false
    }

@Suppress("NO_REFLECTION_IN_CLASS_PATH")
private fun introspectFunction(ctor: Any): FunctionDesc =
    if (ctor is KFunction<*>) {
        val parameters =
            if (isKotlinReflectAvailable) {
                ctor.parameters.withIndex().associate { (i, p) -> i to p.name }
            } else {
                emptyMap()
            }
        FunctionDesc(ctor.name, parameters)
    } else {
        FunctionDesc(ctor.toString(), emptyMap())
    }

sealed interface Arg<OUT> : ObjectFactory<OUT> {
    val value: Any?

    data class Value<IN, OUT>(
        override val value: IN,
        val validator: Validator<IN, OUT>,
    ) : Arg<OUT> {
        override fun execute(context: ValidationContext): ValidationResult<OUT> = validator.execute(value, context)
    }

    data class Factory<IN, OUT>(
        val factory: ObjectFactory<IN>,
        val validator: Validator<IN, OUT>,
    ) : Arg<OUT> {
        override val value: Any = factory

        override fun execute(context: ValidationContext): ValidationResult<OUT> =
            when (val result = factory.execute(context)) {
                is ValidationResult.Success -> validator.execute(result.value, result.context)
                is ValidationResult.Failure -> result
            }
    }
}

data class Arguments<T0, R>(
    val validator: Validator<R, R>,
    val arg0: Arg<T0>,
) {
    fun build(ctor: (T0) -> R): ObjectFactory<R> =
        ObjectFactory {
            val funInfo = introspectFunction(ctor)
            val context = it.addRoot(funInfo.name, ctor)
            val result0 = arg0.execute(context.addPath(funInfo.get(0), arg0.value))
            if (result0.isSuccess()) {
                tryConstruct(context, validator) {
                    ctor(result0.value)
                }
            } else {
                createFailure(result0)
            }
        }
}

data class Arguments1<T0, T1, R>(
    val validator: Validator<R, R>,
    val arg0: Arg<T0>,
    val arg1: Arg<T1>,
) {
    fun build(ctor: (T0, T1) -> R): ObjectFactory<R> {
        return ObjectFactory {
            val funInfo = introspectFunction(ctor)
            val context = it.addRoot(funInfo.name, ctor)
            val result0 =
                arg0.execute(context.addPath(funInfo.get(0), arg0.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result1 =
                arg1.execute(context.addPath(funInfo.get(1), arg1.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            if (result0.isSuccess() && result1.isSuccess()) {
                tryConstruct(context, validator) {
                    ctor(result0.value, result1.value)
                }
            } else {
                createFailure(result0, result1)
            }
        }
    }
}

data class Arguments2<T0, T1, T2, R>(
    val validator: Validator<R, R>,
    val arg0: Arg<T0>,
    val arg1: Arg<T1>,
    val arg2: Arg<T2>,
) {
    fun build(ctor: (T0, T1, T2) -> R): ObjectFactory<R> {
        return ObjectFactory {
            val funInfo = introspectFunction(ctor)
            val context = it.addRoot(funInfo.name, ctor)
            val result0 =
                arg0.execute(context.addPath(funInfo.get(0), arg0.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result1 =
                arg1.execute(context.addPath(funInfo.get(1), arg1.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result2 =
                arg2.execute(context.addPath(funInfo.get(2), arg2.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            if (result0.isSuccess() && result1.isSuccess() && result2.isSuccess()) {
                tryConstruct(context, validator) {
                    ctor(result0.value, result1.value, result2.value)
                }
            } else {
                createFailure(result0, result1, result2)
            }
        }
    }
}

data class Arguments3<T0, T1, T2, T3, R>(
    val validator: Validator<R, R>,
    val arg0: Arg<T0>,
    val arg1: Arg<T1>,
    val arg2: Arg<T2>,
    val arg3: Arg<T3>,
) {
    fun build(ctor: (T0, T1, T2, T3) -> R): ObjectFactory<R> {
        return ObjectFactory {
            val funInfo = introspectFunction(ctor)
            val context = it.addRoot(funInfo.name, ctor)
            val result0 =
                arg0.execute(context.addPath(funInfo.get(0), arg0.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result1 =
                arg1.execute(context.addPath(funInfo.get(1), arg1.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result2 =
                arg2.execute(context.addPath(funInfo.get(2), arg2.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result3 =
                arg3.execute(context.addPath(funInfo.get(3), arg3.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            if (result0.isSuccess() && result1.isSuccess() && result2.isSuccess() && result3.isSuccess()) {
                tryConstruct(context, validator) {
                    ctor(result0.value, result1.value, result2.value, result3.value)
                }
            } else {
                createFailure(result0, result1, result2, result3)
            }
        }
    }
}

data class Arguments4<T0, T1, T2, T3, T4, R>(
    val validator: Validator<R, R>,
    val arg0: Arg<T0>,
    val arg1: Arg<T1>,
    val arg2: Arg<T2>,
    val arg3: Arg<T3>,
    val arg4: Arg<T4>,
) {
    fun build(ctor: (T0, T1, T2, T3, T4) -> R): ObjectFactory<R> {
        return ObjectFactory {
            val funInfo = introspectFunction(ctor)
            val context = it.addRoot(funInfo.name, ctor)
            val result0 =
                arg0.execute(context.addPath(funInfo.get(0), arg0.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result1 =
                arg1.execute(context.addPath(funInfo.get(1), arg1.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result2 =
                arg2.execute(context.addPath(funInfo.get(2), arg2.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result3 =
                arg3.execute(context.addPath(funInfo.get(3), arg3.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result4 =
                arg4.execute(context.addPath(funInfo.get(4), arg4.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            if (result0.isSuccess() && result1.isSuccess() && result2.isSuccess() && result3.isSuccess() && result4.isSuccess()) {
                tryConstruct(context, validator) {
                    ctor(result0.value, result1.value, result2.value, result3.value, result4.value)
                }
            } else {
                createFailure(result0, result1, result2, result3, result4)
            }
        }
    }
}

data class Arguments5<T0, T1, T2, T3, T4, T5, R>(
    val validator: Validator<R, R>,
    val arg0: Arg<T0>,
    val arg1: Arg<T1>,
    val arg2: Arg<T2>,
    val arg3: Arg<T3>,
    val arg4: Arg<T4>,
    val arg5: Arg<T5>,
) {
    fun build(ctor: (T0, T1, T2, T3, T4, T5) -> R): ObjectFactory<R> {
        return ObjectFactory {
            val funInfo = introspectFunction(ctor)
            val context = it.addRoot(funInfo.name, ctor)
            val result0 =
                arg0.execute(context.addPath(funInfo.get(0), arg0.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result1 =
                arg1.execute(context.addPath(funInfo.get(1), arg1.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result2 =
                arg2.execute(context.addPath(funInfo.get(2), arg2.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result3 =
                arg3.execute(context.addPath(funInfo.get(3), arg3.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result4 =
                arg4.execute(context.addPath(funInfo.get(4), arg4.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result5 =
                arg5.execute(context.addPath(funInfo.get(5), arg5.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            if (result0.isSuccess() &&
                result1.isSuccess() &&
                result2.isSuccess() &&
                result3.isSuccess() &&
                result4.isSuccess() &&
                result5.isSuccess()
            ) {
                tryConstruct(context, validator) {
                    ctor(result0.value, result1.value, result2.value, result3.value, result4.value, result5.value)
                }
            } else {
                createFailure(result0, result1, result2, result3, result4, result5)
            }
        }
    }
}

data class Arguments6<T0, T1, T2, T3, T4, T5, T6, R>(
    val validator: Validator<R, R>,
    val arg0: Arg<T0>,
    val arg1: Arg<T1>,
    val arg2: Arg<T2>,
    val arg3: Arg<T3>,
    val arg4: Arg<T4>,
    val arg5: Arg<T5>,
    val arg6: Arg<T6>,
) {
    fun build(ctor: (T0, T1, T2, T3, T4, T5, T6) -> R): ObjectFactory<R> {
        return ObjectFactory {
            val funInfo = introspectFunction(ctor)
            val context = it.addRoot(funInfo.name, ctor)
            val result0 =
                arg0.execute(context.addPath(funInfo.get(0), arg0.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result1 =
                arg1.execute(context.addPath(funInfo.get(1), arg1.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result2 =
                arg2.execute(context.addPath(funInfo.get(2), arg2.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result3 =
                arg3.execute(context.addPath(funInfo.get(3), arg3.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result4 =
                arg4.execute(context.addPath(funInfo.get(4), arg4.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result5 =
                arg5.execute(context.addPath(funInfo.get(5), arg5.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result6 =
                arg6.execute(context.addPath(funInfo.get(6), arg6.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            if (result0.isSuccess() &&
                result1.isSuccess() &&
                result2.isSuccess() &&
                result3.isSuccess() &&
                result4.isSuccess() &&
                result5.isSuccess() &&
                result6.isSuccess()
            ) {
                tryConstruct(context, validator) {
                    ctor(result0.value, result1.value, result2.value, result3.value, result4.value, result5.value, result6.value)
                }
            } else {
                createFailure(result0, result1, result2, result3, result4, result5, result6)
            }
        }
    }
}

data class Arguments7<T0, T1, T2, T3, T4, T5, T6, T7, R>(
    val validator: Validator<R, R>,
    val arg0: Arg<T0>,
    val arg1: Arg<T1>,
    val arg2: Arg<T2>,
    val arg3: Arg<T3>,
    val arg4: Arg<T4>,
    val arg5: Arg<T5>,
    val arg6: Arg<T6>,
    val arg7: Arg<T7>,
) {
    fun build(ctor: (T0, T1, T2, T3, T4, T5, T6, T7) -> R): ObjectFactory<R> {
        return ObjectFactory {
            val funInfo = introspectFunction(ctor)
            val context = it.addRoot(funInfo.name, ctor)
            val result0 =
                arg0.execute(context.addPath(funInfo.get(0), arg0.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result1 =
                arg1.execute(context.addPath(funInfo.get(1), arg1.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result2 =
                arg2.execute(context.addPath(funInfo.get(2), arg2.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result3 =
                arg3.execute(context.addPath(funInfo.get(3), arg3.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result4 =
                arg4.execute(context.addPath(funInfo.get(4), arg4.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result5 =
                arg5.execute(context.addPath(funInfo.get(5), arg5.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result6 =
                arg6.execute(context.addPath(funInfo.get(6), arg6.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result7 =
                arg7.execute(context.addPath(funInfo.get(7), arg7.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            if (result0.isSuccess() &&
                result1.isSuccess() &&
                result2.isSuccess() &&
                result3.isSuccess() &&
                result4.isSuccess() &&
                result5.isSuccess() &&
                result6.isSuccess() &&
                result7.isSuccess()
            ) {
                tryConstruct(context, validator) {
                    ctor(
                        result0.value,
                        result1.value,
                        result2.value,
                        result3.value,
                        result4.value,
                        result5.value,
                        result6.value,
                        result7.value,
                    )
                }
            } else {
                createFailure(result0, result1, result2, result3, result4, result5, result6, result7)
            }
        }
    }
}

data class Arguments8<T0, T1, T2, T3, T4, T5, T6, T7, T8, R>(
    val validator: Validator<R, R>,
    val arg0: Arg<T0>,
    val arg1: Arg<T1>,
    val arg2: Arg<T2>,
    val arg3: Arg<T3>,
    val arg4: Arg<T4>,
    val arg5: Arg<T5>,
    val arg6: Arg<T6>,
    val arg7: Arg<T7>,
    val arg8: Arg<T8>,
) {
    fun build(ctor: (T0, T1, T2, T3, T4, T5, T6, T7, T8) -> R): ObjectFactory<R> {
        return ObjectFactory {
            val funInfo = introspectFunction(ctor)
            val context = it.addRoot(funInfo.name, ctor)
            val result0 =
                arg0.execute(context.addPath(funInfo.get(0), arg0.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result1 =
                arg1.execute(context.addPath(funInfo.get(1), arg1.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result2 =
                arg2.execute(context.addPath(funInfo.get(2), arg2.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result3 =
                arg3.execute(context.addPath(funInfo.get(3), arg3.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result4 =
                arg4.execute(context.addPath(funInfo.get(4), arg4.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result5 =
                arg5.execute(context.addPath(funInfo.get(5), arg5.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result6 =
                arg6.execute(context.addPath(funInfo.get(6), arg6.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result7 =
                arg7.execute(context.addPath(funInfo.get(7), arg7.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result8 =
                arg8.execute(context.addPath(funInfo.get(8), arg8.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            if (result0.isSuccess() &&
                result1.isSuccess() &&
                result2.isSuccess() &&
                result3.isSuccess() &&
                result4.isSuccess() &&
                result5.isSuccess() &&
                result6.isSuccess() &&
                result7.isSuccess() &&
                result8.isSuccess()
            ) {
                tryConstruct(context, validator) {
                    ctor(
                        result0.value,
                        result1.value,
                        result2.value,
                        result3.value,
                        result4.value,
                        result5.value,
                        result6.value,
                        result7.value,
                        result8.value,
                    )
                }
            } else {
                createFailure(result0, result1, result2, result3, result4, result5, result6, result7, result8)
            }
        }
    }
}

data class Arguments9<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, R>(
    val validator: Validator<R, R>,
    val arg0: Arg<T0>,
    val arg1: Arg<T1>,
    val arg2: Arg<T2>,
    val arg3: Arg<T3>,
    val arg4: Arg<T4>,
    val arg5: Arg<T5>,
    val arg6: Arg<T6>,
    val arg7: Arg<T7>,
    val arg8: Arg<T8>,
    val arg9: Arg<T9>,
) {
    fun build(ctor: (T0, T1, T2, T3, T4, T5, T6, T7, T8, T9) -> R): ObjectFactory<R> {
        return ObjectFactory {
            val funInfo = introspectFunction(ctor)
            val context = it.addRoot(funInfo.name, ctor)
            val result0 =
                arg0.execute(context.addPath(funInfo.get(0), arg0.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result1 =
                arg1.execute(context.addPath(funInfo.get(1), arg1.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result2 =
                arg2.execute(context.addPath(funInfo.get(2), arg2.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result3 =
                arg3.execute(context.addPath(funInfo.get(3), arg3.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result4 =
                arg4.execute(context.addPath(funInfo.get(4), arg4.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result5 =
                arg5.execute(context.addPath(funInfo.get(5), arg5.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result6 =
                arg6.execute(context.addPath(funInfo.get(6), arg6.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result7 =
                arg7.execute(context.addPath(funInfo.get(7), arg7.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result8 =
                arg8.execute(context.addPath(funInfo.get(8), arg8.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result9 =
                arg9.execute(context.addPath(funInfo.get(9), arg9.value)).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            if (result0.isSuccess() &&
                result1.isSuccess() &&
                result2.isSuccess() &&
                result3.isSuccess() &&
                result4.isSuccess() &&
                result5.isSuccess() &&
                result6.isSuccess() &&
                result7.isSuccess() &&
                result8.isSuccess() &&
                result9.isSuccess()
            ) {
                tryConstruct(context, validator) {
                    ctor(
                        result0.value,
                        result1.value,
                        result2.value,
                        result3.value,
                        result4.value,
                        result5.value,
                        result6.value,
                        result7.value,
                        result8.value,
                        result9.value,
                    )
                }
            } else {
                createFailure(result0, result1, result2, result3, result4, result5, result6, result7, result8, result9)
            }
        }
    }
}
