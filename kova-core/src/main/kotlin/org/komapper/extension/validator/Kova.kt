package org.komapper.extension.validator

import java.math.BigDecimal
import java.math.BigInteger
import java.time.Clock

interface Kova {
    fun boolean(): Validator<Boolean, Boolean> = generic()

    fun string(): StringValidator = StringValidator()

    fun int(): NumberValidator<Int> = NumberValidator()

    fun long(): NumberValidator<Long> = NumberValidator()

    fun double(): NumberValidator<Double> = NumberValidator()

    fun float(): NumberValidator<Float> = NumberValidator()

    fun byte(): NumberValidator<Byte> = NumberValidator()

    fun short(): NumberValidator<Short> = NumberValidator()

    fun bigDecimal(): NumberValidator<BigDecimal> = NumberValidator()

    fun bigInteger(): NumberValidator<BigInteger> = NumberValidator()

    fun uInt(): ComparableValidator<UInt> = ComparableValidator()

    fun uLong(): ComparableValidator<ULong> = ComparableValidator()

    fun uByte(): ComparableValidator<UByte> = ComparableValidator()

    fun uShort(): ComparableValidator<UShort> = ComparableValidator()

    fun localDate(clock: Clock = Clock.systemDefaultZone()): LocalDateValidator = LocalDateValidator(clock = clock)

    fun <E> collection(): CollectionValidator<E, Collection<E>> = CollectionValidator()

    fun <E> list(): CollectionValidator<E, List<E>> = CollectionValidator()

    fun <E> set(): CollectionValidator<E, Set<E>> = CollectionValidator()

    fun <K, V> map(): MapValidator<K, V> = MapValidator()

    fun <K, V> mapEntry(): MapEntryValidator<K, V> = MapEntryValidator()

    fun <T> generic(): Validator<T, T> = EmptyValidator()

    fun <T : Any> nullable(): NullableValidator<T, T> = NullableValidator(generic())

    fun <T : Any> literal(
        value: T,
        message: (ConstraintContext<T>, T) -> Message = Message.resource1(),
    ): Validator<T, T> = LiteralValidator<T>().single(value, message)

    fun <T : Any> literal(
        values: List<T>,
        message: (ConstraintContext<T>, List<T>) -> Message = Message.resource1(),
    ): Validator<T, T> = LiteralValidator<T>().list(values.toList(), message)

    fun <T : Any> literal(
        vararg values: T,
        message: (ConstraintContext<T>, List<T>) -> Message = Message.resource1(),
    ): Validator<T, T> = literal(values.toList(), message)

    fun <T : Any> isNull(message: ((ConstraintContext<T?>) -> Message)? = null): NullableValidator<T, T> =
        with(this) {
            if (message == null) nullable<T>().isNull() else nullable<T>().isNull(message)
        }

    fun <T : Any> isNullOr(
        value: T,
        message: ((ConstraintContext<T?>) -> Message)? = null,
    ): NullableValidator<T, T> =
        with(this) {
            if (message == null) nullable<T>().isNullOr(literal(value)) else nullable<T>().isNullOr(literal(value), message)
        }

    fun <T : Any> notNull(message: ((ConstraintContext<T?>) -> Message)? = null): NullableValidator<T, T> =
        with(this) {
            if (message == null) nullable<T>().notNull() else nullable<T>().notNull(message)
        }

    fun <T : Any, S : Any> notNullThen(
        next: Validator<T, S>,
        message: ((ConstraintContext<T?>) -> Message)? = null,
    ): Validator<T?, S?> =
        with(this) {
            val notNull = if (message == null) nullable<T>().notNull() else nullable<T>().notNull(message)
            notNull.notNullThen(next)
        }

    fun error(message: Message): Nothing = throw MessageException(message)

    fun <IN, OUT> arg(
        validator: Validator<IN, OUT>,
        value: IN,
    ): Arg<OUT> = Arg.Value(validator, value)

    fun <IN, OUT> arg(
        validator: Validator<IN, OUT>,
        factory: ObjectFactory<IN>,
    ): Arg<OUT> = Arg.Factory(validator, factory)

    fun <T> arguments(arg1: Arg<T>) = Arguments1(arg1)

    fun <T1, T2> arguments(
        arg1: Arg<T1>,
        arg2: Arg<T2>,
    ) = Arguments2(arg1, arg2)

    fun <T1, T2, T3> arguments(
        arg1: Arg<T1>,
        arg2: Arg<T2>,
        arg3: Arg<T3>,
    ) = Arguments3(arg1, arg2, arg3)

    fun <T1, T2, T3, T4> arguments(
        arg1: Arg<T1>,
        arg2: Arg<T2>,
        arg3: Arg<T3>,
        arg4: Arg<T4>,
    ) = Arguments4(arg1, arg2, arg3, arg4)

    fun <T1, T2, T3, T4, T5> arguments(
        arg1: Arg<T1>,
        arg2: Arg<T2>,
        arg3: Arg<T3>,
        arg4: Arg<T4>,
        arg5: Arg<T5>,
    ) = Arguments5(arg1, arg2, arg3, arg4, arg5)

    fun <T1, T2, T3, T4, T5, T6> arguments(
        arg1: Arg<T1>,
        arg2: Arg<T2>,
        arg3: Arg<T3>,
        arg4: Arg<T4>,
        arg5: Arg<T5>,
        arg6: Arg<T6>,
    ) = Arguments6(arg1, arg2, arg3, arg4, arg5, arg6)

    fun <T1, T2, T3, T4, T5, T6, T7> arguments(
        arg1: Arg<T1>,
        arg2: Arg<T2>,
        arg3: Arg<T3>,
        arg4: Arg<T4>,
        arg5: Arg<T5>,
        arg6: Arg<T6>,
        arg7: Arg<T7>,
    ) = Arguments7(arg1, arg2, arg3, arg4, arg5, arg6, arg7)

    fun <T1, T2, T3, T4, T5, T6, T7, T8> arguments(
        arg1: Arg<T1>,
        arg2: Arg<T2>,
        arg3: Arg<T3>,
        arg4: Arg<T4>,
        arg5: Arg<T5>,
        arg6: Arg<T6>,
        arg7: Arg<T7>,
        arg8: Arg<T8>,
    ) = Arguments8(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8)

    fun <T1, T2, T3, T4, T5, T6, T7, T8, T9> arguments(
        arg1: Arg<T1>,
        arg2: Arg<T2>,
        arg3: Arg<T3>,
        arg4: Arg<T4>,
        arg5: Arg<T5>,
        arg6: Arg<T6>,
        arg7: Arg<T7>,
        arg8: Arg<T8>,
        arg9: Arg<T9>,
    ) = Arguments9(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9)

    fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> arguments(
        arg1: Arg<T1>,
        arg2: Arg<T2>,
        arg3: Arg<T3>,
        arg4: Arg<T4>,
        arg5: Arg<T5>,
        arg6: Arg<T6>,
        arg7: Arg<T7>,
        arg8: Arg<T8>,
        arg9: Arg<T9>,
        arg10: Arg<T10>,
    ) = Arguments10(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10)

    companion object : Kova
}
