package org.komapper.extension.validator

import java.text.MessageFormat
import java.util.ResourceBundle

/**
 * A lazy provider for validation error messages.
 *
 * This is a function type that returns a [Message] when invoked. MessageProviders are used
 * throughout the validation framework to defer message creation until a validation constraint
 * actually fails, improving performance by avoiding unnecessary message construction.
 *
 * MessageProviders are typically used with the `satisfies` function in custom validators:
 * ```kotlin
 * context(_: Validation)
 * fun String.alphanumeric() = apply {
 *     constrain("custom.alphanumeric") {
 *         satisfies(it.all { c -> c.isLetterOrDigit() }) {
 *             "kova.string.alphanumeric".resource  // This lambda is a MessageProvider
 *         }
 *     }
 * }
 * ```
 */
public typealias MessageProvider = () -> Message

/**
 * Represents an error message for validation failures.
 *
 * Messages can be simple text, resource bundle entries (i18n), or contain nested validation failures
 * from collection/map element validation or the `or` operator.
 *
 * All message types include constraint metadata (constraintId), validation state (root, path),
 * and the input value being validated. Messages are typically created internally by the validation framework.
 *
 * There are two message types:
 * - [Text]: Simple hardcoded text messages
 * - [Resource]: I18n messages loaded from `kova.properties` resource bundles
 */
public sealed interface Message {
    public fun withDetails(
        input: Any?,
        constraintId: String,
    ): Message

    /** The formatted message text */
    public val text: String

    /** The constraint identifier for this message */
    public val constraintId: String

    /** The root object identifier in the validation hierarchy */
    public val root: String

    /** The path to the validated value in the object graph */
    public val path: Path

    /** The input value being validated */
    public val input: Any?

    /** Arguments for formatting the message text using [MessageFormat] */
    public val args: List<Any?>

    /** Nested validation error messages extracted from [args] */
    public val descendants: List<Message>

    /**
     * A simple text message without i18n support.
     *
     * This message type is used for hardcoded error messages or when i18n is not needed.
     * The message text is provided directly as a string rather than loaded from a resource bundle.
     *
     * @property constraintId The constraint identifier for this message
     * @property root The root object identifier in the validation hierarchy
     * @property path The path to the validated value in the object graph
     * @property text The formatted message text
     * @property input The input value being validated
     */
    public class Text internal constructor(
        override val constraintId: String,
        override val root: String,
        override val path: Path,
        override val text: String,
        override val input: Any?,
    ) : Message {
        override val args: List<Any?> = emptyList()

        override val descendants: List<Message> = emptyList()

        override fun toString(): String =
            "Message(constraintId=$constraintId, text='$text', root=$root, path=${path.fullName}, input=$input)"

        override fun withDetails(
            input: Any?,
            constraintId: String,
        ): Text = Text(constraintId = constraintId, root = root, path = path, text = text, input = input)
    }

    /**
     * A message loaded from a resource bundle for i18n support.
     *
     * Messages are loaded from `kova.properties` files using [MessageFormat] for parameter substitution.
     * The constraint ID is used as the resource bundle key, and arguments are substituted into the message
     * pattern. Arguments that are Message instances are resolved to their text strings to support nested messages.
     *
     * Example resource file (kova.properties):
     * ```properties
     * kova.string.min="{0}" must be at least {1} characters
     * kova.number.ensurePositive=Number {0} must be ensurePositive
     * kova.collection.ensureEach=Some elements do not satisfy the constraint: {0}
     * ```
     *
     * @property constraintId The constraint identifier (used as resource bundle key)
     * @property root The root object identifier in the validation hierarchy
     * @property path The path to the validated value in the object graph
     * @property input The input value being validated
     * @property args Arguments for formatting the resource message using [MessageFormat]
     */
    public class Resource internal constructor(
        private val key: String,
        override val constraintId: String,
        override val root: String,
        override val path: Path,
        override val input: Any?,
        override val args: List<Any?>,
    ) : Message {
        override val text: String by lazy {
            val pattern = getPattern(key)
            val formatArgs = args.map { arg -> resolveMessage(arg, { it.text }) }
            MessageFormat.format(pattern, *formatArgs.toTypedArray())
        }

        override val descendants: List<Message> by lazy {
            buildList {
                args.forEach { arg -> resolveMessage(arg, { add(it) }) }
            }
        }

        @IgnorableReturnValue
        private fun <T> resolveMessage(
            arg: Any?,
            resolver: (Message) -> T,
        ): Any? =
            when (arg) {
                is ClosedRange<*>, is OpenEndRange<*> -> arg
                is Message -> resolver(arg)
                is Iterable<*> -> arg.map { resolveMessage(it, resolver) }
                else -> arg
            }

        override fun toString(): String =
            "Message(constraintId=$constraintId, text='$text', root=$root, path=${path.fullName}, input=$input, args=$args)"

        override fun withDetails(
            input: Any?,
            constraintId: String,
        ): Message =
            Resource(
                key = key,
                constraintId = constraintId,
                root = root,
                path = path,
                input = input,
                args = args,
            )
    }
}

private const val RESOURCE_BUNDLE_BASE_NAME = "kova"

internal fun getPattern(key: String): String {
    val bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_BASE_NAME)
    return bundle.getString(key)
}
