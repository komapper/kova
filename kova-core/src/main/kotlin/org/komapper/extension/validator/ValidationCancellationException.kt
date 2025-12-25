package org.komapper.extension.validator

import kotlin.coroutines.cancellation.CancellationException

class ValidationToken {
    fun raise(): Nothing = throw ValidationCancellationException(this)
}

/*
 * Adapted from Arrow:
 * https://github.com/arrow-kt/arrow/blob/073a962e288cc042749dc8fb581455763219ee8c/arrow-libs/core/arrow-core/src/androidAndJvmMain/kotlin/arrow/core/raise/CancellationExceptionNoTrace.kt
 * Originally inspired by KotlinX Coroutines:
 * https://github.com/Kotlin/kotlinx.coroutines/blob/3788889ddfd2bcfedbff1bbca10ee56039e024a2/kotlinx-coroutines-core/jvm/src/Exceptions.kt#L29
 */
class ValidationCancellationException(
    val token: ValidationToken,
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
    block: ValidationToken.() -> R,
): R {
    val token = ValidationToken()
    return try {
        block(token)
    } catch (e: ValidationCancellationException) {
        if (e.token === token) {
            recover()
        } else {
            throw e
        }
    }
}
