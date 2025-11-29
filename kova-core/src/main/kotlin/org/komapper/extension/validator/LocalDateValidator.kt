package org.komapper.extension.validator

import java.time.Clock
import java.time.LocalDate

interface LocalDateValidator :
    Validator<LocalDate, LocalDate>,
    Constrainable<LocalDate, LocalDateValidator> {
    fun future(message: (ConstraintContext<LocalDate>) -> Message = Message.resource0()): LocalDateValidator

    fun futureOrPresent(message: (ConstraintContext<LocalDate>) -> Message = Message.resource0()): LocalDateValidator

    fun past(message: (ConstraintContext<LocalDate>) -> Message = Message.resource0()): LocalDateValidator

    fun pastOrPresent(message: (ConstraintContext<LocalDate>) -> Message = Message.resource0()): LocalDateValidator
}

fun LocalDateValidator(
    prev: Validator<LocalDate, LocalDate> = EmptyValidator(),
    constraint: Constraint<LocalDate> = Constraint.satisfied(),
    clock: Clock = Clock.systemDefaultZone(),
): LocalDateValidator = LocalDateValidatorImpl(prev, constraint, clock)

private class LocalDateValidatorImpl(
    private val prev: Validator<LocalDate, LocalDate>,
    constraint: Constraint<LocalDate>,
    private val clock: Clock,
) : LocalDateValidator {
    private val next: ConstraintValidator<LocalDate> = ConstraintValidator(constraint)

    override fun execute(
        context: ValidationContext,
        input: LocalDate,
    ): ValidationResult<LocalDate> = prev.chain(next).execute(context, input)

    override fun constrain(
        key: String,
        check: ConstraintScope.(ConstraintContext<LocalDate>) -> ConstraintResult,
    ): LocalDateValidator = LocalDateValidatorImpl(prev = this, constraint = Constraint(key, check), clock = clock)

    override fun future(message: (ConstraintContext<LocalDate>) -> Message): LocalDateValidator =
        constrain("kova.localDate.future") {
            satisfies(it.input > LocalDate.now(clock), message(it))
        }

    override fun futureOrPresent(message: (ConstraintContext<LocalDate>) -> Message): LocalDateValidator =
        constrain("kova.localDate.futureOrPresent") {
            satisfies(it.input >= LocalDate.now(clock), message(it))
        }

    override fun past(message: (ConstraintContext<LocalDate>) -> Message): LocalDateValidator =
        constrain("kova.localDate.past") {
            satisfies(it.input < LocalDate.now(clock), message(it))
        }

    override fun pastOrPresent(message: (ConstraintContext<LocalDate>) -> Message): LocalDateValidator =
        constrain("kova.localDate.pastOrPresent") {
            satisfies(it.input <= LocalDate.now(clock), message(it))
        }
}
