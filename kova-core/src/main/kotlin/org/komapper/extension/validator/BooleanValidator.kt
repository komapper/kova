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

    fun isTrue(message: (ConstraintContext<Boolean>) -> Message = Message.resource0()): BooleanValidator =
        constrain("kova.boolean.isTrue") {
            satisfies(it.input, message(it))
        }

    fun isFalse(message: (ConstraintContext<Boolean>) -> Message = Message.resource0()): BooleanValidator =
        constrain("kova.boolean.isFalse") {
            satisfies(!it.input, message(it))
        }
}
