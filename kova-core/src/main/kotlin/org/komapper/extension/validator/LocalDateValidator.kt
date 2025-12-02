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
    name: String = "empty",
    prev: Validator<LocalDate, LocalDate> = EmptyValidator(),
    constraint: Constraint<LocalDate> = Constraint.satisfied(),
    clock: Clock = Clock.systemDefaultZone(),
): LocalDateValidator = LocalDateValidatorImpl(name, prev, constraint, clock)

private class LocalDateValidatorImpl(
    private val name: String,
    private val prev: Validator<LocalDate, LocalDate>,
    private val constraint: Constraint<LocalDate>,
    private val clock: Clock,
) : LocalDateValidator {
    private val next: ConstraintValidator<LocalDate> = ConstraintValidator(constraint)

    override fun execute(
        context: ValidationContext,
        input: LocalDate,
    ): ValidationResult<LocalDate> {
        val context = context.copy(logs = context.logs + toString())
        return prev.chain(next).execute(context, input)
    }

    override fun constrain(
        id: String,
        check: ConstraintScope.(ConstraintContext<LocalDate>) -> ConstraintResult,
    ): LocalDateValidator = LocalDateValidatorImpl(name = id, prev = this, constraint = Constraint(id, check), clock = clock)

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

    override fun toString(): String = "${LocalDateValidator::class.simpleName}(name=$name)"
}
