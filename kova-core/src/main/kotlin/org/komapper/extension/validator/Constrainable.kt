package org.komapper.extension.validator

interface Constrainable<T, R> {
    fun constrain(
        key: String,
        check: ConstraintScope.(ConstraintContext<T>) -> ConstraintResult,
    ): R
}
