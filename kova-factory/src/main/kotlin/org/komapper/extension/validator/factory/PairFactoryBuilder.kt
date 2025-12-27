package org.komapper.extension.validator.factory

import org.komapper.extension.validator.Validation

fun <A, B> Validation.buildPair(
    buildFirst: Validation.() -> A,
    buildSecond: Validation.() -> B,
): Pair<A, B> =
    factory("kotlin.Pair") {
        val first by buildFirst
        val second by buildSecond
        Pair(first, second)
    }
