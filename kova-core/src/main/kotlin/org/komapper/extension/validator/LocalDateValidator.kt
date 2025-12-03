package org.komapper.extension.validator

import java.time.Clock
import java.time.LocalDate

interface LocalDateValidator :
    Validator<LocalDate, LocalDate>,
    Constrainable<LocalDate, LocalDateValidator> {
    fun min(
        value: LocalDate,
        message: MessageProvider1<LocalDate, LocalDate> = Message.resource1("kova.localDate.min"),
    ): LocalDateValidator

    fun max(
        value: LocalDate,
        message: MessageProvider1<LocalDate, LocalDate> = Message.resource1("kova.localDate.max"),
    ): LocalDateValidator

    fun gt(
        value: LocalDate,
        message: MessageProvider1<LocalDate, LocalDate> = Message.resource1("kova.localDate.gt"),
    ): LocalDateValidator

    fun gte(
        value: LocalDate,
        message: MessageProvider1<LocalDate, LocalDate> = Message.resource1("kova.localDate.gte"),
    ): LocalDateValidator

    fun lt(
        value: LocalDate,
        message: MessageProvider1<LocalDate, LocalDate> = Message.resource1("kova.localDate.lt"),
    ): LocalDateValidator

    fun lte(
        value: LocalDate,
        message: MessageProvider1<LocalDate, LocalDate> = Message.resource1("kova.localDate.lte"),
    ): LocalDateValidator

    fun future(message: MessageProvider0<LocalDate> = Message.resource0("kova.localDate.future")): LocalDateValidator

    fun futureOrPresent(message: MessageProvider0<LocalDate> = Message.resource0("kova.localDate.futureOrPresent")): LocalDateValidator

    fun past(message: MessageProvider0<LocalDate> = Message.resource0("kova.localDate.past")): LocalDateValidator

    fun pastOrPresent(message: MessageProvider0<LocalDate> = Message.resource0("kova.localDate.pastOrPresent")): LocalDateValidator

    operator fun plus(other: Validator<LocalDate, LocalDate>): LocalDateValidator

    infix fun and(other: Validator<LocalDate, LocalDate>): LocalDateValidator

    infix fun or(other: Validator<LocalDate, LocalDate>): LocalDateValidator

    fun chain(other: Validator<LocalDate, LocalDate>): LocalDateValidator
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

    override fun min(
        value: LocalDate,
        message: MessageProvider1<LocalDate, LocalDate>,
    ): LocalDateValidator = constrain(message.key, Constraints.min(value, message))

    override fun max(
        value: LocalDate,
        message: MessageProvider1<LocalDate, LocalDate>,
    ): LocalDateValidator = constrain(message.key, Constraints.max(value, message))

    override fun gt(
        value: LocalDate,
        message: MessageProvider1<LocalDate, LocalDate>,
    ): LocalDateValidator = constrain(message.key, Constraints.gt(value, message))

    override fun gte(
        value: LocalDate,
        message: MessageProvider1<LocalDate, LocalDate>,
    ): LocalDateValidator = constrain(message.key, Constraints.gte(value, message))

    override fun lt(
        value: LocalDate,
        message: MessageProvider1<LocalDate, LocalDate>,
    ): LocalDateValidator = constrain(message.key, Constraints.lt(value, message))

    override fun lte(
        value: LocalDate,
        message: MessageProvider1<LocalDate, LocalDate>,
    ): LocalDateValidator = constrain(message.key, Constraints.lte(value, message))

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

    override operator fun plus(other: Validator<LocalDate, LocalDate>): LocalDateValidator = and(other)

    override fun and(other: Validator<LocalDate, LocalDate>): LocalDateValidator {
        val combined = (this as Validator<LocalDate, LocalDate>).and(other)
        return LocalDateValidatorImpl("and", prev = combined, constraint = Constraint.satisfied(), clock = clock)
    }

    override fun or(other: Validator<LocalDate, LocalDate>): LocalDateValidator {
        val combined = (this as Validator<LocalDate, LocalDate>).or(other)
        return LocalDateValidatorImpl("or", prev = combined, constraint = Constraint.satisfied(), clock = clock)
    }

    override fun chain(other: Validator<LocalDate, LocalDate>): LocalDateValidator {
        val combined = (this as Validator<LocalDate, LocalDate>).chain(other)
        return LocalDateValidatorImpl("chain", prev = combined, constraint = Constraint.satisfied(), clock = clock)
    }

    override fun toString(): String = "${LocalDateValidator::class.simpleName}(name=$name)"
}
