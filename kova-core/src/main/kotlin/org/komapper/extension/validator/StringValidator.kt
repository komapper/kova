package org.komapper.extension.validator

open class StringValidator internal constructor(
    // TODO
    private val constraint: Constraint<String> = Constraint("kova.charSequence") { ConstraintResult.Satisfied },
) : Validator<String, String> {
    override fun execute(
        context: ValidationContext,
        input: String,
    ): ValidationResult<String> {
        // TODO
        return CoreValidator(constraint).execute(context, input)
    }

    fun constraint(
        key: String,
        check: ConstraintScope.(ConstraintContext<String>) -> ConstraintResult,
    ): StringValidator {
        val self = this
        return object : StringValidator(Constraint(key, check)) {
            override fun execute(
                context: ValidationContext,
                input: String,
            ): ValidationResult<String> =
                chain(self, context, input, { it }) { context, input ->
                    super.execute(context, input)
                }
        }
    }

    fun modify(transform: (String) -> String): StringValidator {
        val self = this
        return object : StringValidator() {
            override fun execute(
                context: ValidationContext,
                input: String,
            ): ValidationResult<String> =
                chain(self, context, input, transform) { context, input ->
                    super.execute(context, transform(input))
                }
        }
    }

    fun min(
        length: Int,
        message: (ConstraintContext<String>, Int) -> Message = Message.resource1(),
    ): StringValidator =
        constraint("kova.charSequence.min") {
            satisfies(it.input.length >= length, message(it, length))
        }

    fun max(
        length: Int,
        message: (ConstraintContext<String>, Int) -> Message = Message.resource1(),
    ): StringValidator =
        constraint("kova.charSequence.max") {
            satisfies(it.input.length <= length, message(it, length))
        }

    fun isBlank(message: (ConstraintContext<String>) -> Message = Message.resource0()): StringValidator =
        constraint("kova.charSequence.isBlank") {
            satisfies(it.input.isBlank(), message(it))
        }

    fun isNotBlank(message: (ConstraintContext<String>) -> Message = Message.resource0()): StringValidator =
        constraint("kova.charSequence.isNotBlank") {
            satisfies(it.input.isNotBlank(), message(it))
        }

    fun isEmpty(message: (ConstraintContext<String>) -> Message = Message.resource0()): StringValidator =
        constraint("kova.charSequence.isEmpty") {
            satisfies(it.input.isEmpty(), message(it))
        }

    fun isNotEmpty(message: (ConstraintContext<String>) -> Message = Message.resource0()): StringValidator =
        constraint("kova.charSequence.isNotEmpty") {
            satisfies(it.input.isNotEmpty(), message(it))
        }

    fun length(
        length: Int,
        message: (ConstraintContext<String>, Int) -> Message = Message.resource1(),
    ): StringValidator =
        constraint("kova.charSequence.length") {
            satisfies(it.input.length == length, message(it, length))
        }

    fun startsWith(
        prefix: CharSequence,
        message: (ConstraintContext<String>, CharSequence) -> Message = Message.resource1(),
    ): StringValidator =
        constraint("kova.charSequence.startsWith") {
            satisfies(it.input.startsWith(prefix), message(it, prefix))
        }

    fun endsWith(
        suffix: CharSequence,
        message: (ConstraintContext<String>, CharSequence) -> Message = Message.resource1(),
    ): StringValidator =
        constraint("kova.charSequence.endsWith") {
            satisfies(it.input.endsWith(suffix), message(it, suffix))
        }

    fun contains(
        infix: CharSequence,
        message: (ConstraintContext<String>, CharSequence) -> Message = Message.resource1(),
    ) = constraint("kova.charSequence.contains") {
        satisfies(it.input.contains(infix), message(it, infix))
    }

    fun isInt(message: (ConstraintContext<String>) -> Message = Message.resource0()): StringValidator =
        constraint("kova.charSequence.isInt") {
            satisfies(it.input.toString().toIntOrNull() != null, message(it))
        }

    fun uppercase(message: (ConstraintContext<String>) -> Message = Message.resource0()): StringValidator =
        constraint("kova.charSequence.uppercase") {
            satisfies(it.input.toString() == it.input.toString().uppercase(), message(it))
        }

    fun lowercase(message: (ConstraintContext<String>) -> Message = Message.resource0()): StringValidator =
        constraint("kova.charSequence.lowercase") {
            satisfies(it.input.toString() == it.input.toString().lowercase(), message(it))
        }

    fun literal(
        value: CharSequence,
        message: (ConstraintContext<String>, CharSequence) -> Message = Message.resource1(),
    ): StringValidator =
        constraint("kova.charSequence.literal") {
            satisfies(it.input.contentEquals(value), message(it, value))
        }

    fun literals(
        values: List<CharSequence>,
        message: (ConstraintContext<String>, List<CharSequence>) -> Message = Message.resource1(),
    ): StringValidator =
        constraint("kova.charSequence.literals") { ctx ->
            satisfies(values.any { it.contentEquals(ctx.input) }, message(ctx, values))
        }

    fun trim() = modify { it.trim() }
}

fun StringValidator.toInt(): Validator<String, Int> = isInt().map { it.toInt() }

fun <T> chain(
    before: Validator<T, T>,
    context: ValidationContext,
    input: T,
    transform: (T) -> T,
    next: (ValidationContext, T) -> ValidationResult<T>,
): ValidationResult<T> =
    when (val result = before.execute(context, input)) {
        is ValidationResult.Success -> {
            next(result.context, transform(result.value))
        }

        is ValidationResult.Failure -> {
            if (context.failFast) {
                result
            } else {
                result + next(context, transform(input))
            }
        }
    }
