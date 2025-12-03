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
}

sealed interface FailureDetail {
    val context: ValidationContext
    val message: Message
    val root get() = context.root
    val path get() = context.path

    data class Single(
        override val context: ValidationContext,
        override val message: Message,
        val cause: Throwable? = null,
    ) : FailureDetail

    data class Or(
        override val context: ValidationContext,
        val first: List<FailureDetail>,
        val second: List<FailureDetail>,
    ) : FailureDetail {
        override val message: Message get() {
            val firstMessages = composeMessages(first)
            val secondMessages = composeMessages(second)
            return Message.Resource("kova.or", firstMessages, secondMessages)
        }
    }
}

private fun composeMessages(details: List<FailureDetail>): List<Message> =
    details.map {
        when (it) {
            is FailureDetail.Single -> it.message
            is FailureDetail.Or -> {
                val first = composeMessages(it.first)
                val second = composeMessages(it.second)
                Message.Resource("kova.or.nested", first, second)
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
