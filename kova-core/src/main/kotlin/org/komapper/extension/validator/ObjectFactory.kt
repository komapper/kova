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
            return ValidationResult.Failure(
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
        is ValidationResult.Failure -> ValidationResult.Failure(result.details)
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
        val validator: Validator<IN, OUT>,
        val value: IN,
    ) : Arg<OUT> {
        override fun execute(context: ValidationContext): ValidationResult<OUT> = validator.execute(context, value)
    }

    data class Factory<IN, OUT>(
        val validator: Validator<IN, OUT>,
        val factory: ObjectFactory<IN>,
    ) : Arg<OUT> {
        override fun execute(context: ValidationContext): ValidationResult<OUT> =
            when (val result = factory.execute(context)) {
                is ValidationResult.Success -> validator.execute(result.context, result.value)
                is ValidationResult.Failure -> result
            }
    }
}

data class Arguments1<T>(
    val arg1: Arg<T>,
) {
    fun <R> createFactory(
        validator: Validator<R, R>,
        ctor: (T) -> R,
    ): ObjectFactory<R> =
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

data class Arguments2<T1, T2>(
    val arg1: Arg<T1>,
    val arg2: Arg<T2>,
) {
    fun <R> createFactory(
        validator: Validator<R, R>,
        ctor: (T1, T2) -> R,
    ): ObjectFactory<R> {
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
