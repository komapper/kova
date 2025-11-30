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

    fun <T : Any, S: Any> notNullThen(next: Validator<T, S>, message: ((ConstraintContext<T?>) -> Message)? = null): Validator<T?, S?> =
        with(this) {
            val notNull = if (message == null) nullable<T>().notNull() else nullable<T>().notNull(message)
            notNull.notNullThen(next)
        }

    fun error(message: Message): Nothing = throw MessageException(message)

    fun <A1, B1> args(arg1: Validator<A1, B1>) = Arguments1(arg1)

    fun <A1, B1, A2, B2> args(
        arg1: Validator<A1, B1>,
        arg2: Validator<A2, B2>,
    ) = Arguments2(arg1, arg2)

    fun <A1, B1, A2, B2, A3, B3> args(
        arg1: Validator<A1, B1>,
        arg2: Validator<A2, B2>,
        arg3: Validator<A3, B3>,
    ) = Arguments3(arg1, arg2, arg3)

    fun <A1, B1, A2, B2, A3, B3, A4, B4> args(
        arg1: Validator<A1, B1>,
        arg2: Validator<A2, B2>,
        arg3: Validator<A3, B3>,
        arg4: Validator<A4, B4>,
    ) = Arguments4(arg1, arg2, arg3, arg4)

    fun <A1, B1, A2, B2, A3, B3, A4, B4, A5, B5> args(
        arg1: Validator<A1, B1>,
        arg2: Validator<A2, B2>,
        arg3: Validator<A3, B3>,
        arg4: Validator<A4, B4>,
        arg5: Validator<A5, B5>,
    ) = Arguments5(arg1, arg2, arg3, arg4, arg5)

    fun <A1, B1, A2, B2, A3, B3, A4, B4, A5, B5, A6, B6> args(
        arg1: Validator<A1, B1>,
        arg2: Validator<A2, B2>,
        arg3: Validator<A3, B3>,
        arg4: Validator<A4, B4>,
        arg5: Validator<A5, B5>,
        arg6: Validator<A6, B6>,
    ) = Arguments6(arg1, arg2, arg3, arg4, arg5, arg6)

    fun <A1, B1, A2, B2, A3, B3, A4, B4, A5, B5, A6, B6, A7, B7> args(
        arg1: Validator<A1, B1>,
        arg2: Validator<A2, B2>,
        arg3: Validator<A3, B3>,
        arg4: Validator<A4, B4>,
        arg5: Validator<A5, B5>,
        arg6: Validator<A6, B6>,
        arg7: Validator<A7, B7>,
    ) = Arguments7(arg1, arg2, arg3, arg4, arg5, arg6, arg7)

    fun <A1, B1, A2, B2, A3, B3, A4, B4, A5, B5, A6, B6, A7, B7, A8, B8> args(
        arg1: Validator<A1, B1>,
        arg2: Validator<A2, B2>,
        arg3: Validator<A3, B3>,
        arg4: Validator<A4, B4>,
        arg5: Validator<A5, B5>,
        arg6: Validator<A6, B6>,
        arg7: Validator<A7, B7>,
        arg8: Validator<A8, B8>,
    ) = Arguments8(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8)

    fun <A1, B1, A2, B2, A3, B3, A4, B4, A5, B5, A6, B6, A7, B7, A8, B8, A9, B9> args(
        arg1: Validator<A1, B1>,
        arg2: Validator<A2, B2>,
        arg3: Validator<A3, B3>,
        arg4: Validator<A4, B4>,
        arg5: Validator<A5, B5>,
        arg6: Validator<A6, B6>,
        arg7: Validator<A7, B7>,
        arg8: Validator<A8, B8>,
        arg9: Validator<A9, B9>,
    ) = Arguments9(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9)

    fun <A1, B1, A2, B2, A3, B3, A4, B4, A5, B5, A6, B6, A7, B7, A8, B8, A9, B9, A10, B10> args(
        arg1: Validator<A1, B1>,
        arg2: Validator<A2, B2>,
        arg3: Validator<A3, B3>,
        arg4: Validator<A4, B4>,
        arg5: Validator<A5, B5>,
        arg6: Validator<A6, B6>,
        arg7: Validator<A7, B7>,
        arg8: Validator<A8, B8>,
        arg9: Validator<A9, B9>,
        arg10: Validator<A10, B10>,
    ) = Arguments10(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10)

    companion object : Kova
}
