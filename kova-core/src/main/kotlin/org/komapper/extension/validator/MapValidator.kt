package org.komapper.extension.validator

interface MapValidator<K, V> :
    Validator<Map<K, V>, Map<K, V>>,
    Constrainable<Map<K, V>, MapValidator<K, V>> {
    fun min(
        size: Int,
        message: MessageProvider2<Map<K, V>, Int, Int> = Message.resource2("kova.map.min"),
    ): MapValidator<K, V>

    fun max(
        size: Int,
        message: MessageProvider2<Map<K, V>, Int, Int> = Message.resource2("kova.map.max"),
    ): MapValidator<K, V>

    fun notEmpty(message: MessageProvider0<Map<K, V>> = Message.resource0("kova.map.notEmpty")): MapValidator<K, V>

    fun length(
        size: Int,
        message: MessageProvider1<Map<K, V>, Int> = Message.resource1("kova.map.length"),
    ): MapValidator<K, V>

    fun onEach(validator: Validator<Map.Entry<K, V>, *>): MapValidator<K, V>

    fun onEachKey(validator: Validator<K, *>): MapValidator<K, V>

    fun onEachValue(validator: Validator<V, *>): MapValidator<K, V>

    operator fun plus(other: Validator<Map<K, V>, Map<K, V>>): MapValidator<K, V>

    infix fun and(other: Validator<Map<K, V>, Map<K, V>>): MapValidator<K, V>

    infix fun or(other: Validator<Map<K, V>, Map<K, V>>): MapValidator<K, V>

    fun chain(other: Validator<Map<K, V>, Map<K, V>>): MapValidator<K, V>
}

fun <K, V> MapValidator(
    name: String = "empty",
    prev: Validator<Map<K, V>, Map<K, V>> = EmptyValidator(),
    constraint: Constraint<Map<K, V>> = Constraint.satisfied(),
): MapValidator<K, V> = MapValidatorImpl(name, prev, constraint)

private class MapValidatorImpl<K, V>(
    private val name: String,
    private val prev: Validator<Map<K, V>, Map<K, V>>,
    private val constraint: Constraint<Map<K, V>> = Constraint.satisfied(),
) : MapValidator<K, V> {
    private val next: ConstraintValidator<Map<K, V>> = ConstraintValidator(constraint)

    override fun execute(
        input: Map<K, V>,
        context: ValidationContext,
    ): ValidationResult<Map<K, V>> {
        val context = context.addLog(toString())
        return prev.chain(next).execute(input, context)
    }

    override fun constrain(
        id: String,
        check: ConstraintScope.(ConstraintContext<Map<K, V>>) -> ConstraintResult,
    ): MapValidator<K, V> = MapValidatorImpl(name = id, prev = this, constraint = Constraint(id, check))

    override fun min(
        size: Int,
        message: MessageProvider2<Map<K, V>, Int, Int>,
    ): MapValidator<K, V> =
        constrain(message.id) {
            satisfies(it.input.size >= size, message(it, it.input.size, size))
        }

    override fun max(
        size: Int,
        message: MessageProvider2<Map<K, V>, Int, Int>,
    ): MapValidator<K, V> =
        constrain(message.id) {
            satisfies(it.input.size <= size, message(it, it.input.size, size))
        }

    override fun notEmpty(message: MessageProvider0<Map<K, V>>): MapValidator<K, V> =
        constrain(message.id) {
            satisfies(it.input.isNotEmpty(), message(it))
        }

    override fun length(
        size: Int,
        message: MessageProvider1<Map<K, V>, Int>,
    ): MapValidator<K, V> =
        constrain(message.id) {
            satisfies(it.input.size == size, message(it, size))
        }

    override fun onEach(validator: Validator<Map.Entry<K, V>, *>): MapValidator<K, V> =
        constrain("kova.map.onEach") {
            validateOnEach(it) { entry, validationContext ->
                val path = "<map entry>"
                validator.execute(entry, validationContext.appendPath(text = path))
            }
        }

    override fun onEachKey(validator: Validator<K, *>): MapValidator<K, V> =
        constrain("kova.map.onEachKey") {
            validateOnEach(it) { entry, validationContext ->
                val path = "<map key>"
                validator.execute(entry.key, validationContext.appendPath(text = path))
            }
        }

    override fun onEachValue(validator: Validator<V, *>): MapValidator<K, V> =
        constrain("kova.map.onEachValue") {
            validateOnEach(it) { entry, validationContext ->
                val path = "[${entry.key}]<map value>"
                validator.execute(entry.value, validationContext.appendPath(text = path))
            }
        }

    private fun <T> ConstraintScope.validateOnEach(
        context: ConstraintContext<Map<K, V>>,
        validate: (Map.Entry<K, V>, ValidationContext) -> ValidationResult<T>,
    ): ConstraintResult {
        val validationContext = context.validationContext
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

    override operator fun plus(other: Validator<Map<K, V>, Map<K, V>>): MapValidator<K, V> = and(other)

    override fun and(other: Validator<Map<K, V>, Map<K, V>>): MapValidator<K, V> {
        val combined = (this as Validator<Map<K, V>, Map<K, V>>).and(other)
        return MapValidatorImpl("and", prev = combined)
    }

    override fun or(other: Validator<Map<K, V>, Map<K, V>>): MapValidator<K, V> {
        val combined = (this as Validator<Map<K, V>, Map<K, V>>).or(other)
        return MapValidatorImpl("or", prev = combined)
    }

    override fun chain(other: Validator<Map<K, V>, Map<K, V>>): MapValidator<K, V> {
        val combined = (this as Validator<Map<K, V>, Map<K, V>>).chain(other)
        return MapValidatorImpl("chain", prev = combined)
    }

    override fun toString(): String = "${MapValidator::class.simpleName}(name=$name)"
}
