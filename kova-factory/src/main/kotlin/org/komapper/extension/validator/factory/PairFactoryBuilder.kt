package org.komapper.extension.validator.factory

import org.komapper.extension.validator.ValidationContext
import org.komapper.extension.validator.ValidationResult

context(_: ValidationContext)
fun <A, B> buildPair(
    buildFirst: context(ValidationContext) () -> ValidationResult<A>,
    buildSecond: context(ValidationContext) () -> ValidationResult<B>,
): ValidationResult<Pair<A, B>> =
    factory("kotlin.Pair") {
        val first by buildFirst
        val second by buildSecond
        create { Pair(first(), second()) }
    }
