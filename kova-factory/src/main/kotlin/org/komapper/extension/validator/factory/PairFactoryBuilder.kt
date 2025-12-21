package org.komapper.extension.validator.factory

import org.komapper.extension.validator.Kova
import org.komapper.extension.validator.Validator

class PairFactoryBuilder<A, RA, B, RB>(
    val firstValidator: Validator<A, RA>,
    val secondValidator: Validator<B, RB>,
) {
    fun build(
        first: A,
        second: B,
    ): Factory<Pair<RA, RB>> =
        Kova.factory("kotlin.Pair") {
            val first by bind(first) { firstValidator }
            val second by bind(second) { secondValidator }
            create { Pair(first(), second()) }
        }
}
