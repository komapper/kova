package org.komapper.extension.validator.factory

import org.komapper.extension.validator.Kova
import org.komapper.extension.validator.Validator

class TripleFactoryBuilder<A, RA, B, RB, C, RC>(
    val firstValidator: Validator<A, RA>,
    val secondValidator: Validator<B, RB>,
    val thirdValidator: Validator<C, RC>,
) {
    fun build(
        first: A,
        second: B,
        third: C,
    ): Factory<Triple<RA, RB, RC>> =
        Kova.factory("kotlin.Triple") {
            val first by bind(first) { firstValidator }
            val second by bind(second) { secondValidator }
            val third by bind(third) { thirdValidator }
            create { Triple(first(), second(), third()) }
        }
}
