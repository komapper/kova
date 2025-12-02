package org.komapper.extension.validator

private fun <T> ValidationContext.shouldReturnEarly(validationResult: ValidationResult<T>): Boolean =
    failFast && validationResult.isFailure()

private fun <T> tryConstruct(
    context: ValidationContext,
    validator: Validator<T, T>,
    block: () -> T,
): ValidationResult<T> {
    val instance =
        try {
            block()
        } catch (cause: Exception) {
            return ValidationResult.Failure.Simple(
                detail =
                    ValidationResult.FailureDetail(
                        context = context,
                        message = Message.Text(content = cause.toString()),
                        cause = cause,
                    ),
            )
        }
    return validator.execute(context, instance)
}

private fun <T : Any> createFailure(
    arg: ValidationResult<*>,
    vararg args: ValidationResult<*>,
): ValidationResult<T> {
    val result =
        (listOf(arg) + args)
            .map { it as ValidationResult<Any?> }
            .reduce { a, b -> a + b }
    return when (result) {
        is ValidationResult.Success -> error("This should never happen.")
        is ValidationResult.Failure -> ValidationResult.Failure.Simple(result.details)
    }
}

private fun <T> unwrapValidationResult(result: ValidationResult<T>): T =
    when (result) {
        is ValidationResult.Success -> result.value
        is ValidationResult.Failure -> throw ValidationException(result.details)
    }

fun interface ObjectFactory<T> {
    fun execute(context: ValidationContext): ValidationResult<T>
}

fun <T> ObjectFactory<T>.tryCreate(failFast: Boolean = false): ValidationResult<T> = execute(ValidationContext("", failFast = failFast))

fun <T> ObjectFactory<T>.create(failFast: Boolean = false): T {
    val result = execute(ValidationContext("", failFast = failFast))
    return unwrapValidationResult(result)
}

sealed interface Arg<OUT> : ObjectFactory<OUT> {
    data class Value<IN, OUT>(
        val value: IN,
        val validator: Validator<IN, OUT>,
    ) : Arg<OUT> {
        override fun execute(context: ValidationContext): ValidationResult<OUT> = validator.execute(context, value)
    }

    data class Factory<IN, OUT>(
        val factory: ObjectFactory<IN>,
        val validator: Validator<IN, OUT>,
    ) : Arg<OUT> {
        override fun execute(context: ValidationContext): ValidationResult<OUT> =
            when (val result = factory.execute(context)) {
                is ValidationResult.Success -> validator.execute(result.context, result.value)
                is ValidationResult.Failure -> result
            }
    }
}

data class Arguments1<T, R>(
    val validator: Validator<R, R>,
    val arg1: Arg<T>,
) {
    fun build(ctor: (T) -> R): ObjectFactory<R> =
        ObjectFactory {
            val context = it.addRoot(ctor.toString())
            val result1 = arg1.execute(context.addPath("arg1"))
            if (result1.isSuccess()) {
                tryConstruct(context, validator) {
                    ctor(result1.value)
                }
            } else {
                createFailure(result1)
            }
        }
}

data class Arguments2<T1, T2, R>(
    val validator: Validator<R, R>,
    val arg1: Arg<T1>,
    val arg2: Arg<T2>,
) {
    fun build(ctor: (T1, T2) -> R): ObjectFactory<R> {
        return ObjectFactory {
            val context = it.addRoot(ctor.toString())
            val result1 =
                arg1.execute(context.addPath("arg1")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result2 =
                arg2.execute(context.addPath("arg2")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            if (result1.isSuccess() && result2.isSuccess()) {
                tryConstruct(context, validator) {
                    ctor(result1.value, result2.value)
                }
            } else {
                createFailure(result1, result2)
            }
        }
    }
}

data class Arguments3<T1, T2, T3, R>(
    val validator: Validator<R, R>,
    val arg1: Arg<T1>,
    val arg2: Arg<T2>,
    val arg3: Arg<T3>,
) {
    fun build(ctor: (T1, T2, T3) -> R): ObjectFactory<R> {
        return ObjectFactory {
            val context = it.addRoot(ctor.toString())
            val result1 =
                arg1.execute(context.addPath("arg1")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result2 =
                arg2.execute(context.addPath("arg2")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result3 =
                arg3.execute(context.addPath("arg3")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            if (result1.isSuccess() && result2.isSuccess() && result3.isSuccess()) {
                tryConstruct(context, validator) {
                    ctor(result1.value, result2.value, result3.value)
                }
            } else {
                createFailure(result1, result2, result3)
            }
        }
    }
}

data class Arguments4<T1, T2, T3, T4, R>(
    val validator: Validator<R, R>,
    val arg1: Arg<T1>,
    val arg2: Arg<T2>,
    val arg3: Arg<T3>,
    val arg4: Arg<T4>,
) {
    fun build(ctor: (T1, T2, T3, T4) -> R): ObjectFactory<R> {
        return ObjectFactory {
            val context = it.addRoot(ctor.toString())
            val result1 =
                arg1.execute(context.addPath("arg1")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result2 =
                arg2.execute(context.addPath("arg2")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result3 =
                arg3.execute(context.addPath("arg3")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result4 =
                arg4.execute(context.addPath("arg4")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            if (result1.isSuccess() && result2.isSuccess() && result3.isSuccess() && result4.isSuccess()) {
                tryConstruct(context, validator) {
                    ctor(result1.value, result2.value, result3.value, result4.value)
                }
            } else {
                createFailure(result1, result2, result3, result4)
            }
        }
    }
}

data class Arguments5<T1, T2, T3, T4, T5, R>(
    val validator: Validator<R, R>,
    val arg1: Arg<T1>,
    val arg2: Arg<T2>,
    val arg3: Arg<T3>,
    val arg4: Arg<T4>,
    val arg5: Arg<T5>,
) {
    fun build(ctor: (T1, T2, T3, T4, T5) -> R): ObjectFactory<R> {
        return ObjectFactory {
            val context = it.addRoot(ctor.toString())
            val result1 =
                arg1.execute(context.addPath("arg1")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result2 =
                arg2.execute(context.addPath("arg2")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result3 =
                arg3.execute(context.addPath("arg3")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result4 =
                arg4.execute(context.addPath("arg4")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result5 =
                arg5.execute(context.addPath("arg5")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            if (result1.isSuccess() && result2.isSuccess() && result3.isSuccess() && result4.isSuccess() && result5.isSuccess()) {
                tryConstruct(context, validator) {
                    ctor(result1.value, result2.value, result3.value, result4.value, result5.value)
                }
            } else {
                createFailure(result1, result2, result3, result4, result5)
            }
        }
    }
}

data class Arguments6<T1, T2, T3, T4, T5, T6, R>(
    val validator: Validator<R, R>,
    val arg1: Arg<T1>,
    val arg2: Arg<T2>,
    val arg3: Arg<T3>,
    val arg4: Arg<T4>,
    val arg5: Arg<T5>,
    val arg6: Arg<T6>,
) {
    fun build(ctor: (T1, T2, T3, T4, T5, T6) -> R): ObjectFactory<R> {
        return ObjectFactory {
            val context = it.addRoot(ctor.toString())
            val result1 =
                arg1.execute(context.addPath("arg1")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result2 =
                arg2.execute(context.addPath("arg2")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result3 =
                arg3.execute(context.addPath("arg3")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result4 =
                arg4.execute(context.addPath("arg4")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result5 =
                arg5.execute(context.addPath("arg5")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result6 =
                arg6.execute(context.addPath("arg6")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            if (result1.isSuccess() &&
                result2.isSuccess() &&
                result3.isSuccess() &&
                result4.isSuccess() &&
                result5.isSuccess() &&
                result6.isSuccess()
            ) {
                tryConstruct(context, validator) {
                    ctor(result1.value, result2.value, result3.value, result4.value, result5.value, result6.value)
                }
            } else {
                createFailure(result1, result2, result3, result4, result5, result6)
            }
        }
    }
}

data class Arguments7<T1, T2, T3, T4, T5, T6, T7, R>(
    val validator: Validator<R, R>,
    val arg1: Arg<T1>,
    val arg2: Arg<T2>,
    val arg3: Arg<T3>,
    val arg4: Arg<T4>,
    val arg5: Arg<T5>,
    val arg6: Arg<T6>,
    val arg7: Arg<T7>,
) {
    fun build(ctor: (T1, T2, T3, T4, T5, T6, T7) -> R): ObjectFactory<R> {
        return ObjectFactory {
            val context = it.addRoot(ctor.toString())
            val result1 =
                arg1.execute(context.addPath("arg1")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result2 =
                arg2.execute(context.addPath("arg2")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result3 =
                arg3.execute(context.addPath("arg3")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result4 =
                arg4.execute(context.addPath("arg4")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result5 =
                arg5.execute(context.addPath("arg5")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result6 =
                arg6.execute(context.addPath("arg6")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result7 =
                arg7.execute(context.addPath("arg7")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            if (result1.isSuccess() &&
                result2.isSuccess() &&
                result3.isSuccess() &&
                result4.isSuccess() &&
                result5.isSuccess() &&
                result6.isSuccess() &&
                result7.isSuccess()
            ) {
                tryConstruct(context, validator) {
                    ctor(result1.value, result2.value, result3.value, result4.value, result5.value, result6.value, result7.value)
                }
            } else {
                createFailure(result1, result2, result3, result4, result5, result6, result7)
            }
        }
    }
}

data class Arguments8<T1, T2, T3, T4, T5, T6, T7, T8, R>(
    val validator: Validator<R, R>,
    val arg1: Arg<T1>,
    val arg2: Arg<T2>,
    val arg3: Arg<T3>,
    val arg4: Arg<T4>,
    val arg5: Arg<T5>,
    val arg6: Arg<T6>,
    val arg7: Arg<T7>,
    val arg8: Arg<T8>,
) {
    fun build(ctor: (T1, T2, T3, T4, T5, T6, T7, T8) -> R): ObjectFactory<R> {
        return ObjectFactory {
            val context = it.addRoot(ctor.toString())
            val result1 =
                arg1.execute(context.addPath("arg1")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result2 =
                arg2.execute(context.addPath("arg2")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result3 =
                arg3.execute(context.addPath("arg3")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result4 =
                arg4.execute(context.addPath("arg4")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result5 =
                arg5.execute(context.addPath("arg5")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result6 =
                arg6.execute(context.addPath("arg6")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result7 =
                arg7.execute(context.addPath("arg7")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result8 =
                arg8.execute(context.addPath("arg8")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            if (result1.isSuccess() &&
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
                createFailure(result1, result2, result3, result4, result5, result6, result7, result8)
            }
        }
    }
}

data class Arguments9<T1, T2, T3, T4, T5, T6, T7, T8, T9, R>(
    val validator: Validator<R, R>,
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
    fun build(ctor: (T1, T2, T3, T4, T5, T6, T7, T8, T9) -> R): ObjectFactory<R> {
        return ObjectFactory {
            val context = it.addRoot(ctor.toString())
            val result1 =
                arg1.execute(context.addPath("arg1")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result2 =
                arg2.execute(context.addPath("arg2")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result3 =
                arg3.execute(context.addPath("arg3")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result4 =
                arg4.execute(context.addPath("arg4")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result5 =
                arg5.execute(context.addPath("arg5")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result6 =
                arg6.execute(context.addPath("arg6")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result7 =
                arg7.execute(context.addPath("arg7")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result8 =
                arg8.execute(context.addPath("arg8")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result9 =
                arg9.execute(context.addPath("arg9")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            if (result1.isSuccess() &&
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
                createFailure(result1, result2, result3, result4, result5, result6, result7, result8, result9)
            }
        }
    }
}

data class Arguments10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R>(
    val validator: Validator<R, R>,
    val arg1: Arg<T1>,
    val arg2: Arg<T2>,
    val arg3: Arg<T3>,
    val arg4: Arg<T4>,
    val arg5: Arg<T5>,
    val arg6: Arg<T6>,
    val arg7: Arg<T7>,
    val arg8: Arg<T8>,
    val arg9: Arg<T9>,
    val arg10: Arg<T10>,
) {
    fun build(ctor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) -> R): ObjectFactory<R> {
        return ObjectFactory {
            val context = it.addRoot(ctor.toString())
            val result1 =
                arg1.execute(context.addPath("arg1")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result2 =
                arg2.execute(context.addPath("arg2")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result3 =
                arg3.execute(context.addPath("arg3")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result4 =
                arg4.execute(context.addPath("arg4")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result5 =
                arg5.execute(context.addPath("arg5")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result6 =
                arg6.execute(context.addPath("arg6")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result7 =
                arg7.execute(context.addPath("arg7")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result8 =
                arg8.execute(context.addPath("arg8")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result9 =
                arg9.execute(context.addPath("arg9")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            val result10 =
                arg10.execute(context.addPath("arg10")).let {
                    if (context.shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
                }
            if (result1.isSuccess() &&
                result2.isSuccess() &&
                result3.isSuccess() &&
                result4.isSuccess() &&
                result5.isSuccess() &&
                result6.isSuccess() &&
                result7.isSuccess() &&
                result8.isSuccess() &&
                result9.isSuccess() &&
                result10.isSuccess()
            ) {
                tryConstruct(context, validator) {
                    ctor(
                        result1.value,
                        result2.value,
                        result3.value,
                        result4.value,
                        result5.value,
                        result6.value,
                        result7.value,
                        result8.value,
                        result9.value,
                        result10.value,
                    )
                }
            } else {
                createFailure(result1, result2, result3, result4, result5, result6, result7, result8, result9, result10)
            }
        }
    }
}
