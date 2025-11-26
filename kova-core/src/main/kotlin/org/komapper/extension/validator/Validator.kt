package org.komapper.extension.validator

import org.komapper.extension.validator.ValidationResult.Failure
import org.komapper.extension.validator.ValidationResult.Success

interface Validator<IN, OUT> {
    fun tryValidate(
        input: IN,
        context: ValidationContext = ValidationContext(),
    ): ValidationResult<OUT>
}

fun <IN, OUT> Validator<IN, OUT>.validate(input: IN): OUT =
    when (val result = tryValidate(input)) {
        is Success<OUT> -> result.value
        is Failure -> throw ValidationException(result.details)
    }

operator fun <IN, OUT> Validator<IN, OUT>.plus(other: Validator<IN, OUT>): Validator<IN, OUT> = this and other

infix fun <IN, OUT> Validator<IN, OUT>.and(other: Validator<IN, OUT>): Validator<IN, OUT> {
    val self = this
    return object : Validator<IN, OUT> {
        override fun tryValidate(
            input: IN,
            context: ValidationContext,
        ): ValidationResult<OUT> {
            val thisResult = self.tryValidate(input, context)
            return if (context.failFast && thisResult.isFailure()) {
                thisResult
            } else {
                val otherResult = other.tryValidate(input, context)
                thisResult + otherResult
            }
        }
    }
}

infix fun <IN, OUT> Validator<IN, OUT>.or(other: Validator<IN, OUT>): Validator<IN, OUT> {
    val self = this
    return object : Validator<IN, OUT> {
        override fun tryValidate(
            input: IN,
            context: ValidationContext,
        ): ValidationResult<OUT> =
            when (val selfResult = self.tryValidate(input, context)) {
                is Success -> selfResult
                is Failure -> {
                    when (val otherResult = other.tryValidate(input, context)) {
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
    return object : Validator<IN, NEW> {
        override fun tryValidate(
            input: IN,
            context: ValidationContext,
        ): ValidationResult<NEW> {
            val newContext = context.addPath(name)
            return when (val result = self.tryValidate(input, newContext)) {
                // TODO error handling
                is Success -> Success(transform(result.value), result.context)
                is Failure -> result
            }
        }
    }
}

fun <IN, OUT, NEW> Validator<OUT, NEW>.compose(before: Validator<IN, OUT>): Validator<IN, NEW> = before.andThen(this)

fun <IN, OUT, NEW> Validator<IN, OUT>.andThen(after: Validator<OUT, NEW>): Validator<IN, NEW> {
    val before = this
    return object : Validator<IN, NEW> {
        override fun tryValidate(
            input: IN,
            context: ValidationContext,
        ): ValidationResult<NEW> =
            when (val result = before.tryValidate(input, context)) {
                is Success -> after.tryValidate(result.value, result.context)
                is Failure -> result
            }
    }
}

fun <IN, OUT> Validator<IN, OUT>.constraint(constraint: Constraint<OUT>): Validator<IN, OUT> {
    val self = this
    return object : Validator<IN, OUT> {
        override fun tryValidate(
            input: IN,
            context: ValidationContext,
        ): ValidationResult<OUT> =
            when (val result = self.tryValidate(input, context)) {
                is Success -> {
                    val validator = CoreValidator(listOf(constraint))
                    validator.tryValidate(result.value, context)
                }

                is Failure -> result
            }
    }
}
