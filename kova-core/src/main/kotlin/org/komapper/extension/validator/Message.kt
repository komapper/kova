package org.komapper.extension.validator

import java.text.MessageFormat
import java.util.ResourceBundle

/**
 * Represents an error message for validation failures.
 *
 * Messages can be simple text, resource bundle entries (i18n), or contain nested validation failures
 * from collection/map element validation or the `or` operator.
 *
 * All message types require a [MessageContext] which provides constraint metadata (id, input, args)
 * and validation state (root, path). Messages are typically created internally by the validation framework.
 *
 * There are four message types:
 * - [Text]: Simple hardcoded text messages
 * - [Resource]: I18n messages loaded from `kova.properties` resource bundles
 * - [OnEach]: Composite messages for collection/map element validation failures
 * - [Or]: Composite messages for failures from both branches of an `or` validator
 */
sealed interface Message {
    /** The constraint identifier for this message */
    val id: String

    /** The root object identifier in the validation hierarchy */
    val root: String

    /** The path to the validated value in the object graph */
    val path: Path

    /** The formatted message content */
    val content: String

    /**
     * A simple text message without i18n support.
     *
     * This message type is used for hardcoded error messages or when i18n is not needed.
     * The message content is provided directly as a string rather than loaded from a resource bundle.
     *
     * @property context The message context containing constraint metadata and validation state
     * @property content The formatted message text
     */
    data class Text(
        val context: MessageContext<*>,
        override val content: String,
    ) : Message {
        override val id: String
            get() = context.constraintId

        override val root: String
            get() = context.root

        override val path: Path
            get() = context.path
    }

    /**
     * A message loaded from a resource bundle for i18n support.
     *
     * Messages are loaded from `kova.properties` files using [MessageFormat] for parameter substitution.
     * The constraint ID from the context is used as the resource bundle key, and arguments from the context
     * are substituted into the message pattern. Arguments that are Message instances are resolved to their
     * content strings to support nested messages.
     *
     * Example resource file (kova.properties):
     * ```properties
     * kova.string.min="{0}" must be at least {1} characters
     * kova.number.positive=Number {0} must be positive
     * kova.collection.onEach=Some elements in the collection do not satisfy the constraint: {0}
     * ```
     *
     * @property context The message context containing the constraint ID (used as resource key) and arguments
     */
    data class Resource(
        val context: MessageContext<*>,
    ) : Message {
        override val content: String by lazy {
            val pattern = getPattern(context.constraintId)
            val newArgs = context.args.map { resolveArg(it) }
            MessageFormat.format(pattern, *newArgs.toTypedArray())
        }
        override val id: String
            get() = context.constraintId

        override val root: String
            get() = context.root

        override val path: Path
            get() = context.path

        private fun resolveArg(arg: Any?): Any? =
            when (arg) {
                is Message -> arg.content
                is Iterable<*> -> arg.map { resolveArg(it) }
                else -> arg
            }
    }

    /**
     * A composite message representing validation failures from collection/map element validation.
     *
     * This message type is created when validating elements of a collection or map using `onEach`,
     * `onEachKey`, or `onEachValue` constraints. It aggregates all individual element validation
     * failures into a single message.
     *
     * The content is loaded from a resource bundle using the constraint ID from the context,
     * while the detailed element failures are accessible through the [elements] property.
     *
     * Example:
     * ```kotlin
     * val validator = Kova.collection<String>().onEach(Kova.string().min(3))
     * val result = validator.tryValidate(listOf("ab", "cd", "efg"))
     * // OnEach message with 2 element failures for "ab" at [0] and "cd" at [1]
     * ```
     *
     * @property context The message context containing the constraint ID and validation state
     * @property elements List of validation failures for individual elements that failed validation
     */
    data class OnEach(
        val context: MessageContext<*>,
        val elements: List<ValidationResult.Failure>,
    ) : Message {
        override val content: String get() = Resource(context).content
        override val id: String
            get() = context.constraintId

        override val root: String
            get() = context.root

        override val path: Path
            get() = context.path
    }

    /**
     * A composite message representing a validation failure from the `or` operator.
     *
     * This message type is created when both branches of an `or` validator fail validation.
     * It contains references to the failures from both the first and second validators that were tried.
     *
     * The content is loaded from a resource bundle using the constraint ID from the context
     * (typically "kova.or"), while the detailed branch failures are accessible through the
     * [first] and [second] properties.
     *
     * Example:
     * ```kotlin
     * val validator = Kova.string().max(5) or Kova.string().startsWith("LONG:")
     * val result = validator.tryValidate("medium string")
     * // Or message with failures from both branches
     * ```
     *
     * @property context The message context containing the constraint ID and validation state
     * @property first The validation failure from the first branch of the `or` validator
     * @property second The validation failure from the second branch of the `or` validator
     */
    data class Or(
        val context: MessageContext<*>,
        val first: ValidationResult.Failure,
        val second: ValidationResult.Failure,
    ) : Message {
        override val content: String get() = Resource(context).content
        override val id: String
            get() = context.constraintId

        override val root: String
            get() = context.root

        override val path: Path
            get() = context.path
    }

    companion object : MessageProviderFactory
}

private const val RESOURCE_BUNDLE_BASE_NAME = "kova"

internal fun getPattern(key: String): String {
    val bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_BASE_NAME)
    return bundle.getString(key)
}
