package org.komapper.extension.validator.factory

import org.komapper.extension.validator.Validation
import org.komapper.extension.validator.ValidationResult

context(_: Validation)
fun <A, B, C> buildTriple(
    buildFirst: context(Validation) () -> ValidationResult<A>,
    buildSecond: context(Validation) () -> ValidationResult<B>,
    buildThird: context(Validation) () -> ValidationResult<C>,
): ValidationResult<Triple<A, B, C>> =
    factory("kotlin.Triple") {
        val first by buildFirst
        val second by buildSecond
        val third by buildThird
        create { Triple(first(), second(), third()) }
    }
