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
    fun ValidationContext.execute(input: IN): ValidationResult<OUT>

    companion object {
        /**
         * Creates a validator that always succeeds and returns the input unchanged.
         *
         * This is primarily used as a starting point for lambda-based composition methods
         * like `and { }`, `or { }`, `then { }`, and `compose { }`.
         *
         * Example:
         * ```kotlin
         * val validator = Kova.string().and { it.min(3).max(10) }
         * // Internally uses Validator.success() as the starting point
         * ```
         *
         * @return An identity validator that always succeeds
         */
        fun <T> success() = IdentityValidator<T> { input -> Success(input) }
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
): ValidationResult<OUT> = ValidationContext(config = config).execute(input)

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
    when (val result = ValidationContext(config = config).execute(input)) {
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
    return Validator { input ->
        when (val selfResult = self.execute(input)) {
            is Failure if failFast -> selfResult
            else -> selfResult + other.execute(input)
        }
    }
}

/**
 * Lambda-based overload of [and] for more fluent composition.
 *
 * This allows building a validator using a lambda function instead of providing
 * a pre-built validator instance.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().min(1).and { it.max(10).startsWith("A") }
 * // Equivalent to: Kova.string().min(1) and Kova.string().max(10).startsWith("A")
 * ```
 *
 * @param block A function that builds a validator from a success validator
 * @return A new validator combining both with AND logic
 */
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
    return Validator { input ->
        when (val selfResult = self.execute(input)) {
            is Success -> selfResult
            is Failure -> {
                when (val otherResult = other.execute(input)) {
                    is Success -> otherResult
                    is Failure ->
                        Failure(
                            otherResult.value,
                            listOf("kova.or".resource(selfResult.messages, otherResult.messages)),
                        )
                }
            }
        }
    }
}

/**
 * Lambda-based overload of [or] for more fluent composition.
 *
 * This allows building an alternative validator using a lambda function instead
 * of providing a pre-built validator instance.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().max(10).or { it.startsWith("LONG:") }
 * // Equivalent to: Kova.string().max(10) or Kova.string().startsWith("LONG:")
 * ```
 *
 * @param block A function that builds a validator from a success validator
 * @return A new validator combining both with OR logic
 */
fun <IN, OUT> Validator<IN, OUT>.or(block: (Validator<IN, IN>) -> Validator<IN, OUT>): Validator<IN, OUT> = or(block(Validator.success()))

/**
 * Transforms the output value on successful validation.
 *
 * When validation succeeds, the transform function is applied to the validated value.
 * When validation fails, the behavior depends on the type compatibility:
 * - If [OUT] and [NEW] are the same type and the failure value is available,
 *   the transform is applied so that subsequent validators can use the transformed value.
 * - Otherwise, the failure value is marked as unusable.
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
inline fun <IN, reified OUT, reified NEW> Validator<IN, OUT>.map(noinline transform: (OUT) -> NEW): Validator<IN, NEW> {
    val self = this
    val outClass = OUT::class
    val newClass = NEW::class
    return Validator { input ->
        when (val result = self.execute(input)) {
            is Success -> Success(transform(result.value))
            is Failure -> {
                val failureValue =
                    when (val v = result.value) {
                        is Input.Available -> {
                            val value = v.value
                            if (outClass == newClass && outClass.isInstance(value)) {
                                Input.Available(transform(value)) // subsequent validators can use this value
                            } else {
                                Input.Unusable(value)
                            }
                        }
                        is Input.Unusable -> v
                    }
                Failure(failureValue, result.messages)
            }
        }
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
    return Validator { input ->
        addPath(name, input) { self.execute(input) }
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

/**
 * Lambda-based overload of [compose] for more fluent composition.
 *
 * This allows building the preceding validator using a lambda function instead
 * of providing a pre-built validator instance.
 *
 * Example:
 * ```kotlin
 * val intValidator = Kova.int().min(0).max(100)
 * val parseAndValidate = intValidator.compose { Kova.string().isInt().map { it.toInt() } }
 * // Equivalent to: Kova.string().isInt().map { it.toInt() }.then(Kova.int().min(0).max(100))
 * ```
 *
 * @param block A function that builds the validator to apply first
 * @return A new validator that applies both validators in sequence (block first, then this)
 */
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
    return Validator { input ->
        when (val result = before.execute(input)) {
            is Success -> after.execute(result.value)
            is Failure -> {
                val value =
                    when (val v = result.value) {
                        is Input.Available -> Input.Unusable(v.value)
                        is Input.Unusable -> Input.Unusable(v.value)
                    }
                Failure(value, result.messages)
            }
        }
    }
}

/**
 * Lambda-based overload of [then] for more fluent composition.
 *
 * This allows building the subsequent validator using a lambda function instead
 * of providing a pre-built validator instance.
 *
 * Example:
 * ```kotlin
 * val parseAndValidate = Kova.string()
 *     .isInt()
 *     .map { it.toInt() }
 *     .then { it.min(0).max(100) }
 * // Equivalent to: Kova.string().isInt().map { it.toInt() }.then(Kova.int().min(0).max(100))
 * ```
 *
 * @param block A function that builds the validator to apply after this one
 * @return A new validator that applies both validators in sequence (this first, then block)
 */
fun <IN, OUT, NEW> Validator<IN, OUT>.then(block: (Validator<OUT, OUT>) -> Validator<OUT, NEW>): Validator<IN, NEW> =
    then(block(Validator.success()))

/**
 * Converts a non-nullable validator to a nullable validator.
 *
 * The resulting validator accepts null values and passes them through unchanged.
 * Non-null values are validated using the original validator.
 *
 * Example:
 * ```kotlin
 * val nonNullValidator = Kova.string().min(3).max(10)
 * val nullableValidator = nonNullValidator.asNullable()
 *
 * nullableValidator.validate(null)    // Success: null
 * nullableValidator.validate("hello") // Success: "hello"
 * nullableValidator.validate("ab")    // Failure: too short
 * ```
 *
 * @return A new nullable validator that accepts null input
 */
fun <T : Any, S : Any> Validator<T, S>.asNullable(): NullableValidator<T, S> =
    Validator { input ->
        if (input == null) Success(null) else execute(input)
    }

/**
 * Converts a non-nullable validator to a nullable validator with a default value.
 *
 * When the input is null, the validator returns the provided default value.
 * This ensures the output is always non-null.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().min(3).asNullable("default")
 * validator.validate(null)    // Success: "default"
 * validator.validate("hello") // Success: "hello"
 * validator.validate("ab")    // Failure: too short
 * ```
 *
 * @param defaultValue The value to use when input is null
 * @return A new validator that accepts null input but produces non-nullable output
 */
fun <T : Any, S : Any> Validator<T, S>.asNullable(defaultValue: S): ElvisValidator<T, S> = asNullable { defaultValue }

/**
 * Converts a non-nullable validator to a nullable validator with a lazily-evaluated default value.
 *
 * When the input is null, the provider function is called to generate the default value.
 * This is useful when the default value is expensive to compute or needs to be fresh each time.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().asNullable { UUID.randomUUID().toString() }
 * validator.validate(null)    // Success: newly generated UUID
 * validator.validate("hello") // Success: "hello"
 * ```
 *
 * @param withDefault Function that generates the default value when input is null
 * @return A new validator that accepts null input but produces non-nullable output
 */
fun <T : Any, S : Any> Validator<T, S>.asNullable(withDefault: () -> S): ElvisValidator<T, S> =
    Validator { input ->
        if (input == null) Success(withDefault()) else execute(input)
    }

/**
 * Replaces all validation error messages with a single custom message.
 *
 * This function consolidates multiple validation errors into one message, which is useful
 * when you want to provide simplified, user-friendly error messages instead of exposing
 * individual constraint violations to end users.
 *
 * The lambda receives the list of original [Message] objects from all failed constraints,
 * allowing you to create dynamic messages based on what validations failed.
 *
 * Example - Access original messages for dynamic consolidation:
 * ```kotlin
 * val validator = Kova.string().notEmpty().min(3).max(10)
 *     .withMessage { messages ->
 *         Message.text { "Validation failed: ${messages.joinToString { it.text }}" }
 *     }
 *
 * validator.tryValidate("ab") // Error: "Validation failed: must not be empty, must be at least 3 characters"
 * ```
 *
 * Example - Simple custom message ignoring original errors:
 * ```kotlin
 * val validator = Kova.string().notEmpty().min(3).max(10)
 *     .withMessage { MessageProvider.text { "Invalid username format" } }
 * ```
 *
 * Example - Internationalization with resource messages:
 * ```kotlin
 * val validator = Kova.string().notEmpty().min(3)
 *     .withMessage { MessageProvider.resource() }
 * // Uses the constraint ID "kova.withMessage" to load from kova.properties
 * ```
 *
 * @param block A function that receives the list of original error messages and returns
 *              a [MessageProvider] for the consolidated message
 * @return A new validator that returns a single custom message on validation failure
 * @see withMessage for a simpler overload that accepts a static string message
 */
fun <T, S> Validator<T, S>.withMessage(
    block: ValidationContext.(messages: List<Message>) -> Message = { "kova.withMessage".resource(it) },
): Validator<T, S> =
    Validator { input ->
        when (val result = execute(input)) {
            is Success -> result
            is Failure -> Failure(result.value, listOf(block(result.messages)))
        }
    }

/**
 * Replaces all validation error messages with a static text message.
 *
 * This is a convenience overload for the common case where you want to show a simple,
 * fixed error message instead of the detailed constraint violations.
 *
 * Example:
 * ```kotlin
 * val usernameValidator = Kova.string().notEmpty().min(3).max(20)
 *     .withMessage("Username must be between 3 and 20 characters")
 *
 * val passwordValidator = Kova.string().min(8).matches(Regex(".*[A-Z].*"))
 *     .withMessage("Password must be at least 8 characters with uppercase letters")
 * ```
 *
 * Example - In ObjectSchema:
 * ```kotlin
 * object UserSchema : ObjectSchema<User>({
 *     User::username {
 *         it.notEmpty().min(3).max(20).withMessage("Invalid username")
 *     }
 * })
 *
 * @param message The static text message to display on validation failure
 * @return A new validator that returns the custom message on validation failure
 * @see withMessage for an advanced overload that can access the original error messages
 */
fun <T, S> Validator<T, S>.withMessage(message: String): Validator<T, S> = withMessage { text(message) }
