package org.komapper.extension.validator.ktor.server

import org.komapper.extension.validator.Accumulate
import org.komapper.extension.validator.Validation

interface Validated {
    context(_: Validation, _: Accumulate)
    fun validate()
}
