package org.komapper.extension.validator

/**
 * Type alias for collection validators.
 *
 * Provides a convenient type for validators that work with Collection types.
 *
 * @param C The collection type being validated
 */
typealias CollectionValidator<C> = IdentityValidator<C>

/**
 * Validates that the collection size is at least the specified minimum.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.collection<String>().min(2)
 * validator.validate(listOf("a", "b", "c")) // Success
 * validator.validate(listOf("a"))           // Failure
 * ```
 *
 * @param size Minimum collection size (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the minimum size constraint
 */
fun <E, C : Collection<E>> CollectionValidator<C>.min(
    size: Int,
    message: MessageProvider2<C, Int, Int> = Message.resource2("kova.collection.min"),
) = constrain(message.id) {
    satisfies(it.input.size >= size, message(it, it.input.size, size))
}

/**
 * Validates that the collection size does not exceed the specified maximum.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.collection<String>().max(3)
 * validator.validate(listOf("a", "b"))       // Success
 * validator.validate(listOf("a", "b", "c", "d")) // Failure
 * ```
 *
 * @param size Maximum collection size (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the maximum size constraint
 */
fun <E, C : Collection<E>> CollectionValidator<C>.max(
    size: Int,
    message: MessageProvider2<C, Int, Int> = Message.resource2("kova.collection.max"),
) = constrain(message.id) {
    satisfies(it.input.size <= size, message(it, it.input.size, size))
}

/**
 * Validates that the collection is not empty.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.collection<String>().notEmpty()
 * validator.validate(listOf("a")) // Success
 * validator.validate(listOf())    // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the not-empty constraint
 */
fun <E, C : Collection<E>> CollectionValidator<C>.notEmpty(message: MessageProvider0<C> = Message.resource0("kova.collection.notEmpty")) =
    constrain(message.id) {
        satisfies(it.input.isNotEmpty(), message(it))
    }

/**
 * Validates that the collection size equals exactly the specified value.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.collection<String>().length(3)
 * validator.validate(listOf("a", "b", "c")) // Success
 * validator.validate(listOf("a", "b"))      // Failure
 * ```
 *
 * @param size Exact collection size required
 * @param message Custom error message provider
 * @return A new validator with the exact size constraint
 */
fun <E, C : Collection<E>> CollectionValidator<C>.length(
    size: Int,
    message: MessageProvider1<C, Int> = Message.resource1("kova.collection.length"),
) = constrain(message.id) {
    satisfies(it.input.size == size, message(it, size))
}

/**
 * Validates each element of the collection using the specified validator.
 *
 * If any element fails validation, the entire collection validation fails.
 * Error paths include element indices for better error reporting.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.collection<String>()
 *     .notEmpty()
 *     .onEach(Kova.string().min(2).max(10))
 *
 * validator.validate(listOf("abc", "def"))    // Success
 * validator.validate(listOf("a", "b"))        // Failure: elements too short
 * ```
 *
 * @param validator The validator to apply to each element
 * @return A new validator with per-element validation
 */
fun <E, C : Collection<E>> CollectionValidator<C>.onEach(validator: Validator<E, *>) =
    constrain("kova.collection.onEach") {
        val validationContext = it.validationContext
        val failures = mutableListOf<ValidationResult.Failure>()
        for ((i, element) in it.input.withIndex()) {
            val path = "[$i]<collection element>"
            val result = validator.execute(element, validationContext.appendPath(path))
            if (result.isFailure()) {
                failures.add(result)
                if (validationContext.failFast) {
                    break
                }
            }
        }
        val failureDetails = failures.flatMap { failure -> failure.details }
        satisfies(failureDetails.isEmpty(), Message.ValidationFailure(details = failureDetails))
    }
