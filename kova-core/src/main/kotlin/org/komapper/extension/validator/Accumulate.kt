package org.komapper.extension.validator

import kotlin.coroutines.cancellation.CancellationException
import kotlin.reflect.KProperty

/**
 * A functional interface for accumulating validation errors.
 *
 * This interface defines the strategy for handling validation errors when multiple
 * constraints fail. Implementations determine whether to fail fast or collect all errors.
 */
public fun interface Accumulate {
    /**
     * Represents the result of an accumulating validation operation.
     *
     * @param T the type of the validated value
     */
    public sealed class Value<out T> {
        /**
         * The validated value.
         *
         * @throws ValidationCancellationException if this is an [Error]
         */
        abstract val value: T

        /**
         * Enables property delegation syntax for extracting validated values.
         *
         * @param instance the instance owning the delegated property
         * @param property the delegated property metadata
         * @return the validated value
         */
        public operator fun getValue(
            instance: Any?,
            property: KProperty<*>,
        ): T = value
    }

    /**
     * Represents a successful validation result with a valid value.
     *
     * @param T the type of the validated value
     * @property value the successfully validated value
     */
    public class Ok<T>(
        override val value: T,
    ) : Value<T>()

    /**
     * Represents a failed validation result that accumulates error messages.
     *
     * This class uses a cancellation-based control flow mechanism to short-circuit
     * validation when errors are accumulated. The constructor is internal to prevent
     * direct instantiation outside the validation framework.
     */
    public class Error
        @PublishedApi
        internal constructor() : Value<Nothing>() {
            /**
             * Always raises a [ValidationCancellationException] when accessed.
             *
             * @throws ValidationCancellationException always
             */
            override val value: Nothing
                get() = raise()

            /**
             * Raises a [ValidationCancellationException] to signal validation failure.
             *
             * @throws ValidationCancellationException always
             */
            public fun raise(): Nothing = throw ValidationCancellationException(this)
        }

    /**
     * Accumulates validation error messages.
     *
     * @param messages the validation error messages to accumulate
     * @return an [Error] instance representing the accumulated errors
     */
    @IgnorableReturnValue
    fun accumulate(messages: List<Message>): Error
}

/**
 * Accumulates validation error messages in the current validation context.
 *
 * @param messages the validation error messages to accumulate
 * @return an [Accumulate.Error] instance representing the accumulated errors
 */
@IgnorableReturnValue
context(v: Validation)
public fun accumulate(messages: List<Message>): Accumulate.Error = v.acc.accumulate(messages)

/**
 * Accumulates validation error messages and immediately raises a validation failure.
 *
 * This function combines error accumulation with immediate failure signaling through
 * a [ValidationCancellationException].
 *
 * @param messages the validation error messages to accumulate and raise
 * @throws ValidationCancellationException always
 */
context(_: Validation)
public fun raise(messages: List<Message>): Nothing = accumulate(messages).raise()

/**
 * Accumulates a single validation error message and immediately raises a validation failure.
 *
 * @param message the validation error message to accumulate and raise
 * @throws ValidationCancellationException always
 */
context(_: Validation)
public fun raise(message: Message): Nothing = raise(listOf(message))

/**
 * Executes a validation block in accumulating mode, capturing all errors.
 *
 * This function creates a validation context that accumulates errors instead of
 * failing fast. Validation errors raised within the block are collected and returned
 * as an [Accumulate.Error], or as [Accumulate.Ok] if validation succeeds.
 *
 * @param R the type of the validation result
 * @param block the validation logic to execute
 * @return [Accumulate.Ok] with the result if validation succeeds, or [Accumulate.Error]
 *         if validation fails with accumulated error messages
 */
@IgnorableReturnValue
context(v: Validation)
public inline fun <R> accumulating(block: context(Validation)() -> R): Accumulate.Value<R> {
    lateinit var outsideError: Accumulate.Error
    // raise/error is only used after outsideError is initialized
    return recoverValidation({ outsideError }) {
        block(
            v.copy(acc = {
                outsideError = accumulate(it)
                this
            }),
        ).let(Accumulate::Ok)
    }
}

/**
 * A specialized [CancellationException] used for control flow in validation error handling.
 *
 * This exception is used internally to short-circuit validation when errors are accumulated.
 * It does not fill in stack traces for performance reasons, as validation failures are
 * expected control flow rather than exceptional conditions.
 *
 * Adapted from Arrow:
 * https://github.com/arrow-kt/arrow/blob/073a962e288cc042749dc8fb581455763219ee8c/arrow-libs/core/arrow-core/src/androidAndJvmMain/kotlin/arrow/core/raise/CancellationExceptionNoTrace.kt
 * Originally inspired by KotlinX Coroutines:
 * https://github.com/Kotlin/kotlinx.coroutines/blob/3788889ddfd2bcfedbff1bbca10ee56039e024a2/kotlinx-coroutines-core/jvm/src/Exceptions.kt#L29
 *
 * @property error the accumulated validation errors
 */
public class ValidationCancellationException(
    val error: Accumulate.Error,
) : CancellationException() {
    /**
     * Overridden to prevent stack trace generation for performance optimization.
     *
     * Stack traces are not needed for validation failures as they represent expected
     * control flow rather than exceptional conditions. This also prevents bugs on
     * Android versions 6.0 and below.
     *
     * @return this exception with an ensureEmpty stack trace
     */
    override fun fillInStackTrace(): Throwable {
        // Prevent Android <= 6.0 bug.
        stackTrace = emptyArray()
        // We don't need stacktrace on validation failure, it hurts performance.
        return this
    }
}

/**
 * Executes a validation block with structured error recovery.
 *
 * This function creates an [Accumulate.Error] context and executes the validation block.
 * If the block raises a [ValidationCancellationException] that ensureMatches the created error
 * context, the recovery function is invoked. If the exception belongs to a different
 * error context, it is re-thrown to propagate to the appropriate handler.
 *
 * @param R the type of the result
 * @param recover the recovery function to invoke when validation fails in this context
 * @param block the validation logic to execute
 * @return the result from the block if successful, or the result from recovery if validation fails
 * @throws ValidationCancellationException if the exception belongs to a different error context
 */
public inline fun <R> recoverValidation(
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
