package org.komapper.extension.validator

import org.komapper.extension.validator.ValidationResult.Success

typealias WithDefaultNullableValidator<T, S> = Validator<T?, S>

internal fun <T : Any, S : Any> WithDefaultNullableValidator(
    name: String,
    after: WithDefaultNullableValidator<T, S>,
    constraint: Constraint<T?> = Constraint.satisfied(),
): WithDefaultNullableValidator<T, S> =
    Validator { input, context ->
        val before = ConstraintValidator(constraint)
        before.then(after).execute(input, context)
    }

fun <T : Any, S : Any> Validator<T, S>.asNullable(defaultValue: S): WithDefaultNullableValidator<T, S> = asNullable { defaultValue }

fun <T : Any, S : Any> Validator<T, S>.asNullable(withDefault: () -> S): WithDefaultNullableValidator<T, S> {
    val self = this
    // convert Validator<T, S> to WithDefaultNullableValidator<T, S>
    val wrapped =
        WithDefaultNullableValidator<T, S> { input, context ->
            val defaultValue = withDefault()
            val context = context.addLog("Validator.asNullable(defaultValue=$defaultValue)")
            if (input == null) Success(defaultValue, context) else self.execute(input, context)
        }
    return WithDefaultNullableValidator("asNullable", wrapped)
}

fun <T : Any, S : Any> WithDefaultNullableValidator<T, S>.constrain(
    id: String,
    check: ConstraintScope.(ConstraintContext<T?>) -> ConstraintResult,
): WithDefaultNullableValidator<T, S> = WithDefaultNullableValidator("constrain", after = this, constraint = Constraint(id, check))

fun <T : Any, S : Any> WithDefaultNullableValidator<T, S>.isNull(
    message: MessageProvider0<T?> = Message.resource0("kova.nullable.isNull"),
): WithDefaultNullableValidator<T, S> = constrain(message.id, Constraints.isNull(message))

fun <T : Any, S : Any> WithDefaultNullableValidator<T, S>.notNull(
    message: MessageProvider0<T?> = Message.resource0("kova.nullable.notNull"),
): WithDefaultNullableValidator<T, S> = constrain(message.id, Constraints.notNull(message))

fun <T : Any, S : Any> WithDefaultNullableValidator<T, S>.toNonNullable(): Validator<T?, S> = map { it }
