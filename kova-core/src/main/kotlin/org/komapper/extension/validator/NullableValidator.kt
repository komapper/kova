package org.komapper.extension.validator

import org.komapper.extension.validator.ValidationResult.Success

typealias NullableValidator<T, S> = Validator<T?, S?>

internal fun <T : Any, S : Any> NullableValidator(
    name: String,
    after: NullableValidator<T, S>,
    constraint: Constraint<T?> = Constraint.satisfied(),
): NullableValidator<T, S> =
    Validator { input, context ->
        val before = ConstraintValidator(constraint)
        before.then(after).execute(input, context)
    }

fun <T : Any, S : Any> Validator<T, S>.asNullable(): NullableValidator<T, S> {
    val self = this
    // convert Validator<T, S> to NullableValidator<T, S>
    val wrapped =
        Validator<T?, S?> { input, context ->
            val context = context.addLog("Validator.asNullable")
            if (input == null) Success(null, context) else self.execute(input, context)
        }
    return NullableValidator("asNullable", wrapped)
}

fun <T : Any, S : Any> NullableValidator<T, S>.constrain(
    id: String,
    check: ConstraintScope.(ConstraintContext<T?>) -> ConstraintResult,
): NullableValidator<T, S> = NullableValidator("constrain", after = this, constraint = Constraint(id, check))

fun <T : Any, S : Any> NullableValidator<T, S>.isNull(
    message: MessageProvider0<T?> = Message.resource0("kova.nullable.isNull"),
): NullableValidator<T, S> = constrain(message.id, Constraints.isNull(message))

fun <T : Any, S : Any> NullableValidator<T, S>.notNull(
    message: MessageProvider0<T?> = Message.resource0("kova.nullable.notNull"),
): NullableValidator<T, S> = constrain(message.id, Constraints.notNull(message))

fun <T : Any, S : Any> NullableValidator<T, S>.toNonNullable(): Validator<T?, S> = notNull().map { it!! }

fun <T : Any, S : Any> NullableValidator<T, S>.withDefault(defaultValue: S): WithDefaultNullableValidator<T, S> =
    withDefault { defaultValue }

fun <T : Any, S : Any> NullableValidator<T, S>.withDefault(provide: () -> S): WithDefaultNullableValidator<T, S> =
    WithDefaultNullableValidator(
        "withDefault",
        map { it ?: provide() },
    )

operator fun <T : Any, S : Any> NullableValidator<T, S>.plus(other: Validator<T, S>): NullableValidator<T, S> = and(other)

fun <T : Any, S : Any> NullableValidator<T, S>.and(other: Validator<T, S>): NullableValidator<T, S> = and(other.asNullable())

fun <T : Any, S : Any> NullableValidator<T, S>.or(other: Validator<T, S>): NullableValidator<T, S> = or(other.asNullable())

fun <T : Any, S : Any, U : Any> NullableValidator<T, S>.compose(other: Validator<U, T>): NullableValidator<U, S> =
    compose(other.asNullable())

fun <T : Any, S : Any, U : Any> NullableValidator<T, S>.then(other: Validator<S, U>): NullableValidator<T, U> = then(other.asNullable())
