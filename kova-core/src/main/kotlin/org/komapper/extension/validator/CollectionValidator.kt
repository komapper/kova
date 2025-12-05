package org.komapper.extension.validator

typealias CollectionValidator<C> = IdentityValidator<C>

fun <E, C : Collection<E>> CollectionValidator<C>.min(
    size: Int,
    message: MessageProvider2<C, Int, Int> = Message.resource2("kova.collection.min"),
) = constrain(message.id) {
    satisfies(it.input.size >= size, message(it, it.input.size, size))
}

fun <E, C : Collection<E>> CollectionValidator<C>.max(
    size: Int,
    message: MessageProvider2<C, Int, Int> = Message.resource2("kova.collection.max"),
) = constrain(message.id) {
    satisfies(it.input.size <= size, message(it, it.input.size, size))
}

fun <E, C : Collection<E>> CollectionValidator<C>.notEmpty(message: MessageProvider0<C> = Message.resource0("kova.collection.notEmpty")) =
    constrain(message.id) {
        satisfies(it.input.isNotEmpty(), message(it))
    }

fun <E, C : Collection<E>> CollectionValidator<C>.length(
    size: Int,
    message: MessageProvider1<C, Int> = Message.resource1("kova.collection.length"),
) = constrain(message.id) {
    satisfies(it.input.size == size, message(it, size))
}

fun <E, C : Collection<E>> CollectionValidator<C>.onEach(validator: Validator<E, *>) =
    constrain("kova.collection.onEach") {
        val validationContext = it.validationContext
        val failures = mutableListOf<ValidationResult.Failure>()
        for ((i, element) in it.input.withIndex()) {
            val path = "[$i]<collection element>"
            val result = validator.execute(element, validationContext.appendPath(path))
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
