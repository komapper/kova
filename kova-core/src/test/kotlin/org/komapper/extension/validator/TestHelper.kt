package org.komapper.extension.validator

import io.kotest.matchers.types.shouldBeInstanceOf
import org.komapper.extension.validator.ValidationResult.Failure
import org.komapper.extension.validator.ValidationResult.Success
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import io.kotest.matchers.shouldBe as kotestShouldBe

@IgnorableReturnValue
@OptIn(ExperimentalContracts::class)
fun <T> ValidationResult<T>.shouldBeSuccess(): T {
    contract { returns() implies (this@shouldBeSuccess is Success<T>) }
    return shouldBeInstanceOf<Success<T>>().value
}

@IgnorableReturnValue
@OptIn(ExperimentalContracts::class)
fun ValidationResult<*>.shouldBeFailure(): List<Message> {
    contract { returns() implies (this@shouldBeFailure is Failure) }
    return shouldBeInstanceOf<Failure>().messages
}

val Message.args get() = shouldBeInstanceOf<Message.Resource>().args

/** Kotest's shouldBe, but annotated with @OnlyInputTypes. */
@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@IgnorableReturnValue
infix fun <@kotlin.internal.OnlyInputTypes T> T.shouldBe(expected: T?): T = kotestShouldBe(expected)
