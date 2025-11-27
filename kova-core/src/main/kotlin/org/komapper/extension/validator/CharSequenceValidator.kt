package org.komapper.extension.validator

class CharSequenceValidator<T : CharSequence> internal constructor(
    private val delegate: CoreValidator<T> = CoreValidator(),
) : Validator<T, T> by delegate {
    operator fun plus(other: CharSequenceValidator<T>): CharSequenceValidator<T> = CharSequenceValidator(delegate + other.delegate)

    fun constraint(
        key: String,
        check: ConstraintScope.(ConstraintContext<T>) -> ConstraintResult,
    ): CharSequenceValidator<T> = CharSequenceValidator(delegate + Constraint(key, check))

    fun min(
        length: Int,
        message: (ConstraintContext<T>, Int) -> Message = Message.resource1(),
    ): CharSequenceValidator<T> =
        constraint("kova.charSequence.min") {
            satisfies(it.input.length >= length, message(it, length))
        }

    fun max(
        length: Int,
        message: (ConstraintContext<T>, Int) -> Message = Message.resource1(),
    ): CharSequenceValidator<T> =
        constraint("kova.charSequence.max") {
            satisfies(it.input.length <= length, message(it, length))
        }

    fun isBlank(message: (ConstraintContext<T>) -> Message = Message.resource0()): CharSequenceValidator<T> =
        constraint("kova.charSequence.isBlank") {
            satisfies(it.input.isBlank(), message(it))
        }

    fun isNotBlank(message: (ConstraintContext<T>) -> Message = Message.resource0()): CharSequenceValidator<T> =
        constraint("kova.charSequence.isNotBlank") {
            satisfies(it.input.isNotBlank(), message(it))
        }

    fun isEmpty(message: (ConstraintContext<T>) -> Message = Message.resource0()): CharSequenceValidator<T> =
        constraint("kova.charSequence.isEmpty") {
            satisfies(it.input.isEmpty(), message(it))
        }

    fun isNotEmpty(message: (ConstraintContext<T>) -> Message = Message.resource0()): CharSequenceValidator<T> =
        constraint("kova.charSequence.isNotEmpty") {
            satisfies(it.input.isNotEmpty(), message(it))
        }

    fun length(
        length: Int,
        message: (ConstraintContext<T>, Int) -> Message = Message.resource1(),
    ): CharSequenceValidator<T> =
        constraint("kova.charSequence.length") {
            satisfies(it.input.length == length, message(it, length))
        }

    fun startsWith(
        prefix: CharSequence,
        message: (ConstraintContext<T>, CharSequence) -> Message = Message.resource1(),
    ): CharSequenceValidator<T> =
        constraint("kova.charSequence.startsWith") {
            satisfies(it.input.startsWith(prefix), message(it, prefix))
        }

    fun endsWith(
        suffix: CharSequence,
        message: (ConstraintContext<T>, CharSequence) -> Message = Message.resource1(),
    ): CharSequenceValidator<T> =
        constraint("kova.charSequence.endsWith") {
            satisfies(it.input.endsWith(suffix), message(it, suffix))
        }

    fun contains(
        infix: CharSequence,
        message: (ConstraintContext<T>, CharSequence) -> Message = Message.resource1(),
    ) = constraint("kova.charSequence.contains") {
        satisfies(it.input.contains(infix), message(it, infix))
    }

    fun isInt(message: (ConstraintContext<T>) -> Message = Message.resource0()): CharSequenceValidator<T> =
        constraint("kova.charSequence.isInt") {
            satisfies(it.input.toString().toIntOrNull() != null, message(it))
        }

    fun literal(
        value: CharSequence,
        message: (ConstraintContext<T>, CharSequence) -> Message = Message.resource1(),
    ): CharSequenceValidator<T> =
        constraint("kova.charSequence.literal") {
            satisfies(it.input.contentEquals(value), message(it, value))
        }

    fun literals(
        values: List<CharSequence>,
        message: (ConstraintContext<T>, List<CharSequence>) -> Message = Message.resource1(),
    ): CharSequenceValidator<T> =
        constraint("kova.charSequence.literals") { ctx ->
            satisfies(values.any { it.contentEquals(ctx.input) }, message(ctx, values))
        }
}

fun <T : CharSequence> CharSequenceValidator<T>.toInt(): Validator<T, Int> = isInt().map { it.toString().toInt() }
