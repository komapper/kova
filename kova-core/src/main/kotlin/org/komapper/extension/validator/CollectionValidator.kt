package org.komapper.extension.validator

interface CollectionValidator<E, C : Collection<E>> :
    Validator<C, C>,
    Constrainable<C, CollectionValidator<E, C>> {
    fun min(
        size: Int,
        message: (ConstraintContext<C>, Int, Int) -> Message = Message.resource2(),
    ): CollectionValidator<E, C>

    fun max(
        size: Int,
        message: (ConstraintContext<C>, Int, Int) -> Message = Message.resource2(),
    ): CollectionValidator<E, C>

    fun notEmpty(message: (ConstraintContext<C>) -> Message = Message.resource0()): CollectionValidator<E, C>

    fun length(
        size: Int,
        message: (ConstraintContext<C>, Int) -> Message = Message.resource1(),
    ): CollectionValidator<E, C>

    fun onEach(validator: Validator<E, E>): CollectionValidator<E, C>
}

fun <E, C : Collection<E>> CollectionValidator(
    prev: Validator<C, C> = EmptyValidator(),
    constraint: Constraint<C> = Constraint.satisfied(),
): CollectionValidator<E, C> = CollectionValidatorImpl(prev, constraint)

private class CollectionValidatorImpl<E, C : Collection<E>>(
    private val prev: Validator<C, C>,
    constraint: Constraint<C>,
) : CollectionValidator<E, C> {
    private val next: ConstraintValidator<C> = ConstraintValidator(constraint)

    override fun execute(
        context: ValidationContext,
        input: C,
    ): ValidationResult<C> = prev.chain(next).execute(context, input)

    override fun constrain(
        id: String,
        check: ConstraintScope.(ConstraintContext<C>) -> ConstraintResult,
    ): CollectionValidator<E, C> = CollectionValidatorImpl(prev = this, constraint = Constraint(id, check))

    override fun min(
        size: Int,
        message: (ConstraintContext<C>, Int, Int) -> Message,
    ): CollectionValidator<E, C> =
        constrain("kova.collection.min") {
            satisfies(it.input.size >= size, message(it, it.input.size, size))
        }

    override fun max(
        size: Int,
        message: (ConstraintContext<C>, Int, Int) -> Message,
    ): CollectionValidator<E, C> =
        constrain("kova.collection.max") {
            satisfies(it.input.size <= size, message(it, it.input.size, size))
        }

    override fun notEmpty(message: (ConstraintContext<C>) -> Message): CollectionValidator<E, C> =
        constrain("kova.collection.notEmpty") {
            satisfies(it.input.isNotEmpty(), message(it))
        }

    override fun length(
        size: Int,
        message: (ConstraintContext<C>, Int) -> Message,
    ): CollectionValidator<E, C> =
        constrain("kova.collection.length") {
            satisfies(it.input.size == size, message(it, size))
        }

    override fun onEach(validator: Validator<E, E>): CollectionValidator<E, C> =
        constrain("kova.collection.onEach") {
            val validationContext = it.createValidationContext()
            val failures = mutableListOf<ValidationResult.Failure>()
            for ((i, element) in it.input.withIndex()) {
                val path = "[$i]<collection element>"
                val result = validator.execute(validationContext.appendPath(path), element)
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
}
