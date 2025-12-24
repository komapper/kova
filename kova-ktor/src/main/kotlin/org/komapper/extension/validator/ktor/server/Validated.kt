package org.komapper.extension.validator.ktor.server

import org.komapper.extension.validator.ValidationContext
import org.komapper.extension.validator.ValidationResult

interface Validated {
    context(_: ValidationContext)
    fun validate(): ValidationResult<Unit>
}
