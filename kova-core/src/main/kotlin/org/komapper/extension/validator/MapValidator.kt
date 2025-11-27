package org.komapper.extension.validator

class MapValidator<K, V> internal constructor(
    private val prev: MapValidator<K, V>? = null,
    constraint: Constraint<Map<K, V>> = Constraint("kova.map") { ConstraintResult.Satisfied },
) : Validator<Map<K, V>, Map<K, V>> {
    private val next: ConstraintValidator<Map<K, V>> = ConstraintValidator(constraint)

    override fun execute(
        context: ValidationContext,
        input: Map<K, V>,
    ): ValidationResult<Map<K, V>> =
        if (prev == null) {
            next.execute(context, input)
        } else {
            chain(prev, context, input) { context, input ->
                next.execute(context, input)
            }
        }

    fun constraint(
        key: String,
        check: ConstraintScope.(ConstraintContext<Map<K, V>>) -> ConstraintResult,
    ): MapValidator<K, V> = MapValidator(prev = this, constraint = Constraint(key, check))

    fun min(
        size: Int,
        message: (ConstraintContext<Map<K, V>>, Int, Int) -> Message = Message.resource2(),
    ): MapValidator<K, V> =
        constraint("kova.map.min") {
            satisfies(it.input.size >= size, message(it, it.input.size, size))
        }

    fun onEach(validator: Validator<Map.Entry<K, V>, Map.Entry<K, V>>): MapValidator<K, V> =
        constraint("kova.map.onEach") {
            validateOnEach(it) { entry, validationContext ->
                val path = "<map entry>"
                validator.execute(validationContext.appendPath(path = path), entry)
            }
        }

    fun onEachKey(validator: Validator<K, K>): MapValidator<K, V> =
        constraint("kova.map.onEachKey") {
            validateOnEach(it) { entry, validationContext ->
                val path = "<map key>"
                validator.execute(validationContext.appendPath(path = path), entry.key)
            }
        }

    fun onEachValue(validator: Validator<V, V>): MapValidator<K, V> =
        constraint("kova.map.onEachValue") {
            validateOnEach(it) { entry, validationContext ->
                val path = "[${entry.key}]<map value>"
                validator.execute(validationContext.appendPath(path = path), entry.value)
            }
        }

    private fun <T> ConstraintScope.validateOnEach(
        context: ConstraintContext<Map<K, V>>,
        validate: (Map.Entry<K, V>, ValidationContext) -> ValidationResult<T>,
    ): ConstraintResult {
        val validationContext = context.createValidationContext()
        val failures = mutableListOf<ValidationResult.Failure>()
        for (entry in context.input.entries) {
            val result = validate(entry, validationContext)
            if (result.isFailure()) {
                failures.add(result)
                if (context.failFast) {
                    break
                }
            }
        }
        val failureDetails = failures.flatMap { it.details }
        return satisfies(failureDetails.isEmpty(), Message.ValidationFailure(details = failureDetails))
    }
}
