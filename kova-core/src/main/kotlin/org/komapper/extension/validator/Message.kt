package org.komapper.extension.validator

import org.komapper.extension.validator.ValidationIor.FailureLike
import org.komapper.extension.validator.ValidationResult.Success
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
    /**
     * Creates a new message with updated input value and constraint ID.
     *
     * This method is used internally by the validation framework to attach
     * contextual information to messages after they are created.
     *
     * @param input The input value being validated
     * @param constraintId The constraint identifier for this message
     * @return A new [Message] instance with the updated details
     */
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

        override fun toString(): String = "Message(constraintId=$constraintId, text='$text', root=$root, path=$path, input=$input)"

        /**
         * Creates a new [Text] message with updated input value and constraint ID.
         *
         * @param input The input value being validated
         * @param constraintId The constraint identifier for this message
         * @return A new [Text] instance with the updated details
         */
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
            "Message(constraintId=$constraintId, text='$text', root=$root, path=$path, input=$input, args=$args)"

        /**
         * Creates a new [Resource] message with updated input value and constraint ID.
         *
         * @param input The input value being validated
         * @param constraintId The constraint identifier for this message
         * @return A new [Resource] instance with the updated details
         */
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

/**
 * Creates a text-based validation message with plain text content.
 *
 * Use this method to create ad-hoc validation messages instead of using i18n resource keys.
 * When a validation error occurs, the enclosing `constrain()` call will automatically populate
 * the constraint ID and input value in the message.
 *
 * Example usage in a constraint:
 * ```kotlin
 * tryValidate {
 *     10.constrain("ensurePositive") {
 *         satisfies(it > 0) { text("Value must be ensurePositive") }
 *     }
 * }
 * ```
 *
 * Example with schema validation:
 * ```kotlin
 * data class Period(val startDate: LocalDate, val endDate: LocalDate)
 *
 * context(_: Validation)
 * fun validate(period: Period) = period.schema {
 *     period::startDate { it.ensureInPastOrPresent() }
 *     period::endDate { it.ensureInFutureOrPresent() }
 *     period.constrain("period") {
 *         satisfies(it.startDate <= it.endDate) {
 *             text("Start date must be before or equal to end date")
 *         }
 *     }
 * }
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param content The text content of the error message
 * @return A [Message.Text] instance with the given content
 */
context(v: Validation)
public fun text(content: String): Message = Message.Text("", v.root, v.path, content, null)

/**
 * Creates a resource-based validation message for internationalization.
 *
 * Use this method to create internationalized messages that load text from `kova.properties`.
 * The message key is the receiver string (typically a constraint ID like "kova.number.min").
 * Arguments are provided as a vararg and used for MessageFormat substitution
 * (i.e., the first argument becomes {0}, second becomes {1}, etc.).
 *
 * When a validation error occurs, the enclosing `constrain()` call will automatically populate
 * the constraint ID and input value in the message.
 *
 * Example usage in a constraint:
 * ```kotlin
 * context(_: Validation)
 * fun Int.min(
 *     value: Int,
 *     message: MessageProvider = { "kova.number.min".resource(value) }
 * ) = apply {
 *     constrain("kova.number.min") {
 *         satisfies(it >= value, message)
 *     }
 * }
 *
 * tryValidate { 5.min(0) } // Success
 * ```
 *
 * The corresponding entry in `kova.properties` would be:
 * ```
 * kova.number.min=The value must be greater than or equal to {0}.
 * ```
 *
 * For multiple arguments:
 * ```kotlin
 * context(_: Validation)
 * fun Int.range(
 *     min: Int,
 *     max: Int,
 *     message: MessageProvider = { "kova.number.range".resource(min, max) }
 * ) = apply {
 *     constrain("kova.number.range") {
 *         satisfies(it in min..max, message)
 *     }
 * }
 * ```
 *
 * With corresponding resource:
 * ```
 * kova.number.range=The value must be between {0} and {1}.
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver String The resource key (typically a constraint ID like "kova.number.min")
 * @param args Arguments to be interpolated into the message template using MessageFormat
 * @return A [Message.Resource] instance configured with the provided arguments
 */
context(v: Validation)
public fun String.resource(vararg args: Any?): Message.Resource = Message.Resource(this, this, v.root, v.path, null, args = args.toList())

/**
 * Creates a resource-based validation message without arguments.
 *
 * This is a convenience property that calls [resource] with no arguments.
 * Use this for simple messages that don't require parameter interpolation.
 *
 * Example:
 * ```kotlin
 * satisfies(condition) { "kova.string.notBlank".resource }
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver String The resource key (typically a constraint ID like "kova.string.notBlank")
 * @return A [Message.Resource] instance with no interpolation arguments
 */
context(_: Validation)
public val String.resource: Message.Resource get() = resource()

/**
 * Executes a validation block and wraps any errors in a custom message.
 *
 * If the validation block fails, all accumulated error messages are transformed
 * into a single message using the provided transform function. If validation
 * succeeds, the result is returned directly.
 *
 * This is useful for providing context-specific error messages that wrap
 * detailed validation failures.
 *
 * Example:
 * ```kotlin
 * withMessage({ messages -> "Address validation failed".resource(messages) }) {
 *     // Validate address fields
 * }
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param R The type of the validation result
 * @param transform Function to transform the list of error messages into a single message
 * @param block The validation logic to execute
 * @return The validated result
 */
context(_: Validation)
public inline fun <R> withMessage(
    noinline transform: (List<Message>) -> Message = { "kova.withMessage".resource(it) },
    block: context(Validation)() -> R,
): R =
    when (val result = ior(block)) {
        is Success -> result.value
        is FailureLike -> result.withMessage(transform(result.messages)).bind()
    }

/**
 * Executes a validation block and wraps any errors in a text message.
 *
 * This is a convenience overload of [withMessage] that creates a simple text
 * message instead of requiring a transform function.
 *
 * Example:
 * ```kotlin
 * withMessage("Address validation failed") {
 *     // Validate address fields
 * }
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param R The type of the validation result
 * @param message The error message text to use if validation fails
 * @param block The validation logic to execute
 * @return The validated result
 */
context(_: Validation)
public inline fun <R> withMessage(
    message: String,
    block: context(Validation)() -> R,
): R = withMessage({ text(message) }, block)
