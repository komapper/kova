package org.komapper.extension.validator

import java.time.Clock
import java.time.LocalTime

interface LocalTimeValidator :
    Validator<LocalTime, LocalTime>,
    Constrainable<LocalTime, LocalTimeValidator> {
    fun min(
        value: LocalTime,
        message: MessageProvider1<LocalTime, LocalTime> = Message.resource1("kova.localTime.min"),
    ): LocalTimeValidator

    fun max(
        value: LocalTime,
        message: MessageProvider1<LocalTime, LocalTime> = Message.resource1("kova.localTime.max"),
    ): LocalTimeValidator

    fun gt(
        value: LocalTime,
        message: MessageProvider1<LocalTime, LocalTime> = Message.resource1("kova.localTime.gt"),
    ): LocalTimeValidator

    fun gte(
        value: LocalTime,
        message: MessageProvider1<LocalTime, LocalTime> = Message.resource1("kova.localTime.gte"),
    ): LocalTimeValidator

    fun lt(
        value: LocalTime,
        message: MessageProvider1<LocalTime, LocalTime> = Message.resource1("kova.localTime.lt"),
    ): LocalTimeValidator

    fun lte(
        value: LocalTime,
        message: MessageProvider1<LocalTime, LocalTime> = Message.resource1("kova.localTime.lte"),
    ): LocalTimeValidator

    fun future(message: MessageProvider0<LocalTime> = Message.resource0("kova.localTime.future")): LocalTimeValidator

    fun futureOrPresent(message: MessageProvider0<LocalTime> = Message.resource0("kova.localTime.futureOrPresent")): LocalTimeValidator

    fun past(message: MessageProvider0<LocalTime> = Message.resource0("kova.localTime.past")): LocalTimeValidator

    fun pastOrPresent(message: MessageProvider0<LocalTime> = Message.resource0("kova.localTime.pastOrPresent")): LocalTimeValidator

    operator fun plus(other: Validator<LocalTime, LocalTime>): LocalTimeValidator

    infix fun and(other: Validator<LocalTime, LocalTime>): LocalTimeValidator

    infix fun or(other: Validator<LocalTime, LocalTime>): LocalTimeValidator

    fun chain(other: Validator<LocalTime, LocalTime>): LocalTimeValidator
}

fun LocalTimeValidator(
    name: String = "empty",
    prev: Validator<LocalTime, LocalTime> = EmptyValidator(),
    constraint: Constraint<LocalTime> = Constraint.satisfied(),
    clock: Clock = Clock.systemDefaultZone(),
): LocalTimeValidator = LocalTimeValidatorImpl(name, prev, constraint, clock)

private class LocalTimeValidatorImpl(
    private val name: String,
    private val prev: Validator<LocalTime, LocalTime>,
    private val constraint: Constraint<LocalTime>,
    private val clock: Clock,
) : LocalTimeValidator {
    private val next: ConstraintValidator<LocalTime> = ConstraintValidator(constraint)

    override fun execute(
        input: LocalTime,
        context: ValidationContext,
    ): ValidationResult<LocalTime> {
        val context = context.addLog(toString())
        return prev.chain(next).execute(input, context)
    }

    override fun constrain(
        id: String,
        check: ConstraintScope.(ConstraintContext<LocalTime>) -> ConstraintResult,
    ): LocalTimeValidator = LocalTimeValidatorImpl(name = id, prev = this, constraint = Constraint(id, check), clock = clock)

    override fun min(
        value: LocalTime,
        message: MessageProvider1<LocalTime, LocalTime>,
    ): LocalTimeValidator = constrain(message.key, Constraints.min(value, message))

    override fun max(
        value: LocalTime,
        message: MessageProvider1<LocalTime, LocalTime>,
    ): LocalTimeValidator = constrain(message.key, Constraints.max(value, message))

    override fun gt(
        value: LocalTime,
        message: MessageProvider1<LocalTime, LocalTime>,
    ): LocalTimeValidator = constrain(message.key, Constraints.gt(value, message))

    override fun gte(
        value: LocalTime,
        message: MessageProvider1<LocalTime, LocalTime>,
    ): LocalTimeValidator = constrain(message.key, Constraints.gte(value, message))

    override fun lt(
        value: LocalTime,
        message: MessageProvider1<LocalTime, LocalTime>,
    ): LocalTimeValidator = constrain(message.key, Constraints.lt(value, message))

    override fun lte(
        value: LocalTime,
        message: MessageProvider1<LocalTime, LocalTime>,
    ): LocalTimeValidator = constrain(message.key, Constraints.lte(value, message))

    override fun future(message: MessageProvider0<LocalTime>): LocalTimeValidator =
        constrain(message.key) {
            satisfies(it.input > LocalTime.now(clock), message(it))
        }

    override fun futureOrPresent(message: MessageProvider0<LocalTime>): LocalTimeValidator =
        constrain(message.key) {
            satisfies(it.input >= LocalTime.now(clock), message(it))
        }

    override fun past(message: MessageProvider0<LocalTime>): LocalTimeValidator =
        constrain(message.key) {
            satisfies(it.input < LocalTime.now(clock), message(it))
        }

    override fun pastOrPresent(message: MessageProvider0<LocalTime>): LocalTimeValidator =
        constrain(message.key) {
            satisfies(it.input <= LocalTime.now(clock), message(it))
        }

    override operator fun plus(other: Validator<LocalTime, LocalTime>): LocalTimeValidator = and(other)

    override fun and(other: Validator<LocalTime, LocalTime>): LocalTimeValidator {
        val combined = (this as Validator<LocalTime, LocalTime>).and(other)
        return LocalTimeValidatorImpl("and", prev = combined, constraint = Constraint.satisfied(), clock = clock)
    }

    override fun or(other: Validator<LocalTime, LocalTime>): LocalTimeValidator {
        val combined = (this as Validator<LocalTime, LocalTime>).or(other)
        return LocalTimeValidatorImpl("or", prev = combined, constraint = Constraint.satisfied(), clock = clock)
    }

    override fun chain(other: Validator<LocalTime, LocalTime>): LocalTimeValidator {
        val combined = (this as Validator<LocalTime, LocalTime>).chain(other)
        return LocalTimeValidatorImpl("chain", prev = combined, constraint = Constraint.satisfied(), clock = clock)
    }

    override fun toString(): String = "${LocalTimeValidator::class.simpleName}(name=$name)"
}
