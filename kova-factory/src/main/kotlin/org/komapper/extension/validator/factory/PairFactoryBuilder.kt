package org.komapper.extension.validator.factory

import org.komapper.extension.validator.Accumulate
import org.komapper.extension.validator.Validation
import org.komapper.extension.validator.ValidationResult

context(_: Validation, _: Accumulate)
fun <A, B> buildPair(
    buildFirst: context(Validation, Accumulate) () -> ValidationResult<A>,
    buildSecond: context(Validation, Accumulate) () -> ValidationResult<B>,
): ValidationResult<Pair<A, B>> =
    factory("kotlin.Pair") {
        val first by buildFirst
        val second by buildSecond
        create { Pair(first(), second()) }
    }
