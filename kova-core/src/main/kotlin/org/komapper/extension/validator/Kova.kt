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

    fun localDate(clock: Clock): LocalDateValidator = LocalDateValidator(clock = clock)

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

    fun error(message: Message): Nothing = throw MessageException(message)

    fun <A1, B1> args(arg1: Validator<A1, B1>) = Argument1(arg1)

    fun <A1, B1, A2, B2> args(
        arg1: Validator<A1, B1>,
        arg2: Validator<A2, B2>,
    ) = Argument2(arg1, arg2)

    companion object : Kova
}
