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

data class Arguments1<A1, B1>(
    val arg1: Validator<A1, B1>,
) {
    fun <T : Any> createFactory(ctor: (B1) -> T) = ObjectFactory1(ctor, this)
}

data class Arguments2<A1, B1, A2, B2>(
    val arg1: Validator<A1, B1>,
    val arg2: Validator<A2, B2>,
) {
    fun <T : Any> createFactory(ctor: (B1, B2) -> T) = ObjectFactory2(ctor, this)
}

data class Arguments3<A1, B1, A2, B2, A3, B3>(
    val arg1: Validator<A1, B1>,
    val arg2: Validator<A2, B2>,
    val arg3: Validator<A3, B3>,
) {
    fun <T : Any> createFactory(ctor: (B1, B2, B3) -> T) = ObjectFactory3(ctor, this)
}

data class Arguments4<A1, B1, A2, B2, A3, B3, A4, B4>(
    val arg1: Validator<A1, B1>,
    val arg2: Validator<A2, B2>,
    val arg3: Validator<A3, B3>,
    val arg4: Validator<A4, B4>,
) {
    fun <T : Any> createFactory(ctor: (B1, B2, B3, B4) -> T) = ObjectFactory4(ctor, this)
}

data class Arguments5<A1, B1, A2, B2, A3, B3, A4, B4, A5, B5>(
    val arg1: Validator<A1, B1>,
    val arg2: Validator<A2, B2>,
    val arg3: Validator<A3, B3>,
    val arg4: Validator<A4, B4>,
    val arg5: Validator<A5, B5>,
) {
    fun <T : Any> createFactory(ctor: (B1, B2, B3, B4, B5) -> T) = ObjectFactory5(ctor, this)
}

data class Arguments6<A1, B1, A2, B2, A3, B3, A4, B4, A5, B5, A6, B6>(
    val arg1: Validator<A1, B1>,
    val arg2: Validator<A2, B2>,
    val arg3: Validator<A3, B3>,
    val arg4: Validator<A4, B4>,
    val arg5: Validator<A5, B5>,
    val arg6: Validator<A6, B6>,
) {
    fun <T : Any> createFactory(ctor: (B1, B2, B3, B4, B5, B6) -> T) = ObjectFactory6(ctor, this)
}

data class Arguments7<A1, B1, A2, B2, A3, B3, A4, B4, A5, B5, A6, B6, A7, B7>(
    val arg1: Validator<A1, B1>,
    val arg2: Validator<A2, B2>,
    val arg3: Validator<A3, B3>,
    val arg4: Validator<A4, B4>,
    val arg5: Validator<A5, B5>,
    val arg6: Validator<A6, B6>,
    val arg7: Validator<A7, B7>,
) {
    fun <T : Any> createFactory(ctor: (B1, B2, B3, B4, B5, B6, B7) -> T) = ObjectFactory7(ctor, this)
}

data class Arguments8<A1, B1, A2, B2, A3, B3, A4, B4, A5, B5, A6, B6, A7, B7, A8, B8>(
    val arg1: Validator<A1, B1>,
    val arg2: Validator<A2, B2>,
    val arg3: Validator<A3, B3>,
    val arg4: Validator<A4, B4>,
    val arg5: Validator<A5, B5>,
    val arg6: Validator<A6, B6>,
    val arg7: Validator<A7, B7>,
    val arg8: Validator<A8, B8>,
) {
    fun <T : Any> createFactory(ctor: (B1, B2, B3, B4, B5, B6, B7, B8) -> T) = ObjectFactory8(ctor, this)
}

data class Arguments9<A1, B1, A2, B2, A3, B3, A4, B4, A5, B5, A6, B6, A7, B7, A8, B8, A9, B9>(
    val arg1: Validator<A1, B1>,
    val arg2: Validator<A2, B2>,
    val arg3: Validator<A3, B3>,
    val arg4: Validator<A4, B4>,
    val arg5: Validator<A5, B5>,
    val arg6: Validator<A6, B6>,
    val arg7: Validator<A7, B7>,
    val arg8: Validator<A8, B8>,
    val arg9: Validator<A9, B9>,
) {
    fun <T : Any> createFactory(ctor: (B1, B2, B3, B4, B5, B6, B7, B8, B9) -> T) = ObjectFactory9(ctor, this)
}

data class Arguments10<A1, B1, A2, B2, A3, B3, A4, B4, A5, B5, A6, B6, A7, B7, A8, B8, A9, B9, A10, B10>(
    val arg1: Validator<A1, B1>,
    val arg2: Validator<A2, B2>,
    val arg3: Validator<A3, B3>,
    val arg4: Validator<A4, B4>,
    val arg5: Validator<A5, B5>,
    val arg6: Validator<A6, B6>,
    val arg7: Validator<A7, B7>,
    val arg8: Validator<A8, B8>,
    val arg9: Validator<A9, B9>,
    val arg10: Validator<A10, B10>,
) {
    fun <T : Any> createFactory(ctor: (B1, B2, B3, B4, B5, B6, B7, B8, B9, B10) -> T) = ObjectFactory10(ctor, this)
}

class ObjectFactory1<T : Any, A1, B1>(
    private val constructor: (B1) -> T,
    private val args: Arguments1<A1, B1>,
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
    private val args: Arguments2<A1, B1, A2, B2>,
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

class ObjectFactory3<T : Any, A1, B1, A2, B2, A3, B3>(
    private val constructor: (B1, B2, B3) -> T,
    private val args: Arguments3<A1, B1, A2, B2, A3, B3>,
) {
    fun tryCreate(
        arg1: A1,
        arg2: A2,
        arg3: A3,
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
        val result3 =
            args.arg3.execute(context.addPath("arg3"), arg3).let {
                if (context.shouldReturnEarly(it)) return createFailure(it) else it
            }
        return if (result1.isSuccess() && result2.isSuccess() && result3.isSuccess()) {
            tryConstruct(context) { constructor(result1.value, result2.value, result3.value) }
        } else {
            createFailure(result1, result2, result3)
        }
    }

    fun create(
        arg1: A1,
        arg2: A2,
        arg3: A3,
    ): T = unwrapValidationResult(tryCreate(arg1, arg2, arg3))
}

class ObjectFactory4<T : Any, A1, B1, A2, B2, A3, B3, A4, B4>(
    private val constructor: (B1, B2, B3, B4) -> T,
    private val args: Arguments4<A1, B1, A2, B2, A3, B3, A4, B4>,
) {
    fun tryCreate(
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4,
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
        val result3 =
            args.arg3.execute(context.addPath("arg3"), arg3).let {
                if (context.shouldReturnEarly(it)) return createFailure(it) else it
            }
        val result4 =
            args.arg4.execute(context.addPath("arg4"), arg4).let {
                if (context.shouldReturnEarly(it)) return createFailure(it) else it
            }
        return if (result1.isSuccess() && result2.isSuccess() && result3.isSuccess() && result4.isSuccess()) {
            tryConstruct(context) { constructor(result1.value, result2.value, result3.value, result4.value) }
        } else {
            createFailure(result1, result2, result3, result4)
        }
    }

    fun create(
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4,
    ): T = unwrapValidationResult(tryCreate(arg1, arg2, arg3, arg4))
}

class ObjectFactory5<T : Any, A1, B1, A2, B2, A3, B3, A4, B4, A5, B5>(
    private val constructor: (B1, B2, B3, B4, B5) -> T,
    private val args: Arguments5<A1, B1, A2, B2, A3, B3, A4, B4, A5, B5>,
) {
    fun tryCreate(
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4,
        arg5: A5,
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
        val result3 =
            args.arg3.execute(context.addPath("arg3"), arg3).let {
                if (context.shouldReturnEarly(it)) return createFailure(it) else it
            }
        val result4 =
            args.arg4.execute(context.addPath("arg4"), arg4).let {
                if (context.shouldReturnEarly(it)) return createFailure(it) else it
            }
        val result5 =
            args.arg5.execute(context.addPath("arg5"), arg5).let {
                if (context.shouldReturnEarly(it)) return createFailure(it) else it
            }
        return if (result1.isSuccess() && result2.isSuccess() && result3.isSuccess() && result4.isSuccess() && result5.isSuccess()) {
            tryConstruct(context) { constructor(result1.value, result2.value, result3.value, result4.value, result5.value) }
        } else {
            createFailure(result1, result2, result3, result4, result5)
        }
    }

    fun create(
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4,
        arg5: A5,
    ): T = unwrapValidationResult(tryCreate(arg1, arg2, arg3, arg4, arg5))
}

class ObjectFactory6<T : Any, A1, B1, A2, B2, A3, B3, A4, B4, A5, B5, A6, B6>(
    private val constructor: (B1, B2, B3, B4, B5, B6) -> T,
    private val args: Arguments6<A1, B1, A2, B2, A3, B3, A4, B4, A5, B5, A6, B6>,
) {
    fun tryCreate(
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4,
        arg5: A5,
        arg6: A6,
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
        val result3 =
            args.arg3.execute(context.addPath("arg3"), arg3).let {
                if (context.shouldReturnEarly(it)) return createFailure(it) else it
            }
        val result4 =
            args.arg4.execute(context.addPath("arg4"), arg4).let {
                if (context.shouldReturnEarly(it)) return createFailure(it) else it
            }
        val result5 =
            args.arg5.execute(context.addPath("arg5"), arg5).let {
                if (context.shouldReturnEarly(it)) return createFailure(it) else it
            }
        val result6 =
            args.arg6.execute(context.addPath("arg6"), arg6).let {
                if (context.shouldReturnEarly(it)) return createFailure(it) else it
            }
        return if (result1.isSuccess() && result2.isSuccess() && result3.isSuccess() && result4.isSuccess() && result5.isSuccess() &&
            result6.isSuccess()
        ) {
            tryConstruct(context) { constructor(result1.value, result2.value, result3.value, result4.value, result5.value, result6.value) }
        } else {
            createFailure(result1, result2, result3, result4, result5, result6)
        }
    }

    fun create(
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4,
        arg5: A5,
        arg6: A6,
    ): T = unwrapValidationResult(tryCreate(arg1, arg2, arg3, arg4, arg5, arg6))
}

class ObjectFactory7<T : Any, A1, B1, A2, B2, A3, B3, A4, B4, A5, B5, A6, B6, A7, B7>(
    private val constructor: (B1, B2, B3, B4, B5, B6, B7) -> T,
    private val args: Arguments7<A1, B1, A2, B2, A3, B3, A4, B4, A5, B5, A6, B6, A7, B7>,
) {
    fun tryCreate(
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4,
        arg5: A5,
        arg6: A6,
        arg7: A7,
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
        val result3 =
            args.arg3.execute(context.addPath("arg3"), arg3).let {
                if (context.shouldReturnEarly(it)) return createFailure(it) else it
            }
        val result4 =
            args.arg4.execute(context.addPath("arg4"), arg4).let {
                if (context.shouldReturnEarly(it)) return createFailure(it) else it
            }
        val result5 =
            args.arg5.execute(context.addPath("arg5"), arg5).let {
                if (context.shouldReturnEarly(it)) return createFailure(it) else it
            }
        val result6 =
            args.arg6.execute(context.addPath("arg6"), arg6).let {
                if (context.shouldReturnEarly(it)) return createFailure(it) else it
            }
        val result7 =
            args.arg7.execute(context.addPath("arg7"), arg7).let {
                if (context.shouldReturnEarly(it)) return createFailure(it) else it
            }
        return if (result1.isSuccess() && result2.isSuccess() && result3.isSuccess() && result4.isSuccess() && result5.isSuccess() &&
            result6.isSuccess() &&
            result7.isSuccess()
        ) {
            tryConstruct(context) {
                constructor(result1.value, result2.value, result3.value, result4.value, result5.value, result6.value, result7.value)
            }
        } else {
            createFailure(result1, result2, result3, result4, result5, result6, result7)
        }
    }

    fun create(
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4,
        arg5: A5,
        arg6: A6,
        arg7: A7,
    ): T = unwrapValidationResult(tryCreate(arg1, arg2, arg3, arg4, arg5, arg6, arg7))
}

class ObjectFactory8<T : Any, A1, B1, A2, B2, A3, B3, A4, B4, A5, B5, A6, B6, A7, B7, A8, B8>(
    private val constructor: (B1, B2, B3, B4, B5, B6, B7, B8) -> T,
    private val args: Arguments8<A1, B1, A2, B2, A3, B3, A4, B4, A5, B5, A6, B6, A7, B7, A8, B8>,
) {
    fun tryCreate(
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4,
        arg5: A5,
        arg6: A6,
        arg7: A7,
        arg8: A8,
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
        val result3 =
            args.arg3.execute(context.addPath("arg3"), arg3).let {
                if (context.shouldReturnEarly(it)) return createFailure(it) else it
            }
        val result4 =
            args.arg4.execute(context.addPath("arg4"), arg4).let {
                if (context.shouldReturnEarly(it)) return createFailure(it) else it
            }
        val result5 =
            args.arg5.execute(context.addPath("arg5"), arg5).let {
                if (context.shouldReturnEarly(it)) return createFailure(it) else it
            }
        val result6 =
            args.arg6.execute(context.addPath("arg6"), arg6).let {
                if (context.shouldReturnEarly(it)) return createFailure(it) else it
            }
        val result7 =
            args.arg7.execute(context.addPath("arg7"), arg7).let {
                if (context.shouldReturnEarly(it)) return createFailure(it) else it
            }
        val result8 =
            args.arg8.execute(context.addPath("arg8"), arg8).let {
                if (context.shouldReturnEarly(it)) return createFailure(it) else it
            }
        return if (result1.isSuccess() && result2.isSuccess() && result3.isSuccess() && result4.isSuccess() && result5.isSuccess() &&
            result6.isSuccess() &&
            result7.isSuccess() &&
            result8.isSuccess()
        ) {
            tryConstruct(context) {
                constructor(
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

    fun create(
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4,
        arg5: A5,
        arg6: A6,
        arg7: A7,
        arg8: A8,
    ): T = unwrapValidationResult(tryCreate(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8))
}

class ObjectFactory9<T : Any, A1, B1, A2, B2, A3, B3, A4, B4, A5, B5, A6, B6, A7, B7, A8, B8, A9, B9>(
    private val constructor: (B1, B2, B3, B4, B5, B6, B7, B8, B9) -> T,
    private val args: Arguments9<A1, B1, A2, B2, A3, B3, A4, B4, A5, B5, A6, B6, A7, B7, A8, B8, A9, B9>,
) {
    fun tryCreate(
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4,
        arg5: A5,
        arg6: A6,
        arg7: A7,
        arg8: A8,
        arg9: A9,
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
        val result3 =
            args.arg3.execute(context.addPath("arg3"), arg3).let {
                if (context.shouldReturnEarly(it)) return createFailure(it) else it
            }
        val result4 =
            args.arg4.execute(context.addPath("arg4"), arg4).let {
                if (context.shouldReturnEarly(it)) return createFailure(it) else it
            }
        val result5 =
            args.arg5.execute(context.addPath("arg5"), arg5).let {
                if (context.shouldReturnEarly(it)) return createFailure(it) else it
            }
        val result6 =
            args.arg6.execute(context.addPath("arg6"), arg6).let {
                if (context.shouldReturnEarly(it)) return createFailure(it) else it
            }
        val result7 =
            args.arg7.execute(context.addPath("arg7"), arg7).let {
                if (context.shouldReturnEarly(it)) return createFailure(it) else it
            }
        val result8 =
            args.arg8.execute(context.addPath("arg8"), arg8).let {
                if (context.shouldReturnEarly(it)) return createFailure(it) else it
            }
        val result9 =
            args.arg9.execute(context.addPath("arg9"), arg9).let {
                if (context.shouldReturnEarly(it)) return createFailure(it) else it
            }
        return if (result1.isSuccess() && result2.isSuccess() && result3.isSuccess() && result4.isSuccess() && result5.isSuccess() &&
            result6.isSuccess() &&
            result7.isSuccess() &&
            result8.isSuccess() &&
            result9.isSuccess()
        ) {
            tryConstruct(context) {
                constructor(
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

    fun create(
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4,
        arg5: A5,
        arg6: A6,
        arg7: A7,
        arg8: A8,
        arg9: A9,
    ): T = unwrapValidationResult(tryCreate(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9))
}

class ObjectFactory10<T : Any, A1, B1, A2, B2, A3, B3, A4, B4, A5, B5, A6, B6, A7, B7, A8, B8, A9, B9, A10, B10>(
    private val constructor: (B1, B2, B3, B4, B5, B6, B7, B8, B9, B10) -> T,
    private val args: Arguments10<A1, B1, A2, B2, A3, B3, A4, B4, A5, B5, A6, B6, A7, B7, A8, B8, A9, B9, A10, B10>,
) {
    fun tryCreate(
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4,
        arg5: A5,
        arg6: A6,
        arg7: A7,
        arg8: A8,
        arg9: A9,
        arg10: A10,
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
        val result3 =
            args.arg3.execute(context.addPath("arg3"), arg3).let {
                if (context.shouldReturnEarly(it)) return createFailure(it) else it
            }
        val result4 =
            args.arg4.execute(context.addPath("arg4"), arg4).let {
                if (context.shouldReturnEarly(it)) return createFailure(it) else it
            }
        val result5 =
            args.arg5.execute(context.addPath("arg5"), arg5).let {
                if (context.shouldReturnEarly(it)) return createFailure(it) else it
            }
        val result6 =
            args.arg6.execute(context.addPath("arg6"), arg6).let {
                if (context.shouldReturnEarly(it)) return createFailure(it) else it
            }
        val result7 =
            args.arg7.execute(context.addPath("arg7"), arg7).let {
                if (context.shouldReturnEarly(it)) return createFailure(it) else it
            }
        val result8 =
            args.arg8.execute(context.addPath("arg8"), arg8).let {
                if (context.shouldReturnEarly(it)) return createFailure(it) else it
            }
        val result9 =
            args.arg9.execute(context.addPath("arg9"), arg9).let {
                if (context.shouldReturnEarly(it)) return createFailure(it) else it
            }
        val result10 =
            args.arg10.execute(context.addPath("arg10"), arg10).let {
                if (context.shouldReturnEarly(it)) return createFailure(it) else it
            }
        return if (result1.isSuccess() && result2.isSuccess() && result3.isSuccess() && result4.isSuccess() && result5.isSuccess() &&
            result6.isSuccess() &&
            result7.isSuccess() &&
            result8.isSuccess() &&
            result9.isSuccess() &&
            result10.isSuccess()
        ) {
            tryConstruct(context) {
                constructor(
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

    fun create(
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4,
        arg5: A5,
        arg6: A6,
        arg7: A7,
        arg8: A8,
        arg9: A9,
        arg10: A10,
    ): T = unwrapValidationResult(tryCreate(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10))
}
