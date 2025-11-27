package org.komapper.extension.validator

class MapEntryValidator<K, V> internal constructor(
    private val delegate: CoreValidator<Map.Entry<K, V>> = CoreValidator(),
) : Validator<Map.Entry<K, V>, Map.Entry<K, V>> by delegate {
    operator fun plus(other: MapEntryValidator<K, V>): MapEntryValidator<K, V> = MapEntryValidator(delegate + other.delegate)

    fun constraint(
        key: String,
        check: (ConstraintContext<Map.Entry<K, V>>) -> ConstraintResult,
    ): MapEntryValidator<K, V> =
        MapEntryValidator(
            delegate + Constraint(key, check),
        )
}
