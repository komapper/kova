package org.komapper.extension.validator

import java.time.Clock
import java.time.LocalDate

class LocalDateValidator internal constructor(
    private val prev: Validator<LocalDate, LocalDate> = EmptyValidator(),
    constraint: Constraint<LocalDate> = Constraint.satisfied(),
    private val clock: Clock = Clock.systemDefaultZone(),
) : Validator<LocalDate, LocalDate>,
    Constrainable<LocalDate, LocalDateValidator> {
    private val next: ConstraintValidator<LocalDate> = ConstraintValidator(constraint)

    override fun execute(
        context: ValidationContext,
        input: LocalDate,
    ): ValidationResult<LocalDate> = prev.chain(next).execute(context, input)

    override fun constrain(
        key: String,
        check: ConstraintScope.(ConstraintContext<LocalDate>) -> ConstraintResult,
    ): LocalDateValidator = LocalDateValidator(prev = this, constraint = Constraint(key, check), clock = clock)

    fun future(message: (ConstraintContext<LocalDate>) -> Message = Message.resource0()): LocalDateValidator =
        constrain("kova.localDate.future") {
            satisfies(it.input > LocalDate.now(clock), message(it))
        }

    fun futureOrPresent(message: (ConstraintContext<LocalDate>) -> Message = Message.resource0()): LocalDateValidator =
        constrain("kova.localDate.futureOrPresent") {
            satisfies(it.input >= LocalDate.now(clock), message(it))
        }

    fun past(message: (ConstraintContext<LocalDate>) -> Message = Message.resource0()): LocalDateValidator =
        constrain("kova.localDate.past") {
            satisfies(it.input < LocalDate.now(clock), message(it))
        }

    fun pastOrPresent(message: (ConstraintContext<LocalDate>) -> Message = Message.resource0()): LocalDateValidator =
        constrain("kova.localDate.pastOrPresent") {
            satisfies(it.input <= LocalDate.now(clock), message(it))
        }
}
