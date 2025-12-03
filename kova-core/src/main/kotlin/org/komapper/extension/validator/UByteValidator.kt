package org.komapper.extension.validator

interface UByteValidator :
    Validator<UByte, UByte>,
    Constrainable<UByte, UByteValidator> {
    fun min(
        value: UByte,
        message: MessageProvider1<UByte, UByte> = Message.resource1("kova.ubyte.min"),
    ): UByteValidator

    fun max(
        value: UByte,
        message: MessageProvider1<UByte, UByte> = Message.resource1("kova.ubyte.max"),
    ): UByteValidator

    fun gt(
        value: UByte,
        message: MessageProvider1<UByte, UByte> = Message.resource1("kova.ubyte.gt"),
    ): UByteValidator

    fun gte(
        value: UByte,
        message: MessageProvider1<UByte, UByte> = Message.resource1("kova.ubyte.gte"),
    ): UByteValidator

    fun lt(
        value: UByte,
        message: MessageProvider1<UByte, UByte> = Message.resource1("kova.ubyte.lt"),
    ): UByteValidator

    fun lte(
        value: UByte,
        message: MessageProvider1<UByte, UByte> = Message.resource1("kova.ubyte.lte"),
    ): UByteValidator

    operator fun plus(other: Validator<UByte, UByte>): UByteValidator

    infix fun and(other: Validator<UByte, UByte>): UByteValidator

    infix fun or(other: Validator<UByte, UByte>): UByteValidator

    fun chain(other: Validator<UByte, UByte>): UByteValidator
}

fun UByteValidator(
    name: String = "empty",
    prev: Validator<UByte, UByte> = EmptyValidator(),
    constraint: Constraint<UByte> = Constraint.satisfied(),
): UByteValidator = UByteValidatorImpl(name, prev, constraint)

private class UByteValidatorImpl(
    private val name: String,
    private val prev: Validator<UByte, UByte>,
    private val constraint: Constraint<UByte> = Constraint.satisfied(),
) : UByteValidator {
    private val next: ConstraintValidator<UByte> = ConstraintValidator(constraint)

    override fun execute(
        input: UByte,
        context: ValidationContext,
    ): ValidationResult<UByte> {
        val context = context.addLog(toString())
        return prev.chain(next).execute(input, context)
    }

    override fun constrain(
        id: String,
        check: ConstraintScope.(ConstraintContext<UByte>) -> ConstraintResult,
    ): UByteValidator = UByteValidatorImpl(name = id, prev = this, constraint = Constraint(id, check))

    override fun min(
        value: UByte,
        message: MessageProvider1<UByte, UByte>,
    ): UByteValidator =
        constrain(message.key) {
            satisfies(it.input >= value, message(it, value))
        }

    override fun max(
        value: UByte,
        message: MessageProvider1<UByte, UByte>,
    ): UByteValidator =
        constrain(message.key) {
            satisfies(it.input <= value, message(it, value))
        }

    override fun gt(
        value: UByte,
        message: MessageProvider1<UByte, UByte>,
    ): UByteValidator =
        constrain(message.key) {
            satisfies(it.input > value, message(it, value))
        }

    override fun gte(
        value: UByte,
        message: MessageProvider1<UByte, UByte>,
    ): UByteValidator =
        constrain(message.key) {
            satisfies(it.input >= value, message(it, value))
        }

    override fun lt(
        value: UByte,
        message: MessageProvider1<UByte, UByte>,
    ): UByteValidator =
        constrain(message.key) {
            satisfies(it.input < value, message(it, value))
        }

    override fun lte(
        value: UByte,
        message: MessageProvider1<UByte, UByte>,
    ): UByteValidator =
        constrain(message.key) {
            satisfies(it.input <= value, message(it, value))
        }

    override operator fun plus(other: Validator<UByte, UByte>): UByteValidator = and(other)

    override fun and(other: Validator<UByte, UByte>): UByteValidator {
        val combined = (this as Validator<UByte, UByte>).and(other)
        return UByteValidatorImpl("and", prev = combined)
    }

    override fun or(other: Validator<UByte, UByte>): UByteValidator {
        val combined = (this as Validator<UByte, UByte>).or(other)
        return UByteValidatorImpl("or", prev = combined)
    }

    override fun chain(other: Validator<UByte, UByte>): UByteValidator {
        val combined = (this as Validator<UByte, UByte>).chain(other)
        return UByteValidatorImpl("chain", prev = combined)
    }

    override fun toString(): String = "${UByteValidator::class.simpleName}(name=$name)"
}
