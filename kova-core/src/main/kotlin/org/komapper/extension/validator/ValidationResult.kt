package org.komapper.extension.validator

import org.komapper.extension.validator.ValidationResult.Failure
import org.komapper.extension.validator.ValidationResult.Success
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

sealed interface ValidationResult<out T> {
    data class Success<T>(
        val value: T,
        val context: ValidationContext,
    ) : ValidationResult<T>

    data class Failure(
        val details: List<FailureDetail>,
    ) : ValidationResult<Nothing> {
        constructor(detail: FailureDetail) : this(listOf(detail))
    }

    data class FailureDetail(
        val context: ValidationContext,
        val message: Message,
        val cause: Throwable? = null,
    ) {
        val root get() = context.root
        val path get() = context.path

        companion object {
            fun extract(
                context: ValidationContext,
                constraintResult: ConstraintResult.Violated,
            ): List<FailureDetail> = resolveMessage(context, constraintResult.message)

            fun resolveMessage(
                context: ValidationContext,
                message: Message,
                cause: Throwable? = null,
            ): List<FailureDetail> =
                when (message) {
                    is Message.Text -> listOf(FailureDetail(context, message, cause))
                    is Message.Resource -> listOf(FailureDetail(context, message, cause))
                    is Message.ValidationFailure -> message.details.flatMap { resolveMessage(it.context, it.message, it.cause) }
                }
        }
    }
}

operator fun <T> ValidationResult<T>.plus(other: ValidationResult<T>): ValidationResult<T> =
    when (this) {
        is Success -> other
        is Failure ->
            when (other) {
                is Success -> this
                is Failure -> Failure(this.details + other.details)
            }
    }

@OptIn(ExperimentalContracts::class)
fun <T> ValidationResult<T>.isSuccess(): Boolean {
    contract {
        returns(true) implies (this@isSuccess is ValidationResult.Success)
        returns(false) implies (this@isSuccess is ValidationResult.Failure)
    }
    return this is ValidationResult.Success
}

@OptIn(ExperimentalContracts::class)
fun <T> ValidationResult<T>.isFailure(): Boolean {
    contract {
        returns(true) implies (this@isFailure is ValidationResult.Failure)
        returns(false) implies (this@isFailure is ValidationResult.Success)
    }
    return this is ValidationResult.Failure
}

val ValidationResult<*>.messages: List<Message>
    get() =
        if (isSuccess()) {
            emptyList()
        } else {
            details.map { it.message }
        }
