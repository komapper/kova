package org.komapper.extension.validator.ktor.server

import org.komapper.extension.validator.Validation
import org.komapper.extension.validator.ValidationResult

interface Validated {
    context(_: Validation)
    fun validate(): ValidationResult<Unit>
}
