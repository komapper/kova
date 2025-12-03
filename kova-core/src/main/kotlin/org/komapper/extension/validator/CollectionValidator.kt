package org.komapper.extension.validator

interface CollectionValidator<E, C : Collection<E>> :
    Validator<C, C>,
    Constrainable<C, CollectionValidator<E, C>> {
    fun min(
        size: Int,
        message: MessageProvider2<C, Int, Int> = Message.resource2("kova.collection.min"),
    ): CollectionValidator<E, C>

    fun max(
        size: Int,
        message: MessageProvider2<C, Int, Int> = Message.resource2("kova.collection.max"),
    ): CollectionValidator<E, C>

    fun notEmpty(message: MessageProvider0<C> = Message.resource0("kova.collection.notEmpty")): CollectionValidator<E, C>

    fun length(
        size: Int,
        message: MessageProvider1<C, Int> = Message.resource1("kova.collection.length"),
    ): CollectionValidator<E, C>

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
