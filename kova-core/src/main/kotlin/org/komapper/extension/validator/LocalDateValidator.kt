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

    fun isFuture(message: (ConstraintContext<LocalDate>) -> Message = Message.resource0()): LocalDateValidator =
        constrain("kova.localDate.isFuture") {
            satisfies(it.input > LocalDate.now(clock), message(it))
        }

    fun isFutureOrPresent(message: (ConstraintContext<LocalDate>) -> Message = Message.resource0()): LocalDateValidator =
        constrain("kova.localDate.isFutureOrPresent") {
            satisfies(it.input >= LocalDate.now(clock), message(it))
        }

    fun isPast(message: (ConstraintContext<LocalDate>) -> Message = Message.resource0()): LocalDateValidator =
        constrain("kova.localDate.isPast") {
            satisfies(it.input < LocalDate.now(clock), message(it))
        }

    fun isPastOrPresent(message: (ConstraintContext<LocalDate>) -> Message = Message.resource0()): LocalDateValidator =
        constrain("kova.localDate.isPastOrPresent") {
            satisfies(it.input <= LocalDate.now(clock), message(it))
        }
}
