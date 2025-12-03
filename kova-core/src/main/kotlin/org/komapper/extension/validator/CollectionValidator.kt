package org.komapper.extension.validator

/**
 * Validator for Collection types (List, Set, Collection) with size and element validation.
 *
 * Provides methods for validating collection size and applying validators to elements.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.list<String>()
 *     .min(1)
 *     .max(10)
 *     .onEach(Kova.string().min(1).max(50))
 * ```
 *
 * @param E The element type of the collection
 * @param C The specific collection type (Collection, List, Set)
 */
interface CollectionValidator<E, C : Collection<E>> :
    Validator<C, C>,
    Constrainable<C, CollectionValidator<E, C>> {
    /**
     * Validates that the collection has at least [size] elements.
     *
     * @param size Minimum number of elements required
     * @param message Custom error message provider
     */
    fun min(
        size: Int,
        message: MessageProvider2<C, Int, Int> = Message.resource2("kova.collection.min"),
    ): CollectionValidator<E, C>

    /**
     * Validates that the collection has at most [size] elements.
     *
     * @param size Maximum number of elements allowed
     * @param message Custom error message provider
     */
    fun max(
        size: Int,
        message: MessageProvider2<C, Int, Int> = Message.resource2("kova.collection.max"),
    ): CollectionValidator<E, C>

    /**
     * Validates that the collection is not empty (size > 0).
     *
     * @param message Custom error message provider
     */
    fun notEmpty(message: MessageProvider0<C> = Message.resource0("kova.collection.notEmpty")): CollectionValidator<E, C>

    /**
     * Validates that the collection has exactly [size] elements.
     *
     * @param size Exact number of elements required
     * @param message Custom error message provider
     */
    fun length(
        size: Int,
        message: MessageProvider1<C, Int> = Message.resource1("kova.collection.length"),
    ): CollectionValidator<E, C>

    /**
     * Applies the given validator to each element in the collection.
     *
     * Failures will be reported with paths like "collection[0]", "collection[1]", etc.
     *
     * Example:
     * ```kotlin
     * val validator = Kova.list<String>().onEach(Kova.string().min(1).max(10))
     * ```
     *
     * @param validator The validator to apply to each element
     */
    fun onEach(validator: Validator<E, *>): CollectionValidator<E, C>

    operator fun plus(other: Validator<C, C>): CollectionValidator<E, C>

    infix fun and(other: Validator<C, C>): CollectionValidator<E, C>

    infix fun or(other: Validator<C, C>): CollectionValidator<E, C>

    fun chain(other: Validator<C, C>): CollectionValidator<E, C>
}

fun <E, C : Collection<E>> CollectionValidator(
    name: String = "empty",
    prev: Validator<C, C> = EmptyValidator(),
    constraint: Constraint<C> = Constraint.satisfied(),
): CollectionValidator<E, C> = CollectionValidatorImpl(name, prev, constraint)

private class CollectionValidatorImpl<E, C : Collection<E>>(
    private val name: String,
    private val prev: Validator<C, C>,
    private val constraint: Constraint<C> = Constraint.satisfied(),
) : CollectionValidator<E, C> {
    private val next: ConstraintValidator<C> = ConstraintValidator(constraint)

    override fun execute(
        input: C,
        context: ValidationContext,
    ): ValidationResult<C> {
        val context = context.addLog(toString())
        return prev.chain(next).execute(input, context)
    }

    override fun constrain(
        id: String,
        check: ConstraintScope.(ConstraintContext<C>) -> ConstraintResult,
    ): CollectionValidator<E, C> = CollectionValidatorImpl(name = id, prev = this, constraint = Constraint(id, check))

    override fun min(
        size: Int,
        message: MessageProvider2<C, Int, Int>,
    ): CollectionValidator<E, C> =
        constrain(message.key) {
            satisfies(it.input.size >= size, message(it, it.input.size, size))
        }

    override fun max(
        size: Int,
        message: MessageProvider2<C, Int, Int>,
    ): CollectionValidator<E, C> =
        constrain(message.key) {
            satisfies(it.input.size <= size, message(it, it.input.size, size))
        }

    override fun notEmpty(message: MessageProvider0<C>): CollectionValidator<E, C> =
        constrain(message.key) {
            satisfies(it.input.isNotEmpty(), message(it))
        }

    override fun length(
        size: Int,
        message: MessageProvider1<C, Int>,
    ): CollectionValidator<E, C> =
        constrain(message.key) {
            satisfies(it.input.size == size, message(it, size))
        }

    override fun onEach(validator: Validator<E, *>): CollectionValidator<E, C> =
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

    override operator fun plus(other: Validator<C, C>): CollectionValidator<E, C> = and(other)

    override fun and(other: Validator<C, C>): CollectionValidator<E, C> {
        val combined = (this as Validator<C, C>).and(other)
        return CollectionValidatorImpl("and", prev = combined)
    }

    override fun or(other: Validator<C, C>): CollectionValidator<E, C> {
        val combined = (this as Validator<C, C>).or(other)
        return CollectionValidatorImpl("or", prev = combined)
    }

    override fun chain(other: Validator<C, C>): CollectionValidator<E, C> {
        val combined = (this as Validator<C, C>).chain(other)
        return CollectionValidatorImpl("chain", prev = combined)
    }

    override fun toString(): String = "${CollectionValidator::class.simpleName}(name=$name)"
}
