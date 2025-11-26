package org.komapper.extension.validator

class CollectionValidator<E, C : Collection<E>> internal constructor(
    private val delegate: CoreValidator<C> = CoreValidator(),
) : Validator<C, C> by delegate {
    operator fun plus(other: CollectionValidator<E, C>): CollectionValidator<E, C> = CollectionValidator(delegate + other.delegate)

    fun constraint(constraint: Constraint<C>): CollectionValidator<E, C> = CollectionValidator(delegate + constraint)

    fun min(
        size: Int,
        message: (ConstraintContext<C>, Int, Int) -> Message = Message.resource2("kova.collection.min"),
    ): CollectionValidator<E, C> =
        constraint {
            Constraint.satisfies(it.input.size >= size, message(it, it.input.size, size))
        }

    fun onEach(validator: Validator<E, E>): CollectionValidator<E, C> =
        constraint {
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
            Constraint.satisfies(failureDetails.isEmpty(), Message.ValidationFailure(details = failureDetails))
        }
}
