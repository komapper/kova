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
fun <C : Collection<*>> CollectionValidator<C>.min(
    size: Int,
    message: MessageProvider = MessageProvider.resource(),
) = constrain("kova.collection.min") {
    satisfies(it.input.size >= size, message("actualSize" to it.input.size, "minSize" to size))
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
fun <C : Collection<*>> CollectionValidator<C>.max(
    size: Int,
    message: MessageProvider = MessageProvider.resource(),
) = constrain("kova.collection.max") {
    satisfies(it.input.size <= size, message("actualSize" to it.input.size, "maxSize" to size))
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
fun <C : Collection<*>> CollectionValidator<C>.notEmpty(message: MessageProvider = MessageProvider.resource()) =
    constrain("kova.collection.notEmpty") {
        satisfies(it.input.isNotEmpty(), message())
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
fun <C : Collection<*>> CollectionValidator<C>.length(
    size: Int,
    message: MessageProvider = MessageProvider.resource(),
) = constrain("kova.collection.length") {
    satisfies(it.input.size == size, message("actualSize" to it.input.size, "expectedSize" to size))
}

/**
 * Validates that the collection contains the specified element.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.collection<String>().contains("foo")
 * validator.validate(listOf("foo", "bar"))  // Success
 * validator.validate(listOf("bar", "baz"))  // Failure
 * ```
 *
 * @param element The element that must be present in the collection
 * @param message Custom error message provider
 * @return A new validator with the contains constraint
 */
fun <E, C : Collection<E>> CollectionValidator<C>.contains(
    element: E,
    message: MessageProvider = MessageProvider.resource(),
) = constrain("kova.collection.contains") {
    satisfies(it.input.contains(element), message("element" to element))
}

/**
 * Validates that the collection does not contain the specified element.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.collection<String>().notContains("foo")
 * validator.validate(listOf("bar", "baz"))  // Success
 * validator.validate(listOf("foo", "bar"))  // Failure
 * ```
 *
 * @param element The element that must not be present in the collection
 * @param message Custom error message provider
 * @return A new validator with the notContains constraint
 */
fun <E, C : Collection<E>> CollectionValidator<C>.notContains(
    element: E,
    message: MessageProvider = MessageProvider.resource(),
) = constrain("kova.collection.notContains") {
    satisfies(!it.input.contains(element), message("element" to element))
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
    constrain("kova.collection.onEach") { constraintContext ->
        val validationContext = constraintContext.validationContext
        val failures = mutableListOf<ValidationResult.Failure<*>>()
        for ((i, element) in constraintContext.input.withIndex()) {
            val path = "[$i]<collection element>"
            val result = validationContext.appendPath(path) { validator.execute(element) }
            if (result.isFailure()) {
                failures.add(result)
                if (validationContext.failFast) {
                    break
                }
            }
        }
        val messages = failures.flatMap { it.messages }
        satisfies(messages.isEmpty()) {
            val messageContext = it.createMessageContext(listOf("messages" to messages))
            Message.Collection(messageContext, failures)
        }
    }

/**
 * Lambda-based overload of [onEach] for more fluent validation composition.
 *
 * This allows building an element validator using a lambda function instead of providing
 * a pre-built validator instance.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.collection<String>()
 *     .notEmpty()
 *     .onEach { it.min(2).max(10) }
 *
 * validator.validate(listOf("abc", "def"))    // Success
 * validator.validate(listOf("a", "b"))        // Failure: elements too short
 * ```
 *
 * @param block A function that builds an element validator from a success validator
 * @return A new validator with per-element validation
 */
fun <E, C : Collection<E>> CollectionValidator<C>.onEach(block: (IdentityValidator<E>) -> Validator<E, *>) =
    onEach(block(Validator.success()))
