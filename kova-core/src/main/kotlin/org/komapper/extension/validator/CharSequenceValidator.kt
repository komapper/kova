package org.komapper.extension.validator

class CharSequenceValidator<T : CharSequence> internal constructor(
    private val delegate: CoreValidator<T> = CoreValidator(),
) : Validator<T, T> by delegate {
    operator fun plus(other: CharSequenceValidator<T>): CharSequenceValidator<T> = CharSequenceValidator(delegate + other.delegate)

    fun constraint(constraint: Constraint<T>): CharSequenceValidator<T> = CharSequenceValidator(delegate + constraint)

    fun min(
        length: Int,
        message: (ConstraintContext<T>, Int) -> Message = Message.resource1("kova.charSequence.min"),
    ): CharSequenceValidator<T> =
        constraint {
            Constraint.satisfies(it.input.length >= length, message(it, length))
        }

    fun max(
        length: Int,
        message: (ConstraintContext<T>, Int) -> Message = Message.resource1("kova.charSequence.max"),
    ): CharSequenceValidator<T> =
        constraint {
            Constraint.satisfies(it.input.length <= length, message(it, length))
        }

    fun isBlank(message: (ConstraintContext<T>) -> Message = Message.resource0("kova.charSequence.isBlank")): CharSequenceValidator<T> =
        constraint {
            Constraint.satisfies(it.input.isBlank(), message(it))
        }

    fun isNotBlank(
        message: (ConstraintContext<T>) -> Message = Message.resource0("kova.charSequence.isNotBlank"),
    ): CharSequenceValidator<T> =
        constraint {
            Constraint.satisfies(it.input.isNotBlank(), message(it))
        }

    fun isEmpty(message: (ConstraintContext<T>) -> Message = Message.resource0("kova.charSequence.isEmpty")): CharSequenceValidator<T> =
        constraint {
            Constraint.satisfies(it.input.isEmpty(), message(it))
        }

    fun isNotEmpty(
        message: (ConstraintContext<T>) -> Message = Message.resource0("kova.charSequence.isNotEmpty"),
    ): CharSequenceValidator<T> =
        constraint {
            Constraint.satisfies(it.input.isNotEmpty(), message(it))
        }

    fun length(
        length: Int,
        message: (ConstraintContext<T>, Int) -> Message = Message.resource1("kova.charSequence.length"),
    ): CharSequenceValidator<T> =
        constraint {
            Constraint.satisfies(it.input.length == length, message(it, length))
        }

    fun startsWith(
        prefix: CharSequence,
        message: (ConstraintContext<T>, CharSequence) -> Message = Message.resource1("kova.charSequence.startsWith"),
    ): CharSequenceValidator<T> =
        constraint {
            Constraint.satisfies(it.input.startsWith(prefix), message(it, prefix))
        }

    fun endsWith(
        suffix: CharSequence,
        message: (ConstraintContext<T>, CharSequence) -> Message = Message.resource1("kova.charSequence.endsWith"),
    ): CharSequenceValidator<T> =
        constraint {
            Constraint.satisfies(it.input.endsWith(suffix), message(it, suffix))
        }

    fun contains(
        infix: CharSequence,
        message: (ConstraintContext<T>, CharSequence) -> Message = Message.resource1("kova.charSequence.contains"),
    ) = constraint {
        Constraint.satisfies(it.input.contains(infix), message(it, infix))
    }

    fun isInt(message: (ConstraintContext<T>) -> Message = Message.resource0("kova.charSequence.isInt")): CharSequenceValidator<T> =
        constraint {
            Constraint.satisfies(it.input.toString().toIntOrNull() != null, message(it))
        }

    fun literal(
        value: CharSequence,
        message: (ConstraintContext<T>, CharSequence) -> Message = Message.resource1("kova.charSequence.literal"),
    ): CharSequenceValidator<T> =
        constraint {
            Constraint.satisfies(it.input.contentEquals(value), message(it, value))
        }

    fun literals(
        values: List<CharSequence>,
        message: (ConstraintContext<T>, List<CharSequence>) -> Message = Message.resource1("kova.charSequence.literals"),
    ): CharSequenceValidator<T> =
        constraint { ctx ->
            Constraint.satisfies(values.any { it.contentEquals(ctx.input) }, message(ctx, values))
        }
}

fun <T : CharSequence> CharSequenceValidator<T>.toInt(): Validator<T, Int> = isInt().map { it.toString().toInt() }
