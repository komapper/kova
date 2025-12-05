package org.komapper.extension.validator

import org.komapper.extension.validator.ValidationResult.Success

fun <T> Validator<T, T>.onlyIf(condition: (T) -> Boolean) =
    Validator<T, T> { input, context ->
        if (condition(input)) {
            execute(input, context)
        } else {
            Success(input, context)
        }
    }
