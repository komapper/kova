package org.komapper.extension.validator

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
fun Boolean.mustBeTrue() {
    contract { returns() implies this@mustBeTrue }
    if (!this) AssertionError("Expected value to be true.")
}
