package org.komapper.extension.validator.factory

import org.komapper.extension.validator.Accumulate
import org.komapper.extension.validator.Validation

context(_: Validation, _: Accumulate)
fun <A, B> buildPair(
    buildFirst: context(Validation, Accumulate) () -> A,
    buildSecond: context(Validation, Accumulate) () -> B,
): Pair<A, B> =
    factory("kotlin.Pair") {
        val first by buildFirst
        val second by buildSecond
        Pair(first, second)
    }
