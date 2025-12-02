package org.komapper.extension.validator

import org.komapper.extension.validator.ValidationResult.Failure
import org.komapper.extension.validator.ValidationResult.Success

fun interface Validator<IN, OUT> {
    fun execute(
        context: ValidationContext,
        input: IN,
    ): ValidationResult<OUT>
}

fun <IN, OUT> Validator<IN, OUT>.tryValidate(
    input: IN,
    failFast: Boolean = false,
): ValidationResult<OUT> = execute(ValidationContext(failFast = failFast), input)

fun <IN, OUT> Validator<IN, OUT>.validate(
    input: IN,
    failFast: Boolean = false,
): OUT =
    when (val result = execute(ValidationContext(failFast = failFast), input)) {
        is Success<OUT> -> result.value
        is Failure -> throw ValidationException(result.details)
    }

operator fun <IN, OUT> Validator<IN, OUT>.plus(other: Validator<IN, OUT>): Validator<IN, OUT> = this and other

infix fun <IN, OUT> Validator<IN, OUT>.and(other: Validator<IN, OUT>): Validator<IN, OUT> {
    val self = this
    return Validator { context, input ->
        val context = context.addLog("Validator.and")
        when (val selfResult = self.execute(context, input)) {
            is Success -> {
                val otherResult = other.execute(context, input)
                selfResult + otherResult
            }

            is Failure -> {
                if (context.failFast) {
                    selfResult
                } else {
                    val otherResult = other.execute(context, input)
                    selfResult + otherResult
                }
            }
        }
    }
}

infix fun <IN, OUT> Validator<IN, OUT>.or(other: Validator<IN, OUT>): Validator<IN, OUT> {
    val self = this
    return Validator { context, input ->
        val context = context.addLog("Validator.or")
        when (val selfResult = self.execute(context, input)) {
            is Success -> selfResult
            is Failure -> {
                when (val otherResult = other.execute(context, input)) {
                    is Success -> otherResult
                    is Failure -> selfResult + otherResult
                }
            }
        }
    }
}

fun <IN, OUT, NEW> Validator<IN, OUT>.map(transform: (OUT) -> NEW): Validator<IN, NEW> {
    val self = this
    return Validator { context, input ->
        val context = context.addLog("Validator.map")
        when (val result = self.execute(context, input)) {
            is Success -> {
                try {
                    Success(transform(result.value), result.context)
                } catch (cause: Exception) {
                    val message =
                        if (cause is MessageException) {
                            cause.validationMessage
                        } else {
                            Message.Text(cause.message.toString())
                        }
                    val detail = ValidationResult.FailureDetail(result.context, message, cause)
                    Failure(detail)
                }
            }

            is Failure -> result
        }
    }
}

fun <IN, OUT> Validator<IN, OUT>.name(name: String): Validator<IN, OUT> {
    val self = this
    return Validator { context, input ->
        val context = context.addPath(name).addLog("Validator.name(name=$name)")
        when (val result = self.execute(context, input)) {
            is Success -> Success(result.value, result.context)
            is Failure -> result
        }
    }
}

fun <IN, OUT, NEW> Validator<OUT, NEW>.compose(before: Validator<IN, OUT>): Validator<IN, NEW> = before.then(this)

fun <IN, OUT, NEW> Validator<IN, OUT>.then(after: Validator<OUT, NEW>): Validator<IN, NEW> {
    val before = this
    return Validator { context, input ->
        val context = context.addLog("Validator.then")
        when (val result = before.execute(context, input)) {
            is Success -> after.execute(result.context, result.value)
            is Failure -> result
        }
    }
}

fun <T> Validator<T, T>.chain(next: Validator<T, T>): Validator<T, T> =
    Validator { context, input ->
        val context = context.addLog("Validator.chain")
        when (val result = this.execute(context, input)) {
            is Success -> {
                next.execute(result.context, result.value)
            }

            is Failure -> {
                if (context.failFast) {
                    result
                } else {
                    result + next.execute(context, input)
                }
            }
        }
    }
