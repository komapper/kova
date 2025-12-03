package org.komapper.extension.validator

import java.text.MessageFormat
import java.util.ResourceBundle

sealed interface Message {
    val constraintId: String? get() = null
    val content: String

    data class Text(
        override val constraintId: String? = null,
        override val content: String,
    ) : Message {
        constructor(content: String) : this(null, content)
    }

    data class Resource(
        override val constraintId: String,
        val args: List<Any?>,
    ) : Message {
        constructor(key: String, vararg args: Any?) : this(key, args.toList())

        override val content: String by lazy {
            val pattern = getPattern(constraintId)
            val newArgus = args.map { resolveArg(it) }
            MessageFormat.format(pattern, *newArgus.toTypedArray())
        }

        private fun resolveArg(arg: Any?): Any? =
            when (arg) {
                is Message -> arg.content
                is Iterable<*> -> arg.map { resolveArg(it) }
                else -> arg
            }
    }

    data class ValidationFailure(
        override val constraintId: String? = null,
        val details: List<ValidationResult.FailureDetail>,
    ) : Message {
        override val content: String get() = details.toString()
    }

    companion object : MessageProvider0Factory, MessageProvider1Factory, MessageProvider2Factory
}

private const val RESOURCE_BUNDLE_BASE_NAME = "kova"

internal fun getPattern(key: String): String {
    val bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_BASE_NAME)
    return bundle.getString(key)
}
