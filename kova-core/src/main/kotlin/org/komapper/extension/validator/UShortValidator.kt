package org.komapper.extension.validator

interface UShortValidator :
    Validator<UShort, UShort>,
    Constrainable<UShort, UShortValidator> {
    fun min(
        value: UShort,
        message: MessageProvider1<UShort, UShort> = Message.resource1("kova.ushort.min"),
    ): UShortValidator

    fun max(
        value: UShort,
        message: MessageProvider1<UShort, UShort> = Message.resource1("kova.ushort.max"),
    ): UShortValidator

    fun gt(
        value: UShort,
        message: MessageProvider1<UShort, UShort> = Message.resource1("kova.ushort.gt"),
    ): UShortValidator

    fun gte(
        value: UShort,
        message: MessageProvider1<UShort, UShort> = Message.resource1("kova.ushort.gte"),
    ): UShortValidator

    fun lt(
        value: UShort,
        message: MessageProvider1<UShort, UShort> = Message.resource1("kova.ushort.lt"),
    ): UShortValidator

    fun lte(
        value: UShort,
        message: MessageProvider1<UShort, UShort> = Message.resource1("kova.ushort.lte"),
    ): UShortValidator

    operator fun plus(other: Validator<UShort, UShort>): UShortValidator

    infix fun and(other: Validator<UShort, UShort>): UShortValidator

    infix fun or(other: Validator<UShort, UShort>): UShortValidator

    fun chain(other: Validator<UShort, UShort>): UShortValidator
}

fun UShortValidator(
    name: String = "empty",
    prev: Validator<UShort, UShort> = EmptyValidator(),
    constraint: Constraint<UShort> = Constraint.satisfied(),
): UShortValidator = UShortValidatorImpl(name, prev, constraint)

private class UShortValidatorImpl(
    private val name: String,
    private val prev: Validator<UShort, UShort>,
    private val constraint: Constraint<UShort> = Constraint.satisfied(),
) : UShortValidator {
    private val next: ConstraintValidator<UShort> = ConstraintValidator(constraint)

    override fun execute(
        input: UShort,
        context: ValidationContext,
    ): ValidationResult<UShort> {
        val context = context.addLog(toString())
        return prev.chain(next).execute(input, context)
    }

    override fun constrain(
        id: String,
        check: ConstraintScope.(ConstraintContext<UShort>) -> ConstraintResult,
    ): UShortValidator = UShortValidatorImpl(name = id, prev = this, constraint = Constraint(id, check))

    override fun min(
        value: UShort,
        message: MessageProvider1<UShort, UShort>,
    ): UShortValidator =
        constrain(message.key) {
            satisfies(it.input >= value, message(it, value))
        }

    override fun max(
        value: UShort,
        message: MessageProvider1<UShort, UShort>,
    ): UShortValidator =
        constrain(message.key) {
            satisfies(it.input <= value, message(it, value))
        }

    override fun gt(
        value: UShort,
        message: MessageProvider1<UShort, UShort>,
    ): UShortValidator =
        constrain(message.key) {
            satisfies(it.input > value, message(it, value))
        }

    override fun gte(
        value: UShort,
        message: MessageProvider1<UShort, UShort>,
    ): UShortValidator =
        constrain(message.key) {
            satisfies(it.input >= value, message(it, value))
        }

    override fun lt(
        value: UShort,
        message: MessageProvider1<UShort, UShort>,
    ): UShortValidator =
        constrain(message.key) {
            satisfies(it.input < value, message(it, value))
        }

    override fun lte(
        value: UShort,
        message: MessageProvider1<UShort, UShort>,
    ): UShortValidator =
        constrain(message.key) {
            satisfies(it.input <= value, message(it, value))
        }

    override operator fun plus(other: Validator<UShort, UShort>): UShortValidator = and(other)

    override fun and(other: Validator<UShort, UShort>): UShortValidator {
        val combined = (this as Validator<UShort, UShort>).and(other)
        return UShortValidatorImpl("and", prev = combined)
    }

    override fun or(other: Validator<UShort, UShort>): UShortValidator {
        val combined = (this as Validator<UShort, UShort>).or(other)
        return UShortValidatorImpl("or", prev = combined)
    }

    override fun chain(other: Validator<UShort, UShort>): UShortValidator {
        val combined = (this as Validator<UShort, UShort>).chain(other)
        return UShortValidatorImpl("chain", prev = combined)
    }

    override fun toString(): String = "${UShortValidator::class.simpleName}(name=$name)"
}
