package org.komapper.extension.validator

class StringValidator internal constructor(
    private val prev: Validator<String, String> = EmptyValidator(),
    private val transform: (String) -> String = { it },
    constraint: Constraint<String> = Constraint.satisfied(),
) : Validator<String, String>,
    Constrainable<String, StringValidator> {
    private val next: ConstraintValidator<String> = ConstraintValidator(constraint)

    override fun execute(
        context: ValidationContext,
        input: String,
    ): ValidationResult<String> = prev.map(transform).chain(next).execute(context, input)

    override fun constrain(
        key: String,
        check: ConstraintScope.(ConstraintContext<String>) -> ConstraintResult,
    ): StringValidator = StringValidator(prev = this, constraint = Constraint(key, check))

    fun modify(transform: (String) -> String): StringValidator = StringValidator(prev = this, transform = transform)

    fun min(
        length: Int,
        message: (ConstraintContext<String>, Int) -> Message = Message.resource1(),
    ): StringValidator =
        constrain("kova.charSequence.min") {
            satisfies(it.input.length >= length, message(it, length))
        }

    fun max(
        length: Int,
        message: (ConstraintContext<String>, Int) -> Message = Message.resource1(),
    ): StringValidator =
        constrain("kova.charSequence.max") {
            satisfies(it.input.length <= length, message(it, length))
        }

    fun isBlank(message: (ConstraintContext<String>) -> Message = Message.resource0()): StringValidator =
        constrain("kova.charSequence.isBlank") {
            satisfies(it.input.isBlank(), message(it))
        }

    fun isNotBlank(message: (ConstraintContext<String>) -> Message = Message.resource0()): StringValidator =
        constrain("kova.charSequence.isNotBlank") {
            satisfies(it.input.isNotBlank(), message(it))
        }

    fun isEmpty(message: (ConstraintContext<String>) -> Message = Message.resource0()): StringValidator =
        constrain("kova.charSequence.isEmpty") {
            satisfies(it.input.isEmpty(), message(it))
        }

    fun isNotEmpty(message: (ConstraintContext<String>) -> Message = Message.resource0()): StringValidator =
        constrain("kova.charSequence.isNotEmpty") {
            satisfies(it.input.isNotEmpty(), message(it))
        }

    fun length(
        length: Int,
        message: (ConstraintContext<String>, Int) -> Message = Message.resource1(),
    ): StringValidator =
        constrain("kova.charSequence.length") {
            satisfies(it.input.length == length, message(it, length))
        }

    fun startsWith(
        prefix: CharSequence,
        message: (ConstraintContext<String>, CharSequence) -> Message = Message.resource1(),
    ): StringValidator =
        constrain("kova.charSequence.startsWith") {
            satisfies(it.input.startsWith(prefix), message(it, prefix))
        }

    fun endsWith(
        suffix: CharSequence,
        message: (ConstraintContext<String>, CharSequence) -> Message = Message.resource1(),
    ): StringValidator =
        constrain("kova.charSequence.endsWith") {
            satisfies(it.input.endsWith(suffix), message(it, suffix))
        }

    fun contains(
        infix: CharSequence,
        message: (ConstraintContext<String>, CharSequence) -> Message = Message.resource1(),
    ) = constrain("kova.charSequence.contains") {
        satisfies(it.input.contains(infix), message(it, infix))
    }

    fun isInt(message: (ConstraintContext<String>) -> Message = Message.resource0()): StringValidator =
        constrain("kova.charSequence.isInt") {
            satisfies(it.input.toString().toIntOrNull() != null, message(it))
        }

    fun uppercase(message: (ConstraintContext<String>) -> Message = Message.resource0()): StringValidator =
        constrain("kova.charSequence.uppercase") {
            satisfies(it.input.toString() == it.input.toString().uppercase(), message(it))
        }

    fun lowercase(message: (ConstraintContext<String>) -> Message = Message.resource0()): StringValidator =
        constrain("kova.charSequence.lowercase") {
            satisfies(it.input.toString() == it.input.toString().lowercase(), message(it))
        }

    fun literal(
        value: CharSequence,
        message: (ConstraintContext<String>, CharSequence) -> Message = Message.resource1(),
    ): StringValidator =
        constrain("kova.charSequence.literal") {
            satisfies(it.input.contentEquals(value), message(it, value))
        }

    fun literals(
        values: List<CharSequence>,
        message: (ConstraintContext<String>, List<CharSequence>) -> Message = Message.resource1(),
    ): StringValidator =
        constrain("kova.charSequence.literals") { ctx ->
            satisfies(values.any { it.contentEquals(ctx.input) }, message(ctx, values))
        }

    fun trim() = modify { it.trim() }

    fun toUpperCase() = modify { it.uppercase() }

    fun toLowerCase() = modify { it.lowercase() }
}

fun StringValidator.toInt(): Validator<String, Int> = isInt().map { it.toInt() }
