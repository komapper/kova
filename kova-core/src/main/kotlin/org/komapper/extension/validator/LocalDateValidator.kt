package org.komapper.extension.validator

import java.time.Clock
import java.time.LocalDate

interface LocalDateValidator :
    Validator<LocalDate, LocalDate>,
    Constrainable<LocalDate, LocalDateValidator> {
    fun future(message: MessageProvider0<LocalDate> = Message.resource0("kova.localDate.future")): LocalDateValidator

    fun futureOrPresent(message: MessageProvider0<LocalDate> = Message.resource0("kova.localDate.futureOrPresent")): LocalDateValidator

    fun past(message: MessageProvider0<LocalDate> = Message.resource0("kova.localDate.past")): LocalDateValidator

    fun pastOrPresent(message: MessageProvider0<LocalDate> = Message.resource0("kova.localDate.pastOrPresent")): LocalDateValidator
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
        input: LocalDate,
        context: ValidationContext,
    ): ValidationResult<LocalDate> {
        val context = context.addLog(toString())
        return prev.chain(next).execute(input, context)
    }

    override fun constrain(
        id: String,
        check: ConstraintScope.(ConstraintContext<LocalDate>) -> ConstraintResult,
    ): LocalDateValidator = LocalDateValidatorImpl(name = id, prev = this, constraint = Constraint(id, check), clock = clock)

    override fun future(message: MessageProvider0<LocalDate>): LocalDateValidator =
        constrain(message.key) {
            satisfies(it.input > LocalDate.now(clock), message(it))
        }

    override fun futureOrPresent(message: MessageProvider0<LocalDate>): LocalDateValidator =
        constrain(message.key) {
            satisfies(it.input >= LocalDate.now(clock), message(it))
        }

    override fun past(message: MessageProvider0<LocalDate>): LocalDateValidator =
        constrain(message.key) {
            satisfies(it.input < LocalDate.now(clock), message(it))
        }

    override fun pastOrPresent(message: MessageProvider0<LocalDate>): LocalDateValidator =
        constrain(message.key) {
            satisfies(it.input <= LocalDate.now(clock), message(it))
        }

    override fun toString(): String = "${LocalDateValidator::class.simpleName}(name=$name)"
}
