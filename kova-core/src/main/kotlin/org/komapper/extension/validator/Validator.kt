package org.komapper.extension.validator

import org.komapper.extension.validator.ValidationResult.Failure
import org.komapper.extension.validator.ValidationResult.Success

/**
 * Core validator interface for type-safe validation.
 *
 * A validator transforms an input of type [IN] into an output of type [OUT],
 * or produces a validation failure with detailed error information.
 *
 * Validators are immutable and composable using operators like [plus], [and], [orNullable],
 * [map], [then], and [chain].
 *
 * @param IN The input type to validate
 * @param OUT The output type after successful validation
 */
fun interface Validator<IN, OUT> {
    /**
     * Executes the validation on the given input.
     *
     * @param input The value to validate
     * @param context The validation context tracking state and configuration
     * @return A [ValidationResult] containing either the validated value or failure details
     */
    fun execute(
        input: IN,
        context: ValidationContext,
    ): ValidationResult<OUT>

    companion object {
        fun <T> success() = IdentityValidator<T> { input, context -> Success(input, context) }
    }
}

/**
 * Validates the input and returns a [ValidationResult].
 *
 * This is the recommended way to perform validation when you want to handle
 * both success and failure cases programmatically.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().min(1).max(10)
 * when (val result = validator.tryValidate("hello")) {
 *     is ValidationResult.Success -> println("Valid: ${result.value}")
 *     is ValidationResult.Failure -> println("Errors: ${result.details}")
 * }
 * ```
 *
 * @param input The value to validate
 * @param config Configuration options for validation (failFast, logging)
 * @return A [ValidationResult] containing either the validated value or failure details
 */
fun <IN, OUT> Validator<IN, OUT>.tryValidate(
    input: IN,
    config: ValidationConfig = ValidationConfig(),
): ValidationResult<OUT> = execute(input, ValidationContext(config = config))

/**
 * Validates the input and returns the validated value, or throws an exception on failure.
 *
 * Use this when you want validation failures to throw exceptions rather than
 * handling them programmatically.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().min(1).max(10)
 * try {
 *     val validated = validator.validate("hello")
 *     println("Valid: $validated")
 * } catch (e: ValidationException) {
 *     println("Errors: ${e.details}")
 * }
 * ```
 *
 * @param input The value to validate
 * @param config Configuration options for validation (failFast, logging)
 * @return The validated value of type [OUT]
 * @throws ValidationException if validation fails
 */
fun <IN, OUT> Validator<IN, OUT>.validate(
    input: IN,
    config: ValidationConfig = ValidationConfig(),
): OUT =
    when (val result = execute(input, ValidationContext(config = config))) {
        is Success<OUT> -> result.value
        is Failure<*> -> throw ValidationException(result.messages)
    }

/**
 * Operator overload for [and]. Combines two validators that both must succeed.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().min(3) + Kova.string().max(10)
 * // Equivalent to: Kova.string().min(3).max(10)
 * ```
 */
operator fun <IN, OUT> Validator<IN, OUT>.plus(other: Validator<IN, OUT>): Validator<IN, OUT> = this and other

/**
 * Combines two validators where both must succeed for the overall validation to succeed.
 *
 * If either validator fails, the failure is included in the result. With failFast enabled,
 * execution stops at the first failure.
 *
 * Example:
 * ```kotlin
 * val nameValidator = Kova.string().min(1) and Kova.string().max(50)
 * val ageValidator = Kova.int().min(0) and Kova.int().max(120)
 * ```
 *
 * @param other The second validator to apply
 * @return A new validator that succeeds only if both validators succeed
 */
infix fun <IN, OUT> Validator<IN, OUT>.and(other: Validator<IN, OUT>): Validator<IN, OUT> {
    val self = this
    return Validator { input, context ->
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

fun <IN, OUT> Validator<IN, OUT>.and(block: (Validator<IN, IN>) -> Validator<IN, OUT>): Validator<IN, OUT> = and(block(Validator.success()))

/**
 * Combines two validators where at least one must succeed for the overall validation to succeed.
 *
 * If the first validator succeeds, the second is not executed. If both fail,
 * a composite failure containing both error branches is returned.
 *
 * Example:
 * ```kotlin
 * // Accept either a short string or a string starting with "LONG:"
 * val validator = Kova.string().max(10) or
 *     (Kova.string().min(6).startsWith("LONG:"))
 * ```
 *
 * @param other The alternative validator to try if this one fails
 * @return A new validator that succeeds if either validator succeeds
 */
infix fun <IN, OUT> Validator<IN, OUT>.or(other: Validator<IN, OUT>): Validator<IN, OUT> {
    val self = this
    return Validator { input, context ->
        when (val selfResult = self.execute(input, context)) {
            is Success -> selfResult
            is Failure -> {
                when (val otherResult = other.execute(input, context)) {
                    is Success -> otherResult
                    is Failure -> {
                        val constraintContext = context.createConstraintContext(input, "kova.or")
                        val messageContext = constraintContext.createMessageContext(listOf(selfResult.messages, otherResult.messages))
                        Failure(otherResult.value, listOf(Message.Or(messageContext, selfResult, otherResult)))
                    }
                }
            }
        }
    }
}

fun <IN, OUT> Validator<IN, OUT>.or(block: (Validator<IN, IN>) -> Validator<IN, OUT>): Validator<IN, OUT> = or(block(Validator.success()))

/**
 * Transforms the output value on successful validation.
 *
 * The transform function is only called if validation succeeds. If it throws
 * a [MessageException], the exception is converted to a validation failure.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().min(1).map { it.trim().uppercase() }
 * validator.validate("  hello  ") // Returns "HELLO"
 * ```
 *
 * @param transform Function to transform the validated value
 * @return A new validator with the transformed output type
 */
inline fun <reified IN, reified OUT, reified NEW> Validator<IN, OUT>.map(noinline transform: (OUT) -> NEW): Validator<IN, NEW> {
    val self = this
    val outClass = OUT::class
    val newClass = NEW::class
    return Validator { input, context ->
        when (val result = self.execute(input, context)) {
            is Success -> tryTransform(result.value, result.context, transform)
            is Failure -> {
                // TODO
                val failureInput =
                    when (val v = result.value) {
                        is Input.Unknown -> Input.Unknown(v.value)
                        is Input.Some -> {
                            val value = v.value
                            if (outClass == newClass && outClass.isInstance(value)) {
                                when (val r = tryTransform(value, context, transform)) {
                                    is Success -> Input.Some(r.value)
                                    is Failure -> Input.Unknown(value)
                                }
                            } else {
                                Input.Unknown(value)
                            }
                        }
                    }
                Failure(failureInput, result.messages)
            }
        }
    }
}

fun <T, R> tryTransform(
    input: T,
    context: ValidationContext,
    transform: (T) -> R,
): ValidationResult<R> {
    return try {
        return Success(transform(input), context)
    } catch (cause: Exception) {
        val content =
            if (cause is MessageException) {
                cause.message
            } else {
                throw cause
            }
        val constraintContext = context.createConstraintContext(input, "kova.transform")
        val messageContext = constraintContext.createMessageContext(emptyList())
        val message = Message.Text(messageContext, content.toString())
        Failure(Input.Unknown(input), listOf(message))
    }
}

/**
 * Adds a name to the validation path for better error reporting.
 *
 * This is useful for identifying which field failed validation in complex objects.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().min(1).name("username")
 * // Failures will show path like "User.username"
 * ```
 *
 * @param name The name to add to the validation path
 * @return A new validator that tracks the path
 */
fun <IN, OUT> Validator<IN, OUT>.name(name: String): Validator<IN, OUT> {
    val self = this
    return Validator { input, context ->
        val context = context.addPath(name, input)
        when (val result = self.execute(input, context)) {
            is Success -> Success(result.value, result.context)
            is Failure -> result
        }
    }
}

/**
 * Composes two validators by applying the [before] validator first, then this validator.
 *
 * This is the reverse of [then].
 *
 * @param before The validator to apply first
 * @return A new validator that applies both validators in sequence
 */
fun <IN, OUT, NEW> Validator<OUT, NEW>.compose(before: Validator<IN, OUT>): Validator<IN, NEW> = before.then(this)

fun <IN, OUT, NEW> Validator<OUT, NEW>.compose(block: (Validator<IN, IN>) -> Validator<IN, OUT>): Validator<IN, NEW> =
    compose(block(Validator.success()))

/**
 * Chains two validators sequentially, passing the output of the first to the second.
 *
 * **Key characteristic**: The input type (IN) and output type (OUT) can be different types.
 * This allows type transformation through the validation chain.
 *
 * If the first validator fails, the second is not executed.
 *
 * Example with type transformation:
 * ```kotlin
 * // String -> Int transformation
 * val parseAndValidate = Kova.string()
 *     .isInt()
 *     .map { it.toInt() }
 *     .then(Kova.int().min(0).max(100))
 * // Input: String, Output: Int
 * ```
 *
 * @param after The validator to apply to the output of this validator
 * @return A new validator that applies both validators in sequence
 */
fun <IN, OUT, NEW> Validator<IN, OUT>.then(after: Validator<OUT, NEW>): Validator<IN, NEW> {
    val before = this
    return Validator { input, context ->
        when (val result = before.execute(input, context)) {
            is Success -> after.execute(result.value, result.context)
            is Failure -> {
                val value =
                    when (val v = result.value) {
                        is Input.Unknown -> Input.Unknown(v.value)
                        is Input.Some -> Input.Unknown(v.value)
                    }
                Failure(value, result.messages)
            }
        }
    }
}

fun <IN, OUT, NEW> Validator<IN, OUT>.then(block: (Validator<OUT, OUT>) -> Validator<OUT, NEW>): Validator<IN, NEW> =
    then(block(Validator.success()))
