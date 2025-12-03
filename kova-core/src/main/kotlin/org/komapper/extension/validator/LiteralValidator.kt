package org.komapper.extension.validator

interface LiteralValidator<T : Any> :
    Validator<T, T>,
    Constrainable<T, LiteralValidator<T>> {
    fun single(
        value: T,
        message: MessageProvider1<T, T> = Message.resource1("kova.literal.single"),
    ): LiteralValidator<T>

    fun list(
        values: List<T>,
        message: MessageProvider1<T, List<T>> = Message.resource1("kova.literal.list"),
    ): LiteralValidator<T>
}

fun <T : Any> LiteralValidator(
    name: String = "empty",
    prev: Validator<T, T> = EmptyValidator(),
    constraint: Constraint<T> = Constraint.satisfied(),
): LiteralValidator<T> = LiteralValidatorImpl(name, prev, constraint)

private class LiteralValidatorImpl<T : Any>(
    private val name: String,
    private val prev: Validator<T, T>,
    private val constraint: Constraint<T>,
) : LiteralValidator<T> {
    private val next: ConstraintValidator<T> = ConstraintValidator(constraint)

    override fun execute(
        input: T,
        context: ValidationContext,
    ): ValidationResult<T> {
        val context = context.addLog(toString())
        return prev.chain(next).execute(input, context)
    }

    override fun constrain(
        id: String,
        check: ConstraintScope.(ConstraintContext<T>) -> ConstraintResult,
    ): LiteralValidator<T> = LiteralValidatorImpl(name = id, prev = this, constraint = Constraint(id, check))

    override fun single(
        value: T,
        message: MessageProvider1<T, T>,
    ): LiteralValidator<T> =
        constrain(message.key) {
            satisfies(it.input == value, message(it, value))
        }

    override fun list(
        values: List<T>,
        message: MessageProvider1<T, List<T>>,
    ): LiteralValidator<T> =
        constrain(message.key) {
            satisfies(it.input in values, message(it, values))
        }

    override fun toString(): String = "${LiteralValidator::class.simpleName}(name=$name)"
}
