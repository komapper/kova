package org.komapper.extension.validator.factory

import io.kotest.matchers.types.shouldBeInstanceOf
import org.komapper.extension.validator.Message
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
fun Boolean.mustBeTrue(message: Any? = null) {
    contract { returns() implies this@mustBeTrue }
    if (!this) throw AssertionError(message ?: "Expected value to be true.")
}

val Message.constraintId get() = shouldBeInstanceOf<Message.Resource>().constraintId