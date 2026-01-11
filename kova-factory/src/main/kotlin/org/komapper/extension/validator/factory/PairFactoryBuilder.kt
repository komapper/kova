package org.komapper.extension.validator.factory

import org.komapper.extension.validator.Validation

context(_: Validation)
public fun <A, B> buildPair(
    buildFirst: context(Validation)() -> A,
    buildSecond: context(Validation)() -> B,
): Pair<A, B> =
    factory("kotlin.Pair") {
        val first by buildFirst
        val second by buildSecond
        Pair(first, second)
    }
