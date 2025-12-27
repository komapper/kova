package org.komapper.extension.validator.ktor.server

import org.komapper.extension.validator.Validation

interface Validated {
    fun Validation.validate()
}
