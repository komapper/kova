package org.komapper.extension.validator

private fun <T> ValidationContext.shouldReturnEarly(validationResult: ValidationResult<T>): Boolean =
    failFast && validationResult.isFailure()

private fun <T : Any> tryConstruct(
    context: ValidationContext,
    block: () -> T,
): ValidationResult<T> =
    try {
        ValidationResult.Success(block(), context)
    } catch (cause: Exception) {
        ValidationResult.Failure(
            detail =
                ValidationResult.FailureDetail(
                    context = context,
                    message = Message.Text(content = cause.toString()),
                    cause = cause,
                ),
        )
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

private fun <T : Any> unwrapValidationResult(result: ValidationResult<T>): T =
    when (result) {
        is ValidationResult.Success -> result.value
        is ValidationResult.Failure -> throw ValidationException(result.details)
    }

class ObjectConstructor1<T : Any, B1>(
    private val constructor: (B1) -> T,
) {
    fun <A1> args(v1: Validator<A1, B1>): ObjectFactory1<T, A1, B1> = ObjectFactory1(constructor, v1)
}

class ObjectFactory1<T : Any, A1, B1>(
    private val constructor: (B1) -> T,
    private val v1: Validator<A1, B1>,
) {
    fun tryCreate(
        arg1: A1,
        failFast: Boolean = false,
    ): ValidationResult<T> {
        val context = ValidationContext(constructor.toString(), failFast = failFast)
        val result1 =
            v1.execute(context.addPath("arg1"), arg1).let {
                if (context.shouldReturnEarly(it)) return createFailure(it) else it
            }
        return if (result1.isSuccess()) {
            tryConstruct(context) { constructor(result1.value) }
        } else {
            ValidationResult.Failure(result1.details)
        }
    }

    fun create(arg1: A1): T = unwrapValidationResult(tryCreate(arg1))
}

class ObjectConstructor2<T : Any, B1, B2>(
    private val constructor: (B1, B2) -> T,
) {
    fun <A1, A2> args(
        v1: Validator<A1, B1>,
        v2: Validator<A2, B2>,
    ): ObjectFactory2<T, A1, B1, A2, B2> = ObjectFactory2(constructor, v1, v2)
}

class ObjectFactory2<T : Any, A1, B1, A2, B2>(
    private val constructor: (B1, B2) -> T,
    private val v1: Validator<A1, B1>,
    private val v2: Validator<A2, B2>,
) {
    fun tryCreate(
        arg1: A1,
        arg2: A2,
        failFast: Boolean = false,
    ): ValidationResult<T> {
        val context = ValidationContext(constructor.toString(), failFast = failFast)
        val result1 =
            v1.execute(context.addPath("arg1"), arg1).let {
                if (context.shouldReturnEarly(it)) return createFailure(it) else it
            }
        val result2 =
            v2.execute(context.addPath("arg2"), arg2).let {
                if (context.shouldReturnEarly(it)) return createFailure(it) else it
            }
        return if (result1.isSuccess() && result2.isSuccess()) {
            tryConstruct(context) { constructor(result1.value, result2.value) }
        } else {
            createFailure(result1, result2)
        }
    }

    fun create(
        arg1: A1,
        arg2: A2,
    ): T = unwrapValidationResult(tryCreate(arg1, arg2))
}
