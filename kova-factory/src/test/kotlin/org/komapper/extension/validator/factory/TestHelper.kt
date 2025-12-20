package org.komapper.extension.validator.factory

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
fun Boolean.mustBeTrue(message: Any? = null) {
    contract { returns() implies this@mustBeTrue }
    if (!this) throw AssertionError(message ?: "Expected value to be true.")
}
