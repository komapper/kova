package org.komapper.extension.validator

import java.math.BigDecimal
import java.math.BigInteger

interface Kova {
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

    fun <E> collection(): CollectionValidator<E, Collection<E>> = CollectionValidator()

    fun <E> list(): CollectionValidator<E, List<E>> = CollectionValidator()

    fun <E> set(): CollectionValidator<E, Set<E>> = CollectionValidator()

    fun <K, V> map(): MapValidator<K, V> = MapValidator()

    fun <K, V> mapEntry(): MapEntryValidator<K, V> = MapEntryValidator()

    fun <T> generic(): Validator<T, T> = EmptyValidator()

    fun <T : Any> nullable(): NullableValidator<T, T> = NullableValidator(generic())

    fun <E : Enum<E>> enum(): EnumValidator<E> = EnumValidator()

    fun <T : Any> factory(block: KovaFactoryScope.() -> T): T = block(KovaFactoryScope)

    fun error(message: Message): Nothing = throw MessageException(message)

    companion object : Kova
}

@DslMarker
annotation class KovaMarker

@KovaMarker
interface KovaFactoryScope {
    operator fun <T : Any, R, B1> ((B1) -> T).invoke(block: ObjectConstructor1<T, B1>.() -> R): R {
        val ctor = ObjectConstructor1(this)
        return block(ctor)
    }

    operator fun <T : Any, R, B1, B2> ((B1, B2) -> T).invoke(block: ObjectConstructor2<T, B1, B2>.() -> R): R {
        val ctor = ObjectConstructor2(this)
        return block(ctor)
    }

    companion object : KovaFactoryScope
}
