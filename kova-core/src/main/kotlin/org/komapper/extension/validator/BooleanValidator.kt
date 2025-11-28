package org.komapper.extension.validator

class BooleanValidator internal constructor(
    private val prev: Validator<Boolean, Boolean> = EmptyValidator(),
    constraint: Constraint<Boolean> = Constraint.satisfied(),
) : Validator<Boolean, Boolean>,
    Constrainable<Boolean, BooleanValidator> {
    private val next: ConstraintValidator<Boolean> = ConstraintValidator(constraint)

    override fun execute(
        context: ValidationContext,
        input: Boolean,
    ): ValidationResult<Boolean> = prev.chain(next).execute(context, input)

    override fun constrain(
        key: String,
        check: ConstraintScope.(ConstraintContext<Boolean>) -> ConstraintResult,
    ): BooleanValidator = BooleanValidator(prev = this, constraint = Constraint(key, check))

    fun literal(
        value: Boolean,
        message: (ConstraintContext<Boolean>, Boolean) -> Message = Message.resource1()): BooleanValidator =
        constrain("kova.boolean.literal") {
            satisfies(it.input == value, message(it, value))
        }

}
