package org.komapper.extension.validator

import org.komapper.extension.validator.CoreValidator.Companion.getPattern
import org.komapper.extension.validator.ValidationResult.Failure
import org.komapper.extension.validator.ValidationResult.Success
import java.text.MessageFormat
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
        val messages: List<String>,
        val cause: Throwable? = null,
    ) {
        val root get() = context.root
        val path get() = context.path

        companion object {
            fun extract(
                context: ValidationContext,
                constraintResult: ConstraintResult,
            ): List<FailureDetail> = extractFailureDetailsFromConstraintResult(context, constraintResult)
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

val <T> ValidationResult<T>.messages: List<String>
    get() =
        if (isSuccess()) emptyList() else details.map { it.messages }.flatten()

private fun extractFailureDetailsFromConstraintResult(
    context: ValidationContext,
    constraintResult: ConstraintResult,
): List<ValidationResult.FailureDetail> =
    when (constraintResult) {
        is ConstraintResult.Satisfied -> emptyList()
        is ConstraintResult.Violated ->
            when (val message = constraintResult.message) {
                is Message.Text -> {
                    listOf(ValidationResult.FailureDetail(context, listOf(message.content)))
                }

                is Message.Resource -> {
                    val pattern = getPattern(message.key)
                    val formatted = MessageFormat.format(pattern, *message.args.toTypedArray())
                    listOf(ValidationResult.FailureDetail(context, listOf(formatted)))
                }

                is Message.ValidationFailure -> {
                    message.details
                }
            }
    }
