package org.komapper.extension.validator

interface Constrainable<T, R> {
    fun constrain(
        id: String,
        check: ConstraintScope.(ConstraintContext<T>) -> ConstraintResult,
    ): R
}
