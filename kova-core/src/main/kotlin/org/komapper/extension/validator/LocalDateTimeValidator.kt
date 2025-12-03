package org.komapper.extension.validator

import java.time.Clock
import java.time.LocalDateTime

interface LocalDateTimeValidator :
    Validator<LocalDateTime, LocalDateTime>,
    Constrainable<LocalDateTime, LocalDateTimeValidator> {
    fun min(
        value: LocalDateTime,
        message: MessageProvider1<LocalDateTime, LocalDateTime> = Message.resource1("kova.localDateTime.min"),
    ): LocalDateTimeValidator

    fun max(
        value: LocalDateTime,
        message: MessageProvider1<LocalDateTime, LocalDateTime> = Message.resource1("kova.localDateTime.max"),
    ): LocalDateTimeValidator

    fun gt(
        value: LocalDateTime,
        message: MessageProvider1<LocalDateTime, LocalDateTime> = Message.resource1("kova.localDateTime.gt"),
    ): LocalDateTimeValidator

    fun gte(
        value: LocalDateTime,
        message: MessageProvider1<LocalDateTime, LocalDateTime> = Message.resource1("kova.localDateTime.gte"),
    ): LocalDateTimeValidator

    fun lt(
        value: LocalDateTime,
        message: MessageProvider1<LocalDateTime, LocalDateTime> = Message.resource1("kova.localDateTime.lt"),
    ): LocalDateTimeValidator

    fun lte(
        value: LocalDateTime,
        message: MessageProvider1<LocalDateTime, LocalDateTime> = Message.resource1("kova.localDateTime.lte"),
    ): LocalDateTimeValidator

    fun future(message: MessageProvider0<LocalDateTime> = Message.resource0("kova.localDateTime.future")): LocalDateTimeValidator

    fun futureOrPresent(
        message: MessageProvider0<LocalDateTime> = Message.resource0("kova.localDateTime.futureOrPresent"),
    ): LocalDateTimeValidator

    fun past(message: MessageProvider0<LocalDateTime> = Message.resource0("kova.localDateTime.past")): LocalDateTimeValidator

    fun pastOrPresent(
        message: MessageProvider0<LocalDateTime> = Message.resource0("kova.localDateTime.pastOrPresent"),
    ): LocalDateTimeValidator

    operator fun plus(other: Validator<LocalDateTime, LocalDateTime>): LocalDateTimeValidator

    infix fun and(other: Validator<LocalDateTime, LocalDateTime>): LocalDateTimeValidator

    infix fun or(other: Validator<LocalDateTime, LocalDateTime>): LocalDateTimeValidator

    fun chain(other: Validator<LocalDateTime, LocalDateTime>): LocalDateTimeValidator
}

fun LocalDateTimeValidator(
    name: String = "empty",
    prev: Validator<LocalDateTime, LocalDateTime> = EmptyValidator(),
    constraint: Constraint<LocalDateTime> = Constraint.satisfied(),
    clock: Clock = Clock.systemDefaultZone(),
): LocalDateTimeValidator = LocalDateTimeValidatorImpl(name, prev, constraint, clock)

private class LocalDateTimeValidatorImpl(
    private val name: String,
    private val prev: Validator<LocalDateTime, LocalDateTime>,
    private val constraint: Constraint<LocalDateTime>,
    private val clock: Clock,
) : LocalDateTimeValidator {
    private val next: ConstraintValidator<LocalDateTime> = ConstraintValidator(constraint)

    override fun execute(
        input: LocalDateTime,
        context: ValidationContext,
    ): ValidationResult<LocalDateTime> {
        val context = context.addLog(toString())
        return prev.chain(next).execute(input, context)
    }

    override fun constrain(
        id: String,
        check: ConstraintScope.(ConstraintContext<LocalDateTime>) -> ConstraintResult,
    ): LocalDateTimeValidator = LocalDateTimeValidatorImpl(name = id, prev = this, constraint = Constraint(id, check), clock = clock)

    override fun min(
        value: LocalDateTime,
        message: MessageProvider1<LocalDateTime, LocalDateTime>,
    ): LocalDateTimeValidator = constrain(message.key, Constraints.min(value, message))

    override fun max(
        value: LocalDateTime,
        message: MessageProvider1<LocalDateTime, LocalDateTime>,
    ): LocalDateTimeValidator = constrain(message.key, Constraints.max(value, message))

    override fun gt(
        value: LocalDateTime,
        message: MessageProvider1<LocalDateTime, LocalDateTime>,
    ): LocalDateTimeValidator = constrain(message.key, Constraints.gt(value, message))

    override fun gte(
        value: LocalDateTime,
        message: MessageProvider1<LocalDateTime, LocalDateTime>,
    ): LocalDateTimeValidator = constrain(message.key, Constraints.gte(value, message))

    override fun lt(
        value: LocalDateTime,
        message: MessageProvider1<LocalDateTime, LocalDateTime>,
    ): LocalDateTimeValidator = constrain(message.key, Constraints.lt(value, message))

    override fun lte(
        value: LocalDateTime,
        message: MessageProvider1<LocalDateTime, LocalDateTime>,
    ): LocalDateTimeValidator = constrain(message.key, Constraints.lte(value, message))

    override fun future(message: MessageProvider0<LocalDateTime>): LocalDateTimeValidator =
        constrain(message.key) {
            satisfies(it.input > LocalDateTime.now(clock), message(it))
        }

    override fun futureOrPresent(message: MessageProvider0<LocalDateTime>): LocalDateTimeValidator =
        constrain(message.key) {
            satisfies(it.input >= LocalDateTime.now(clock), message(it))
        }

    override fun past(message: MessageProvider0<LocalDateTime>): LocalDateTimeValidator =
        constrain(message.key) {
            satisfies(it.input < LocalDateTime.now(clock), message(it))
        }

    override fun pastOrPresent(message: MessageProvider0<LocalDateTime>): LocalDateTimeValidator =
        constrain(message.key) {
            satisfies(it.input <= LocalDateTime.now(clock), message(it))
        }

    override operator fun plus(other: Validator<LocalDateTime, LocalDateTime>): LocalDateTimeValidator = and(other)

    override fun and(other: Validator<LocalDateTime, LocalDateTime>): LocalDateTimeValidator {
        val combined = (this as Validator<LocalDateTime, LocalDateTime>).and(other)
        return LocalDateTimeValidatorImpl("and", prev = combined, constraint = Constraint.satisfied(), clock = clock)
    }

    override fun or(other: Validator<LocalDateTime, LocalDateTime>): LocalDateTimeValidator {
        val combined = (this as Validator<LocalDateTime, LocalDateTime>).or(other)
        return LocalDateTimeValidatorImpl("or", prev = combined, constraint = Constraint.satisfied(), clock = clock)
    }

    override fun chain(other: Validator<LocalDateTime, LocalDateTime>): LocalDateTimeValidator {
        val combined = (this as Validator<LocalDateTime, LocalDateTime>).chain(other)
        return LocalDateTimeValidatorImpl("chain", prev = combined, constraint = Constraint.satisfied(), clock = clock)
    }

    override fun toString(): String = "${LocalDateTimeValidator::class.simpleName}(name=$name)"
}
