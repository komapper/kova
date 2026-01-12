package org.komapper.extension.validator

import org.komapper.extension.validator.Constraint.satisfies
import kotlin.contracts.contract

/**
 * Adds a custom constraint to the input value.
 *
 * This is a fundamental building block for creating custom validation rules.
 * The constraint function is executed within an accumulating context that collects
 * validation errors. If the constraint fails, the error is accumulated and validation
 * continues (unless failFast is enabled).
 *
 * Example:
 * ```kotlin
 * context(_: Validation)
 * fun String.alphanumeric(
 *     message: MessageProvider = { "kova.string.alphanumeric".resource }
 * ) = constrain("kova.string.alphanumeric") {
 *     satisfies(it.all { c -> c.isLetterOrDigit() }, message)
 * }
 *
 * tryValidate { "abc123".alphanumeric() } // Success
 * tryValidate { "abc-123".alphanumeric() } // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver T The value to validate
 * @param T The type of the value being validated
 * @param id Unique identifier for the constraint (used in error messages and logging)
 * @param check Constraint logic that validates the input value; receives the [Validation] context
 *              and executes within a [Constraint] receiver scope
 * @return The validated input value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public inline fun <T> T.constrain(
    id: String,
    check: context(Validation) Constraint.(T) -> Unit,
): T = apply { checkWithAccumulating(id, check) }

@IgnorableReturnValue
@PublishedApi
context(_: Validation)
internal inline fun <T> T.checkWithAccumulating(
    id: String,
    check: context(Validation) Constraint.(T) -> Unit,
): Accumulate.Value<T> =
    accumulating {
        mapEachMessage({ logAndAddDetails(it, this@checkWithAccumulating, id) }) {
            val v = contextOf<Validation>()
            Constraint.check(this)
            log {
                LogEntry.Satisfied(
                    constraintId = id,
                    root = v.root,
                    path = v.path.fullName,
                    input = this,
                )
            }
        }
        this
    }

/**
 * Executes a validation block with a message transformation function applied to all errors.
 *
 * This function wraps the error accumulator so that each validation message accumulated
 * during the block's execution is transformed by the provided function. This is useful
 * for enriching messages with additional context or modifying their content.
 *
 * The transformation is applied to all validation errors that occur within the block,
 * including nested validation errors. This is used internally by the [constrain] function
 * to add constraint details (input value and constraint ID) to error messages.
 *
 * Example internal usage:
 * ```kotlin
 * mapEachMessage({ logAndAddDetails(it, input, "kova.string.min") }) {
 *     // All messages from this block will have details added
 *     satisfies(condition) { "kova.string.min".resource }
 * }
 * ```
 *
 * @param R the type of the validation result
 * @param transform The function to apply to each validation message
 * @param block The validation logic to execute
 * @return The result of executing the validation block
 */
@PublishedApi
context(v: Validation)
internal inline fun <R> mapEachMessage(
    noinline transform: (Message) -> Message,
    block: context(Validation)() -> R,
): R = block(v.copy(acc = { accumulate(it.map(transform)) }))

/**
 * Raises a validation error immediately if the predicate returns true.
 *
 * This function evaluates the predicate against the input value and, if true,
 * immediately raises a validation error (bypassing error accumulation). This is
 * useful for fail-fast scenarios where subsequent validation cannot continue
 * if a certain condition is met (e.g., null checks before accessing properties).
 *
 * The predicate logic is inverted internally: `satisfies(!predicate(it), message)`
 * means the constraint is satisfied when the predicate is false.
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver T The value to validate
 * @param T The type of the value being validated
 * @param constraintId Unique identifier for the constraint (used in error messages and logging)
 * @param message Provider for the error message if the predicate returns true
 * @param predicate Function that returns true when validation should fail
 */
@IgnorableReturnValue
@PublishedApi
context(_: Validation)
internal fun <T> T.raiseIf(
    constraintId: String,
    message: MessageProvider,
    predicate: (T) -> Boolean,
) {
    contract { returns() implies (this@raiseIf != null) }
    val result = checkWithAccumulating(constraintId) { satisfies(!predicate(it), message) }
    when (result) {
        is Accumulate.Ok -> {}
        is Accumulate.Error -> result.raise()
    }
}

/**
 * Transforms a value and raises an error immediately if the transformation fails.
 *
 * This function applies a transformation to the input value. If the transformation
 * returns null, it immediately raises a validation error. This is designed for
 * type-transforming validators (e.g., `String.transformToInt()`) where the original
 * type cannot continue through the validation chain.
 *
 * Example:
 * ```kotlin
 * context(_: Validation)
 * fun String.transformToInt(
 *     message: MessageProvider = { "kova.string.int".resource },
 * ): Int = transformOrRaise("kova.string.int", message) { it.toIntOrNull() }
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver T The value to transform
 * @param T The type of the input value
 * @param R The type of the transformed value
 * @param constraintId Unique identifier for the constraint (used in error messages and logging)
 * @param message Provider for the error message if transformation returns null
 * @param transform Function that transforms the input; returns null on failure
 * @return The transformed non-null value
 */
@IgnorableReturnValue
context(_: Validation)
public fun <T, R> T.transformOrRaise(
    constraintId: String,
    message: MessageProvider,
    transform: (T) -> R?,
): R & Any {
    val transformed = transform(this)
    raiseIf(constraintId, message) { transformed == null }
    return transformed!!
}

/**
 * Represents a validation constraint context that provides methods to evaluate conditions
 * and raise validation errors.
 *
 * This object is typically accessed through the `constrain()` extension function, which
 * creates a constraint context for a given input value and constraint ID. Within this context,
 * you can use the [satisfies] method to define validation rules.
 *
 * Example:
 * ```kotlin
 * context(_: Validation)
 * fun Int.ensurePositive() = constrain("kova.number.ensurePositive") {
 *     satisfies(it > 0) { "kova.number.ensurePositive".resource }
 * }
 * ```
 *
 * @see satisfies
 * @see constrain
 */
public object Constraint {
    /**
     * Evaluates a condition and raises a validation error if it fails.
     *
     * This accepts a [MessageProvider]
     * lambda that is only evaluated when the condition is false, enabling lazy message construction.
     * This is beneficial when message creation involves resource lookups or formatting.
     *
     * This function uses a Kotlin contract to enable smart casting: when it returns normally,
     * the compiler knows the condition was true.
     *
     * Example:
     * ```kotlin
     * context(_: Validation)
     * fun Int.ensurePositive(
     *     message: MessageProvider = { "kova.number.ensurePositive".resource }
     * ) = constrain("kova.number.ensurePositive") {
     *     satisfies(it > 0, message)
     * }
     *
     * tryValidate { 5.ensurePositive() }  // Success (message provider not evaluated)
     * tryValidate { (-1).ensurePositive() } // Failure (message provider evaluated)
     * ```
     *
     * @param Validation (context parameter) The validation context for constraint checking and error accumulation
     * @param condition The condition to evaluate; if false, the message provider is invoked and an error is raised
     * @param message A [MessageProvider] lambda that produces the error message if the condition is false
     * @return Unit. This function returns normally only when the condition is true; otherwise, it raises an error
     */
    context(_: Validation)
    public fun satisfies(
        condition: Boolean,
        message: MessageProvider,
    ) {
        contract { returns() implies condition }
        if (!condition) raise(message())
    }
}
