package org.komapper.extension.validator

open class MapEntryValidator<K, V> internal constructor(
    // TODO
    private val constraint: Constraint<Map.Entry<K, V>> = Constraint("kova.mapEntry") { ConstraintResult.Satisfied },
) : Validator<Map.Entry<K, V>, Map.Entry<K, V>> {
    override fun execute(
        context: ValidationContext,
        input: Map.Entry<K, V>,
    ): ValidationResult<Map.Entry<K, V>> = CoreValidator(constraint).execute(context, input)

    fun constraint(
        key: String,
        check: ConstraintScope.(ConstraintContext<Map.Entry<K, V>>) -> ConstraintResult,
    ): MapEntryValidator<K, V> {
        val before = this
        return object : MapEntryValidator<K, V>(
            Constraint(key, check),
        ) {
            override fun execute(
                context: ValidationContext,
                input: Map.Entry<K, V>,
            ): ValidationResult<Map.Entry<K, V>> =
                chain(before, context, input) { context, input ->
                    super.execute(context, input)
                }
        }
    }
}
