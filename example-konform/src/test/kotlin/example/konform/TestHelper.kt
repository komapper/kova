package example.konform

import io.kotest.matchers.types.shouldBeInstanceOf
import org.komapper.extension.validator.Message
import org.komapper.extension.validator.ValidationResult
import org.komapper.extension.validator.ValidationResult.Failure
import org.komapper.extension.validator.ValidationResult.Success
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

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
