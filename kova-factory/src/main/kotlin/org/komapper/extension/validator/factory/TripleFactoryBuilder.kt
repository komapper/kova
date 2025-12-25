package org.komapper.extension.validator.factory

import org.komapper.extension.validator.Accumulate
import org.komapper.extension.validator.Validation
import org.komapper.extension.validator.ValidationResult

context(_: Validation, _: Accumulate)
fun <A, B, C> buildTriple(
    buildFirst: context(Validation, Accumulate) () -> ValidationResult<A>,
    buildSecond: context(Validation, Accumulate) () -> ValidationResult<B>,
    buildThird: context(Validation, Accumulate) () -> ValidationResult<C>,
): ValidationResult<Triple<A, B, C>> =
    factory("kotlin.Triple") {
        val first by bind { buildFirst() }
        val second by bind { buildSecond() }
        val third by bind { buildThird() }
        create { Triple(first(), second(), third()) }
    }
