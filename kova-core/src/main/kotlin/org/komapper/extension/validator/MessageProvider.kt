package org.komapper.extension.validator

/**
 * Provides error messages for constraint violations.
 *
 * MessageProvider is a functional interface that creates [Message] objects when
 * constraints are violated. It accepts named arguments as pairs and returns a function
 * that generates contextual error messages from a [ConstraintContext].
 *
 * Message providers are typically created using the [MessageProviderFactory] methods
 * available on the [MessageProvider] companion object.
 *
 * Example usage:
 * ```kotlin
 * // Using default resource-based message provider
 * fun StringValidator.min(
 *     length: Int,
 *     message: MessageProvider = MessageProvider.resource()
 * ) = constrain("kova.string.min") {
 *     satisfies(it.input.length >= length, message("input" to it.input, "length" to length))
 * }
 *
 * // Using custom text message provider with named argument access
 * fun StringValidator.customMin(
 *     length: Int,
 *     message: MessageProvider = MessageProvider.text { ctx ->
 *         "String '${ctx.input}' is too short. Minimum length is ${ctx["length"]}"
 *     }
 * ) = constrain("custom.min") {
 *     satisfies(it.input.length >= length, message("length" to length))
 * }
 *
 * // Arguments can also be accessed by index
 * val provider = MessageProvider.text { ctx ->
 *     "Value '${ctx.input}' must be at least ${ctx[0]}"
 * }
 * ```
 *
 */
interface MessageProvider {
    /**
     * Creates a message factory for a constraint violation.
     *
     * This method is called by constraint validators to create a message factory function.
     * It accepts named arguments as pairs (name to value) for message formatting and returns a
     * function that generates a [Message] from a [ConstraintContext].
     *
     * The arguments can be accessed in message templates either by name using [MessageContext.get]
     * with a String key, or by index using [MessageContext.get] with an Int. Both access methods
     * are fully supported.
     *
     * @param args Named arguments for message formatting as pairs of (name, value)
     * @return A function that accepts a ConstraintContext and returns a Message object
     */
    operator fun invoke(vararg args: Pair<String, Any?>): (ConstraintContext<*>) -> Message

    companion object : MessageProviderFactory
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
 * val customProvider = MessageProvider.text { ctx ->
 *     "Value ${ctx.input} failed validation at path ${ctx.path.fullName}"
 * }
 *
 * // Create a resource bundle message provider
 * val resourceProvider = MessageProvider.resource()
 * ```
 */
interface MessageProviderFactory {
    /**
     * Creates a message provider that generates text messages.
     *
     * Text messages are created dynamically using the provided lambda function,
     * which receives a [MessageContext] with access to the input value, named arguments,
     * and validation state.
     *
     * Arguments passed to the provider can be accessed either by name using [MessageContext.get]
     * with a String key, or by index using [MessageContext.get] with an Int. Both access methods
     * are fully supported, and you can choose the one that best fits your use case.
     *
     * Example with named argument access:
     * ```kotlin
     * val provider = MessageProvider.text { ctx ->
     *     "Value ${ctx.input} must be at least ${ctx["minValue"]}"
     * }
     * // Usage: provider("minValue" to 10)
     * ```
     *
     * Example with index access:
     * ```kotlin
     * val provider = MessageProvider.text { ctx ->
     *     "Value ${ctx.input} must be at least ${ctx[0]}"
     * }
     * // Usage: provider("minValue" to 10)
     * ```
     *
     * @param format Lambda that formats the message text from the context
     * @return A MessageProvider that creates Text messages
     */
    fun text(format: (MessageContext<*>) -> String): MessageProvider =
        object : MessageProvider {
            override fun invoke(vararg args: Pair<String, Any?>): (ConstraintContext<*>) -> Message =
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
     * Arguments are passed as named pairs (name to value), but in resource bundle messages,
     * they are typically accessed by index in the MessageFormat pattern (e.g., {0}, {1}, {2}).
     * The order of arguments in the vararg determines their index for MessageFormat substitution.
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
     *     message: MessageProvider = MessageProvider.resource()
     * ) = constrain("kova.string.min") { ctx ->
     *     satisfies(ctx.input.length >= length, message("input" to ctx.input, "length" to length))
     * }
     * // This will use kova.string.min from resources with ctx.input as {0} and length as {1}
     * ```
     *
     * @return A MessageProvider that creates Resource messages
     */
    fun resource(): MessageProvider =
        object : MessageProvider {
            override fun invoke(vararg args: Pair<String, Any?>): (ConstraintContext<*>) -> Message =
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
 * MessageContext provides access to the input value, named arguments, constraint ID,
 * and validation state. It is passed to message provider lambdas to enable
 * contextual error message generation.
 *
 * Arguments can be accessed either by name using [get] with a String key,
 * or by index using [get] with an Int. Both access methods are fully supported.
 *
 * Example usage with named argument access:
 * ```kotlin
 * val provider = MessageProvider.text { ctx ->
 *     // Access input value
 *     val value = ctx.input
 *
 *     // Access arguments by name
 *     val minLength = ctx["minLength"]
 *     val maxLength = ctx["maxLength"]
 *
 *     // Access validation path
 *     val path = ctx.path.fullName
 *
 *     "Value '$value' at path '$path' must be between $minLength and $maxLength characters"
 * }
 * // Usage: provider("minLength" to 5, "maxLength" to 10)
 * ```
 *
 * Example usage with index access:
 * ```kotlin
 * val provider = MessageProvider.text { ctx ->
 *     "Value '${ctx.input}' must be at least ${ctx[0]} characters"
 * }
 * // Usage: provider("minLength" to 5)
 * ```
 *
 * @param T The type of the input value being validated
 * @property args Named arguments passed to the message provider as pairs of (name, value)
 * @property constraintContext The underlying constraint context
 */
data class MessageContext<T>(
    val args: List<Pair<String, Any?>> = emptyList(),
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
     * This method provides index-based access to arguments. If the index is out of bounds,
     * returns a descriptive error string instead of throwing an exception. This allows
     * message formatting to gracefully handle missing arguments.
     *
     * @param index The zero-based index of the argument
     * @return The argument value, or an error string if index is out of bounds
     */
    operator fun get(index: Int): Any? =
        if (index < 0 || index >= args.size) {
            "<index $index is out of range. args.size=${args.size}>"
        } else {
            args[index].second
        }

    /**
     * Retrieves an argument by name with safe lookup.
     *
     * This method provides named access to arguments passed to the message provider.
     * If the key is not found in the arguments, returns a descriptive error string
     * instead of throwing an exception. This allows message formatting to gracefully
     * handle missing arguments.
     *
     * Example:
     * ```kotlin
     * val provider = MessageProvider.text { ctx ->
     *     "Value must be between ${ctx["min"]} and ${ctx["max"]}"
     * }
     * // Usage: provider("min" to 1, "max" to 10)
     * ```
     *
     * @param key The name of the argument to retrieve
     * @return The argument value, or an error string if key is not found
     */
    operator fun get(key: String): Any? {
        val value = args.find { it.first == key }?.let { return it.second }
        return value ?: "<key $key not found>"
    }
}
