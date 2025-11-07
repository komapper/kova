package org.komapper.extension.validator

class GenericValidator<T, S> internal constructor(
    private val transform: (T) -> S,
    private val delegate: CoreValidator<T, S> = CoreValidator(transform = transform),
) : Validator<T, S> by delegate
