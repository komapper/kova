package org.komapper.extension.validator

sealed interface Message {
    data class Text(
        val content: String,
    ) : Message

    data class Resource(
        val key: String,
        val args: List<Any?>,
    ) : Message {
        constructor(key: String, vararg args: Any?) : this(key, args.toList())
    }

    data class ValidationFailure(
        val details: List<ValidationResult.FailureDetail>,
    ) : Message

    companion object {
        fun <T, A0 : ConstraintContext<T>> resource0(key: String): (A0) -> Message =
            { ctx ->
                Resource(key, ctx.input)
            }

        fun <T, A0 : ConstraintContext<T>, A1> resource1(key: String): (A0, A1) -> Message =
            { ctx, arg1 ->
                Resource(key, ctx.input, arg1)
            }

        fun <T, A0 : ConstraintContext<T>, A1, A2> resource2(key: String): (A0, A1, A2) -> Message =
            { ctx, arg1, arg2 ->
                Resource(key, ctx.input, arg1, arg2)
            }

        fun <T, A0 : ConstraintContext<T>, A1, A2, A3> resource3(key: String): (A0, A1, A2, A3) -> Message =
            { ctx, arg1, arg2, arg3 ->
                Resource(key, ctx.input, arg1, arg2, arg3)
            }
    }
}
