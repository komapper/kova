package org.komapper.extension.validator

import kotlin.coroutines.cancellation.CancellationException
import kotlin.reflect.KProperty

fun interface Accumulate {
    sealed class Value<out T> {
        abstract val value: T

        operator fun getValue(
            instance: Any?,
            property: KProperty<*>,
        ): T = value
    }

    class Ok<T>(
        override val value: T,
    ) : Value<T>()

    class Error
        @PublishedApi
        internal constructor() : Value<Nothing>() {
            override val value: Nothing
                get() = raise()

            fun raise(): Nothing = throw ValidationCancellationException(this)
        }

    @IgnorableReturnValue
    fun accumulate(messages: List<Message>): Error
}

@IgnorableReturnValue
fun Validation.accumulate(messages: List<Message>) = acc.accumulate(messages)

fun Validation.raise(messages: List<Message>): Nothing = accumulate(messages).raise()

fun Validation.raise(message: Message): Nothing = raise(listOf(message))

@IgnorableReturnValue
inline fun <R> Validation.accumulating(block: Validation.() -> R): Accumulate.Value<R> {
    lateinit var outsideError: Accumulate.Error
    // raise/error is only used after outsideError is initialized
    return recoverValidation({ outsideError }) {
        block(
            copy(acc = {
                outsideError = accumulate(it)
                this
            }),
        ).let(Accumulate::Ok)
    }
}

/*
 * Adapted from Arrow:
 * https://github.com/arrow-kt/arrow/blob/073a962e288cc042749dc8fb581455763219ee8c/arrow-libs/core/arrow-core/src/androidAndJvmMain/kotlin/arrow/core/raise/CancellationExceptionNoTrace.kt
 * Originally inspired by KotlinX Coroutines:
 * https://github.com/Kotlin/kotlinx.coroutines/blob/3788889ddfd2bcfedbff1bbca10ee56039e024a2/kotlinx-coroutines-core/jvm/src/Exceptions.kt#L29
 */
class ValidationCancellationException(
    val error: Accumulate.Error,
) : CancellationException() {
    override fun fillInStackTrace(): Throwable {
        // Prevent Android <= 6.0 bug.
        stackTrace = emptyArray()
        // We don't need stacktrace on validation failure, it hurts performance.
        return this
    }
}

inline fun <R> recoverValidation(
    recover: () -> R,
    block: Accumulate.Error.() -> R,
): R =
    with(Accumulate.Error()) {
        try {
            block()
        } catch (e: ValidationCancellationException) {
            if (e.error !== this) throw e
            recover()
        }
    }
