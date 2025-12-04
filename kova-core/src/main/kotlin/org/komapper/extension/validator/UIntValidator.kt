package org.komapper.extension.validator

interface UIntValidator :
    Validator<UInt, UInt>,
    Constrainable<UInt, UIntValidator> {
    fun min(
        value: UInt,
        message: MessageProvider1<UInt, UInt> = Message.resource1("kova.uint.min"),
    ): UIntValidator

    fun max(
        value: UInt,
        message: MessageProvider1<UInt, UInt> = Message.resource1("kova.uint.max"),
    ): UIntValidator

    fun gt(
        value: UInt,
        message: MessageProvider1<UInt, UInt> = Message.resource1("kova.uint.gt"),
    ): UIntValidator

    fun gte(
        value: UInt,
        message: MessageProvider1<UInt, UInt> = Message.resource1("kova.uint.gte"),
    ): UIntValidator

    fun lt(
        value: UInt,
        message: MessageProvider1<UInt, UInt> = Message.resource1("kova.uint.lt"),
    ): UIntValidator

    fun lte(
        value: UInt,
        message: MessageProvider1<UInt, UInt> = Message.resource1("kova.uint.lte"),
    ): UIntValidator

    operator fun plus(other: Validator<UInt, UInt>): UIntValidator

    infix fun and(other: Validator<UInt, UInt>): UIntValidator

    infix fun or(other: Validator<UInt, UInt>): UIntValidator

    fun chain(other: Validator<UInt, UInt>): UIntValidator
}

fun UIntValidator(
    name: String = "empty",
    prev: Validator<UInt, UInt> = EmptyValidator(),
    constraint: Constraint<UInt> = Constraint.satisfied(),
): UIntValidator = UIntValidatorImpl(name, prev, constraint)

private class UIntValidatorImpl(
    private val name: String,
    private val prev: Validator<UInt, UInt>,
    private val constraint: Constraint<UInt> = Constraint.satisfied(),
) : UIntValidator {
    private val next: ConstraintValidator<UInt> = ConstraintValidator(constraint)

    override fun execute(
        input: UInt,
        context: ValidationContext,
    ): ValidationResult<UInt> {
        val context = context.addLog(toString())
        return prev.chain(next).execute(input, context)
    }

    override fun constrain(
        id: String,
        check: ConstraintScope.(ConstraintContext<UInt>) -> ConstraintResult,
    ): UIntValidator = UIntValidatorImpl(name = id, prev = this, constraint = Constraint(id, check))

    override fun min(
        value: UInt,
        message: MessageProvider1<UInt, UInt>,
    ): UIntValidator =
        constrain(message.id) {
            satisfies(it.input >= value, message(it, value))
        }

    override fun max(
        value: UInt,
        message: MessageProvider1<UInt, UInt>,
    ): UIntValidator =
        constrain(message.id) {
            satisfies(it.input <= value, message(it, value))
        }

    override fun gt(
        value: UInt,
        message: MessageProvider1<UInt, UInt>,
    ): UIntValidator =
        constrain(message.id) {
            satisfies(it.input > value, message(it, value))
        }

    override fun gte(
        value: UInt,
        message: MessageProvider1<UInt, UInt>,
    ): UIntValidator =
        constrain(message.id) {
            satisfies(it.input >= value, message(it, value))
        }

    override fun lt(
        value: UInt,
        message: MessageProvider1<UInt, UInt>,
    ): UIntValidator =
        constrain(message.id) {
            satisfies(it.input < value, message(it, value))
        }

    override fun lte(
        value: UInt,
        message: MessageProvider1<UInt, UInt>,
    ): UIntValidator =
        constrain(message.id) {
            satisfies(it.input <= value, message(it, value))
        }

    override operator fun plus(other: Validator<UInt, UInt>): UIntValidator = and(other)

    override fun and(other: Validator<UInt, UInt>): UIntValidator {
        val combined = (this as Validator<UInt, UInt>).and(other)
        return UIntValidatorImpl("and", prev = combined)
    }

    override fun or(other: Validator<UInt, UInt>): UIntValidator {
        val combined = (this as Validator<UInt, UInt>).or(other)
        return UIntValidatorImpl("or", prev = combined)
    }

    override fun chain(other: Validator<UInt, UInt>): UIntValidator {
        val combined = (this as Validator<UInt, UInt>).chain(other)
        return UIntValidatorImpl("chain", prev = combined)
    }

    override fun toString(): String = "${UIntValidator::class.simpleName}(name=$name)"
}
