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
        val thisResult = self.execute(context, input)
        if (context.failFast && thisResult.isFailure()) {
            thisResult
        } else {
            val otherResult = other.execute(context, input)
            thisResult + otherResult
        }
    }
}

infix fun <IN, OUT> Validator<IN, OUT>.or(other: Validator<IN, OUT>): Validator<IN, OUT> {
    val self = this
    return Validator { context, input ->
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

fun <IN, OUT, NEW> Validator<IN, OUT>.map(transform: (OUT) -> NEW): Validator<IN, NEW> = map("", transform)

fun <IN, OUT, NEW> Validator<IN, OUT>.map(
    name: String,
    transform: (OUT) -> NEW,
): Validator<IN, NEW> {
    val self = this
    return Validator { context, input ->
        val context = context.addPath(name)
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

fun <IN, OUT, NEW> Validator<OUT, NEW>.compose(before: Validator<IN, OUT>): Validator<IN, NEW> = before.andThen(this)

fun <IN, OUT, NEW> Validator<IN, OUT>.andThen(after: Validator<OUT, NEW>): Validator<IN, NEW> {
    val before = this
    return Validator { context, input ->
        when (val result = before.execute(context, input)) {
            is Success -> after.execute(result.context, result.value)
            is Failure -> result
        }
    }
}

fun <IN, OUT> Validator<IN, OUT>.constraint(
    key: String,
    check: ConstraintScope.(ConstraintContext<OUT>) -> ConstraintResult,
): Validator<IN, OUT> =
    constraint(
        Constraint(key, check),
    )

fun <IN, OUT> Validator<IN, OUT>.constraint(constraint: Constraint<OUT>): Validator<IN, OUT> {
    val self = this
    return Validator { context, input ->
        when (val result = self.execute(context, input)) {
            is Success -> {
                val validator = CoreValidator(listOf(constraint))
                validator.execute(context, result.value)
            }

            is Failure -> result
        }
    }
}

fun <T> chain(
    before: Validator<T, T>,
    context: ValidationContext,
    input: T,
    transform: (T) -> T = { it },
    next: (ValidationContext, T) -> ValidationResult<T>,
): ValidationResult<T> =
    when (val result = before.execute(context, input)) {
        is Success -> {
            next(result.context, transform(result.value))
        }

        is Failure -> {
            if (context.failFast) {
                result
            } else {
                result + next(context, transform(input))
            }
        }
    }
