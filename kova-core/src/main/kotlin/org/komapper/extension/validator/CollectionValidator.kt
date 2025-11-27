package org.komapper.extension.validator

open class CollectionValidator<E, C : Collection<E>> internal constructor(
    // TODO
    private val constraint: Constraint<C> = Constraint("kova.collection") { ConstraintResult.Satisfied },
) : Validator<C, C> {
    override fun execute(
        context: ValidationContext,
        input: C,
    ): ValidationResult<C> = ConstraintValidator(constraint).execute(context, input)

    fun constraint(
        key: String,
        check: ConstraintScope.(ConstraintContext<C>) -> ConstraintResult,
    ): CollectionValidator<E, C> {
        val before = this
        return object : CollectionValidator<E, C>(Constraint(key, check)) {
            override fun execute(
                context: ValidationContext,
                input: C,
            ): ValidationResult<C> =
                chain(before, context, input) { context, input ->
                    super.execute(context, input)
                }
        }
    }

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
