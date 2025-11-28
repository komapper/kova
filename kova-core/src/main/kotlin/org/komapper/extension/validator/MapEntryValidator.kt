package org.komapper.extension.validator

class MapEntryValidator<K, V> internal constructor(
    private val prev: Validator<Map.Entry<K, V>, Map.Entry<K, V>> = EmptyValidator(),
    constraint: Constraint<Map.Entry<K, V>> = Constraint.satisfied(),
) : Validator<Map.Entry<K, V>, Map.Entry<K, V>>,
    Constrainable<Map.Entry<K, V>, MapEntryValidator<K, V>> {
    private val next: ConstraintValidator<Map.Entry<K, V>> = ConstraintValidator(constraint)

    override fun execute(
        context: ValidationContext,
        input: Map.Entry<K, V>,
    ): ValidationResult<Map.Entry<K, V>> = prev.chain(next).execute(context, input)

    override fun constrain(
        key: String,
        check: ConstraintScope.(ConstraintContext<Map.Entry<K, V>>) -> ConstraintResult,
    ): MapEntryValidator<K, V> = MapEntryValidator(prev = this, constraint = Constraint(key, check))
}
