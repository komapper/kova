package org.komapper.extension.validator

import org.komapper.extension.validator.ValidationResult.Failure
import org.komapper.extension.validator.ValidationResult.Success
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Result of a validation operation, either [Success] or [Failure].
 *
 * Use [isSuccess] and [isFailure] extension functions with Kotlin contracts
 * for type-safe result handling.
 *
 * Example:
 * ```kotlin
 * val result = validator.tryValidate(input)
 * when (result) {
 *     is ValidationResult.Success -> println("Value: ${result.value}")
 *     is ValidationResult.Failure -> println("Errors: ${result.details}")
 * }
 * ```
 *
 * @param T The type of the validated value on success
 */
sealed interface ValidationResult<out T> {
    /**
     * Represents a successful validation with the validated value.
     *
     * @param value The validated value
     * @param context The validation context after validation completed
     */
    data class Success<T>(
        val value: T,
        val context: ValidationContext,
    ) : ValidationResult<T>

    /**
     * Represents a failed validation with detailed error information.
     *
     * @param details List of failure details describing what went wrong
     */
    data class Failure(
        val details: List<FailureDetail>,
    ) : ValidationResult<Nothing> {
        /**
         * Creates a Failure with a single detail.
         *
         * @param detail The failure detail
         */
        constructor(detail: FailureDetail) : this(listOf(detail))
    }
}

/**
 * Detailed information about a validation failure.
 *
 * Contains the validation context, error message, root object name, and field path
 * where the validation failed.
 *
 * Implementations include simple failures and composite failures (from OR operations).
 */
sealed interface FailureDetail {
    /** The validation context at the point of failure */
    val context: ValidationContext

    /** The error message describing the failure */
    val message: Message

    /** The root object's qualified class name (e.g., "com.example.User") */
    val root get() = context.root

    /** The field path where validation failed, excluding the root (e.g., "name" or "address.city") */
    val path get() = context.path
}

internal data class SimpleFailureDetail(
    override val context: ValidationContext,
    override val message: Message,
) : FailureDetail

internal data class CompositeFailureDetail(
    val input: Any?,
    override val context: ValidationContext,
    val first: List<FailureDetail>,
    val second: List<FailureDetail>,
) : FailureDetail {
    override val message: Message get() {
        val firstMessages = composeMessages(input, context, first)
        val secondMessages = composeMessages(input, context, second)
        // TODO
        val constraintContext = context.createConstraintContext(input).copy(constraintId = "kova.or")
        val messageContext = MessageContext(constraintContext, listOf(firstMessages, secondMessages))
        return Message.Resource(messageContext)
    }
}

private fun composeMessages(
    input: Any?,
    context: ValidationContext,
    details: List<FailureDetail>,
): List<Message> =
    details.map {
        when (it) {
            is SimpleFailureDetail -> it.message
            is CompositeFailureDetail -> {
                val first = composeMessages(input, context, it.first)
                val second = composeMessages(input, context, it.second)
                // TODO
                val constraintContext = context.createConstraintContext(input).copy(constraintId = "kova.or.nested")
                val messageContext = MessageContext(constraintContext, listOf(first, second))
                Message.Resource(messageContext)
            }
        }
    }

/**
 * Combines two validation results.
 *
 * - If this is [Success], returns [other]
 * - If this is [Failure] and [other] is [Success], returns this failure
 * - If both are [Failure], combines their failure details
 *
 * This is used internally by the [and] operator to accumulate failures.
 */
operator fun <T> ValidationResult<T>.plus(other: ValidationResult<T>): ValidationResult<T> =
    when (this) {
        is Success -> other
        is Failure ->
            when (other) {
                is Success -> this
                is Failure -> Failure(this.details + other.details)
            }
    }

/**
 * Type-safe check if this result is a success.
 *
 * Uses Kotlin contracts for smart casting, so after checking `isSuccess()`,
 * the result is automatically cast to [ValidationResult.Success].
 *
 * Example:
 * ```kotlin
 * val result = validator.tryValidate(input)
 * if (result.isSuccess()) {
 *     // result is automatically cast to Success here
 *     println("Value: ${result.value}")
 * }
 * ```
 */
@OptIn(ExperimentalContracts::class)
fun <T> ValidationResult<T>.isSuccess(): Boolean {
    contract {
        returns(true) implies (this@isSuccess is ValidationResult.Success)
        returns(false) implies (this@isSuccess is ValidationResult.Failure)
    }
    return this is ValidationResult.Success
}

/**
 * Type-safe check if this result is a failure.
 *
 * Uses Kotlin contracts for smart casting, so after checking `isFailure()`,
 * the result is automatically cast to [ValidationResult.Failure].
 *
 * Example:
 * ```kotlin
 * val result = validator.tryValidate(input)
 * if (result.isFailure()) {
 *     // result is automatically cast to Failure here
 *     println("Errors: ${result.details}")
 * }
 * ```
 */
@OptIn(ExperimentalContracts::class)
fun <T> ValidationResult<T>.isFailure(): Boolean {
    contract {
        returns(true) implies (this@isFailure is ValidationResult.Failure)
        returns(false) implies (this@isFailure is ValidationResult.Success)
    }
    return this is ValidationResult.Failure
}

/**
 * Extracts all error messages from this validation result.
 *
 * Returns an empty list for successful results, or a list of all error messages
 * for failed results.
 *
 * Example:
 * ```kotlin
 * val result = validator.tryValidate(input)
 * result.messages.forEach { message ->
 *     println(message.content)
 * }
 * ```
 */
val ValidationResult<*>.messages: List<Message>
    get() =
        if (isSuccess()) {
            emptyList()
        } else {
            details.map { it.message }
        }
