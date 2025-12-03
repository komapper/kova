package org.komapper.extension.validator

interface ULongValidator :
    Validator<ULong, ULong>,
    Constrainable<ULong, ULongValidator> {
    fun min(
        value: ULong,
        message: MessageProvider1<ULong, ULong> = Message.resource1("kova.ulong.min"),
    ): ULongValidator

    fun max(
        value: ULong,
        message: MessageProvider1<ULong, ULong> = Message.resource1("kova.ulong.max"),
    ): ULongValidator

    fun gt(
        value: ULong,
        message: MessageProvider1<ULong, ULong> = Message.resource1("kova.ulong.gt"),
    ): ULongValidator

    fun gte(
        value: ULong,
        message: MessageProvider1<ULong, ULong> = Message.resource1("kova.ulong.gte"),
    ): ULongValidator

    fun lt(
        value: ULong,
        message: MessageProvider1<ULong, ULong> = Message.resource1("kova.ulong.lt"),
    ): ULongValidator

    fun lte(
        value: ULong,
        message: MessageProvider1<ULong, ULong> = Message.resource1("kova.ulong.lte"),
    ): ULongValidator

    operator fun plus(other: Validator<ULong, ULong>): ULongValidator

    infix fun and(other: Validator<ULong, ULong>): ULongValidator

    infix fun or(other: Validator<ULong, ULong>): ULongValidator

    fun chain(other: Validator<ULong, ULong>): ULongValidator
}

fun ULongValidator(
    name: String = "empty",
    prev: Validator<ULong, ULong> = EmptyValidator(),
    constraint: Constraint<ULong> = Constraint.satisfied(),
): ULongValidator = ULongValidatorImpl(name, prev, constraint)

private class ULongValidatorImpl(
    private val name: String,
    private val prev: Validator<ULong, ULong>,
    private val constraint: Constraint<ULong> = Constraint.satisfied(),
) : ULongValidator {
    private val next: ConstraintValidator<ULong> = ConstraintValidator(constraint)

    override fun execute(
        input: ULong,
        context: ValidationContext,
    ): ValidationResult<ULong> {
        val context = context.addLog(toString())
        return prev.chain(next).execute(input, context)
    }

    override fun constrain(
        id: String,
        check: ConstraintScope.(ConstraintContext<ULong>) -> ConstraintResult,
    ): ULongValidator = ULongValidatorImpl(name = id, prev = this, constraint = Constraint(id, check))

    override fun min(
        value: ULong,
        message: MessageProvider1<ULong, ULong>,
    ): ULongValidator =
        constrain(message.key) {
            satisfies(it.input >= value, message(it, value))
        }

    override fun max(
        value: ULong,
        message: MessageProvider1<ULong, ULong>,
    ): ULongValidator =
        constrain(message.key) {
            satisfies(it.input <= value, message(it, value))
        }

    override fun gt(
        value: ULong,
        message: MessageProvider1<ULong, ULong>,
    ): ULongValidator =
        constrain(message.key) {
            satisfies(it.input > value, message(it, value))
        }

    override fun gte(
        value: ULong,
        message: MessageProvider1<ULong, ULong>,
    ): ULongValidator =
        constrain(message.key) {
            satisfies(it.input >= value, message(it, value))
        }

    override fun lt(
        value: ULong,
        message: MessageProvider1<ULong, ULong>,
    ): ULongValidator =
        constrain(message.key) {
            satisfies(it.input < value, message(it, value))
        }

    override fun lte(
        value: ULong,
        message: MessageProvider1<ULong, ULong>,
    ): ULongValidator =
        constrain(message.key) {
            satisfies(it.input <= value, message(it, value))
        }

    override operator fun plus(other: Validator<ULong, ULong>): ULongValidator = and(other)

    override fun and(other: Validator<ULong, ULong>): ULongValidator {
        val combined = (this as Validator<ULong, ULong>).and(other)
        return ULongValidatorImpl("and", prev = combined)
    }

    override fun or(other: Validator<ULong, ULong>): ULongValidator {
        val combined = (this as Validator<ULong, ULong>).or(other)
        return ULongValidatorImpl("or", prev = combined)
    }

    override fun chain(other: Validator<ULong, ULong>): ULongValidator {
        val combined = (this as Validator<ULong, ULong>).chain(other)
        return ULongValidatorImpl("chain", prev = combined)
    }

    override fun toString(): String = "${ULongValidator::class.simpleName}(name=$name)"
}
