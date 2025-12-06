package org.komapper.extension.validator

interface MessageProvider<T> {
    operator fun invoke(
        constraintContext: ConstraintContext<T>,
        vararg args: Any?,
    ): Message
}

interface MessageProviderFactory {
    fun <T> text(get: (MessageContext<T>) -> String): MessageProvider<T> =
        object : MessageProvider<T> {
            override fun invoke(
                constraintContext: ConstraintContext<T>,
                vararg args: Any?,
            ): Message {
                val messageContext = MessageContext(constraintContext, args.toList())
                return Message.Text(messageContext, get(messageContext))
            }
        }

    fun <T> resource(): MessageProvider<T> =
        object : MessageProvider<T> {
            override fun invoke(
                constraintContext: ConstraintContext<T>,
                vararg args: Any?,
            ): Message {
                val messageContext = MessageContext(constraintContext, args.toList())
                return Message.Resource(messageContext)
            }
        }
}

// TODO
data class MessageContext<T>(
    val constraintContext: ConstraintContext<T>,
    val args: List<Any?> = emptyList(),
) {
    val input: T get() = constraintContext.input
    val constraintId: String get() = constraintContext.constraintId

    /** The root object's qualified class name */
    val root: String get() = constraintContext.validationContext.root

    /** The current validation path */
    val path: Path get() = constraintContext.validationContext.path

    operator fun get(index: Int): Any? = args[index]
}
