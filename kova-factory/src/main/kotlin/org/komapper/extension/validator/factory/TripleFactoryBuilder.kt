package org.komapper.extension.validator.factory

import org.komapper.extension.validator.Validation

context(_: Validation)
fun <A, B, C> buildTriple(
    buildFirst: context(Validation)() -> A,
    buildSecond: context(Validation)() -> B,
    buildThird: context(Validation)() -> C,
): Triple<A, B, C> =
    factory("kotlin.Triple") {
        val first by bind { buildFirst() }
        val second by bind { buildSecond() }
        val third by bind { buildThird() }
        Triple(first, second, third)
    }
