package org.komapper.extension.validator

/**
 * Provides error messages for constraint violations.
 *
 * MessageProvider is a functional interface that creates [Message] objects when
 * constraints are violated. It receives the constraint context and optional arguments
 * to generate contextual error messages.
 *
 * Message providers are typically created using the [MessageProviderFactory] methods
 * available on the [Message] companion object.
 *
 * Example usage:
 * ```kotlin
 * // Using default resource-based message provider
 * fun StringValidator.min(
 *     length: Int,
 *     message: MessageProvider = Message.resource()
 * ) = constrain("kova.string.min") {
 *     satisfies(it.input.length >= length, message(it, it.input, length))
 * }
 *
 * // Using custom text message provider
 * fun StringValidator.customMin(
 *     length: Int,
 *     message: MessageProvider = Message.text { ctx ->
 *         "String '${ctx.input}' is too short. Minimum length is ${ctx[0]}"
 *     }
 * ) = constrain("custom.min") {
 *     satisfies(it.input.length >= length, message(it, length))
 * }
 * ```
 *
 */
interface MessageProvider {
    /**
     * Creates a message for a constraint violation.
     *
     * This method is called by constraint validators when a constraint is violated.
     * It receives the constraint context and any additional arguments needed for
     * message formatting.
     *
     * @param constraintContext The context containing the input value and validation state
     * @param args Additional arguments for message formatting (e.g., constraint parameters)
     * @return A Message object representing the error
     */
    operator fun invoke(vararg args: Any?): (ConstraintContext<*>) -> Message
}

/**
 * Factory for creating [MessageProvider] instances.
 *
 * This interface is implemented by the [Message] companion object, providing
 * convenient factory methods for creating message providers.
 *
 * Example usage:
 * ```kotlin
 * // Create a text message provider with custom logic
 * val customProvider = Message.text { ctx ->
 *     "Value ${ctx.input} failed validation at path ${ctx.path.fullName}"
 * }
 *
 * // Create a resource bundle message provider
 * val resourceProvider = Message.resource()
 * ```
 */
interface MessageProviderFactory {
    /**
     * Creates a message provider that generates text messages.
     *
     * Text messages are created dynamically using the provided lambda function,
     * which receives a [MessageContext] with access to the input value, arguments,
     * and validation state.
     *
     * Example:
     * ```kotlin
     * val provider = Message.text { ctx ->
     *     "Value ${ctx.input} must be at least ${ctx[0]}"
     * }
     * ```
     *
     * @param format Lambda that formats the message text from the context
     * @return A MessageProvider that creates Text messages
     */
    fun text(format: (MessageContext<*>) -> String): MessageProvider =
        object : MessageProvider {
            override fun invoke(vararg args: Any?): (ConstraintContext<*>) -> Message =
                {
                    val messageContext = it.createMessageContext(args.toList())
                    Message.Text(messageContext, format(messageContext))
                }
        }

    /**
     * Creates a message provider that loads messages from resource bundles.
     *
     * Resource messages are loaded from `kova.properties` files using the constraint ID
     * as the resource key. Arguments are formatted using [java.text.MessageFormat].
     *
     * Example resource file (kova.properties):
     * ```properties
     * kova.string.min=String {0} must have at least {1} characters
     * kova.int.range=Value {0} must be between {1} and {2}
     * ```
     *
     * Example usage:
     * ```kotlin
     * // The resource provider uses the constraint ID from the context
     * fun StringValidator.min(
     *     length: Int,
     *     message: MessageProvider = Message.resource()
     * ) = constrain("kova.string.min") { ctx ->
     *     satisfies(ctx.input.length >= length, message(ctx, ctx.input, length))
     * }
     * ```
     *
     * @return A MessageProvider that creates Resource messages
     */
    fun resource(): MessageProvider =
        object : MessageProvider {
            override fun invoke(vararg args: Any?): (ConstraintContext<*>) -> Message =
                {
                    it
                    val messageContext = it.createMessageContext(args.toList())
                    Message.Resource(messageContext)
                }
        }
}

/**
 * Context information available when generating error messages.
 *
 * MessageContext provides access to the input value, arguments, constraint ID,
 * and validation state. It is passed to message provider lambdas to enable
 * contextual error message generation.
 *
 * Example usage:
 * ```kotlin
 * val provider = Message.text { ctx ->
 *     // Access input value
 *     val value = ctx.input
 *
 *     // Access arguments by index
 *     val minLength = ctx[0]
 *
 *     // Access validation path
 *     val path = ctx.path.fullName
 *
 *     "Value '$value' at path '$path' must be at least $minLength characters"
 * }
 * ```
 *
 * @param T The type of the input value being validated
 * @property args Arguments passed to the message provider (e.g., constraint parameters)
 * @property constraintContext The underlying constraint context
 */
data class MessageContext<T>(
    val args: List<Any?> = emptyList(),
    private val constraintContext: ConstraintContext<T>,
) {
    /** The input value being validated */
    val input: T get() = constraintContext.input

    /** The constraint identifier (e.g., "kova.string.min") */
    val constraintId: String get() = constraintContext.constraintId

    /** The root object's qualified class name */
    val root: String get() = constraintContext.validationContext.root

    /** The current validation path */
    val path: Path get() = constraintContext.validationContext.path

    /**
     * Retrieves an argument by index with safe bounds checking.
     *
     * If the index is out of bounds, returns a descriptive error string instead
     * of throwing an exception. This allows message formatting to gracefully
     * handle missing arguments.
     *
     * @param index The zero-based index of the argument
     * @return The argument value, or an error string if index is out of bounds
     */
    operator fun get(index: Int): Any? =
        if (index < 0 || index >= args.size) {
            "<index $index is out of range. args.size=${args.size}>"
        } else {
            args[index]
        }
}
