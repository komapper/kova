package org.komapper.extension.validator

import org.komapper.extension.validator.ValidationResult.Failure
import org.komapper.extension.validator.ValidationResult.Success

fun interface Validator<IN, OUT> {
    fun execute(
        input: IN,
        context: ValidationContext,
    ): ValidationResult<OUT>
}

fun <IN, OUT> Validator<IN, OUT>.tryValidate(
    input: IN,
    config: ValidationConfig = ValidationConfig(),
): ValidationResult<OUT> = execute(input, ValidationContext(config = config))

fun <IN, OUT> Validator<IN, OUT>.validate(
    input: IN,
    config: ValidationConfig = ValidationConfig(),
): OUT =
    when (val result = execute(input, ValidationContext(config = config))) {
        is Success<OUT> -> result.value
        is Failure -> throw ValidationException(result.details)
    }

operator fun <IN, OUT> Validator<IN, OUT>.plus(other: Validator<IN, OUT>): Validator<IN, OUT> = this and other

infix fun <IN, OUT> Validator<IN, OUT>.and(other: Validator<IN, OUT>): Validator<IN, OUT> {
    val self = this
    return Validator { input, context ->
        val context = context.addLog("Validator.and")
        when (val selfResult = self.execute(input, context)) {
            is Success -> {
                val otherResult = other.execute(input, context)
                selfResult + otherResult
            }

            is Failure -> {
                if (context.failFast) {
                    selfResult
                } else {
                    val otherResult = other.execute(input, context)
                    selfResult + otherResult
                }
            }
        }
    }
}

infix fun <IN, OUT> Validator<IN, OUT>.or(other: Validator<IN, OUT>): Validator<IN, OUT> {
    val self = this
    return Validator { input, context ->
        val context = context.addLog("Validator.or")
        when (val selfResult = self.execute(input, context)) {
            is Success -> selfResult
            is Failure -> {
                when (val otherResult = other.execute(input, context)) {
                    is Success -> otherResult
                    is Failure -> {
                        val composite = FailureDetail.Or(context, selfResult.details, otherResult.details)
                        Failure(composite)
                    }
                }
            }
        }
    }
}

fun <IN, OUT, NEW> Validator<IN, OUT>.map(transform: (OUT) -> NEW): Validator<IN, NEW> {
    val self = this
    return Validator { input, context ->
        val context = context.addLog("Validator.map")
        when (val result = self.execute(input, context)) {
            is Success -> tryRun(result.context) { transform(result.value) }
            is Failure -> result
        }
    }
}

fun <IN, OUT> Validator<IN, OUT>.name(name: String): Validator<IN, OUT> {
    val self = this
    return Validator { input, context ->
        val context = context.addPath(name, input).addLog("Validator.name(name=$name)")
        when (val result = self.execute(input, context)) {
            is Success -> Success(result.value, result.context)
            is Failure -> result
        }
    }
}

fun <IN, OUT, NEW> Validator<OUT, NEW>.compose(before: Validator<IN, OUT>): Validator<IN, NEW> = before.then(this)

fun <IN, OUT, NEW> Validator<IN, OUT>.then(after: Validator<OUT, NEW>): Validator<IN, NEW> {
    val before = this
    return Validator { input, context ->
        val context = context.addLog("Validator.then")
        when (val result = before.execute(input, context)) {
            is Success -> after.execute(result.value, result.context)
            is Failure -> result
        }
    }
}

fun <T> Validator<T, T>.chain(next: Validator<T, T>): Validator<T, T> =
    Validator { input, context ->
        val context = context.addLog("Validator.chain")
        when (val result = this.execute(input, context)) {
            is Success -> {
                next.execute(result.value, result.context)
            }

            is Failure -> {
                if (context.failFast) {
                    result
                } else {
                    result + next.execute(input, context)
                }
            }
        }
    }

internal fun <R> tryRun(
    context: ValidationContext,
    block: () -> R,
): ValidationResult<R> {
    return try {
        return Success(block(), context)
    } catch (cause: Exception) {
        val message =
            if (cause is MessageException) {
                cause.validationMessage
            } else {
                throw cause
            }
        Failure(FailureDetail.Single(context, message, cause))
    }
}
