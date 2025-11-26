package org.komapper.extension.validator

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
fun assertTrue(actual: Boolean) {
    contract { returns() implies actual }
    if (!actual) AssertionError("Expected value to be true.")
}
