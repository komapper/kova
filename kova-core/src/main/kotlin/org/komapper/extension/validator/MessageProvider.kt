package org.komapper.extension.validator

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
    /** The message key or identifier */
    val key: String

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
    /** The message key or identifier */
    val key: String

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
    /** The message key or identifier */
    val key: String

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
     * @param key Optional identifier for this message provider
     * @param get Function that generates the message text
     * @return A message provider
     */
    fun <T> text0(
        key: String = "",
        get: (ConstraintContext<T>) -> String,
    ): MessageProvider0<T> =
        object : MessageProvider0<T> {
            override val key: String = key

            override fun invoke(context: ConstraintContext<T>): Message = Message.Text(get(context))
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
     * @param key The resource bundle key
     * @return A message provider that loads messages from resources
     */
    fun <T> resource0(key: String): MessageProvider0<T> =
        object : MessageProvider0<T> {
            override val key: String = key

            override fun invoke(context: ConstraintContext<T>): Message = Message.Resource(key, context.input)
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
     * @param key Optional identifier for this message provider
     * @param get Function that generates the message text from context and argument
     * @return A message provider
     */
    fun <T, A1> text1(
        key: String = "",
        get: (ConstraintContext<T>, A1) -> String,
    ): MessageProvider1<T, A1> =
        object : MessageProvider1<T, A1> {
            override val key: String = key

            override fun invoke(
                context: ConstraintContext<T>,
                arg1: A1,
            ): Message = Message.Text(get(context, arg1))
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
     * @param key The resource bundle key
     * @return A message provider that loads messages from resources
     */
    fun <T, A1> resource1(key: String): MessageProvider1<T, A1> =
        object : MessageProvider1<T, A1> {
            override val key: String = key

            override fun invoke(
                context: ConstraintContext<T>,
                arg1: A1,
            ): Message = Message.Resource(key, context.input, arg1)
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
     * @param key Optional identifier for this message provider
     * @param get Function that generates the message text from context and arguments
     * @return A message provider
     */
    fun <T, A1, A2> text2(
        key: String = "",
        get: (ConstraintContext<T>, A1, A2) -> String,
    ): MessageProvider2<T, A1, A2> =
        object : MessageProvider2<T, A1, A2> {
            override val key: String = key

            override fun invoke(
                context: ConstraintContext<T>,
                arg1: A1,
                arg2: A2,
            ): Message = Message.Text(get(context, arg1, arg2))
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
     * @param key The resource bundle key
     * @return A message provider that loads messages from resources
     */
    fun <T, A1, A2> resource2(key: String): MessageProvider2<T, A1, A2> =
        object : MessageProvider2<T, A1, A2> {
            override val key: String = key

            override fun invoke(
                context: ConstraintContext<T>,
                arg1: A1,
                arg2: A2,
            ): Message = Message.Resource(key, context.input, arg1, arg2)
        }
}
