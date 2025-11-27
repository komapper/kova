package org.komapper.extension.validator

class MapEntryValidator<K, V> internal constructor(
    private val prev: MapEntryValidator<K, V>? = null,
    constraint: Constraint<Map.Entry<K, V>> = Constraint("kova.mapEntry") { ConstraintResult.Satisfied },
) : Validator<Map.Entry<K, V>, Map.Entry<K, V>> {
    private val next: ConstraintValidator<Map.Entry<K, V>> = ConstraintValidator(constraint)

    override fun execute(
        context: ValidationContext,
        input: Map.Entry<K, V>,
    ): ValidationResult<Map.Entry<K, V>> =
        if (prev == null) {
            next.execute(context, input)
        } else {
            chain(prev, context, input) { context, input ->
                next.execute(context, input)
            }
        }

    fun constraint(
        key: String,
        check: ConstraintScope.(ConstraintContext<Map.Entry<K, V>>) -> ConstraintResult,
    ): MapEntryValidator<K, V> = MapEntryValidator(prev = this, constraint = Constraint(key, check))
}
