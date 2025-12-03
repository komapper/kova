package org.komapper.extension.validator

import java.text.MessageFormat
import java.util.ResourceBundle

/**
 * Represents an error message for validation failures.
 *
 * Messages can be simple text, resource bundle entries (i18n), or contain nested validation failures.
 * Use the companion object factory methods to create message providers for custom validators.
 *
 * Example usage:
 * ```kotlin
 * // Simple text message
 * val textMessage = Message.Text("Value must be positive")
 *
 * // Resource bundle message (i18n)
 * val resourceMessage = Message.Resource("kova.string.min", input, minLength)
 *
 * // Custom message provider
 * val customProvider = Message.text0<String> { context ->
 *     "Invalid value: ${context.input}"
 * }
 * ```
 */
sealed interface Message {
    /** Optional constraint identifier for this message */
    val constraintId: String? get() = null

    /** The formatted message content */
    val content: String

    /**
     * A simple text message without i18n support.
     *
     * Use this for hardcoded error messages or when i18n is not needed.
     *
     * Example:
     * ```kotlin
     * Message.Text("Value must be positive")
     * Message.Text(constraintId = "positive", content = "Value must be positive")
     * ```
     */
    data class Text(
        override val constraintId: String? = null,
        override val content: String,
    ) : Message {
        constructor(content: String) : this(null, content)
    }

    /**
     * A message loaded from a resource bundle for i18n support.
     *
     * Messages are loaded from `kova.properties` files using [MessageFormat] for parameter substitution.
     *
     * Example resource file (kova.properties):
     * ```properties
     * custom.positive=Number {0} must be positive
     * custom.range=Number {0} must be between {1} and {2}
     * ```
     *
     * Example usage:
     * ```kotlin
     * Message.Resource("custom.positive", value)
     * Message.Resource("custom.range", value, min, max)
     * ```
     *
     * @property constraintId The resource bundle key
     * @property args Arguments to substitute into the message pattern
     */
    data class Resource(
        override val constraintId: String,
        val args: List<Any?>,
    ) : Message {
        constructor(key: String, vararg args: Any?) : this(key, args.toList())

        override val content: String by lazy {
            val pattern = getPattern(constraintId)
            val newArgs = args.map { resolveArg(it) }
            MessageFormat.format(pattern, *newArgs.toTypedArray())
        }

        private fun resolveArg(arg: Any?): Any? =
            when (arg) {
                is Message -> arg.content
                is Iterable<*> -> arg.map { resolveArg(it) }
                else -> arg
            }
    }

    /**
     * A message containing nested validation failures.
     *
     * Used internally for composite failures from OR operations.
     */
    data class ValidationFailure(
        override val constraintId: String? = null,
        val details: List<FailureDetail>,
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
