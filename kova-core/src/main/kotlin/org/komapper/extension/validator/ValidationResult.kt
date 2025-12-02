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

    sealed class Failure : ValidationResult<Nothing> {
        abstract val details: List<FailureDetail>

        data class Simple(
            override val details: List<FailureDetail>,
        ) : Failure() {
            constructor(detail: FailureDetail) : this(listOf(detail))
        }

        data class Or(
            val first: Failure,
            val second: Failure,
        ) : Failure() {
            override val details: List<FailureDetail> get() = first.details + second.details
        }
    }

    data class FailureDetail(
        val context: ValidationContext,
        val message: Message,
        val cause: Throwable? = null,
    ) {
        val root get() = context.root
        val path get() = context.path
    }
}

operator fun <T> ValidationResult<T>.plus(other: ValidationResult<T>): ValidationResult<T> =
    when (this) {
        is Success -> other
        is Failure ->
            when (other) {
                is Success -> this
                is Failure -> Failure.Simple(this.details + other.details)
            }
    }

fun <T> ValidationResult<T>.or(other: ValidationResult<T>): ValidationResult<T> =
    when (this) {
        is Success -> other
        is Failure ->
            when (other) {
                is Success -> this
                is Failure -> Failure.Or(this, other)
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
            compositeMessage(this, false)
        }

private fun compositeMessage(
    failure: Failure,
    nested: Boolean,
): List<Message> =
    when (failure) {
        is Failure.Simple -> failure.details.map { it.message }
        is Failure.Or -> {
            val firstMessages = compositeMessage(failure.first, true)
            val secondMessages = compositeMessage(failure.second, true)
            val key = if (nested) "kova.or.nested" else "kova.or"
            listOf(Message.Resource(key, firstMessages, secondMessages))
        }
    }
