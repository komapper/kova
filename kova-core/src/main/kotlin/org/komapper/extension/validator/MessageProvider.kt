package org.komapper.extension.validator

interface MessageProvider0<T> {
    val key: String

    operator fun invoke(context: ConstraintContext<T>): Message
}

interface MessageProvider1<T, A1> {
    val key: String

    operator fun invoke(
        context: ConstraintContext<T>,
        arg1: A1,
    ): Message
}

interface MessageProvider2<T, A1, A2> {
    val key: String

    operator fun invoke(
        context: ConstraintContext<T>,
        arg1: A1,
        arg2: A2,
    ): Message
}

interface MessageProvider0Factory {
    fun <T> text0(
        key: String = "",
        get: (ConstraintContext<T>) -> String,
    ): MessageProvider0<T> =
        object : MessageProvider0<T> {
            override val key: String = key

            override fun invoke(context: ConstraintContext<T>): Message = Message.Text(get(context))
        }

    fun <T> resource0(key: String): MessageProvider0<T> =
        object : MessageProvider0<T> {
            override val key: String = key

            override fun invoke(context: ConstraintContext<T>): Message = Message.Resource(key, context.input)
        }
}

interface MessageProvider1Factory {
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

    fun <T, A1> resource1(key: String): MessageProvider1<T, A1> =
        object : MessageProvider1<T, A1> {
            override val key: String = key

            override fun invoke(
                context: ConstraintContext<T>,
                arg1: A1,
            ): Message = Message.Resource(key, context.input, arg1)
        }
}

interface MessageProvider2Factory {
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
