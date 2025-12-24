package org.komapper.extension.validator.factory

import org.komapper.extension.validator.ValidationContext
import org.komapper.extension.validator.ValidationResult

context(_: ValidationContext)
fun <A, B, C> buildTriple(
    buildFirst: context(ValidationContext) () -> ValidationResult<A>,
    buildSecond: context(ValidationContext) () -> ValidationResult<B>,
    buildThird: context(ValidationContext) () -> ValidationResult<C>,
): ValidationResult<Triple<A, B, C>> =
    factory("kotlin.Triple") {
        val first by buildFirst
        val second by buildSecond
        val third by buildThird
        create { Triple(first(), second(), third()) }
    }
