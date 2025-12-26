package org.komapper.extension.validator.factory

import org.komapper.extension.validator.Accumulate
import org.komapper.extension.validator.Validation

context(_: Validation, _: Accumulate)
fun <A, B, C> buildTriple(
    buildFirst: context(Validation, Accumulate) () -> A,
    buildSecond: context(Validation, Accumulate) () -> B,
    buildThird: context(Validation, Accumulate) () -> C,
): Triple<A, B, C> =
    factory("kotlin.Triple") {
        val first by bind { buildFirst() }
        val second by bind { buildSecond() }
        val third by bind { buildThird() }
        Triple(first, second, third)
    }
