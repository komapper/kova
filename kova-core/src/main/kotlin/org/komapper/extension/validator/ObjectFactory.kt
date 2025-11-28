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

data class Argument1<A1, B1>(
    val arg1: Validator<A1, B1>,
) {
    fun <T : Any> by(ctor: (B1) -> T) = ObjectFactory1(ctor, this)
}

data class Argument2<A1, B1, A2, B2>(
    val arg1: Validator<A1, B1>,
    val arg2: Validator<A2, B2>,
) {
    fun <T : Any> bindTo(ctor: (B1, B2) -> T) = ObjectFactory2(ctor, this)
}

class ObjectFactory1<T : Any, A1, B1>(
    private val constructor: (B1) -> T,
    private val args: Argument1<A1, B1>,
) {
    fun tryCreate(
        arg1: A1,
        failFast: Boolean = false,
    ): ValidationResult<T> {
        val context = ValidationContext(constructor.toString(), failFast = failFast)
        val result1 =
            args.arg1.execute(context.addPath("arg1"), arg1).let {
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

class ObjectFactory2<T : Any, A1, B1, A2, B2>(
    private val constructor: (B1, B2) -> T,
    private val args: Argument2<A1, B1, A2, B2>,
) {
    fun tryCreate(
        arg1: A1,
        arg2: A2,
        failFast: Boolean = false,
    ): ValidationResult<T> {
        val context = ValidationContext(constructor.toString(), failFast = failFast)
        val result1 =
            args.arg1.execute(context.addPath("arg1"), arg1).let {
                if (context.shouldReturnEarly(it)) return createFailure(it) else it
            }
        val result2 =
            args.arg2.execute(context.addPath("arg2"), arg2).let {
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
