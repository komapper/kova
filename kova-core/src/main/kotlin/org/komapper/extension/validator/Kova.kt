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

    fun uInt(): UIntValidator = UIntValidator()

    fun uLong(): ULongValidator = ULongValidator()

    fun uByte(): UByteValidator = UByteValidator()

    fun uShort(): UShortValidator = UShortValidator()

    fun localDate(clock: Clock = Clock.systemDefaultZone()): LocalDateValidator = LocalDateValidator(clock = clock)

    fun <E> collection(): CollectionValidator<E, Collection<E>> = CollectionValidator()

    fun <E> list(): CollectionValidator<E, List<E>> = CollectionValidator()

    fun <E> set(): CollectionValidator<E, Set<E>> = CollectionValidator()

    fun <K, V> map(): MapValidator<K, V> = MapValidator()

    fun <K, V> mapEntry(): MapEntryValidator<K, V> = MapEntryValidator()

    fun <T> generic(): Validator<T, T> = EmptyValidator()

    fun <T : Any> nullable(): NullableValidator<T, T> = NullableValidator("nullable", generic())

    fun <T : Any> nullable(defaultValue: T): WithDefaultNullableValidator<T, T> = nullable { defaultValue }

    fun <T : Any> nullable(withDefault: () -> T): WithDefaultNullableValidator<T, T> =
        WithDefaultNullableValidator("nullable", generic<T>().asNullable(withDefault))

    fun <T : Any> literal(
        value: T,
        message: MessageProvider1<T, T> = Message.resource1("kova.literal.single"),
    ): Validator<T, T> = LiteralValidator<T>().single(value, message)

    fun <T : Any> literal(
        values: List<T>,
        message: MessageProvider1<T, List<T>> = Message.resource1("kova.literal.list"),
    ): Validator<T, T> = LiteralValidator<T>().list(values.toList(), message)

    fun <T : Any> literal(
        vararg values: T,
        message: MessageProvider1<T, List<T>> = Message.resource1("kova.literal.list"),
    ): Validator<T, T> = literal(values.toList(), message)

    fun fail(content: String): Nothing = fail(Message.Text(content))

    fun fail(message: Message): Nothing = throw MessageException(message)

    companion object : Kova
}
