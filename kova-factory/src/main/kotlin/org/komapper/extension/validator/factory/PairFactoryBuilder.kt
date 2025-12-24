package org.komapper.extension.validator.factory

import org.komapper.extension.validator.Validation
import org.komapper.extension.validator.ValidationResult

context(_: Validation)
fun <A, B> buildPair(
    buildFirst: context(Validation) () -> ValidationResult<A>,
    buildSecond: context(Validation) () -> ValidationResult<B>,
): ValidationResult<Pair<A, B>> =
    factory("kotlin.Pair") {
        val first by buildFirst
        val second by buildSecond
        create { Pair(first(), second()) }
    }
