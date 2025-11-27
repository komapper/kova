package org.komapper.extension.validator

import java.text.MessageFormat
import java.util.ResourceBundle

sealed interface Message {
    val key: String? get() = null
    val content: String

    data class Text(
        override val key: String? = null,
        override val content: String,
    ) : Message {
        constructor(content: String) : this(null, content)
    }

    data class Resource(
        override val key: String,
        val args: List<Any?>,
    ) : Message {
        constructor(key: String, vararg args: Any?) : this(key, args.toList())

        override val content: String by lazy {
            val pattern = getPattern(key)
            MessageFormat.format(pattern, *args.toTypedArray())
        }
    }

    data class ValidationFailure(
        override val key: String? = null,
        val details: List<ValidationResult.FailureDetail>,
    ) : Message {
        override val content: String get() = details.toString()
    }

    companion object {
        fun <T, A0 : ConstraintContext<T>> resource0(): (A0) -> Message =
            { ctx ->
                Resource(ctx.key, ctx.input)
            }

        fun <T, A0 : ConstraintContext<T>, A1> resource1(): (A0, A1) -> Message =
            { ctx, arg1 ->
                Resource(ctx.key, ctx.input, arg1)
            }

        fun <T, A0 : ConstraintContext<T>, A1, A2> resource2(): (A0, A1, A2) -> Message =
            { ctx, arg1, arg2 ->
                Resource(ctx.key, ctx.input, arg1, arg2)
            }

        fun <T, A0 : ConstraintContext<T>, A1, A2, A3> resource3(): (A0, A1, A2, A3) -> Message =
            { ctx, arg1, arg2, arg3 ->
                Resource(ctx.key, ctx.input, arg1, arg2, arg3)
            }
    }
}

private const val RESOURCE_BUNDLE_BASE_NAME = "kova"

internal fun getPattern(key: String): String {
    val bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_BASE_NAME)
    return bundle.getString(key)
}
