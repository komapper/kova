package org.komapper.extension.validator

interface MessageProvider<T> {
    operator fun invoke(
        context: ConstraintContext<T>,
        vararg args: Any?,
    ): Message
}

interface MessageProviderFactory {
    fun <T> text(get: (ConstraintContext<T>, List<Any?>) -> String): MessageProvider<T> =
        object : MessageProvider<T> {
            override fun invoke(
                context: ConstraintContext<T>,
                vararg args: Any?,
            ): Message = Message.Text(context.constraintId, get(context, args.toList()))
        }

    fun <T> resource(): MessageProvider<T> =
        object : MessageProvider<T> {
            override fun invoke(
                context: ConstraintContext<T>,
                vararg args: Any?,
            ): Message = Message.Resource(context.constraintId, *args)
        }
}

/**
 * Provides error messages for validation failures with no additional arguments.
 *
 * Use this for validators that don't need to include dynamic values in error messages.
 *
 * Example:
 * ```kotlin
 * val notBlankMessage = Message.text0<String> { context ->
 *     "Value must not be blank"
 * }
 * ```
 *
 * @param T The type being validated
 */
interface MessageProvider0<T> {
    /** The message identifier */
    val id: String

    /**
     * Creates a message for the given constraint context.
     *
     * @param context The constraint context containing the input value
     * @return The error message
     */
    operator fun invoke(context: ConstraintContext<T>): Message
}

/**
 * Provides error messages for validation failures with one additional argument.
 *
 * Use this for validators that include one dynamic value in error messages,
 * such as a minimum/maximum value.
 *
 * Example:
 * ```kotlin
 * val minMessage = Message.text1<String, Int> { context, min ->
 *     "Value must be at least $min characters, but was ${context.input.length}"
 * }
 * ```
 *
 * @param T The type being validated
 * @param A1 The type of the first argument
 */
interface MessageProvider1<T, A1> {
    /** The message identifier */
    val id: String

    /**
     * Creates a message for the given constraint context and argument.
     *
     * @param context The constraint context containing the input value
     * @param arg1 The first argument to include in the message
     * @return The error message
     */
    operator fun invoke(
        context: ConstraintContext<T>,
        arg1: A1,
    ): Message
}

/**
 * Provides error messages for validation failures with two additional arguments.
 *
 * Use this for validators that include two dynamic values in error messages,
 * such as a range with minimum and maximum values.
 *
 * Example:
 * ```kotlin
 * val rangeMessage = Message.text2<Int, Int, Int> { context, min, max ->
 *     "Value must be between $min and $max, but was ${context.input}"
 * }
 * ```
 *
 * @param T The type being validated
 * @param A1 The type of the first argument
 * @param A2 The type of the second argument
 */
interface MessageProvider2<T, A1, A2> {
    /** The message identifier */
    val id: String

    /**
     * Creates a message for the given constraint context and arguments.
     *
     * @param context The constraint context containing the input value
     * @param arg1 The first argument to include in the message
     * @param arg2 The second argument to include in the message
     * @return The error message
     */
    operator fun invoke(
        context: ConstraintContext<T>,
        arg1: A1,
        arg2: A2,
    ): Message
}

/**
 * Factory for creating [MessageProvider0] instances.
 *
 * Available through `Message.text0()` and `Message.resource0()`.
 */
interface MessageProvider0Factory {
    /**
     * Creates a text-based message provider with no arguments.
     *
     * Use this for custom hardcoded error messages.
     *
     * Example:
     * ```kotlin
     * fun positive(message: MessageProvider0<Int> = Message.text0 { context ->
     *     "Number ${context.input} must be positive"
     * }): NumberValidator<Int>
     * ```
     *
     * @param id Optional identifier for this message provider
     * @param get Function that generates the message text
     * @return A message provider
     */
    fun <T> text0(
        id: String = "",
        get: (ConstraintContext<T>) -> String,
    ): MessageProvider0<T> =
        object : MessageProvider0<T> {
            override val id: String = id

            override fun invoke(context: ConstraintContext<T>): Message = Message.Text(id, get(context))
        }

    /**
     * Creates a resource bundle-based message provider with no arguments.
     *
     * Use this for i18n support. The message is loaded from `kova.properties`.
     *
     * Example:
     * ```kotlin
     * fun notBlank(message: MessageProvider0<String> = Message.resource0("kova.string.notBlank")): StringValidator
     * ```
     *
     * @param id The resource bundle key
     * @return A message provider that loads messages from resources
     */
    fun <T> resource0(id: String): MessageProvider0<T> =
        object : MessageProvider0<T> {
            override val id: String = id

            override fun invoke(context: ConstraintContext<T>): Message = Message.Resource(id, context.input)
        }
}

/**
 * Factory for creating [MessageProvider1] instances.
 *
 * Available through `Message.text1()` and `Message.resource1()`.
 */
interface MessageProvider1Factory {
    /**
     * Creates a text-based message provider with one argument.
     *
     * Use this for custom error messages that include one dynamic value.
     *
     * Example:
     * ```kotlin
     * fun min(
     *     minValue: Int,
     *     message: MessageProvider1<String, Int> = Message.text1 { context, min ->
     *         "String must be at least $min characters, but was ${context.input.length}"
     *     }
     * ): StringValidator
     * ```
     *
     * @param id Optional identifier for this message provider
     * @param get Function that generates the message text from context and argument
     * @return A message provider
     */
    fun <T, A1> text1(
        id: String = "",
        get: (ConstraintContext<T>, A1) -> String,
    ): MessageProvider1<T, A1> =
        object : MessageProvider1<T, A1> {
            override val id: String = id

            override fun invoke(
                context: ConstraintContext<T>,
                arg1: A1,
            ): Message = Message.Text(id, get(context, arg1))
        }

    /**
     * Creates a resource bundle-based message provider with one argument.
     *
     * Use this for i18n support with one dynamic value.
     * The message pattern uses `{0}` for the input and `{1}` for the argument.
     *
     * Example resource (kova.properties):
     * ```properties
     * kova.string.min={0} must be at least {1} characters
     * ```
     *
     * Example usage:
     * ```kotlin
     * fun min(
     *     minLength: Int,
     *     message: MessageProvider1<String, Int> = Message.resource1("kova.string.min")
     * ): StringValidator
     * ```
     *
     * @param id The resource bundle key
     * @return A message provider that loads messages from resources
     */
    fun <T, A1> resource1(id: String): MessageProvider1<T, A1> =
        object : MessageProvider1<T, A1> {
            override val id: String = id

            override fun invoke(
                context: ConstraintContext<T>,
                arg1: A1,
            ): Message = Message.Resource(id, context.input, arg1)
        }
}

/**
 * Factory for creating [MessageProvider2] instances.
 *
 * Available through `Message.text2()` and `Message.resource2()`.
 */
interface MessageProvider2Factory {
    /**
     * Creates a text-based message provider with two arguments.
     *
     * Use this for custom error messages that include two dynamic values.
     *
     * Example:
     * ```kotlin
     * fun range(
     *     min: Int,
     *     max: Int,
     *     message: MessageProvider2<Int, Int, Int> = Message.text2 { context, minVal, maxVal ->
     *         "Value must be between $minVal and $maxVal, but was ${context.input}"
     *     }
     * ): NumberValidator<Int>
     * ```
     *
     * @param id Optional identifier for this message provider
     * @param get Function that generates the message text from context and arguments
     * @return A message provider
     */
    fun <T, A1, A2> text2(
        id: String = "",
        get: (ConstraintContext<T>, A1, A2) -> String,
    ): MessageProvider2<T, A1, A2> =
        object : MessageProvider2<T, A1, A2> {
            override val id: String = id

            override fun invoke(
                context: ConstraintContext<T>,
                arg1: A1,
                arg2: A2,
            ): Message = Message.Text(id, get(context, arg1, arg2))
        }

    /**
     * Creates a resource bundle-based message provider with two arguments.
     *
     * Use this for i18n support with two dynamic values.
     * The message pattern uses `{0}` for the input, `{1}` for the first argument,
     * and `{2}` for the second argument.
     *
     * Example resource (kova.properties):
     * ```properties
     * kova.collection.min={0} must have at least {1} elements, but has {2}
     * ```
     *
     * Example usage:
     * ```kotlin
     * fun min(
     *     size: Int,
     *     message: MessageProvider2<List<*>, Int, Int> = Message.resource2("kova.collection.min")
     * ): CollectionValidator<E, C>
     * ```
     *
     * @param id The resource bundle key
     * @return A message provider that loads messages from resources
     */
    fun <T, A1, A2> resource2(id: String): MessageProvider2<T, A1, A2> =
        object : MessageProvider2<T, A1, A2> {
            override val id: String = id

            override fun invoke(
                context: ConstraintContext<T>,
                arg1: A1,
                arg2: A2,
            ): Message = Message.Resource(id, context.input, arg1, arg2)
        }
}
