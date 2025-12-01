package org.komapper.extension.validator

interface MapEntryValidator<K, V> :
    Validator<Map.Entry<K, V>, Map.Entry<K, V>>,
    Constrainable<Map.Entry<K, V>, MapEntryValidator<K, V>> {
    operator fun plus(other: Validator<Map.Entry<K, V>, Map.Entry<K, V>>): MapEntryValidator<K, V>

    infix fun and(other: Validator<Map.Entry<K, V>, Map.Entry<K, V>>): MapEntryValidator<K, V>

    infix fun or(other: Validator<Map.Entry<K, V>, Map.Entry<K, V>>): MapEntryValidator<K, V>

    fun chain(other: Validator<Map.Entry<K, V>, Map.Entry<K, V>>): MapEntryValidator<K, V>
}

fun <K, V> MapEntryValidator(
    prev: Validator<Map.Entry<K, V>, Map.Entry<K, V>> = EmptyValidator(),
    constraint: Constraint<Map.Entry<K, V>> = Constraint.satisfied(),
): MapEntryValidator<K, V> = MapEntryValidatorImpl(prev, constraint)

private class MapEntryValidatorImpl<K, V> internal constructor(
    private val prev: Validator<Map.Entry<K, V>, Map.Entry<K, V>>,
    constraint: Constraint<Map.Entry<K, V>> = Constraint.satisfied(),
) : MapEntryValidator<K, V> {
    private val next: ConstraintValidator<Map.Entry<K, V>> = ConstraintValidator(constraint)

    override fun execute(
        context: ValidationContext,
        input: Map.Entry<K, V>,
    ): ValidationResult<Map.Entry<K, V>> = prev.chain(next).execute(context, input)

    override fun constrain(
        id: String,
        check: ConstraintScope.(ConstraintContext<Map.Entry<K, V>>) -> ConstraintResult,
    ): MapEntryValidator<K, V> = MapEntryValidatorImpl(prev = this, constraint = Constraint(id, check))

    override operator fun plus(other: Validator<Map.Entry<K, V>, Map.Entry<K, V>>): MapEntryValidator<K, V> = and(other)

    override fun and(other: Validator<Map.Entry<K, V>, Map.Entry<K, V>>): MapEntryValidator<K, V> {
        val combined = (this as Validator<Map.Entry<K, V>, Map.Entry<K, V>>).and(other)
        return MapEntryValidatorImpl(prev = combined)
    }

    override fun or(other: Validator<Map.Entry<K, V>, Map.Entry<K, V>>): MapEntryValidator<K, V> {
        val combined = (this as Validator<Map.Entry<K, V>, Map.Entry<K, V>>).or(other)
        return MapEntryValidatorImpl(prev = combined)
    }

    override fun chain(other: Validator<Map.Entry<K, V>, Map.Entry<K, V>>): MapEntryValidator<K, V> {
        val combined = (this as Validator<Map.Entry<K, V>, Map.Entry<K, V>>).chain(other)
        return MapEntryValidatorImpl(prev = combined)
    }
}
