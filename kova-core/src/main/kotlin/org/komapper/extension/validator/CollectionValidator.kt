package org.komapper.extension.validator

class CollectionValidator<E, C : Collection<E>> internal constructor(
    private val prev: CollectionValidator<E, C>? = null,
    constraint: Constraint<C> = Constraint("kova.collection") { ConstraintResult.Satisfied },
) : Validator<C, C> {
    private val next: ConstraintValidator<C> = ConstraintValidator(constraint)

    override fun execute(
        context: ValidationContext,
        input: C,
    ): ValidationResult<C> =
        if (prev == null) {
            next.execute(context, input)
        } else {
            prev.chain(next = next).execute(context, input)
        }

    fun constraint(
        key: String,
        check: ConstraintScope.(ConstraintContext<C>) -> ConstraintResult,
    ): CollectionValidator<E, C> = CollectionValidator(prev = this, constraint = Constraint(key, check))

    fun min(
        size: Int,
        message: (ConstraintContext<C>, Int, Int) -> Message = Message.resource2(),
    ): CollectionValidator<E, C> =
        constraint("kova.collection.min") {
            satisfies(it.input.size >= size, message(it, it.input.size, size))
        }

    fun onEach(validator: Validator<E, E>): CollectionValidator<E, C> =
        constraint("kova.collection.onEach") {
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
