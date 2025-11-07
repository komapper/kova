package org.komapper.extension.validator

class MapValidator<K, V> internal constructor(
    private val delegate: CoreValidator<Map<K, V>, Map<K, V>> = CoreValidator(transform = { it }),
) : Validator<Map<K, V>, Map<K, V>> by delegate {
    operator fun plus(other: MapValidator<K, V>): MapValidator<K, V> = MapValidator(delegate + other.delegate)

    fun constraint(constraint: Constraint<Map<K, V>>): MapValidator<K, V> = MapValidator(delegate + constraint)

    fun min(
        size: Int,
        message: (ConstraintContext<Map<K, V>>, Int, Int) -> Message = Message.resource2("kova.map.min"),
    ): MapValidator<K, V> =
        constraint {
            Constraint.check({ it.input.size >= size }, { message(it, it.input.size, size) })
        }

    fun onEach(validator: Validator<Map.Entry<K, V>, Map.Entry<K, V>>): MapValidator<K, V> =
        constraint {
            validateOnEach(it) { entry, validationContext ->
                val path = "<map entry>"
                validator.tryValidate(entry, validationContext.appendPath(path = path))
            }
        }

    fun onEachKey(validator: Validator<K, K>): MapValidator<K, V> =
        constraint {
            validateOnEach(it) { entry, validationContext ->
                val path = "<map key>"
                validator.tryValidate(entry.key, validationContext.appendPath(path = path))
            }
        }

    fun onEachValue(validator: Validator<V, V>): MapValidator<K, V> =
        constraint {
            validateOnEach(it) { entry, validationContext ->
                val path = "[${entry.key}]<map value>"
                validator.tryValidate(entry.value, validationContext.appendPath(path = path))
            }
        }

    private fun <T> validateOnEach(
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
        return Constraint.check({ failureDetails.isEmpty() }, { Message.ValidationFailure(failureDetails) })
    }
}
