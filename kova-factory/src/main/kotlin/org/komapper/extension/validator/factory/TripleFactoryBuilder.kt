package org.komapper.extension.validator.factory

import org.komapper.extension.validator.Validation

fun <A, B, C> Validation.buildTriple(
    buildFirst: Validation.() -> A,
    buildSecond: Validation.() -> B,
    buildThird: Validation.() -> C,
): Triple<A, B, C> =
    factory("kotlin.Triple") {
        val first by bind { buildFirst() }
        val second by bind { buildSecond() }
        val third by bind { buildThird() }
        Triple(first, second, third)
    }
