package org.komapper.extension.validator

typealias MapValidator<K, V> = IdentityValidator<Map<K, V>>

fun <K, V> MapValidator<K, V>.min(
    size: Int,
    message: MessageProvider2<Map<K, V>, Int, Int> = Message.resource2("kova.map.min"),
) = constrain(message.id) {
    satisfies(it.input.size >= size, message(it, it.input.size, size))
}

fun <K, V> MapValidator<K, V>.max(
    size: Int,
    message: MessageProvider2<Map<K, V>, Int, Int> = Message.resource2("kova.map.max"),
) = constrain(message.id) {
    satisfies(it.input.size <= size, message(it, it.input.size, size))
}

fun <K, V> MapValidator<K, V>.notEmpty(message: MessageProvider0<Map<K, V>> = Message.resource0("kova.map.notEmpty")) =
    constrain(message.id) {
        satisfies(it.input.isNotEmpty(), message(it))
    }

fun <K, V> MapValidator<K, V>.length(
    size: Int,
    message: MessageProvider1<Map<K, V>, Int> = Message.resource1("kova.map.length"),
) = constrain(message.id) {
    satisfies(it.input.size == size, message(it, size))
}

fun <K, V> MapValidator<K, V>.onEach(validator: Validator<Map.Entry<K, V>, *>) =
    constrain("kova.map.onEach") {
        validateOnEach(it) { entry, validationContext ->
            val path = "<map entry>"
            validator.execute(entry, validationContext.appendPath(text = path))
        }
    }

fun <K, V> MapValidator<K, V>.onEachKey(validator: Validator<K, *>) =
    constrain("kova.map.onEachKey") {
        validateOnEach(it) { entry, validationContext ->
            val path = "<map key>"
            validator.execute(entry.key, validationContext.appendPath(text = path))
        }
    }

fun <K, V> MapValidator<K, V>.onEachValue(validator: Validator<V, *>) =
    constrain("kova.map.onEachValue") {
        validateOnEach(it) { entry, validationContext ->
            val path = "[${entry.key}]<map value>"
            validator.execute(entry.value, validationContext.appendPath(text = path))
        }
    }

private fun <K, V, T> ConstraintScope.validateOnEach(
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
